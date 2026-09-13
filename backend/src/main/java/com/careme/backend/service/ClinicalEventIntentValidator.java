package com.careme.backend.service;

import com.careme.backend.dto.ClinicalEventIntent;
import org.springframework.stereotype.Service;

/** Validates the provider-neutral chat contract before persistence. */
@Service
public class ClinicalEventIntentValidator {

    public void validate(ClinicalEventIntent intent) {
        if (intent == null) {
            throw new IllegalArgumentException("Clinical event intent must not be null");
        }
        if (intent.kind() == ClinicalEventIntent.Kind.EVENTS) {
            if (intent.events().isEmpty()) {
                throw new IllegalArgumentException("Events intent must contain at least one event");
            }
            intent.events().forEach(this::validateCandidate);
        } else if (!intent.events().isEmpty()) {
            throw new IllegalArgumentException("Non-event intent must not contain events");
        }
    }

    private void validateCandidate(ClinicalEventIntent.Candidate candidate) {
        if (candidate == null) {
            throw new IllegalArgumentException("Clinical event candidate must not be null");
        }
        if (candidate.type() == null) {
            throw new IllegalArgumentException("Clinical event candidate type must not be null");
        }
        if (candidate.content() == null || candidate.content().isBlank()) {
            throw new IllegalArgumentException("Clinical event candidate content must not be blank");
        }
        if (candidate.datePrecision() == null) {
            throw new IllegalArgumentException("Clinical event candidate date precision must not be null");
        }
        if (candidate.datePrecision() == com.careme.backend.entity.ClinicalEvent.DatePrecision.EXACT
                && candidate.date() == null
                && (candidate.dateText() == null || candidate.dateText().isBlank())) {
            throw new IllegalArgumentException("Exact clinical event candidate needs a date");
        }
    }
}
