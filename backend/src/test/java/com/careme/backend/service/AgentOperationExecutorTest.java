package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.careme.backend.dto.AgentOperation;
import com.careme.backend.dto.ChatMessageResponse;
import com.careme.backend.dto.ClinicalEventIntent;
import com.careme.backend.entity.ClinicalEvent;
import com.careme.backend.entity.EncounterNote;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.careme.backend.repository.AdmittedMetricRepository;

class AgentOperationExecutorTest {

    private static final String CONVERSATION_ID = "conversation-1";
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 19);

    /** A consultation the application can act on: it carries the question and the terms to search with. */
    private static final String CONSULTATION =
            "{\"question\":\"¿qué diagnósticos tengo?\",\"search_terms\":[\"diagnostico\"]}";

    private final ClinicalHistoryQueryService historyQueryService = mock(ClinicalHistoryQueryService.class);
    private final EncounterService encounterService = mock(EncounterService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AgentOperationExecutor executor = new AgentOperationExecutor(
            historyQueryService, encounterService, new ClinicalEventIntentValidator(),
            new MetricCatalogService(mock(AdmittedMetricRepository.class)),
            mock(MeasurementQueryService.class));

    @Test
    void runsTheConsultationThroughTheHistoryQueryService() {
        when(historyQueryService.answer(any()))
                .thenReturn(ClinicalAnswerResult.answered(List.of(event()), "Tienes un diagnóstico registrado."));

        AgentOperationResult result = executor.execute(
                CONVERSATION_ID,
                consultation(CONSULTATION),
                TODAY);

        assertThat(result.operation()).isEqualTo(AgentOperation.CONSULT_HISTORY);
        assertThat(result.kind()).isEqualTo(AgentOperationResult.Kind.COMPLETED);
        assertThat(result.message()).isEqualTo("Tienes un diagnóstico registrado.");
        assertThat(result.completed()).isTrue();
    }

    @Test
    void handsBackTheRetrievedEventsWithTheirPrecisionAndNoStorageDetail() throws Exception {
        when(historyQueryService.answer(any()))
                .thenReturn(ClinicalAnswerResult.answered(List.of(event()), "Respuesta fundada."));

        AgentOperationResult result = executor.execute(
                CONVERSATION_ID, consultation(CONSULTATION), TODAY);

        String payload = objectMapper.writeValueAsString(result.payload());
        assertThat(payload).contains("evt_001", "approximate", "hace unos dos años");
        assertThat(payload).doesNotContain("data/", ".md", "filesystem", "path");
    }

    @Test
    void reportsAnAbsenceOfRecordsAsAnAbsenceAndNotAsAFailure() {
        when(historyQueryService.answer(any()))
                .thenReturn(ClinicalAnswerResult.noRecords(
                        ChatMessageResponse.AbsenceReason.EMPTY_HISTORY, List.of(), "Todavía no hay registros."));

        AgentOperationResult result = executor.execute(
                CONVERSATION_ID, consultation(CONSULTATION), TODAY);

        assertThat(result.kind()).isEqualTo(AgentOperationResult.Kind.NO_RECORDS);
        assertThat(result.completed()).isTrue();
        assertThat(result.payload().get("absence_reason")).isEqualTo("empty_history");
    }

    @Test
    void reportsAFailedConsultationAsAFailedOperation() {
        when(historyQueryService.answer(any())).thenReturn(ClinicalAnswerResult.failure("No pude buscarlo."));

        AgentOperationResult result = executor.execute(
                CONVERSATION_ID, consultation(CONSULTATION), TODAY);

        assertThat(result.kind()).isEqualTo(AgentOperationResult.Kind.FAILED);
        assertThat(result.completed()).isFalse();
    }

    @Test
    void returnsTheNoteOnlyAfterTheEncounterServiceCollectedIt() {
        EncounterNote collected = note();
        when(encounterService.collect(anyString(), any(), any()))
                .thenReturn(new EncounterNoteResult(
                        EncounterNoteResult.Kind.COLLECTED, collected, "Lo he anotado."));

        AgentOperationResult result = executor.execute(CONVERSATION_ID, registration(), TODAY);

        assertThat(result.kind()).isEqualTo(AgentOperationResult.Kind.COMPLETED);
        assertThat(result.payload().get("noted")).isEqualTo(true);
        assertThat(result.note().note()).isEqualTo(collected);
    }

    @Test
    void validatesTheNoteBeforeItReachesTheEncounterService() {
        when(encounterService.collect(anyString(), any(), any()))
                .thenReturn(new EncounterNoteResult(EncounterNoteResult.Kind.COLLECTED, note(), "Lo he anotado."));

        executor.execute(CONVERSATION_ID, registration(), TODAY);

        ArgumentCaptor<ClinicalEventIntent.Candidate> candidate =
                ArgumentCaptor.forClass(ClinicalEventIntent.Candidate.class);
        verify(encounterService).collect(eq(CONVERSATION_ID), candidate.capture(), eq(TODAY));
        assertThat(candidate.getValue().type()).isEqualTo(ClinicalEvent.ClinicalEventType.DIAGNOSIS);
        assertThat(candidate.getValue().date()).isEqualTo(LocalDate.of(2024, 3, 1));
        assertThat(candidate.getValue().datePrecision()).isEqualTo(ClinicalEvent.DatePrecision.EXACT);
    }

    @Test
    void doesNotWriteWhenTheRegistrationDoesNotPassValidation() {
        AgentOperationResult result = executor.execute(
                CONVERSATION_ID,
                registration("{\"type\":\"diagnosis\",\"content\":\"Hipertensión.\",\"date_precision\":\"exact\"}"),
                TODAY);

        assertThat(result.kind()).isEqualTo(AgentOperationResult.Kind.REJECTED);
        assertThat(result.completed()).isFalse();
        assertThat(result.note()).isNull();
        verify(encounterService, never()).collect(anyString(), any(), any());
    }

    @Test
    void doesNotWriteWhenTheRegistrationRequestIsIncomplete() {
        AgentOperationResult result = executor.execute(
                CONVERSATION_ID, registration("{\"type\":\"diagnosis\",\"date_precision\":\"exact\"}"), TODAY);

        assertThat(result.kind()).isEqualTo(AgentOperationResult.Kind.REJECTED);
        verify(encounterService, never()).collect(anyString(), any(), any());
    }

    @Test
    void doesNotRegisterWhenTheSuppliedDateCannotBeRead() {
        AgentOperationResult result = executor.execute(
                CONVERSATION_ID,
                registration("{\"type\":\"diagnosis\",\"content\":\"Hipertensión.\","
                        + "\"date\":\"el mes pasado\",\"date_precision\":\"exact\"}"),
                TODAY);

        assertThat(result.kind()).isEqualTo(AgentOperationResult.Kind.REJECTED);
        verify(encounterService, never()).collect(anyString(), any(), any());
    }

    @Test
    void doesNotConsultWhenTheConsultationHasNeitherTermsNorFilters() {
        AgentOperationResult result = executor.execute(
                CONVERSATION_ID, consultation("{\"question\":\"¿qué diagnósticos tengo?\"}"), TODAY);
        // A question with no search terms and no filter is not admissible, so the
        // consultation is never run and the history query service is not reached.
        assertThat(result.kind()).isEqualTo(AgentOperationResult.Kind.REJECTED);
        verifyNoInteractions(historyQueryService);
    }

    @Test
    void neverRunsAnOperationThatIsNotInTheAvailableSet() {
        assertThat(AgentOperationCall.of("delete_event", arguments("{}"))).isEmpty();
        assertThat(AgentOperationCall.of("register_measurement", arguments("{}"))).isEmpty();
        verifyNoInteractions(historyQueryService, encounterService);
    }

    @Test
    void rejectsAnAbsentCallWithoutRunningAnything() {
        AgentOperationResult result = executor.execute(CONVERSATION_ID, null, TODAY);

        assertThat(result.kind()).isEqualTo(AgentOperationResult.Kind.REJECTED);
        assertThat(result.operation()).isNull();
        assertThat(result.payload()).isEmpty();
        verifyNoInteractions(historyQueryService, encounterService);
    }

    private AgentOperationCall consultation(String json) {
        return new AgentOperationCall(AgentOperation.CONSULT_HISTORY, arguments(json));
    }

    private AgentOperationCall registration() {
        return registration("{\"type\":\"diagnosis\",\"content\":\"Hipertensión.\","
                + "\"date\":\"2024-03-01\",\"date_precision\":\"exact\",\"date_text\":\"en marzo\"}");
    }

    private AgentOperationCall registration(String json) {
        return new AgentOperationCall(AgentOperation.RECORD_NOTE, arguments(json));
    }

    private static EncounterNote note() {
        return new EncounterNote(
                ClinicalEvent.ClinicalEventType.DIAGNOSIS,
                "Hipertensión.",
                LocalDate.of(2024, 3, 1),
                ClinicalEvent.DatePrecision.EXACT,
                "en marzo");
    }

    private JsonNode arguments(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static ClinicalEvent event() {
        return new ClinicalEvent(
                UUID.randomUUID(),
                "evt_001",
                ClinicalEvent.ClinicalEventType.DIAGNOSIS,
                LocalDate.of(2024, 3, 1),
                ClinicalEvent.DatePrecision.APPROXIMATE,
                "hace unos dos años",
                "Hipertensión diagnosticada.",
                ClinicalEvent.EventSource.PATIENT,
                OffsetDateTime.now(ZoneOffset.UTC),
                null);
    }
}
