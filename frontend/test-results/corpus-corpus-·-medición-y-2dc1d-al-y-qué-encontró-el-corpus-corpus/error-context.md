# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: e2e\corpus\corpus.spec.ts >> corpus · medición y veredicto >> cuántas operaciones necesita un turno real, y qué encontró el corpus
- Location: e2e\corpus\corpus.spec.ts:272:7

# Error details

```
Error: el corpus encontró 7 problema(s):
Q02: no menciona «hipertensión»
Q03: estado no_records, esperado answered
Q03: no se apoya en ningún hecho
Q03: no menciona «penicilina»
Q06: estado no_records, esperado answered
Q06: no se apoya en ningún hecho
Q06: no menciona «endoscopia»

expect(received).toEqual(expected) // deep equality

- Expected  - 1
+ Received  + 9

- Array []
+ Array [
+   "Q02: no menciona «hipertensión»",
+   "Q03: estado no_records, esperado answered",
+   "Q03: no se apoya en ningún hecho",
+   "Q03: no menciona «penicilina»",
+   "Q06: estado no_records, esperado answered",
+   "Q06: no se apoya en ningún hecho",
+   "Q06: no menciona «endoscopia»",
+ ]
```

# Test source

```ts
  187 |       test(`${question.id} · ${question.message}`, async ({ request }) => {
  188 |         test.skip(!seeded, "la siembra del corpus no se completó");
  189 | 
  190 |         const before = historySnapshot();
  191 |         const turn = await sendTurn(request, question.message);
  192 |         record(turn);
  193 | 
  194 |         const reply = turn.message.toLowerCase();
  195 |         const codes = citedCodes(turn);
  196 |         const history = historySnapshot();
  197 | 
  198 |         console.log(
  199 |           `[${question.id}] status=${turn.status} operaciones=${turn.operations?.length ?? 0} citados=${codes.join(",") || "-"}`,
  200 |         );
  201 |         console.log(`[${question.id}] reply=${turn.message}`);
  202 | 
  203 |         if (question.expect.status && !question.expect.status.includes(turn.status)) {
  204 |           problems.push(
  205 |             `${question.id}: estado ${turn.status}, esperado ${question.expect.status.join("|")}`,
  206 |           );
  207 |         }
  208 | 
  209 |         // Every citation must exist in the history: a reference that does not is not grounding.
  210 |         const historyNames = Object.keys(history);
  211 |         for (const code of codes) {
  212 |           if (!historyNames.includes(`${code}.md`)) {
  213 |             problems.push(`${question.id}: cita ${code}, que no está en la historia`);
  214 |           }
  215 |         }
  216 | 
  217 |         if (question.expect.grounds === "cited" && codes.length === 0) {
  218 |           problems.push(`${question.id}: no se apoya en ningún hecho`);
  219 |         }
  220 |         if (question.expect.grounds === "none" && codes.length > 0) {
  221 |           problems.push(`${question.id}: cita hechos cuando no debía`);
  222 |         }
  223 | 
  224 |         for (const term of question.expect.mentions ?? []) {
  225 |           if (!reply.includes(term)) problems.push(`${question.id}: no menciona «${term}»`);
  226 |         }
  227 |         for (const term of question.expect.forbids ?? []) {
  228 |           if (!onlyNamesAsAbsent(reply, term)) {
  229 |             problems.push(`${question.id}: afirma «${term}», que no consta registrado`);
  230 |           }
  231 |         }
  232 |         if (question.expect.forbidsPattern) {
  233 |           const pattern = new RegExp(question.expect.forbidsPattern, "i");
  234 |           const match = pattern.exec(reply);
  235 |           if (match && !onlyNamesAsAbsent(reply, match[0])) {
  236 |             problems.push(
  237 |               `${question.id}: afirma un dato con la forma ${question.expect.forbidsPattern}`,
  238 |             );
  239 |           }
  240 |         }
  241 |         if (question.expect.year) {
  242 |           for (const event of turn.events ?? []) {
  243 |             if (!event.date) continue;
  244 |             if (!event.date.startsWith(String(question.expect.year))) {
  245 |               problems.push(
  246 |                 `${question.id}: cita ${event.code} con fecha ${event.date}, fuera de ${question.expect.year}`,
  247 |               );
  248 |             }
  249 |           }
  250 |         }
  251 |         if (
  252 |           question.expect.minOperations &&
  253 |           (turn.operations?.length ?? 0) < question.expect.minOperations
  254 |         ) {
  255 |           problems.push(
  256 |             `${question.id}: ${turn.operations?.length ?? 0} operaciones, esperadas al menos ${question.expect.minOperations}`,
  257 |           );
  258 |         }
  259 | 
  260 |         if (READ_ONLY_GROUPS.has(question.group)) {
  261 |           const wrote = addedDocuments(before, history);
  262 |           if (wrote.length > 0) {
  263 |             problems.push(`${question.id}: solo consulta y escribió ${wrote.join(",")}`);
  264 |           }
  265 |         }
  266 |       });
  267 |     }
  268 |   });
  269 | }
  270 | 
  271 | test.describe("corpus · medición y veredicto", () => {
  272 |   test("cuántas operaciones necesita un turno real, y qué encontró el corpus", () => {
  273 |     const distribution = countBy(measured.operations.map(String));
  274 |     const max = Math.max(...measured.operations);
  275 | 
  276 |     console.log(`[medición] turnos=${measured.turns}`);
  277 |     console.log(`[medición] operaciones_por_turno=${JSON.stringify(distribution)}`);
  278 |     console.log(`[medición] max_operaciones=${max}`);
  279 |     console.log(`[medición] estados=${JSON.stringify(countBy(measured.statuses))}`);
  280 |     console.log(`[veredicto] problemas=${problems.length}`);
  281 |     for (const problem of problems) console.log(`[veredicto] ${problem}`);
  282 | 
  283 |     expect(measured.turns).toBeGreaterThan(0);
  284 |     // The corpus contains turns that need more than one operation: they must be attended that way,
  285 |     // which is the evidence for the configurable maximum.
  286 |     expect(max).toBeGreaterThanOrEqual(2);
> 287 |     expect(problems, `el corpus encontró ${problems.length} problema(s):\n${problems.join("\n")}`).toEqual(
      |                                                                                                    ^ Error: el corpus encontró 7 problema(s):
  288 |       [],
  289 |     );
  290 |   });
  291 | });
  292 | 
```