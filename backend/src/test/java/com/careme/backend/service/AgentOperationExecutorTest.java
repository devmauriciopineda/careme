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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AgentOperationExecutorTest {

    private static final String CONVERSATION_ID = "conversation-1";
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 19);

    /** A consultation the application can act on: it carries the question and the terms to search with. */
    private static final String CONSULTATION =
            "{\"question\":\"¿qué diagnósticos tengo?\",\"search_terms\":[\"diagnostico\"]}";

    private final ClinicalHistoryQueryService historyQueryService = mock(ClinicalHistoryQueryService.class);
    private final ClinicalEventRegistrationService registrationService =
            mock(ClinicalEventRegistrationService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AgentOperationExecutor executor = new AgentOperationExecutor(
            historyQueryService, registrationService, new ClinicalEventIntentValidator());

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
    void returnsTheRegisteredEventOnlyAfterTheRegistrationReturnedIt() {
        ClinicalEvent registered = event();
        when(registrationService.register(anyString(), any(), any()))
                .thenReturn(new ClinicalEventRegistrationResult(
                        ClinicalEventRegistrationResult.Kind.REGISTERED,
                        List.of(registered),
                        "He registrado el hecho."));

        AgentOperationResult result = executor.execute(CONVERSATION_ID, registration(), TODAY);

        assertThat(result.kind()).isEqualTo(AgentOperationResult.Kind.COMPLETED);
        assertThat(result.payload().get("registered")).isEqualTo(true);
        assertThat(result.registration().events()).containsExactly(registered);
    }

    @Test
    void validatesTheRegistrationBeforeItReachesTheRegistrationService() {
        when(registrationService.register(anyString(), any(), any()))
                .thenReturn(new ClinicalEventRegistrationResult(
                        ClinicalEventRegistrationResult.Kind.REGISTERED, List.of(event()), "He registrado el hecho."));

        executor.execute(CONVERSATION_ID, registration(), TODAY);

        ArgumentCaptor<ClinicalEventIntent> intent = ArgumentCaptor.forClass(ClinicalEventIntent.class);
        verify(registrationService).register(eq(CONVERSATION_ID), intent.capture(), eq(TODAY));
        assertThat(intent.getValue().kind()).isEqualTo(ClinicalEventIntent.Kind.EVENTS);
        assertThat(intent.getValue().events())
                .singleElement()
                .satisfies(candidate -> {
                    assertThat(candidate.type()).isEqualTo(ClinicalEvent.ClinicalEventType.DIAGNOSIS);
                    assertThat(candidate.date()).isEqualTo(LocalDate.of(2024, 3, 1));
                    assertThat(candidate.datePrecision()).isEqualTo(ClinicalEvent.DatePrecision.EXACT);
                });
    }

    @Test
    void doesNotWriteWhenTheRegistrationDoesNotPassValidation() {
        AgentOperationResult result = executor.execute(
                CONVERSATION_ID,
                registration("{\"type\":\"diagnosis\",\"content\":\"Hipertensión.\",\"date_precision\":\"exact\"}"),
                TODAY);

        assertThat(result.kind()).isEqualTo(AgentOperationResult.Kind.REJECTED);
        assertThat(result.completed()).isFalse();
        assertThat(result.registration()).isNull();
        verify(registrationService, never()).register(anyString(), any(), any());
    }

    @Test
    void doesNotWriteWhenTheRegistrationRequestIsIncomplete() {
        AgentOperationResult result = executor.execute(
                CONVERSATION_ID, registration("{\"type\":\"diagnosis\",\"date_precision\":\"exact\"}"), TODAY);

        assertThat(result.kind()).isEqualTo(AgentOperationResult.Kind.REJECTED);
        verify(registrationService, never()).register(anyString(), any(), any());
    }

    @Test
    void doesNotRegisterWhenTheSuppliedDateCannotBeRead() {
        AgentOperationResult result = executor.execute(
                CONVERSATION_ID,
                registration("{\"type\":\"diagnosis\",\"content\":\"Hipertensión.\","
                        + "\"date\":\"el mes pasado\",\"date_precision\":\"exact\"}"),
                TODAY);

        assertThat(result.kind()).isEqualTo(AgentOperationResult.Kind.REJECTED);
        verify(registrationService, never()).register(anyString(), any(), any());
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
        verifyNoInteractions(historyQueryService, registrationService);
    }

    @Test
    void rejectsAnAbsentCallWithoutRunningAnything() {
        AgentOperationResult result = executor.execute(CONVERSATION_ID, null, TODAY);

        assertThat(result.kind()).isEqualTo(AgentOperationResult.Kind.REJECTED);
        assertThat(result.operation()).isNull();
        assertThat(result.payload()).isEmpty();
        verifyNoInteractions(historyQueryService, registrationService);
    }

    private AgentOperationCall consultation(String json) {
        return new AgentOperationCall(AgentOperation.CONSULT_HISTORY, arguments(json));
    }

    private AgentOperationCall registration() {
        return registration("{\"type\":\"diagnosis\",\"content\":\"Hipertensión.\","
                + "\"date\":\"2024-03-01\",\"date_precision\":\"exact\",\"date_text\":\"en marzo\"}");
    }

    private AgentOperationCall registration(String json) {
        return new AgentOperationCall(AgentOperation.REGISTER_EVENT, arguments(json));
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
                OffsetDateTime.now(ZoneOffset.UTC));
    }
}
