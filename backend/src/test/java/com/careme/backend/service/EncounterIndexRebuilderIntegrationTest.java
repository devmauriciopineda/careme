package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.careme.backend.PostgresIntegrationTest;
import com.careme.backend.entity.ClinicalEvent;
import com.careme.backend.entity.Encounter;
import com.careme.backend.entity.EncounterNote;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "careme.encounters.directory=target/test-encounters-reindex")
class EncounterIndexRebuilderIntegrationTest extends PostgresIntegrationTest {

    private static final Path ENCOUNTERS_DIRECTORY = Path.of("target/test-encounters-reindex");

    @Autowired
    private EncounterMarkdownStore markdownStore;

    @Autowired
    private EncounterIndexWriter indexWriter;

    @Autowired
    private EncounterIndexRebuilder rebuilder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanEncounterData() throws IOException {
        jdbcTemplate.update("DELETE FROM encounter_index");
        if (Files.exists(ENCOUNTERS_DIRECTORY)) {
            try (var paths = Files.walk(ENCOUNTERS_DIRECTORY)) {
                paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException ignored) {
                    }
                });
            }
        }
    }

    private static Encounter openConsultation(String code) {
        return Encounter.opened(
                UUID.nameUUIDFromBytes(code.getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                code,
                "conversation-" + code,
                OffsetDateTime.parse("2026-09-20T09:00:00Z"));
    }

    @Test
    void rebuildsTheIndexFromTheMarkdownSourceOfTruth() throws Exception {
        markdownStore.create(openConsultation("enc_001"));
        jdbcTemplate.update("DELETE FROM encounter_index");

        int indexed = rebuilder.rebuild();

        assertThat(indexed).isEqualTo(1);
        assertThat(indexedStatusOf("enc_001")).isEqualTo("open");
    }

    @Test
    void recoversAClosedConsultationWithItsMotiveAndSummary() throws Exception {
        markdownStore.create(openConsultation("enc_001")
                .withNote(new EncounterNote(
                        ClinicalEvent.ClinicalEventType.DIAGNOSIS,
                        "colesterol alto",
                        LocalDate.of(2025, 3, 1),
                        ClinicalEvent.DatePrecision.APPROXIMATE,
                        "hace unos dos años"))
                .closed("Colesterol", "La persona menciona colesterol alto hace unos dos años.",
                        OffsetDateTime.parse("2026-09-20T09:45:00Z")));
        jdbcTemplate.update("DELETE FROM encounter_index");

        rebuilder.rebuild();

        assertThat(indexedStatusOf("enc_001")).isEqualTo("closed");
        assertThat(jdbcTemplate.queryForObject(
                        "SELECT summary FROM encounter_index WHERE code = ?", String.class, "enc_001"))
                .contains("colesterol alto");
    }

    @Test
    void rebuildIsIdempotent() throws Exception {
        markdownStore.create(openConsultation("enc_001"));

        rebuilder.rebuild();
        int indexedAgain = rebuilder.rebuild();

        assertThat(indexedAgain).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                        "SELECT count(*) FROM encounter_index", Integer.class))
                .isEqualTo(1);
    }

    @Test
    void doesNotIndexTheNotesOfAConsultation() throws Exception {
        markdownStore.create(openConsultation("enc_001")
                .withNote(new EncounterNote(
                        ClinicalEvent.ClinicalEventType.DIAGNOSIS,
                        "zumbidoeneloido",
                        null,
                        ClinicalEvent.DatePrecision.UNKNOWN,
                        null))
                .closed("Colesterol", "Resumen de la consulta sobre el colesterol.",
                        OffsetDateTime.parse("2026-09-20T09:45:00Z")));

        rebuilder.rebuild();

        assertThat(matches("zumbidoeneloido")).isZero();
        assertThat(matches("colesterol")).isEqualTo(1);
    }

    @Test
    void upsertsTheIndexWhenTheSameConsultationIsWrittenAgain() throws Exception {
        Encounter open = openConsultation("enc_001");
        markdownStore.create(open);
        indexWriter.write(open);

        Encounter closed = open.closed("Colesterol", "Resumen", OffsetDateTime.parse("2026-09-20T09:45:00Z"));
        markdownStore.replace(closed);
        indexWriter.write(closed);

        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM encounter_index", Integer.class))
                .isEqualTo(1);
        assertThat(indexedStatusOf("enc_001")).isEqualTo("closed");
    }

    @Test
    void keepsAClosedConsultationAndItsCloseTimeConsistentInTheSchema() {
        assertThatThrownBy(() -> jdbcTemplate.update(
                        "INSERT INTO encounter_index (id, code, status, created_at) VALUES (?, ?, 'closed', now())",
                        UUID.randomUUID(),
                        "enc_900"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private String indexedStatusOf(String code) {
        return jdbcTemplate.queryForObject(
                "SELECT status FROM encounter_index WHERE code = ?", String.class, code);
    }

    private int matches(String term) {
        return jdbcTemplate.queryForObject(
                "SELECT count(*) FROM encounter_index WHERE search_vector @@ plainto_tsquery('simple', ?)",
                Integer.class,
                term);
    }
}
