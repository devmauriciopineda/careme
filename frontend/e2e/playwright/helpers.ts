import { expect, type Locator, type Page } from "@playwright/test";

export { eventsDirectory, historySnapshot } from "../support/history";

const PENDING = "Procesando tu mensaje";
const CLOSING = "Cerrando la consulta";

/** The assistant turns rendered so far. The person's own bubbles carry `ml-auto`. */
export function assistantTurns(page: Page): Locator {
  return page.locator('main div[class*="max-w-[85%]"]:not(.ml-auto)');
}

/** The status label the interface renders under an assistant turn. */
export function statusLabel(turn: Locator): Locator {
  return turn.locator("p").first();
}

/**
 * Types a message, sends it and waits until the turn has closed with exactly one new assistant
 * reply. One reply per turn is the behaviour under test, so the helper fails instead of timing out
 * if a turn produced two.
 */
export async function sendTurn(page: Page, message: string): Promise<Locator> {
  const before = await assistantTurns(page).count();
  await page.getByLabel("Mensaje para el asistente").fill(message);
  await page.getByRole("button", { name: "Enviar mensaje" }).click();
  await expect(page.getByRole("status").filter({ hasText: PENDING })).toHaveCount(0, {
    timeout: 240_000,
  });
  await expect(assistantTurns(page)).toHaveCount(before + 1);
  return assistantTurns(page).last();
}

/** Reads the reply text of a closed turn. */
export async function replyText(turn: Locator): Promise<string> {
  return (await turn.locator("div").first().innerText()).trim();
}

/**
 * Ends the consultation in progress and waits until the close turn has closed with exactly one new
 * assistant reply, the same way `sendTurn` waits for a message turn.
 */
export async function endConsultation(page: Page): Promise<Locator> {
  const before = await assistantTurns(page).count();
  await page.getByRole("button", { name: "Terminar consulta" }).click();
  await expect(page.getByRole("status").filter({ hasText: CLOSING })).toHaveCount(0, {
    timeout: 240_000,
  });
  await expect(assistantTurns(page)).toHaveCount(before + 1);
  return assistantTurns(page).last();
}

/** The notice the surface shows once the consultation is closed. */
export function closedNotice(page: Page): Locator {
  return page.getByText("La consulta está cerrada.");
}

/** Starts a new consultation from the closed surface. */
export async function startNewConsultation(page: Page): Promise<void> {
  await page.getByRole("button", { name: "Empezar una nueva consulta" }).click();
  await expect(page.getByLabel("Mensaje para el asistente")).toBeVisible();
}
