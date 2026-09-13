package com.careme.backend.dto;

import com.careme.backend.entity.ClinicalEvent;
import java.time.LocalDate;
import java.util.List;

/** Provider-neutral structured result produced by the chat integration. */
public record ClinicalEventIntent(
        Kind kind,
        List<Candidate> events,
        String clarification) {

    public ClinicalEventIntent {
        if (kind == null) {
            throw new IllegalArgumentException("Intent kind must not be null");
        }
        events = events == null ? List.of() : List.copyOf(events);
        if (kind == Kind.CLARIFICATION && (clarification == null || clarification.isBlank())) {
            throw new IllegalArgumentException("Clarification intent must contain a message");
        }
        if (kind != Kind.CLARIFICATION && clarification != null && !clarification.isBlank()) {
            throw new IllegalArgumentException("Only clarification intent may contain a clarification");
        }
    }

    public static ClinicalEventIntent conversation() {
        return new ClinicalEventIntent(Kind.CONVERSATION, List.of(), null);
    }

    public static ClinicalEventIntent clarification(String message) {
        return new ClinicalEventIntent(Kind.CLARIFICATION, List.of(), message);
    }

    public enum Kind {
        EVENTS,
        CLARIFICATION,
        CONVERSATION
    }

    public record Candidate(
            ClinicalEvent.ClinicalEventType type,
            String content,
            LocalDate date,
            ClinicalEvent.DatePrecision datePrecision,
            String dateText) {
    }
}
