package com.careme.backend.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class MeasurementTest {

    private static final String VALID_ID = "measurement-2026-09-06";
    private static final LocalDate VALID_DATE = LocalDate.of(2026, 9, 6);

    @Test
    void acceptsACompleteMeasurement() {
        Measurement measurement = new Measurement(
                VALID_ID,
                VALID_DATE,
                new BigDecimal("81.1"),
                new BigDecimal("96.0"));

        assertThat(measurement.id()).isEqualTo(VALID_ID);
        assertThat(measurement.date()).isEqualTo(VALID_DATE);
    }

    @Test
    void rejectsABlankId() {
        assertThatThrownBy(() -> new Measurement(
                " ",
                VALID_DATE,
                new BigDecimal("81.1"),
                new BigDecimal("96.0")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("id");

        assertThatThrownBy(() -> new Measurement(
                null,
                VALID_DATE,
                new BigDecimal("81.1"),
                new BigDecimal("96.0")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsAMissingDate() {
        assertThatThrownBy(() -> new Measurement(
                VALID_ID,
                null,
                new BigDecimal("81.1"),
                new BigDecimal("96.0")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("date");
    }

    @Test
    void rejectsAMissingOrNonPositiveMetric() {
        assertThatThrownBy(() -> new Measurement(
                VALID_ID,
                VALID_DATE,
                null,
                new BigDecimal("96.0")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("weightKg");

        assertThatThrownBy(() -> new Measurement(
                VALID_ID,
                VALID_DATE,
                new BigDecimal("81.1"),
                BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("waistCm");
    }
}
