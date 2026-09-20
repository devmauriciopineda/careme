package com.careme.backend.service;

import java.util.List;
import java.util.Map;

/**
 * One exchange with the assistant provider.
 *
 * <p>The provider receives the conversation so far and answers either with the
 * operations it needs —one or more— or with the message it produced. It never
 * reaches the clinical history itself: the operations it asks for are executed by
 * the application.
 */
public interface AgentProvider {

    ProviderTurn send(List<Map<String, Object>> conversation);

    /**
     * One answer from the provider.
     *
     * @param assistantMessage the assistant turn to echo back in the next request,
     *                         in the shape the provider expects
     * @param calls            the operations it asked for, in order
     * @param message          the text it produced, or {@code null} when it only
     *                         asked for operations
     */
    record ProviderTurn(Map<String, Object> assistantMessage, List<ProviderCall> calls, String message) {

        public ProviderTurn {
            calls = calls == null ? List.of() : List.copyOf(calls);
        }
    }

    /**
     * One operation the provider asked for.
     *
     * @param call the resolved operation, or {@code null} when it asked for one
     *             that is not available; the application rejects it without
     *             executing anything and tells the provider, so it can explain
     *             instead of guessing
     */
    record ProviderCall(String id, AgentOperationCall call) {
    }
}
