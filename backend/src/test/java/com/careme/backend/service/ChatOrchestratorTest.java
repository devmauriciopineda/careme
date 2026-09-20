package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.careme.backend.dto.AgentOperation;
import com.careme.backend.dto.ChatMessageRequest;
import com.careme.backend.dto.ChatMessageResponse;
import com.careme.backend.entity.ClinicalEvent;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * The orchestrator's contract with the client: one outcome per message, retries
 * that do not repeat work, a bounded in-memory conversation, and a turn that is
 * handed to the assistant instead of being decided here.
 */
class ChatOrchestratorTest {

    private static final String CONVERSATION_ID = "conversation-1";

    private final ClinicalAgent agent = mock(ClinicalAgent.class);
    private final ConversationStateStore stateStore = new ConversationStateStore(10, Duration.ofMinutes(30));
    private final ChatOrchestrator orchestrator = new ChatOrchestrator(agent, stateStore);

    @Test
    void createsAConversationWhenNoneIsGiven() {
        when(agent.respond(anyString(), any())).thenReturn(answered());

        ChatMessageResponse response =
                orchestrator.process(request("¿Qué diagnósticos tengo?", null, UUID.randomUUID().toString()));

        assertThat(response.conversationId()).isNotBlank();
        assertThat(response.status()).isEqualTo(ChatMessageResponse.Status.ANSWERED);
        assertThat(response.message()).isEqualTo("Hace unos dos años.");
        assertThat(response.operations()).singleElement()
                .satisfies(operation -> assertThat(operation.status())
                        .isEqualTo(ChatMessageResponse.Status.ANSWERED));
        assertThat(response.events()).singleElement()
                .satisfies(event -> assertThat(event.code()).isEqualTo("evt_001"));
    }

    @Test
    void answersARepeatedMessageWithTheOutcomeItAlreadyProduced() {
        when(agent.respond(anyString(), any())).thenReturn(answered());
        String messageId = UUID.randomUUID().toString();

        ChatMessageResponse first = orchestrator.process(request("¿Qué diagnósticos tengo?", CONVERSATION_ID, messageId));
        ChatMessageResponse second = orchestrator.process(request("¿Qué diagnósticos tengo?", CONVERSATION_ID, messageId));

        assertThat(second).isEqualTo(first);
        verify(agent, times(1)).respond(anyString(), any());
    }

    @Test
    void handsTheRecentTurnsOfTheActiveConversationToTheAssistant() {
        when(agent.respond(anyString(), any())).thenReturn(answered());

        orchestrator.process(request("¿Qué diagnósticos tengo?", CONVERSATION_ID, UUID.randomUUID().toString()));
        orchestrator.process(request("¿y la medicación?", CONVERSATION_ID, UUID.randomUUID().toString()));

        ArgumentCaptor<AgentContext> contexts = ArgumentCaptor.forClass(AgentContext.class);
        verify(agent, times(2)).respond(anyString(), contexts.capture());
        assertThat(contexts.getAllValues().get(0).recentTurns()).isEmpty();
        assertThat(contexts.getAllValues().get(1).recentTurns()).hasSize(1);
        assertThat(contexts.getAllValues().get(1).conversationId()).isEqualTo(CONVERSATION_ID);
    }

    @Test
    void carriesThePendingMessageIntoTheNextTurn() {
        when(agent.respond(anyString(), any())).thenReturn(clarification()).thenReturn(answered());

        orchestrator.process(request("Tomaba algo", CONVERSATION_ID, UUID.randomUUID().toString()));
        orchestrator.process(request("para la tensión", CONVERSATION_ID, UUID.randomUUID().toString()));

        ArgumentCaptor<String> messages = ArgumentCaptor.forClass(String.class);
        verify(agent, times(2)).respond(messages.capture(), any());
        assertThat(messages.getAllValues().get(1)).isEqualTo("Tomaba algo para la tensión");
    }

    @Test
    void clearsThePendingMessageOnceATurnCompletes() {
        when(agent.respond(anyString(), any()))
                .thenReturn(clarification())
                .thenReturn(answered())
                .thenReturn(answered());

        orchestrator.process(request("Tomaba algo", CONVERSATION_ID, UUID.randomUUID().toString()));
        orchestrator.process(request("para la tensión", CONVERSATION_ID, UUID.randomUUID().toString()));
        orchestrator.process(request("Gracias", CONVERSATION_ID, UUID.randomUUID().toString()));

        ArgumentCaptor<String> messages = ArgumentCaptor.forClass(String.class);
        verify(agent, times(3)).respond(messages.capture(), any());
        assertThat(messages.getAllValues().get(2)).isEqualTo("Gracias");
    }

