import { render, screen, within } from "@testing-library/react";
import { describe, expect, it } from "vitest";

import { MeasurementsTable } from "./MeasurementsTable";
import { STRINGS } from "../lib/strings";
import type { Measurement } from "../types";

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
