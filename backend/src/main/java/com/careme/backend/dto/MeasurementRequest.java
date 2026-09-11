package com.careme.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

/**
 * Registration payload for a body measurement.
 *
 * <p>Bean Validation enforces the business rules that apply to the request
 * itself: both metrics are required, positive, expressed with a single decimal
 * and kept within the allowed limits, and the date is a calendar day that is not
 * in the future. The service decides whether it creates or replaces the day.
 */
public record MeasurementRequest(
        @NotNull
        @PastOrPresent
        LocalDate date,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        @DecimalMax("500.0")
        @Digits(integer = 3, fraction = 1)
        BigDecimal weightKg,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        @DecimalMax("400.0")
        @Digits(integer = 3, fraction = 1)
        BigDecimal waistCm) {
}
