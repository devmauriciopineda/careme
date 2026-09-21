package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.careme.backend.entity.ClinicalEvent;
import com.careme.backend.entity.Encounter;
import com.careme.backend.entity.EncounterMeasurementNote;
import com.careme.backend.entity.EncounterNote;

/**
 * Verifies that a consultation keeps the measurements the conversation collected:
 * metric, values in the reference unit, the unit the person stated and the date with
 * its precision survive the Markdown round trip alongside the clinical-fact notes.
 */
class EncounterMeasurementNoteRoundTripTest {

    private static final String CREATED_AT = "2026-09-20T10:15:30-05:00";

    private static Encounter encounterWithMeasurementNotes() {
        return Encounter.opened(
                        UUID.fromString("6f1d0f9a-1f5f-4a0e-9a4e-4f0f6a1c2b3d"),
                        "enc_007",
                        "conversation-1",
                        OffsetDateTime.parse(CREATED_AT))
                .withNote(new EncounterNote(
                        ClinicalEvent.ClinicalEventType.NOTE,
                        "me duele la espalda",
                        null,
                        ClinicalEvent.DatePrecision.UNKNOWN,
                        null))
                .withMeasurementNote(new EncounterMeasurementNote(
                        "weight",
                        Map.of("value", new BigDecimal("70.0000")),
                        "lb",
                        LocalDate.of(2026, 9, 19),
                        ClinicalEvent.DatePrecision.EXACT,
                        "ayer"))
                .withMeasurementNote(new EncounterMeasurementNote(
                        "blood_pressure",
                        Map.of("systolic", new BigDecimal("145"), "diastolic", new BigDecimal("92")),
                        null,
                        null,
                        ClinicalEvent.DatePrecision.APPROXIMATE,
                        "el mes pasado"));
    }

    @Test
    void keepsEveryMeasurementNoteAcrossTheRoundTrip() {
        Encounter restored = EncounterMarkdownStore.deserialize(
                EncounterMarkdownStore.serialize(encounterWithMeasurementNotes()));

        assertThat(restored.measurementNotes()).hasSize(2);
        assertThat(restored.measurementNotes())
                .extracting(EncounterMeasurementNote::metricCode)
                .containsExactly("weight", "blood_pressure");
    }

    @Test
    void keepsTheMetricTheValuesAndTheUnitThePersonStated() {
        Encounter restored = EncounterMarkdownStore.deserialize(
                EncounterMarkdownStore.serialize(encounterWithMeasurementNotes()));

        EncounterMeasurementNote weight = restored.measurementNotes().getFirst();
        assertThat(weight.values()).containsEntry("value", new BigDecimal("70.0000"));
        assertThat(weight.declaredUnit()).isEqualTo("lb");
    }

    @Test
    void keepsBothValuesOfACompositeMetricAsOneNote() {
        Encounter restored = EncounterMarkdownStore.deserialize(
                EncounterMarkdownStore.serialize(encounterWithMeasurementNotes()));

        EncounterMeasurementNote bloodPressure = restored.measurementNotes().get(1);
        assertThat(bloodPressure.values())
                .containsEntry("systolic", new BigDecimal("145"))
                .containsEntry("diastolic", new BigDecimal("92"));
    }

    @Test
    void keepsTheDateWithItsPrecisionAndTheOriginalExpression() {
        Encounter restored = EncounterMarkdownStore.deserialize(
                EncounterMarkdownStore.serialize(encounterWithMeasurementNotes()));

        EncounterMeasurementNote weight = restored.measurementNotes().getFirst();
        assertThat(weight.date()).isEqualTo(LocalDate.of(2026, 9, 19));
        assertThat(weight.datePrecision()).isEqualTo(ClinicalEvent.DatePrecision.EXACT);
        assertThat(weight.dateText()).isEqualTo("ayer");

        EncounterMeasurementNote bloodPressure = restored.measurementNotes().get(1);
        assertThat(bloodPressure.date()).isNull();
        assertThat(bloodPressure.datePrecision()).isEqualTo(ClinicalEvent.DatePrecision.APPROXIMATE);
        assertThat(bloodPressure.dateText()).isEqualTo("el mes pasado");
    }

    @Test
    void keepsTheClinicalFactNotesAlongsideTheMeasurementOnes() {
        Encounter restored = EncounterMarkdownStore.deserialize(
                EncounterMarkdownStore.serialize(encounterWithMeasurementNotes()));

        assertThat(restored.notes()).hasSize(1);
        assertThat(restored.notes().getFirst().content()).isEqualTo("me duele la espalda");
        assertThat(restored.notes().getFirst().type()).isEqualTo(ClinicalEvent.ClinicalEventType.NOTE);
    }

    @Test
    void keepsAConsultationWithoutMeasurementNotesUnchanged() {
        Encounter opened = Encounter.opened(
                UUID.randomUUID(), "enc_008", "conversation-2", OffsetDateTime.parse(CREATED_AT));

        Encounter restored = EncounterMarkdownStore.deserialize(EncounterMarkdownStore.serialize(opened));

        assertThat(restored.measurementNotes()).isEmpty();
    }
}
