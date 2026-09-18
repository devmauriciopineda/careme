package com.careme.backend.exception;

/** A client supplied clinical-event inspection filter is invalid. */
public class ClinicalEventInspectionException extends RuntimeException {

    public ClinicalEventInspectionException(String message) {
        super(message);
    }
}