package com.careme.backend.dto;

import com.careme.backend.entity.ClinicalEvent;
import java.util.List;

public record ChatMessageResponse(
        String conversationId,
        String messageId,
        Status status,
        String message,
        List<EventSummary> events) {

    public ChatMessageResponse {
        events = events == null ? List.of() : List.copyOf(events);
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
                events == null ? List.of() : events.stream().map(EventSummary::from).toList());
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
}