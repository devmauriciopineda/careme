package com.careme.backend.dto;

import com.careme.backend.entity.ClinicalEvent;
import java.util.List;

public record ChatMessageResponse(
        String conversationId,
        String messageId,
        Status status,
        String message,
        List<EventSummary> events,
        AbsenceReason absenceReason,
        List<SuggestedAction> suggestedActions,
        String generalReply) {

    public ChatMessageResponse {
        events = events == null ? List.of() : List.copyOf(events);
        suggestedActions = suggestedActions == null ? List.of() : List.copyOf(suggestedActions);
        generalReply = generalReply == null || generalReply.isBlank() ? null : generalReply;
    }

    /** Outcome that carries neither absence detail nor a general part. */
    public ChatMessageResponse(
            String conversationId,
            String messageId,
            Status status,
            String message,
            List<EventSummary> events) {
        this(conversationId, messageId, status, message, events, null, null, null);
    }

    /** Outcome that carries an absence but no general part. */
    public ChatMessageResponse(
            String conversationId,
            String messageId,
            Status status,
            String message,
            List<EventSummary> events,
            AbsenceReason absenceReason,
            List<SuggestedAction> suggestedActions) {
        this(conversationId, messageId, status, message, events, absenceReason, suggestedActions, null);
    }

    public static ChatMessageResponse of(
            String conversationId,
            String messageId,
            Status status,
            String message,
            List<ClinicalEvent> events) {
        return new ChatMessageResponse(
                conversationId,
                messageId,
                status,
                message,
                events == null ? List.of() : events.stream().map(EventSummary::from).toList(),
                null,
                null,
                null);
    }

    /**
     * Outcome that reports why no records were found and what the user can do
     * next, so the client presents both without reading the Spanish message.
     */
    public static ChatMessageResponse of(
            String conversationId,
            String messageId,
            Status status,
            String message,
            List<ClinicalEvent> events,
            AbsenceReason absenceReason,
            List<SuggestedAction> suggestedActions) {
        return of(conversationId, messageId, status, message, events, absenceReason, suggestedActions, null);
    }

    /**
     * Outcome of a message that also carried a general part, which travels apart
     * from the clinical result so a client presents the two as different kinds of
     * answer without reading either Spanish text.
     */
    public static ChatMessageResponse of(
            String conversationId,
            String messageId,
            Status status,
            String message,
            List<ClinicalEvent> events,
            AbsenceReason absenceReason,
            List<SuggestedAction> suggestedActions,
            String generalReply) {
        return new ChatMessageResponse(
                conversationId,
                messageId,
                status,
                message,
                events == null ? List.of() : events.stream().map(EventSummary::from).toList(),
                absenceReason,
                suggestedActions,
                generalReply);
    }

    public enum Status {
        REGISTERED("registered"),
        ANSWERED("answered"),
        NO_RECORDS("no_records"),
        CLARIFICATION_REQUIRED("clarification_required"),
        GENERAL_CONVERSATION("general_conversation"),
        DUPLICATE("duplicate"),
        FAILED("failed");

        private final String value;

        Status(String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue
        public String value() {
            return value;
        }
    }

    public record EventSummary(String code, String type, String date, String datePrecision, String content) {
        static EventSummary from(ClinicalEvent event) {
            return new EventSummary(
                    event.code(),
                    event.type().name().toLowerCase(),
                    event.date() == null ? null : event.date().toString(),
                    event.datePrecision().name().toLowerCase(),
                    event.content());
        }
    }

    /**
     * Why the history could not answer a question. It is reported only with the
     * `no_records` status, and it names the scope of the absence so a client never
     * presents a scoped absence as an empty history.
     */
    public enum AbsenceReason {
        /** The history holds no registered events at all. */
        EMPTY_HISTORY("empty_history"),
        /** The history holds events, but none of the questioned type. */
        NO_EVENTS_OF_TYPE("no_events_of_type"),
        /** The history holds events, but none inside the questioned period. */
        NO_EVENTS_IN_PERIOD("no_events_in_period"),
        /** The history holds events, but none matched the terms the question used. */
        NO_TERM_MATCH("no_term_match");

        private final String value;

        AbsenceReason(String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue
        public String value() {
            return value;
        }
    }

    /** What the user is offered after an absence, so the outcome stays actionable. */
    public enum SuggestedAction {
        /** Ask the same thing in other words. */
        REFORMULATE("reformulate"),
        /** State the fact so it becomes a registered event. */
        REGISTER("register");

        private final String value;

        SuggestedAction(String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue
        public String value() {
            return value;
        }
    }
}