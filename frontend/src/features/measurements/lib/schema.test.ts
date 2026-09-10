import { describe, expect, it } from "vitest";

import { measurementSchema, measurementsSchema } from "./schema";

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
