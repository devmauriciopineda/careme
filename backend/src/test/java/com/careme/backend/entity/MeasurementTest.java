package com.careme.backend.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class MeasurementTest {

    private static final UUID VALID_ID = UUID.fromString("6f1d0f9a-1f5f-4a0e-9a4e-4f0f6a1c2b3d");
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
    void rejectsAMissingId() {
        assertThatThrownBy(() -> new Measurement(
                null,
                VALID_DATE,
                new BigDecimal("81.1"),
                new BigDecimal("96.0")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("id");
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
