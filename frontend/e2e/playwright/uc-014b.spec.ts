import { expect, test } from "@playwright/test";

import { endConsultation, replyText, sendTurn, startNewConsultation } from "./helpers";

/**
 * The scenario of UC-014b that needs the real interface, the real backend and the real assistant:
 * the tracking answers a question about the measurements with their values, their units and their
 * dates, and a metric without measurements declares the absence instead of inventing values.
 *
 * Precondition: the run starts from an empty clinical-event and encounters directory, so the only
 * measurement of the questioned metric is the one this scenario registers.
 */
// Four provider round trips in one walkthrough —two message turns, a close and the question that
// follows—, so the budget is wider than the per-test default.
test.describe.configure({ mode: "serial", timeout: 600_000 });

test.describe("UC-014b · consultar las mediciones por lenguaje natural", () => {
  test("C1 · responde con los valores registrados y declara la ausencia", async ({ page }) => {
    await page.goto("/");

    // A measurement the person mentions is registered when the consultation closes.
    await sendTurn(page, "Hoy me tomaron la presión y fue 145/92.");
    await endConsultation(page);

    // The tracking is the person's, not the consultation's: a new consultation asks about what the
    // previous one registered.
    await startNewConsultation(page);

    // Asking for it is answered from the tracking, with its values, its unit and its date.
    const answeredTurn = await sendTurn(page, "¿Cuáles son mis valores de presión arterial?");
    const answeredReply = await replyText(answeredTurn);

    console.log(`[C1] respuesta=${answeredReply}`);

    expect(answeredReply).toContain("presión arterial");

    const supporting = answeredTurn.getByRole("list", {
      name: "Mediciones que sustentan la respuesta",
    });
    await expect(supporting.getByText("presión arterial")).toBeVisible();
    await expect(supporting.getByText("145 mmHg · 92 mmHg")).toBeVisible();
    await expect(supporting.getByText(/\d{4}-\d{2}-\d{2}/)).toBeVisible();

    // A metric without measurements declares the absence instead of inventing values. The wording is
    // the assistant's, so what is asserted is the outcome the surface reports, not the phrasing.
    const absentTurn = await sendTurn(page, "¿Cuánto colesterol tengo?");
    const absentReply = await replyText(absentTurn);

    console.log(`[C1] ausencia=${absentReply}`);

    expect(absentReply.toLowerCase()).toContain("colesterol");
    await expect(absentTurn.getByText("Sin mediciones registradas de esa métrica")).toBeVisible();
    await expect(
      absentTurn.getByRole("list", { name: "Mediciones que sustentan la respuesta" }),
    ).toHaveCount(0);
  });
});
