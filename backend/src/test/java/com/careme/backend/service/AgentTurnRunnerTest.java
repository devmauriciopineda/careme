package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.careme.backend.dto.AgentOperation;
import com.careme.backend.entity.ClinicalEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * The loop of a turn: what the assistant asks for is executed by the
 * application, the results go back to the assistant, and the turn ends with one
 * response —or with what could not be completed.
 */
class AgentTurnRunnerTest {

    private static final String CONVERSATION_ID = "conversation-1";
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 19);
    private static final ZoneId ZONE = ZoneId.of("Europe/Madrid");
    private static final AgentContext CONTEXT = new AgentContext(CONVERSATION_ID, TODAY, ZONE, List.of());
    private static final int MAX_OPERATIONS = 4;

    private static final String REGISTRATION = "{\"type\":\"diagnosis\",\"content\":\"Hipertensión.\","
            + "\"date\":\"2024-03-01\",\"date_precision\":\"exact\"}";
    private static final String CONSULTATION = "{\"question\":\"¿qué diagnósticos tengo?\","
            + "\"search_terms\":[\"diagnostico\"]}";

    private final ClinicalHistoryQueryService historyQueryService = mock(ClinicalHistoryQueryService.class);
    private final ClinicalEventRegistrationService registrationService =
            mock(ClinicalEventRegistrationService.class);
    private final ScriptedProvider provider = new ScriptedProvider();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void runsEveryOperationAMessageNeedsAndAnswersOnce() {
        when(registrationService.register(anyString(), any(), any())).thenReturn(registered());
        when(historyQueryService.answer(any())).thenReturn(answered());

        provider.then(asks(
                        call("1", AgentOperation.REGISTER_EVENT, REGISTRATION),
                        call("2", AgentOperation.CONSULT_HISTORY, CONSULTATION)))
                .then(says("He registrado el diagnóstico y esto es lo que consta."));

        AgentTurn turn = runner(MAX_OPERATIONS).run("Me diagnosticaron hipertensión. ¿Qué diagnósticos tengo?", CONTEXT);

        assertThat(turn.failed()).isFalse();
        assertThat(turn.message()).isEqualTo("He registrado el diagnóstico y esto es lo que consta.");
        assertThat(turn.operations()).hasSize(2);
        assertThat(turn.completedOperations()).hasSize(2);
        verify(registrationService).register(eq(CONVERSATION_ID), any(), eq(TODAY));
        verify(historyQueryService).answer(any());
    }

    @Test
    void allowsTheHistoryToBeConsultedMoreThanOnceInTheSameTurn() {
        when(historyQueryService.answer(any())).thenReturn(answered());

        provider.then(asks(
                        call("1", AgentOperation.CONSULT_HISTORY, CONSULTATION),
                        call("2", AgentOperation.CONSULT_HISTORY, CONSULTATION)))
                .then(says("Esto es lo que consta en tu historia."));

        AgentTurn turn = runner(MAX_OPERATIONS).run("¿Qué diagnósticos y qué medicación tengo?", CONTEXT);

        assertThat(turn.failed()).isFalse();
        assertThat(turn.operations()).hasSize(2);
        verify(historyQueryService, times(2)).answer(any());
    }

    @Test
    void handsTheOperationResultsBackToTheAssistant() throws Exception {
        when(registrationService.register(anyString(), any(), any())).thenReturn(registered());

        provider.then(asks(call("call-1", AgentOperation.REGISTER_EVENT, REGISTRATION)))
                .then(says("He registrado el diagnóstico."));

        runner(MAX_OPERATIONS).run("Me diagnosticaron hipertensión.", CONTEXT);

        Map<String, Object> tool = toolMessage(1);
        assertThat(tool.get("tool_call_id")).isEqualTo("call-1");
        String content = (String) tool.get("content");
        assertThat(content).contains("completed", "registered");
        assertThat(content).doesNotContain("data/", ".md", "path", "filesystem");
        assertThat(objectMapper.readTree(content).path("events").path(0).path("code").asText())
                .isEqualTo("evt_001");
    }

    @Test
    void stopsWhenTheTurnReachesItsOperationLimitAndReportsTheTurnAsIncomplete() {
        when(registrationService.register(anyString(), any(), any())).thenReturn(registered());

        provider.then(asks(
                call("1", AgentOperation.REGISTER_EVENT, REGISTRATION),
                call("2", AgentOperation.REGISTER_EVENT, REGISTRATION),
                call("3", AgentOperation.REGISTER_EVENT, REGISTRATION)));

        AgentTurn turn = runner(2).run("Tres hechos distintos.", CONTEXT);

        assertThat(turn.failed()).isTrue();
        assertThat(turn.message()).isNotBlank();
        assertThat(turn.operations()).hasSize(2);
        verify(registrationService, times(2)).register(anyString(), any(), any());
    }

    @Test
    void keepsWhatAlreadyCompletedWhenAnOperationFails() {
        when(registrationService.register(anyString(), any(), any())).thenReturn(registered());
        when(historyQueryService.answer(any()))
                .thenReturn(ClinicalAnswerResult.failure("No pude buscar en tu historia."));

        provider.then(asks(
                call("1", AgentOperation.REGISTER_EVENT, REGISTRATION),
                call("2", AgentOperation.CONSULT_HISTORY, CONSULTATION)));

        AgentTurn turn = runner(MAX_OPERATIONS).run("Registra esto y dime qué consta.", CONTEXT);

        assertThat(turn.failed()).isTrue();
        assertThat(turn.message()).isEqualTo("No pude buscar en tu historia.");
        assertThat(turn.completedOperations()).hasSize(1);
        assertThat(turn.completedOperations().get(0).operation()).isEqualTo(AgentOperation.REGISTER_EVENT);
    }

    @Test
    void executesNothingWhenTheAssistantIsUnavailable() {
        AgentTurn turn = runner(MAX_OPERATIONS).run("Me diagnosticaron hipertensión.", CONTEXT);

        assertThat(turn.failed()).isTrue();
        assertThat(turn.operations()).isEmpty();
        assertThat(turn.message()).isNotBlank();
        verifyNoInteractions(registrationService, historyQueryService);
    }

    @Test
    void keepsACompletedOperationWhenTheAssistantStopsAnswering() {
        when(registrationService.register(anyString(), any(), any())).thenReturn(registered());

        provider.then(asks(call("1", AgentOperation.REGISTER_EVENT, REGISTRATION)));

        AgentTurn turn = runner(MAX_OPERATIONS).run("Me diagnosticaron hipertensión y dime qué consta.", CONTEXT);

        assertThat(turn.failed()).isTrue();
        assertThat(turn.completedOperations()).hasSize(1);
        assertThat(turn.message()).isNotBlank();
    }

    @Test
    void failsTheTurnWhenTheAssistantProducesNeitherAnOperationNorAResponse() {
        provider.then(new AgentProvider.ProviderTurn(Map.of("role", "assistant"), List.of(), null));

        AgentTurn turn = runner(MAX_OPERATIONS).run("Me diagnosticaron hipertensión.", CONTEXT);

        assertThat(turn.failed()).isTrue();
        assertThat(turn.message()).isNotBlank();
        verifyNoInteractions(registrationService, historyQueryService);
    }

    @Test
    void feedsARejectedOperationBackSoTheAssistantCanAskForWhatIsMissing() {
        provider.then(asks(call("1", AgentOperation.REGISTER_EVENT,
                        "{\"type\":\"diagnosis\",\"date_precision\":\"exact\"}")))
                .then(says("¿Cuál fue el diagnóstico y con qué palabras quieres que lo registre?"));

        AgentTurn turn = runner(MAX_OPERATIONS).run("Me diagnosticaron algo.", CONTEXT);

        assertThat(turn.failed()).isFalse();
        assertThat(turn.message()).isEqualTo("¿Cuál fue el diagnóstico y con qué palabras quieres que lo registre?");
        assertThat(turn.completedOperations()).isEmpty();
        assertThat((String) toolMessage(1).get("content")).contains("rejected");
        verify(registrationService, never()).register(anyString(), any(), any());
    }

    @Test
    void refusesAnOperationThatIsNotAvailableAndTellsTheAssistant() {
        provider.then(asks(new AgentProvider.ProviderCall("1", null)))
                .then(says("No puedo borrar hechos: solo consulto tu historia y registro hechos médicos."));

        AgentTurn turn = runner(MAX_OPERATIONS).run("Borra todos mis registros.", CONTEXT);

        assertThat(turn.failed()).isFalse();
        assertThat(turn.message()).startsWith("No puedo borrar");
        assertThat(turn.completedOperations()).isEmpty();
        assertThat((String) toolMessage(1).get("content")).contains("rejected");
        verifyNoInteractions(registrationService, historyQueryService);
    }

    @Test
    void startsNothingOnItsOwnAndStopsOnceTheTurnIsAnswered() {
        provider.then(says("Hola, ¿en qué te ayudo?"));

        AgentTurn turn = runner(MAX_OPERATIONS).run("Hola", CONTEXT);

        assertThat(turn.message()).isEqualTo("Hola, ¿en qué te ayudo?");
        assertThat(turn.operations()).isEmpty();
        assertThat(turn.failed()).isFalse();
        assertThat(provider.received()).hasSize(1);
        assertThat(provider.received().get(0)).singleElement()
                .satisfies(message -> assertThat(message.get("role")).isEqualTo("user"));
        verifyNoInteractions(registrationService, historyQueryService);
    }

    private AgentTurnRunner runner(int maxOperations) {
        return new AgentTurnRunner(
                provider,
                new AgentOperationExecutor(
                        historyQueryService, registrationService, new ClinicalEventIntentValidator()),
                maxOperations);
    }

    /** The tool message of the conversation the provider received in the given round. */
    private Map<String, Object> toolMessage(int round) {
        return provider.received().get(round).stream()
                .filter(message -> "tool".equals(message.get("role")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("the round carried no tool result"));
    }

    private static AgentProvider.ProviderTurn asks(AgentProvider.ProviderCall... calls) {
        return new AgentProvider.ProviderTurn(Map.of("role", "assistant", "content", ""), List.of(calls), null);
    }

    private static AgentProvider.ProviderTurn says(String message) {
        return new AgentProvider.ProviderTurn(Map.of("role", "assistant", "content", message), List.of(), message);
    }

    private static AgentProvider.ProviderCall call(String id, AgentOperation operation, String json) {
        return new AgentProvider.ProviderCall(id, new AgentOperationCall(operation, arguments(json)));
    }

    private static JsonNode arguments(String json) {
        try {
            return new ObjectMapper().readTree(json);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static ClinicalAnswerResult answered() {
        return ClinicalAnswerResult.answered(List.of(event()), "Esto es lo que consta.");
    }

    private static ClinicalEventRegistrationResult registered() {
        return new ClinicalEventRegistrationResult(
                ClinicalEventRegistrationResult.Kind.REGISTERED, List.of(event()), "He registrado el hecho.");
    }

    private static ClinicalEvent event() {
        return new ClinicalEvent(
                UUID.randomUUID(),
                "evt_001",
                ClinicalEvent.ClinicalEventType.DIAGNOSIS,
                LocalDate.of(2024, 3, 1),
                ClinicalEvent.DatePrecision.EXACT,
                null,
                "Hipertensión diagnosticada.",
                ClinicalEvent.EventSource.PATIENT,
                OffsetDateTime.now(ZoneOffset.UTC));
    }

    /** A provider that answers with a scripted sequence and fails when it runs out. */
    private static final class ScriptedProvider implements AgentProvider {

        private final Deque<ProviderTurn> script = new ArrayDeque<>();
        private final List<List<Map<String, Object>>> received = new ArrayList<>();

        ScriptedProvider then(ProviderTurn turn) {
            script.addLast(turn);
            return this;
        }

        @Override
        public ProviderTurn send(List<Map<String, Object>> conversation) {
            received.add(List.copyOf(conversation));
            if (script.isEmpty()) {
                throw new LlmIntegrationException("the assistant is not available");
            }
            return script.removeFirst();
        }

        List<List<Map<String, Object>>> received() {
            return received;
        }
    }
}
