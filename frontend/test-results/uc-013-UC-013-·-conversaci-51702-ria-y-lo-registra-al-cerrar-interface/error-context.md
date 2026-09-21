# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: e2e\playwright\uc-013.spec.ts >> UC-013 · conversación, recogida y cierre >> C1 · recoge un hecho sin tocar la historia y lo registra al cerrar
- Location: e2e\playwright\uc-013.spec.ts:34:7

# Error details

```
Error: expect(received).toHaveLength(expected)

Expected length: 1
Received length: 0
Received array:  []
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
          - generic [ref=e24]: Me diagnosticaron hipertensión hace unos dos años.
          - generic [ref=e26]:
            - generic [ref=e27]: "He anotado el diagnóstico de hipertensión, con la referencia temporal tal como la has expresado: «hace unos dos años», es decir, con fecha aproximada. Quedará en tu historia clínica cuando cierres la consulta. Si recuerdas el año o una fecha más concreta de aquel diagnóstico, dímela y la afino; si hubo tratamiento indicado entonces y quieres que también conste, cuéntamelo y lo anoto."
            - paragraph [ref=e28]: Anotado
          - generic [ref=e29]:
            - generic [ref=e30]: He registrado un hecho en tu historia clínica. Ya queda disponible para consulta.
            - paragraph [ref=e31]: Registrado
            - list "Hechos que sustentan la respuesta" [ref=e32]:
              - listitem [ref=e33]:
                - text: evt_052
                - generic [ref=e34]: · fecha aproximada
                - paragraph [ref=e35]: Me diagnosticaron hipertensión hace unos dos años.
        - status [ref=e36]:
          - paragraph [ref=e37]: La consulta está cerrada.
          - paragraph [ref=e38]: Ya no se añaden hechos a esta consulta. Puedes empezar una nueva cuando quieras.
          - button "Empezar una nueva consulta" [ref=e39]
      - complementary [ref=e40]:
        - paragraph [ref=e41]: Tu espacio
        - paragraph [ref=e42]: Los hechos registrados pasan a formar parte de tu historia clínica. Las preguntas generales no crean registros.
  - alert [ref=e43]
```

# Test source

