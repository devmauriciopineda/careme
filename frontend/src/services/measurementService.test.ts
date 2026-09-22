import { afterEach, describe, expect, it, vi } from "vitest";

import {
    ImportRejectedError,
    UNKNOWN_IMPORT_ERROR,
    measurementService,
} from "./measurementService";

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

describe("measurementService tracking reads", () => {
    it("loads the metric catalog with component definitions", async () => {
        const fetchMock = stubFetch({
            ok: true,
            status: 200,
            json: async () => ({
                success: true,
                messageCode: "SUCCESS",
                message: "ok",
                data: [{
                    code: "blood_pressure",
                    label: "presión arterial",
                    referenceUnit: "mmHg",
                    components: [{ key: "systolic" }, { key: "diastolic" }],
                }],
            }),
        });

        const catalog = await measurementService.getTrackingCatalog();

        expect(fetchMock).toHaveBeenCalledWith(
            expect.stringContaining("/api/v1/measurements/catalog"),
            expect.objectContaining({ cache: "no-store" })
        );
        expect(catalog[0].components.map((component) => component.key)).toEqual([
            "systolic",
            "diastolic",
        ]);
    });

    it("loads composite measurements as one metric", async () => {
        stubFetch({
            ok: true,
            status: 200,
            json: async () => ({
                success: true,
                messageCode: "SUCCESS",
                message: "ok",
                data: {
                    code: "blood_pressure",
                    label: "presión arterial",
                    unit: "mmHg",
                    measurements: [{
                        id: "bp-1",
                        date: "2026-09-06",
                        values: [
                            { component: "systolic", value: 120 },
                            { component: "diastolic", value: 80 },
                        ],
                    }],
                },
            }),
        });

        const metric = await measurementService.getTrackingMetric("blood_pressure");

        expect(metric.measurements).toHaveLength(1);
        expect(metric.measurements[0].values).toHaveLength(2);
    });

    it("rejects malformed tracking payloads", async () => {
        silenceConsoleError();
        stubFetch({
            ok: true,
            status: 200,
            json: async () => ({ success: true, data: [] }),
        });

        await expect(measurementService.getTrackingCatalog()).rejects.toThrow();
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

const IMPORT_PREVIEW_RESPONSE = {
    success: true,
    messageCode: "SUCCESS",
    message: "Operation completed successfully",
    data: {
        rows: [
            {
                date: "2026-09-10",
                weightKg: 80.1,
                waistCm: 94.8,
                replacesExisting: true,
            },
        ],
        totalRows: 1,
        newCount: 0,
        replacedCount: 1,
        ignoredCount: 0,
    },
};

const IMPORT_RESULT_RESPONSE = {
    success: true,
    messageCode: "SUCCESS",
    message: "Operation completed successfully",
    data: { createdCount: 1, replacedCount: 1, ignoredCount: 0, totalRows: 2 },
};

function importFile(): File {
    return new File(
        ["date,weight_kg,abdominal_circumference_cm\n"],
        "measurements.csv",
        { type: "text/csv" }
    );
}

async function rejectionOf(
    promise: Promise<unknown>
): Promise<ImportRejectedError> {
    try {
        await promise;
    } catch (error) {
        if (error instanceof ImportRejectedError) {
            return error;
        }
        throw error;
    }

    throw new Error("The request was expected to be rejected");
}

describe("measurementService.previewMeasurementImport", () => {
    it("uploads the file and returns what loading it would do", async () => {
        const fetchMock = stubFetch({
            ok: true,
            status: 200,
            json: async () => IMPORT_PREVIEW_RESPONSE,
        });

        const preview = await measurementService.previewMeasurementImport(
            importFile()
        );

        const [, init] = fetchMock.mock.calls[0];
        expect(fetchMock).toHaveBeenCalledWith(
            expect.stringContaining("/api/v1/measurements/import/preview"),
            expect.objectContaining({ method: "POST", cache: "no-store" })
        );
        expect((init.body as FormData).get("file")).toBeInstanceOf(File);
        expect(preview.replacedCount).toBe(1);
        expect(preview.rows[0].replacesExisting).toBe(true);
    });

    it("fails when the preview payload is malformed", async () => {
        silenceConsoleError();
        stubFetch({
            ok: true,
            status: 200,
            json: async () => ({
                ...IMPORT_PREVIEW_RESPONSE,
                data: { ...IMPORT_PREVIEW_RESPONSE.data, totalRows: "1" },
            }),
        });

        await expect(
            measurementService.previewMeasurementImport(importFile())
        ).rejects.toThrow();
    });

    it("reports why the file was refused", async () => {
        silenceConsoleError();
        stubFetch({
            ok: false,
            status: 400,
            json: async () => ({
                success: false,
                error: {
                    message: "The file contains rows that are not valid measurements",
                    code: "INVALID_IMPORT_VALUE",
                    details: ["2|weight_kg|NOT_POSITIVE"],
                },
            }),
        });

        const rejection = await rejectionOf(
            measurementService.previewMeasurementImport(importFile())
        );

        expect(rejection.code).toBe("INVALID_IMPORT_VALUE");
        expect(rejection.details).toEqual(["2|weight_kg|NOT_POSITIVE"]);
    });

    it("falls back to an unknown code when the refusal cannot be read", async () => {
        silenceConsoleError();
        stubFetch({
            ok: false,
            status: 413,
            json: async () => {
                throw new Error("no body");
            },
        });

        const rejection = await rejectionOf(
            measurementService.previewMeasurementImport(importFile())
        );

        expect(rejection.code).toBe(UNKNOWN_IMPORT_ERROR);
        expect(rejection.details).toEqual([]);
    });
});

describe("measurementService.importMeasurements", () => {
    it("uploads the file and returns what the load did", async () => {
        const fetchMock = stubFetch({
            ok: true,
            status: 200,
            json: async () => IMPORT_RESULT_RESPONSE,
        });

        const result = await measurementService.importMeasurements(importFile());

        expect(fetchMock).toHaveBeenCalledWith(
            expect.stringContaining("/api/v1/measurements/import"),
            expect.objectContaining({ method: "POST", cache: "no-store" })
        );
        expect(result).toEqual(IMPORT_RESULT_RESPONSE.data);
    });

    it("reports a rejected load", async () => {
        silenceConsoleError();
        stubFetch({
            ok: false,
            status: 400,
            json: async () => ({
                success: false,
                error: {
                    message: "The file does not carry exactly the required columns",
                    code: "INVALID_FILE_STRUCTURE",
                    details: ["expected=date"],
                },
            }),
        });

        const rejection = await rejectionOf(
            measurementService.importMeasurements(importFile())
        );

        expect(rejection.code).toBe("INVALID_FILE_STRUCTURE");
    });
});
