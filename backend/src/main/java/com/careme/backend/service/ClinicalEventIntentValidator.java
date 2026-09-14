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
        } else if (intent.kind() == ClinicalEventIntent.Kind.QUERY) {
            if (!intent.events().isEmpty()) {
                throw new IllegalArgumentException("Query intent must not contain events");
            }
            validateQuery(intent.query());
        } else if (!intent.events().isEmpty()) {
            throw new IllegalArgumentException("Non-event intent must not contain events");
        }
    }

    private void validateQuery(ClinicalEventIntent.Query query) {
        if (query == null) {
            throw new IllegalArgumentException("Query intent must contain a query");
        }
        if (query.question() == null || query.question().isBlank()) {
            throw new IllegalArgumentException("Query question must not be blank");
        }
        if (query.scope() == null) {
            throw new IllegalArgumentException("Query scope must not be null");
        }
        boolean hasSearchTerm = query.searchTerms().stream().anyMatch(term -> term != null && !term.isBlank());
        boolean hasFilter = query.type() != null || query.fromDate() != null || query.toDate() != null;
        if (!hasSearchTerm && !hasFilter) {
            throw new IllegalArgumentException("Query must contain at least one search term or filter");
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
