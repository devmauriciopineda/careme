package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.careme.backend.entity.ClinicalEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ClinicalEventMarkdownStoreTest {

    @TempDir
    Path eventsDirectory;

    private static ClinicalEvent eventWithCode(String code) {
        return new ClinicalEvent(
                UUID.fromString("6f1d0f9a-1f5f-4a0e-9a4e-4f0f6a1c2b3d"),
                code,
                ClinicalEvent.ClinicalEventType.MEASUREMENT,
                LocalDate.of(2026, 9, 13),
                ClinicalEvent.DatePrecision.EXACT,
                "hoy",
                "Presion arterial 145/92",
                ClinicalEvent.EventSource.PATIENT,
                OffsetDateTime.parse("2026-09-13T10:15:30Z"));
    }

    private static ClinicalEvent sampleEvent() {
        return eventWithCode("evt_001");
    }

    @Test
    void preservesClinicalEventThroughMarkdownRoundTrip() throws Exception {
        ClinicalEvent event = sampleEvent();
        ClinicalEventMarkdownStore store = new ClinicalEventMarkdownStore(eventsDirectory);

        store.write(event);
        ClinicalEvent loaded = store.read("evt_001");

        assertThat(Files.exists(eventsDirectory.resolve("evt_001.md"))).isTrue();
        assertThat(loaded).isEqualTo(event);
    }

    @Test
    void writesUnderTheConfiguredDirectory() throws Exception {
        Path configured = eventsDirectory.resolve("nested").resolve("events");
        ClinicalEventMarkdownStore store = new ClinicalEventMarkdownStore(configured.toString());

        store.write(sampleEvent());

        assertThat(Files.exists(configured.resolve("evt_001.md"))).isTrue();
    }

    @Test
    void reservesCodesAfterTheDocumentsAlreadyPersisted() throws Exception {
        ClinicalEventMarkdownStore store = new ClinicalEventMarkdownStore(eventsDirectory);
        store.write(sampleEvent());
        store.write(eventWithCode("evt_007"));

        assertThat(store.reserveCodes(2)).containsExactly("evt_008", "evt_009");
    }

    @Test
    void refusesToOverwriteAPersistedDocument() throws Exception {
        ClinicalEventMarkdownStore store = new ClinicalEventMarkdownStore(eventsDirectory);
        store.write(sampleEvent());
        String persisted = Files.readString(eventsDirectory.resolve("evt_001.md"));

        assertThatThrownBy(() -> store.writeAtomically(List.of(sampleEvent())))
                .isInstanceOf(IOException.class);

        assertThat(Files.readString(eventsDirectory.resolve("evt_001.md"))).isEqualTo(persisted);
    }

    @Test
    void publishesSeveralDocumentsAtomicallyAndReadsOnlyMarkdownFiles() throws Exception {
        ClinicalEventMarkdownStore store = new ClinicalEventMarkdownStore(eventsDirectory);

        store.writeAtomically(List.of(sampleEvent(), eventWithCode("evt_002")));
        Files.writeString(eventsDirectory.resolve("notes.txt"), "ignore me");

        assertThat(store.readAll()).extracting(ClinicalEvent::code)
                .containsExactlyInAnyOrder("evt_001", "evt_002");
    }

    @Test
    void returnsEmptyForMissingDirectoryAndDeletesExistingOrMissingDocuments() throws Exception {
        ClinicalEventMarkdownStore store = new ClinicalEventMarkdownStore(eventsDirectory.resolve("missing"));

        assertThat(store.readAll()).isEmpty();
        store.write(sampleEvent());
        store.delete("evt_001");
        store.delete("evt_999");

        assertThat(store.readAll()).isEmpty();
    }

    @Test
    void reservesCodesWhileIgnoringNonCodeAndMalformedDocuments() throws Exception {
        ClinicalEventMarkdownStore store = new ClinicalEventMarkdownStore(eventsDirectory);

        Files.writeString(eventsDirectory.resolve("evt_bad.md"), "not an event");
        Files.writeString(eventsDirectory.resolve("evt_004.md"), "not an event");
        Files.writeString(eventsDirectory.resolve("other.txt"), "not an event");

        assertThat(store.reserveCodes(2)).containsExactly("evt_005", "evt_006");
    }

    @Test
    void serializesAndReadsEventsWithoutOptionalDates() throws Exception {
        ClinicalEvent event = new ClinicalEvent(
                UUID.randomUUID(),
                "evt_010",
                ClinicalEvent.ClinicalEventType.NOTE,
                null,
                ClinicalEvent.DatePrecision.UNKNOWN,
                null,
                "Sin fecha conocida",
                ClinicalEvent.EventSource.PATIENT,
                OffsetDateTime.parse("2026-09-13T10:15:30Z"));
        ClinicalEventMarkdownStore store = new ClinicalEventMarkdownStore(eventsDirectory);

        store.write(event);

        var loaded = store.read("evt_010");
        assertThat(loaded.id()).isEqualTo(event.id());
        assertThat(loaded.date()).isNull();
        assertThat(loaded.dateText()).isEmpty();
        assertThat(loaded.content()).isEqualTo(event.content());
    }

    @Test
    void rejectsMalformedMarkdown() {
        assertThatThrownBy(() -> ClinicalEventMarkdownStore.deserialize("not markdown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("front matter");
        assertThatThrownBy(() -> ClinicalEventMarkdownStore.deserialize(
                "---\nid: 6f1d0f9a-1f5f-4a0e-9a4e-4f0f6a1c2b3d\n---\nbody"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("body");
    }

            @Test
            void handlesMissingDirectoriesEscapedTextAndUnreadableDocuments() throws Exception {
            ClinicalEventMarkdownStore missingStore = new ClinicalEventMarkdownStore(eventsDirectory.resolve("absent"));
            assertThat(missingStore.reserveCodes(1)).containsExactly("evt_001");

            String escaped = "---\n"
                + "id: 6f1d0f9a-1f5f-4a0e-9a4e-4f0f6a1c2b3d\n"
                + "code: evt_020\n"
                + "type: note\n"
                + "date: \n"
                + "date_precision: unknown\n"
                + "date_text: \"hoy \\\"literal\\\"\"\n"
                + "source: patient\n"
                + "created_at: 2026-09-13T10:15:30Z\n"
                + "---\n\n# note\n\nContenido\n";
            assertThat(ClinicalEventMarkdownStore.deserialize(escaped).dateText())
                .isEqualTo("hoy \"literal\"");

            Files.createDirectories(eventsDirectory);
            Files.writeString(eventsDirectory.resolve("broken.md"), "broken");
            ClinicalEventMarkdownStore store = new ClinicalEventMarkdownStore(eventsDirectory);
            assertThatThrownBy(store::readAll)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("front matter");
            }
}
