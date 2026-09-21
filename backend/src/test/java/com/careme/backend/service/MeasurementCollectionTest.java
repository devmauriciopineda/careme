package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.careme.backend.dto.AgentOperation;
import com.careme.backend.entity.EncounterMeasurementNote;
import com.careme.backend.entity.Metric;
import com.careme.backend.repository.AdmittedMetricRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Verifies the measurement channel of a turn: a message that mentions a measurement is
 * collected in the consultation's notes with its metric, its value converted to the
 * metric's reference unit and an exact date, and it never becomes a clinical fact.
 *
 * <p>Everything the application cannot take as asked is rejected with what has to be
 * asked for, and nothing is collected.
 */
class MeasurementCollectionTest {

    private static final String CONVERSATION_ID = "conversation-1";
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 19);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EncounterService encounterService = mock(EncounterService.class);
    private final AdmittedMetricRepository admittedMetricRepository = mock(AdmittedMetricRepository.class);
    private final AgentOperationExecutor executor = new AgentOperationExecutor(
            mock(ClinicalHistoryQueryService.class),
            encounterService,
            new ClinicalEventIntentValidator(),
            new MetricCatalogService(admittedMetricRepository));

    @BeforeEach
    void admitTheInitialMetrics() {
        Set<Metric> admitted = Set.of(Metric.WEIGHT, Metric.WAIST, Metric.BLOOD_PRESSURE, Metric.CHOLESTEROL);
        when(admittedMetricRepository.admits(any()))
                .thenAnswer(invocation -> admitted.contains(invocation.getArgument(0)));
    }

    private AgentOperationResult collect(String arguments) throws Exception {
        return executor.execute(
                CONVERSATION_ID,
                new AgentOperationCall(AgentOperation.RECORD_MEASUREMENT, objectMapper.readTree(arguments)),
                TODAY);
    }

    private EncounterMeasurementNote collectedNote() {
        ArgumentCaptor<EncounterMeasurementNote> captor =
                ArgumentCaptor.forClass(EncounterMeasurementNote.class);
        verify(encounterService).collectMeasurement(any(), captor.capture());
        return captor.getValue();
    }

    private static String weight(String value, String unit, String date) {
        ObjectNode node = new ObjectMapper().createObjectNode();
        node.put("metric", "weight");
        node.putObject("values").put("value", new BigDecimal(value));
        if (unit != null) {
            node.put("unit", unit);
        }
        if (date != null) {
            node.put("date", date);
        }
        return node.toString();
    }

    @Test
    void collectsAMeasurementInTheConsultationsNotes() throws Exception {
        when(encounterService.collectMeasurement(any(), any())).thenReturn(new EncounterMeasurementResult(
                EncounterMeasurementResult.Kind.COLLECTED, null, "Lo he anotado."));

        AgentOperationResult result = collect(weight("70", null, "2026-09-19"));

        assertThat(result.measurement()).isNotNull();
        assertThat(result.registration()).isNull();
        assertThat(collectedNote().metricCode()).isEqualTo("weight");
    }

    @Test
    void usesTheBareUnitOfTheMetricWhenThePersonStatesNone() throws Exception {
        when(encounterService.collectMeasurement(any(), any())).thenReturn(new EncounterMeasurementResult(
                EncounterMeasurementResult.Kind.COLLECTED, null, "Lo he anotado."));

        collect(weight("70", null, "2026-09-19"));

        EncounterMeasurementNote note = collectedNote();
        assertThat(note.values()).containsEntry("value", new BigDecimal("70"));
        assertThat(note.declaredUnit()).isNull();
    }

    @Test
    void convertsAValueStatedInAnotherUnitOfTheSameMetric() throws Exception {
        when(encounterService.collectMeasurement(any(), any())).thenReturn(new EncounterMeasurementResult(
                EncounterMeasurementResult.Kind.COLLECTED, null, "Lo he anotado."));

        collect(weight("100", "lb", "2026-09-19"));

        EncounterMeasurementNote note = collectedNote();
        assertThat(note.values().get("value")).isEqualByComparingTo("45.3592");
        assertThat(note.declaredUnit()).isEqualTo("lb");
    }

    @Test
    void registersTheDayTheMessageIsHandledWhenNoDateWasGiven() throws Exception {
        when(encounterService.collectMeasurement(any(), any())).thenReturn(new EncounterMeasurementResult(
                EncounterMeasurementResult.Kind.COLLECTED, null, "Lo he anotado."));

        collect(weight("70", null, null));

        assertThat(collectedNote().date()).isEqualTo(TODAY);
    }

    @Test
    void collectsBloodPressureAsOneMeasurementWithItsTwoValues() throws Exception {
        when(encounterService.collectMeasurement(any(), any())).thenReturn(new EncounterMeasurementResult(
                EncounterMeasurementResult.Kind.COLLECTED, null, "Lo he anotado."));

        collect("""
                {"metric":"blood_pressure","values":{"systolic":145,"diastolic":92},"date":"2026-09-19"}
                """);

        EncounterMeasurementNote note = collectedNote();
        assertThat(note.values())
                .containsEntry("systolic", new BigDecimal("145"))
                .containsEntry("diastolic", new BigDecimal("92"));
    }

    @Test
    void asksForTheUnitWhenTheMetricDoesNotDetermineIt() throws Exception {
        AgentOperationResult result = collect("""
                {"metric":"cholesterol","values":{"value":200},"date":"2026-09-19"}
                """);

        assertThat(result.kind()).isEqualTo(AgentOperationResult.Kind.REJECTED);
        assertThat(result.message()).contains("unidad");
        verifyNoInteractions(encounterService);
    }

    @Test
    void asksForTheExactDateOfAnApproximateMeasurement() throws Exception {
        AgentOperationResult result = collect("""
                {"metric":"weight","values":{"value":70},"date_precision":"approximate","date_text":"el mes pasado"}
                """);

        assertThat(result.kind()).isEqualTo(AgentOperationResult.Kind.REJECTED);
        assertThat(result.message()).contains("día exacto");
        verifyNoInteractions(encounterService);
    }

    @Test
    void rejectsAValueOutsideTheRangeOfItsMetric() throws Exception {
        AgentOperationResult result = collect(weight("900", null, "2026-09-19"));

        assertThat(result.kind()).isEqualTo(AgentOperationResult.Kind.REJECTED);
        verifyNoInteractions(encounterService);
    }

    @Test
    void rejectsACompositeValueThatIsMissingAComponent() throws Exception {
        AgentOperationResult result = collect("""
                {"metric":"blood_pressure","values":{"systolic":145},"date":"2026-09-19"}
                """);

        assertThat(result.kind()).isEqualTo(AgentOperationResult.Kind.REJECTED);
        verifyNoInteractions(encounterService);
    }

    @Test
    void asksBeforeStartingToRecordAMetricThatIsNotAdmittedYet() throws Exception {
        AgentOperationResult result = collect("""
                {"metric":"creatinine","values":{"value":1.1},"date":"2026-09-19"}
                """);

        assertThat(result.kind()).isEqualTo(AgentOperationResult.Kind.REJECTED);
        assertThat(result.message()).contains("empecemos a registrar esa métrica");
        verifyNoInteractions(encounterService);
    }

    @Test
    void doesNotSubstituteAnUnknownMetricForAnotherOne() throws Exception {
        AgentOperationResult result = collect("""
                {"metric":"haemoglobin","values":{"value":14},"date":"2026-09-19"}
                """);

        assertThat(result.kind()).isEqualTo(AgentOperationResult.Kind.REJECTED);
        verifyNoInteractions(encounterService);
    }

    @Test
    void asksForTheUnitWhenTheStatedOneIsNotAUnitOfTheMetric() throws Exception {
        AgentOperationResult result = collect("""
                {"metric":"weight","values":{"value":70},"unit":"stone","date":"2026-09-19"}
                """);

        assertThat(result.kind()).isEqualTo(AgentOperationResult.Kind.REJECTED);
        verifyNoInteractions(encounterService);
    }

    @Test
    void asksForTheExactDateWhenNoDateAndNoReferenceDayAreAvailable() throws Exception {
        when(encounterService.collectMeasurement(any(), any())).thenReturn(new EncounterMeasurementResult(
                EncounterMeasurementResult.Kind.COLLECTED, null, "Lo he anotado."));

        AgentOperationResult result = executor.execute(
                CONVERSATION_ID,
                new AgentOperationCall(
                        AgentOperation.RECORD_MEASUREMENT,
                        objectMapper.readTree("{\"metric\":\"weight\",\"values\":{\"value\":70}}")),
                null);

        assertThat(result.kind()).isEqualTo(AgentOperationResult.Kind.REJECTED);
        assertThat(result.message()).contains("día exacto");
        verifyNoInteractions(encounterService);
    }

    @Test
    void neverCollectsAMeasurementAsAClinicalFact() throws Exception {
        when(encounterService.collectMeasurement(any(), any())).thenReturn(new EncounterMeasurementResult(
                EncounterMeasurementResult.Kind.COLLECTED, null, "Lo he anotado."));

        collect(weight("70", null, "2026-09-19"));

        verify(encounterService).collectMeasurement(any(), any());
        verify(encounterService, never()).collect(any(), any(), any());
    }
}
