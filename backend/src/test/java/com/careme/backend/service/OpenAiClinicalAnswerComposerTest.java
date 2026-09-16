package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.careme.backend.entity.ClinicalEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.client.RestClient;

class OpenAiClinicalAnswerComposerTest {

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
    void composesAnAnswerThatCitesRetrievedEvents() throws Exception {
        respondWithContent("{\\\"coverage\\\":\\\"full\\\",\\\"answer\\\":\\\"Te la diagnosticaron el 10 de enero\\\",\\\"references\\\":[\\\"evt_001\\\"]}");

        var composed = composer().compose("¿Cuándo?", List.of(event("evt_001", "Hipertensión")));

        assertThat(composed.text()).isEqualTo("Te la diagnosticaron el 10 de enero");
        assertThat(composed.references()).containsExactly("evt_001");
        assertThat(composed.unsupported()).as("la pregunta quedó cubierta por completo").isNull();
    }

    @Test
    void reportsThePartOfTheQuestionTheRetrievedEventsDoNotSupport() throws Exception {
        respondWithContent("{\\\"coverage\\\":\\\"partial\\\",\\\"answer\\\":\\\"Te la diagnosticaron el 10 de enero\\\","
                + "\\\"references\\\":[\\\"evt_001\\\"],\\\"unsupported\\\":\\\"lo que pasó el año pasado\\\"}");

        var composed = composer().compose("¿Cuándo me la diagnosticaron y qué pasó el año pasado?",
                List.of(event("evt_001", "Hipertensión")));

        assertThat(composed.text()).isEqualTo("Te la diagnosticaron el 10 de enero");
        assertThat(composed.unsupported()).isEqualTo("lo que pasó el año pasado");
    }

    @Test
    void treatsABlankUnsupportedPartAsAFullAnswer() throws Exception {
        respondWithContent("{\\\"coverage\\\":\\\"full\\\",\\\"answer\\\":\\\"Te la diagnosticaron el 10 de enero\\\","
                + "\\\"references\\\":[\\\"evt_001\\\"],\\\"unsupported\\\":\\\"   \\\"}");

        var composed = composer().compose("¿Cuándo?", List.of(event("evt_001", "Hipertensión")));

        assertThat(composed.unsupported()).isNull();
    }

    @Test
    void acceptsAnAnswerWithoutReferences() throws Exception {
        respondWithContent("{\\\"coverage\\\":\\\"full\\\",\\\"answer\\\":\\\"No lo sé\\\",\\\"references\\\":[]}");

        var composed = composer().compose("¿Cuándo?", List.of(event("evt_001", "Hipertensión")));

        assertThat(composed.references()).isEmpty();
    }

    @Test
    void rejectsAnInventedReference() {
        respondWithContent("{\\\"coverage\\\":\\\"full\\\",\\\"answer\\\":\\\"Algo\\\",\\\"references\\\":[\\\"evt_999\\\"]}");

        var events = List.of(event("evt_001", "Hipertensión"));

        assertThatThrownBy(() -> composer().compose("¿Cuándo?", events))
                .isInstanceOf(LlmIntegrationException.class)
                .hasMessage("Composed answer cites an event outside the retrieved set");
    }

    @Test
    void rejectsAMalformedResponse() {
        respondWithContent("not-json");

        var events = List.of(event("evt_001", "Hipertensión"));

        assertThatThrownBy(() -> composer().compose("¿Cuándo?", events))
                .isInstanceOf(LlmIntegrationException.class)
                .hasMessage("LLM response does not match the clinical answer schema");
    }

    @Test
    void refusesToComposeFromAnEmptySet() {
        assertThatThrownBy(() -> composer().compose("¿Cuándo?", List.of()))
                .isInstanceOf(LlmIntegrationException.class)
                .hasMessage("There is nothing to answer with");
    }

    @Test
    void reportsNoSupportWhenTheRetrievedEventsDoNotAnswerTheQuestion() throws Exception {
        respondWithContent("{\\\"coverage\\\":\\\"none\\\",\\\"answer\\\":\\\"Los hechos no responden\\\","
                + "\\\"references\\\":[]}");

        var composed = composer().compose("¿He tenido migrañas?", List.of(event("evt_001", "Hipertensión")));

        assertThat(composed.coverage()).isEqualTo(ClinicalAnswerComposer.Coverage.NONE);
        assertThat(composed.references()).as("ningún hecho sostiene una respuesta").isEmpty();
    }

    @Test
    void dropsCitedReferencesWhenTheEventsDoNotAnswer() throws Exception {
        respondWithContent("{\\\"coverage\\\":\\\"none\\\",\\\"answer\\\":\\\"Los hechos no responden\\\","
                + "\\\"references\\\":[\\\"evt_001\\\"]}");

        var composed = composer().compose("¿He tenido migrañas?", List.of(event("evt_001", "Hipertensión")));

        assertThat(composed.coverage()).isEqualTo(ClinicalAnswerComposer.Coverage.NONE);
        assertThat(composed.references()).isEmpty();
    }

    @Test
    void rejectsAResponseWithoutCoverage() {
        respondWithContent("{\\\"answer\\\":\\\"Algo\\\",\\\"references\\\":[\\\"evt_001\\\"]}");

        var events = List.of(event("evt_001", "Hipertensión"));

        assertThatThrownBy(() -> composer().compose("¿Cuándo?", events))
                .isInstanceOf(LlmIntegrationException.class)
                .hasMessage("LLM response does not report the answer coverage");
    }

    @Test
    void rejectsAnUnknownCoverage() {
        respondWithContent("{\\\"coverage\\\":\\\"quizá\\\",\\\"answer\\\":\\\"Algo\\\","
                + "\\\"references\\\":[\\\"evt_001\\\"]}");

        var events = List.of(event("evt_001", "Hipertensión"));

        assertThatThrownBy(() -> composer().compose("¿Cuándo?", events))
                .isInstanceOf(LlmIntegrationException.class)
                .hasMessage("LLM response does not report the answer coverage");
    }

    private OpenAiClinicalAnswerComposer composer() {
        try {
            return new OpenAiClinicalAnswerComposer(
                    RestClient.builder(),
                    new ObjectMapper(),
                    "http://localhost:" + server.getAddress().getPort(),
                    "test-key",
                    "deepseek-flash",
                    Duration.ofSeconds(1),
                    Duration.ofSeconds(1),
                    new ClassPathResource("prompts/clinical-answer-v3.txt"));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private void respondWithContent(String contentJson) {
        String body = "{\"choices\":[{\"message\":{\"content\":\"" + contentJson + "\"}}]}";
        server.createContext("/chat/completions", exchange -> {
            byte[] response = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (var output = exchange.getResponseBody()) {
                output.write(response);
            }
        });
    }

    private static ClinicalEvent event(String code, String content) {
        return new ClinicalEvent(
                UUID.randomUUID(),
                code,
                ClinicalEvent.ClinicalEventType.DIAGNOSIS,
                LocalDate.of(2026, 1, 10),
                ClinicalEvent.DatePrecision.EXACT,
                "el 10 de enero",
                content,
                ClinicalEvent.EventSource.PATIENT,
                OffsetDateTime.now(ZoneOffset.UTC));
    }
}
