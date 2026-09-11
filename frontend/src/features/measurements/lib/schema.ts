import { z } from "zod";

import { isFutureIsoDate, todayIsoDate } from "./metrics";
import { STRINGS } from "./strings";

import type { MeasurementInput } from "../types";

const ISO_DATE_PATTERN = /^\d{4}-\d{2}-\d{2}$/;
/** A signed number with an optional decimal part, dot or comma separated. */
const NUMBER_PATTERN = /^[+-]?\d+(?:[.,]\d+)?$/;
/** A signed number with at most one decimal digit. */
const SINGLE_DECIMAL_PATTERN = /^[+-]?\d+(?:[.,]\d)?$/;

const WEIGHT_MAX_KG = 500;
const WAIST_MAX_CM = 400;

/** Validates measurement payloads at the data boundary, so a malformed or changed
 * API response fails here instead of deep inside the UI. */
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

/** Success envelope returned when a single measurement is registered. */
export const measurementCreatedResponseSchema = z.object({
    success: z.literal(true),
    data: measurementSchema,
    messageCode: z.string(),
    message: z.string(),
});

/** A number that fits in one decimal place, within JavaScript rounding noise. */
function hasSingleDecimal(value: number): boolean {
    return Number.isFinite(value) && Math.abs(value * 10 - Math.round(value * 10)) < 1e-9;
}

/**
 * The payload the server action accepts. It mirrors the backend rules so an
 * invalid registration never leaves the server.
 */
export const measurementInputSchema = z.object({
    date: z
        .string()
        .regex(ISO_DATE_PATTERN)
        .refine((date) => !isFutureIsoDate(date, todayIsoDate())),
    weightKg: z.number().positive().max(WEIGHT_MAX_KG).refine(hasSingleDecimal),
    waistCm: z.number().positive().max(WAIST_MAX_CM).refine(hasSingleDecimal),
});

/** The raw text the form holds while the user types. */
export type MeasurementFormValues = {
    date: string;
    weightKg: string;
    waistCm: string;
};

function validateMetric(
    raw: string,
    max: number,
    field: "weightKg" | "waistCm",
    ctx: z.RefinementCtx
) {
    const value = raw.trim();

    if (value.length === 0) {
        ctx.addIssue({ code: "custom", path: [field], message: STRINGS.form.validation.required });
        return;
    }
    if (!NUMBER_PATTERN.test(value)) {
        ctx.addIssue({ code: "custom", path: [field], message: STRINGS.form.validation.notNumber });
        return;
    }
    if (!SINGLE_DECIMAL_PATTERN.test(value)) {
        ctx.addIssue({ code: "custom", path: [field], message: STRINGS.form.validation.oneDecimal });
        return;
    }

    const numeric = Number(value.replace(",", "."));
    if (numeric <= 0) {
        ctx.addIssue({ code: "custom", path: [field], message: STRINGS.form.validation.notPositive });
        return;
    }
    if (numeric > max) {
        ctx.addIssue({ code: "custom", path: [field], message: STRINGS.form.validation.tooLarge(max) });
    }
}

/**
 * Validates the form's text fields and turns them into the numeric payload the
 * server action expects, with every message in the user's language.
 */
export const measurementFormSchema = z
    .object({
        date: z.string(),
        weightKg: z.string(),
        waistCm: z.string(),
    })
    .superRefine((values, ctx) => {
        const date = values.date.trim();
        if (date.length === 0) {
            ctx.addIssue({ code: "custom", path: ["date"], message: STRINGS.form.validation.required });
        } else if (!ISO_DATE_PATTERN.test(date)) {
            ctx.addIssue({ code: "custom", path: ["date"], message: STRINGS.form.validation.invalidDate });
        } else if (isFutureIsoDate(date, todayIsoDate())) {
            ctx.addIssue({ code: "custom", path: ["date"], message: STRINGS.form.validation.futureDate });
        }

        validateMetric(values.weightKg, WEIGHT_MAX_KG, "weightKg", ctx);
        validateMetric(values.waistCm, WAIST_MAX_CM, "waistCm", ctx);
    })
    .transform(
        (values): MeasurementInput => ({
            date: values.date.trim(),
            weightKg: Number(values.weightKg.trim().replace(",", ".")),
            waistCm: Number(values.waistCm.trim().replace(",", ".")),
        })
    );
