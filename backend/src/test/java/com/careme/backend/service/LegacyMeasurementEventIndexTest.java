package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import com.careme.backend.PostgresIntegrationTest;

/**
 * Verifies that the derived index can still be rebuilt from a document written before
 * `measurement` was retired: the document is read back and indexed as it is, and it is
 * never rewritten. The retired type is not deleted from the catalogue precisely so a
 * document like this one does not become unreadable.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "careme.events.directory=target/test-events-legacy-measurement",
        "careme.encounters.directory=target/test-encounters-legacy-measurement"
})
class LegacyMeasurementEventIndexTest extends PostgresIntegrationTest {

    private static final Path EVENTS_DIRECTORY = Path.of("target/test-events-legacy-measurement");

    private static final String LEGACY_DOCUMENT = """
            ---
            id: 11111111-1111-1111-1111-111111111111
            code: evt_900
            type: measurement
            date: 2026-01-02
            date_precision: exact
            date_text: ""
            source: patient
            created_at: 2026-01-02T10:00:00-05:00
            ---

            # Hecho clínico evt_900

            Presion 145/92
            """;

    @Autowired
    private ClinicalEventIndexRebuilder rebuilder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void writeTheLegacyDocument() throws IOException {
        jdbcTemplate.update("DELETE FROM clinical_event_index");
        if (Files.exists(EVENTS_DIRECTORY)) {
            try (var paths = Files.walk(EVENTS_DIRECTORY)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                    Files.deleteIfExists(path);
                }
            }
        }
        Files.createDirectories(EVENTS_DIRECTORY);
        Files.writeString(EVENTS_DIRECTORY.resolve("evt_900.md"), LEGACY_DOCUMENT);
    }

    @Test
    void rebuildsTheIndexFromALegacyMeasurementDocument() throws IOException {
        int rebuilt = rebuilder.rebuild();

        assertThat(rebuilt).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT count(*) FROM clinical_event_index WHERE type = 'measurement'", Long.class))
                .isEqualTo(1);
    }

    @Test
    void doesNotRewriteTheLegacyDocument() throws IOException {
        rebuilder.rebuild();

        assertThat(Files.readString(EVENTS_DIRECTORY.resolve("evt_900.md")))
                .as("un documento ya escrito no se reescribe")
                .isEqualTo(LEGACY_DOCUMENT);
    }
}
