package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.careme.backend.PostgresIntegrationTest;
import com.careme.backend.dto.ChatMessageRequest;
import com.careme.backend.dto.ChatMessageResponse;
import com.careme.backend.dto.ClinicalEventIntent;
import com.careme.backend.entity.ClinicalEvent;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

/**
 * End-to-end checks of the register → ask cycle against real PostgreSQL and a
 * real events directory: the answer is grounded and the clinical history stays
 * exactly as it was.
 */
@SpringBootTest
@TestPropertySource(properties = "careme.events.directory=target/test-events-query")
class ClinicalHistoryQueryFlowIntegrationTest extends PostgresIntegrationTest {

    private static final Path EVENTS_DIRECTORY = Path.of("target/test-events-query");

    @Autowired
    private ChatOrchestrator orchestrator;

    @Autowired
    private ClinicalEventMarkdownStore markdownStore;

    @Autowired
    private ClinicalEventIndexWriter indexWriter;

    @Autowired
    private ClinicalHistoryQueryService historyQueryService;

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
    void answeringAQuestionLeavesTheIndexAndTheDocumentsUntouched() throws IOException {
        var event = new ClinicalEvent(
                UUID.randomUUID(), "evt_001", ClinicalEvent.ClinicalEventType.DIAGNOSIS,
                LocalDate.of(2026, 1, 10), ClinicalEvent.DatePrecision.EXACT, "el 10 de enero",
                "Hipertensión diagnosticada", ClinicalEvent.EventSource.PATIENT,
                OffsetDateTime.now(ZoneOffset.UTC));
        markdownStore.write(event);
        indexWriter.write(event);
        String indexBefore = indexSnapshot();
        String documentsBefore = documentsSnapshot();

        var response = orchestrator.process(new ChatMessageRequest(
                "¿Cuándo me diagnosticaron hipertensión?", null, "msg-read-only"));

        assertThat(response.status()).isEqualTo(ChatMessageResponse.Status.ANSWERED);
        assertThat(indexSnapshot()).isEqualTo(indexBefore);
        assertThat(documentsSnapshot()).isEqualTo(documentsBefore);
    }

    @Test
    void registersThenAnswersPreservingPrecisionAndTheHistory() throws IOException {
        var registered = orchestrator.process(new ChatMessageRequest(
                "Hace años me diagnosticaron hipertensión", null, "msg-register"));
        assertThat(registered.status()).isEqualTo(ChatMessageResponse.Status.REGISTERED);

        String indexAfterRegistration = indexSnapshot();
        String documentsAfterRegistration = documentsSnapshot();

        var answered = orchestrator.process(new ChatMessageRequest(
                "¿Cuándo me diagnosticaron hipertensión?", registered.conversationId(), "msg-ask"));

        assertThat(answered.status()).isEqualTo(ChatMessageResponse.Status.ANSWERED);
        assertThat(answered.events()).isNotEmpty();
        assertThat(answered.events().getFirst().datePrecision()).isEqualTo("approximate");
        assertThat(answered.message()).contains("hipertensión");

        var noRecords = orchestrator.process(new ChatMessageRequest(
                "¿He tenido migrañas?", registered.conversationId(), "msg-none"));
        assertThat(noRecords.status()).isEqualTo(ChatMessageResponse.Status.NO_RECORDS);
        assertThat(noRecords.events()).isEmpty();

        assertThat(indexSnapshot()).isEqualTo(indexAfterRegistration);
        assertThat(documentsSnapshot()).isEqualTo(documentsAfterRegistration);
    }

    @Test
    void declaresEveryAbsenceReasonAndLeavesTheHistoryUntouched() throws IOException {
        // The history is still empty, so the absence is the whole history.
        var emptyHistory = askThroughChat("¿He tenido migrañas?");
        assertThat(emptyHistory.status()).isEqualTo(ChatMessageResponse.Status.NO_RECORDS);
        assertThat(emptyHistory.absenceReason())
                .isEqualTo(ChatMessageResponse.AbsenceReason.EMPTY_HISTORY);

        var registered = new ClinicalEvent(
                UUID.randomUUID(), "evt_001", ClinicalEvent.ClinicalEventType.DIAGNOSIS,
                LocalDate.of(2026, 1, 10), ClinicalEvent.DatePrecision.EXACT, "el 10 de enero",
                "Hipertensión diagnosticada", ClinicalEvent.EventSource.PATIENT,
                OffsetDateTime.now(ZoneOffset.UTC));
        markdownStore.write(registered);
        indexWriter.write(registered);
        String indexBefore = indexSnapshot();
        String documentsBefore = documentsSnapshot();

        // The history holds events, but none of the type the question asks about.
        var noEventsOfType = askThroughChat("¿Qué medicación tomo?");
        assertThat(noEventsOfType.status()).isEqualTo(ChatMessageResponse.Status.NO_RECORDS);
        assertThat(noEventsOfType.absenceReason())
                .isEqualTo(ChatMessageResponse.AbsenceReason.NO_EVENTS_OF_TYPE);
        assertThat(noEventsOfType.suggestedActions()).containsExactly(
                ChatMessageResponse.SuggestedAction.REFORMULATE,
                ChatMessageResponse.SuggestedAction.REGISTER);

        // The history holds events, but none matched the terms the question used.
        var noTermMatch = askThroughChat("¿He tenido migrañas?");
        assertThat(noTermMatch.status()).isEqualTo(ChatMessageResponse.Status.NO_RECORDS);
        assertThat(noTermMatch.absenceReason())
                .isEqualTo(ChatMessageResponse.AbsenceReason.NO_TERM_MATCH);

        // The history holds events, but none inside the questioned period. The
        // offline interpreter never derives a period, so this question reaches the
        // query service directly with the period the user stated.
        var noEventsInPeriod = historyQueryService.answer(ClinicalEventIntent.query(
                new ClinicalEventIntent.Query(
                        "¿Qué me pasó en 2025?",
                        ClinicalEventIntent.Query.Scope.HISTORY,
                        List.of("paso"),
                        null,
                        LocalDate.of(2025, 1, 1),
                        LocalDate.of(2025, 12, 31))));
        assertThat(noEventsInPeriod.kind()).isEqualTo(ClinicalAnswerResult.Kind.NO_RECORDS);
        assertThat(noEventsInPeriod.absenceReason())
                .isEqualTo(ChatMessageResponse.AbsenceReason.NO_EVENTS_IN_PERIOD);

        assertThat(indexSnapshot()).as("el índice no cambia al declarar una ausencia").isEqualTo(indexBefore);
        assertThat(documentsSnapshot()).as("los documentos no cambian al declarar una ausencia")
                .isEqualTo(documentsBefore);
    }

    private ChatMessageResponse askThroughChat(String message) {
        return orchestrator.process(new ChatMessageRequest(message, null, UUID.randomUUID().toString()));
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

    private String documentsSnapshot() throws IOException {
        if (!Files.exists(EVENTS_DIRECTORY)) {
            return "";
        }
        try (var paths = Files.list(EVENTS_DIRECTORY)) {
            return paths
                    .filter(path -> path.getFileName().toString().endsWith(".md"))
                    .sorted()
                    .map(this::readDocument)
                    .collect(Collectors.joining("\n"));
        }
    }

    private String readDocument(Path path) {
        try {
            return path.getFileName() + ":" + Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
