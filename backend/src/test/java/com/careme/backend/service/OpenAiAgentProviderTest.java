package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.client.RestClient;

/**
 * The assistant is the real one by default, so a deployment without a credential
 * must fail at startup and say why — not fall back to a simulated assistant or
 * start serving turns it cannot answer.
 */
class OpenAiAgentProviderTest {

    private static final String BASE_URL = "https://api.deepseek.com";
    private static final String MODEL = "deepseek-flash";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(30);

    @Test
    void refusesToStartWithoutTheProviderCredential() {
        assertThatThrownBy(() -> new OpenAiAgentProvider(
                        RestClient.builder(),
                        new ObjectMapper(),
                        BASE_URL,
                        "",
                        MODEL,
                        CONNECT_TIMEOUT,
                        READ_TIMEOUT,
                        new ClassPathResource("prompts/clinical-agent-v1.txt")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CAREME_LLM_API_KEY");
    }

    @Test
    void failsToStartWhenTheOperationsPromptIsMissing() {
        assertThatThrownBy(() -> new OpenAiAgentProvider(
                        RestClient.builder(),
                        new ObjectMapper(),
                        BASE_URL,
                        "a-configured-credential",
                        MODEL,
                        CONNECT_TIMEOUT,
                        READ_TIMEOUT,
                        new FileSystemResource("no-existe.txt")))
                .isInstanceOf(IOException.class);
    }
}
