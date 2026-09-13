package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

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
}
