package com.careme.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * A measurement of one metric on one day.
 *
 * <p>It carries the metric, the values in the metric's reference unit, an exact
 * calendar day without time, and its provenance: the consultation it came from,
 * when it came from one. A composite metric is a single measurement with several
 * values, never several measurements.
 *
 * <p>The compact constructor enforces the domain invariants, so an invalid
 * measurement cannot exist.
 */
public record MetricMeasurement(
        UUID id,
        Metric metric,
        MeasurementUnit unit,
        LocalDate date,
        String encounterCode,
        OffsetDateTime createdAt,
        List<MeasurementValue> values) {

    public MetricMeasurement {
        if (id == null) {
            throw new IllegalArgumentException("Measurement id must not be null");
        }
        if (metric == null) {
            throw new IllegalArgumentException("Measurement metric must not be null");
        }
        if (date == null) {
            throw new IllegalArgumentException("Measurement date must not be null");
        }
        if (unit != metric.referenceUnit()) {
            throw new IllegalArgumentException(
                    "Measurement of " + metric.code() + " must be stored in " + metric.referenceUnit().code());
        }
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Measurement values must not be empty");
        }

        Set<String> seen = new HashSet<>();
        for (MeasurementValue value : values) {
            if (!seen.add(value.component())) {
                throw new IllegalArgumentException(
                        "Measurement repeats the component " + value.component());
            }
            if (metric.components().stream().noneMatch(component -> component.key().equals(value.component()))) {
                throw new IllegalArgumentException(
                        "Measurement of " + metric.code() + " has no component " + value.component());
            }
            if (!metric.admits(value.component(), value.value())) {
                throw new IllegalArgumentException(
                        "Measurement value is not admissible for " + metric.code() + "/" + value.component());
            }
        }
        if (seen.size() != metric.components().size()) {
            throw new IllegalArgumentException(
                    "Measurement of " + metric.code() + " must carry every component");
        }
    }

    /** @return the value of the component, if the measurement carries it */
    public Optional<BigDecimal> value(String component) {
        return values.stream()
                .filter(value -> value.component().equals(component))
                .map(MeasurementValue::value)
                .findFirst();
    }
}
