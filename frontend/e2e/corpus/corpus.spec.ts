import { randomUUID } from "node:crypto";
import { readFileSync } from "node:fs";
import { join } from "node:path";

import { expect, test, type APIRequestContext } from "@playwright/test";

import { addedDocuments, historySnapshot } from "../support/history";

type Fact = { id: string; group: string; message: string };

type Expectation = {
  status?: string[];
  grounds?: "cited" | "none" | "any";
  mentions?: string[];
  forbids?: string[];
  forbidsPattern?: string;
  year?: number;
  minOperations?: number;
};

type Question = { id: string; group: string; message: string; expect: Expectation };

type EventSummary = {
  code: string;
  type: string;
  content: string;
  date: string | null;
  datePrecision: string;
};

type OperationSummary = { status: string; events?: EventSummary[] };

type ChatResponse = {
  conversationId: string;
  status: string;
  message: string;
  events?: EventSummary[];
  operations?: OperationSummary[];
};

/**
 * The evaluation corpus of the phase (`roadmap_asistente_historia_clinica.md` §4.11): fictional
 * clinical facts, plus an independent set of test questions. The run drives the chat API rather
 * than the browser because the measurements this corpus exists to produce — the reported status and
 * **how many operations a turn needed** — are part of the turn contract and the interface does not
 * show all of them.
 *
 * Precondition: the run starts from an empty clinical history, like the interface scenarios.
 */
const corpus = JSON.parse(readFileSync(join(__dirname, "clinical-history.json"), "utf8")) as {
  facts: Fact[];
  questions: Question[];
};

const CHAT_PATH = `${process.env.E2E_API_BASE_URL ?? "http://localhost:8080"}/api/v1/chat/messages`;

/** The groups whose questions only read: they must leave the history exactly as it was. */
const READ_ONLY_GROUPS = new Set(["retrieval", "temporal", "absence", "no_invention"]);

/** The measurements the corpus exists to produce, read at the end of the run. */
const measured = {
  turns: 0,
  operations: [] as number[],
  statuses: [] as string[],
};

/**
 * Everything the corpus found wrong. An evaluation run reports a scorecard instead of stopping at
 * the first finding, so a single run answers both questions of the task: whether a turn that needs
 * more than one operation is attended completely, and how many operations a real turn needs.
 */
const problems: string[] = [];

/** Whether the facts reached the history; the questions are meaningless without them. */
let seeded = false;

function countBy(values: string[]): Record<string, number> {
  return values.reduce<Record<string, number>>((counts, value) => {
    counts[value] = (counts[value] ?? 0) + 1;
    return counts;
  }, {});
}

async function sendTurn(request: APIRequestContext, message: string): Promise<ChatResponse> {
  const response = await request.post(CHAT_PATH, {
    data: { message, messageId: randomUUID() },
    timeout: 180_000,
  });
  expect(response.ok(), `el endpoint de chat respondió ${response.status()}`).toBeTruthy();
  return (await response.json()) as ChatResponse;
}

function record(turn: ChatResponse) {
  measured.turns += 1;
  measured.operations.push(turn.operations?.length ?? 0);
  measured.statuses.push(turn.status);
}

/** Words that turn a mention into a declaration of absence, so the mention is not an invention. */
const NEGATIONS = [
  "no ",
  "sin ",
  "ni ",
  "ningún",
  "ninguna",
  "tampoco",
  "ausencia",
  // A reply that reports the search it ran lists the terms it tried, which is not an assertion
  // about the history either.
  "buscado",
  "busqué",
  "términos",
  "palabras",
];

/**
 * Whether every mention of the term is part of a declaration of absence. A reply that says «no
 * consta ninguna alergia al gluten» is exactly the behaviour the corpus wants, so the check looks
 * at what surrounds the term instead of forbidding the word outright.
 */
function onlyNamesAsAbsent(reply: string, term: string): boolean {
  const needle = term.toLowerCase();
  let index = reply.indexOf(needle);
  while (index >= 0) {
    const before = reply.slice(Math.max(0, index - 80), index);
    if (!NEGATIONS.some((negation) => before.includes(negation))) return false;
    index = reply.indexOf(needle, index + needle.length);
  }
  return true;
}

/** Every event code the turn presented as support, top-level or inside an operation. */
function citedCodes(turn: ChatResponse): string[] {
  const events = [
    ...(turn.events ?? []),
    ...(turn.operations ?? []).flatMap((operation) => operation.events ?? []),
  ];
  return [...new Set(events.map((event) => event.code))];
}

