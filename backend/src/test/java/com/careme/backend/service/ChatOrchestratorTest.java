package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.careme.backend.dto.ChatMessageRequest;
import com.careme.backend.dto.ClinicalEventIntent;
import com.careme.backend.entity.ClinicalEvent;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ChatOrchestratorTest {

    private final ClinicalIntentInterpreter interpreter = mock(ClinicalIntentInterpreter.class);
    private final ClinicalEventRegistrationService registration = mock(ClinicalEventRegistrationService.class);
    private final ClinicalHistoryQueryService historyQuery = mock(ClinicalHistoryQueryService.class);
    private final ConversationStateStore stateStore = new ConversationStateStore(10, java.time.Duration.ofHours(1));
    private final ChatOrchestrator orchestrator = new ChatOrchestrator(interpreter, registration, historyQuery, stateStore);

    @Test
    void registersEventAndReturnsOriginalOutcomeOnRetry() {
        var intent = new ClinicalEventIntent(
                ClinicalEventIntent.Kind.EVENTS,
                List.of(new ClinicalEventIntent.Candidate(
                        ClinicalEvent.ClinicalEventType.NOTE,
                        "Tuve fiebre",
                        null,
                        ClinicalEvent.DatePrecision.UNKNOWN,
                        null)),
                null);
        var event = new ClinicalEvent(
                UUID.randomUUID(), "evt_100", ClinicalEvent.ClinicalEventType.NOTE, null,
                ClinicalEvent.DatePrecision.UNKNOWN, null, "Tuve fiebre",
                ClinicalEvent.EventSource.PATIENT, OffsetDateTime.now(ZoneOffset.UTC));
        when(interpreter.interpret(org.mockito.ArgumentMatchers.eq("Tuve fiebre"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(intent);
        when(registration.register(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.same(intent),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(new ClinicalEventRegistrationResult(
                        ClinicalEventRegistrationResult.Kind.REGISTERED,
                        List.of(event),
                        "Hecho registrado en tu historia clínica."));

        var request = new ChatMessageRequest("Tuve fiebre", null, "msg-1");
        var first = orchestrator.process(request);
        var retry = orchestrator.process(new ChatMessageRequest("Tuve fiebre", first.conversationId(), "msg-1"));

        assertThat(first.status()).isEqualTo(com.careme.backend.dto.ChatMessageResponse.Status.REGISTERED);
        assertThat(retry).isEqualTo(first);
        verify(registration).register(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.same(intent),
                org.mockito.ArgumentMatchers.any());
        verifyNoMoreInteractions(registration);
    }

    @Test
    void keepsPendingMessageWhenClarificationIsRequired() {
        when(interpreter.interpret(org.mockito.ArgumentMatchers.eq("Me duele"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(ClinicalEventIntent.clarification("¿Cuándo ocurrió?"));

        var response = orchestrator.process(new ChatMessageRequest("Me duele", null, "msg-2"));

        assertThat(response.status()).isEqualTo(com.careme.backend.dto.ChatMessageResponse.Status.CLARIFICATION_REQUIRED);
        assertThat(response.message()).isEqualTo("¿Cuándo ocurrió?");
        verifyNoMoreInteractions(registration);
    }

        @Test
        void turnsProviderFailureIntoRetryableOutcomeWithoutRegistration() {
                when(interpreter.interpret(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
                                .thenThrow(new LlmIntegrationException("provider detail must stay internal", new RuntimeException()));

                var response = orchestrator.process(new ChatMessageRequest("Tuve fiebre", null, "msg-failure"));

                assertThat(response.status()).isEqualTo(com.careme.backend.dto.ChatMessageResponse.Status.FAILED);
                assertThat(response.message()).isEqualTo("No pude procesar tu mensaje. Puedes reintentarlo.");
                verifyNoMoreInteractions(registration);
        }

        @Test
        void routesAHistoryQueryToTheQueryServiceWithoutRegistering() {
                var intent = queryIntent(ClinicalEventIntent.Query.Scope.HISTORY);
                var event = event("evt_100", "Hipertensión");
                when(interpreter.interpret(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
                                .thenReturn(intent);
                when(historyQuery.answer(intent)).thenReturn(ClinicalAnswerResult.answered(
                                List.of(event), "Te la diagnosticaron en enero."));

                var response = orchestrator.process(new ChatMessageRequest("¿Cuándo me diagnosticaron hipertensión?", null, "msg-query"));

                assertThat(response.status())
                                .isEqualTo(com.careme.backend.dto.ChatMessageResponse.Status.ANSWERED);
                assertThat(response.message()).isEqualTo("Te la diagnosticaron en enero.");
                assertThat(response.events()).singleElement()
                                .satisfies(summary -> assertThat(summary.code()).isEqualTo("evt_100"));
                verifyNoMoreInteractions(registration);
        }

        @Test
        void refersMeasurementsWithoutSearchingTheHistoryOrRegistering() {
                var intent = queryIntent(ClinicalEventIntent.Query.Scope.MEASUREMENTS);
                when(interpreter.interpret(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
                                .thenReturn(intent);

                var response = orchestrator.process(new ChatMessageRequest("¿Cuánto peso?", null, "msg-measurements"));

                assertThat(response.status())
                                .isEqualTo(com.careme.backend.dto.ChatMessageResponse.Status.GENERAL_CONVERSATION);
                assertThat(response.message()).contains("peso");
                verifyNoMoreInteractions(registration);
                verifyNoMoreInteractions(historyQuery);
        }

        @Test
        void returnsNoRecordsWithoutSupportingEvents() {
                var intent = queryIntent(ClinicalEventIntent.Query.Scope.HISTORY);
                when(interpreter.interpret(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
                                .thenReturn(intent);
                when(historyQuery.answer(intent)).thenReturn(ClinicalAnswerResult.noRecords(
                                com.careme.backend.dto.ChatMessageResponse.AbsenceReason.NO_TERM_MATCH,
                                List.of(com.careme.backend.dto.ChatMessageResponse.SuggestedAction.REFORMULATE,
                                                com.careme.backend.dto.ChatMessageResponse.SuggestedAction.REGISTER),
                                "No encuentro registros."));

                var response = orchestrator.process(new ChatMessageRequest("¿He tenido migrañas?", null, "msg-none"));

                assertThat(response.status())
                                .isEqualTo(com.careme.backend.dto.ChatMessageResponse.Status.NO_RECORDS);
                assertThat(response.events()).isEmpty();
                assertThat(response.absenceReason())
                                .isEqualTo(com.careme.backend.dto.ChatMessageResponse.AbsenceReason.NO_TERM_MATCH);
                assertThat(response.suggestedActions()).containsExactly(
                                com.careme.backend.dto.ChatMessageResponse.SuggestedAction.REFORMULATE,
                                com.careme.backend.dto.ChatMessageResponse.SuggestedAction.REGISTER);
        }

        @Test
        void turnsAQueryFailureIntoRetryableFailedWithoutEvents() {
                var intent = queryIntent(ClinicalEventIntent.Query.Scope.HISTORY);
                when(interpreter.interpret(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
                                .thenReturn(intent);
                when(historyQuery.answer(intent)).thenReturn(ClinicalAnswerResult.failure(
                                "No pude completar la búsqueda. Puedes volver a intentarlo."));

                var response = orchestrator.process(new ChatMessageRequest("¿Cuándo?", null, "msg-fail"));

                assertThat(response.status()).isEqualTo(com.careme.backend.dto.ChatMessageResponse.Status.FAILED);
                assertThat(response.events()).isEmpty();
                assertThat(response.absenceReason()).isNull();
                assertThat(response.suggestedActions()).isEmpty();
                verifyNoMoreInteractions(registration);
        }

        @Test
        void repeatsAQueryWithoutCallingTheProviderAgain() {
                var intent = queryIntent(ClinicalEventIntent.Query.Scope.HISTORY);
                when(interpreter.interpret(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
                                .thenReturn(intent);
                when(historyQuery.answer(intent)).thenReturn(ClinicalAnswerResult.answered(
                                List.of(event("evt_100", "Hipertensión")), "Respuesta"));

                var first = orchestrator.process(new ChatMessageRequest("¿Cuándo?", null, "msg-1"));
                var retry = orchestrator.process(new ChatMessageRequest("¿Cuándo?", first.conversationId(), "msg-1"));

                assertThat(retry).isEqualTo(first);
                verify(interpreter, org.mockito.Mockito.times(1))
                                .interpret(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
                verify(historyQuery, org.mockito.Mockito.times(1)).answer(intent);
        }

        @Test
        void feedsRecentTurnsIntoFollowingInterpretations() {
                when(interpreter.interpret(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
                                .thenReturn(ClinicalEventIntent.conversation());

                var first = orchestrator.process(new ChatMessageRequest("Hola", null, "msg-a"));
                orchestrator.process(new ChatMessageRequest("¿Y eso?", first.conversationId(), "msg-b"));

                var captor = org.mockito.ArgumentCaptor.forClass(ClinicalIntentInterpreter.InterpretationContext.class);
                verify(interpreter, org.mockito.Mockito.times(2))
                                .interpret(org.mockito.ArgumentMatchers.anyString(), captor.capture());

                assertThat(captor.getAllValues().get(0).recentTurns()).isEmpty();
                assertThat(captor.getAllValues().get(1).recentTurns()).singleElement()
                                .asString().contains("Hola");
        }

        private static ClinicalEventIntent queryIntent(ClinicalEventIntent.Query.Scope scope) {
                return ClinicalEventIntent.query(new ClinicalEventIntent.Query(
                                "¿Cuándo me diagnosticaron hipertensión?", scope, List.of("hipertension"), null, null, null));
        }

        private static ClinicalEvent event(String code, String content) {
                return new ClinicalEvent(
                                UUID.randomUUID(), code, ClinicalEvent.ClinicalEventType.DIAGNOSIS,
                                LocalDate.of(2026, 1, 10), ClinicalEvent.DatePrecision.EXACT, "el 10 de enero",
                                content, ClinicalEvent.EventSource.PATIENT, OffsetDateTime.now(ZoneOffset.UTC));
        }
}