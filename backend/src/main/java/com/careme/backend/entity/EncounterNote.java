package com.careme.backend.entity;

import java.time.LocalDate;

/**
 * A clinical fact the person mentioned during a consultation, kept as a note.
 *
 * <p>A note is not a registered fact: it carries the temporal fidelity the
 * person gave so the close can register it, but it never reaches the clinical
 * history on its own and never carries an event code.
 */
public record EncounterNote(
        ClinicalEvent.ClinicalEventType type,
        String content,
        LocalDate date,
        ClinicalEvent.DatePrecision datePrecision,
        String dateText) {

    public EncounterNote {
        if (type == null) {
            throw new IllegalArgumentException("Encounter note type must not be null");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Encounter note content must not be blank");
        }
        if (datePrecision == null) {
            throw new IllegalArgumentException("Encounter note date precision must not be null");
        }
        if (datePrecision == ClinicalEvent.DatePrecision.EXACT && date == null) {
            throw new IllegalArgumentException("Exact encounter note date must not be null");
        }
    }
}
