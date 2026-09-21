package com.careme.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Composes the measurement answer with the real provider.
 *
 * <p>It receives the measurements already formatted by the application and is asked
 * to reproduce them verbatim. The caller still checks the answer against those
 * measurements, so a value the provider rounds or converts can never reach the
 * person.
 */
@Service
@ConditionalOnProperty(name = "careme.llm.mode", havingValue = "openai")
public class OpenAiMeasurementAnswerComposer implements MeasurementAnswerComposer {

    private final RestClient client;
    private final ObjectMapper objectMapper;
    private final String model;
    private final String prompt;

    public OpenAiMeasurementAnswerComposer(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${careme.llm.base-url:https://api.deepseek.com}") String baseUrl,
            @Value("${CAREME_LLM_API_KEY:}") String apiKey,
            @Value("${careme.llm.model:deepseek-flash}") String model,
            @Value("${careme.llm.connect-timeout:PT5S}") Duration connectTimeout,
            @Value("${careme.llm.read-timeout:PT30S}") Duration readTimeout,
            @Value("classpath:prompts/measurement-answer-v1.txt")
                    org.springframework.core.io.Resource promptResource)
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
        this.prompt = new String(
                promptResource.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
    }

    @Override
    public String compose(String question, List<MeasurementFact> facts, boolean interpretationRequested) {
        if (facts == null || facts.isEmpty()) {
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
                        java.util.Map.of(
                                "role", "user", "content", userContent(question, facts, interpretationRequested)))))
                .retrieve()
                    .body(String.class);
        } catch (RestClientException exception) {
            throw new LlmIntegrationException("LLM provider request failed", exception);
        }

        try {
            JsonNode responseNode = objectMapper.readTree(response);
            String content = responseNode.path("choices").path(0).path("message").path("content").asText();
            JsonNode parsed = objectMapper.readTree(content);
            String answer = parsed.path("answer").asText("");
            if (answer.isBlank()) {
                throw new LlmIntegrationException("LLM response carries no measurement answer");
            }
            return answer;
        } catch (LlmIntegrationException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new LlmIntegrationException(
                    "LLM response does not match the measurement answer schema", exception);
        }
    }

    private static String userContent(
            String question, List<MeasurementFact> facts, boolean interpretationRequested) {
        String listed = facts.stream().map(MeasurementFact::text).collect(Collectors.joining("\n"));
        return "Pregunta: " + (question == null ? "" : question.trim())
                + "\nMediciones registradas:\n" + listed
                + "\nPidió interpretación o recomendación: " + (interpretationRequested ? "sí" : "no");
    }
}
