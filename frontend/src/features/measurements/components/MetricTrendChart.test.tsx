import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";

import { MetricTrendChart } from "./MetricTrendChart";
import { STRINGS } from "../lib/strings";
import type { TrackingMetric, TrackingMetricCatalog } from "../types";

const WEIGHT: TrackingMetricCatalog = {
    code: "weight",
    label: "Peso",
    referenceUnit: "kg",
    components: [{ key: "value" }],
};

const WEIGHT_METRIC: TrackingMetric = {
    code: "weight",
    label: "Peso",
    unit: "kg",
    measurements: [
        { id: "w1", date: "2026-09-06", values: [{ component: "value", value: 81.1 }] },
        { id: "w2", date: "2026-09-10", values: [{ component: "value", value: 80.1 }] },
    ],
};

const BLOOD_PRESSURE: TrackingMetricCatalog = {
    code: "blood_pressure",
    label: "presión arterial",
    referenceUnit: "mmHg",
    components: [{ key: "systolic" }, { key: "diastolic" }],
};

const BLOOD_PRESSURE_METRIC: TrackingMetric = {
    code: "blood_pressure",
    label: "presión arterial",
    unit: "mmHg",
    measurements: [
        {
            id: "bp1",
            date: "2026-09-10",
            values: [
                { component: "systolic", value: 125 },
                { component: "diastolic", value: 82 },
            ],
        },
    ],
};

describe("MetricTrendChart", () => {
    it("shows the metric label and its reference unit", () => {
        render(<MetricTrendChart metric={WEIGHT_METRIC} definition={WEIGHT} />);

        expect(screen.getByText("Peso")).toBeInTheDocument();
        expect(screen.getByText("kg")).toBeInTheDocument();
    });

    it("renders one series for a simple metric", () => {
        const { container } = render(
            <MetricTrendChart metric={WEIGHT_METRIC} definition={WEIGHT} />
        );

        expect(container.querySelectorAll(".recharts-line")).toHaveLength(1);
    });

    it("renders a systolic and a diastolic series for blood pressure", () => {
        const { container } = render(
            <MetricTrendChart
                metric={BLOOD_PRESSURE_METRIC}
                definition={BLOOD_PRESSURE}
            />
        );

        expect(container.querySelectorAll(".recharts-line")).toHaveLength(2);
    });

    it("exposes a textual summary so the chart is not the only source", () => {
        render(<MetricTrendChart metric={WEIGHT_METRIC} definition={WEIGHT} />);

        const caption = screen.getByText(/Peso: 2 mediciones en kg/);

        expect(caption).toHaveTextContent(STRINGS.charts.keyboardHint);
        expect(caption).toHaveClass("sr-only");
    });
});
