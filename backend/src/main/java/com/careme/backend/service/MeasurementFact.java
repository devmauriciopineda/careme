package com.careme.backend.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import com.careme.backend.entity.MeasurementValue;
import com.careme.backend.entity.Metric;
import com.careme.backend.entity.MetricMeasurement;

/**
 * One measurement as it is shown to the person: its metric, its values in the
 * reference unit of that metric, the unit itself and its exact date, all taken
 * from the stored measurement.
 *
 * <p>It is built by the application and never by the assistant, so the value, the
 * unit and the date that reach the person are the ones the tracking holds. Its
 * {@link #text()} is the whole measurement written out, with the unit of reference
 * made explicit, and it is the form the application checks the answer against.
 */
public record MeasurementFact(
        String reference,
        String metric,
        String label,
        String unit,
        String date,
        List<MeasurementFactValue> values,
        String text) {

    public MeasurementFact {
        values = values == null ? List.of() : List.copyOf(values);
    }

    /** One component of the measurement, written exactly as it is stored. */
    public record MeasurementFactValue(String component, String value) {
    }

    /** Reads a stored measurement without altering any of its data. */
    public static MeasurementFact of(MetricMeasurement measurement) {
        Metric metric = measurement.metric();
        String unit = measurement.unit().code();
        String date = measurement.date().toString();
        List<MeasurementFactValue> values = measurement.values().stream()
                .map(value -> new MeasurementFactValue(value.component(), written(value)))
                .toList();
        return new MeasurementFact(
                metric.code() + "@" + date,
                metric.code(),
                metric.label(),
                unit,
                date,
                values,
                text(metric, values, unit, date));
    }

    /**
     * The value as written, without a trailing zero scale. Dropping trailing zeros
     * is not rounding: the magnitude the tracking holds is the one shown.
     */
    private static String written(MeasurementValue value) {
        return plain(value.value());
    }

    private static String plain(BigDecimal value) {
        BigDecimal trimmed = value.stripTrailingZeros();
        return trimmed.scale() < 0 ? trimmed.setScale(0).toPlainString() : trimmed.toPlainString();
    }

    private static String text(
            Metric metric, List<MeasurementFactValue> values, String unit, String date) {
        String written = values.stream()
                .map(value -> metric.isComposite()
                        ? componentLabel(value.component()) + " " + value.value() + " " + unit
                        : value.value() + " " + unit)
                .collect(Collectors.joining(", "));
        return metric.label() + ": " + written + " (" + date + ")";
    }

    /** The component named in Spanish, so a composite metric is not two bare numbers. */
    private static String componentLabel(String component) {
        return switch (component) {
            case "systolic" -> "sistólica";
            case "diastolic" -> "diastólica";
            default -> component;
        };
    }
}
