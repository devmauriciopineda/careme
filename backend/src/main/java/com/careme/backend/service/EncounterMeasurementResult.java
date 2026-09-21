package com.careme.backend.service;

import com.careme.backend.entity.EncounterMeasurementNote;

/** The outcome of collecting one measurement in the notes of an open consultation. */
public record EncounterMeasurementResult(Kind kind, EncounterMeasurementNote note, String message) {

    public enum Kind {
        /** The measurement was collected in the consultation's notes. */
        COLLECTED,
        /** The consultation already held that same measurement. */
        DUPLICATE,
        /** The measurement could not be collected. */
        FAILURE
    }
}
