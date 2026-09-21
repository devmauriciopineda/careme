package com.careme.backend.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Verifies the invariants of the small carriers of the measurement model: a component and
 * its admissible range, a draft, and the measurement note the consultation collects.
 */
class MeasurementModelInvariantsTest {

    @Test
    void aComponentNeedsAKeyAndAnOrderedPositiveRange() {
        assertThatThrownBy(() -> new MetricComponent("  ", BigDecimal.ONE, BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MetricComponent("value", null, BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MetricComponent("value", BigDecimal.ONE, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MetricComponent("value", BigDecimal.TEN, BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MetricComponent("value", BigDecimal.valueOf(-1), BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aComponentAdmitsOnlyTheValuesInsideItsRange() {
        MetricComponent component = new MetricComponent("value", BigDecimal.ONE, BigDecimal.TEN);

        assertThat(component.admits(null)).isFalse();
        assertThat(component.admits(BigDecimal.ZERO)).isFalse();
        assertThat(component.admits(BigDecimal.ONE)).isTrue();
        assertThat(component.admits(BigDecimal.TEN)).isTrue();
        assertThat(component.admits(BigDecimal.valueOf(11))).isFalse();
    }

    @Test
    void aDraftNeedsAMetricADateAndValues() {
        assertThatThrownBy(() -> new MetricMeasurementDraft(
                null, LocalDate.of(2026, 9, 6), null, Map.of("value", BigDecimal.ONE)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MetricMeasurementDraft(
                Metric.WEIGHT, null, null, Map.of("value", BigDecimal.ONE)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MetricMeasurementDraft(Metric.WEIGHT, LocalDate.of(2026, 9, 6), null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MetricMeasurementDraft(
                Metric.WEIGHT, LocalDate.of(2026, 9, 6), null, Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aMeasurementNoteNeedsAMetricValuesAndAPrecision() {
        Map<String, BigDecimal> values = Map.of("value", BigDecimal.ONE);

        assertThatThrownBy(() -> new EncounterMeasurementNote(
                "  ", values, null, LocalDate.of(2026, 9, 6), ClinicalEvent.DatePrecision.EXACT, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new EncounterMeasurementNote(
                "weight", null, null, LocalDate.of(2026, 9, 6), ClinicalEvent.DatePrecision.EXACT, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new EncounterMeasurementNote(
                "weight", Map.of(), null, LocalDate.of(2026, 9, 6), ClinicalEvent.DatePrecision.EXACT, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new EncounterMeasurementNote(
                "weight", values, null, LocalDate.of(2026, 9, 6), null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new EncounterMeasurementNote(
                "weight", values, null, null, ClinicalEvent.DatePrecision.EXACT, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aMeasurementNoteKeepsTheValuesItWasGiven() {
        EncounterMeasurementNote note = new EncounterMeasurementNote(
                "weight",
                Map.of("value", BigDecimal.ONE),
                "lb",
                LocalDate.of(2026, 9, 6),
                ClinicalEvent.DatePrecision.EXACT,
                "ayer");

        assertThat(note.metricCode()).isEqualTo("weight");
        assertThat(note.declaredUnit()).isEqualTo("lb");
        assertThat(note.dateText()).isEqualTo("ayer");
    }
}
