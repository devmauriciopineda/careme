import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";

import { loadTrackingMetric } from "@/features/measurements/actions";
import type { MetricStates } from "@/features/measurements/lib/tracking";
import type { TrackingMetric, TrackingMetricCatalog } from "@/features/measurements/types";

import { MeasurementsTrackingView } from "./MeasurementsTrackingView";

vi.mock("@/features/measurements/actions", () => ({
    loadTrackingMetric: vi.fn(),
}));

const retryMock = vi.mocked(loadTrackingMetric);

const CATALOG: TrackingMetricCatalog[] = [
    { code: "weight", label: "Peso", referenceUnit: "kg", components: [{ key: "value" }] },
    {
        code: "waist",
        label: "circunferencia abdominal",
        referenceUnit: "cm",
        components: [{ key: "value" }],
    },
];

function simpleMetric(
    code: string,
    label: string,
    unit: string,
    value: number
): TrackingMetric {
    return {
        code,
        label,
        unit,
        measurements: [
            { id: `${code}-1`, date: "2026-09-06", values: [{ component: "value", value }] },
        ],
    };
}

function success(...metrics: TrackingMetric[]): MetricStates {
    return Object.fromEntries(metrics.map((metric) => [metric.code, { status: "success", data: metric }]));
}

const READY: MetricStates = success(
    simpleMetric("weight", "Peso", "kg", 80.1),
    simpleMetric("waist", "circunferencia abdominal", "cm", 94.8)
);

beforeEach(() => {
    retryMock.mockReset();
});

