package com.careme.backend.entity;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

/** A patient-reported clinical fact with preserved temporal precision. */
public record ClinicalEvent(
        UUID id,
        String code,
        ClinicalEventType type,
        LocalDate date,
        DatePrecision datePrecision,
        String dateText,
        String content,
        EventSource source,
        OffsetDateTime createdAt) {

    private static final Pattern CODE_PATTERN = Pattern.compile("evt_[0-9]{3,}");

    public ClinicalEvent {
        if (id == null) {
            throw new IllegalArgumentException("Clinical event id must not be null");
        }
        if (code == null || !CODE_PATTERN.matcher(code).matches()) {
            throw new IllegalArgumentException("Clinical event code must match evt_NNN");
        }
        if (type == null) {
            throw new IllegalArgumentException("Clinical event type must not be null");
        }
        if (datePrecision == null) {
            throw new IllegalArgumentException("Clinical event date precision must not be null");
        }
        if (datePrecision == DatePrecision.EXACT && date == null) {
            throw new IllegalArgumentException("Exact clinical event date must not be null");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Clinical event content must not be blank");
        }
        if (source == null) {
            throw new IllegalArgumentException("Clinical event source must not be null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("Clinical event creation time must not be null");
        }
    }

    public enum ClinicalEventType {
        DIAGNOSIS,
        MEDICATION,
        MEASUREMENT,
        NOTE
    }

    public enum DatePrecision {
        EXACT,
        APPROXIMATE,
        UNKNOWN
    }

    public enum EventSource {
        PATIENT
    }
}
