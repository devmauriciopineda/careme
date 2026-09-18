import { describe, expect, it } from "vitest";

import { clinicalEventsResponseSchema } from "./schema";

describe("clinical event response schema", () => {
    it("accepts both visible dates and approximate precision", () => {
        const result = clinicalEventsResponseSchema.safeParse({
            success: true,
            data: [{
                id: "id-1",
                code: "evt_001",
                type: "note",
                content: "Dolor de cabeza",
                occurrenceDate: "2026-09-01",
                occurrenceDatePrecision: "approximate",
                recordDate: "2026-09-02",
            }],
            messageCode: "SUCCESS",
            message: "ok",
        });

        expect(result.success).toBe(true);
    });

    it("rejects incomplete event dates", () => {
        const result = clinicalEventsResponseSchema.safeParse({
            success: true,
            data: [{
                id: "id-1",
                code: "evt_001",
                type: "note",
                content: "Dolor de cabeza",
                occurrenceDate: "2026-09-01",
            }],
            messageCode: "SUCCESS",
            message: "ok",
        });

        expect(result.success).toBe(false);
    });
});