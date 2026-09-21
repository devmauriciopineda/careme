package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.careme.backend.PostgresIntegrationTest;
import com.careme.backend.dto.ChatMessageResponse;
import com.careme.backend.dto.MeasurementQueryIntent;
import com.careme.backend.entity.Metric;
import com.careme.backend.entity.MetricMeasurement;
import com.careme.backend.entity.MetricMeasurementDraft;
import com.careme.backend.repository.MetricMeasurementRepository;

/**
 * End to end against real PostgreSQL: the question about the measurements is read from
 * the stored tracking, bounded by metric and period, and it leaves the tracking exactly
 * as it was.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "careme.events.directory=target/test-events-measurement-query",
        "careme.encounters.directory=target/test-encounters-measurement-query"
})
class MeasurementQueryIntegrationTest extends PostgresIntegrationTest {

    private static final LocalDate JANUARY = LocalDate.of(2026, 1, 10);
    private static final LocalDate FEBRUARY = LocalDate.of(2026, 2, 10);

    @Autowired
    private MetricMeasurementRepository repository;

    @Autowired
    private MeasurementQueryService service;

    @BeforeEach
    void storeTheTracking() {
        repository.upsertAll(List.of(
                new MetricMeasurementDraft(Metric.WEIGHT, JANUARY, null, Map.of("value", new BigDecimal("70.5"))),
                new MetricMeasurementDraft(Metric.WEIGHT, FEBRUARY, null, Map.of("value", new BigDecimal("71.5"))),
                new MetricMeasurementDraft(Metric.WAIST, JANUARY, null, Map.of("value", new BigDecimal("88")))));
    }

    @Test
    void readsOnlyTheMetricsAskedInsideThePeriod() {
        List<MetricMeasurement> found =
                repository.findByMetricsAndDateBetween(List.of(Metric.WEIGHT), JANUARY, JANUARY);

        assertThat(found).singleElement().satisfies(measurement -> {
            assertThat(measurement.metric()).isEqualTo(Metric.WEIGHT);
            assertThat(measurement.date()).isEqualTo(JANUARY);
        });
    }

    @Test
    void readsEveryMeasurementOfAMetricWhenThePeriodIsOpen() {
        List<MetricMeasurement> found = repository.findByMetricsAndDateBetween(List.of(Metric.WEIGHT), null, null);

        assertThat(found).hasSize(2);
    }

    @Test
    void answersWithTheValuesTheTrackingHolds() {
        MeasurementAnswerResult result = service.answer(new MeasurementQueryIntent(
                "¿cuánto peso?", List.of("weight"), null, null, false));

        assertThat(result.kind()).isEqualTo(MeasurementAnswerResult.Kind.ANSWERED);
        assertThat(result.facts()).extracting(MeasurementFact::text).containsExactlyInAnyOrder(
                "peso: 70.5 kg (2026-01-10)",
                "peso: 71.5 kg (2026-02-10)");
    }

    @Test
    void leavesTheTrackingExactlyAsItWas() {
        List<MetricMeasurement> before = repository.findAll();

        service.answer(new MeasurementQueryIntent("¿cuánto peso?", List.of("weight"), null, null, false));
        service.answer(new MeasurementQueryIntent("¿y mi cintura?", List.of("waist"), null, null, false));
        service.answer(new MeasurementQueryIntent("¿cómo van mis mediciones?", List.of(), null, null, false));

        assertThat(repository.findAll()).usingRecursiveComparison().isEqualTo(before);
    }

    @Test
    void declaresTheAbsenceWithoutChangingTheTracking() {
        MeasurementAnswerResult result = service.answer(new MeasurementQueryIntent(
                "¿cuánto colesterol tengo?", List.of("cholesterol"), null, null, false));

        assertThat(result.kind()).isEqualTo(MeasurementAnswerResult.Kind.NO_RECORDS);
        assertThat(result.absenceReason()).isEqualTo(ChatMessageResponse.AbsenceReason.NO_MEASUREMENTS);
        assertThat(repository.findAll()).hasSize(3);
    }
}
