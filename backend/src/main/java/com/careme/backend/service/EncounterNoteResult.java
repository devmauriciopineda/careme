package com.careme.backend.service;

import com.careme.backend.entity.EncounterNote;

/** The outcome of collecting one fact in the notes of an open consultation. */
public record EncounterNoteResult(Kind kind, EncounterNote note, String message) {

    public enum Kind {
        /** The fact was collected in the consultation's notes. */
        COLLECTED,
        /** The consultation already held that same fact. */
        DUPLICATE,
        /** The fact could not be collected. */
        FAILURE
    }
}
