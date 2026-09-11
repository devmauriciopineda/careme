package com.careme.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A single body measurement recorded on a given day.
 *
 * <p>The measurement {@code id} is the entity identity. The compact constructor
 * enforces the domain invariants, so an invalid measurement cannot exist.
 */
public record Measurement(UUID id, LocalDate date, BigDecimal weightKg, BigDecimal waistCm) {

    public Measurement {
        if (id == null) {
            throw new IllegalArgumentException("Measurement id must not be null");
        }
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
