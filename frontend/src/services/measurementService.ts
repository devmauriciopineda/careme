import { RAW_MEASUREMENTS } from "@/features/measurements/data/measurements.mock";
import { sortByDateAsc } from "@/features/measurements/lib/metrics";
import { measurementsSchema } from "@/features/measurements/lib/schema";
import type { Measurement } from "@/features/measurements/types";

/**
 * Data access boundary for body measurements.
 *
 * It returns the local mock dataset today. When the measurements API exists,
 * this becomes a `fetch` against `NEXT_PUBLIC_API_BASE_URL` and nothing else in
 * the app has to change: the signature is already async and the payload is
 * already validated here.
 */
export const measurementService = {
    async getMeasurements(): Promise<Measurement[]> {
        try {
            const measurements = measurementsSchema.parse(RAW_MEASUREMENTS);
            return sortByDateAsc(measurements);
        } catch (error) {
            console.error("Failed to load measurements:", error);
            throw error;
        }
    },
};
