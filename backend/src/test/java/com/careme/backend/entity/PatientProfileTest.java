package com.careme.backend.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class PatientProfileTest {

    @Test
    void startsEmptyWithEveryFieldAbsent() {
        assertThat(PatientProfile.empty())
                .isEqualTo(new PatientProfile(null, null, null, null, null, null, null));
    }

    @Test
    void normalizesBlankTextToAbsenceAndTrimsTheRest() {
        PatientProfile profile = new PatientProfile(
                "  ", null, "   ", "", "O", "  positivo  ", null);

        assertThat(profile.name()).isNull();
        assertThat(profile.sex()).isNull();
        assertThat(profile.occupation()).isNull();
        assertThat(profile.rh()).isEqualTo("positivo");
    }

    @Test
    void rejectsABirthDateInTheFuture() {
        assertThatThrownBy(() -> new PatientProfile(
                null, LocalDate.now().plusDays(1), null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("future");
    }

    @Test
    void acceptsTodayAsABirthDate() {
        assertThat(new PatientProfile(null, LocalDate.now(), null, null, null, null, null).dateOfBirth())
                .isEqualTo(LocalDate.now());
    }

    @Test
    void rejectsAHeightOutsideTheAdmissibleRange() {
        assertThatThrownBy(() -> new PatientProfile(
                null, null, null, null, null, null, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PatientProfile(
                null, null, null, null, null, null, new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PatientProfile(
                null, null, null, null, null, null, new BigDecimal("301")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(new PatientProfile(null, null, null, null, null, null, new BigDecimal("300")).heightCm())
                .isEqualByComparingTo("300");
    }

    @Test
    void ignoresAnAbsentOrUnconfirmedUpdate() {
        PatientProfile current = new PatientProfile(
                "Ana", LocalDate.of(1984, 2, 3), null, null, null, null, null);

        assertThat(current.apply(null)).isEqualTo(current);
        assertThat(current.apply(new PatientProfileUpdate(
                "Bea", null, null, null, null, null, null, false))).isEqualTo(current);
    }

    @Test
    void replacesOnlyTheFieldsTheUpdateCarries() {
        PatientProfile current = new PatientProfile(
                "Ana", LocalDate.of(1984, 2, 3), "mujer", "Médica", "O", "positivo", new BigDecimal("170"));

        PatientProfile updated = current.apply(new PatientProfileUpdate(
                "Bea", null, null, null, null, null, new BigDecimal("171"), true));

        assertThat(updated).isEqualTo(new PatientProfile(
                "Bea", LocalDate.of(1984, 2, 3), "mujer", "Médica", "O", "positivo", new BigDecimal("171")));
        assertThat(current.name()).isEqualTo("Ana");
    }

    @Test
    void derivesNoAgeWithoutADateOfBirthOrAReferenceDate() {
        PatientProfile withBirthDate = new PatientProfile(
                null, LocalDate.of(1984, 2, 3), null, null, null, null, null);

        assertThat(withBirthDate.ageOn(null)).isNull();
        assertThat(PatientProfile.empty().ageOn(LocalDate.of(2026, 9, 30))).isNull();
        assertThat(withBirthDate.ageOn(LocalDate.of(2026, 2, 2))).isEqualTo(41);
    }

    @Test
    void treatsAnUpdateWithNoValuesAsNoCandidate() {
        PatientProfileUpdate update =
                new PatientProfileUpdate(null, null, null, null, null, null, null, true);

        assertThat(update.confirmed()).isTrue();
        assertThat(PatientProfile.empty().apply(update)).isEqualTo(PatientProfile.empty());
    }
}
