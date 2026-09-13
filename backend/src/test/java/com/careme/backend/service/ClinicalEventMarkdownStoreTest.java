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
}
