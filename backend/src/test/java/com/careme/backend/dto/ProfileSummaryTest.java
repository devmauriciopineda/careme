package com.careme.backend.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.careme.backend.entity.PatientProfile;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ProfileSummaryTest {

    private static final java.util.List<String> KEYS = java.util.List.of(
            "name", "date_of_birth", "sex", "occupation", "blood_group", "rh", "height_cm");

    @Test
    void reportsNothingWhenThereIsNoProfileToReport() {
        assertThat(ProfileSummary.of(null)).isNull();
    }

    @Test
    void reportsEveryFieldAsAbsentForAnEmptyProfile() {
        ProfileSummary summary = ProfileSummary.of(PatientProfile.empty());

        assertThat(summary.fields()).extracting(ProfileSummary.Field::key).containsExactlyElementsOf(KEYS);
        assertThat(summary.fields()).allSatisfy(field -> assertThat(field.value()).isNull());
    }

    @Test
    void reportsTheConfirmedValuesAndKeepsTheUnconfirmedOnesAbsent() {
        ProfileSummary summary = ProfileSummary.of(new PatientProfile(
                " Ana ", LocalDate.of(1984, 2, 3), "mujer", null, "O", "positivo", new BigDecimal("170.5")));

        assertThat(summary.fields())
                .filteredOn(field -> field.value() != null)
                .extracting(ProfileSummary.Field::key, ProfileSummary.Field::value)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("name", "Ana"),
                        org.assertj.core.groups.Tuple.tuple("date_of_birth", "1984-02-03"),
                        org.assertj.core.groups.Tuple.tuple("sex", "mujer"),
                        org.assertj.core.groups.Tuple.tuple("blood_group", "O"),
                        org.assertj.core.groups.Tuple.tuple("rh", "positivo"),
                        org.assertj.core.groups.Tuple.tuple("height_cm", "170.5"));
        assertThat(summary.fields())
                .filteredOn(field -> field.value() == null)
                .extracting(ProfileSummary.Field::key)
                .containsExactly("occupation");
    }

    @Test
    void treatsAnAbsentFieldListAsAnEmptyOne() {
        assertThat(new ProfileSummary(null).fields()).isEmpty();
    }
}
