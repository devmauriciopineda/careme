package com.careme.backend.service;

import java.util.List;

import com.careme.backend.repository.MetricMeasurementOutcome;

/**
 * The outcome of registering the measurements a consultation collected.
 *
 * @param kind     what happened to the batch
 * @param outcomes one entry per stored measurement, each saying whether it replaced
 *                 the value of a metric and day that already had one
 * @param message  what to tell the person, when there is something to tell
 */
public record MeasurementRegistrationResult(
        Kind kind, List<MetricMeasurementOutcome> outcomes, String message) {

    public enum Kind {
        /** The batch was stored. */
        REGISTERED,
        /** The consultation did not collect any admissible measurement. */
        NOTHING,
        /** The batch could not be stored, and the tracking is exactly as it was. */
        FAILURE
    }

    /** @return how many measurements the batch stored */
    public int registered() {
        return outcomes.size();
    }

    /** @return how many of them replaced the value of a metric and day that already had one */
    public long replaced() {
        return outcomes.stream().filter(MetricMeasurementOutcome::replaced).count();
    }
}
