package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.careme.backend.entity.ClinicalEvent;
import com.careme.backend.entity.EncounterMeasurementNote;
import com.careme.backend.entity.MeasurementUnit;
import com.careme.backend.entity.MeasurementValue;
import com.careme.backend.entity.Metric;
import com.careme.backend.entity.MetricMeasurement;
import com.careme.backend.entity.MetricMeasurementDraft;
import com.careme.backend.repository.AdmittedMetricRepository;
import com.careme.backend.repository.MetricMeasurementOutcome;
import com.careme.backend.repository.MetricMeasurementRepository;

/**
 * Verifies what the close writes into the tracking: only the admissible measurements,
 * all of them in one batch, each with the consultation it came from as its provenance,
 * and without a half-written record when the batch cannot be stored.
 */
class MeasurementRegistrationServiceTest {

    private static final String ENCOUNTER_CODE = "enc_007";
    private static final LocalDate DAY = LocalDate.of(2026, 9, 19);

    private final MetricMeasurementRepository metricMeasurementRepository =
            mock(MetricMeasurementRepository.class);
    private final AdmittedMetricRepository admittedMetricRepository = mock(AdmittedMetricRepository.class);
    private final MeasurementRegistrationService service = new MeasurementRegistrationService(
            metricMeasurementRepository, new MetricCatalogService(admittedMetricRepository));

    @BeforeEach
    void admitTheInitialMetrics() {
        Set<Metric> admitted = Set.of(Metric.WEIGHT, Metric.WAIST, Metric.BLOOD_PRESSURE, Metric.CHOLESTEROL);
        when(admittedMetricRepository.admits(any()))
                .thenAnswer(invocation -> admitted.contains(invocation.getArgument(0)));
    }

    private static EncounterMeasurementNote note(
            String metric, Map<String, BigDecimal> values, LocalDate date,
            ClinicalEvent.DatePrecision precision) {
        return new EncounterMeasurementNote(metric, values, null, date, precision, null);
    }

    private static EncounterMeasurementNote weight(String value) {
        return note("weight", Map.of("value", new BigDecimal(value)), DAY, ClinicalEvent.DatePrecision.EXACT);
    }

    private static MetricMeasurementOutcome outcome(Metric metric) {
        return new MetricMeasurementOutcome(
                new MetricMeasurement(
                        UUID.randomUUID(),
                        metric,
                        metric.referenceUnit(),
                        DAY,
                        ENCOUNTER_CODE,
                        OffsetDateTime.now(),
                        List.of(new MeasurementValue("value", new BigDecimal("70")))),
                false);
    }

    @Test
    void storesTheAdmissibleMeasurementsInOneBatch() {
        when(metricMeasurementRepository.upsertAll(any())).thenReturn(List.of(outcome(Metric.WEIGHT)));

        MeasurementRegistrationResult result = service.register(ENCOUNTER_CODE, List.of(weight("70")));

        assertThat(result.kind()).isEqualTo(MeasurementRegistrationResult.Kind.REGISTERED);
        assertThat(result.registered()).isEqualTo(1);
    }

    @Test
    void declaresTheConsultationTheMeasurementCameFrom() {
        when(metricMeasurementRepository.upsertAll(any())).thenReturn(List.of(outcome(Metric.WEIGHT)));

        service.register(ENCOUNTER_CODE, List.of(weight("70")));

        ArgumentCaptor<List<MetricMeasurementDraft>> captor = ArgumentCaptor.forClass(List.class);
        verify(metricMeasurementRepository).upsertAll(captor.capture());
        assertThat(captor.getValue()).singleElement()
                .satisfies(draft -> assertThat(draft.encounterCode()).isEqualTo(ENCOUNTER_CODE));
    }

    @Test
    void leavesOutAMeasurementWhoseDateIsNotExact() {
        MeasurementRegistrationResult result = service.register(ENCOUNTER_CODE, List.of(
                note("weight", Map.of("value", new BigDecimal("70")), DAY,
                        ClinicalEvent.DatePrecision.APPROXIMATE),
                note("weight", Map.of("value", new BigDecimal("70")), null,
                        ClinicalEvent.DatePrecision.UNKNOWN)));

        assertThat(result.kind()).isEqualTo(MeasurementRegistrationResult.Kind.NOTHING);
        verify(metricMeasurementRepository, never()).upsertAll(any());
    }

