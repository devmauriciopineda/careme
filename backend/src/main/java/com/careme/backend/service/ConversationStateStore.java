package com.careme.backend.service;

import com.careme.backend.dto.ChatMessageResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ConversationStateStore {

    private final int maxConversations;
    private final Duration ttl;
    private final Map<String, State> states = new LinkedHashMap<>();

    public ConversationStateStore(
            @Value("${careme.chat.max-conversations:1000}") int maxConversations,
            @Value("${careme.chat.ttl:PT2H}") Duration ttl) {
        this.maxConversations = maxConversations;
        this.ttl = ttl;
    }

    public synchronized State getOrCreate(String conversationId) {
        evictExpired();
        State state = states.computeIfAbsent(conversationId, ignored -> new State());
        state.touchedAt = Instant.now();
        while (states.size() > maxConversations) {
            states.remove(states.keySet().iterator().next());
        }
        return state;
    }

    public synchronized Optional<ChatMessageResponse> outcome(String conversationId, String messageId) {
        State state = states.get(conversationId);
        if (state == null || state.outcomes.get(messageId) == null) {
            return Optional.empty();
        }
        state.touchedAt = Instant.now();
        return Optional.of(state.outcomes.get(messageId));
    }

    private void evictExpired() {
        Instant cutoff = Instant.now().minus(ttl);
        states.entrySet().removeIf(entry -> entry.getValue().touchedAt.isBefore(cutoff));
    }

    public static final class State {
        private Instant touchedAt = Instant.now();
        private String pendingMessage;
        private final Map<String, ChatMessageResponse> outcomes = new LinkedHashMap<>();

        public String pendingMessage() {
            return pendingMessage;
        }

        public void pendingMessage(String pendingMessage) {
            this.pendingMessage = pendingMessage;
        }

        public void clearPendingMessage() {
            pendingMessage = null;
        }

        public void remember(String messageId, ChatMessageResponse response) {
            outcomes.put(messageId, response);
        }
    }
}