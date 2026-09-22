"use server";

import { revalidatePath } from "next/cache";

import { measurementInputSchema } from "@/features/measurements/lib/schema";
import { metricStateOf, type MetricState } from "@/features/measurements/lib/tracking";
import type { ImportPreview, ImportResult } from "@/features/measurements/types";
import {
    UNKNOWN_IMPORT_ERROR,
    ImportRejectedError,
    measurementService,
} from "@/services/measurementService";

/**
 * Result of a registration attempt, small enough to cross the server boundary.
 */
export type RegisterMeasurementResult =
    | { ok: true }
    | { ok: false; errorCode: "VALIDATION_ERROR" | "SAVE_FAILED" };

/** Why a file was refused, in terms the client can word itself. */
export type ImportErrorInfo = {
    code: string;
    details: string[];
};

export type PreviewImportResult =
    | { ok: true; preview: ImportPreview }
    | { ok: false; error: ImportErrorInfo };

export type ConfirmImportResult =
    | { ok: true; result: ImportResult }
    | { ok: false; error: ImportErrorInfo };

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

/**
 * Re-reads one metric of the tracking view.
 *
 * It runs on the server, so the browser never needs the backend address, and it
 * returns a state instead of throwing: a metric that cannot be read becomes an
 * isolated error the view can retry while the others stay on screen.
 */
export async function loadTrackingMetric(code: string): Promise<MetricState> {
    try {
        return metricStateOf(await measurementService.getTrackingMetric(code));
    } catch (error) {
        console.error(`Failed to load metric ${code}:`, error);
        return { status: "error" };
    }
}

/** The uploaded file, or `null` when the request carried none. */
function uploadedFile(formData: FormData): File | null {
    const file = formData.get("file");

    return file instanceof File ? file : null;
}

function toImportError(error: unknown): ImportErrorInfo {
    if (error instanceof ImportRejectedError) {
        return { code: error.code, details: [...error.details] };
    }

    return { code: UNKNOWN_IMPORT_ERROR, details: [] };
}

/**
 * Checks a file and reports what loading it would do, storing nothing. The same
 * validation runs again on the load, so this is a preview and not a promise.
 */
export async function previewMeasurementImport(
    formData: FormData
): Promise<PreviewImportResult> {
    const file = uploadedFile(formData);

    if (file === null) {
        return { ok: false, error: { code: "EMPTY_FILE", details: [] } };
    }

    try {
        return { ok: true, preview: await measurementService.previewMeasurementImport(file) };
    } catch (error) {
        console.error("Failed to preview the measurement import:", error);
        return { ok: false, error: toImportError(error) };
    }
}

/**
 * Loads every measurement of a file. Revalidates the tracking path so the
 * charts and the daily detail reflect the load without the user reloading.
 */
export async function confirmMeasurementImport(
    formData: FormData
): Promise<ConfirmImportResult> {
    const file = uploadedFile(formData);

    if (file === null) {
        return { ok: false, error: { code: "EMPTY_FILE", details: [] } };
    }

    let result: ImportResult;

    try {
        result = await measurementService.importMeasurements(file);
    } catch (error) {
        console.error("Failed to import measurements:", error);
        return { ok: false, error: toImportError(error) };
    }

    revalidatePath("/");
    return { ok: true, result };
}
