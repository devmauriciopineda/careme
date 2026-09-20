package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.careme.backend.entity.ClinicalEvent;
import com.careme.backend.entity.Encounter;
import com.careme.backend.entity.EncounterNote;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class EncounterMarkdownStoreTest {

    @TempDir
    Path encountersDirectory;

    private static Encounter openConsultation(String code) {
        return Encounter.opened(
                UUID.fromString("9c1f5a62-3d4b-4c7e-9f10-5a6b7c8d9e0f"),
                code,
                "conversation-1",
                OffsetDateTime.parse("2026-09-20T09:00:00Z"));
    }

    private static EncounterNote approximateNote() {
        return new EncounterNote(
                ClinicalEvent.ClinicalEventType.DIAGNOSIS,
                "colesterol alto",
                LocalDate.of(2025, 3, 1),
                ClinicalEvent.DatePrecision.APPROXIMATE,
                "hace unos dos años");
    }

    private static EncounterNote multiLineNote() {
        return new EncounterNote(
                ClinicalEvent.ClinicalEventType.MEDICATION,
                "toma ibuprofeno\npor las mañanas",
                null,
                ClinicalEvent.DatePrecision.UNKNOWN,
                null);
    }

    @Test
    void preservesAConsultationWithItsNotesThroughTheRoundTrip() throws Exception {
        Encounter consultation = openConsultation("enc_001")
                .withNote(approximateNote())
                .withNote(multiLineNote());
        EncounterMarkdownStore store = new EncounterMarkdownStore(encountersDirectory);

        store.create(consultation);
        Encounter loaded = store.read("enc_001");

        assertThat(Files.exists(encountersDirectory.resolve("enc_001.md"))).isTrue();
        assertThat(loaded).isEqualTo(consultation);
        assertThat(loaded.notes().get(1).content()).isEqualTo("toma ibuprofeno\npor las mañanas");
    }

    @Test
    void preservesTheClosedConsultationWithItsSummary() throws Exception {
        Encounter closed = openConsultation("enc_001")
                .withNote(approximateNote())
                .closed("Colesterol", "La persona menciona colesterol alto hace unos dos años.",
                        OffsetDateTime.parse("2026-09-20T09:45:00Z"));
        EncounterMarkdownStore store = new EncounterMarkdownStore(encountersDirectory);

        store.create(closed);

        assertThat(store.read("enc_001")).isEqualTo(closed);
    }

    @Test
    void replacesTheDocumentOfAnOpenConsultationWhenItCollectsAnotherNote() throws Exception {
        EncounterMarkdownStore store = new EncounterMarkdownStore(encountersDirectory);
        store.create(openConsultation("enc_001"));

        store.replace(openConsultation("enc_001").withNote(approximateNote()));

        Encounter loaded = store.read("enc_001");
        assertThat(loaded.notes()).hasSize(1);
        assertThat(loaded.isOpen()).isTrue();
    }

    @Test
    void keepsTheNotesInsideTheConsultationDocument() throws Exception {
        EncounterMarkdownStore store = new EncounterMarkdownStore(encountersDirectory);

        store.create(openConsultation("enc_001").withNote(approximateNote()));

        try (var paths = Files.list(encountersDirectory)) {
            assertThat(paths.map(path -> path.getFileName().toString()).toList())
                    .containsExactly("enc_001.md");
        }
        assertThat(Files.readString(encountersDirectory.resolve("enc_001.md"), StandardCharsets.UTF_8))
                .contains("colesterol alto");
    }

    @Test
    void refusesToOverwriteAConsultationThatAlreadyExists() throws Exception {
        EncounterMarkdownStore store = new EncounterMarkdownStore(encountersDirectory);
        store.create(openConsultation("enc_001"));
        String persisted = Files.readString(encountersDirectory.resolve("enc_001.md"));

        assertThatThrownBy(() -> store.create(openConsultation("enc_001")))
                .isInstanceOf(IOException.class);

        assertThat(Files.readString(encountersDirectory.resolve("enc_001.md"))).isEqualTo(persisted);
    }

    @Test
    void reservesCodesAfterTheDocumentsAlreadyPersisted() throws Exception {
        EncounterMarkdownStore store = new EncounterMarkdownStore(encountersDirectory);
        store.create(openConsultation("enc_001"));
        store.create(openConsultation("enc_007"));

        assertThat(store.reserveCodes(2)).containsExactly("enc_008", "enc_009");
    }

    @Test
    void writesUnderTheConfiguredDirectory() throws Exception {
        Path configured = encountersDirectory.resolve("nested").resolve("encounters");
        EncounterMarkdownStore store = new EncounterMarkdownStore(configured.toString());

        store.create(openConsultation("enc_001"));

        assertThat(Files.exists(configured.resolve("enc_001.md"))).isTrue();
    }

    @Test
    void readsOnlyMarkdownDocuments() throws Exception {
        EncounterMarkdownStore store = new EncounterMarkdownStore(encountersDirectory);
        store.create(openConsultation("enc_001"));
        Files.writeString(encountersDirectory.resolve("notes.txt"), "ignore me");

        assertThat(store.readAll()).extracting(Encounter::code).containsExactly("enc_001");
    }

    @Test
    void returnsNothingWhenTheDirectoryDoesNotExistYet() throws Exception {
        EncounterMarkdownStore store = new EncounterMarkdownStore(encountersDirectory.resolve("missing"));

        assertThat(store.readAll()).isEqualTo(List.of());
    }

    @Test
    void refusesAFrontMatterThatIsNotAFrontMatter() {
        assertThatThrownBy(() -> EncounterMarkdownStore.deserialize("no es un documento de consulta"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refusesAFrontMatterWithoutTheConversation() {
        String document = "---\n"
                + "id: 9c1f5a62-3d4b-4c7e-9f10-5a6b7c8d9e0f\n"
                + "code: enc_001\n"
                + "status: open\n"
                + "created_at: 2026-09-20T09:00:00Z\n"
                + "---\n\n# Consulta enc_001\n";

        assertThatThrownBy(() -> EncounterMarkdownStore.deserialize(document))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("conversation");
    }

    @Test
    void preservesQuotesAndNewlinesThroughTheRoundTrip() throws Exception {
        EncounterMarkdownStore store = new EncounterMarkdownStore(encountersDirectory);
        EncounterNote tricky = new EncounterNote(
                ClinicalEvent.ClinicalEventType.NOTE,
                "dice \"me duele\"\ny desde ayer",
                null,
                ClinicalEvent.DatePrecision.UNKNOWN,
                null);

        store.create(openConsultation("enc_001").withNote(tricky));

        assertThat(store.read("enc_001").notes().getFirst().content())
                .isEqualTo("dice \"me duele\"\ny desde ayer");
    }

    @Test
    void reservesCodesIgnoringNamesThatAreNotConsultations() throws Exception {
        EncounterMarkdownStore store = new EncounterMarkdownStore(encountersDirectory);
        store.create(openConsultation("enc_003"));
        Files.writeString(encountersDirectory.resolve("resumen.md"), "no es una consulta");

        assertThat(store.reserveCodes(1)).containsExactly("enc_004");
    }

    @Test
    void readsOnlyTheOpenConsultations() throws Exception {
        EncounterMarkdownStore store = new EncounterMarkdownStore(encountersDirectory);
        store.create(openConsultation("enc_001")
                .closed("Motivo", "Resumen", OffsetDateTime.parse("2026-09-20T09:45:00Z")));
        store.create(openConsultation("enc_002"));

        assertThat(store.readOpen()).extracting(Encounter::code).containsExactly("enc_002");
        assertThat(store.findOpenByConversation("conversation-1")).get()
                .extracting(Encounter::code).isEqualTo("enc_002");
    }
}
