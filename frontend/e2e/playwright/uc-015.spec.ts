import { expect, test } from "@playwright/test";

import { closedNotice, endConsultation, replyText, sendTurn, statusLabel } from "./helpers";
import { profileDocument, profileKeys, profileValue } from "../support/profile";

/**
 * The scenarios of UC-015 that need the real interface, the real backend and the real assistant:
 * the profile interview is optional, the profile is published only when the consultation closes,
 * and the profile keeps the last confirmed value of each field.
 *
 * They are serial on purpose: they share one profile document and every turn costs real provider
 * calls. Each one opens the application from scratch, so it starts its own conversation.
 *
 * Precondition: the run starts from an **empty** clinical-event and encounters directory and from
 * an **absent** profile, exactly as `README.md` prepares the end-to-end run. `P1` publishes the
 * profile itself, and `P2` corrects what `P1` left.
 */
test.describe.configure({ mode: "serial" });

test.describe("UC-015 · perfil del paciente", () => {
  test("P1 · propone la entrevista, permite omitirla y solo publica al cerrar", async ({ page }) => {
    await page.goto("/");

    // Nothing is published before the first close of the run.
    const before = profileDocument();
    expect(before).toBeNull();

    // The first turn invites the optional interview. The person declines it, and declining must not
    // block the conversation: the very next turns are ordinary clinical ones.
    const invitation = await sendTurn(page, "Hola");
    const invitationReply = await replyText(invitation);
    console.log(`[P1] invitación=${invitationReply}`);
    expect(invitationReply.length).toBeGreaterThan(0);

    const skipped = await sendTurn(page, "Prefiero no completar mi perfil ahora.");
    const skippedReply = await replyText(skipped);
    console.log(`[P1] omisión=${skippedReply}`);

    const clinical = await sendTurn(page, "Me diagnosticaron hipertensión hace unos dos años.");
    const clinicalStatus = (await statusLabel(clinical).innerText()).trim();
    console.log(`[P1] clínico status=${clinicalStatus}`);

    // A confirmed personal datum, which is still only a candidate: the profile is published at the
    // close, never per turn.
    const profileTurn = await sendTurn(page, "Me llamo Ana y quiero guardarlo en mi perfil.");
    const profileStatus = (await statusLabel(profileTurn).innerText()).trim();
    console.log(`[P1] perfil status=${profileStatus}`);

    expect(profileDocument()).toBeNull();
    expect(await closedNotice(page).count()).toBe(0);

    const closeTurn = await endConsultation(page);
    const closeReply = await replyText(closeTurn);
    const closeStatus = (await statusLabel(closeTurn).innerText()).trim();
    console.log(`[P1] cierre status=${closeStatus} reply=${closeReply}`);

    expect(closeStatus).not.toBe("No se pudo completar");
    expect(await closedNotice(page)).toBeVisible();

    // The published profile is a well-formed current-value document with the seven fields.
    const after = profileDocument();
    console.log(`[P1] perfil=${JSON.stringify(after)}`);
    expect(after).not.toBeNull();
    expect(after?.startsWith("---\n")).toBe(true);
    expect(profileKeys(after).sort()).toEqual(
      ["blood_group", "date_of_birth", "height_cm", "name", "occupation", "rh", "sex"].sort(),
    );

    // The confirmed datum is what got published, and the fields the person never mentioned stayed
    // absent instead of being invented.
    expect(profileValue(after, "name")).not.toBeNull();
    expect(profileValue(after, "blood_group")).toBeNull();
    expect(profileValue(after, "height_cm")).toBeNull();

    // The profile never stores a history or a provenance block.
    expect(after).not.toMatch(/\bhistory\b|\bversion\b|\bprevious\b|\bsource\b/i);
  });

  test("P2 · conserva el último valor confirmado y no inventa los datos desconocidos", async ({
    page,
  }) => {
    await page.goto("/");

    const before = profileDocument();
    expect(before).not.toBeNull();

    // The person corrects a field and declares another one unknown. Correcting is the same kind of
    // candidate: it replaces the current value only when the close publishes it.
    const correction = await sendTurn(page, "Me llamo Ana López y no conozco mi grupo sanguíneo.");
    const correctionStatus = (await statusLabel(correction).innerText()).trim();
    console.log(`[P2] corrección status=${correctionStatus}`);
    expect(profileDocument()).toEqual(before);

    const closeTurn = await endConsultation(page);
    const closeStatus = (await statusLabel(closeTurn).innerText()).trim();
    console.log(`[P2] cierre status=${closeStatus}`);
    expect(closeStatus).not.toBe("No se pudo completar");

    const after = profileDocument();
    console.log(`[P2] perfil=${JSON.stringify(after)}`);

    // The declared-unknown field stays absent instead of being filled in.
    expect(profileValue(after, "blood_group")).toBeNull();

    // The corrected value is the only current one: the field appears once, so the previous value is
    // gone, because the profile keeps a current value and not a history of them.
    const nameAfter = profileValue(after, "name");
    expect(nameAfter).toContain("López");
    expect(profileKeys(after).filter((key) => key === "name")).toHaveLength(1);
    expect(after).not.toMatch(/\bhistory\b|\bversion\b|\bprevious\b/i);
  });
});
