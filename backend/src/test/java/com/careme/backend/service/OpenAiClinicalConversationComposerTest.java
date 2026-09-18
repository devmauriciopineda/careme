package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.client.RestClient;

/**
 * UC-010: the conversational reply is composed by the provider, from the message
 * and the active turns only, and the prompt it is composed with carries the
 * clinical limits.
 */
class OpenAiClinicalConversationComposerTest {

    private static final String PROMPT_RESOURCE = "prompts/conversation-reply-v1.txt";

    private HttpServer server;
    private final AtomicReference<String> lastRequestBody = new AtomicReference<>("");

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
    void composesTheReplyTheProviderReturned() throws Exception {
        respondWithContent("{\\\"reply\\\":\\\"Es una condición que se mide en consulta.\\\"}");

        assertThat(composer().compose("¿Qué es la hipertensión?", List.of()))
                .isEqualTo("Es una condición que se mide en consulta.");
    }

    @Test
    void sendsTheMessageAndTheRecentTurnsOnly() throws Exception {
        respondWithContent("{\\\"reply\\\":\\\"Te respondo como conversación.\\\"}");

        composer().compose("¿Y eso?", List.of("Usuario: Hola | Asistente: Hola."));

        assertThat(lastRequestBody.get()).contains("¿Y eso?");
        assertThat(lastRequestBody.get()).contains("Usuario: Hola");
        assertThat(lastRequestBody.get())
                .as("el compositor no recibe ningún hecho clínico que pueda presentar como registrado")
                .doesNotContain("Hechos recuperados")
                .doesNotContain("references")
                .doesNotContain("coverage");
    }

    @Test
    void sendsTheVersionedConversationPrompt() throws Exception {
        respondWithContent("{\\\"reply\\\":\\\"Te respondo como conversación.\\\"}");

        composer().compose("Hola", List.of());

        assertThat(lastRequestBody.get())
                .as("el prompt conversacional viaja como mensaje de sistema")
                .contains("Devuelve solo JSON");
    }

    @Test
    void declaresTheClinicalLimitsInThePrompt() throws IOException {
        String prompt = read(PROMPT_RESOURCE);

        assertThat(prompt).contains("nunca lo apliques");
        assertThat(prompt).contains("al caso concreto de la persona");
        assertThat(prompt).contains("declina la petición de forma explícita");
        assertThat(prompt).contains("no recomiendes un");
        assertThat(prompt).contains("No afirmes nada sobre la historia clínica");
    }

    @Test
    void refusesAReplyWithoutAMessage() {
        assertThatThrownBy(() -> composer().compose("  ", List.of()))
                .isInstanceOf(LlmIntegrationException.class)
                .hasMessage("There is nothing to reply to");
    }

            @Test
            void refusesAReplyWithNullMessageOrApiKey() throws Exception {
            assertThatThrownBy(() -> composer().compose(null, List.of()))
                .isInstanceOf(LlmIntegrationException.class)
                .hasMessage("There is nothing to reply to");
            assertThatThrownBy(() -> new OpenAiClinicalConversationComposer(
                RestClient.builder(), new ObjectMapper(), "http://localhost", " ", "model",
                Duration.ofSeconds(1), Duration.ofSeconds(1), new ClassPathResource(PROMPT_RESOURCE)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CAREME_LLM_API_KEY");
            }

    @Test
    void rejectsAnEmptyReply() throws Exception {
        respondWithContent("{\\\"reply\\\":\\\"   \\\"}");

        assertThatThrownBy(() -> composer().compose("Hola", List.of()))
                .isInstanceOf(LlmIntegrationException.class)
                .hasMessage("LLM response does not match the conversation reply schema");
    }

    @Test
    void rejectsAMalformedResponse() throws Exception {
        respondWithContent("not-json");

        assertThatThrownBy(() -> composer().compose("Hola", List.of()))
                .isInstanceOf(LlmIntegrationException.class)
                .hasMessage("LLM response does not match the conversation reply schema");
    }

    @Test
    void mapsProviderErrorsToControlledFailures() throws Exception {
        respond(503, "provider unavailable");

        assertThatThrownBy(() -> composer().compose("Hola", List.of()))
                .isInstanceOf(LlmIntegrationException.class)
                .hasMessage("LLM provider request failed");
    }

    private OpenAiClinicalConversationComposer composer() {
        try {
            return new OpenAiClinicalConversationComposer(
                    RestClient.builder(),
                    new ObjectMapper(),
                    "http://localhost:" + server.getAddress().getPort(),
                    "test-key",
                    "deepseek-flash",
                    Duration.ofSeconds(1),
                    Duration.ofSeconds(1),
                    new ClassPathResource(PROMPT_RESOURCE));
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static String read(String resource) throws IOException {
        try (InputStream input = new ClassPathResource(resource).getInputStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void respond(int status, String body) {
        server.createContext("/chat/completions", exchange -> {
            lastRequestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] response = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, response.length);
            try (var output = exchange.getResponseBody()) {
                output.write(response);
            }
        });
    }

    /** Answers with a DeepSeek-shaped envelope carrying {@code content}. */
    private void respondWithContent(String content) {
        respond(200, "{\"choices\":[{\"message\":{\"content\":\"" + content + "\"}}]}");
    }
}
