package com.careme.backend.service;

import com.careme.backend.dto.ChatMessageRequest;
import com.careme.backend.dto.ChatMessageResponse;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Runs one chat message.
 *
 * <p>The orchestrator no longer decides the channel of a message: the assistant
 * decides what it needs and the application executes it. What the orchestrator
 * keeps is the contract with the client —one outcome per message, idempotent
 * retries, and a bounded in-memory conversation that never persists history.
 */
@Service
public class ChatOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(ChatOrchestrator.class);

    private static final String RETRYABLE_FAILURE =
            "No he podido completar lo que me pedías. Puedes reintentarlo.";

    private final ClinicalAgent agent;
    private final ConversationStateStore stateStore;

    public ChatOrchestrator(ClinicalAgent agent, ConversationStateStore stateStore) {
        this.agent = agent;
        this.stateStore = stateStore;
    }

    public ChatMessageResponse process(ChatMessageRequest request) {
        String conversationId = request.conversationId() == null || request.conversationId().isBlank()
                ? UUID.randomUUID().toString()
                : request.conversationId();
        ConversationStateStore.State state = stateStore.getOrCreate(conversationId);
        var previous = stateStore.outcome(conversationId, request.messageId());
        if (previous.isPresent()) {
            // A repeated message is answered with the outcome it already produced, so
            // no operation runs a second time.
            return previous.get();
        }

        // A message that answers a pending question travels with it, so an operation
        // that lacked information can be completed without repeating everything.
        String message = state.pendingMessage() == null
                ? request.message()
                : state.pendingMessage() + " " + request.message();

        AgentTurn turn;
        try {
            turn = agent.respond(
                    message,
                    new AgentContext(
                            conversationId, LocalDate.now(), ZoneId.systemDefault(), state.recentTurns()));
        } catch (RuntimeException exception) {
            // Nothing was executed on this path, so the clinical history is untouched
            // and the message can be repeated.
            log.warn(
                    "Could not complete the turn conversationId={} messageId={}",
                    conversationId,
                    request.messageId());
            ChatMessageResponse failure = ChatMessageResponse.of(
                    conversationId,
                    request.messageId(),
                    ChatMessageResponse.Status.FAILED,
                    RETRYABLE_FAILURE,
                    null);
            state.remember(request.messageId(), failure);
            return failure;
        }

        ChatMessageResponse response = AgentTurnOutcome.respond(conversationId, request.messageId(), turn);
        if (response.status() == ChatMessageResponse.Status.CLARIFICATION_REQUIRED) {
            state.pendingMessage(request.message());
        } else {
            state.clearPendingMessage();
        }
        state.remember(request.messageId(), response);
        if (response.status() != ChatMessageResponse.Status.FAILED) {
            state.addTurn(request.message(), response.message());
        }
        return response;
    }
}
