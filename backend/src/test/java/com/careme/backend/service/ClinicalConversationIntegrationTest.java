package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;

import com.careme.backend.PostgresIntegrationTest;
import com.careme.backend.dto.ChatMessageRequest;
import com.careme.backend.dto.ChatMessageResponse;
import com.careme.backend.entity.ClinicalEvent;
import com.careme.backend.repository.ClinicalEventQueryRepository;
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
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

/**
 * UC-010 end to end against real PostgreSQL and a real events directory: the
 * conversational outcomes answer the message, and none of them reads, creates,
 * changes or removes anything in the clinical history.
 */
@SpringBootTest
@TestPropertySource(properties = "careme.events.directory=target/test-events-conversation")
class ClinicalConversationIntegrationTest extends PostgresIntegrationTest {

    private static final Path EVENTS_DIRECTORY = Path.of("target/test-events-conversation");

    @Autowired
    private ChatOrchestrator orchestrator;

    @Autowired
    private ClinicalEventMarkdownStore markdownStore;

    @Autowired
    private ClinicalEventIndexWriter indexWriter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private ClinicalEventQueryRepository queryRepository;

    @MockitoSpyBean
    private ClinicalAgent clinicalAgent;

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
    void answersEveryConversationalOutcomeWithoutTouchingTheHistory() throws IOException {
        String indexBefore = indexSnapshot();
        long documentsBefore = countDocuments();

        var general = ask("¿Es normal hacer deporte los domingos?");
        assertThat(general.status()).isEqualTo(ChatMessageResponse.Status.GENERAL_CONVERSATION);
        assertThat(general.message()).isNotBlank();
        assertThat(general.events()).isEmpty();
        assertThat(general.absenceReason()).isNull();
        assertThat(general.operations()).as("un turno conversacional no pasa por operaciones").isEmpty();

        var concept = ask("¿Qué es la hipertensión?");
        assertThat(concept.status()).isEqualTo(ChatMessageResponse.Status.GENERAL_CONVERSATION);
        assertThat(concept.message()).contains("hipertensión");

        var courtesy = ask("Hola, ¿cómo estás?");
        assertThat(courtesy.status()).isEqualTo(ChatMessageResponse.Status.GENERAL_CONVERSATION);
        assertThat(courtesy.message()).isNotBlank();

        var declined = ask("¿Qué me recomiendas para la hipertensión?");
        assertThat(declined.status()).isEqualTo(ChatMessageResponse.Status.GENERAL_CONVERSATION);
        assertThat(declined.message()).contains("no diagnostica ni recomienda");

        var undetermined = ask("¿Eso es bueno?");
        assertThat(undetermined.status()).isEqualTo(ChatMessageResponse.Status.CLARIFICATION_REQUIRED);
        assertThat(undetermined.message()).contains("historia clínica");

        assertThat(indexSnapshot()).as("el índice no cambia en un turno conversacional").isEqualTo(indexBefore);
        assertThat(countDocuments()).as("los documentos no cambian").isEqualTo(documentsBefore);
        verifyNoInteractions(queryRepository);
    }

    @Test
    void reportsTheOperationsOfEachConversationalTurn() throws IOException {
        String indexBefore = indexSnapshot();

        var absent = ask("¿He tenido migrañas?");

        assertThat(absent.status()).isEqualTo(ChatMessageResponse.Status.NO_RECORDS);
        assertThat(absent.operations()).as("la consulta que no encontró registros se informa").hasSize(1);
        assertThat(absent.operations().get(0).status()).isEqualTo(ChatMessageResponse.Status.NO_RECORDS);
        assertThat(absent.operations().get(0).absenceReason())
                .isEqualTo(ChatMessageResponse.AbsenceReason.EMPTY_HISTORY);
        assertThat(absent.events()).as("la ausencia no presenta hechos").isEmpty();

        assertThat(indexSnapshot()).isEqualTo(indexBefore);
        assertThat(countDocuments()).isZero();
    }

    @Test
    void attendsAColloquialQuestionAsAHistoryQuery() throws IOException {
        var event = new ClinicalEvent(
                UUID.randomUUID(), "evt_001", ClinicalEvent.ClinicalEventType.DIAGNOSIS,
                LocalDate.of(2026, 1, 10), ClinicalEvent.DatePrecision.EXACT, "el 10 de enero",
                "Hipertensión diagnosticada", ClinicalEvent.EventSource.PATIENT,
                OffsetDateTime.now(ZoneOffset.UTC));
        markdownStore.write(event);
        indexWriter.write(event);
        String indexBefore = indexSnapshot();

        var response = ask("¿Qué me dijeron de la hipertensión?");

        assertThat(response.status())
                .as("una reformulación coloquial se atiende como consulta de la historia")
                .isEqualTo(ChatMessageResponse.Status.ANSWERED);
        assertThat(response.events()).isNotEmpty();
        assertThat(response.operations()).singleElement()
                .satisfies(operation -> assertThat(operation.status())
                        .isEqualTo(ChatMessageResponse.Status.ANSWERED));
        assertThat(indexSnapshot()).isEqualTo(indexBefore);
    }

    @Test
    void reportsAFailedTurnWithoutReadingTheHistory() throws IOException {
        doThrow(new LlmIntegrationException("provider unavailable"))
                .when(clinicalAgent).respond(anyString(), any());
        String indexBefore = indexSnapshot();

        var response = ask("¿Qué es la hipertensión?");

        assertThat(response.status()).isEqualTo(ChatMessageResponse.Status.FAILED);
        assertThat(response.message()).contains("Puedes reintentarlo");
        assertThat(response.events()).isEmpty();
        assertThat(response.message()).doesNotContain("provider unavailable");
        assertThat(indexSnapshot()).as("el índice no cambia al fallar la conversación").isEqualTo(indexBefore);
        verifyNoInteractions(queryRepository);
    }

    private ChatMessageResponse ask(String message) {
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

    private long countDocuments() throws IOException {
        if (!Files.exists(EVENTS_DIRECTORY)) {
            return 0L;
        }
        try (var paths = Files.list(EVENTS_DIRECTORY)) {
            return paths.filter(path -> path.getFileName().toString().endsWith(".md")).count();
        }
    }
}
