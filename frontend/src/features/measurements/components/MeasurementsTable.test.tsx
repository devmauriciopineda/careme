import { render, screen, within } from "@testing-library/react";
import { describe, expect, it } from "vitest";

import { MeasurementsTable } from "./MeasurementsTable";
import { STRINGS } from "../lib/strings";
import type { Measurement, TrackingMetric } from "../types";

const MEASUREMENTS: Measurement[] = [
    { id: "oldest", date: "2026-09-06", weightKg: 81.1, waistCm: 96 },
    { id: "newest", date: "2026-09-10", weightKg: 80.1, waistCm: 94.8 },
];

function renderTable(measurements: Measurement[] = MEASUREMENTS) {
    render(<MeasurementsTable measurements={measurements} />);

    const [headerRow, ...dataRows] = screen.getAllByRole("row");
    return { headerRow, dataRows };
}

describe("MeasurementsTable", () => {
    it("renders one row per measurement plus the header row", () => {
        const { headerRow, dataRows } = renderTable();

        expect(dataRows).toHaveLength(MEASUREMENTS.length);
        expect(within(headerRow).getAllByRole("columnheader")).toHaveLength(3);
    });

    it("lists the most recent measurement first", () => {
        const { dataRows } = renderTable();

        expect(dataRows[0].querySelector("time")).toHaveAttribute(
            "datetime",
            "2026-09-10"
        );
        expect(dataRows[1].querySelector("time")).toHaveAttribute(
            "datetime",
            "2026-09-06"
        );
    });

    it("formats both metrics with one decimal", () => {
        const { dataRows } = renderTable();

        expect(within(dataRows[0]).getByText("80,1")).toBeInTheDocument();
        expect(within(dataRows[0]).getByText("94,8")).toBeInTheDocument();
        expect(within(dataRows[1]).getByText("81,1")).toBeInTheDocument();
        expect(within(dataRows[1]).getByText("96,0")).toBeInTheDocument();
    });

    it("exposes the column headers from the shared copy", () => {
        render(<MeasurementsTable measurements={MEASUREMENTS} />);

        expect(
            screen.getByRole("columnheader", { name: STRINGS.table.date })
        ).toBeInTheDocument();
        expect(
            screen.getByRole("columnheader", { name: STRINGS.table.weight })
        ).toBeInTheDocument();
        expect(
            screen.getByRole("columnheader", { name: STRINGS.table.waist })
        ).toBeInTheDocument();
    });

    it("exposes an accessible caption for the table", () => {
        render(<MeasurementsTable measurements={MEASUREMENTS} />);

        expect(screen.getByText(STRINGS.table.caption).tagName).toBe("CAPTION");
    });

    it("renders only the header when there are no measurements", () => {
        const { dataRows } = renderTable([]);

        expect(dataRows).toHaveLength(0);
    });
});

const TRACKING_METRICS: TrackingMetric[] = [
    {
        code: "weight",
        label: "Peso",
        unit: "kg",
        measurements: [
            { id: "w2", date: "2026-09-10", values: [{ component: "value", value: 80.1 }] },
            { id: "w1", date: "2026-09-06", values: [{ component: "value", value: 81.1 }] },
        ],
    },
    {
        code: "blood_pressure",
        label: "presión arterial",
        unit: "mmHg",
        measurements: [
            {
                id: "bp1",
                date: "2026-09-06",
                values: [
                    { component: "systolic", value: 120 },
                    { component: "diastolic", value: 80 },
                ],
            },
        ],
    },
];

function renderTrackingTable(metrics: TrackingMetric[]) {
    render(<MeasurementsTable trackingMetrics={metrics} />);

    const [headerRow, ...dataRows] = screen.getAllByRole("row");
    return { headerRow, dataRows };
}

describe("MeasurementsTable tracking view", () => {
    it("renders one column per selected metric", () => {
        const { headerRow } = renderTrackingTable(TRACKING_METRICS);

        expect(within(headerRow).getAllByRole("columnheader")).toHaveLength(3);
        expect(
            screen.getByRole("columnheader", { name: /Peso \(kg\)/ })
        ).toBeInTheDocument();
        expect(
            screen.getByRole("columnheader", { name: /presión arterial \(mmHg\)/ })
        ).toBeInTheDocument();
    });

    it("does not render columns for metrics that are not selected", () => {
        renderTrackingTable([TRACKING_METRICS[0]]);

        expect(
            screen.getByRole("columnheader", { name: /Peso \(kg\)/ })
        ).toBeInTheDocument();
        expect(screen.queryByRole("columnheader", { name: /presión arterial/ })).not.toBeInTheDocument();
    });

    it("orders the days from oldest to newest", () => {
        const { dataRows } = renderTrackingTable(TRACKING_METRICS);

        expect(dataRows[0].querySelector("time")).toHaveAttribute("datetime", "2026-09-06");
        expect(dataRows[1].querySelector("time")).toHaveAttribute("datetime", "2026-09-10");
    });

    it("presents blood pressure as one metric with both values", () => {
        const { dataRows } = renderTrackingTable([TRACKING_METRICS[1]]);

        expect(
            within(dataRows[0]).getByText("Sistólica: 120 / Diastólica: 80")
        ).toBeInTheDocument();
    });

    it("exposes an accessible caption for the selected metrics", () => {
        renderTrackingTable(TRACKING_METRICS);

        expect(screen.getByText(STRINGS.table.trackingCaption).tagName).toBe("CAPTION");
    });
});

