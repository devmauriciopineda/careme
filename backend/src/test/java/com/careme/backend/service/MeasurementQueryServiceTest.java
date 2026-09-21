package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.careme.backend.dto.ChatMessageResponse;
import com.careme.backend.dto.MeasurementQueryIntent;
import com.careme.backend.entity.MeasurementValue;
import com.careme.backend.entity.Metric;
import com.careme.backend.entity.MetricMeasurement;
import com.careme.backend.repository.MetricMeasurementRepository;

/**
 * Answers a question about the person's measurements with the values, the units and the
 * dates the tracking holds, asks which metric when the question does not say, declares an
 * absence only when it was verified, and never answers from the clinical history.
 */
class MeasurementQueryServiceTest {

    private static final LocalDate DAY = LocalDate.of(2026, 9, 19);

    private final MetricMeasurementRepository repository = mock(MetricMeasurementRepository.class);
    private final MetricCatalogService catalog = mock(MetricCatalogService.class);
    private final MeasurementQueryService service =
            new MeasurementQueryService(repository, catalog, new FakeMeasurementAnswerComposer());

    @BeforeEach
    void admitTheInitialMetrics() {
        Set<Metric> admitted = Set.of(Metric.WEIGHT, Metric.WAIST, Metric.BLOOD_PRESSURE, Metric.CHOLESTEROL);
        when(catalog.isAdmitted(any())).thenAnswer(
                invocation -> admitted.contains(invocation.getArgument(0, Metric.class)));
    }

    @Test
    void answersWithTheStoredValueUnitAndDate() {
        when(repository.findByMetricsAndDateBetween(anyCollection(), any(), any()))
                .thenReturn(List.of(measurement(Metric.WEIGHT, DAY, "value", "72.5000")));

        MeasurementAnswerResult result = service.answer(intent(List.of("weight")));

        assertThat(result.kind()).isEqualTo(MeasurementAnswerResult.Kind.ANSWERED);
        assertThat(result.absenceReason()).isNull();
        assertThat(result.facts()).singleElement().satisfies(fact -> {
            assertThat(fact.label()).isEqualTo("peso");
            assertThat(fact.unit()).isEqualTo("kg");
            assertThat(fact.date()).isEqualTo("2026-09-19");
            assertThat(fact.values()).singleElement().satisfies(
                    value -> assertThat(value.value()).isEqualTo("72.5"));
            assertThat(fact.text()).isEqualTo("peso: 72.5 kg (2026-09-19)");
        });
        assertThat(result.message()).contains("peso: 72.5 kg (2026-09-19)");
    }

    @Test
    void presentsACompositeMetricAsOneMeasurement() {
        when(repository.findByMetricsAndDateBetween(anyCollection(), any(), any()))
                .thenReturn(List.of(
                        new MetricMeasurement(
                                UUID.randomUUID(), Metric.BLOOD_PRESSURE, Metric.BLOOD_PRESSURE.referenceUnit(),
                                DAY, "enc_001", OffsetDateTime.now(ZoneOffset.UTC),
                                List.of(
                                        new MeasurementValue("systolic", new BigDecimal("145")),
                                        new MeasurementValue("diastolic", new BigDecimal("92"))))));

        MeasurementAnswerResult result = service.answer(intent(List.of("blood_pressure")));

        assertThat(result.facts()).singleElement().satisfies(fact -> {
            assertThat(fact.values()).hasSize(2);
            assertThat(fact.text())
                    .isEqualTo("presión arterial: sistólica 145 mmHg, diastólica 92 mmHg (2026-09-19)");
        });
    }

    @Test
    void boundsTheRetrievalToTheQuestionedPeriod() {
        when(repository.findByMetricsAndDateBetween(anyCollection(), any(), any())).thenReturn(List.of());

        service.answer(new MeasurementQueryIntent(
                "¿cuánto pesé el mes pasado?",
                List.of("weight"),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31),
                false));

