import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";

import { ClinicalEventsBrowser } from "./ClinicalEventsBrowser";
import type { ClinicalEvent } from "../types";

const EVENTS: ClinicalEvent[] = [
    {
        id: "one",
        code: "evt_001",
        type: "diagnosis",
        content: "Hipertensión",
        occurrenceDate: "2026-09-01",
        occurrenceDatePrecision: "exact",
        recordDate: "2026-09-03",
    },
    {
        id: "two",
        code: "evt_002",
        type: "medication",
        content: "Enalapril",
        occurrenceDate: "2026-09-12",
        occurrenceDatePrecision: "approximate",
        recordDate: "2026-09-04",
    },
];

describe("ClinicalEventsBrowser", () => {
    it("filters, changes order, and preserves filters when returning from detail", () => {
        render(<ClinicalEventsBrowser events={EVENTS} />);

        const typeFilter = screen.getByLabelText("Tipo");
        fireEvent.change(typeFilter, { target: { value: "diagnosis" } });
        fireEvent.change(screen.getByLabelText("Ordenar por"), {
            target: { value: "occurrenceDate" },
        });

        const eventButton = screen.getByRole("button", { name: /evt_001/ });
        expect(eventButton).toHaveTextContent("Hipertensión");
        fireEvent.click(eventButton);

        expect(screen.getByText("Detalle del hecho")).toBeInTheDocument();
        expect(screen.getByText("Hipertensión")).toBeInTheDocument();

        fireEvent.click(screen.getByRole("button", { name: /Volver a la historia/ }));

        expect(screen.getByLabelText("Tipo")).toHaveValue("diagnosis");
        expect(screen.getByLabelText("Ordenar por")).toHaveValue("occurrenceDate");
        expect(screen.getByRole("button", { name: /evt_001/ })).toBeInTheDocument();
        expect(screen.queryByRole("button", { name: /evt_002/ })).not.toBeInTheDocument();
    });

    it("distinguishes an empty history from a rendered event list", () => {
        render(<ClinicalEventsBrowser events={[]} />);

        expect(screen.getByRole("status")).toHaveTextContent(
            "Todavía no hay hechos registrados en tu historia clínica."
        );
    });
});