package com.careme.backend.dto;

import com.careme.backend.entity.ClinicalEvent;
import java.time.LocalDate;
import java.util.List;

/** Provider-neutral structured result produced by the chat integration. */
public record ClinicalEventIntent(
        Kind kind,
        List<Candidate> events,
        String clarification,
        Query query) {

    /**
     * Compatibility constructor for the intents that carry no query, so the
     * existing registration, clarification and conversation call sites keep
     * working unchanged.
     */
    public ClinicalEventIntent(Kind kind, List<Candidate> events, String clarification) {
        this(kind, events, clarification, null);
    }

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
        if (kind == Kind.QUERY && !events.isEmpty()) {
            throw new IllegalArgumentException("Query intent must not contain event candidates");
        }
        if (kind == Kind.QUERY && query == null) {
            throw new IllegalArgumentException("Query intent must contain a query");
        }
        if (kind != Kind.QUERY && query != null) {
            throw new IllegalArgumentException("Only query intent may contain a query");
        }
    }

    public static ClinicalEventIntent conversation() {
        return new ClinicalEventIntent(Kind.CONVERSATION, List.of(), null, null);
    }

    public static ClinicalEventIntent clarification(String message) {
        return new ClinicalEventIntent(Kind.CLARIFICATION, List.of(), message, null);
    }

    public static ClinicalEventIntent query(Query query) {
        return new ClinicalEventIntent(Kind.QUERY, List.of(), null, query);
    }

    public enum Kind {
        EVENTS,
        QUERY,
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

    /**
     * A question about the patient's own clinical history and the criteria the
     * backend uses to retrieve the events that can answer it. The scope decides
     * the destination: the clinical history or the measurement tracking space.
     */
    public record Query(
            String question,
            Scope scope,
            List<String> searchTerms,
            ClinicalEvent.ClinicalEventType type,
            LocalDate fromDate,
            LocalDate toDate) {

        public Query {
            searchTerms = searchTerms == null ? List.of() : List.copyOf(searchTerms);
        }

        public enum Scope {
            HISTORY,
            MEASUREMENTS
        }
    }
}
