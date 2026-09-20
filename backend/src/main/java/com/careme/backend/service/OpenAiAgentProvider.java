package com.careme.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * The agent provider backed by an OpenAI-compatible API.
 *
 * <p>It declares the operation set to the provider and reads back what the
 * provider asks for: the operations to run or the response to deliver. The set
 * is the only thing the provider can ask for, so the assistant never reaches the
 * clinical history, the filesystem or any operation outside it.
 */
@Service
@ConditionalOnProperty(name = "careme.llm.mode", havingValue = "openai")
public class OpenAiAgentProvider implements AgentProvider {

    private final RestClient client;
    private final ObjectMapper objectMapper;
    private final String model;
    private final String prompt;

    public OpenAiAgentProvider(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${careme.llm.base-url:https://api.deepseek.com}") String baseUrl,
            @Value("${CAREME_LLM_API_KEY:}") String apiKey,
            @Value("${careme.llm.model:deepseek-flash}") String model,
            @Value("${careme.llm.connect-timeout:PT5S}") Duration connectTimeout,
            @Value("${careme.llm.read-timeout:PT30S}") Duration readTimeout,
            @Value("classpath:prompts/clinical-agent-v1.txt") Resource promptResource)
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
    public ProviderTurn send(List<Map<String, Object>> conversation) {
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", prompt));
        messages.addAll(conversation);

        String response;
        try {
            response = client.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "model", model,
                            "temperature", 0,
                            "tools", AgentToolContract.tools(),
                            "tool_choice", "auto",
                            "messages", messages))
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException exception) {
            throw new LlmIntegrationException("LLM provider request failed", exception);
        }

        try {
            JsonNode message = objectMapper.readTree(response).path("choices").path(0).path("message");
            return new ProviderTurn(
                    objectMapper.convertValue(message, new com.fasterxml.jackson.core.type.TypeReference<
                            Map<String, Object>>() {}),
                    calls(message),
                    AgentToolContract.finalMessage(message));
        } catch (Exception exception) {
            throw new LlmIntegrationException("The assistant response could not be read", exception);
        }
    }

    /**
     * The operations the provider asked for, paired with the identifiers its tool
     * results must answer to. A name that is not one of the available operations
     * resolves to no call, so the application rejects it instead of executing it.
     */
    private static List<ProviderCall> calls(JsonNode message) {
        List<ProviderCall> calls = new ArrayList<>();
        for (JsonNode toolCall : message.path("tool_calls")) {
            calls.add(new ProviderCall(
                    toolCall.path("id").asText(null), AgentToolContract.operationCall(toolCall)));
        }
        return calls;
    }
}
