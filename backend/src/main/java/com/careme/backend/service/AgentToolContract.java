package com.careme.backend.service;

import com.careme.backend.dto.AgentOperation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The provider-facing shape of the operation set: how the available operations
 * are declared to the assistant, and how the operations it asks for are read
 * back.
 *
 * <p>This is the only place that knows the wire shape of a declaration, so the
 * set of operations stays defined once, in {@link AgentOperation}.
 */
public final class AgentToolContract {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private AgentToolContract() {
    }

    /** Every available operation, declared for the assistant. */
    public static List<Map<String, Object>> tools() {
        return Arrays.stream(AgentOperation.values())
                .map(operation -> Map.<String, Object>of(
                        "type", "function",
                        "function", Map.of(
                                "name", operation.operationName(),
                                "description", operation.description(),
                                "parameters", operation.arguments())))
                .toList();
    }

    /**
     * The user message of one turn: the reference date, the timezone relative
     * expressions are resolved against, the recent turns that keep a follow-up's
     * referents, and the message itself.
     */
    public static String userContent(
            String message, LocalDate referenceDate, ZoneId zoneId, List<String> recentTurns) {
        StringBuilder content = new StringBuilder()
                .append("Fecha de referencia: ").append(referenceDate)
                .append(". Zona horaria: ").append(zoneId);
        if (recentTurns != null && !recentTurns.isEmpty()) {
            content.append(". Turnos recientes: ").append(String.join(" || ", recentTurns));
        }
        return content.append(". Mensaje: ").append(message).toString();
    }

    /**
     * The operations the assistant asked for, in the order it asked for them.
     *
     * <p>A name that is not one of the available operations resolves to nothing
     * and never becomes a call, so an operation outside the set cannot be
     * executed even if the assistant asks for it.
     */
    public static List<AgentOperationCall> toolCalls(JsonNode providerMessage) {
        List<AgentOperationCall> calls = new ArrayList<>();
        if (providerMessage == null) {
            return calls;
        }
        for (JsonNode toolCall : providerMessage.path("tool_calls")) {
            AgentOperationCall call = operationCall(toolCall);
            if (call != null) {
                calls.add(call);
            }
        }
        return calls;
    }

    /**
     * Resolves one tool call the assistant asked for, or {@code null} when it
     * names an operation that is not available.
     */
    public static AgentOperationCall operationCall(JsonNode toolCall) {
        if (toolCall == null) {
            return null;
        }
        JsonNode function = toolCall.path("function");
        return AgentOperationCall.of(function.path("name").asText(null), arguments(function.path("arguments")))
                .orElse(null);
    }

    /** The text the assistant produced for the turn, or {@code null} when it produced none. */
    public static String finalMessage(JsonNode providerMessage) {
        if (providerMessage == null) {
            return null;
        }
        JsonNode content = providerMessage.path("content");
        if (!content.isTextual()) {
            return null;
        }
        String text = content.asText().trim();
        return text.isEmpty() ? null : text;
    }

    /**
     * Reads the arguments of a tool call. The provider sends them as a JSON
     * string; anything that cannot be read is treated as absent, so the operation
     * that needed it is rejected downstream instead of being guessed at.
     */
    private static JsonNode arguments(JsonNode arguments) {
        if (arguments == null || arguments.isMissingNode() || arguments.isNull()) {
            return null;
        }
        if (!arguments.isTextual()) {
            return arguments;
        }
        try {
            return OBJECT_MAPPER.readTree(arguments.asText());
        } catch (JsonProcessingException exception) {
            return null;
        }
    }
}
