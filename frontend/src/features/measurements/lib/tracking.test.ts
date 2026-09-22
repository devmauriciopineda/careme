import { describe, expect, it } from "vitest";

import {
    availableMetrics,
    defaultSelection,
    metricStateOf,
    type MetricStates,
} from "./tracking";
import type { TrackingMetric, TrackingMetricCatalog } from "../types";

const CATALOG: TrackingMetricCatalog[] = [
    { code: "weight", label: "Peso", referenceUnit: "kg", components: [{ key: "value" }] },
    {
        code: "waist",
        label: "circunferencia abdominal",
        referenceUnit: "cm",
        components: [{ key: "value" }],
    },
    {
        code: "cholesterol",
        label: "colesterol",
        referenceUnit: "mg/dL",
        components: [{ key: "value" }],
    },
];

function metric(code: string, dates: string[]): TrackingMetric {
    const entry = CATALOG.find((item) => item.code === code);
    return {
        code,
        label: entry?.label ?? code,
        unit: entry?.referenceUnit ?? "",
        measurements: dates.map((date) => ({
            id: `${code}-${date}`,
            date,
            values: [{ component: "value", value: 80 }],
        })),
    };
}

describe("metricStateOf", () => {
    it("reports success when the metric has measurements", () => {
        expect(metricStateOf(metric("weight", ["2026-09-06"]))).toEqual({
            status: "success",
            data: expect.objectContaining({ code: "weight" }),
        });
    });

    it("reports the absence when the metric has none", () => {
        expect(metricStateOf(metric("weight", []))).toEqual({
            status: "empty",
            data: expect.objectContaining({ code: "weight" }),
        });
    });
});

describe("availableMetrics", () => {
    it("keeps only the metrics with measurements, in catalogue order", () => {
        const states: MetricStates = {
            weight: metricStateOf(metric("weight", ["2026-09-06"])),
            waist: metricStateOf(metric("waist", ["2026-09-10"])),
            cholesterol: metricStateOf(metric("cholesterol", [])),
        };

        expect(availableMetrics(CATALOG, states).map((entry) => entry.code)).toEqual([
            "weight",
            "waist",
        ]);
    });

    it("leaves out a metric that could not be read", () => {
        const states: MetricStates = {
            weight: { status: "error" },
            waist: metricStateOf(metric("waist", ["2026-09-10"])),
        };

        expect(availableMetrics(CATALOG, states).map((entry) => entry.code)).toEqual(["waist"]);
    });
});

describe("defaultSelection", () => {
    it("selects weight and abdominal circumference when both have data", () => {
        const states: MetricStates = {
            weight: metricStateOf(metric("weight", ["2026-09-06"])),
            waist: metricStateOf(metric("waist", ["2026-09-06"])),
            cholesterol: metricStateOf(metric("cholesterol", ["2026-09-06"])),
        };

        expect(defaultSelection(CATALOG, states)).toEqual(["weight", "waist"]);
    });

    it("falls back to every metric with data when neither preferred one has any", () => {
        const states: MetricStates = {
            weight: metricStateOf(metric("weight", [])),
            cholesterol: metricStateOf(metric("cholesterol", ["2026-09-06"])),
        };

        expect(defaultSelection(CATALOG, states)).toEqual(["cholesterol"]);
    });

    it("selects nothing when no metric has measurements", () => {
        const states: MetricStates = {
            weight: metricStateOf(metric("weight", [])),
        };

        expect(defaultSelection(CATALOG, states)).toEqual([]);
    });
});
