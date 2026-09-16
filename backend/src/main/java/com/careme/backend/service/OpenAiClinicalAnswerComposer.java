package com.careme.backend.service;

import com.careme.backend.entity.ClinicalEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class OpenAiClinicalAnswerComposer implements ClinicalAnswerComposer {

    private final RestClient client;
    private final ObjectMapper objectMapper;
    private final String model;
    private final String prompt;

    public OpenAiClinicalAnswerComposer(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${careme.llm.base-url:https://api.deepseek.com}") String baseUrl,
            @Value("${CAREME_LLM_API_KEY:}") String apiKey,
            @Value("${careme.llm.model:deepseek-flash}") String model,
            @Value("${careme.llm.connect-timeout:PT5S}") Duration connectTimeout,
            @Value("${careme.llm.read-timeout:PT30S}") Duration readTimeout,
            @Value("classpath:prompts/clinical-answer-v3.txt") org.springframework.core.io.Resource promptResource)
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
    public ComposedAnswer compose(String question, List<ClinicalEvent> events) {
        if (events == null || events.isEmpty()) {
            throw new LlmIntegrationException("There is nothing to answer with");
        }
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
                        java.util.Map.of("role", "user", "content", userContent(question, events)))))
                .retrieve()
                    .body(String.class);
        } catch (RestClientException exception) {
            throw new LlmIntegrationException("LLM provider request failed", exception);
        }

        try {
            JsonNode responseNode = objectMapper.readTree(response);
            String content = responseNode.path("choices").path(0).path("message").path("content").asText();
            JsonNode parsed = objectMapper.readTree(content);
            List<String> references = new ArrayList<>();
            JsonNode referenceNodes = parsed.path("references");
            if (referenceNodes.isArray()) {
                referenceNodes.forEach(reference -> {
                    if (reference.isTextual()) {
                        references.add(reference.asText());
                    }
                });
            }
            validateReferences(references, events);
            JsonNode unsupportedNode = parsed.path("unsupported");
            String unsupported = unsupportedNode.isTextual() && !unsupportedNode.asText().isBlank()
                    ? unsupportedNode.asText().trim()
                    : null;
            ClinicalAnswerComposer.Coverage coverage = coverageOf(parsed);
            // Events that answer nothing support nothing: the answer reports no
            // reference, so a retrieved fact can never appear as its support.
            return new ComposedAnswer(
                    parsed.path("answer").asText(""),
                    coverage == ClinicalAnswerComposer.Coverage.NONE ? List.of() : references,
                    coverage,
                    unsupported);
        } catch (LlmIntegrationException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new LlmIntegrationException("LLM response does not match the clinical answer schema", exception);
        }
    }

    /**
     * Reads how much of the question the provider says the retrieved events
     * support. The field is part of the response contract, so an unrecognized or
     * missing value is a controlled failure rather than a guess: guessing "full"
     * would present an unsupported answer and guessing "none" would deny an answer
     * the user did have.
     */
    private static ClinicalAnswerComposer.Coverage coverageOf(JsonNode parsed) {
        String reported = parsed.path("coverage").asText("");
        return switch (reported.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "full" -> ClinicalAnswerComposer.Coverage.FULL;
            case "partial" -> ClinicalAnswerComposer.Coverage.PARTIAL;
            case "none" -> ClinicalAnswerComposer.Coverage.NONE;
            default -> throw new LlmIntegrationException("LLM response does not report the answer coverage");
        };
    }

    /** Every cited event must belong to the retrieved set or the answer is not grounded. */
    private static void validateReferences(List<String> references, List<ClinicalEvent> events) {
        List<String> retrieved = events.stream().map(ClinicalEvent::code).toList();
        for (String reference : references) {
            if (!retrieved.contains(reference)) {
                throw new LlmIntegrationException("Composed answer cites an event outside the retrieved set");
            }
        }
    }

    private static String userContent(String question, List<ClinicalEvent> events) {
        StringBuilder content = new StringBuilder("Pregunta: ").append(question).append("\nHechos recuperados:\n");
        for (ClinicalEvent event : events) {
            content.append("- ").append(event.code())
                .append(" | tipo: ").append(event.type().name().toLowerCase())
                .append(" | fecha: ").append(event.date() == null ? "" : event.date())
                .append(" | precision: ").append(event.datePrecision().name().toLowerCase())
                .append(" | contenido: ").append(event.content())
                .append("\n");
        }
        return content.toString();
    }
}
