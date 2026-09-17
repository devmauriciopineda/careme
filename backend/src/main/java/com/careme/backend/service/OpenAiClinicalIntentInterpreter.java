package com.careme.backend.service;

import com.careme.backend.dto.ClinicalEventIntent;
import com.careme.backend.entity.ClinicalEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@ConditionalOnProperty(name = "careme.llm.mode", havingValue = "openai")
public class OpenAiClinicalIntentInterpreter implements ClinicalIntentInterpreter {

    private final RestClient client;
    private final ObjectMapper objectMapper;
    private final String model;
    private final String prompt;

    public OpenAiClinicalIntentInterpreter(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${careme.llm.base-url:https://api.deepseek.com}") String baseUrl,
            @Value("${CAREME_LLM_API_KEY:}") String apiKey,
            @Value("${careme.llm.model:deepseek-flash}") String model,
            @Value("${careme.llm.connect-timeout:PT5S}") Duration connectTimeout,
            @Value("${careme.llm.read-timeout:PT30S}") Duration readTimeout,
            @Value("classpath:prompts/clinical-intent-v3.txt") org.springframework.core.io.Resource promptResource)
            throws java.io.IOException {
        if (apiKey.isBlank()) {
            throw new IllegalStateException("CAREME_LLM_API_KEY is required when CAREME_LLM_MODE=openai");
        }
        var httpClient = java.net.http.HttpClient.newBuilder()
            .connectTimeout(connectTimeout)
            .build();
        var requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);
        this.client = restClientBuilder
                .baseUrl(baseUrl)
            .requestFactory(requestFactory)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.objectMapper = objectMapper;
        this.model = model;
        this.prompt = new String(promptResource.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
    }

    @Override
    public ClinicalEventIntent interpret(String message, InterpretationContext context) {
        String response;
        try {
            response = client.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(java.util.Map.of(
                    "model", model,
                    "temperature", 0,
                    "response_format", java.util.Map.of("type", "json_object"),
                    "messages", List.of(
                        java.util.Map.of("role", "system", "content", prompt),
                        java.util.Map.of("role", "user", "content", userContent(message, context)))))
                .retrieve()
                    .body(String.class);
        } catch (RestClientException exception) {
            throw new LlmIntegrationException("LLM provider request failed", exception);
        }

        try {
                JsonNode responseNode = objectMapper.readTree(response);
                String content = responseNode.path("choices").path(0).path("message").path("content").asText();
                return parseIntent(objectMapper.readTree(content));
        } catch (Exception exception) {
            throw new LlmIntegrationException("LLM response does not match the clinical intent schema", exception);
        }
    }

    /** Adds the active conversation's recent turns so a follow-up question keeps its referents. */
    private static String userContent(String message, InterpretationContext context) {
        StringBuilder content = new StringBuilder()
                .append("Fecha de referencia: ").append(context.referenceDate())
                .append(". Zona horaria: ").append(context.zoneId());
        if (!context.recentTurns().isEmpty()) {
            content.append(". Turnos recientes: ").append(String.join(" || ", context.recentTurns()));
        }
        return content.append(". Mensaje: ").append(message).toString();
    }

    private ClinicalEventIntent parseIntent(JsonNode node) {
        String kind = node.path("kind").asText("").toLowerCase();
        return switch (kind) {
            case "events" -> new ClinicalEventIntent(
                    ClinicalEventIntent.Kind.EVENTS,
                    parseCandidates(node.path("events")),
                    null);
            case "query" -> ClinicalEventIntent.query(parseQuery(node));
            case "clarification" -> ClinicalEventIntent.clarification(node.path("clarification").asText());
            case "conversation" -> ClinicalEventIntent.conversation();
            default -> throw new IllegalArgumentException("Unknown clinical intent kind");
        };
    }

    private ClinicalEventIntent.Query parseQuery(JsonNode node) {
        String question = node.path("question").isTextual() ? node.path("question").asText() : null;
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Query intent must contain a question");
        }
        return new ClinicalEventIntent.Query(
                question,
                parseScope(node.path("scope").asText(null)),
                parseSearchTerms(node.path("search_terms")),
                parseType(node.path("type")),
                parseDate(node.path("date_from")),
                parseDate(node.path("date_to")),
                parseGeneralPart(node.path("general_part")));
    }

    /**
     * Reads the part of the message that does not depend on the clinical history.
     * It stays apart from the search criteria, so the general part of a mixed
     * message is never used to retrieve events.
     */
    private static String parseGeneralPart(JsonNode node) {
        return node.isTextual() && !node.asText().isBlank() ? node.asText().trim() : null;
    }

    private static ClinicalEventIntent.Query.Scope parseScope(String value) {
        if (value == null || value.isBlank()) {
            return ClinicalEventIntent.Query.Scope.HISTORY;
        }
        return ClinicalEventIntent.Query.Scope.valueOf(value.toUpperCase());
    }

    private static List<String> parseSearchTerms(JsonNode terms) {
        List<String> searchTerms = new ArrayList<>();
        if (terms.isArray()) {
            terms.forEach(term -> {
                if (term.isTextual() && !term.asText().isBlank()) {
                    searchTerms.add(term.asText().trim());
                }
            });
        }
        return searchTerms;
    }

    private static ClinicalEvent.ClinicalEventType parseType(JsonNode type) {
        String value = type.isTextual() ? type.asText() : null;
        return value == null || value.isBlank()
                ? null
                : ClinicalEvent.ClinicalEventType.valueOf(value.toUpperCase());
    }

    private static LocalDate parseDate(JsonNode date) {
        String value = date.isTextual() ? date.asText() : null;
        return value == null || value.isBlank() ? null : LocalDate.parse(value);
    }

    private List<ClinicalEventIntent.Candidate> parseCandidates(JsonNode events) {
        if (!events.isArray() || events.isEmpty()) {
            throw new IllegalArgumentException("Events intent must contain candidates");
        }
        List<ClinicalEventIntent.Candidate> candidates = new ArrayList<>();
        for (JsonNode event : events) {
            ClinicalEvent.ClinicalEventType type = ClinicalEvent.ClinicalEventType.valueOf(
                    event.path("type").asText("").toUpperCase());
            String dateValue = event.path("date").isNull() ? null : event.path("date").asText(null);
            LocalDate date = dateValue == null || dateValue.isBlank() ? null : LocalDate.parse(dateValue);
            ClinicalEvent.DatePrecision precision = ClinicalEvent.DatePrecision.valueOf(
                    event.path("date_precision").asText("").toUpperCase());
            candidates.add(new ClinicalEventIntent.Candidate(
                    type,
                    event.path("content").asText(),
                    date,
                    precision,
                    event.path("date_text").isNull() ? null : event.path("date_text").asText(null)));
        }
        return candidates;
    }
}