# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: e2e\playwright\uc-012.spec.ts >> 8.1 · turnos que requieren más de una operación >> A1 · recoge un hecho y responde una pregunta en el mismo mensaje
- Location: e2e\playwright\uc-012.spec.ts:52:7

# Error details

```
Error: expect(received).toBe(expected) // Object.is equality

Expected: "Anotado"
Received: "Respuesta"
```

# Page snapshot

```yaml
- generic [active] [ref=e1]:
  - main [ref=e2]:
    - generic [ref=e3]:
      - generic [ref=e9]:
        - paragraph [ref=e10]: Careme
        - paragraph [ref=e11]: Historia clínica personal
      - link "Mediciones" [ref=e12] [cursor=pointer]:
        - /url: /measurements
    - generic [ref=e17]:
      - generic [ref=e18]:
        - generic [ref=e19]:
          - paragraph [ref=e20]: Asistente
          - heading "Cuéntame qué quieres dejar registrado." [level=1] [ref=e21]
          - paragraph [ref=e22]: Puedes escribirlo con tus propias palabras. Mantendré la fecha tal como la recuerdas.
        - generic [ref=e23]:
          - generic [ref=e24]: Me diagnosticaron hipertensión hace unos dos años. ¿Qué me han registrado de eso?
          - generic [ref=e26]:
            - generic [ref=e27]: "He anotado lo que me cuentas: «Me diagnosticaron hipertensión hace unos dos años», con la fecha indicada como aproximada y tus propias palabras. Quedará en tu historia cuando cierres la consulta. Sobre lo que tienes registrado de la hipertensión, aparecen dos diagnósticos: - Uno con fecha exacta del 12 de septiembre de 2026, cuyo contenido es «Ayer me diagnosticaron hipertensión» (es decir, el diagnóstico correspondería al día anterior a esa anotación). - Otro con fecha aproximada del 20 de septiembre de 2024, registrado como «Me diagnosticaron hipertensión». Como ves, los dos registros no coinciden entre sí en el tiempo. Si quieres, puedes aclararme cuál refleja mejor lo que viviste y lo dejo anotado tal como me lo indiques."
            - paragraph [ref=e28]: Respuesta
            - list "Operaciones del turno" [ref=e29]:
              - listitem [ref=e30]: Respuesta
              - listitem [ref=e31]: Anotado
            - list "Hechos que sustentan la respuesta" [ref=e32]:
              - listitem [ref=e33]:
                - text: evt_001
                - generic [ref=e34]: · 2026-09-12 (fecha exacta)
                - paragraph [ref=e35]: Ayer me diagnosticaron hipertensión
              - listitem [ref=e36]:
                - text: evt_019
                - generic [ref=e37]: · 2024-09-20 (fecha aproximada)
                - paragraph [ref=e38]: Me diagnosticaron hipertensión
          - generic [ref=e39]:
            - generic [ref=e40]: He registrado un hecho en tu historia clínica. Ya queda disponible para consulta.
            - paragraph [ref=e41]: Registrado
            - list "Hechos que sustentan la respuesta" [ref=e42]:
              - listitem [ref=e43]:
                - text: evt_051
                - generic [ref=e44]: · fecha aproximada
                - paragraph [ref=e45]: Me diagnosticaron hipertensión hace unos dos años.
        - status [ref=e46]:
          - paragraph [ref=e47]: La consulta está cerrada.
          - paragraph [ref=e48]: Ya no se añaden hechos a esta consulta. Puedes empezar una nueva cuando quieras.
          - button "Empezar una nueva consulta" [ref=e49]
      - complementary [ref=e50]:
        - paragraph [ref=e51]: Tu espacio
        - paragraph [ref=e52]: Los hechos registrados pasan a formar parte de tu historia clínica. Las preguntas generales no crean registros.
  - alert [ref=e53]
```

# Test source

