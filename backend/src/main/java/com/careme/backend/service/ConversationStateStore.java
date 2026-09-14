package com.careme.backend.service;

import com.careme.backend.dto.ChatMessageResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ConversationStateStore {

    private final int maxConversations;
    private final int maxRecentTurns;
    private final Duration ttl;
    private final Map<String, State> states = new LinkedHashMap<>();

    @Autowired
    public ConversationStateStore(
            @Value("${careme.chat.max-conversations:1000}") int maxConversations,
            @Value("${careme.chat.ttl:PT2H}") Duration ttl,
            @Value("${careme.chat.max-recent-turns:6}") int maxRecentTurns) {
        this.maxConversations = maxConversations;
        this.ttl = ttl;
        this.maxRecentTurns = maxRecentTurns;
    }

    /** Convenience constructor for tests that do not exercise the turn buffer. */
    public ConversationStateStore(int maxConversations, Duration ttl) {
        this(maxConversations, ttl, 6);
    }

    public synchronized State getOrCreate(String conversationId) {
        evictExpired();
        State state = states.computeIfAbsent(conversationId, ignored -> new State(maxRecentTurns));
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
        private final Deque<Turn> recentTurns = new ArrayDeque<>();
        private final int maxRecentTurns;

        State(int maxRecentTurns) {
            this.maxRecentTurns = maxRecentTurns;
        }

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

        /**
         * Records a completed turn. The buffer is bounded and lives and dies with
         * the state, so it is only ever a short-term aid for follow-up questions.
         */
        public void addTurn(String userMessage, String assistantSummary) {
            recentTurns.addLast(new Turn(userMessage, assistantSummary));
            while (recentTurns.size() > maxRecentTurns) {
                recentTurns.removeFirst();
            }
        }

        public List<String> recentTurns() {
            return recentTurns.stream()
                    .map(turn -> "Usuario: " + turn.userMessage() + " | Asistente: " + turn.assistantSummary())
                    .toList();
        }

        public record Turn(String userMessage, String assistantSummary) {
        }
    }
}