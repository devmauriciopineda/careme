import { act, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";

import { registerMeasurement } from "@/features/measurements/actions";
import { todayIsoDate } from "@/features/measurements/lib/metrics";
import { STRINGS } from "@/features/measurements/lib/strings";

import { MeasurementForm } from "./MeasurementForm";

vi.mock("@/features/measurements/actions", () => ({
    registerMeasurement: vi.fn(),
}));

const registerMock = vi.mocked(registerMeasurement);

function deferred<T>() {
    let resolve!: (value: T) => void;
    const promise = new Promise<T>((res) => {
        resolve = res;
    });
    return { promise, resolve };
}

function getField(label: string): HTMLInputElement {
    return screen.getByLabelText(label) as HTMLInputElement;
}

function getSubmit(): HTMLButtonElement {
    return screen.getByRole("button", {
        name: STRINGS.form.submit,
    }) as HTMLButtonElement;
}

async function fillValidMetrics(user: ReturnType<typeof userEvent.setup>) {
    await user.type(getField(STRINGS.form.weight), "80,1");
    await user.type(getField(STRINGS.form.waist), "94,8");
}

beforeEach(() => {
    registerMock.mockReset();
    registerMock.mockResolvedValue({ ok: true });
});

describe("MeasurementForm", () => {
    it("presents a labelled field per value and defaults the date to today", () => {
        render(<MeasurementForm />);

        expect(getField(STRINGS.form.date)).toHaveValue(todayIsoDate());
        expect(getField(STRINGS.form.weight)).toHaveValue("");
        expect(getField(STRINGS.form.waist)).toHaveValue("");
        expect(getSubmit()).toBeEnabled();
    });

    it("flags the missing values and does not submit", async () => {
        const user = userEvent.setup();
        render(<MeasurementForm />);

        await user.click(getSubmit());

        expect(
            await screen.findAllByText(STRINGS.form.validation.required)
        ).toHaveLength(2);
        expect(registerMock).not.toHaveBeenCalled();
    });

    it("flags a value with more than one decimal and does not submit", async () => {
        const user = userEvent.setup();
        render(<MeasurementForm />);

        await user.type(getField(STRINGS.form.weight), "80.12");
        await user.type(getField(STRINGS.form.waist), "94.8");
        await user.click(getSubmit());

        expect(
            await screen.findByText(STRINGS.form.validation.oneDecimal)
        ).toBeInTheDocument();
        expect(registerMock).not.toHaveBeenCalled();
    });

    it("submits the parsed values for a valid day", async () => {
        const user = userEvent.setup();
        render(<MeasurementForm />);

        await fillValidMetrics(user);
        await user.click(getSubmit());

        await waitFor(() =>
            expect(registerMock).toHaveBeenCalledWith({
                date: todayIsoDate(),
                weightKg: 80.1,
                waistCm: 94.8,
            })
        );
    });

    it("confirms the save and clears the metric fields on success", async () => {
        const user = userEvent.setup();
        render(<MeasurementForm />);

        await fillValidMetrics(user);
        await user.click(getSubmit());

        expect(await screen.findByText(STRINGS.form.success)).toBeInTheDocument();
        expect(getField(STRINGS.form.weight)).toHaveValue("");
        expect(getField(STRINGS.form.waist)).toHaveValue("");
    });

    it("keeps what was typed and offers to retry when the save fails", async () => {
        registerMock.mockResolvedValue({ ok: false, errorCode: "SAVE_FAILED" });
        const user = userEvent.setup();
        render(<MeasurementForm />);

        await fillValidMetrics(user);
        await user.click(getSubmit());

        expect(await screen.findByText(STRINGS.form.saveFailed)).toBeInTheDocument();
        expect(getField(STRINGS.form.weight)).toHaveValue("80,1");
        expect(getField(STRINGS.form.waist)).toHaveValue("94,8");
    });

    it("disables the submit button while the save is in flight", async () => {
        const pending = deferred<{ ok: true }>();
        registerMock.mockReturnValue(pending.promise);
        const user = userEvent.setup();
        render(<MeasurementForm />);

        await fillValidMetrics(user);
        const submit = getSubmit();
        await user.click(submit);

        await waitFor(() => expect(submit).toBeDisabled());
        expect(submit).toHaveTextContent(STRINGS.form.submitting);

        await act(async () => {
            pending.resolve({ ok: true });
        });

        expect(
            await screen.findByText(STRINGS.form.success)
        ).toBeInTheDocument();
    });
});
