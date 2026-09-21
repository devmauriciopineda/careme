package com.careme.backend.entity;

import java.math.BigDecimal;

/**
 * One value of a metric: its key and the admissible range for it.
 *
 * <p>A simple metric has a single component ({@code value}); a composite metric
 * has one per value, so a blood pressure is one measurement with two components
 * instead of two measurements.
 *
 * @param key the component key persisted alongside the value
 * @param min the lowest admissible value for the component, inclusive
 * @param max the highest admissible value for the component, inclusive
 */
public record MetricComponent(String key, BigDecimal min, BigDecimal max) {

    public MetricComponent {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Metric component key must not be blank");
        }
        if (min == null || max == null || min.signum() <= 0 || min.compareTo(max) > 0) {
            throw new IllegalArgumentException("Metric component range must be positive and ordered");
        }
    }

    /**
     * @return whether the value falls inside the admissible range
     */
    public boolean admits(BigDecimal value) {
        return value != null && value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
    }
}
