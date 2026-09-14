package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class ConversationStateStoreTest {

    @Test
    void retainsRecentTurnsInOrder() {
        var store = new ConversationStateStore(10, Duration.ofHours(1), 3);
        var state = store.getOrCreate("conversation-1");

        state.addTurn("primera", "respuesta 1");
        state.addTurn("segunda", "respuesta 2");

        assertThat(state.recentTurns()).hasSize(2);
        assertThat(state.recentTurns().get(0)).contains("primera");
        assertThat(state.recentTurns().get(1)).contains("segunda");
    }

    @Test
    void boundsTheBufferToItsMaximumSize() {
        var store = new ConversationStateStore(10, Duration.ofHours(1), 2);
        var state = store.getOrCreate("conversation-1");

        state.addTurn("primera", "respuesta 1");
        state.addTurn("segunda", "respuesta 2");
        state.addTurn("tercera", "respuesta 3");

        assertThat(state.recentTurns()).hasSize(2);
        assertThat(state.recentTurns().get(0)).contains("segunda");
        assertThat(state.recentTurns().get(1)).contains("tercera");
    }

    @Test
    void discardsTheBufferWhenTheConversationIsEvictedByTheLimit() {
        var store = new ConversationStateStore(1, Duration.ofHours(1), 5);
        var first = store.getOrCreate("conversation-1");
        first.addTurn("primera", "respuesta 1");

        store.getOrCreate("conversation-2");

        var recreated = store.getOrCreate("conversation-1");
        assertThat(recreated).isNotSameAs(first);
        assertThat(recreated.recentTurns()).isEmpty();
    }

    @Test
    void discardsTheBufferWhenTheConversationExpires() {
        var store = new ConversationStateStore(10, Duration.ofSeconds(-1), 5);
        var first = store.getOrCreate("conversation-1");
        first.addTurn("primera", "respuesta 1");

        var recreated = store.getOrCreate("conversation-1");

        assertThat(recreated).isNotSameAs(first);
        assertThat(recreated.recentTurns()).isEmpty();
    }
}
