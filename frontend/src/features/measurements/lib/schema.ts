import { z } from "zod";

const ISO_DATE_PATTERN = /^\d{4}-\d{2}-\d{2}$/;

/**
 * Validates measurement payloads at the data boundary. Today it guards the mock
 * dataset; once the API exists it guards the response instead, with no changes
 * needed in the UI.
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
