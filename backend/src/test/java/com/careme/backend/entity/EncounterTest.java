package com.careme.backend.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EncounterTest {

    private static final OffsetDateTime OPENED_AT = OffsetDateTime.parse("2026-09-20T09:00:00Z");

    private static Encounter openEncounter() {
        return Encounter.opened(
                UUID.fromString("9c1f5a62-3d4b-4c7e-9f10-5a6b7c8d9e0f"), "enc_001", "conversation-1", OPENED_AT);
    }

    private static EncounterNote note(String content) {
        return new EncounterNote(
                ClinicalEvent.ClinicalEventType.NOTE,
                content,
                LocalDate.of(2026, 9, 18),
                ClinicalEvent.DatePrecision.EXACT,
                "anteayer");
    }

    @Test
    void opensInProgressWithItsOwnIdentity() {
        Encounter encounter = openEncounter();

        assertThat(encounter.status()).isEqualTo(Encounter.Status.OPEN);
        assertThat(encounter.isOpen()).isTrue();
        assertThat(encounter.closedAt()).isNull();
        assertThat(encounter.notes()).isEmpty();
    }

    @Test
    void distinguishesTwoConsultationsByTheirIdentity() {
        Encounter first = openEncounter();
        Encounter second = Encounter.opened(
                UUID.fromString("1a2b3c4d-5e6f-4a7b-8c9d-0e1f2a3b4c5d"), "enc_002", "conversation-2", OPENED_AT);

        assertThat(first.id()).isNotEqualTo(second.id());
        assertThat(first.code()).isNotEqualTo(second.code());
    }

    @Test
    void collectsNotesPreservingTheTemporalPrecision() {
        EncounterNote collected = new EncounterNote(
                ClinicalEvent.ClinicalEventType.DIAGNOSIS,
                "colesterol alto",
                LocalDate.of(2025, 3, 1),
                ClinicalEvent.DatePrecision.APPROXIMATE,
                "hace unos dos años");

        Encounter encounter = openEncounter().withNote(collected);

        assertThat(encounter.notes()).containsExactly(collected);
        assertThat(encounter.notes().getFirst().datePrecision())
                .isEqualTo(ClinicalEvent.DatePrecision.APPROXIMATE);
        assertThat(encounter.notes().getFirst().dateText()).isEqualTo("hace unos dos años");
        assertThat(encounter.isOpen()).isTrue();
    }

    @Test
    void keepsEveryCollectedNoteInOrder() {
        Encounter encounter = openEncounter().withNote(note("primera")).withNote(note("segunda"));

        assertThat(encounter.notes()).extracting(EncounterNote::content)
                .containsExactly("primera", "segunda");
    }

    @Test
    void rejectsANoteWithAnExactDateAndNoDate() {
        assertThatThrownBy(() -> new EncounterNote(
                        ClinicalEvent.ClinicalEventType.NOTE, "sin fecha", null,
                        ClinicalEvent.DatePrecision.EXACT, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsANoteWithoutContent() {
        assertThatThrownBy(() -> new EncounterNote(
                        ClinicalEvent.ClinicalEventType.NOTE, "  ", null,
                        ClinicalEvent.DatePrecision.UNKNOWN, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void closesKeepingItsMotiveItsSummaryAndItsNotes() {
        Encounter closed = openEncounter()
                .withNote(note("dolor de rodilla"))
                .closed("Dolor de rodilla", "La persona refiere dolor de rodilla desde anteayer.",
                        OffsetDateTime.parse("2026-09-20T09:40:00Z"));

        assertThat(closed.status()).isEqualTo(Encounter.Status.CLOSED);
        assertThat(closed.isOpen()).isFalse();
        assertThat(closed.closedAt()).isEqualTo(OffsetDateTime.parse("2026-09-20T09:40:00Z"));
        assertThat(closed.motive()).isEqualTo("Dolor de rodilla");
        assertThat(closed.summary()).contains("dolor de rodilla");
        assertThat(closed.notes()).hasSize(1);
    }

    @Test
    void acceptsACloseWithoutSummaryBecauseItMightNotBeStorable() {
        Encounter closed = openEncounter().closed("Consulta breve", null,
                OffsetDateTime.parse("2026-09-20T09:40:00Z"));

        assertThat(closed.summary()).isNull();
        assertThat(closed.status()).isEqualTo(Encounter.Status.CLOSED);
    }

    @Test
    void refusesToCollectNotesOrCloseAgainOnceItIsClosed() {
        Encounter closed = openEncounter().closed("Consulta breve", "Resumen", OPENED_AT);

        assertThatThrownBy(() -> closed.withNote(note("tarde")))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> closed.closed("otra vez", "Resumen", OPENED_AT))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void refusesAClosedConsultationWithoutACloseTime() {
        assertThatThrownBy(() -> new Encounter(
                        UUID.randomUUID(), "enc_001", "conversation-1", Encounter.Status.CLOSED,
                        java.util.List.of(), null, null, OPENED_AT, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refusesAnOpenConsultationWithACloseTime() {
        assertThatThrownBy(() -> new Encounter(
                        UUID.randomUUID(), "enc_001", "conversation-1", Encounter.Status.OPEN,
                        java.util.List.of(), null, null, OPENED_AT, OPENED_AT))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refusesACodeThatIsNotAConsultationCode() {
        assertThatThrownBy(() -> Encounter.opened(UUID.randomUUID(), "evt_001", "conversation-1", OPENED_AT))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
