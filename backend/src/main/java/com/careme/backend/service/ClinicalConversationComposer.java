package com.careme.backend.service;

import java.util.List;

/**
 * Composes the Spanish reply for a message that neither records a clinical event
 * nor depends on the clinical history.
 *
 * <p>The signature is the guarantee: the composer receives only the user's words
 * and the recent turns of the active conversation. It has no retrieved event, no
 * reference and no absence to work from, so it cannot present anything as a fact
 * recorded in the clinical history.
 */
public interface ClinicalConversationComposer {

    /**
     * Replies to {@code message} as general conversation.
     *
     * @param message     the user's message, in their own words
     * @param recentTurns the bounded buffer of the active conversation, so a
     *                    follow-up keeps its referents without the history
     * @throws LlmIntegrationException when the reply cannot be composed
     */
    String compose(String message, List<String> recentTurns);
}
