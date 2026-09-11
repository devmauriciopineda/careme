import { describe, expect, it } from "vitest";

import {
    METRICS,
    buildChartSummary,
    buildSeries,
    computeDateRange,
    computeDomain,
    formatMeasurementDate,
    formatMetricValue,
    isFutureIsoDate,
    sortByDateAsc,
    sortByDateDesc,
    todayIsoDate,
} from "./metrics";
import type { Measurement } from "../types";

const MEASUREMENTS: Measurement[] = [
    { id: "middle", date: "2026-09-08", weightKg: 80.4, waistCm: 95.2 },
    { id: "oldest", date: "2026-09-06", weightKg: 81.1, waistCm: 96 },
    { id: "newest", date: "2026-09-10", weightKg: 80.1, waistCm: 94.8 },
];

describe("sortByDateAsc", () => {
    it("orders measurements chronologically without mutating the input", () => {
        const sorted = sortByDateAsc(MEASUREMENTS);

        expect(sorted.map((measurement) => measurement.date)).toEqual([
            "2026-09-06",
            "2026-09-08",
            "2026-09-10",
        ]);
        expect(MEASUREMENTS[0].date).toBe("2026-09-08");
    });
});

describe("sortByDateDesc", () => {
    it("orders measurements from the most recent to the oldest", () => {
        expect(sortByDateDesc(MEASUREMENTS).map((item) => item.date)).toEqual([
            "2026-09-10",
            "2026-09-08",
            "2026-09-06",
        ]);
    });
});

describe("buildSeries", () => {
    it("extracts the requested metric in chronological order with a label", () => {
        const series = buildSeries(MEASUREMENTS, "weightKg");

        expect(series.map((point) => point.value)).toEqual([81.1, 80.4, 80.1]);
        expect(series.map((point) => point.date)).toEqual([
            "2026-09-06",
            "2026-09-08",
            "2026-09-10",
        ]);
        expect(series[0].label).toBe(formatMeasurementDate("2026-09-06"));
    });

    it("supports every registered metric", () => {
        expect(buildSeries(MEASUREMENTS, "waistCm").map((point) => point.value)).toEqual([
            96, 95.2, 94.8,
        ]);
    });
});

describe("computeDomain", () => {
    it("pads the domain on both sides so the axis does not start at zero", () => {
        const [min, max] = computeDomain([80.1, 80.4, 81.1], 0.15);

        expect(min).toBeLessThan(80.1);
        expect(min).toBeGreaterThan(0);
        expect(max).toBeGreaterThan(81.1);
    });

    it("still produces a usable range when every value is identical", () => {
        const [min, max] = computeDomain([80, 80, 80], 0.15);

        expect(min).toBeLessThan(80);
        expect(max).toBeGreaterThan(80);
    });

    it("returns a collapsed domain for an empty series", () => {
        expect(computeDomain([], 0.15)).toEqual([0, 0]);
    });
});

describe("formatMetricValue", () => {
    it("uses the metric decimals and the Spanish decimal separator", () => {
        expect(formatMetricValue(80.4, METRICS.weightKg)).toBe("80,4");
        expect(formatMetricValue(96, METRICS.waistCm)).toBe("96,0");
    });
});

describe("formatMeasurementDate", () => {
    it("keeps the calendar day regardless of the local time zone", () => {
        const label = formatMeasurementDate("2026-09-06");

        expect(label.startsWith("06")).toBe(true);
        expect(label).not.toBe("2026-09-06");
    });
});

describe("computeDateRange", () => {
    it("returns the formatted first and last dates", () => {
        expect(computeDateRange(MEASUREMENTS)).toEqual({
            from: formatMeasurementDate("2026-09-06"),
            to: formatMeasurementDate("2026-09-10"),
        });
    });

    it("returns empty labels when there are no measurements", () => {
        expect(computeDateRange([])).toEqual({ from: "", to: "" });
    });
});

describe("buildChartSummary", () => {
    it("describes the series for assistive technology", () => {
        const summary = buildChartSummary(
            buildSeries(MEASUREMENTS, "weightKg"),
            METRICS.weightKg
        );

        expect(summary).toContain("Peso");
        expect(summary).toContain("3 mediciones");
        expect(summary).toContain("81,1 kg");
        expect(summary).toContain("80,1 kg");
    });

    it("returns an empty summary for an empty series", () => {
        expect(buildChartSummary([], METRICS.weightKg)).toBe("");
    });
});

describe("todayIsoDate", () => {
    it("formats the local calendar day as yyyy-MM-dd", () => {
        expect(todayIsoDate(new Date(2026, 8, 6))).toBe("2026-09-06");
    });

    it("does not shift the day near midnight", () => {
        expect(todayIsoDate(new Date(2026, 0, 1, 23, 59))).toBe("2026-01-01");
    });
});

describe("isFutureIsoDate", () => {
    it("treats a later day as future", () => {
        expect(isFutureIsoDate("2026-09-11", "2026-09-10")).toBe(true);
    });

    it("treats today and earlier days as not future", () => {
        expect(isFutureIsoDate("2026-09-10", "2026-09-10")).toBe(false);
        expect(isFutureIsoDate("2026-09-09", "2026-09-10")).toBe(false);
    });
});
