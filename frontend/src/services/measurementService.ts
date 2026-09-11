import { sortByDateAsc } from "@/features/measurements/lib/metrics";
import { measurementResponseSchema } from "@/features/measurements/lib/schema";
import type { Measurement } from "@/features/measurements/types";
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
};
