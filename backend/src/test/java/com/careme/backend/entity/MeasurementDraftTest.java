package com.careme.backend.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/**
 * A draft carries no identity, so it cannot be stored as-is; the values it does
 * carry have to be as valid as those of a stored measurement.
 */
class MeasurementDraftTest {

    private static final LocalDate DATE = LocalDate.of(2026, 9, 10);

    @Test
    void keepsTheGivenValues() {
        MeasurementDraft draft = new MeasurementDraft(DATE, new BigDecimal("80.1"), new BigDecimal("94.8"));

        assertThat(draft.date()).isEqualTo(DATE);
        assertThat(draft.weightKg()).isEqualByComparingTo("80.1");
        assertThat(draft.waistCm()).isEqualByComparingTo("94.8");
    }
    @Test
    void rejectsAMissingDate() {
        assertThatThrownBy(() -> new MeasurementDraft(null, new BigDecimal("80.1"), new BigDecimal("94.8")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("date");
    }

    @Test
    void rejectsAMissingWeight() {
        assertThatThrownBy(() -> new MeasurementDraft(DATE, null, new BigDecimal("94.8")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("weightKg");
    }

    @Test
    void rejectsAWeightThatIsNotPositive() {
        assertThatThrownBy(() -> new MeasurementDraft(DATE, BigDecimal.ZERO, new BigDecimal("94.8")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("weightKg");
    }

    @Test
    void rejectsAMissingWaist() {
        assertThatThrownBy(() -> new MeasurementDraft(DATE, new BigDecimal("80.1"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("waistCm");
    }

    @Test
    void rejectsAWaistThatIsNotPositive() {
        assertThatThrownBy(() -> new MeasurementDraft(DATE, new BigDecimal("80.1"), new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("waistCm");
    }
}