test.describe("corpus · siembra", () => {
  // Seeding must finish before the questions: they read the history it creates.
  test.describe.configure({ mode: "serial" });

  test("los hechos del corpus se almacenan", async ({ request }) => {
    const before = historySnapshot();
    const threshold = Math.floor(corpus.facts.length * 0.8);

    // A history that is already populated is not seeded again: re-stating the same facts would add
    // duplicate documents and make every re-run cost the full corpus. The questions only need the
    // history to be there.
    if (Object.keys(before).length >= threshold) {
      console.log(
        `[siembra] omitida: la historia ya tiene ${Object.keys(before).length} documentos`,
      );
    } else {
      for (const fact of corpus.facts) {
        const turn = await sendTurn(request, fact.message);
        record(turn);
        expect(turn.status, `${fact.id} no debería fallar`).not.toBe("failed");
      }
    }

    const after = historySnapshot();
    const added = addedDocuments(before, after);
    console.log(
      `[siembra] hechos=${corpus.facts.length} documentos_nuevos=${added.length} total=${Object.keys(after).length}`,
    );
    console.log(`[siembra] estados=${JSON.stringify(countBy(measured.statuses))}`);

    // Measured against the total, not against what this run added, so a run over a history that is
    // already populated still evaluates the questions instead of reporting a false seeding problem.
    if (Object.keys(after).length < threshold) {
      problems.push(
        `siembra: ${Object.keys(after).length} documentos en la historia, esperados al menos ` +
          `${threshold}; comprueba que el backend atendido es el que se arrancó con ` +
          "CAREME_EVENTS_DIRECTORY y que es el único que escucha en ese puerto",
      );
    }
    seeded = true;
  });
});

for (const group of ["retrieval", "temporal", "absence", "multi_operation", "no_invention"]) {
  test.describe(`corpus · ${group}`, () => {
    for (const question of corpus.questions.filter((item) => item.group === group)) {
      test(`${question.id} · ${question.message}`, async ({ request }) => {
        test.skip(!seeded, "la siembra del corpus no se completó");

        const before = historySnapshot();
        const turn = await sendTurn(request, question.message);
        record(turn);

        const reply = turn.message.toLowerCase();
        const codes = citedCodes(turn);
        const history = historySnapshot();

        console.log(
          `[${question.id}] status=${turn.status} operaciones=${turn.operations?.length ?? 0} citados=${codes.join(",") || "-"}`,
        );
        console.log(`[${question.id}] reply=${turn.message}`);

        if (question.expect.status && !question.expect.status.includes(turn.status)) {
          problems.push(
            `${question.id}: estado ${turn.status}, esperado ${question.expect.status.join("|")}`,
          );
        }

        // Every citation must exist in the history: a reference that does not is not grounding.
        const historyNames = Object.keys(history);
        for (const code of codes) {
          if (!historyNames.includes(`${code}.md`)) {
            problems.push(`${question.id}: cita ${code}, que no está en la historia`);
          }
        }

        if (question.expect.grounds === "cited" && codes.length === 0) {
          problems.push(`${question.id}: no se apoya en ningún hecho`);
        }
        if (question.expect.grounds === "none" && codes.length > 0) {
          problems.push(`${question.id}: cita hechos cuando no debía`);
        }

        for (const term of question.expect.mentions ?? []) {
          if (!reply.includes(term)) problems.push(`${question.id}: no menciona «${term}»`);
        }
        for (const term of question.expect.forbids ?? []) {
          if (!onlyNamesAsAbsent(reply, term)) {
            problems.push(`${question.id}: afirma «${term}», que no consta registrado`);
          }
        }
        if (question.expect.forbidsPattern) {
          const pattern = new RegExp(question.expect.forbidsPattern, "i");
          const match = pattern.exec(reply);
          if (match && !onlyNamesAsAbsent(reply, match[0])) {
            problems.push(
              `${question.id}: afirma un dato con la forma ${question.expect.forbidsPattern}`,
            );
          }
        }
        if (question.expect.year) {
          for (const event of turn.events ?? []) {
            if (!event.date) continue;
            if (!event.date.startsWith(String(question.expect.year))) {
              problems.push(
                `${question.id}: cita ${event.code} con fecha ${event.date}, fuera de ${question.expect.year}`,
              );
            }
          }
        }
        if (
          question.expect.minOperations &&
          (turn.operations?.length ?? 0) < question.expect.minOperations
        ) {
          problems.push(
            `${question.id}: ${turn.operations?.length ?? 0} operaciones, esperadas al menos ${question.expect.minOperations}`,
          );
        }

        if (READ_ONLY_GROUPS.has(question.group)) {
          const wrote = addedDocuments(before, history);
          if (wrote.length > 0) {
            problems.push(`${question.id}: solo consulta y escribió ${wrote.join(",")}`);
          }
        }
      });
    }
  });
}

test.describe("corpus · medición y veredicto", () => {
  test("cuántas operaciones necesita un turno real, y qué encontró el corpus", () => {
    const distribution = countBy(measured.operations.map(String));
    const max = Math.max(...measured.operations);

    console.log(`[medición] turnos=${measured.turns}`);
    console.log(`[medición] operaciones_por_turno=${JSON.stringify(distribution)}`);
    console.log(`[medición] max_operaciones=${max}`);
    console.log(`[medición] estados=${JSON.stringify(countBy(measured.statuses))}`);
    console.log(`[veredicto] problemas=${problems.length}`);
    for (const problem of problems) console.log(`[veredicto] ${problem}`);

    expect(measured.turns).toBeGreaterThan(0);
    // The corpus contains turns that need more than one operation: they must be attended that way,
    // which is the evidence for the configurable maximum.
    expect(max).toBeGreaterThanOrEqual(2);
    expect(problems, `el corpus encontró ${problems.length} problema(s):\n${problems.join("\n")}`).toEqual(
      [],
    );
  });
});
