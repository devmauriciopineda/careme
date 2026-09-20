package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import com.careme.backend.PostgresIntegrationTest;
import com.careme.backend.dto.AgentOperation;
import com.careme.backend.entity.ClinicalEvent;
import com.careme.backend.repository.ClinicalEventQueryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

/**
 * UC-012 `E2` end to end against real PostgreSQL and a real events directory: a
 * turn that publishes a fact and then fails on a later operation leaves the
 * published fact exactly as it was.
 *
 * <p>It drives the loop with a scripted provider and the real executor, so the
 * registration goes through the real markdown store and the real index.
 */
@SpringBootTest
@TestPropertySource(properties = "careme.events.directory=target/test-events-turn-failure")
class ClinicalEventSurvivesLaterFailureIntegrationTest extends PostgresIntegrationTest {

    private static final Path EVENTS_DIRECTORY = Path.of("target/test-events-turn-failure");
    private static final String CONVERSATION_ID = "conversation-1";
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 19);

    private static final String REGISTRATION = "{\"type\":\"diagnosis\","
            + "\"content\":\"Hipertensión diagnosticada.\",\"date\":\"2026-01-10\","
            + "\"date_precision\":\"exact\"}";
    private static final String CONSULTATION = "{\"question\":\"¿qué diagnósticos tengo?\","
            + "\"search_terms\":[\"diagnostico\"]}";

    @Autowired
    private AgentOperationExecutor executor;

    @Autowired
    private ClinicalEventQueryRepository queryRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private ClinicalHistoryQueryService historyQueryService;

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
    void keepsAPublishedEventWhenALaterOperationOfTheSameTurnFails() throws IOException {
        // The consultation of the same turn cannot be completed, so the turn fails
        // after the registration has already reached the clinical history.
        doReturn(ClinicalAnswerResult.failure("No pude buscar en tu historia."))
                .when(historyQueryService).answer(any());

        ScriptedProvider provider = new ScriptedProvider()
                .then(asks(operation(AgentOperation.REGISTER_EVENT, REGISTRATION)))
                .then(asks(operation(AgentOperation.CONSULT_HISTORY, CONSULTATION)));
        AgentTurnRunner runner = new AgentTurnRunner(provider, executor, 4);

        AgentTurn turn = runner.run(
                "Me diagnosticaron hipertensión. ¿Qué diagnósticos tengo?",
                new AgentContext(CONVERSATION_ID, TODAY, java.time.ZoneId.of("Europe/Madrid"), List.of()));

        assertThat(turn.failed()).isTrue();
        assertThat(turn.message()).isEqualTo("No pude buscar en tu historia.");
        assertThat(turn.completedOperations()).singleElement()
                .satisfies(result -> assertThat(result.operation()).isEqualTo(AgentOperation.REGISTER_EVENT));

        // The published fact survives: its document is there and its code is still
        // consultable through the derived index.
        Path document = EVENTS_DIRECTORY.resolve("evt_001.md");
        assertThat(document).exists();
        String published = Files.readString(document);
        assertThat(published).contains("Hipertensión diagnosticada.");

        assertThat(countIndexRows()).isEqualTo(1);
        assertThat(queryRepository.search(List.of("hipertensión"), null, null, null))
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.code()).isEqualTo("evt_001");
                    assertThat(event.content()).isEqualTo("Hipertensión diagnosticada.");
                    assertThat(event.datePrecision()).isEqualTo(ClinicalEvent.DatePrecision.EXACT);
                });

        // And it is left alone: the next registration takes the following code and
        // does not touch the document that was already published.
        AgentOperationResult next = executor.execute("conversation-2", registration(), TODAY);

        assertThat(next.registration().events()).singleElement()
                .satisfies(event -> assertThat(event.code()).isEqualTo("evt_002"));
        assertThat(Files.readString(document)).as("el documento publicado no se reescribe").isEqualTo(published);
        assertThat(countIndexRows()).isEqualTo(2);
    }

    private long countIndexRows() {
        Long count = jdbcTemplate.queryForObject("SELECT count(*) FROM clinical_event_index", Long.class);
        return count == null ? 0L : count;
    }

    private static AgentOperationCall registration() {
        return operation(AgentOperation.REGISTER_EVENT, REGISTRATION);
    }

    private static AgentOperationCall operation(AgentOperation operation, String json) {
        return AgentOperationCall.of(operation.operationName(), arguments(json))
                .orElseThrow(() -> new AssertionError("la operación declarada no se resolvió"));
    }

    private static JsonNode arguments(String json) {
        try {
            return new ObjectMapper().readTree(json);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static AgentProvider.ProviderTurn asks(AgentOperationCall... calls) {
        List<AgentProvider.ProviderCall> providerCalls = new ArrayList<>();
        for (int index = 0; index < calls.length; index++) {
            providerCalls.add(new AgentProvider.ProviderCall("call-" + index, calls[index]));
        }
        return new AgentProvider.ProviderTurn(
                Map.of("role", "assistant", "content", ""), providerCalls, null);
    }

    /** A provider that answers with a scripted sequence of turns. */
    private static final class ScriptedProvider implements AgentProvider {

        private final Deque<ProviderTurn> script = new ArrayDeque<>();

        ScriptedProvider then(ProviderTurn turn) {
            script.addLast(turn);
            return this;
        }

        @Override
        public ProviderTurn send(List<Map<String, Object>> conversation) {
            if (script.isEmpty()) {
                throw new LlmIntegrationException("the scripted provider ran out of turns");
            }
            return script.removeFirst();
        }
    }
}
