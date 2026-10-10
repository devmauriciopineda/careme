package com.careme.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

/** The current personal profile of the single patient. */
public record PatientProfile(
        String name,
        LocalDate dateOfBirth,
        String sex,
        String occupation,
        String bloodGroup,
        String rh,
        BigDecimal heightCm) {

    public PatientProfile {
        name = clean(name);
        sex = clean(sex);
        occupation = clean(occupation);
        bloodGroup = clean(bloodGroup);
        rh = clean(rh);
        if (dateOfBirth != null && dateOfBirth.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date of birth must not be in the future");
        }
        if (heightCm != null && (heightCm.signum() <= 0 || heightCm.compareTo(new BigDecimal("300")) > 0)) {
            throw new IllegalArgumentException("Height must be between 0 and 300 centimetres");
        }
    }

    public static PatientProfile empty() {
        return new PatientProfile(null, null, null, null, null, null, null);
    }

    public PatientProfile apply(PatientProfileUpdate update) {
        if (update == null || !update.confirmed()) {
            return this;
        }
        return new PatientProfile(
                update.name() == null ? name : update.name(),
                update.dateOfBirth() == null ? dateOfBirth : update.dateOfBirth(),
                update.sex() == null ? sex : update.sex(),
                update.occupation() == null ? occupation : update.occupation(),
                update.bloodGroup() == null ? bloodGroup : update.bloodGroup(),
                update.rh() == null ? rh : update.rh(),
                update.heightCm() == null ? heightCm : update.heightCm());
    }

    public Integer ageOn(LocalDate date) {
        return dateOfBirth == null || date == null ? null : Period.between(dateOfBirth, date).getYears();
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}