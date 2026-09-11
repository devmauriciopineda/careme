import { sortByDateAsc } from "@/features/measurements/lib/metrics";
import {
    measurementCreatedResponseSchema,
    measurementResponseSchema,
} from "@/features/measurements/lib/schema";
import type { Measurement, MeasurementInput } from "@/features/measurements/types";
import { API_BASE_URL } from "@/services/apiConfig";

const MEASUREMENTS_PATH = "/api/v1/measurements";

/**
 * Data access boundary for body measurements.
 *
 * It calls the measurements API and validates the response before returning it,
 * so the rest of the app only ever sees well-formed measurements.
 */
export const measurementService = {
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
};
