package com.careme.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** One stored measurement with all component values in the reference unit. */
public record TrackingMeasurementResponse(
        UUID id,
        LocalDate date,
        List<Value> values) {

    public TrackingMeasurementResponse {
        values = values == null ? List.of() : List.copyOf(values);
    }

    public record Value(String component, BigDecimal value) {
    }
}