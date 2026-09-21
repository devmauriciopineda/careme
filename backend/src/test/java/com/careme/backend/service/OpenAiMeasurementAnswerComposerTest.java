package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.client.RestClient;

/**
 * The real composition of a measurement answer: the provider frames the measurements the
 * application formatted, and a response that cannot be read is a controlled failure
 * rather than an answer.
 */
class OpenAiMeasurementAnswerComposerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
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
    void returnsTheAnswerTheProviderComposed() throws Exception {
        respondWithMessageContent(
                "{\"answer\":\"Estas son tus mediciones registradas: peso: 70.5 kg (2026-01-10).\"}");

        String answer = composer().compose("¿cuánto peso?", List.of(FakeMeasurementAnswerComposerTest.fact()), false);

        assertThat(answer).isEqualTo("Estas son tus mediciones registradas: peso: 70.5 kg (2026-01-10).");
    }

    @Test
    void failsWhenTheResponseCarriesNoAnswer() throws Exception {
        respondWithMessageContent("{}");

        assertThatThrownBy(() -> composer().compose(
                "¿cuánto peso?", List.of(FakeMeasurementAnswerComposerTest.fact()), false))
                .isInstanceOf(LlmIntegrationException.class);
    }

    @Test
    void failsWhenTheProviderIsNotReachable() throws Exception {
        server.stop(0);
        var composer = composer();

        assertThatThrownBy(() -> composer.compose(
                "¿cuánto peso?", List.of(FakeMeasurementAnswerComposerTest.fact()), false))
                .isInstanceOf(LlmIntegrationException.class);
    }

    @Test
    void refusesToAnswerWithoutMeasurements() throws Exception {
        assertThatThrownBy(() -> composer().compose("¿cuánto peso?", List.of(), false))
                .isInstanceOf(LlmIntegrationException.class);
    }

    private OpenAiMeasurementAnswerComposer composer() throws Exception {
        return new OpenAiMeasurementAnswerComposer(
                RestClient.builder(),
                objectMapper,
                "http://localhost:" + server.getAddress().getPort(),
                "test-key",
                "test-model",
                Duration.ofSeconds(5),
                Duration.ofSeconds(5),
                new ClassPathResource("prompts/measurement-answer-v1.txt"));
    }

    private void respondWithMessageContent(String content) {
        server.createContext("/chat/completions", exchange -> {
            String body = objectMapper.writeValueAsString(Map.of(
                    "choices", List.of(Map.of("message", Map.of("content", content)))));
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
    }
}
