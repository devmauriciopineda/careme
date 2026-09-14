package com.careme.backend.service;

import com.careme.backend.entity.ClinicalEvent;
import java.util.List;

/** Observable outcome of answering a question about the clinical history. */
public record ClinicalAnswerResult(
        Kind kind,
        List<ClinicalEvent> events,
        String message) {

    public ClinicalAnswerResult {
        events = events == null ? List.of() : List.copyOf(events);
    }

    public enum Kind {
        ANSWERED,
        NO_RECORDS,
        FAILURE
    }
}
