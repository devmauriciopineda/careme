package com.careme.backend.service;

import com.careme.backend.dto.ChatMessageRequest;
import com.careme.backend.dto.ChatMessageResponse;
import com.careme.backend.dto.ClinicalEventIntent;
import com.careme.backend.service.ClinicalIntentInterpreter.InterpretationContext;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ChatOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(ChatOrchestrator.class);

    /**
     * The measurement tracking has its own space, so the answer is a fixed
     * redirect rather than a composed conversation.
     */
    private static final String MEASUREMENT_REDIRECT =
            "El peso y la circunferencia abdominal tienen su propio espacio de seguimiento"
                    + " y no forman parte de la historia clínica que consulto aquí.";

    private final ClinicalIntentInterpreter interpreter;
    private final ClinicalEventRegistrationService registrationService;
    private final ClinicalHistoryQueryService historyQueryService;
    private final ClinicalConversationComposer conversationComposer;
    private final ConversationStateStore stateStore;

    public ChatOrchestrator(
            ClinicalIntentInterpreter interpreter,
            ClinicalEventRegistrationService registrationService,
            ClinicalHistoryQueryService historyQueryService,
            ClinicalConversationComposer conversationComposer,
            ConversationStateStore stateStore) {
        this.interpreter = interpreter;
        this.registrationService = registrationService;
        this.historyQueryService = historyQueryService;
        this.conversationComposer = conversationComposer;
        this.stateStore = stateStore;
    }

    public ChatMessageResponse process(ChatMessageRequest request) {
        String conversationId = request.conversationId() == null || request.conversationId().isBlank()
                ? UUID.randomUUID().toString()
                : request.conversationId();
        ConversationStateStore.State state = stateStore.getOrCreate(conversationId);
        var previous = stateStore.outcome(conversationId, request.messageId());
        if (previous.isPresent()) {
            return previous.get();
        }

        String message = state.pendingMessage() == null
                ? request.message()
                : state.pendingMessage() + " " + request.message();
        ClinicalEventIntent intent;
        try {
            intent = interpreter.interpret(
                message,
                new InterpretationContext(
                    LocalDate.now(), ZoneId.systemDefault(), "clinical-intent-v3", state.recentTurns()));
        } catch (LlmIntegrationException exception) {
            log.warn("LLM provider failure conversationId={} messageId={}", conversationId, request.messageId());
            ChatMessageResponse response = ChatMessageResponse.of(
                conversationId,
                request.messageId(),
                ChatMessageResponse.Status.FAILED,
                "No pude procesar tu mensaje. Puedes reintentarlo.",
                null);
            state.remember(request.messageId(), response);
            return response;
        }
        ChatMessageResponse response = switch (intent.kind()) {
            case CLARIFICATION -> {
                state.pendingMessage(request.message());
                yield ChatMessageResponse.of(conversationId, request.messageId(),
                        ChatMessageResponse.Status.CLARIFICATION_REQUIRED, intent.clarification(), null);
            }
            case CONVERSATION -> {
                state.clearPendingMessage();
                yield conversationTurn(conversationId, request.messageId(), request.message(), state);
            }
            case EVENTS -> {
                var result = registrationService.register(conversationId, intent, LocalDate.now());
                state.clearPendingMessage();
                yield ChatMessageResponse.of(conversationId, request.messageId(), mapStatus(result.kind()),
                        result.message(), result.events());
            }
            case QUERY -> answerQuery(conversationId, request.messageId(), intent, state);
        };
        state.remember(request.messageId(), response);
        if (response.status() != ChatMessageResponse.Status.FAILED) {
            state.addTurn(request.message(), response.message());
        }
        return response;
    }

    /**
     * Answers a message that neither registers an event nor depends on the clinical
     * history. The clinical history is never read here: the composer receives only
     * the message and the recent turns of the active conversation, so the reply
     * cannot present anything as a recorded fact.
     */
    private ChatMessageResponse conversationTurn(
            String conversationId,
            String messageId,
            String message,
            ConversationStateStore.State state) {
        String reply;
        try {
            reply = conversationComposer.compose(message, state.recentTurns());
        } catch (LlmIntegrationException exception) {
            log.warn(
                    "Could not compose a conversational reply conversationId={} messageId={}",
                    conversationId,
                    messageId);
            return ChatMessageResponse.of(
                    conversationId,
                    messageId,
                    ChatMessageResponse.Status.FAILED,
                    "No pude elaborar la respuesta. Puedes reintentarlo.",
                    null);
        }
        return ChatMessageResponse.of(
                conversationId,
                messageId,
                ChatMessageResponse.Status.GENERAL_CONVERSATION,
                reply,
                null);
    }

    private ChatMessageResponse answerQuery(
            String conversationId,
            String messageId,
            ClinicalEventIntent intent,
            ConversationStateStore.State state) {
        state.clearPendingMessage();
        String generalReply = generalReply(intent.query().generalPart(), state);
        if (intent.query().scope() == ClinicalEventIntent.Query.Scope.MEASUREMENTS) {
            return ChatMessageResponse.of(
                    conversationId,
                    messageId,
                    ChatMessageResponse.Status.GENERAL_CONVERSATION,
                    MEASUREMENT_REDIRECT,
                    null,
                    null,
                    List.of(),
                    generalReply);
        }
        ClinicalAnswerResult result = historyQueryService.answer(intent);
        return ChatMessageResponse.of(conversationId, messageId, mapStatus(result.kind()),
                result.message(), result.events(), result.absenceReason(), result.suggestedActions(), generalReply);
    }

    /**
     * Composes the general part of a message that also depends on the clinical
     * history. A failure here never costs the clinical result already obtained: the
     * turn keeps its history outcome and simply carries no general part, and the
     * clinical history stays untouched either way.
     */
    private String generalReply(String generalPart, ConversationStateStore.State state) {
        if (generalPart == null) {
            return null;
        }
        try {
            return conversationComposer.compose(generalPart, state.recentTurns());
        } catch (LlmIntegrationException exception) {
            log.warn("Could not compose the general part of a mixed message");
            return null;
        }
    }

    private ChatMessageResponse.Status mapStatus(ClinicalEventRegistrationResult.Kind kind) {
        return switch (kind) {
            case REGISTERED -> ChatMessageResponse.Status.REGISTERED;
            case DUPLICATE -> ChatMessageResponse.Status.DUPLICATE;
            case CLARIFICATION -> ChatMessageResponse.Status.CLARIFICATION_REQUIRED;
            case CONVERSATION -> ChatMessageResponse.Status.GENERAL_CONVERSATION;
            case FAILURE -> ChatMessageResponse.Status.FAILED;
        };
    }

    private ChatMessageResponse.Status mapStatus(ClinicalAnswerResult.Kind kind) {
        return switch (kind) {
            case ANSWERED -> ChatMessageResponse.Status.ANSWERED;
            case NO_RECORDS -> ChatMessageResponse.Status.NO_RECORDS;
            case FAILURE -> ChatMessageResponse.Status.FAILED;
        };
    }
}