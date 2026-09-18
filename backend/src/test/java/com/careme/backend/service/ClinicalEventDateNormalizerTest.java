package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.careme.backend.entity.ClinicalEvent;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ClinicalEventDateNormalizerTest {

    private final ClinicalEventDateNormalizer normalizer = new ClinicalEventDateNormalizer();
    private final LocalDate referenceDate = LocalDate.of(2026, 9, 13);

    @Test
    void resolvesRelativeExpressionsAndPreservesOriginalText() {
        var today = normalizer.normalize(null, ClinicalEvent.DatePrecision.UNKNOWN, "hoy", referenceDate);
        var yesterday = normalizer.normalize(null, ClinicalEvent.DatePrecision.UNKNOWN, "ayer", referenceDate);
        var weeks = normalizer.normalize(null, ClinicalEvent.DatePrecision.UNKNOWN, "hace dos semanas", referenceDate);

        assertThat(today.date()).isEqualTo(referenceDate);
        assertThat(yesterday.date()).isEqualTo(referenceDate.minusDays(1));
        assertThat(weeks.date()).isEqualTo(referenceDate.minusWeeks(2));
        assertThat(weeks.precision()).isEqualTo(ClinicalEvent.DatePrecision.EXACT);
        assertThat(weeks.text()).isEqualTo("hace dos semanas");
    }

    @Test
    void preservesApproximateAndUnknownDates() {
        var approximate = normalizer.normalize(
                null, ClinicalEvent.DatePrecision.APPROXIMATE, "hace unos dos años", referenceDate);
        var unknown = normalizer.normalize(null, ClinicalEvent.DatePrecision.UNKNOWN, null, referenceDate);

        assertThat(approximate.date()).isNull();
        assertThat(approximate.precision()).isEqualTo(ClinicalEvent.DatePrecision.APPROXIMATE);
        assertThat(approximate.text()).isEqualTo("hace unos dos años");
        assertThat(unknown.date()).isNull();
        assertThat(unknown.precision()).isEqualTo(ClinicalEvent.DatePrecision.UNKNOWN);
    }

    @Test
    void resolvesCaseInsensitiveTrimmedDaysAndWeeks() {
        var today = normalizer.normalize(null, ClinicalEvent.DatePrecision.UNKNOWN, " HOY ", referenceDate);
        var yesterday = normalizer.normalize(null, ClinicalEvent.DatePrecision.UNKNOWN, "Ayer", referenceDate);
        var oneDay = normalizer.normalize(null, ClinicalEvent.DatePrecision.UNKNOWN, "hace una dia", referenceDate);
        var numericDays = normalizer.normalize(null, ClinicalEvent.DatePrecision.UNKNOWN, "hace 12 días", referenceDate);
        var oneWeek = normalizer.normalize(null, ClinicalEvent.DatePrecision.UNKNOWN, "hace uno semana", referenceDate);
        var numericWeeks = normalizer.normalize(null, ClinicalEvent.DatePrecision.UNKNOWN, "hace 3 semanas", referenceDate);
        var threeDays = normalizer.normalize(null, ClinicalEvent.DatePrecision.UNKNOWN, "hace tres días", referenceDate);
        var fourDays = normalizer.normalize(null, ClinicalEvent.DatePrecision.UNKNOWN, "hace cuatro días", referenceDate);
        var fiveWeeks = normalizer.normalize(null, ClinicalEvent.DatePrecision.UNKNOWN, "hace cinco semanas", referenceDate);

        assertThat(today.date()).isEqualTo(referenceDate);
        assertThat(yesterday.date()).isEqualTo(referenceDate.minusDays(1));
        assertThat(oneDay.date()).isEqualTo(referenceDate.minusDays(1));
        assertThat(numericDays.date()).isEqualTo(referenceDate.minusDays(12));
        assertThat(oneWeek.date()).isEqualTo(referenceDate.minusWeeks(1));
        assertThat(numericWeeks.date()).isEqualTo(referenceDate.minusWeeks(3));
        assertThat(threeDays.date()).isEqualTo(referenceDate.minusDays(3));
        assertThat(fourDays.date()).isEqualTo(referenceDate.minusDays(4));
        assertThat(fiveWeeks.date()).isEqualTo(referenceDate.minusWeeks(5));
    }

    @Test
    void preservesSuppliedDateForBlankOrUnsupportedExpressions() {
        var suppliedDate = referenceDate.minusDays(4);

        var blank = normalizer.normalize(
                suppliedDate, ClinicalEvent.DatePrecision.APPROXIMATE, "  ", referenceDate);
        var unsupported = normalizer.normalize(
                suppliedDate, ClinicalEvent.DatePrecision.UNKNOWN, "hace dos años", referenceDate);

        assertThat(blank.date()).isEqualTo(suppliedDate);
        assertThat(blank.text()).isNull();
        assertThat(unsupported.date()).isEqualTo(suppliedDate);
        assertThat(unsupported.precision()).isEqualTo(ClinicalEvent.DatePrecision.UNKNOWN);
    }

    @Test
    void rejectsMissingPrecisionOrReferenceDate() {
        assertThatThrownBy(() -> normalizer.normalize(null, null, "hoy", referenceDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("precision");
        assertThatThrownBy(() -> normalizer.normalize(
                null, ClinicalEvent.DatePrecision.UNKNOWN, "hoy", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Reference date");
    }
}
