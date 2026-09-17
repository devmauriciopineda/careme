package com.careme.backend.service;

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

/**
 * Conversational composer backed by the configured provider.
 *
 * <p>It sends only the message and the recent turns of the active conversation.
 * No clinical event and no retrieval criterion ever reaches this call, so the
 * reply cannot be grounded in the history even by accident.
 */
@Service
@ConditionalOnProperty(name = "careme.llm.mode", havingValue = "openai")
public class OpenAiClinicalConversationComposer implements ClinicalConversationComposer {

    private final RestClient client;
    private final ObjectMapper objectMapper;
    private final String model;
    private final String prompt;

    public OpenAiClinicalConversationComposer(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${careme.llm.base-url:https://api.deepseek.com}") String baseUrl,
            @Value("${CAREME_LLM_API_KEY:}") String apiKey,
            @Value("${careme.llm.model:deepseek-flash}") String model,
            @Value("${careme.llm.connect-timeout:PT5S}") Duration connectTimeout,
            @Value("${careme.llm.read-timeout:PT30S}") Duration readTimeout,
            @Value("classpath:prompts/conversation-reply-v1.txt") org.springframework.core.io.Resource promptResource)
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
    public String compose(String message, List<String> recentTurns) {
        if (message == null || message.isBlank()) {
            throw new LlmIntegrationException("There is nothing to reply to");
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
                                    java.util.Map.of("role", "user", "content", userContent(message, recentTurns)))))
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException exception) {
            throw new LlmIntegrationException("LLM provider request failed", exception);
        }

        try {
            JsonNode responseNode = objectMapper.readTree(response);
            String content = responseNode.path("choices").path(0).path("message").path("content").asText();
            String reply = objectMapper.readTree(content).path("reply").asText("");
            if (reply.isBlank()) {
                throw new LlmIntegrationException("LLM response does not match the conversation reply schema");
            }
            return reply.trim();
        } catch (LlmIntegrationException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new LlmIntegrationException("LLM response does not match the conversation reply schema", exception);
        }
    }

    /** Carries the message and the active turns, and nothing else. */
    private static String userContent(String message, List<String> recentTurns) {
        List<String> turns = recentTurns == null ? List.of() : new ArrayList<>(recentTurns);
        StringBuilder content = new StringBuilder();
        if (!turns.isEmpty()) {
            content.append("Turnos recientes: ").append(String.join(" || ", turns)).append(". ");
        }
        return content.append("Mensaje: ").append(message).toString();
    }
}
