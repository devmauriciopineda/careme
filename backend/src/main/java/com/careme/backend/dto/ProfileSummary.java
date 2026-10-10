package com.careme.backend.dto;

import com.careme.backend.entity.PatientProfile;
import java.util.List;

/**
 * The current patient profile, as the conversation reports it.
 *
 * <p>It carries one entry per profile field, in a fixed order, with the current
 * confirmed value or a null value when the field is absent. It never carries the
 * word the person used: the label the interface shows belongs to the interface,
 * so no user-facing wording is fixed here. It also carries no provenance and no
 * previous value: the profile is a current-value snapshot.
 */
public record ProfileSummary(List<Field> fields) {

    public ProfileSummary {
        fields = fields == null ? List.of() : List.copyOf(fields);
    }

    /** One profile field: its stable key and its current value, absent when null. */
    public record Field(String key, String value) {
    }

    /** The snapshot of a profile, or {@code null} when there is nothing to report. */
    public static ProfileSummary of(PatientProfile profile) {
        if (profile == null) {
            return null;
        }
        return new ProfileSummary(List.of(
                new Field("name", profile.name()),
                new Field("date_of_birth", profile.dateOfBirth() == null ? null : profile.dateOfBirth().toString()),
                new Field("sex", profile.sex()),
                new Field("occupation", profile.occupation()),
                new Field("blood_group", profile.bloodGroup()),
                new Field("rh", profile.rh()),
                new Field("height_cm", profile.heightCm() == null ? null : profile.heightCm().toPlainString())));
    }
}
