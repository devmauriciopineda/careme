package com.careme.backend.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * Verifies the invariants of a measurement: its metric, its reference unit, an
 * exact date, its provenance and its component values.
 */
class MetricMeasurementTest {

    private static final UUID ID = UUID.fromString("6f1d0f9a-1f5f-4a0e-9a4e-4f0f6a1c2b3d");

    private static MetricMeasurement bloodPressure(String systolic, String diastolic) {
        return new MetricMeasurement(
                ID,
                Metric.BLOOD_PRESSURE,
                MeasurementUnit.MMHG,
                LocalDate.of(2026, 9, 6),
                "enc_003",
                null,
                List.of(
                        new MeasurementValue("systolic", new BigDecimal(systolic)),
                        new MeasurementValue("diastolic", new BigDecimal(diastolic))));
    }

    @Test
    void representsBloodPressureAsASingleMeasurementWithItsTwoValues() {
        MetricMeasurement measurement = bloodPressure("145", "92");

        assertThat(measurement.values()).hasSize(2);
        assertThat(measurement.value("systolic")).contains(new BigDecimal("145"));
        assertThat(measurement.value("diastolic")).contains(new BigDecimal("92"));
    }

    @Test
    void keepsTheMetricAndTheReferenceUnitItWasStoredWith() {
        MetricMeasurement measurement = bloodPressure("145", "92");

        assertThat(measurement.metric()).isEqualTo(Metric.BLOOD_PRESSURE);
        assertThat(measurement.unit()).isEqualTo(MeasurementUnit.MMHG);
    }

    @Test
    void refusesAValueExpressedInAUnitOtherThanTheReferenceOne() {
        assertThatThrownBy(() -> new MetricMeasurement(
                ID, Metric.WEIGHT, MeasurementUnit.LB, LocalDate.of(2026, 9, 6), null, null,
                List.of(new MeasurementValue("value", new BigDecimal("70")))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refusesAValueOutsideTheRangeOfItsMetric() {
        assertThatThrownBy(() -> new MetricMeasurement(
                ID, Metric.WEIGHT, MeasurementUnit.KG, LocalDate.of(2026, 9, 6), null, null,
                List.of(new MeasurementValue("value", new BigDecimal("501")))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refusesACompositeMetricWithAMissingComponent() {
        assertThatThrownBy(() -> new MetricMeasurement(
                ID, Metric.BLOOD_PRESSURE, MeasurementUnit.MMHG, LocalDate.of(2026, 9, 6), null, null,
                List.of(new MeasurementValue("systolic", new BigDecimal("145")))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refusesAMeasurementWithoutAnExactDate() {
        assertThatThrownBy(() -> new MetricMeasurement(
                ID, Metric.WEIGHT, MeasurementUnit.KG, null, null, null,
                List.of(new MeasurementValue("value", new BigDecimal("70")))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refusesANonPositiveValue() {
        assertThatThrownBy(() -> new MeasurementValue("value", new BigDecimal("0")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void carriesItsProvenanceWhenItComesFromAConsultation() {
        assertThat(bloodPressure("145", "92").encounterCode()).isEqualTo("enc_003");
    }

    @Test
    void refusesARepeatedComponentOfACompositeMetric() {
        assertThatThrownBy(() -> new MetricMeasurement(
                ID, Metric.BLOOD_PRESSURE, MeasurementUnit.MMHG, LocalDate.of(2026, 9, 6), null, null,
                List.of(
                        new MeasurementValue("systolic", new BigDecimal("145")),
                        new MeasurementValue("systolic", new BigDecimal("92")))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refusesAComponentTheMetricDoesNotHave() {
        assertThatThrownBy(() -> new MetricMeasurement(
                ID, Metric.WEIGHT, MeasurementUnit.KG, LocalDate.of(2026, 9, 6), null, null,
                List.of(
                        new MeasurementValue("value", new BigDecimal("70")),
                        new MeasurementValue("systolic", new BigDecimal("145")))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refusesAMeasurementWithoutValues() {
        assertThatThrownBy(() -> new MetricMeasurement(
                ID, Metric.WEIGHT, MeasurementUnit.KG, LocalDate.of(2026, 9, 6), null, null, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void givesNoValueForAComponentItDoesNotCarry() {
        assertThat(bloodPressure("145", "92").value("pulse")).isEmpty();
    }
}
