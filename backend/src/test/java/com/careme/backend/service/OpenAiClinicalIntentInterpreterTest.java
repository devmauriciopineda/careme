package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.client.RestClient;

class OpenAiClinicalIntentInterpreterTest {

    private HttpServer server;

    @BeforeEach
    void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void mapsDeepSeekCompatibleResponse() throws Exception {
        respond(200, "{\"choices\":[{\"message\":{\"content\":\"{\\\"kind\\\":\\\"conversation\\\"}\"}}]}");

        var interpreter = interpreter(Duration.ofSeconds(1), Duration.ofSeconds(1));
        var intent = interpreter.interpret("¿Qué puedes hacer?", context());

        assertThat(intent.kind()).isEqualTo(com.careme.backend.dto.ClinicalEventIntent.Kind.CONVERSATION);
    }

    @Test
    void mapsProviderErrorsToControlledFailures() throws Exception {
        respond(503, "provider unavailable");

        var interpreter = interpreter(Duration.ofSeconds(1), Duration.ofSeconds(1));

        assertThatThrownBy(() -> interpreter.interpret("Tuve fiebre", context()))
                .isInstanceOf(LlmIntegrationException.class)
                .hasMessage("LLM provider request failed");
    }

    @Test
    void rejectsInvalidJsonWithoutReturningAnIntent() throws Exception {
        respond(200, "not-json");

        var interpreter = interpreter(Duration.ofSeconds(1), Duration.ofSeconds(1));

        assertThatThrownBy(() -> interpreter.interpret("Tuve fiebre", context()))
                .isInstanceOf(LlmIntegrationException.class)
                .hasMessage("LLM response does not match the clinical intent schema");
    }

