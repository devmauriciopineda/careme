package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.careme.backend.PostgresIntegrationTest;
import com.careme.backend.entity.ClinicalEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

/**
 * The provenance of a registered fact must survive the rebuild of the derived
 * index, which is the only reason it lives in the Markdown front matter and not
 * only in PostgreSQL.
 */
@SpringBootTest
@TestPropertySource(properties = "careme.events.directory=target/test-events-provenance")
class ClinicalEventProvenanceIntegrationTest extends PostgresIntegrationTest {

    private static final Path EVENTS_DIRECTORY = Path.of("target/test-events-provenance");

    @Autowired
    private ClinicalEventMarkdownStore markdownStore;

    @Autowired
    private ClinicalEventIndexRebuilder rebuilder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanProvenanceData() throws IOException {
        jdbcTemplate.update("DELETE FROM clinical_event_index");
        if (Files.exists(EVENTS_DIRECTORY)) {
            try (var paths = Files.walk(EVENTS_DIRECTORY)) {
                paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException ignored) {
                    }
                });
            }
        }
    }

    private static ClinicalEvent event(String code, String encounterCode) {
        return new ClinicalEvent(
                UUID.nameUUIDFromBytes(code.getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                code,
                ClinicalEvent.ClinicalEventType.DIAGNOSIS,
                LocalDate.of(2026, 9, 1),
                ClinicalEvent.DatePrecision.EXACT,
                "el 1 de septiembre",
                "Hipertensión diagnosticada",
                ClinicalEvent.EventSource.PATIENT,
                OffsetDateTime.now(ZoneOffset.UTC),
                encounterCode);
    }

    @Test
    void keepsTheConsultationOfOriginWhenTheIndexIsRebuiltFromMarkdown() throws Exception {
        markdownStore.write(event("evt_001", "enc_001"));
        jdbcTemplate.update("DELETE FROM clinical_event_index");

        rebuilder.rebuild();

        assertThat(jdbcTemplate.queryForObject(
                        "SELECT encounter_code FROM clinical_event_index WHERE code = ?",
                        String.class,
                        "evt_001"))
                .isEqualTo("enc_001");
    }

    @Test
    void leavesTheProvenanceOpenForAFactRegisteredWithoutAConsultation() throws Exception {
        markdownStore.write(event("evt_002", null));
        rebuilder.rebuild();

        assertThat(jdbcTemplate.queryForObject(
                        "SELECT encounter_code FROM clinical_event_index WHERE code = ?",
                        String.class,
                        "evt_002"))
                .isNull();
    }
}