describe("MeasurementsTrackingView", () => {
    it("offers only the metrics with data and selects weight and waist by default", () => {
        render(
            <MeasurementsTrackingView
                catalog={[
                    ...CATALOG,
                    {
                        code: "cholesterol",
                        label: "colesterol",
                        referenceUnit: "mg/dL",
                        components: [{ key: "value" }],
                    },
                ]}
                states={{
                    ...READY,
                    cholesterol: { status: "empty", data: simpleMetric("cholesterol", "colesterol", "mg/dL", 0) },
                }}
            />
        );

        expect(screen.getByRole("checkbox", { name: /Peso/ })).toBeChecked();
        expect(screen.getByRole("checkbox", { name: /circunferencia abdominal/ })).toBeChecked();
        expect(screen.queryByRole("checkbox", { name: /colesterol/ })).not.toBeInTheDocument();
        expect(screen.getAllByRole("columnheader")).toHaveLength(3);
    });

    it("uses one selection for charts and detail columns", async () => {
        const user = userEvent.setup();
        render(<MeasurementsTrackingView catalog={CATALOG} states={READY} />);

        await user.click(screen.getByRole("checkbox", { name: /Peso/ }));

        expect(screen.getAllByRole("figure")).toHaveLength(1);
        expect(screen.getByRole("columnheader", { name: /circunferencia abdominal/ })).toBeInTheDocument();
        expect(screen.queryByRole("columnheader", { name: /Peso/ })).not.toBeInTheDocument();
    });

    it("keeps blood pressure as one metric with two values", () => {
        render(
            <MeasurementsTrackingView
                catalog={[
                    {
                        code: "blood_pressure",
                        label: "presión arterial",
                        referenceUnit: "mmHg",
                        components: [{ key: "systolic" }, { key: "diastolic" }],
                    },
                ]}
                states={{
                    blood_pressure: {
                        status: "success",
                        data: {
                            code: "blood_pressure",
                            label: "presión arterial",
                            unit: "mmHg",
                            measurements: [
                                {
                                    id: "bp-1",
                                    date: "2026-09-06",
                                    values: [
                                        { component: "systolic", value: 120 },
                                        { component: "diastolic", value: 80 },
                                    ],
                                },
                            ],
                        },
                    },
                }}
            />
        );

        expect(screen.getByRole("checkbox", { name: /presión arterial/ })).toBeChecked();
        expect(screen.getByText("Sistólica: 120 / Diastólica: 80")).toBeInTheDocument();
        expect(screen.getAllByRole("columnheader")).toHaveLength(2);
    });

    it("retries one failed metric without removing the other", async () => {
        const user = userEvent.setup();
        retryMock.mockResolvedValue({
            status: "success",
            data: simpleMetric("weight", "Peso", "kg", 80),
        });

        render(
            <MeasurementsTrackingView
                catalog={CATALOG}
                states={{ ...READY, weight: { status: "error" } }}
            />
        );

        expect(screen.getByText("No se pudo cargar Peso.")).toBeInTheDocument();
        expect(screen.getByRole("checkbox", { name: /circunferencia abdominal/ })).toBeInTheDocument();

        await user.click(screen.getByRole("button", { name: "Reintentar métrica" }));

        await waitFor(() => expect(screen.getByRole("checkbox", { name: /Peso/ })).toBeInTheDocument());
        expect(retryMock).toHaveBeenCalledWith("weight");
        expect(screen.getByRole("checkbox", { name: /circunferencia abdominal/ })).toBeInTheDocument();
    });

    it("does not declare the whole tracking empty when every metric fails", () => {
        render(
            <MeasurementsTrackingView
                catalog={CATALOG}
                states={{ weight: { status: "error" }, waist: { status: "error" } }}
            />
        );

        expect(screen.getByText("No se pudo cargar Peso.")).toBeInTheDocument();
        expect(
            screen.getByText("No se pudo cargar circunferencia abdominal.")
        ).toBeInTheDocument();
        expect(screen.queryByText("Todavía no hay mediciones registradas.")).not.toBeInTheDocument();
    });

    it("keeps trend and detail on the same selected metrics", async () => {
        const user = userEvent.setup();
        render(<MeasurementsTrackingView catalog={CATALOG} states={READY} />);

        expect(screen.getAllByRole("figure")).toHaveLength(2);
        expect(screen.getAllByRole("columnheader")).toHaveLength(3);

        await user.click(screen.getByRole("checkbox", { name: /Peso/ }));

        expect(screen.getAllByRole("figure")).toHaveLength(1);
        expect(screen.getAllByRole("columnheader")).toHaveLength(2);
        expect(screen.getByRole("columnheader", { name: /circunferencia abdominal/ })).toBeInTheDocument();
        expect(screen.queryByRole("columnheader", { name: /Peso/ })).not.toBeInTheDocument();
    });

    it("lets the metric selector be operated with the keyboard", async () => {
        const user = userEvent.setup();
        render(<MeasurementsTrackingView catalog={CATALOG} states={READY} />);

        const weight = screen.getByRole("checkbox", { name: /Peso/ });
        weight.focus();
        await user.keyboard(" ");

        expect(weight).not.toBeChecked();
    });

    it("offers a new catalog metric that has measurements", async () => {
        const user = userEvent.setup();
        render(
            <MeasurementsTrackingView
                catalog={[
                    ...CATALOG,
                    { code: "glucose", label: "glucemia", referenceUnit: "mg/dL", components: [{ key: "value" }] },
                ]}
                states={{
                    ...READY,
                    glucose: { status: "success", data: simpleMetric("glucose", "glucemia", "mg/dL", 95) },
                }}
            />
        );

        const glucose = screen.getByRole("checkbox", { name: /glucemia \(mg\/dL\)/ });
        expect(glucose).not.toBeChecked();

        await user.click(glucose);

        expect(screen.getAllByRole("figure")).toHaveLength(3);
        expect(screen.getAllByRole("columnheader")).toHaveLength(4);
        expect(screen.getByRole("columnheader", { name: /glucemia/ })).toBeInTheDocument();
    });

    it("shows the empty state when no metric has measurements", () => {
        render(
            <MeasurementsTrackingView
                catalog={CATALOG}
                states={{
                    weight: { status: "empty", data: simpleMetric("weight", "Peso", "kg", 0) },
                    waist: { status: "empty", data: simpleMetric("waist", "circunferencia abdominal", "cm", 0) },
                }}
            />
        );

        expect(screen.getByText("Todavía no hay mediciones registradas.")).toBeInTheDocument();
        expect(screen.queryByRole("checkbox")).not.toBeInTheDocument();
    });
});
