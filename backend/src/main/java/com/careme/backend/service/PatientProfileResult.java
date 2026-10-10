package com.careme.backend.service;

import com.careme.backend.entity.PatientProfileUpdate;

/** Outcome of collecting one patient-profile update in an open consultation. */
public record PatientProfileResult(Kind kind, PatientProfileUpdate update, String message) {

    public enum Kind {
        COLLECTED,
        DUPLICATE,
        FAILURE
    }
}