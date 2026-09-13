package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.careme.backend.entity.ClinicalEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ClinicalEventMarkdownStoreTest {

    @TempDir
    Path eventsDirectory;

    @Test
    void preservesClinicalEventThroughMarkdownRoundTrip() throws Exception {
        ClinicalEvent event = new ClinicalEvent(
                UUID.fromString("6f1d0f9a-1f5f-4a0e-9a4e-4f0f6a1c2b3d"),
                "evt_001",
                ClinicalEvent.ClinicalEventType.MEASUREMENT,
                LocalDate.of(2026, 9, 13),
                ClinicalEvent.DatePrecision.EXACT,
                "hoy",
                "Presion arterial 145/92",
                ClinicalEvent.EventSource.PATIENT,
                OffsetDateTime.parse("2026-09-13T10:15:30Z"));
        ClinicalEventMarkdownStore store = new ClinicalEventMarkdownStore(eventsDirectory);

        store.write(event);
        ClinicalEvent loaded = store.read("evt_001");

        assertThat(Files.exists(eventsDirectory.resolve("evt_001.md"))).isTrue();
        assertThat(loaded).isEqualTo(event);
    }
}
