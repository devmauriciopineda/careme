import { expect, test } from "@playwright/test";

import {
  assistantTurns,
  closedNotice,
  endConsultation,
  historySnapshot,
  replyText,
  sendTurn,
  startNewConsultation,
  statusLabel,
} from "./helpers";

/**
 * The scenarios of UC-013 that need the real interface, the real backend and the real assistant:
 * the conversation collects notes, the clinical history is untouched until the consultation is
 * closed, and the close is what registers the admissible facts.
 *
 * They are serial on purpose: they share one clinical history on disk and every turn costs real
 * provider calls.
 *
 * Precondition: the run starts from an **empty** clinical-event and encounters directory, because
 * `C1` asserts the close added exactly one document and `C2` asserts a close with no facts added
 * none.
 */
test.describe.configure({ mode: "serial" });

/** The documents the history gained, comparing content and not only names. */
function addedDocuments(before: Record<string, string>, after: Record<string, string>): string[] {
  return Object.keys(after).filter((name) => !(name in before));
}

test.describe("UC-013 · conversación, recogida y cierre", () => {
  test("C1 · recoge un hecho sin tocar la historia y lo registra al cerrar", async ({ page }) => {
    await page.goto("/");
    const before = historySnapshot();

    // The turn only takes note: nothing reaches the clinical history yet.
    const notedTurn = await sendTurn(page, "Me diagnosticaron hipertensión hace unos dos años.");
    const notedReply = await replyText(notedTurn);
    const notedStatus = (await statusLabel(notedTurn).innerText()).trim();
    const afterNote = historySnapshot();

    console.log(`[C1] nota status=${notedStatus} reply=${notedReply}`);
    console.log(`[C1] nuevos_tras_anotar=${addedDocuments(before, afterNote).join(",")}`);

    expect(notedReply.length).toBeGreaterThan(0);
    expect(notedStatus).toBe("Anotado");
    expect(afterNote).toEqual(before);
    expect(await closedNotice(page).count()).toBe(0);

    // Closing is what registers the collected fact.
    const closeTurn = await endConsultation(page);
    const closeReply = await replyText(closeTurn);
    const closeStatus = (await statusLabel(closeTurn).innerText()).trim();
    const afterClose = historySnapshot();
    const added = addedDocuments(before, afterClose);

    console.log(`[C1] cierre status=${closeStatus} reply=${closeReply}`);
    console.log(`[C1] nuevos_tras_cerrar=${added.join(",")}`);

    expect(closeReply.length).toBeGreaterThan(0);
    expect(closeStatus).toBe("Registrado");
    expect(added).toHaveLength(1);
    expect(await closedNotice(page)).toBeVisible();

    // A closed consultation does not offer to continue, and the surface lets a new one start.
    await expect(page.getByLabel("Mensaje para el asistente")).toHaveCount(0);
    await startNewConsultation(page);

    // The registered fact is consultable from a new consultation.
    const consultation = await sendTurn(page, "¿Qué consta sobre la hipertensión que te conté?");
    const consultationStatus = (await statusLabel(consultation).innerText()).trim();
    const facts = await consultation
      .locator('ul[aria-label="Hechos que sustentan la respuesta"] li')
      .count();

    console.log(`[C1] consulta status=${consultationStatus} hechos=${facts}`);

    expect(consultationStatus).toBe("Respuesta");
    expect(facts).toBeGreaterThan(0);
    expect(historySnapshot()).toEqual(afterClose);
  });

  test("C2 · cierra sin hechos sin confundirlo con un fallo", async ({ page }) => {
    await page.goto("/");
    const before = historySnapshot();

    // A general question collects no clinical facts.
    const turn = await sendTurn(page, "¿Qué es la hipertensión?");
    const status = (await statusLabel(turn).innerText()).trim();
    expect(status).toBe("Conversación");
    expect(await closedNotice(page).count()).toBe(0);

    const closeTurn = await endConsultation(page);
    const closeReply = await replyText(closeTurn);
    const closeStatus = (await statusLabel(closeTurn).innerText()).trim();

    console.log(`[C2] cierre status=${closeStatus} reply=${closeReply}`);

    expect(closeStatus).toBe("Sin hechos que registrar");
    expect(await closedNotice(page)).toBeVisible();
    // Closing a consultation with no facts leaves the history exactly as it was.
    expect(historySnapshot()).toEqual(before);

    // The close outcome is shown as its own turn, not as an error with a retry.
    expect(await assistantTurns(page).count()).toBeGreaterThan(1);
    await expect(page.getByRole("button", { name: "Reintentar" })).toHaveCount(0);
  });
});
