package com.careme.backend.entity;

import java.math.BigDecimal;

/**
 * One component value of a measurement.
 *
 * @param component the component key of the metric, such as {@code value} or
 *                  {@code systolic}
 * @param value     the numeric value, already expressed in the metric's reference unit
 */
public record MeasurementValue(String component, BigDecimal value) {

    public MeasurementValue {
        if (component == null || component.isBlank()) {
            throw new IllegalArgumentException("Measurement value component must not be blank");
        }
        if (value == null || value.signum() <= 0) {
            throw new IllegalArgumentException("Measurement value must be positive");
        }
    }
}
