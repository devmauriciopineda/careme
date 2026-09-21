package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.careme.backend.dto.AgentOperation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AgentToolContractTest {

    private static final LocalDate REFERENCE_DATE = LocalDate.of(2026, 9, 19);
    private static final ZoneId ZONE = ZoneId.of("Europe/Madrid");

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void declaresEveryAvailableOperationToTheAssistant() {
        assertThat(AgentToolContract.tools()).hasSameSizeAs(AgentOperation.values());
        assertThat(AgentToolContract.tools())
                .allSatisfy(tool -> assertThat(tool.get("type")).isEqualTo("function"));
        assertThat(names()).containsExactlyInAnyOrder("consult_history", "record_note", "record_measurement");
    }

    @Test
    void declaresEachOperationWithItsDescriptionAndItsArguments() {
        assertThat(AgentToolContract.tools()).allSatisfy(tool -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> function = (Map<String, Object>) tool.get("function");
            assertThat(function.get("description")).asString().isNotBlank();
            assertThat(function.get("parameters")).isInstanceOf(Map.class);
        });
    }

    @Test
    void declaresNoStorageDetailToTheAssistant() {
        String declaration = AgentToolContract.tools().toString();
        assertThat(declaration).doesNotContain("data/", ".md", "filesystem", "path", "Markdown");
    }

    @Test
    void carriesTheReferenceDateAndTheTimezoneOfTheTurn() {
        String content = AgentToolContract.userContent("¿qué diagnósticos tengo?", REFERENCE_DATE, ZONE, List.of());

        assertThat(content).contains("Fecha de referencia: 2026-09-19");
        assertThat(content).contains("Zona horaria: Europe/Madrid");
        assertThat(content).endsWith("Mensaje: ¿qué diagnósticos tengo?");
    }

    @Test
    void carriesTheRecentTurnsWhenTheConversationHasThem() {
        String content = AgentToolContract.userContent(
                "¿y el año pasado?", REFERENCE_DATE, ZONE, List.of("Usuario: ¿qué diagnósticos tengo?"));

        assertThat(content).contains("Turnos recientes: Usuario: ¿qué diagnósticos tengo?");
    }

    @Test
    void readsTheOperationsTheAssistantAskedForInOrder() throws Exception {
        JsonNode message = objectMapper.readTree("""
                {"role":"assistant","content":null,"tool_calls":[
                  {"id":"1","type":"function","function":{"name":"record_note",
                   "arguments":"{\\"type\\":\\"diagnosis\\",\\"content\\":\\"Hipertensión.\\",\\"date_precision\\":\\"exact\\",\\"date\\":\\"2024-03-01\\"}"}},
                  {"id":"2","type":"function","function":{"name":"consult_history",
                   "arguments":"{\\"question\\":\\"¿qué medicación tomo?\\",\\"search_terms\\":[\\"medicacion\\"]}"}}
                ]}
                """);

        List<AgentOperationCall> calls = AgentToolContract.toolCalls(message);

        assertThat(calls).hasSize(2);
        assertThat(calls.get(0).operation()).isEqualTo(AgentOperation.RECORD_NOTE);
        assertThat(calls.get(1).operation()).isEqualTo(AgentOperation.CONSULT_HISTORY);
        assertThat(calls.get(0).arguments().path("content").asText()).isEqualTo("Hipertensión.");
    }

    @Test
    void readsNoCallForAnOperationThatIsNotAvailable() throws Exception {
        JsonNode message = objectMapper.readTree("""
                {"content":null,"tool_calls":[
                  {"id":"1","type":"function","function":{"name":"delete_event","arguments":"{}"}}
                ]}
                """);

        assertThat(AgentToolContract.toolCalls(message)).isEmpty();
    }

    @Test
    void readsAWholeTurnThatAskedForNothing() throws Exception {
        JsonNode message = objectMapper.readTree("{\"role\":\"assistant\",\"content\":\"Hola, en qué te ayudo\"}");

        assertThat(AgentToolContract.toolCalls(message)).isEmpty();
        assertThat(AgentToolContract.finalMessage(message)).isEqualTo("Hola, en qué te ayudo");
    }

    @Test
    void reportsNoMessageWhenTheAssistantProducedOnlyCalls() throws Exception {
        JsonNode message = objectMapper.readTree("{\"role\":\"assistant\",\"content\":\"\"}");

        assertThat(AgentToolContract.finalMessage(message)).isNull();
    }

    @Test
    void treatsArgumentsThatCannotBeReadAsAbsent() throws Exception {
        JsonNode message = objectMapper.readTree("""
                {"content":null,"tool_calls":[
                  {"id":"1","type":"function","function":{"name":"consult_history","arguments":"no es json"}}
                ]}
                """);

        List<AgentOperationCall> calls = AgentToolContract.toolCalls(message);

        assertThat(calls).hasSize(1);
        assertThat(calls.get(0).arguments().isEmpty()).isTrue();
    }

    private List<String> names() {
        return AgentToolContract.tools().stream()
                .map(tool -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> function = (Map<String, Object>) tool.get("function");
                    return (String) function.get("name");
                })
                .toList();
    }
}
