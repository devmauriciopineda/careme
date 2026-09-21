import { expect, test } from "@playwright/test";

import {
  endConsultation,
  historySnapshot,
  replyText,
  sendTurn,
  statusLabel,
} from "./helpers";

/**
 * The scenario of UC-014 that needs the real interface, the real backend and the real assistant: a
 * measurement mentioned in the conversation is collected in the notes, and the close is what
 * registers it in the tracking. It never becomes a clinical fact, so the clinical history does not
 * change.
 *
 * Precondition: the run starts from an empty clinical-event and encounters directory, because the
 * scenario asserts the close added no clinical document at all.
 */
test.describe.configure({ mode: "serial" });

test.describe("UC-014 · registrar una medición por lenguaje natural", () => {
  test("C1 · registra la medición al cerrar sin escribir un hecho clínico", async ({ page }) => {
    await page.goto("/");
    const before = historySnapshot();

    // The turn only takes note: neither the tracking nor the clinical history changes yet.
    const notedTurn = await sendTurn(page, "Hoy me tomaron la presión y fue 145/92.");
    const notedReply = await replyText(notedTurn);
    const notedStatus = (await statusLabel(notedTurn).innerText()).trim();

    console.log(`[C1] nota status=${notedStatus} reply=${notedReply}`);

    expect(notedReply.length).toBeGreaterThan(0);
    expect(notedStatus).toBe("Anotado");
    expect(historySnapshot()).toEqual(before);

    // Closing is what registers it, and it goes through the measurement channel: the confirmation
    // names the measurement, and no clinical document is written.
    const closeTurn = await endConsultation(page);
    const closeReply = await replyText(closeTurn);
    const closeStatus = (await statusLabel(closeTurn).innerText()).trim();

    console.log(`[C1] cierre status=${closeStatus} reply=${closeReply}`);

    expect(closeReply).toContain("medición");
    expect(closeStatus).toBe("Registrado");
    expect(historySnapshot()).toEqual(before);
  });
});
