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
    private final ConversationStateStore stateStore = new ConversationStateStore(10, java.time.Duration.ofHours(1));
    private final ChatOrchestrator orchestrator = new ChatOrchestrator(interpreter, registration, stateStore);

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
}