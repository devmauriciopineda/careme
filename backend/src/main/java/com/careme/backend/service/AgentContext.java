package com.careme.backend.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/** What one turn needs to know about the person, the day and the active conversation. */
public record AgentContext(
        String conversationId, LocalDate referenceDate, ZoneId zoneId, List<String> recentTurns) {

    public AgentContext {
        recentTurns = recentTurns == null ? List.of() : List.copyOf(recentTurns);
    }
}
