package com.careme.backend.service;

import java.util.List;

/**
 * What one turn produced: the single response the person receives and the
 * operations the turn completed.
 *
 * @param message    the one response that covers everything the message required
 * @param operations every operation the turn asked for, in the order it ran,
 *                   including the ones that were rejected or failed
 * @param failed     whether the turn could not be completed in full
 */
public record AgentTurn(String message, List<AgentOperationResult> operations, boolean failed) {

    public AgentTurn {
        operations = operations == null ? List.of() : List.copyOf(operations);
    }

    public static AgentTurn completed(String message, List<AgentOperationResult> operations) {
        return new AgentTurn(message, operations, false);
    }

    /** A turn that could not be completed, which still reports what it did complete. */
    public static AgentTurn failed(String message, List<AgentOperationResult> operations) {
        return new AgentTurn(message, operations, true);
    }

    /** The operations that produced something the person can be told about. */
    public List<AgentOperationResult> completedOperations() {
        return operations.stream().filter(AgentOperationResult::completed).toList();
    }
}
