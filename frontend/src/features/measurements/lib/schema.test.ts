import { describe, expect, it } from "vitest";

import {
    measurementFormSchema,
    measurementInputSchema,
    measurementSchema,
    measurementsSchema,
} from "./schema";

const VALID_MEASUREMENT = {
    id: "measurement-2026-09-06",
    date: "2026-09-06",
    weightKg: 81.1,
    waistCm: 96,
};

describe("measurementSchema", () => {
    it("accepts a complete measurement", () => {
        expect(measurementSchema.parse(VALID_MEASUREMENT)).toEqual(VALID_MEASUREMENT);
    });

    it("rejects a date that is not in yyyy-MM-dd format", () => {
        expect(() =>
            measurementSchema.parse({ ...VALID_MEASUREMENT, date: "06-09-2026" })
        ).toThrow();
    });

    it("rejects a missing metric", () => {
        const { weightKg, ...withoutWeight } = VALID_MEASUREMENT;

        expect(weightKg).toBe(81.1);
        expect(() => measurementSchema.parse(withoutWeight)).toThrow();
    });

    it("rejects a metric that is not a number", () => {
        expect(() =>
            measurementSchema.parse({ ...VALID_MEASUREMENT, waistCm: "96" })
        ).toThrow();
    });

    it("rejects a non-positive metric", () => {
        expect(() =>
            measurementSchema.parse({ ...VALID_MEASUREMENT, weightKg: 0 })
        ).toThrow();
    });

    it("rejects an empty id", () => {
        expect(() =>
            measurementSchema.parse({ ...VALID_MEASUREMENT, id: "" })
        ).toThrow();
    });
});

describe("measurementsSchema", () => {
    it("accepts a list and reports the index of an invalid entry", () => {
        const result = measurementsSchema.safeParse([
            VALID_MEASUREMENT,
            { ...VALID_MEASUREMENT, date: "not-a-date" },
        ]);

        expect(result.success).toBe(false);
        expect(result.error?.issues[0]?.path).toEqual([1, "date"]);
    });
});

describe("measurementInputSchema", () => {
    const VALID_INPUT = { date: "2020-01-01", weightKg: 80.1, waistCm: 94.8 };

    it("accepts a complete, single-decimal payload", () => {
        expect(measurementInputSchema.parse(VALID_INPUT)).toEqual(VALID_INPUT);
    });

    it("rejects a future date", () => {
        expect(
            measurementInputSchema.safeParse({ ...VALID_INPUT, date: "2999-01-01" })
                .success
        ).toBe(false);
    });

    it("rejects a value with more than one decimal", () => {
        expect(
            measurementInputSchema.safeParse({ ...VALID_INPUT, weightKg: 80.12 })
                .success
        ).toBe(false);
    });

    it("rejects a value above the allowed limit", () => {
        expect(
            measurementInputSchema.safeParse({ ...VALID_INPUT, waistCm: 401 })
                .success
        ).toBe(false);
    });
});

describe("measurementFormSchema", () => {
    const VALID_FORM = { date: "2020-01-01", weightKg: "80,1", waistCm: "94,8" };

    it("accepts valid text and converts it to a numeric payload", () => {
        expect(measurementFormSchema.parse(VALID_FORM)).toEqual({
            date: "2020-01-01",
            weightKg: 80.1,
            waistCm: 94.8,
        });
    });

    it("accepts a dot as the decimal separator", () => {
        expect(
            measurementFormSchema.parse({ ...VALID_FORM, weightKg: "80.1" })
        ).toMatchObject({ weightKg: 80.1 });
    });

    it("reports every missing field", () => {
        const result = measurementFormSchema.safeParse({
            date: "2020-01-01",
            weightKg: "",
            waistCm: "  ",
        });

        expect(result.success).toBe(false);
        expect(result.error?.issues.map((issue) => issue.path[0])).toEqual([
            "weightKg",
            "waistCm",
        ]);
    });

    it("rejects a non-numeric value", () => {
        const result = measurementFormSchema.safeParse({
            ...VALID_FORM,
            weightKg: "ochenta",
        });

        expect(result.success).toBe(false);
        expect(result.error?.issues[0]?.path[0]).toBe("weightKg");
    });

    it("rejects a non-positive value", () => {
        expect(
            measurementFormSchema.safeParse({ ...VALID_FORM, waistCm: "0" }).success
        ).toBe(false);
    });

    it("rejects a value with more than one decimal", () => {
        expect(
            measurementFormSchema.safeParse({ ...VALID_FORM, waistCm: "94,85" })
                .success
        ).toBe(false);
    });

    it("rejects a value above the allowed limit", () => {
        expect(
            measurementFormSchema.safeParse({ ...VALID_FORM, weightKg: "501" })
                .success
        ).toBe(false);
    });

    it("rejects a future date", () => {
        const result = measurementFormSchema.safeParse({
            ...VALID_FORM,
            date: "2999-01-01",
        });

        expect(result.success).toBe(false);
        expect(result.error?.issues[0]?.path[0]).toBe("date");
    });
});
