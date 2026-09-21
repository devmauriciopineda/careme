package com.careme.backend.service;

import com.careme.backend.dto.ChatMessageResponse;
import java.util.List;

/**
 * Turns what the agent did into the observable outcome of the turn.
 *
 * <p>The status of the turn describes the turn as a whole: the outcome of the
 * operation when the turn ran a single one, an answer when it produced one, and a
 * failure when any operation did not complete —even if another one of the same
 * turn did.
 */
public final class AgentTurnOutcome {

    /**
     * From the most informative to the least. A turn that both registered a fact
     * and answered a question reports an answer, and the registration travels in
     * the operations; a turn whose operations only registered reports a
     * registration.
     */
    private static final List<ChatMessageResponse.Status> PRECEDENCE = List.of(
            ChatMessageResponse.Status.ANSWERED,
            ChatMessageResponse.Status.NOTED,
            ChatMessageResponse.Status.DUPLICATE,
            ChatMessageResponse.Status.NO_RECORDS,
            ChatMessageResponse.Status.GENERAL_CONVERSATION);

    private AgentTurnOutcome() {
    }

    /**
     * The response of one turn: the single message the assistant produced, the
     * outcome of the turn as a whole, and the operations it went through.
     */
    public static ChatMessageResponse respond(String conversationId, String messageId, AgentTurn turn) {
        AgentOperationResult primary = primary(turn);
        return ChatMessageResponse.of(
                        conversationId,
                        messageId,
                        status(turn),
                        turn.message(),
                        primary == null ? List.of() : supportingEvents(primary),
                        primary == null ? null : absenceReason(primary),
                        primary == null ? List.of() : suggestedActions(primary))
                .withOperations(operations(turn));
    }

    public static ChatMessageResponse.Status status(AgentTurn turn) {
        if (turn.failed()) {
            return ChatMessageResponse.Status.FAILED;
        }
        List<ChatMessageResponse.Status> completed =
                turn.completedOperations().stream().map(AgentTurnOutcome::operationStatus).toList();
        for (ChatMessageResponse.Status candidate : PRECEDENCE) {
            if (completed.contains(candidate)) {
                return candidate;
            }
        }
        if (turn.operations().stream().anyMatch(AgentTurnOutcome::asksForInformation)) {
            return ChatMessageResponse.Status.CLARIFICATION_REQUIRED;
        }
        return ChatMessageResponse.Status.GENERAL_CONVERSATION;
    }

    /** The outcome of one operation, on its own. */
    public static ChatMessageResponse.Status operationStatus(AgentOperationResult result) {
        if (result.answer() != null) {
            return switch (result.answer().kind()) {
                case ANSWERED -> ChatMessageResponse.Status.ANSWERED;
                case NO_RECORDS -> ChatMessageResponse.Status.NO_RECORDS;
                case FAILURE -> ChatMessageResponse.Status.FAILED;
            };
        }
        if (result.registration() != null) {
            return switch (result.registration().kind()) {
                case REGISTERED -> ChatMessageResponse.Status.REGISTERED;
                case DUPLICATE -> ChatMessageResponse.Status.DUPLICATE;
                case CLARIFICATION -> ChatMessageResponse.Status.CLARIFICATION_REQUIRED;
                case CONVERSATION -> ChatMessageResponse.Status.GENERAL_CONVERSATION;
                case FAILURE -> ChatMessageResponse.Status.FAILED;
            };
        }
        if (result.note() != null) {
            return switch (result.note().kind()) {
                case COLLECTED -> ChatMessageResponse.Status.NOTED;
                case DUPLICATE -> ChatMessageResponse.Status.DUPLICATE;
                case FAILURE -> ChatMessageResponse.Status.FAILED;
            };
        }
        if (result.measurement() != null) {
            return switch (result.measurement().kind()) {
                case COLLECTED -> ChatMessageResponse.Status.NOTED;
                case DUPLICATE -> ChatMessageResponse.Status.DUPLICATE;
                case FAILURE -> ChatMessageResponse.Status.FAILED;
            };
        }
        if (result.kind() == AgentOperationResult.Kind.REJECTED) {
            return asksForInformation(result)
                    ? ChatMessageResponse.Status.CLARIFICATION_REQUIRED
                    : ChatMessageResponse.Status.GENERAL_CONVERSATION;
        }
        return ChatMessageResponse.Status.FAILED;
    }

    /**
     * Every operation the turn went through, in the order it ran, each with the
     * events it produced and its absence reason. The turn's message carries the
     * text; this carries what completed and what did not.
     */
    public static List<ChatMessageResponse.OperationSummary> operations(AgentTurn turn) {
        return turn.operations().stream()
                .map(result -> ChatMessageResponse.OperationSummary.of(
                        operationStatus(result),
                        supportingEvents(result),
                        absenceReason(result),
                        suggestedActions(result)))
                .toList();
    }

    /**
     * The operation that carries the turn's own outcome, so the response's
     * top-level fields describe the same thing the status names.
     */
    public static AgentOperationResult primary(AgentTurn turn) {
        ChatMessageResponse.Status status = status(turn);
        return turn.operations().stream()
                .filter(result -> operationStatus(result) == status)
                .findFirst()
                .orElse(null);
    }

    private static boolean asksForInformation(AgentOperationResult result) {
        return result.kind() == AgentOperationResult.Kind.REJECTED
                && result.rejection() == AgentOperationResult.Rejection.NOT_ADMISSIBLE;
    }

    private static List<com.careme.backend.entity.ClinicalEvent> supportingEvents(AgentOperationResult result) {
        if (result.answer() != null) {
            return result.answer().events();
        }
        if (result.registration() != null) {
            return result.registration().events();
        }
        return List.of();
    }

    private static ChatMessageResponse.AbsenceReason absenceReason(AgentOperationResult result) {
        return result.answer() == null ? null : result.answer().absenceReason();
    }

    private static List<ChatMessageResponse.SuggestedAction> suggestedActions(AgentOperationResult result) {
        return result.answer() == null ? List.of() : result.answer().suggestedActions();
    }
}
