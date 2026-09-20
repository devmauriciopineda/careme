package com.careme.backend.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ClinicalEventTest {

    private static final UUID VALID_ID = UUID.fromString("6f1d0f9a-1f5f-4a0e-9a4e-4f0f6a1c2b3d");
    private static final OffsetDateTime CREATED_AT = OffsetDateTime.parse("2026-09-13T10:15:30Z");

    @Test
    void acceptsAnExactPatientEvent() {
        ClinicalEvent event = new ClinicalEvent(
                VALID_ID,
                "evt_001",
                ClinicalEvent.ClinicalEventType.DIAGNOSIS,
                LocalDate.of(2026, 8, 10),
                ClinicalEvent.DatePrecision.EXACT,
                "el 10 de agosto",
                "Me diagnosticaron hipertension",
                ClinicalEvent.EventSource.PATIENT,
                CREATED_AT,
                null);

        assertThat(event.code()).isEqualTo("evt_001");
        assertThat(event.datePrecision()).isEqualTo(ClinicalEvent.DatePrecision.EXACT);
    }

    @Test
    void acceptsAnApproximateEventWithoutInventedDate() {
        ClinicalEvent event = new ClinicalEvent(
                VALID_ID,
                "evt_002",
                ClinicalEvent.ClinicalEventType.NOTE,
                null,
                ClinicalEvent.DatePrecision.APPROXIMATE,
                "hace unos dos anos",
                "Tuve fiebre",
                ClinicalEvent.EventSource.PATIENT,
                CREATED_AT,
                null);

        assertThat(event.date()).isNull();
        assertThat(event.dateText()).isEqualTo("hace unos dos anos");
    }

    @Test
    void rejectsInvalidValues() {
        assertThatThrownBy(() -> new ClinicalEvent(
                VALID_ID, "event_001", ClinicalEvent.ClinicalEventType.NOTE, null,
                ClinicalEvent.DatePrecision.UNKNOWN, null, "Hecho", ClinicalEvent.EventSource.PATIENT, CREATED_AT, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("code");

        assertThatThrownBy(() -> new ClinicalEvent(
                VALID_ID, "evt_003", ClinicalEvent.ClinicalEventType.NOTE, null,
                ClinicalEvent.DatePrecision.EXACT, null, "Hecho", ClinicalEvent.EventSource.PATIENT, CREATED_AT, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("date");

        assertThatThrownBy(() -> new ClinicalEvent(
                VALID_ID, "evt_004", ClinicalEvent.ClinicalEventType.NOTE, null,
                ClinicalEvent.DatePrecision.UNKNOWN, null, " ", ClinicalEvent.EventSource.PATIENT, CREATED_AT, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("content");
    }

    @Test
    void rejectsMissingRequiredIdentityTypeSourceAndCreationTime() {
        assertThatThrownBy(() -> event(null, "evt_005", ClinicalEvent.ClinicalEventType.NOTE,
                ClinicalEvent.DatePrecision.UNKNOWN, "content", ClinicalEvent.EventSource.PATIENT, CREATED_AT))
                .hasMessageContaining("id");
        assertThatThrownBy(() -> event(VALID_ID, "evt_005", null,
                ClinicalEvent.DatePrecision.UNKNOWN, "content", ClinicalEvent.EventSource.PATIENT, CREATED_AT))
                .hasMessageContaining("type");
        assertThatThrownBy(() -> event(VALID_ID, "evt_006", ClinicalEvent.ClinicalEventType.NOTE,
                null, "content", ClinicalEvent.EventSource.PATIENT, CREATED_AT))
                .hasMessageContaining("precision");
        assertThatThrownBy(() -> event(VALID_ID, "evt_007", ClinicalEvent.ClinicalEventType.NOTE,
                ClinicalEvent.DatePrecision.UNKNOWN, "content", null, CREATED_AT))
                .hasMessageContaining("source");
        assertThatThrownBy(() -> event(VALID_ID, "evt_008", ClinicalEvent.ClinicalEventType.NOTE,
                ClinicalEvent.DatePrecision.UNKNOWN, "content", ClinicalEvent.EventSource.PATIENT, null))
                .hasMessageContaining("creation");
    }

    private static ClinicalEvent event(UUID id, String code, ClinicalEvent.ClinicalEventType type,
            ClinicalEvent.DatePrecision precision, String content, ClinicalEvent.EventSource source,
            OffsetDateTime createdAt) {
        return new ClinicalEvent(id, code, type, null, precision, null, content, source, createdAt, null);
    }

    @Test
    void rejectsAProvenanceThatIsNotAConsultationCode() {
        assertThatThrownBy(() -> new ClinicalEvent(
                VALID_ID, "evt_009", ClinicalEvent.ClinicalEventType.NOTE, null,
                ClinicalEvent.DatePrecision.UNKNOWN, null, "Hecho", ClinicalEvent.EventSource.PATIENT,
                CREATED_AT, "evt_001"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("encounter");
    }
}
