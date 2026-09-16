package com.careme.backend.service;

import com.careme.backend.dto.ChatMessageResponse;
import com.careme.backend.entity.ClinicalEvent;
import java.util.List;

/** Observable outcome of answering a question about the clinical history. */
public record ClinicalAnswerResult(
        Kind kind,
        List<ClinicalEvent> events,
        String message,
        ChatMessageResponse.AbsenceReason absenceReason,
        List<ChatMessageResponse.SuggestedAction> suggestedActions) {

    public ClinicalAnswerResult {
        events = events == null ? List.of() : List.copyOf(events);
        suggestedActions = suggestedActions == null ? List.of() : List.copyOf(suggestedActions);
    }

    /** Answer grounded in retrieved events. It never reports an absence. */
    public static ClinicalAnswerResult answered(List<ClinicalEvent> events, String message) {
        return new ClinicalAnswerResult(Kind.ANSWERED, events, message, null, List.of());
    }

    /** Recoverable failure. It never reports an absence that could not be verified. */
    public static ClinicalAnswerResult failure(String message) {
        return new ClinicalAnswerResult(Kind.FAILURE, List.of(), message, null, List.of());
    }

    /**
     * Absence of records that support the question, with the reason that produced
     * it and the actions offered so the user can continue.
     */
    public static ClinicalAnswerResult noRecords(
            ChatMessageResponse.AbsenceReason absenceReason,
            List<ChatMessageResponse.SuggestedAction> suggestedActions,
            String message) {
        return new ClinicalAnswerResult(Kind.NO_RECORDS, List.of(), message, absenceReason, suggestedActions);
    }

    public enum Kind {
        ANSWERED,
        NO_RECORDS,
        FAILURE
    }
}
