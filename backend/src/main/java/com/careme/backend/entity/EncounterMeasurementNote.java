package com.careme.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * A measurement the person mentioned during a consultation, kept as a note.
 *
 * <p>Like a clinical-fact note it is not a registered measurement: it carries what
 * the conversation collected so the close can register it, and it never reaches the
 * tracking on its own.
 *
 * <p>The values are already expressed in the metric's reference unit, because the
 * conversion happens when the measurement is collected and never depends on the
 * assistant's judgement; {@code declaredUnit} keeps the unit the person actually
 * stated, when they stated one.
 *
 * @param metricCode   the metric the person named
 * @param values       the component values, in the metric's reference unit
 * @param declaredUnit the unit the person stated, or {@code null} when they stated none
 * @param date         the exact calendar day, when the note carries one
 * @param datePrecision the precision of the date the person gave
 * @param dateText     the person's original temporal expression, when there was one
 */
public record EncounterMeasurementNote(
        String metricCode,
        Map<String, BigDecimal> values,
        String declaredUnit,
        LocalDate date,
        ClinicalEvent.DatePrecision datePrecision,
        String dateText) {

    public EncounterMeasurementNote {
        if (metricCode == null || metricCode.isBlank()) {
            throw new IllegalArgumentException("Measurement note metric must not be blank");
        }
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Measurement note values must not be empty");
        }
        values = Map.copyOf(values);
        if (datePrecision == null) {
            throw new IllegalArgumentException("Measurement note date precision must not be null");
        }
        if (datePrecision == ClinicalEvent.DatePrecision.EXACT && date == null) {
            throw new IllegalArgumentException("Exact measurement note date must not be null");
        }
    }
}
