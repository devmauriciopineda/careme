package com.careme.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * API representation of a body measurement.
 *
 * <p>Kept separate from {@link com.careme.backend.entity.Measurement} so the
 * HTTP contract does not change when the entity gains persistence concerns.
 */
public record MeasurementResponse(UUID id, LocalDate date, BigDecimal weightKg, BigDecimal waistCm) {
}
