import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";

import {
    confirmMeasurementImport,
    previewMeasurementImport,
} from "@/features/measurements/actions";
import { STRINGS } from "@/features/measurements/lib/strings";

import { MeasurementImport } from "./MeasurementImport";

vi.mock("@/features/measurements/actions", () => ({
    previewMeasurementImport: vi.fn(),
    confirmMeasurementImport: vi.fn(),
}));

const previewMock = vi.mocked(previewMeasurementImport);
const confirmMock = vi.mocked(confirmMeasurementImport);

const PREVIEW = {
    rows: [
        {
            date: "2026-09-10",
            weightKg: 80.1,
            waistCm: 94.8,
            replacesExisting: true,
        },
        {
            date: "2026-09-08",
            weightKg: 80.4,
            waistCm: 95.2,
            replacesExisting: false,
        },
    ],
    totalRows: 2,
    newCount: 1,
    replacedCount: 1,
    ignoredCount: 0,
};

const RESULT = {
    createdCount: 1,
    replacedCount: 1,
    ignoredCount: 0,
    totalRows: 2,
};

/** A file of the given size in megabytes. */
function csvFile(name = "measurements.csv", megabytes = 0): File {
    if (megabytes === 0) {
        return new File(["date,weight_kg,abdominal_circumference_cm\n"], name, {
            type: "text/csv",
        });
    }

    return new File([new Uint8Array(megabytes * 1024 * 1024)], name, {
        type: "text/csv",
    });
}

function fileInput(): HTMLInputElement {
    return screen.getByLabelText(STRINGS.import.file) as HTMLInputElement;
}

function button(name: string): HTMLButtonElement {
    return screen.getByRole("button", { name }) as HTMLButtonElement;
}

async function choose(user: ReturnType<typeof userEvent.setup>, file: File) {
    await user.upload(fileInput(), file);
}

beforeEach(() => {
    previewMock.mockReset();
    confirmMock.mockReset();
    previewMock.mockResolvedValue({ ok: true, preview: PREVIEW });
    confirmMock.mockResolvedValue({ ok: true, result: RESULT });
});

describe("MeasurementImport", () => {
    it("does not offer a check until a file is chosen", async () => {
        render(<MeasurementImport />);

        expect(button(STRINGS.import.preview)).toBeDisabled();
        expect(
            screen.queryByRole("button", { name: STRINGS.import.confirm })
        ).toBeNull();
    });

    it("shows what the file would do and asks for confirmation", async () => {
        const user = userEvent.setup();
        render(<MeasurementImport />);

        await choose(user, csvFile());
        expect(button(STRINGS.import.preview)).toBeEnabled();
        expect(previewMock).not.toHaveBeenCalled();

        await user.click(button(STRINGS.import.preview));

        expect(
            await screen.findByText(
                STRINGS.import.previewSummary(
                    PREVIEW.totalRows,
                    PREVIEW.newCount,
                    PREVIEW.replacedCount,
                    PREVIEW.ignoredCount
                )
            )
        ).toBeInTheDocument();
        expect(confirmMock).not.toHaveBeenCalled();
        expect(screen.getByText(STRINGS.import.effectReplaced)).toBeInTheDocument();
        expect(screen.getByText(STRINGS.import.effectNew)).toBeInTheDocument();
    });

    it("loads the file only after the user confirms", async () => {
        const user = userEvent.setup();
        render(<MeasurementImport />);

        await choose(user, csvFile());
        await user.click(button(STRINGS.import.preview));
        await screen.findByText(STRINGS.import.previewRowsTitle);

        await user.click(button(STRINGS.import.confirm));

        expect(
            await screen.findByText(
                STRINGS.import.result(
                    RESULT.createdCount,
                    RESULT.replacedCount,
                    RESULT.ignoredCount
                )
            )
        ).toBeInTheDocument();
        expect(confirmMock).toHaveBeenCalledTimes(1);
        expect(previewMock).toHaveBeenCalledTimes(1);
    });

    it("explains a rejected file with its line and reason", async () => {
        const user = userEvent.setup();
        previewMock.mockResolvedValue({
            ok: false,
            error: {
                code: "INVALID_IMPORT_VALUE",
                details: ["2|weight_kg|NOT_POSITIVE"],
            },
        });
        render(<MeasurementImport />);

        await choose(user, csvFile());
        await user.click(button(STRINGS.import.preview));

        expect(await screen.findByRole("alert")).toHaveTextContent("Fila 2");
        expect(screen.getByRole("alert")).toHaveTextContent(
            STRINGS.import.errors.header
        );
    });

    it("keeps the preview available when the load fails", async () => {
        const user = userEvent.setup();
        confirmMock.mockResolvedValue({
            ok: false,
            error: { code: "UNREADABLE_FILE", details: [] },
        });
        render(<MeasurementImport />);

        await choose(user, csvFile());
        await user.click(button(STRINGS.import.preview));
        await screen.findByText(STRINGS.import.previewRowsTitle);

        await user.click(button(STRINGS.import.confirm));

        expect(await screen.findByRole("alert")).toHaveTextContent(
            STRINGS.import.errors.unreadable
        );
        expect(screen.getByText(STRINGS.import.previewRowsTitle)).toBeInTheDocument();
    });

    it("refuses a file above the size limit without asking the server", async () => {
        const user = userEvent.setup();
        render(<MeasurementImport />);

        await choose(user, csvFile("huge.csv", 11));

        expect(await screen.findByRole("alert")).toHaveTextContent(
            STRINGS.import.fileTooLarge
        );
        expect(button(STRINGS.import.preview)).toBeDisabled();
        expect(previewMock).not.toHaveBeenCalled();
    });

    it("announces that there is nothing to load", async () => {
        const user = userEvent.setup();
        previewMock.mockResolvedValue({
            ok: true,
            preview: {
                rows: [],
                totalRows: 1,
                newCount: 0,
                replacedCount: 0,
                ignoredCount: 1,
            },
        });
        render(<MeasurementImport />);

        await choose(user, csvFile());
        await user.click(button(STRINGS.import.preview));

        expect(await screen.findByText(STRINGS.import.noRows)).toBeInTheDocument();
        expect(
            screen.queryByRole("button", { name: STRINGS.import.confirm })
        ).not.toBeNull();
    });

    it("forgets the outcome when the file is discarded", async () => {
        const user = userEvent.setup();
        render(<MeasurementImport />);

        await choose(user, csvFile());
        await user.click(button(STRINGS.import.preview));
        await screen.findByText(STRINGS.import.previewRowsTitle);

        await user.click(button(STRINGS.import.discard));

        await waitFor(() =>
            expect(
                screen.queryByText(STRINGS.import.previewRowsTitle)
            ).toBeNull()
        );
        expect(button(STRINGS.import.preview)).toBeDisabled();
    });

    it("says what it is doing while the file is being checked", async () => {
        const user = userEvent.setup();
        let release!: (value: { ok: true; preview: typeof PREVIEW }) => void;
        previewMock.mockReturnValue(
            new Promise((resolve) => {
                release = resolve;
            })
        );
        render(<MeasurementImport />);

        await choose(user, csvFile());
        await user.click(button(STRINGS.import.preview));

        expect(await screen.findByRole("status")).toHaveTextContent(
            STRINGS.import.previewing
        );

        release({ ok: true, preview: PREVIEW });

        expect(
            await screen.findByText(STRINGS.import.previewRowsTitle)
        ).toBeInTheDocument();
    });
});