    @Test
    void remembersThePendingMessageOnlyWhileTheTurnAsksForClarification() {
        when(agent.respond(anyString(), any())).thenReturn(clarification());

        ChatMessageResponse response =
                orchestrator.process(request("Tomaba algo", CONVERSATION_ID, UUID.randomUUID().toString()));

        assertThat(response.status()).isEqualTo(ChatMessageResponse.Status.CLARIFICATION_REQUIRED);
        assertThat(response.message()).isEqualTo("¿Cuándo ocurrió?");
    }

    @Test
    void keepsAFailedTurnOutOfTheRecentTurns() {
        when(agent.respond(anyString(), any())).thenReturn(failedTurn());

        ChatMessageResponse response =
                orchestrator.process(request("¿Qué diagnósticos tengo?", CONVERSATION_ID, UUID.randomUUID().toString()));

        assertThat(response.status()).isEqualTo(ChatMessageResponse.Status.FAILED);
        assertThat(response.operations()).singleElement()
                .satisfies(operation -> assertThat(operation.status())
                        .isEqualTo(ChatMessageResponse.Status.FAILED));
    }

    @Test
    void reportsARetryableFailureWhenTheAssistantThrows() {
        when(agent.respond(anyString(), any())).thenThrow(new LlmIntegrationException("provider unavailable"));

        ChatMessageResponse response =
                orchestrator.process(request("¿Qué diagnósticos tengo?", CONVERSATION_ID, UUID.randomUUID().toString()));

        assertThat(response.status()).isEqualTo(ChatMessageResponse.Status.FAILED);
        assertThat(response.message()).contains("Puedes reintentarlo");
        assertThat(response.message()).doesNotContain("provider unavailable");
        assertThat(response.operations()).isEmpty();
    }

    @Test
    void answersConversationWithTheReplyTheAssistantProduced() {
        when(agent.respond(anyString(), any()))
                .thenReturn(AgentTurn.completed("Hola, cuéntame qué quieres registrar.", List.of()));

        ChatMessageResponse response =
                orchestrator.process(request("Hola", CONVERSATION_ID, UUID.randomUUID().toString()));

        assertThat(response.status()).isEqualTo(ChatMessageResponse.Status.GENERAL_CONVERSATION);
        assertThat(response.message()).isEqualTo("Hola, cuéntame qué quieres registrar.");
        assertThat(response.operations()).isEmpty();
    }

    private static ChatMessageRequest request(String message, String conversationId, String messageId) {
        return new ChatMessageRequest(message, conversationId, messageId);
    }

    private static AgentTurn answered() {
        return AgentTurn.completed(
                "Hace unos dos años.",
                List.of(AgentOperationResult.of(
                        AgentOperation.CONSULT_HISTORY,
                        ClinicalAnswerResult.answered(List.of(event()), "Hace unos dos años."))));
    }

    private static AgentTurn clarification() {
        return AgentTurn.completed(
                "¿Cuándo ocurrió?",
                List.of(AgentOperationResult.rejected(
                        AgentOperationResult.Rejection.NOT_ADMISSIBLE,
                        "No he podido completar esa operación con lo que me has dicho.")));
    }

    private static AgentTurn failedTurn() {
        return AgentTurn.failed(
                "No pude buscar en tu historia.",
                List.of(AgentOperationResult.of(
                        AgentOperation.CONSULT_HISTORY,
                        ClinicalAnswerResult.failure("No pude buscar en tu historia."))));
    }

    private static ClinicalEvent event() {
        return new ClinicalEvent(
                UUID.randomUUID(),
                "evt_001",
                ClinicalEvent.ClinicalEventType.DIAGNOSIS,
                LocalDate.of(2024, 3, 1),
                ClinicalEvent.DatePrecision.EXACT,
                null,
                "Hipertensión diagnosticada.",
                ClinicalEvent.EventSource.PATIENT,
                OffsetDateTime.now(ZoneOffset.UTC));
    }
}
