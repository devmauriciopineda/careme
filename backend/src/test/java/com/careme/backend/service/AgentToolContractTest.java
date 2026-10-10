package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.careme.backend.dto.AgentOperation;
import com.careme.backend.entity.PatientProfile;
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
        assertThat(names())
                .containsExactlyInAnyOrder(
                        "consult_history", "consult_measurements", "record_note", "record_measurement",
                        "record_profile");
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
    void carriesOnlyTheMissingProfileFieldsToTheAssistant() {
        String content = AgentToolContract.userContent(
                "Hola", REFERENCE_DATE, ZONE, List.of(),
                new PatientProfile("Ana", null, null, null, null, null, null));

        assertThat(content).contains("Perfil vigente: faltan date_of_birth, sex, occupation, blood_group, rh, height_cm");
        assertThat(content).doesNotContain("Ana");
    }

    @Test
    void reportsEachSingleMissingProfileFieldOnItsOwn() {
        assertThat(profileWithMissing("name")).contains("faltan name");
        assertThat(profileWithMissing("date_of_birth")).contains("faltan date_of_birth");
        assertThat(profileWithMissing("sex")).contains("faltan sex");
        assertThat(profileWithMissing("occupation")).contains("faltan occupation");
        assertThat(profileWithMissing("blood_group")).contains("faltan blood_group");
        assertThat(profileWithMissing("rh")).contains("faltan rh");
        assertThat(profileWithMissing("height_cm")).contains("faltan height_cm");
    }

    private static String profileWithMissing(String missing) {
        PatientProfile profile = new PatientProfile(
                missing.equals("name") ? null : "Ana",
                missing.equals("date_of_birth") ? null : LocalDate.of(1984, 2, 3),
                missing.equals("sex") ? null : "mujer",
                missing.equals("occupation") ? null : "Médica",
                missing.equals("blood_group") ? null : "O",
                missing.equals("rh") ? null : "positivo",
                missing.equals("height_cm") ? null : new java.math.BigDecimal("170"));
        return AgentToolContract.userContent("Hola", REFERENCE_DATE, ZONE, List.of(), profile);
    }

    @Test
    void reportsACompleteProfileWithoutNamingTheFields() {
        String content = AgentToolContract.userContent(
                "Hola", REFERENCE_DATE, ZONE, List.of(),
                new PatientProfile(
                        "Ana", LocalDate.of(1984, 2, 3), "mujer", "Médica", "O", "positivo",
                        new java.math.BigDecimal("170")));

        assertThat(content).contains("Perfil vigente: completo");
        assertThat(content).doesNotContain("Ana");
    }

    @Test
    void carriesNoProfileWhenThereIsNoneToCarry() {
        String content = AgentToolContract.userContent(
                "Hola", REFERENCE_DATE, ZONE, List.of(), null);

        assertThat(content).doesNotContain("Perfil vigente");
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
    void readsAProfileCallTheAssistantAskedFor() throws Exception {
        JsonNode message = objectMapper.readTree("""
                {"content":null,"tool_calls":[
                  {"id":"1","type":"function","function":{"name":"record_profile",
                   "arguments":"{\\"name\\":\\"Ana\\",\\"confirmed\\":true}"}}
                ]}
                """);

        List<AgentOperationCall> calls = AgentToolContract.toolCalls(message);

        assertThat(calls).singleElement()
                .satisfies(call -> assertThat(call.operation()).isEqualTo(AgentOperation.RECORD_PROFILE));
        assertThat(calls.getFirst().arguments().path("name").asText()).isEqualTo("Ana");
    }

    @Test
    void readsNoCallFromAToolCallWithoutAFunction() throws Exception {
        JsonNode message = objectMapper.readTree("{\"content\":null,\"tool_calls\":[{\"id\":\"1\"}]}");

        assertThat(AgentToolContract.toolCalls(message)).isEmpty();
        assertThat(AgentToolContract.operationCall(null)).isNull();
    }

    @Test
    void readsNoCallForAnOperationThatIsNotAvailable() throws Exception {        JsonNode message = objectMapper.readTree("""
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
