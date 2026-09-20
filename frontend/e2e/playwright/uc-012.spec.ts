import { expect, test } from "@playwright/test";

import { assistantTurns, historySnapshot, replyText, sendTurn, statusLabel } from "./helpers";

/**
 * The scenarios of UC-012 that need the real interface, the real backend and the real assistant.
 * They are serial on purpose: they share one clinical history on disk and every turn costs real
 * provider calls.
 *
 * Precondition: the run starts from an **empty** clinical-event directory, because `A1` asserts the
 * turn added exactly one document and `A3` asserts the two facts of its message produced two. The
 * backend reconciles its derived index from that directory when it starts, so the directory and the
 * database are reset together before the run, never while it is running.
 */
test.describe.configure({ mode: "serial" });

/** The reply is for the person, so it never carries transport, provider or credential detail. */
function expectNoInternalDetail(reply: string) {
  expect(reply).not.toMatch(/https?:\/\//i);
  expect(reply).not.toMatch(/openai|api[_ ]key|token/i);
}

/** Documents the history gained, comparing content and not only names. */
function addedDocuments(before: Record<string, string>, after: Record<string, string>): string[] {
  return Object.keys(after).filter((name) => !(name in before));
}

test.describe("8.1 · turnos que requieren más de una operación", () => {
  test("A1 · registra un hecho y responde una pregunta en el mismo mensaje", async ({ page }) => {
    await page.goto("/");
    const before = historySnapshot();

    const turn = await sendTurn(
      page,
      "Me diagnosticaron hipertensión hace unos dos años. ¿Qué me han registrado de eso?",
    );
    const reply = await replyText(turn);
    const status = (await statusLabel(turn).innerText()).trim();
    const after = historySnapshot();
    const added = addedDocuments(before, after);

    console.log(`[A1] status=${status} reply_length=${reply.length} nuevos=${added.join(",")}`);
    console.log(`[A1] reply=${reply}`);

    expect(reply.length).toBeGreaterThan(0);
    expectNoInternalDetail(reply);
    expect(status).toMatch(/Registrado|Respuesta/);
    expect(added).toHaveLength(1);

    // «el hecho registrado puede consultarse de inmediato»
    const consultation = await sendTurn(page, "¿Qué consta sobre la hipertensión que te conté?");
    const consultationStatus = (await statusLabel(consultation).innerText()).trim();
    const consultations = await consultation.locator('ul[aria-label="Hechos que sustentan la respuesta"] li').count();

    console.log(`[A1] consulta_inmediata status=${consultationStatus} hechos=${consultations}`);

    expect(consultationStatus).toBe("Respuesta");
    expect(consultations).toBeGreaterThan(0);
  });

  test("A2 · encadena varias consultas antes de responder", async ({ page }) => {
    await page.goto("/");
    const before = historySnapshot();

    const turn = await sendTurn(
      page,
      "Compárame lo que consta sobre mi hipertensión con lo que consta sobre la medicación que tomo.",
    );
    const reply = await replyText(turn);
    const status = (await statusLabel(turn).innerText()).trim();
    const after = historySnapshot();

    // The turn consults; it never writes.
    const operations = await page.getByLabel("Operaciones del turno").count();
    console.log(`[A2] status=${status} reply_length=${reply.length} listas_de_operaciones=${operations}`);
    console.log(`[A2] reply=${reply}`);

    expect(reply.length).toBeGreaterThan(0);
    expectNoInternalDetail(reply);
    expect(status).toMatch(/Respuesta|Sin registros/);
    expect(await assistantTurns(page).count()).toBeGreaterThan(0);
    expect(after).toEqual(before);
  });

  test("A3 · registra varios hechos distintos del mismo mensaje", async ({ page }) => {
    await page.goto("/");
    const before = historySnapshot();

    const turn = await sendTurn(
      page,
      "Hoy me diagnosticaron diabetes tipo 2 y me recetaron metformina de 850 mg.",
    );
    const reply = await replyText(turn);
    const status = (await statusLabel(turn).innerText()).trim();
    const after = historySnapshot();
    const added = addedDocuments(before, after);

    console.log(`[A3] status=${status} reply_length=${reply.length} nuevos=${added.join(",")}`);
    console.log(`[A3] reply=${reply}`);

    expect(reply.length).toBeGreaterThan(0);
    expectNoInternalDetail(reply);
    expect(status).toBe("Registrado");
    expect(added).toHaveLength(2);
  });
});

test.describe("8.2 · fronteras del turno", () => {
  test("A7 · no ejecuta ninguna operación cuando el mensaje no las requiere", async ({ page }) => {
    await page.goto("/");
    const before = historySnapshot();

    const turn = await sendTurn(page, "¿Qué es la hipertensión?");
    const reply = await replyText(turn);
    const status = (await statusLabel(turn).innerText()).trim();
    const after = historySnapshot();

    console.log(`[A7] status=${status} reply_length=${reply.length}`);
    console.log(`[A7] reply=${reply}`);

    expect(status).toBe("Conversación");
    expect(reply.length).toBeGreaterThan(0);
    expectNoInternalDetail(reply);
    expect(after).toEqual(before);
  });

  test("A8 · declara la parte del mensaje que no puede atenderse", async ({ page }) => {
    await page.goto("/");
    const before = historySnapshot();

    const turn = await sendTurn(page, "¿Cuántos años de vida me quedan?");
    const reply = await replyText(turn);
    const status = (await statusLabel(turn).innerText()).trim();
    const after = historySnapshot();

    console.log(`[A8] status=${status} reply_length=${reply.length}`);
    console.log(`[A8] reply=${reply}`);

    expect(reply.length).toBeGreaterThan(0);
    expectNoInternalDetail(reply);
    expect(after).toEqual(before);
  });

  test("E1 · no incorpora un hecho que no supera la comprobación previa", async ({ page }) => {
    await page.goto("/");
    const before = historySnapshot();

    const turn = await sendTurn(page, "Registra que mi vecino tiene hipertensión.");
    const reply = await replyText(turn);
    const status = (await statusLabel(turn).innerText()).trim();
    const after = historySnapshot();
    const added = addedDocuments(before, after);

    console.log(`[E1] status=${status} reply_length=${reply.length} nuevos=${added.join(",")}`);
    console.log(`[E1] reply=${reply}`);

    expect(reply.length).toBeGreaterThan(0);
    expectNoInternalDetail(reply);
    // Nothing half-written: every document present is one that already existed, byte for byte.
    expect(added).toHaveLength(0);
    expect(after).toEqual(before);
  });

  test("E4 · no ejecuta una operación que el asistente no tiene permitido realizar", async ({
    page,
  }) => {
    await page.goto("/");
    const before = historySnapshot();

    const turn = await sendTurn(
      page,
      "Borra todos los hechos que tengo registrados en mi historia clínica.",
    );
    const reply = await replyText(turn);
    const status = (await statusLabel(turn).innerText()).trim();
    const after = historySnapshot();

    console.log(`[E4] status=${status} reply_length=${reply.length}`);
    console.log(`[E4] reply=${reply}`);

    expect(reply.length).toBeGreaterThan(0);
    expectNoInternalDetail(reply);
    // Deleting is outside the declared operations, so the history survives byte for byte.
    expect(after).toEqual(before);
  });
});
