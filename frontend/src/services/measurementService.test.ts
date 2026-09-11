import { afterEach, describe, expect, it, vi } from "vitest";

import { measurementService } from "./measurementService";

const SUCCESSFUL_RESPONSE = {
    success: true,
    messageCode: "SUCCESS",
    message: "Operation completed successfully",
    data: [
        {
            id: "measurement-2026-09-10",
            date: "2026-09-10",
            weightKg: 80.1,
            waistCm: 94.8,
        },
        {
            id: "measurement-2026-09-06",
            date: "2026-09-06",
            weightKg: 81.1,
            waistCm: 96,
        },
    ],
};

function stubFetch(response: Partial<Response>) {
    const fetchMock = vi.fn().mockResolvedValue(response as Response);
    vi.stubGlobal("fetch", fetchMock);
    return fetchMock;
}

function silenceConsoleError() {
    return vi.spyOn(console, "error").mockImplementation(() => {});
}

afterEach(() => {
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
});

describe("measurementService.getMeasurements", () => {
    it("unwraps the envelope and returns the measurements oldest first", async () => {
        const fetchMock = stubFetch({
            ok: true,
            status: 200,
            json: async () => SUCCESSFUL_RESPONSE,
        });

        const measurements = await measurementService.getMeasurements();

        expect(fetchMock).toHaveBeenCalledWith(
            expect.stringContaining("/api/v1/measurements"),
            expect.objectContaining({ cache: "no-store" })
        );
        expect(measurements.map((measurement) => measurement.date)).toEqual([
            "2026-09-06",
            "2026-09-10",
        ]);
    });

    it("fails when the API responds with an error status", async () => {
        silenceConsoleError();
        stubFetch({
            ok: false,
            status: 404,
            json: async () => ({
                success: false,
                error: { message: "not found", code: "NOT_FOUND", details: [] },
            }),
        });

        await expect(measurementService.getMeasurements()).rejects.toThrow(
            "404"
        );
    });

    it("fails when the envelope does not report success", async () => {
        silenceConsoleError();
        stubFetch({
            ok: true,
            status: 200,
            json: async () => ({ ...SUCCESSFUL_RESPONSE, success: false }),
        });

        await expect(measurementService.getMeasurements()).rejects.toThrow();
    });

    it("fails when a measurement in the payload is malformed", async () => {
        silenceConsoleError();
        stubFetch({
            ok: true,
            status: 200,
            json: async () => ({
                ...SUCCESSFUL_RESPONSE,
                data: [{ ...SUCCESSFUL_RESPONSE.data[0], date: "10-09-2026" }],
            }),
        });

        await expect(measurementService.getMeasurements()).rejects.toThrow();
    });
});

const CREATED_RESPONSE = {
    success: true,
    messageCode: "SUCCESS",
    message: "Operation completed successfully",
    data: {
        id: "measurement-2026-09-10",
        date: "2026-09-10",
        weightKg: 80.1,
        waistCm: 94.8,
    },
};

const INPUT = { date: "2026-09-10", weightKg: 80.1, waistCm: 94.8 };

describe("measurementService.createMeasurement", () => {
    it("posts the payload and returns the stored measurement", async () => {
        const fetchMock = stubFetch({
            ok: true,
            status: 201,
            json: async () => CREATED_RESPONSE,
        });

        const created = await measurementService.createMeasurement(INPUT);

        expect(fetchMock).toHaveBeenCalledWith(
            expect.stringContaining("/api/v1/measurements"),
            expect.objectContaining({
                method: "POST",
                cache: "no-store",
                body: JSON.stringify(INPUT),
            })
        );
        expect(created.date).toBe("2026-09-10");
    });

    it("fails when the API responds with an error status", async () => {
        silenceConsoleError();
        stubFetch({
            ok: false,
            status: 500,
            json: async () => ({
                success: false,
                error: { message: "boom", code: "INTERNAL_ERROR", details: [] },
            }),
        });

        await expect(
            measurementService.createMeasurement(INPUT)
        ).rejects.toThrow("500");
    });

    it("fails when the stored measurement is malformed", async () => {
        silenceConsoleError();
        stubFetch({
            ok: true,
            status: 201,
            json: async () => ({
                ...CREATED_RESPONSE,
                data: { ...CREATED_RESPONSE.data, waistCm: "94.8" },
            }),
        });

        await expect(measurementService.createMeasurement(INPUT)).rejects.toThrow();
    });
});
