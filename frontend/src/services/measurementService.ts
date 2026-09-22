import { sortByDateAsc } from "@/features/measurements/lib/metrics";
import {
    importErrorResponseSchema,
    importPreviewResponseSchema,
    importResultResponseSchema,
    measurementCreatedResponseSchema,
    measurementResponseSchema,
    trackingCatalogResponseSchema,
    trackingMetricResponseSchema,
} from "@/features/measurements/lib/schema";
import type {
    ImportPreview,
    ImportResult,
    Measurement,
    MeasurementInput,
    TrackingMetric,
    TrackingMetricCatalog,
} from "@/features/measurements/types";
import { API_BASE_URL } from "@/services/apiConfig";

const MEASUREMENTS_PATH = "/api/v1/measurements";

/** Code used when the API rejected a file for a reason this app cannot name. */
export const UNKNOWN_IMPORT_ERROR = "UNKNOWN";

/**
 * A file the API refused. It carries the stable code and the machine-readable
 * details, so the caller can word the rejection in the user's language.
 */
export class ImportRejectedError extends Error {
    readonly code: string;
    readonly details: readonly string[];

    constructor(code: string, details: readonly string[]) {
        super(`The import was rejected with ${code}`);
        this.name = "ImportRejectedError";
        this.code = code;
        this.details = details;
    }
}

async function rejectionOf(response: Response): Promise<ImportRejectedError> {
    try {
        const { error } = importErrorResponseSchema.parse(await response.json());
        return new ImportRejectedError(error.code, error.details);
    } catch {
        return new ImportRejectedError(UNKNOWN_IMPORT_ERROR, []);
    }
}

/** Sends the file and returns the parsed payload of the success envelope. */
async function postFile(path: string, file: File): Promise<unknown> {
    const body = new FormData();
    body.append("file", file);

    const response = await fetch(`${API_BASE_URL}${MEASUREMENTS_PATH}${path}`, {
        method: "POST",
        body,
        cache: "no-store",
    });

    if (!response.ok) {
        throw await rejectionOf(response);
    }

    return response.json();
}

/**
 * Data access boundary for body measurements.
 *
 * It calls the measurements API and validates the response before returning it,
 * so the rest of the app only ever sees well-formed measurements.
 */
export const measurementService = {
    async getTrackingCatalog(): Promise<TrackingMetricCatalog[]> {
        const response = await fetch(`${API_BASE_URL}${MEASUREMENTS_PATH}/catalog`, {
            cache: "no-store",
        });
        if (!response.ok) {
            throw new Error(`Measurements catalog API responded with ${response.status}`);
        }
        const { data } = trackingCatalogResponseSchema.parse(await response.json());
        return data;
    },

    async getTrackingMetric(metricCode: string): Promise<TrackingMetric> {
        const response = await fetch(
            `${API_BASE_URL}${MEASUREMENTS_PATH}/tracking/${encodeURIComponent(metricCode)}`,
            { cache: "no-store" }
        );
        if (!response.ok) {
            throw new Error(`Metric tracking API responded with ${response.status}`);
        }
        const { data } = trackingMetricResponseSchema.parse(await response.json());
        return data;
    },

    async getMeasurements(): Promise<Measurement[]> {
        try {
            const response = await fetch(`${API_BASE_URL}${MEASUREMENTS_PATH}`, {
                // Measurements change outside this app, so never serve a cached copy.
                cache: "no-store",
            });

            if (!response.ok) {
                throw new Error(
                    `Measurements API responded with ${response.status}`
                );
            }

            const { data } = measurementResponseSchema.parse(
                await response.json()
            );

            return sortByDateAsc(data);
        } catch (error) {
            console.error("Failed to load measurements:", error);
            throw error;
        }
    },

    /**
     * Registers the measurement of a day. The backend replaces the day's values
     * when it already had a measurement, so the same call handles both cases.
     */
    async createMeasurement(input: MeasurementInput): Promise<Measurement> {
        try {
            const response = await fetch(`${API_BASE_URL}${MEASUREMENTS_PATH}`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(input),
                cache: "no-store",
            });

            if (!response.ok) {
                throw new Error(
                    `Measurements API responded with ${response.status}`
                );
            }

            const { data } = measurementCreatedResponseSchema.parse(
                await response.json()
            );

            return data;
        } catch (error) {
            console.error("Failed to save measurement:", error);
            throw error;
        }
    },

    /**
     * Checks a file and reports what loading it would do, without storing
     * anything.
     */
    async previewMeasurementImport(file: File): Promise<ImportPreview> {
        try {
            const { data } = importPreviewResponseSchema.parse(
                await postFile("/import/preview", file)
            );

            return data;
        } catch (error) {
            console.error("Failed to preview the measurement import:", error);
            throw error;
        }
    },

    /**
     * Loads every measurement of a file, replacing the values of the days that
     * already had one.
     */
    async importMeasurements(file: File): Promise<ImportResult> {
        try {
            const { data } = importResultResponseSchema.parse(
                await postFile("/import", file)
            );

            return data;
        } catch (error) {
            console.error("Failed to import measurements:", error);
            throw error;
        }
    },
};
