package com.careme.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A measurement that has no database identity yet, used when several ones are
 * written at once.
 *
 * <p>Unlike {@link Measurement}, it carries no {@code id}: the value is what a
 * daily record would hold if it does not exist yet. The compact constructor
 * enforces the same invariants, so an invalid draft cannot reach the database.
 */
public record MeasurementDraft(LocalDate date, BigDecimal weightKg, BigDecimal waistCm) {

    public MeasurementDraft {
        if (date == null) {
            throw new IllegalArgumentException("Measurement date must not be null");
        }
        requirePositive(weightKg, "weightKg");
        requirePositive(waistCm, "waistCm");
    }

    private static void requirePositive(BigDecimal value, String field) {
        if (value == null || value.signum() <= 0) {
            throw new IllegalArgumentException("Measurement " + field + " must be positive");
        }
    }
}
