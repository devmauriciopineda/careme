package com.careme.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * The assistant backed by a real provider: it holds the loop that asks the
 * provider what it needs, executes it and stops when the provider answers.
 */
@Service
@ConditionalOnProperty(name = "careme.llm.mode", havingValue = "openai")
public class OpenAiClinicalAgent implements ClinicalAgent {

    private final AgentTurnRunner runner;

    public OpenAiClinicalAgent(
            OpenAiAgentProvider provider,
            AgentOperationExecutor executor,
            @Value("${careme.chat.agent.max-operations:4}") int maxOperations) {
        this.runner = new AgentTurnRunner(provider, executor, maxOperations);
    }

    @Override
    public AgentTurn respond(String message, AgentContext context) {
        return runner.run(message, context);
    }
}
