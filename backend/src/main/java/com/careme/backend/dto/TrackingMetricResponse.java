package com.careme.backend.dto;

import java.util.List;

/** Read model for one metric and its chronological measurements. */
public record TrackingMetricResponse(
        String code,
        String label,
        String unit,
        List<TrackingMeasurementResponse> measurements) {

    public TrackingMetricResponse {
        measurements = measurements == null ? List.of() : List.copyOf(measurements);
    }
}