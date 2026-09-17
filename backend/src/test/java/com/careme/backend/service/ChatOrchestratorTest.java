package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.careme.backend.dto.ChatMessageRequest;
import com.careme.backend.dto.ChatMessageResponse;
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
    private final ClinicalConversationComposer conversationComposer = mock(ClinicalConversationComposer.class);
    private final ConversationStateStore stateStore = new ConversationStateStore(10, java.time.Duration.ofHours(1));
    private final ChatOrchestrator orchestrator =
            new ChatOrchestrator(interpreter, registration, historyQuery, conversationComposer, stateStore);

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
                when(conversationComposer.compose(org.mockito.ArgumentMatchers.anyString(),
                                org.mockito.ArgumentMatchers.any())).thenReturn("Te respondo como conversación general.");
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

        @Test
        void composesAConversationalReplyInsteadOfAnAcknowledgement() {
                when(interpreter.interpret(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
                                .thenReturn(ClinicalEventIntent.conversation());
                when(conversationComposer.compose(org.mockito.ArgumentMatchers.eq("¿Qué es la hipertensión?"),
                                org.mockito.ArgumentMatchers.any())).thenReturn("Es una condición que se mide en consulta.");

                var response = orchestrator.process(new ChatMessageRequest("¿Qué es la hipertensión?", null, "msg-conv"));

                assertThat(response.status()).isEqualTo(ChatMessageResponse.Status.GENERAL_CONVERSATION);
                assertThat(response.message()).isEqualTo("Es una condición que se mide en consulta.");
        }

        @Test
        void neverReadsTheClinicalHistoryForAGeneralMessage() {
                when(interpreter.interpret(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
                                .thenReturn(ClinicalEventIntent.conversation());
                when(conversationComposer.compose(org.mockito.ArgumentMatchers.anyString(),
                                org.mockito.ArgumentMatchers.any())).thenReturn("Te respondo como conversación general.");

                orchestrator.process(new ChatMessageRequest("Hola, ¿cómo estás?", null, "msg-general"));

                verifyNoMoreInteractions(historyQuery);
                verifyNoMoreInteractions(registration);
        }

        @Test
        void keepsTheConversationalTurnFreeOfEventsAndAbsence() {
                when(interpreter.interpret(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
                                .thenReturn(ClinicalEventIntent.conversation());
                when(conversationComposer.compose(org.mockito.ArgumentMatchers.anyString(),
                                org.mockito.ArgumentMatchers.any())).thenReturn("Te respondo como conversación general.");

                var response = orchestrator.process(new ChatMessageRequest("Hola", null, "msg-free"));

                assertThat(response.events()).isEmpty();
                assertThat(response.absenceReason()).isNull();
                assertThat(response.suggestedActions()).isEmpty();
                assertThat(response.generalReply()).isNull();
        }

        @Test
        void turnsAConversationCompositionFailureIntoRetryableFailed() {
                when(interpreter.interpret(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
                                .thenReturn(ClinicalEventIntent.conversation());
                when(conversationComposer.compose(org.mockito.ArgumentMatchers.anyString(),
                                org.mockito.ArgumentMatchers.any()))
                                .thenThrow(new LlmIntegrationException("provider detail must stay internal"));

                var first = orchestrator.process(new ChatMessageRequest("Hola", null, "msg-fail-conv"));

                assertThat(first.status()).isEqualTo(ChatMessageResponse.Status.FAILED);
                assertThat(first.message()).isEqualTo("No pude elaborar la respuesta. Puedes reintentarlo.");
                verifyNoMoreInteractions(historyQuery);

                // The message is preserved: sending it again with a new identifier is
                // attempted again instead of being stuck in the failed outcome.
                var retried = orchestrator.process(new ChatMessageRequest("Hola", first.conversationId(), "msg-fail-conv-2"));
                assertThat(retried.status()).isEqualTo(ChatMessageResponse.Status.FAILED);
                verify(conversationComposer, org.mockito.Mockito.times(2))
                                .compose(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
        }

        @Test
        void answersWithRecentTurnsWithoutTreatingThemAsRecords() {
                when(interpreter.interpret(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
                                .thenReturn(ClinicalEventIntent.conversation());
                when(conversationComposer.compose(org.mockito.ArgumentMatchers.anyString(),
                                org.mockito.ArgumentMatchers.any())).thenReturn("Te respondo como conversación general.");

                var first = orchestrator.process(new ChatMessageRequest("Hola", null, "msg-turn-a"));
                orchestrator.process(new ChatMessageRequest("¿Y eso qué significa?", first.conversationId(), "msg-turn-b"));

                var captor = org.mockito.ArgumentCaptor.forClass(List.class);
                verify(conversationComposer, org.mockito.Mockito.times(2))
                                .compose(org.mockito.ArgumentMatchers.anyString(), captor.capture());

                assertThat(captor.getAllValues().get(0)).isEmpty();
                assertThat(captor.getAllValues().get(1).toString()).contains("Hola");
                verifyNoMoreInteractions(historyQuery);
        }

        @Test
        void refersMeasurementsWithoutPassingThroughTheConversationComposer() {
                var intent = queryIntent(ClinicalEventIntent.Query.Scope.MEASUREMENTS);
                when(interpreter.interpret(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
                                .thenReturn(intent);

                var response = orchestrator.process(new ChatMessageRequest("¿Cuánto peso?", null, "msg-measurements-composer"));

                assertThat(response.status()).isEqualTo(ChatMessageResponse.Status.GENERAL_CONVERSATION);
                assertThat(response.message()).contains("peso");
                assertThat(response.generalReply()).isNull();
                verifyNoMoreInteractions(conversationComposer);
                verifyNoMoreInteractions(historyQuery);
        }

        @Test
        void separatesTheGeneralPartOfAMixedMessage() {
                var intent = mixedQueryIntent("¿Qué es la hipertensión?");
                when(interpreter.interpret(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
                                .thenReturn(intent);
                when(historyQuery.answer(intent)).thenReturn(ClinicalAnswerResult.answered(
                                List.of(event("evt_100", "Hipertensión")), "Te la diagnosticaron en enero."));
                when(conversationComposer.compose(org.mockito.ArgumentMatchers.eq("¿Qué es la hipertensión?"),
                                org.mockito.ArgumentMatchers.any())).thenReturn("Es una condición que se mide en consulta.");

                var response = orchestrator.process(
                                new ChatMessageRequest("¿Qué es la hipertensión? ¿Cuándo me la diagnosticaron?", null, "msg-mixed"));

                assertThat(response.status()).as("el estado es el de la historia").isEqualTo(ChatMessageResponse.Status.ANSWERED);
                assertThat(response.message()).isEqualTo("Te la diagnosticaron en enero.");
                assertThat(response.generalReply()).isEqualTo("Es una condición que se mide en consulta.");
                assertThat(response.events()).singleElement()
                                .satisfies(summary -> assertThat(summary.code()).isEqualTo("evt_100"));
        }

        @Test
        void keepsTheAbsenceSeparateFromTheGeneralPartOfAMixedMessage() {
                var intent = mixedQueryIntent("¿Qué es la hipertensión?");
                when(interpreter.interpret(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
                                .thenReturn(intent);
                when(historyQuery.answer(intent)).thenReturn(ClinicalAnswerResult.noRecords(
                                ChatMessageResponse.AbsenceReason.NO_TERM_MATCH,
                                List.of(ChatMessageResponse.SuggestedAction.REFORMULATE,
                                                ChatMessageResponse.SuggestedAction.REGISTER),
                                "No encuentro registros."));
                when(conversationComposer.compose(org.mockito.ArgumentMatchers.eq("¿Qué es la hipertensión?"),
                                org.mockito.ArgumentMatchers.any())).thenReturn("Es una condición que se mide en consulta.");

                var response = orchestrator.process(
                                new ChatMessageRequest("¿Qué es la hipertensión? ¿He tenido migrañas?", null, "msg-mixed-absence"));

                assertThat(response.status()).isEqualTo(ChatMessageResponse.Status.NO_RECORDS);
                assertThat(response.absenceReason()).isEqualTo(ChatMessageResponse.AbsenceReason.NO_TERM_MATCH);
                assertThat(response.events()).isEmpty();
                assertThat(response.generalReply()).isEqualTo("Es una condición que se mide en consulta.");
        }

        @Test
        void doesNotUseTheGeneralPartAsSearchCriteriaOrSupport() {
                var intent = mixedQueryIntent("¿Qué es la hipertensión?");
                when(interpreter.interpret(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
                                .thenReturn(intent);
                when(historyQuery.answer(intent)).thenReturn(ClinicalAnswerResult.answered(
                                List.of(event("evt_100", "Hipertensión")), "Te la diagnosticaron en enero."));
                when(conversationComposer.compose(org.mockito.ArgumentMatchers.anyString(),
                                org.mockito.ArgumentMatchers.any())).thenReturn("Es una condición que se mide en consulta.");

                var response = orchestrator.process(
                                new ChatMessageRequest("¿Qué es la hipertensión? ¿Cuándo me la diagnosticaron?", null, "msg-mixed-criteria"));

                // The query service receives the query exactly as classified, so the general
                // part never becomes a retrieval criterion, and the only support reported is
                // the set the history returned.
                verify(historyQuery).answer(intent);
                assertThat(intent.query().generalPart()).isEqualTo("¿Qué es la hipertensión?");
                assertThat(response.events()).hasSize(1);
        }

        @Test
        void keepsTheHistoryOutcomeWhenTheGeneralPartFailsToCompose() {
                var intent = mixedQueryIntent("¿Qué es la hipertensión?");
                when(interpreter.interpret(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
                                .thenReturn(intent);
                when(historyQuery.answer(intent)).thenReturn(ClinicalAnswerResult.answered(
                                List.of(event("evt_100", "Hipertensión")), "Te la diagnosticaron en enero."));
                when(conversationComposer.compose(org.mockito.ArgumentMatchers.anyString(),
                                org.mockito.ArgumentMatchers.any()))
                                .thenThrow(new LlmIntegrationException("provider unavailable"));

                var response = orchestrator.process(
                                new ChatMessageRequest("¿Qué es la hipertensión? ¿Cuándo me la diagnosticaron?", null, "msg-mixed-failure"));

                assertThat(response.status()).isEqualTo(ChatMessageResponse.Status.ANSWERED);
                assertThat(response.message()).isEqualTo("Te la diagnosticaron en enero.");
                assertThat(response.generalReply()).isNull();
                assertThat(response.events()).hasSize(1);
        }

        @Test
        void requiresClarificationWhenTheChannelIsUndetermined() {
                when(interpreter.interpret(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
                                .thenReturn(ClinicalEventIntent.clarification(
                                                "¿Tu pregunta se refiere a tu historia clínica o es una duda general?"));

                var response = orchestrator.process(new ChatMessageRequest("¿Eso es bueno?", null, "msg-undetermined"));

                assertThat(response.status()).isEqualTo(ChatMessageResponse.Status.CLARIFICATION_REQUIRED);
                assertThat(response.message()).contains("historia clínica");
                verifyNoMoreInteractions(historyQuery);
                verifyNoMoreInteractions(conversationComposer);
        }

        @Test
        void interpretsTheAnswerToAChannelClarificationWithThePendingMessage() {
                when(interpreter.interpret(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
                                .thenReturn(ClinicalEventIntent.clarification(
                                                "¿Tu pregunta se refiere a tu historia clínica o es una duda general?"));

                var first = orchestrator.process(new ChatMessageRequest("¿Eso es bueno?", null, "msg-pending-a"));
                assertThat(first.status()).isEqualTo(ChatMessageResponse.Status.CLARIFICATION_REQUIRED);

                when(interpreter.interpret(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
                                .thenReturn(ClinicalEventIntent.conversation());
                when(conversationComposer.compose(org.mockito.ArgumentMatchers.anyString(),
                                org.mockito.ArgumentMatchers.any())).thenReturn("Te respondo como conversación general.");

                orchestrator.process(new ChatMessageRequest("Es una duda general", first.conversationId(), "msg-pending-b"));

                verify(interpreter).interpret(
                                org.mockito.ArgumentMatchers.eq("¿Eso es bueno? Es una duda general"),
                                org.mockito.ArgumentMatchers.any());
        }

        private static ClinicalEventIntent mixedQueryIntent(String generalPart) {
                return ClinicalEventIntent.query(new ClinicalEventIntent.Query(
                                "¿Cuándo me diagnosticaron hipertensión?",
                                ClinicalEventIntent.Query.Scope.HISTORY,
                                List.of("diagnosticaron"),
                                null,
                                null,
                                null,
                                generalPart));
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