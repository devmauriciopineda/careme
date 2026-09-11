package com.careme.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A measurement read from an imported file, together with what loading it would
 * do. Nothing is stored while building a preview.
 *
 * @param replacesExisting {@code true} when that day already had a measurement,
 *                         so the load would replace its values instead of
 *                         creating a new record
 */
public record ImportPreviewRow(
        LocalDate date,
        BigDecimal weightKg,
        BigDecimal waistCm,
        boolean replacesExisting) {
}
