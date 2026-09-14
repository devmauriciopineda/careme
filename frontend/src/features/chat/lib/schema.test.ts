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
});
