package com.careme.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * A measurement that has no database identity yet: the values are already
 * expressed in the metric's reference unit and the date is already exact.
 *
 * @param metric        the metric being recorded
 * @param date          the exact calendar day of the measurement
 * @param encounterCode the consultation the measurement came from, or {@code null}
 * @param values        the component values, keyed by component
 */
public record MetricMeasurementDraft(
        Metric metric,
        LocalDate date,
        String encounterCode,
        Map<String, BigDecimal> values) {

    public MetricMeasurementDraft {
        if (metric == null) {
            throw new IllegalArgumentException("Measurement draft metric must not be null");
        }
        if (date == null) {
            throw new IllegalArgumentException("Measurement draft date must not be null");
        }
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Measurement draft values must not be empty");
        }
    }
}
