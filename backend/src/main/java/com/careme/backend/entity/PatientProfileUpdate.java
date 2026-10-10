package com.careme.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One confirmed profile candidate collected during a consultation. */
public record PatientProfileUpdate(
        String name,
        LocalDate dateOfBirth,
        String sex,
        String occupation,
        String bloodGroup,
        String rh,
        BigDecimal heightCm,
        boolean confirmed) {
}