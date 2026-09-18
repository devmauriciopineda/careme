package com.careme.backend.dto;

import java.time.LocalDate;
import java.util.UUID;

/** Read-only clinical event representation used by the inspection view. */
public record ClinicalEventInspectionResponse(
        UUID id,
        String code,
        String type,
        String content,
        LocalDate occurrenceDate,
        String occurrenceDatePrecision,
        LocalDate recordDate) {
}