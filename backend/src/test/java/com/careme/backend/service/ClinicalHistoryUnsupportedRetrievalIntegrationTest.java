package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.careme.backend.PostgresIntegrationTest;
import com.careme.backend.dto.ChatMessageRequest;
import com.careme.backend.dto.ChatMessageResponse;
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
 * The case this change was extended for: retrieval returns a registered event that
 * does not answer the question.
 *
 * <p>The turn must declare the absence of records instead of presenting that event
 * as the support of an answer, and the clinical history must stay untouched.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "careme.events.directory=target/test-events-absence",
        "careme.llm.fake.coverage=none"
})
class ClinicalHistoryUnsupportedRetrievalIntegrationTest extends PostgresIntegrationTest {

    private static final Path EVENTS_DIRECTORY = Path.of("target/test-events-absence");

    @Autowired
    private ChatOrchestrator orchestrator;

    @Autowired
    private ClinicalEventMarkdownStore markdownStore;

    @Autowired
    private ClinicalEventIndexWriter indexWriter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanIndexAndDocuments() throws IOException {
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

    @Test
    void declaresAbsenceWhenTheRetrievedEventDoesNotAnswerTheQuestion() throws IOException {
        var event = new ClinicalEvent(
                UUID.randomUUID(), "evt_001", ClinicalEvent.ClinicalEventType.DIAGNOSIS,
                LocalDate.of(2026, 1, 10), ClinicalEvent.DatePrecision.EXACT, "el 10 de enero",
                "Hipertensión diagnosticada", ClinicalEvent.EventSource.PATIENT,
                OffsetDateTime.now(ZoneOffset.UTC));
        markdownStore.write(event);
        indexWriter.write(event);
        String indexBefore = indexSnapshot();

        // The question shares a term with the registered event, so retrieval returns
        // it, but the event is about something the question does not ask.
        var response = orchestrator.process(new ChatMessageRequest(
                "¿Qué consta sobre hipertensión?", null, "msg-unsupported-retrieval"));

        assertThat(response.status()).isEqualTo(ChatMessageResponse.Status.NO_RECORDS);
        assertThat(response.absenceReason()).isEqualTo(ChatMessageResponse.AbsenceReason.NO_TERM_MATCH);
        assertThat(response.events()).as("el hecho recuperado no se presenta como apoyo").isEmpty();
        assertThat(response.suggestedActions()).containsExactlyInAnyOrder(
                ChatMessageResponse.SuggestedAction.REFORMULATE,
                ChatMessageResponse.SuggestedAction.REGISTER);
        assertThat(response.message()).contains("no significa que no haya ocurrido");
        assertThat(indexSnapshot()).as("el índice no cambia al declarar la ausencia").isEqualTo(indexBefore);
        assertThat(countDocuments()).isEqualTo(1L);
    }

    private String indexSnapshot() {
        return jdbcTemplate.query(
                "SELECT code, type, event_date, date_precision, date_text, content, source, created_at "
                        + "FROM clinical_event_index ORDER BY code",
                (resultSet, rowNumber) -> resultSet.getString(1) + "|" + resultSet.getString(2) + "|"
                        + resultSet.getObject(3) + "|" + resultSet.getString(4) + "|"
                        + resultSet.getString(5) + "|" + resultSet.getString(6) + "|"
                        + resultSet.getString(7) + "|" + resultSet.getObject(8))
                .toString();
    }

    private long countDocuments() throws IOException {
        if (!Files.exists(EVENTS_DIRECTORY)) {
            return 0L;
        }
        try (var paths = Files.list(EVENTS_DIRECTORY)) {
            return paths.filter(path -> path.getFileName().toString().endsWith(".md")).count();
        }
    }
}