```ts
  1   | import { expect, test } from "@playwright/test";
  2   | 
  3   | import {
  4   |   assistantTurns,
  5   |   endConsultation,
  6   |   historySnapshot,
  7   |   replyText,
  8   |   sendTurn,
  9   |   startNewConsultation,
  10  |   statusLabel,
  11  | } from "./helpers";
  12  | 
  13  | /**
  14  |  * The scenarios of UC-012 that need the real interface, the real backend and the real assistant.
  15  |  * They are serial on purpose: they share one clinical history on disk and every turn costs real
  16  |  * provider calls.
  17  |  *
  18  |  * Precondition: the run starts from an **empty** clinical-event directory, because `A1` asserts the
  19  |  * turn added exactly one document and `A3` asserts the two facts of its message produced two. The
  20  |  * backend reconciles its derived index from that directory when it starts, so the directory and the
  21  |  * database are reset together before the run, never while it is running.
  22  |  */
  23  | test.describe.configure({ mode: "serial" });
  24  | 
  25  | /** The reply is for the person, so it never carries transport, provider or credential detail. */
  26  | function expectNoInternalDetail(reply: string) {
  27  |   expect(reply).not.toMatch(/https?:\/\//i);
  28  |   expect(reply).not.toMatch(/openai|api[_ ]key|token/i);
  29  | }
  30  | 
  31  | /**
  32  |  * Closes whatever consultation the scenario left open, so the next one starts from a
  33  |  * history that no earlier turn can still add to: a later conversation closes the
  34  |  * previous consultation, and its notes would otherwise land in the middle of another
  35  |  * scenario's snapshot.
  36  |  */
  37  | test.afterEach(async ({ page }) => {
  38  |   const end = page.getByRole("button", { name: "Terminar consulta" });
  39  |   if ((await end.count()) === 0) {
  40  |     return;
  41  |   }
  42  |   await end.click();
  43  |   await expect(page.getByText("La consulta está cerrada.")).toBeVisible({ timeout: 240_000 });
  44  | });
  45  | 
  46  | /** Documents the history gained, comparing content and not only names. */
  47  | function addedDocuments(before: Record<string, string>, after: Record<string, string>): string[] {
  48  |   return Object.keys(after).filter((name) => !(name in before));
  49  | }
  50  | 
  51  | test.describe("8.1 · turnos que requieren más de una operación", () => {
  52  |   test("A1 · recoge un hecho y responde una pregunta en el mismo mensaje", async ({ page }) => {
  53  |     await page.goto("/");
  54  |     const before = historySnapshot();
  55  | 
  56  |     const turn = await sendTurn(
  57  |       page,
  58  |       "Me diagnosticaron hipertensión hace unos dos años. ¿Qué me han registrado de eso?",
  59  |     );
  60  |     const reply = await replyText(turn);
  61  |     const status = (await statusLabel(turn).innerText()).trim();
  62  |     const after = historySnapshot();
  63  |     const added = addedDocuments(before, after);
  64  | 
  65  |     console.log(`[A1] status=${status} reply_length=${reply.length} nuevos=${added.join(",")}`);
  66  |     console.log(`[A1] reply=${reply}`);
  67  | 
  68  |     expect(reply.length).toBeGreaterThan(0);
  69  |     expectNoInternalDetail(reply);
  70  |     // The history is still empty at this point of the run, so the turn notes the fact
  71  |     // and reports the absence of records instead of an answer.
> 72  |     expect(status).toBe("Anotado");
      |                    ^ Error: expect(received).toBe(expected) // Object.is equality
  73  |     const operations = turn.getByRole("list", { name: "Operaciones del turno" });
  74  |     // The turn reports both operations it went through: the consultation and the note.
  75  |     expect(await operations.locator("li").count()).toBe(2);
  76  |     // The turn only takes note: nothing reaches the clinical history until the close.
  77  |     expect(added).toHaveLength(0);
  78  | 
  79  |     // «el hecho recogido se registra al cerrar la consulta y entonces puede consultarse»
  80  |     await endConsultation(page);
  81  |     expect(addedDocuments(before, historySnapshot())).toHaveLength(1);
  82  | 
  83  |     await startNewConsultation(page);
  84  |     const consultation = await sendTurn(page, "¿Qué consta sobre la hipertensión que te conté?");
  85  |     const consultationStatus = (await statusLabel(consultation).innerText()).trim();
  86  |     const consultations = await consultation.locator('ul[aria-label="Hechos que sustentan la respuesta"] li').count();
  87  | 
  88  |     console.log(`[A1] consulta_inmediata status=${consultationStatus} hechos=${consultations}`);
  89  | 
  90  |     expect(consultationStatus).toBe("Respuesta");
  91  |     expect(consultations).toBeGreaterThan(0);
  92  |   });
  93  | 
  94  |   test("A2 · encadena varias consultas antes de responder", async ({ page }) => {
  95  |     await page.goto("/");
  96  |     const before = historySnapshot();
  97  | 
  98  |     const turn = await sendTurn(
  99  |       page,
  100 |       "Compárame lo que consta sobre mi hipertensión con lo que consta sobre la medicación que tomo.",
  101 |     );
  102 |     const reply = await replyText(turn);
  103 |     const status = (await statusLabel(turn).innerText()).trim();
  104 |     const after = historySnapshot();
  105 | 
  106 |     // The turn consults; it never writes.
  107 |     const operations = await page.getByLabel("Operaciones del turno").count();
  108 |     console.log(`[A2] status=${status} reply_length=${reply.length} listas_de_operaciones=${operations}`);
  109 |     console.log(`[A2] reply=${reply}`);
  110 | 
  111 |     expect(reply.length).toBeGreaterThan(0);
  112 |     expectNoInternalDetail(reply);
  113 |     expect(status).toMatch(/Respuesta|Sin registros/);
  114 |     expect(await assistantTurns(page).count()).toBeGreaterThan(0);
  115 |     expect(after).toEqual(before);
  116 |   });
  117 | 
  118 |   test("A3 · registra varios hechos distintos del mismo mensaje", async ({ page }) => {
  119 |     await page.goto("/");
  120 |     const before = historySnapshot();
  121 | 
  122 |     const turn = await sendTurn(
  123 |       page,
  124 |       "Hoy me diagnosticaron diabetes tipo 2 y me recetaron metformina de 850 mg.",
  125 |     );
  126 |     const reply = await replyText(turn);
  127 |     const status = (await statusLabel(turn).innerText()).trim();
  128 |     const after = historySnapshot();
  129 |     const added = addedDocuments(before, after);
  130 | 
  131 |     console.log(`[A3] status=${status} reply_length=${reply.length} nuevos=${added.join(",")}`);
  132 |     console.log(`[A3] reply=${reply}`);
  133 | 
  134 |     expect(reply.length).toBeGreaterThan(0);
  135 |     expectNoInternalDetail(reply);
  136 |     expect(status).toBe("Anotado");
  137 |     // The facts are collected as notes; the close is what registers them.
  138 |     expect(added).toHaveLength(0);
  139 | 
  140 |     await endConsultation(page);
  141 |     expect(addedDocuments(before, historySnapshot())).toHaveLength(2);
  142 |   });
  143 | });
  144 | 
  145 | test.describe("8.2 · fronteras del turno", () => {
  146 |   test("A7 · no ejecuta ninguna operación cuando el mensaje no las requiere", async ({ page }) => {
  147 |     await page.goto("/");
  148 |     const before = historySnapshot();
  149 | 
  150 |     const turn = await sendTurn(page, "¿Qué es la hipertensión?");
  151 |     const reply = await replyText(turn);
  152 |     const status = (await statusLabel(turn).innerText()).trim();
  153 |     const after = historySnapshot();
  154 | 
  155 |     console.log(`[A7] status=${status} reply_length=${reply.length}`);
  156 |     console.log(`[A7] reply=${reply}`);
  157 | 
  158 |     expect(status).toBe("Conversación");
  159 |     expect(reply.length).toBeGreaterThan(0);
  160 |     expectNoInternalDetail(reply);
  161 |     expect(after).toEqual(before);
  162 |   });
  163 | 
  164 |   test("A8 · declara la parte del mensaje que no puede atenderse", async ({ page }) => {
  165 |     await page.goto("/");
  166 |     const before = historySnapshot();
  167 | 
  168 |     const turn = await sendTurn(page, "¿Cuántos años de vida me quedan?");
  169 |     const reply = await replyText(turn);
  170 |     const status = (await statusLabel(turn).innerText()).trim();
  171 |     const after = historySnapshot();
  172 | 
```