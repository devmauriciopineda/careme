package com.careme.backend.exception;

/** Raised when a tracking endpoint names an unknown or unadmitted metric. */
public class MetricNotFoundException extends RuntimeException {

    public MetricNotFoundException(String code) {
        super("Metric is not available for tracking: " + code);
    }
}