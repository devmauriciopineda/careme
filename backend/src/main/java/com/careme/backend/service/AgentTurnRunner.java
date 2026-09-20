package com.careme.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs one turn: it keeps asking the assistant what it needs, executes the
 * operations it asks for through the application, hands the results back and
 * stops when the assistant produces the response —or when the turn cannot be
 * completed.
 *
 * <p>The turn is bounded twice over: by the number of operations it may execute,
 * which keeps a runaway assistant from going on forever, and by the fact that
 * every execution is driven by something the assistant asked for. The runner
 * never starts an operation on its own and never acts after it has answered.
 *
 * <p>A failure never undoes what was already completed: a registration that
 * reached the clinical history stays there, and the turn reports both what it
 * completed and what it could not.
 */
public class AgentTurnRunner {

    private static final Logger log = LoggerFactory.getLogger(AgentTurnRunner.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /** Spanish, retryable and with no internal detail. */
    private static final String RETRYABLE_FAILURE =
            "No he podido completar lo que me pedías. Puedes reintentarlo.";

    private static final String INCOMPLETE =
            "No he podido atender todo lo que me pedías en este mensaje. Puedes pedírmelo de nuevo.";

    private final AgentProvider provider;
    private final AgentOperationExecutor executor;
    private final int maxOperations;

    public AgentTurnRunner(AgentProvider provider, AgentOperationExecutor executor, int maxOperations) {
        this.provider = provider;
        this.executor = executor;
        this.maxOperations = maxOperations;
    }

    public AgentTurn run(String message, AgentContext context) {
        List<Map<String, Object>> conversation = new ArrayList<>();
        conversation.add(userMessage(message, context));

        List<AgentOperationResult> operations = new ArrayList<>();
        int budget = maxOperations;
        while (true) {
            AgentProvider.ProviderTurn turn;
            try {
                turn = provider.send(conversation);
            } catch (LlmIntegrationException exception) {
                // The assistant is not available. Whatever an earlier round already
                // completed stays in the clinical history; nothing new is executed.
                log.warn("The assistant could not answer conversationId={}", context.conversationId());
                return AgentTurn.failed(RETRYABLE_FAILURE, operations);
            }

            if (turn.calls().isEmpty()) {
                if (turn.message() == null) {
                    log.warn("The assistant produced neither an operation nor a response");
                    return AgentTurn.failed(RETRYABLE_FAILURE, operations);
                }
                return AgentTurn.completed(turn.message(), operations);
            }

            conversation.add(turn.assistantMessage());
            for (AgentProvider.ProviderCall call : turn.calls()) {
                if (budget == 0) {
                    log.warn("The turn reached its operation limit conversationId={}", context.conversationId());
                    return AgentTurn.failed(INCOMPLETE, operations);
                }
                budget--;
                AgentOperationResult result =
                        executor.execute(context.conversationId(), call.call(), context.referenceDate());
                operations.add(result);
                if (result.kind() == AgentOperationResult.Kind.FAILED) {
                    return AgentTurn.failed(retryable(result.message()), operations);
                }
                conversation.add(toolResult(call.id(), result));
            }
        }
    }

    private static Map<String, Object> userMessage(String message, AgentContext context) {
        return Map.of(
                "role", "user",
                "content", AgentToolContract.userContent(
                        message, context.referenceDate(), context.zoneId(), context.recentTurns()));
    }

    /**
     * Hands one operation result back to the assistant. The payload carries no
     * storage detail —only what the operation produced— and the outcome names
     * whether it completed, found nothing or was rejected, so a rejection becomes
     * a reason to ask for what is missing instead of a guess.
     */
    private static Map<String, Object> toolResult(String toolCallId, AgentOperationResult result) {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("outcome", result.kind().name().toLowerCase(Locale.ROOT));
        content.put("note", result.message() == null ? "" : result.message());
        content.putAll(result.payload());
        return Map.of(
                "role", "tool",
                "tool_call_id", toolCallId == null ? "" : toolCallId,
                "content", json(content));
    }

    private static String json(Map<String, Object> content) {
        try {
            return OBJECT_MAPPER.writeValueAsString(content);
        } catch (JsonProcessingException exception) {
            log.warn("Could not serialise an operation result for the assistant");
            return "{}";
        }
    }

    private static String retryable(String message) {
        return message == null || message.isBlank() ? RETRYABLE_FAILURE : message;
    }
}
