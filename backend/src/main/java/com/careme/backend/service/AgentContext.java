package com.careme.backend.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/** What one turn needs to know about the person, the day and the active conversation. */
public record AgentContext(
        String conversationId,
        LocalDate referenceDate,
        ZoneId zoneId,
        List<String> recentTurns,
        com.careme.backend.entity.PatientProfile profile) {

    public AgentContext(
            String conversationId, LocalDate referenceDate, ZoneId zoneId, List<String> recentTurns) {
        this(conversationId, referenceDate, zoneId, recentTurns,
                com.careme.backend.entity.PatientProfile.empty());
    }

    public AgentContext {
        recentTurns = recentTurns == null ? List.of() : List.copyOf(recentTurns);
        profile = profile == null ? com.careme.backend.entity.PatientProfile.empty() : profile;
    }
}
