package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import com.careme.backend.PostgresIntegrationTest;
import com.careme.backend.dto.ChatMessageRequest;
import com.careme.backend.dto.ChatMessageResponse;
import com.careme.backend.repository.MetricMeasurementRepository;

/**
 * Contract of a turn that mentions a measurement: it is collected in the consultation's
 * notes, and neither the tracking nor the clinical history changes during the turn. Only
 * the close writes.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "careme.events.directory=target/test-events-measurement-turn",
        "careme.encounters.directory=target/test-encounters-measurement-turn"
})
class MeasurementTurnContractTest extends PostgresIntegrationTest {

    private static final Path EVENTS_DIRECTORY = Path.of("target/test-events-measurement-turn");
    private static final Path ENCOUNTERS_DIRECTORY = Path.of("target/test-encounters-measurement-turn");
    private static final String CONVERSATION_ID = "conversation-measurement-turn";

    @Autowired
    private ChatOrchestrator orchestrator;

    @Autowired
    private EncounterService encounterService;

    @Autowired
    private MetricMeasurementRepository metricMeasurementRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanIndexAndDocuments() throws IOException {
        jdbcTemplate.update("DELETE FROM clinical_event_index");
        jdbcTemplate.update("DELETE FROM encounter_index");
        deleteDirectoryContents(EVENTS_DIRECTORY);
        deleteDirectoryContents(ENCOUNTERS_DIRECTORY);
    }

    private static void deleteDirectoryContents(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(directory)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    private ChatMessageResponse send(String message, String messageId) {
        return orchestrator.process(new ChatMessageRequest(message, CONVERSATION_ID, messageId));
    }

    @Test
    void collectsTheMeasurementWithoutChangingTheTrackingOrTheHistory() throws IOException {
        ChatMessageResponse response = send("Hoy me tomaron la presión y fue 145/92.", "m-1");

        assertThat(response.status()).isEqualTo(ChatMessageResponse.Status.NOTED);
        assertThat(metricMeasurementRepository.findAll())
                .as("el seguimiento no cambia durante el turno")
                .isEmpty();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT count(*) FROM clinical_event_index", Long.class))
                .as("ningún hecho se registra durante el turno")
                .isZero();
        assertThat(countEventDocuments()).as("no se escribe ningún documento de hecho").isZero();
    }

    @Test
    void keepsTheMeasurementInTheNotesOfTheConsultation() {
        send("Hoy me tomaron la presión y fue 145/92.", "m-2");

        assertThat(encounterService.current(CONVERSATION_ID).measurementNotes())
                .singleElement()
                .satisfies(note -> assertThat(note.metricCode()).isEqualTo("blood_pressure"));
    }

    @Test
    void registersTheMeasurementOnlyWhenTheConsultationCloses() {
        send("Hoy me tomaron la presión y fue 145/92.", "m-3");
        assertThat(metricMeasurementRepository.findAll()).isEmpty();

        ChatMessageResponse close = orchestrator.closeConsultation(CONVERSATION_ID);

        assertThat(close.status()).isEqualTo(ChatMessageResponse.Status.REGISTERED);
        assertThat(metricMeasurementRepository.findAll()).hasSize(1);
    }

    private long countEventDocuments() throws IOException {
        if (!Files.exists(EVENTS_DIRECTORY)) {
            return 0;
        }
        try (Stream<Path> paths = Files.list(EVENTS_DIRECTORY)) {
            return paths.filter(path -> path.getFileName().toString().endsWith(".md")).count();
        }
    }
}
