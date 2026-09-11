"use server";

import { revalidatePath } from "next/cache";

import { measurementInputSchema } from "@/features/measurements/lib/schema";
import { measurementService } from "@/services/measurementService";

/**
 * Result of a registration attempt, small enough to cross the server boundary.
 */
export type RegisterMeasurementResult =
    | { ok: true }
    | { ok: false; errorCode: "VALIDATION_ERROR" | "SAVE_FAILED" };

/**
 * Registers the measurement of a day.
 *
 * Revalidates the payload even though the form already did, then refreshes the
 * tracking path so the charts and the daily detail reflect the new record
 * without the user reloading.
 */
export async function registerMeasurement(
    input: unknown
): Promise<RegisterMeasurementResult> {
    const parsed = measurementInputSchema.safeParse(input);

    if (!parsed.success) {
        return { ok: false, errorCode: "VALIDATION_ERROR" };
    }

    try {
        await measurementService.createMeasurement(parsed.data);
    } catch (error) {
        console.error("Failed to register measurement:", error);
        return { ok: false, errorCode: "SAVE_FAILED" };
    }

    revalidatePath("/");
    return { ok: true };
}
