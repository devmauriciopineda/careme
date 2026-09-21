import { describe, expect, it } from "vitest";

import { chatResponseSchema } from "./schema";

const base = {
  conversationId: "conversation-1",
  messageId: "message-1",
  message: "hola",
  events: [],
};

describe("chatResponseSchema", () => {
  it.each([
    "registered",
    "answered",
    "no_records",
    "clarification_required",
    "general_conversation",
    "duplicate",
    "failed",
  ])("accepts the %s status", (status) => {
    expect(chatResponseSchema.safeParse({ ...base, status }).success).toBe(true);
  });

  it("rejects an unknown status", () => {
    expect(chatResponseSchema.safeParse({ ...base, status: "otro" }).success).toBe(false);
  });

  it.each(["no_measurements", "no_measurements_in_period", "metric_not_tracked"])(
    "accepts the %s absence reason",
    (absenceReason) => {
      const parsed = chatResponseSchema.safeParse({ ...base, status: "no_records", absenceReason });

      expect(parsed.success).toBe(true);
    },
  );

  it("accepts an answer that carries the measurements supporting it", () => {
    const parsed = chatResponseSchema.safeParse({
      ...base,
      status: "answered",
      measurements: [
        {
          reference: "blood_pressure@2026-01-10",
          metric: "blood_pressure",
          label: "presión arterial",
          unit: "mmHg",
          date: "2026-01-10",
          values: [
            { component: "systolic", value: "145" },
            { component: "diastolic", value: "92" },
          ],
          text: "presión arterial: sistólica 145 mmHg, diastólica 92 mmHg (2026-01-10)",
        },
      ],
    });

    expect(parsed.success).toBe(true);
  });

  it("rejects a measurement the interface cannot present", () => {
    const parsed = chatResponseSchema.safeParse({
      ...base,
      status: "answered",
      measurements: [{ reference: "weight@2026-01-10", label: "peso" }],
    });

    expect(parsed.success).toBe(false);
  });

  it("accepts an answered turn that carries its supporting events and their precision", () => {
    const parsed = chatResponseSchema.safeParse({
      ...base,
      status: "answered",
      events: [
        {
          code: "evt_001",
          type: "diagnosis",
          date: "2026-01-10",
          datePrecision: "approximate",
          content: "Hipertensión diagnosticada",
        },
      ],
    });

    expect(parsed.success).toBe(true);
  });

  it("accepts a no-records turn that reports its reason and the offered actions", () => {
    const parsed = chatResponseSchema.safeParse({
      ...base,
      status: "no_records",
      message: "No encuentro registros en ese periodo de tu historia clínica.",
      absenceReason: "no_events_in_period",
      suggestedActions: ["reformulate", "register"],
    });

    expect(parsed.success).toBe(true);
  });

  it("accepts a turn that omits the optional absence detail", () => {
    expect(chatResponseSchema.safeParse({ ...base, status: "answered" }).success).toBe(true);
  });

  it("accepts a null absence reason", () => {
    const parsed = chatResponseSchema.safeParse({
      ...base,
      status: "answered",
      absenceReason: null,
    });

    expect(parsed.success).toBe(true);
  });

  it.each([
    "empty_history",
    "no_events_of_type",
    "no_events_in_period",
    "no_term_match",
  ])("accepts the %s absence reason", (absenceReason) => {
    expect(chatResponseSchema.safeParse({ ...base, status: "no_records", absenceReason }).success).toBe(true);
  });

  it("rejects an absence reason the interface cannot present", () => {
    const parsed = chatResponseSchema.safeParse({
      ...base,
      status: "no_records",
      absenceReason: "otro",
    });

    expect(parsed.success).toBe(false);
  });

  it("rejects a suggested action the interface cannot offer", () => {
    const parsed = chatResponseSchema.safeParse({
      ...base,
      status: "no_records",
      suggestedActions: ["otro"],
    });

    expect(parsed.success).toBe(false);
  });

  it("accepts a turn that reports the operations it went through", () => {
    const parsed = chatResponseSchema.safeParse({
      ...base,
      status: "answered",
      operations: [
        { status: "registered", events: [] },
        {
          status: "answered",
          events: [
            {
              code: "evt_001",
              type: "diagnosis",
              date: "2026-01-10",
              datePrecision: "exact",
              content: "Hipertensión diagnosticada",
            },
          ],
        },
      ],
    });

    expect(parsed.success).toBe(true);
  });

  it("accepts a turn that omits the operations", () => {
    expect(chatResponseSchema.safeParse({ ...base, status: "answered" }).success).toBe(true);
    expect(chatResponseSchema.safeParse({ ...base, status: "answered", operations: [] }).success).toBe(true);
  });

  it("rejects an operation the interface cannot read", () => {
    const parsed = chatResponseSchema.safeParse({
      ...base,
      status: "answered",
      operations: [{ status: "inventado", events: [] }],
    });

    expect(parsed.success).toBe(false);
  });
});
