import { z } from "zod";

const isoDate = z.string().regex(/^\d{4}-\d{2}-\d{2}$/);

export const clinicalEventSchema = z.object({
    id: z.string().min(1),
    code: z.string().regex(/^evt_\d{3,}$/),
    type: z.enum(["diagnosis", "medication", "measurement", "note"]),
    content: z.string().min(1),
    occurrenceDate: isoDate.nullable(),
    occurrenceDatePrecision: z.enum(["exact", "approximate", "unknown"]),
    recordDate: isoDate,
});

export const clinicalEventsResponseSchema = z.object({
    success: z.literal(true),
    data: z.array(clinicalEventSchema),
    messageCode: z.string(),
    message: z.string(),
});

export const clinicalEventResponseSchema = z.object({
    success: z.literal(true),
    data: clinicalEventSchema,
    messageCode: z.string(),
    message: z.string(),
});