        ArgumentCaptor<LocalDate> from = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> to = ArgumentCaptor.forClass(LocalDate.class);
        verify(repository).findByMetricsAndDateBetween(anyCollection(), from.capture(), to.capture());
        assertThat(from.getValue()).hasToString("2026-08-01");
        assertThat(to.getValue()).hasToString("2026-08-31");
    }

    @Test
    void resolvesTheMetricTheQuestionNamesWhenNoneWasGiven() {
        when(repository.findByMetricsAndDateBetween(anyCollection(), any(), any()))
                .thenReturn(List.of(measurement(Metric.WEIGHT, DAY, "value", "72.5")));

        MeasurementAnswerResult result = service.answer(intent(List.of(), "¿cuál es mi peso?"));

        assertThat(result.kind()).isEqualTo(MeasurementAnswerResult.Kind.ANSWERED);
        assertThat(result.facts()).singleElement()
                .satisfies(fact -> assertThat(fact.metric()).isEqualTo("weight"));
    }

    @Test
    void asksWhichMetricWhenTheQuestionNamesNone() {
        MeasurementAnswerResult result = service.answer(intent(List.of(), "¿cómo van mis mediciones?"));

        assertThat(result.kind()).isEqualTo(MeasurementAnswerResult.Kind.CLARIFICATION_REQUIRED);
        assertThat(result.message()).contains("¿De qué medición");
        assertThat(result.facts()).isEmpty();
    }

    @Test
    void declaresAMetricThatIsNotPartOfTheTracking() {
        MeasurementAnswerResult result = service.answer(intent(List.of("creatinine")));

        assertThat(result.kind()).isEqualTo(MeasurementAnswerResult.Kind.NO_RECORDS);
        assertThat(result.absenceReason())
                .isEqualTo(ChatMessageResponse.AbsenceReason.METRIC_NOT_TRACKED);
        assertThat(result.facts()).isEmpty();
        assertThat(result.message()).contains("no forma parte de tu seguimiento");
    }

    @Test
    void declaresTheAbsenceOfAMetricWithoutMeasurements() {
        when(repository.findByMetricsAndDateBetween(anyCollection(), any(), any())).thenReturn(List.of());

        MeasurementAnswerResult result = service.answer(intent(List.of("weight")));

        assertThat(result.kind()).isEqualTo(MeasurementAnswerResult.Kind.NO_RECORDS);
        assertThat(result.absenceReason()).isEqualTo(ChatMessageResponse.AbsenceReason.NO_MEASUREMENTS);
        assertThat(result.message())
                .contains("No consta ninguna medición")
                .contains("no significa que haya ocurrido");
    }

    @Test
    void scopesTheAbsenceToTheQuestionedPeriod() {
        when(repository.findByMetricsAndDateBetween(anyCollection(), any(), any())).thenReturn(List.of());

        MeasurementAnswerResult result = service.answer(new MeasurementQueryIntent(
                "¿cuánto pesé el mes pasado?",
                List.of("weight"),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31),
                false));

        assertThat(result.absenceReason())
                .isEqualTo(ChatMessageResponse.AbsenceReason.NO_MEASUREMENTS_IN_PERIOD);
        assertThat(result.message()).contains("en ese periodo");
    }

    @Test
    void declinesAnInterpretationWhileOfferingTheRegisteredValues() {
        when(repository.findByMetricsAndDateBetween(anyCollection(), any(), any()))
                .thenReturn(List.of(measurement(Metric.CHOLESTEROL, DAY, "value", "210")));

        MeasurementAnswerResult result = service.answer(new MeasurementQueryIntent(
                "¿es grave mi colesterol?",
                List.of("cholesterol"),
                null,
                null,
                true));

        assertThat(result.kind()).isEqualTo(MeasurementAnswerResult.Kind.ANSWERED);
        assertThat(result.message())
                .contains("colesterol: 210 mg/dL (2026-09-19)")
                .contains("no diagnostica ni recomienda tratamiento");
    }

    @Test
    void reportsAFailureInsteadOfAnAbsenceWhenTheRetrievalFails() {
        when(repository.findByMetricsAndDateBetween(anyCollection(), any(), any()))
                .thenThrow(new IllegalStateException("database is down"));

        MeasurementAnswerResult result = service.answer(intent(List.of("weight")));

        assertThat(result.kind()).isEqualTo(MeasurementAnswerResult.Kind.FAILURE);
        assertThat(result.absenceReason()).isNull();
        assertThat(result.facts()).isEmpty();
        assertThat(result.message()).contains("reintentarlo");
    }

    @Test
    void dropsAComposedAnswerThatDoesNotReproduceTheMeasurements() {
        when(repository.findByMetricsAndDateBetween(anyCollection(), any(), any()))
                .thenReturn(List.of(measurement(Metric.WEIGHT, DAY, "value", "72.5")));
        var roundingComposer = new MeasurementQueryService(
                repository,
                catalog,
                (question, facts, interpretationRequested) -> "Tu peso ronda los 73 kg.");

        MeasurementAnswerResult result = roundingComposer.answer(intent(List.of("weight")));

        assertThat(result.message())
                .contains("peso: 72.5 kg (2026-09-19)")
                .doesNotContain("73");
    }

    @Test
    void answersBySeveralMetricsAtOnce() {
        when(repository.findByMetricsAndDateBetween(anyCollection(), any(), any()))
                .thenReturn(List.of(
                        measurement(Metric.WEIGHT, DAY, "value", "72.5"),
                        measurement(Metric.WAIST, DAY, "value", "88")));

        MeasurementAnswerResult result = service.answer(intent(List.of("weight", "waist")));

        assertThat(result.facts()).hasSize(2);
        assertThat(result.message()).contains("peso: 72.5 kg").contains("circunferencia abdominal: 88 cm");
    }

    private static MeasurementQueryIntent intent(List<String> metricCodes) {
        return intent(metricCodes, "¿cuál es mi peso?");
    }

    private static MeasurementQueryIntent intent(List<String> metricCodes, String question) {
        return new MeasurementQueryIntent(question, metricCodes, null, null, false);
    }

    private static MetricMeasurement measurement(Metric metric, LocalDate date, String component, String value) {
        return new MetricMeasurement(
                UUID.randomUUID(),
                metric,
                metric.referenceUnit(),
                date,
                "enc_001",
                OffsetDateTime.now(ZoneOffset.UTC),
                List.of(new MeasurementValue(component, new BigDecimal(value))));
    }
}
