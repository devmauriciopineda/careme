package com.careme.backend.service;

import com.careme.backend.entity.ClinicalEvent;
import java.util.List;

public record ClinicalEventRegistrationResult(
        Kind kind,
        List<ClinicalEvent> events,
        String message) {

    public ClinicalEventRegistrationResult {
        events = events == null ? List.of() : List.copyOf(events);
    }

    public enum Kind {
        REGISTERED,
        DUPLICATE,
        CLARIFICATION,
        CONVERSATION,
        FAILURE
    }
}