    @Test
    void leavesOutAMeasurementOfAMetricThatIsNotAdmitted() {
        MeasurementRegistrationResult result = service.register(
                ENCOUNTER_CODE,
                List.of(note("creatinine", Map.of("value", new BigDecimal("1.1")), DAY,
                        ClinicalEvent.DatePrecision.EXACT)));

        assertThat(result.kind()).isEqualTo(MeasurementRegistrationResult.Kind.NOTHING);
        verify(metricMeasurementRepository, never()).upsertAll(any());
    }

    @Test
    void leavesOutAMeasurementOfAMetricTheCatalogueDoesNotKnow() {
        MeasurementRegistrationResult result = service.register(
                ENCOUNTER_CODE,
                List.of(note("haemoglobin", Map.of("value", new BigDecimal("14")), DAY,
                        ClinicalEvent.DatePrecision.EXACT)));

        assertThat(result.kind()).isEqualTo(MeasurementRegistrationResult.Kind.NOTHING);
        verify(metricMeasurementRepository, never()).upsertAll(any());
    }

    @Test
    void leavesOutAMeasurementOutsideTheRangeOfItsMetric() {
        MeasurementRegistrationResult result = service.register(ENCOUNTER_CODE, List.of(weight("900")));

        assertThat(result.kind()).isEqualTo(MeasurementRegistrationResult.Kind.NOTHING);
        verify(metricMeasurementRepository, never()).upsertAll(any());
    }

    @Test
    void registersTheAdmissibleOnesWhenAnotherOneCannotBeTaken() {
        when(metricMeasurementRepository.upsertAll(any())).thenReturn(List.of(outcome(Metric.WEIGHT)));

        MeasurementRegistrationResult result = service.register(ENCOUNTER_CODE, List.of(
                weight("900"),
                weight("70")));

        assertThat(result.kind()).isEqualTo(MeasurementRegistrationResult.Kind.REGISTERED);
        assertThat(result.registered()).isEqualTo(1);
    }

    @Test
    void reportsAFailureWithoutLeavingAnythingHalfWritten() {
        when(metricMeasurementRepository.upsertAll(any()))
                .thenThrow(new IllegalStateException("database is unreachable"));

        MeasurementRegistrationResult result = service.register(ENCOUNTER_CODE, List.of(weight("70")));

        assertThat(result.kind()).isEqualTo(MeasurementRegistrationResult.Kind.FAILURE);
        assertThat(result.outcomes()).isEmpty();
        assertThat(result.message()).isEmpty();
    }

    @Test
    void reportsHowManyReplacedTheValueOfThatMetricAndDay() {
        MetricMeasurement replaced = new MetricMeasurement(
                UUID.randomUUID(), Metric.WEIGHT, MeasurementUnit.KG, DAY, ENCOUNTER_CODE,
                OffsetDateTime.now(), List.of(new MeasurementValue("value", new BigDecimal("70"))));
        when(metricMeasurementRepository.upsertAll(any()))
                .thenReturn(List.of(new MetricMeasurementOutcome(replaced, true)));

        MeasurementRegistrationResult result = service.register(ENCOUNTER_CODE, List.of(weight("70")));

        assertThat(result.replaced()).isEqualTo(1);
    }

    @Test
    void asksTheRepositoryForNothingWhenTheConsultationCollectedNoMeasurement() {
        MeasurementRegistrationResult result = service.register(ENCOUNTER_CODE, List.of());

        assertThat(result.kind()).isEqualTo(MeasurementRegistrationResult.Kind.NOTHING);
        assertThat(result.registered()).isZero();
        verify(metricMeasurementRepository, never()).upsertAll(any());
    }

    @Test
    void storesEveryAdmissibleMeasurementOfTheConsultation() {
        when(metricMeasurementRepository.upsertAll(any()))
                .thenReturn(List.of(outcome(Metric.WEIGHT), outcome(Metric.WAIST)));

        MeasurementRegistrationResult result = service.register(ENCOUNTER_CODE, List.of(
                weight("70"),
                note("blood_pressure", Map.of(
                        "systolic", new BigDecimal("145"),
                        "diastolic", new BigDecimal("92")), DAY, ClinicalEvent.DatePrecision.EXACT)));

        ArgumentCaptor<List<MetricMeasurementDraft>> captor = ArgumentCaptor.forClass(List.class);
        verify(metricMeasurementRepository).upsertAll(captor.capture());
        assertThat(captor.getValue()).hasSize(2);
        assertThat(result.registered()).isEqualTo(2);
    }
}