    @Test
    void appliesReadTimeout() throws Exception {
        server.createContext("/chat/completions", exchange -> {
            try {
                Thread.sleep(250);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
            exchange.close();
        });

        var interpreter = interpreter(Duration.ofSeconds(1), Duration.ofMillis(50));

        assertThatThrownBy(() -> interpreter.interpret("Tuve fiebre", context()))
                .isInstanceOf(LlmIntegrationException.class)
                .hasMessage("LLM provider request failed");
    }

    @Test
    void mapsAQueryIntentWithScopeAndCriteria() throws Exception {
        respond(200, "{\"choices\":[{\"message\":{\"content\":\"{\\\"kind\\\":\\\"query\\\",\\\"question\\\":\\\"¿Cuándo me diagnosticaron hipertensión?\\\",\\\"scope\\\":\\\"history\\\",\\\"search_terms\\\":[\\\"hipertension\\\"],\\\"type\\\":\\\"diagnosis\\\",\\\"date_from\\\":\\\"2026-01-01\\\",\\\"date_to\\\":null}\"}}]}");

        var interpreter = interpreter(Duration.ofSeconds(1), Duration.ofSeconds(1));
        var intent = interpreter.interpret("¿Cuándo me diagnosticaron hipertensión?", context());

        assertThat(intent.kind()).isEqualTo(com.careme.backend.dto.ClinicalEventIntent.Kind.QUERY);
        assertThat(intent.events()).isEmpty();
        assertThat(intent.query().scope())
                .isEqualTo(com.careme.backend.dto.ClinicalEventIntent.Query.Scope.HISTORY);
        assertThat(intent.query().searchTerms()).containsExactly("hipertension");
        assertThat(intent.query().type())
                .isEqualTo(com.careme.backend.entity.ClinicalEvent.ClinicalEventType.DIAGNOSIS);
        assertThat(intent.query().fromDate()).isEqualTo(java.time.LocalDate.of(2026, 1, 1));
        assertThat(intent.query().toDate()).isNull();
    }

    @Test
    void mapsTheMeasurementsScope() throws Exception {
        respond(200, "{\"choices\":[{\"message\":{\"content\":\"{\\\"kind\\\":\\\"query\\\",\\\"question\\\":\\\"¿Cuánto peso?\\\",\\\"scope\\\":\\\"measurements\\\",\\\"search_terms\\\":[\\\"peso\\\"]}\"}}]}");

        var interpreter = interpreter(Duration.ofSeconds(1), Duration.ofSeconds(1));
        var intent = interpreter.interpret("¿Cuánto peso?", context());

        assertThat(intent.query().scope())
                .isEqualTo(com.careme.backend.dto.ClinicalEventIntent.Query.Scope.MEASUREMENTS);
    }

    @Test
    void rejectsAnUnknownQueryScope() throws Exception {
        respond(200, "{\"choices\":[{\"message\":{\"content\":\"{\\\"kind\\\":\\\"query\\\",\\\"question\\\":\\\"¿Cuándo?\\\",\\\"scope\\\":\\\"otro\\\",\\\"search_terms\\\":[\\\"x\\\"]}\"}}]}");

        var interpreter = interpreter(Duration.ofSeconds(1), Duration.ofSeconds(1));

        assertThatThrownBy(() -> interpreter.interpret("¿Cuándo?", context()))
                .isInstanceOf(LlmIntegrationException.class)
                .hasMessage("LLM response does not match the clinical intent schema");
    }

    @Test
    void rejectsAQueryWithoutAQuestion() throws Exception {
        respond(200, "{\"choices\":[{\"message\":{\"content\":\"{\\\"kind\\\":\\\"query\\\",\\\"scope\\\":\\\"history\\\",\\\"search_terms\\\":[\\\"x\\\"]}\"}}]}");

        var interpreter = interpreter(Duration.ofSeconds(1), Duration.ofSeconds(1));

        assertThatThrownBy(() -> interpreter.interpret("¿Cuándo?", context()))
                .isInstanceOf(LlmIntegrationException.class)
                .hasMessage("LLM response does not match the clinical intent schema");
    }

    @Test
    void mapsTheGeneralPartOfAMixedMessageWithoutUsingItAsCriteria() throws Exception {
        respond(200, "{\"choices\":[{\"message\":{\"content\":\"{\\\"kind\\\":\\\"query\\\",\\\"question\\\":\\\"¿Cuándo me la diagnosticaron?\\\",\\\"scope\\\":\\\"history\\\",\\\"search_terms\\\":[\\\"diagnosticaron\\\"],\\\"general_part\\\":\\\"¿Qué es la hipertensión?\\\"}\"}}]}");

        var interpreter = interpreter(Duration.ofSeconds(1), Duration.ofSeconds(1));
        var intent = interpreter.interpret("¿Qué es la hipertensión? ¿Cuándo me la diagnosticaron?", context());

        assertThat(intent.kind()).isEqualTo(com.careme.backend.dto.ClinicalEventIntent.Kind.QUERY);
        assertThat(intent.query().generalPart()).isEqualTo("¿Qué es la hipertensión?");
        assertThat(intent.query().question()).isEqualTo("¿Cuándo me la diagnosticaron?");
        assertThat(intent.query().searchTerms())
                .as("la parte general no se usa para recuperar hechos")
                .containsExactly("diagnosticaron");
    }

    @Test
    void leavesTheGeneralPartAbsentOnAPlainQuestion() throws Exception {
        respond(200, "{\"choices\":[{\"message\":{\"content\":\"{\\\"kind\\\":\\\"query\\\",\\\"question\\\":\\\"¿Cuándo me la diagnosticaron?\\\",\\\"scope\\\":\\\"history\\\",\\\"search_terms\\\":[\\\"diagnosticaron\\\"],\\\"general_part\\\":null}\"}}]}");

        var interpreter = interpreter(Duration.ofSeconds(1), Duration.ofSeconds(1));
        var intent = interpreter.interpret("¿Cuándo me la diagnosticaron?", context());

        assertThat(intent.query().generalPart()).isNull();
    }

    @Test
    void mapsAnUndeterminedChannelToClarification() throws Exception {
        respond(200, "{\"choices\":[{\"message\":{\"content\":\"{\\\"kind\\\":\\\"clarification\\\",\\\"clarification\\\":\\\"¿Tu pregunta se refiere a tu historia clínica o es una duda general?\\\"}\"}}]}");

        var interpreter = interpreter(Duration.ofSeconds(1), Duration.ofSeconds(1));
        var intent = interpreter.interpret("¿Eso es bueno?", context());

        assertThat(intent.kind()).isEqualTo(com.careme.backend.dto.ClinicalEventIntent.Kind.CLARIFICATION);
        assertThat(intent.query())
                .as("una duda de cauce no elige consulta ni conversación")
                .isNull();
    }

    @Test
    void keepsAColloquialQuestionInTheClinicalHistoryChannel() throws Exception {
        respond(200, "{\"choices\":[{\"message\":{\"content\":\"{\\\"kind\\\":\\\"query\\\",\\\"question\\\":\\\"¿Qué me dijeron del azúcar?\\\",\\\"scope\\\":\\\"history\\\",\\\"search_terms\\\":[\\\"azucar\\\"]}\"}}]}");

        var interpreter = interpreter(Duration.ofSeconds(1), Duration.ofSeconds(1));
        var intent = interpreter.interpret("¿Qué me dijeron del azúcar?", context());

        assertThat(intent.kind()).isEqualTo(com.careme.backend.dto.ClinicalEventIntent.Kind.QUERY);
    }

    @Test
    void declaresTheChannelRulesInThePrompt() throws Exception {
        String prompt = new String(
                new ClassPathResource("prompts/clinical-intent-v3.txt").getInputStream().readAllBytes(),
                java.nio.charset.StandardCharsets.UTF_8);

        assertThat(prompt).contains("even when the rest of the message does not");
        assertThat(prompt).contains("general_part");
        assertThat(prompt).contains("colloquial");
        assertThat(prompt).contains("never guess one");
    }

    private OpenAiClinicalIntentInterpreter interpreter(Duration connectTimeout, Duration readTimeout)
            throws Exception {
        return new OpenAiClinicalIntentInterpreter(
                RestClient.builder(),
                new ObjectMapper(),
                "http://localhost:" + server.getAddress().getPort(),
                "test-key",
                "deepseek-flash",
                connectTimeout,
                readTimeout,
                new ClassPathResource("prompts/clinical-intent-v3.txt"));
    }

    private void respond(int status, String body) {
        server.createContext("/chat/completions", exchange -> {
            byte[] response = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, response.length);
            try (var output = exchange.getResponseBody()) {
                output.write(response);
            }
        });
    }

    private ClinicalIntentInterpreter.InterpretationContext context() {
        return new ClinicalIntentInterpreter.InterpretationContext(
                java.time.LocalDate.of(2026, 9, 13), java.time.ZoneId.of("UTC"), "clinical-intent-v3");
    }
}