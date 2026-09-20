package com.careme.backend.service;

import com.careme.backend.dto.AgentOperation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.Optional;

/**
 * A request from the assistant to run one of the available operations, with the
 * arguments it supplied.
 *
 * <p>Resolution is the guard: a name that is not one of the declared operations
 * produces no call at all, so an operation outside the set can never reach an
 * executor.
 */
public record AgentOperationCall(AgentOperation operation, JsonNode arguments) {

    public AgentOperationCall {
        if (operation == null) {
            throw new IllegalArgumentException("Agent operation must not be null");
        }
        arguments = arguments == null ? JsonNodeFactory.instance.objectNode() : arguments;
    }

    /**
     * Resolves the requested operation, or empty when it is not one of the
     * available ones.
     */
    public static Optional<AgentOperationCall> of(String operationName, JsonNode arguments) {
        return AgentOperation.byName(operationName)
                .map(operation -> new AgentOperationCall(operation, arguments));
    }
}
