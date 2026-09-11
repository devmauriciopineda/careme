import { z } from "zod";

const ISO_DATE_PATTERN = /^\d{4}-\d{2}-\d{2}$/;

/**
 * Validates measurement payloads at the data boundary, so a malformed or changed
 * API response fails here instead of deep inside the UI.
 */
export const measurementSchema = z.object({
    id: z.string().min(1),
    date: z
        .string()
        .regex(ISO_DATE_PATTERN, "Expected a date in yyyy-MM-dd format"),
    weightKg: z.number().positive().max(500),
    waistCm: z.number().positive().max(400),
});

export const measurementsSchema = z.array(measurementSchema);

/** Success envelope returned by the backend, with `data` as the measurement list. */
export const measurementResponseSchema = z.object({
    success: z.literal(true),
    data: measurementsSchema,
    messageCode: z.string(),
    message: z.string(),
});
