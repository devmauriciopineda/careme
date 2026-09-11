"use client";

import { useId, useState, useTransition } from "react";
import type { FormEvent } from "react";
import type { ZodError } from "zod";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { registerMeasurement } from "@/features/measurements/actions";
import { todayIsoDate } from "@/features/measurements/lib/metrics";
import {
    measurementFormSchema,
    type MeasurementFormValues,
} from "@/features/measurements/lib/schema";
import { STRINGS } from "@/features/measurements/lib/strings";

type FieldErrors = Partial<Record<keyof MeasurementFormValues, string>>;
type Status = "idle" | "success" | "error";

function toFieldErrors(error: ZodError): FieldErrors {
    const errors: FieldErrors = {};

    for (const issue of error.issues) {
        const field = issue.path[0];
        if (field === "date" || field === "weightKg" || field === "waistCm") {
            errors[field] ??= issue.message;
        }
    }

    return errors;
}

/**
 * Registration form for the day's measurement.
 *
 * Holds the text the user types, validates it in Spanish, and hands the numeric
 * payload to the server action. A failed save keeps every value so the user can
 * retry without retyping.
 */
export function MeasurementForm() {
    const baseId = useId();
    const today = todayIsoDate();
    const [values, setValues] = useState<MeasurementFormValues>(() => ({
        date: today,
        weightKg: "",
        waistCm: "",
    }));
    const [errors, setErrors] = useState<FieldErrors>({});
    const [status, setStatus] = useState<Status>("idle");
    const [isPending, startTransition] = useTransition();

    const fieldIds = {
        date: `${baseId}-date`,
        weightKg: `${baseId}-weight`,
        waistCm: `${baseId}-waist`,
    };
    const errorIds = {
        date: `${baseId}-date-error`,
        weightKg: `${baseId}-weight-error`,
        waistCm: `${baseId}-waist-error`,
    };

    function updateField(field: keyof MeasurementFormValues, value: string) {
        setValues((current) => ({ ...current, [field]: value }));
        setErrors((current) => ({ ...current, [field]: undefined }));
        setStatus("idle");
    }

    function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();

        if (isPending) {
            return;
        }

        const result = measurementFormSchema.safeParse(values);
        if (!result.success) {
            setErrors(toFieldErrors(result.error));
            setStatus("idle");
            return;
        }

        setErrors({});
        startTransition(async () => {
            const outcome = await registerMeasurement(result.data);

            if (outcome.ok) {
                setValues((current) => ({
                    ...current,
                    weightKg: "",
                    waistCm: "",
                }));
                setStatus("success");
                return;
            }

            setStatus("error");
        });
    }

    return (
        <section
            aria-labelledby={`${baseId}-heading`}
            className="flex flex-col gap-3 rounded-xl bg-card p-4 ring-1 ring-foreground/10 sm:p-5"
        >
            <div className="flex flex-col gap-1">
                <h2
                    id={`${baseId}-heading`}
                    className="font-heading text-lg font-medium"
                >
                    {STRINGS.form.title}
                </h2>
                <p className="text-sm text-muted-foreground">
                    {STRINGS.form.description}
                </p>
            </div>

            <form
                noValidate
                onSubmit={handleSubmit}
                className="flex flex-col gap-4"
            >
                <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
                    <div className="flex flex-col gap-1.5">
                        <Label htmlFor={fieldIds.date}>
                            {STRINGS.form.date}
                        </Label>
                        <Input
                            id={fieldIds.date}
                            name="date"
                            type="date"
                            max={today}
                            value={values.date}
                            disabled={isPending}
                            onChange={(event) =>
                                updateField("date", event.target.value)
                            }
                            aria-invalid={errors.date ? true : undefined}
                            aria-describedby={
                                errors.date ? errorIds.date : undefined
                            }
                        />
                        {errors.date ? (
                            <p
                                id={errorIds.date}
                                role="alert"
                                className="text-sm text-destructive"
                            >
                                {errors.date}
                            </p>
                        ) : null}
                    </div>

                    <div className="flex flex-col gap-1.5">
                        <Label htmlFor={fieldIds.weightKg}>
                            {STRINGS.form.weight}
                        </Label>
                        <Input
                            id={fieldIds.weightKg}
                            name="weightKg"
                            type="text"
                            inputMode="decimal"
                            autoComplete="off"
                            value={values.weightKg}
                            disabled={isPending}
                            onChange={(event) =>
                                updateField("weightKg", event.target.value)
                            }
                            aria-invalid={errors.weightKg ? true : undefined}
                            aria-describedby={
                                errors.weightKg ? errorIds.weightKg : undefined
                            }
                        />
                        {errors.weightKg ? (
                            <p
                                id={errorIds.weightKg}
                                role="alert"
                                className="text-sm text-destructive"
                            >
                                {errors.weightKg}
                            </p>
                        ) : null}
                    </div>

                    <div className="flex flex-col gap-1.5">
                        <Label htmlFor={fieldIds.waistCm}>
                            {STRINGS.form.waist}
                        </Label>
                        <Input
                            id={fieldIds.waistCm}
                            name="waistCm"
                            type="text"
                            inputMode="decimal"
                            autoComplete="off"
                            value={values.waistCm}
                            disabled={isPending}
                            onChange={(event) =>
                                updateField("waistCm", event.target.value)
                            }
                            aria-invalid={errors.waistCm ? true : undefined}
                            aria-describedby={
                                errors.waistCm ? errorIds.waistCm : undefined
                            }
                        />
                        {errors.waistCm ? (
                            <p
                                id={errorIds.waistCm}
                                role="alert"
                                className="text-sm text-destructive"
                            >
                                {errors.waistCm}
                            </p>
                        ) : null}
                    </div>
                </div>

                <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                    <Button type="submit" disabled={isPending}>
                        {isPending
                            ? STRINGS.form.submitting
                            : STRINGS.form.submit}
                    </Button>

                    <div aria-live="polite" className="min-h-5 text-sm">
                        {status === "success" ? (
                            <p role="status" className="text-foreground">
                                {STRINGS.form.success}
                            </p>
                        ) : null}
                        {status === "error" ? (
                            <p role="alert" className="text-destructive">
                                {STRINGS.form.saveFailed}
                            </p>
                        ) : null}
                    </div>
                </div>
            </form>
        </section>
    );
}
