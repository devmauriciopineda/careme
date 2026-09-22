import { render, screen } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";

import { measurementService } from "@/services/measurementService";

import { MeasurementsDashboard } from "./MeasurementsDashboard";

vi.mock("@/features/measurements/actions", () => ({
    loadTrackingMetric: vi.fn(),
}));

vi.mock("@/services/measurementService", () => ({
    measurementService: {
        getTrackingCatalog: vi.fn(),
        getTrackingMetric: vi.fn(),
    },
}));

const serviceMock = vi.mocked(measurementService);

const catalog = [
    { code: "weight", label: "Peso", referenceUnit: "kg", components: [{ key: "value" }] },
    { code: "waist", label: "circunferencia abdominal", referenceUnit: "cm", components: [{ key: "value" }] },
    { code: "cholesterol", label: "colesterol", referenceUnit: "mg/dL", components: [{ key: "value" }] },
];

beforeEach(() => {
    serviceMock.getTrackingCatalog.mockReset();
    serviceMock.getTrackingMetric.mockReset();
    serviceMock.getTrackingCatalog.mockResolvedValue(catalog);
    serviceMock.getTrackingMetric.mockImplementation(async (code) => ({
        code,
        label: catalog.find((entry) => entry.code === code)?.label ?? code,
        unit: catalog.find((entry) => entry.code === code)?.referenceUnit ?? "",
        measurements: code === "cholesterol"
            ? []
            : [{ id: `${code}-1`, date: "2026-09-06", values: [{ component: "value", value: code === "weight" ? 80.1 : 94.8 }] }],
    }));
});

/** Renders what the page renders: the resolved output of the Server Component. */
async function renderDashboard() {
    render(await MeasurementsDashboard());
}

describe("MeasurementsDashboard", () => {
    it("reads the catalogue and one payload per metric and offers those with data", async () => {
        await renderDashboard();

        expect(serviceMock.getTrackingCatalog).toHaveBeenCalledTimes(1);
        expect(serviceMock.getTrackingMetric).toHaveBeenCalledTimes(catalog.length);
        expect(screen.getByRole("checkbox", { name: /Peso/ })).toBeChecked();
        expect(screen.getByRole("checkbox", { name: /circunferencia abdominal/ })).toBeChecked();
        expect(screen.queryByRole("checkbox", { name: /colesterol/ })).not.toBeInTheDocument();
    });

    it("keeps a metric read failure isolated from the others", async () => {
        serviceMock.getTrackingMetric.mockImplementation(async (code) => {
            if (code === "weight") {
                throw new Error("temporary failure");
            }
            const entry = catalog.find((item) => item.code === code);
            return {
                code,
                label: entry?.label ?? code,
                unit: entry?.referenceUnit ?? "",
                measurements: [{ id: `${code}-1`, date: "2026-09-06", values: [{ component: "value", value: 80 }] }],
            };
        });

        await renderDashboard();

        expect(screen.getByText("No se pudo cargar Peso.")).toBeInTheDocument();
        expect(screen.getByRole("checkbox", { name: /circunferencia abdominal/ })).toBeInTheDocument();
        expect(screen.queryByText("Todavía no hay mediciones registradas.")).not.toBeInTheDocument();
    });

    it("shows the empty state when the catalogue has no measurements", async () => {
        serviceMock.getTrackingMetric.mockImplementation(async (code) => {
            const entry = catalog.find((item) => item.code === code);
            return {
                code,
                label: entry?.label ?? code,
                unit: entry?.referenceUnit ?? "",
                measurements: [],
            };
        });

        await renderDashboard();

        expect(screen.getByText("Todavía no hay mediciones registradas.")).toBeInTheDocument();
        expect(screen.queryByRole("checkbox")).not.toBeInTheDocument();
    });
});
