package com.careme.backend.service;

import java.util.List;

import com.careme.backend.dto.ChatMessageResponse;

/**
 * Observable outcome of answering a question about the person's measurements.
 *
 * <p>An absence is never a failure: a retrieval that could not complete is reported
 * as {@link Kind#FAILURE}, so an absence is declared only when it was verified.
 */
public record MeasurementAnswerResult(
        Kind kind,
        List<MeasurementFact> facts,
        String message,
        ChatMessageResponse.AbsenceReason absenceReason,
        List<ChatMessageResponse.SuggestedAction> suggestedActions) {

    public MeasurementAnswerResult {
        facts = facts == null ? List.of() : List.copyOf(facts);
        suggestedActions = suggestedActions == null ? List.of() : List.copyOf(suggestedActions);
    }

    public enum Kind {
        /** The question was answered with measurements of the tracking. */
        ANSWERED,
        /** The retrieval completed and no measurement of the questioned metric is on record. */
        NO_RECORDS,
        /** The question did not specify which metric, so the system asks before answering. */
        CLARIFICATION_REQUIRED,
        /** The retrieval could not complete. It never stands for an absence. */
        FAILURE
    }

    /** An answer grounded in retrieved measurements. It never reports an absence. */
    public static MeasurementAnswerResult answered(List<MeasurementFact> facts, String message) {
        return new MeasurementAnswerResult(Kind.ANSWERED, facts, message, null, List.of());
    }

    /**
     * The absence of any measurement of the questioned metric, with the scope that
     * applies and the continuation offered to the person.
     */
    public static MeasurementAnswerResult noRecords(
            ChatMessageResponse.AbsenceReason absenceReason,
            List<ChatMessageResponse.SuggestedAction> suggestedActions,
            String message) {
        return new MeasurementAnswerResult(
                Kind.NO_RECORDS, List.of(), message, absenceReason, suggestedActions);
    }

    /** The question did not specify which metric: the system asks instead of assuming. */
    public static MeasurementAnswerResult askForMetric(String message) {
        return new MeasurementAnswerResult(Kind.CLARIFICATION_REQUIRED, List.of(), message, null, List.of());
    }

    /** A recoverable failure. It never reports an absence that could not be verified. */
    public static MeasurementAnswerResult failure(String message) {
        return new MeasurementAnswerResult(Kind.FAILURE, List.of(), message, null, List.of());
    }
}