```ts
  1   | import { expect, test } from "@playwright/test";
  2   | 
  3   | import {
  4   |   assistantTurns,
  5   |   closedNotice,
  6   |   endConsultation,
  7   |   historySnapshot,
  8   |   replyText,
  9   |   sendTurn,
  10  |   startNewConsultation,
  11  |   statusLabel,
  12  | } from "./helpers";
  13  | 
  14  | /**
  15  |  * The scenarios of UC-013 that need the real interface, the real backend and the real assistant:
  16  |  * the conversation collects notes, the clinical history is untouched until the consultation is
  17  |  * closed, and the close is what registers the admissible facts.
  18  |  *
  19  |  * They are serial on purpose: they share one clinical history on disk and every turn costs real
  20  |  * provider calls.
  21  |  *
  22  |  * Precondition: the run starts from an **empty** clinical-event and encounters directory, because
  23  |  * `C1` asserts the close added exactly one document and `C2` asserts a close with no facts added
  24  |  * none.
  25  |  */
  26  | test.describe.configure({ mode: "serial" });
  27  | 
  28  | /** The documents the history gained, comparing content and not only names. */
  29  | function addedDocuments(before: Record<string, string>, after: Record<string, string>): string[] {
  30  |   return Object.keys(after).filter((name) => !(name in before));
  31  | }
  32  | 
  33  | test.describe("UC-013 · conversación, recogida y cierre", () => {
  34  |   test("C1 · recoge un hecho sin tocar la historia y lo registra al cerrar", async ({ page }) => {
  35  |     await page.goto("/");
  36  |     const before = historySnapshot();
  37  | 
  38  |     // The turn only takes note: nothing reaches the clinical history yet.
  39  |     const notedTurn = await sendTurn(page, "Me diagnosticaron hipertensión hace unos dos años.");
  40  |     const notedReply = await replyText(notedTurn);
  41  |     const notedStatus = (await statusLabel(notedTurn).innerText()).trim();
  42  |     const afterNote = historySnapshot();
  43  | 
  44  |     console.log(`[C1] nota status=${notedStatus} reply=${notedReply}`);
  45  |     console.log(`[C1] nuevos_tras_anotar=${addedDocuments(before, afterNote).join(",")}`);
  46  | 
  47  |     expect(notedReply.length).toBeGreaterThan(0);
  48  |     expect(notedStatus).toBe("Anotado");
  49  |     expect(afterNote).toEqual(before);
  50  |     expect(await closedNotice(page).count()).toBe(0);
  51  | 
  52  |     // Closing is what registers the collected fact.
  53  |     const closeTurn = await endConsultation(page);
  54  |     const closeReply = await replyText(closeTurn);
  55  |     const closeStatus = (await statusLabel(closeTurn).innerText()).trim();
  56  |     const afterClose = historySnapshot();
  57  |     const added = addedDocuments(before, afterClose);
  58  | 
  59  |     console.log(`[C1] cierre status=${closeStatus} reply=${closeReply}`);
  60  |     console.log(`[C1] nuevos_tras_cerrar=${added.join(",")}`);
  61  | 
  62  |     expect(closeReply.length).toBeGreaterThan(0);
  63  |     expect(closeStatus).toBe("Registrado");
> 64  |     expect(added).toHaveLength(1);
      |                   ^ Error: expect(received).toHaveLength(expected)
  65  |     expect(await closedNotice(page)).toBeVisible();
  66  | 
  67  |     // A closed consultation does not offer to continue, and the surface lets a new one start.
  68  |     await expect(page.getByLabel("Mensaje para el asistente")).toHaveCount(0);
  69  |     await startNewConsultation(page);
  70  | 
  71  |     // The registered fact is consultable from a new consultation.
  72  |     const consultation = await sendTurn(page, "¿Qué consta sobre la hipertensión que te conté?");
  73  |     const consultationStatus = (await statusLabel(consultation).innerText()).trim();
  74  |     const facts = await consultation
  75  |       .locator('ul[aria-label="Hechos que sustentan la respuesta"] li')
  76  |       .count();
  77  | 
  78  |     console.log(`[C1] consulta status=${consultationStatus} hechos=${facts}`);
  79  | 
  80  |     expect(consultationStatus).toBe("Respuesta");
  81  |     expect(facts).toBeGreaterThan(0);
  82  |     expect(historySnapshot()).toEqual(afterClose);
  83  |   });
  84  | 
  85  |   test("C2 · cierra sin hechos sin confundirlo con un fallo", async ({ page }) => {
  86  |     await page.goto("/");
  87  |     const before = historySnapshot();
  88  | 
  89  |     // A general question collects no clinical facts.
  90  |     const turn = await sendTurn(page, "¿Qué es la hipertensión?");
  91  |     const status = (await statusLabel(turn).innerText()).trim();
  92  |     expect(status).toBe("Conversación");
  93  |     expect(await closedNotice(page).count()).toBe(0);
  94  | 
  95  |     const closeTurn = await endConsultation(page);
  96  |     const closeReply = await replyText(closeTurn);
  97  |     const closeStatus = (await statusLabel(closeTurn).innerText()).trim();
  98  | 
  99  |     console.log(`[C2] cierre status=${closeStatus} reply=${closeReply}`);
  100 | 
  101 |     expect(closeStatus).toBe("Sin hechos que registrar");
  102 |     expect(await closedNotice(page)).toBeVisible();
  103 |     // Closing a consultation with no facts leaves the history exactly as it was.
  104 |     expect(historySnapshot()).toEqual(before);
  105 | 
  106 |     // The close outcome is shown as its own turn, not as an error with a retry.
  107 |     expect(await assistantTurns(page).count()).toBeGreaterThan(1);
  108 |     await expect(page.getByRole("button", { name: "Reintentar" })).toHaveCount(0);
  109 |   });
  110 | });
  111 | 
```