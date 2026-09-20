package com.careme.backend.service;

/**
 * The assistant, as the conversation needs it: it receives a message with the
 * context of the turn and answers with what it did and what to say.
 *
 * <p>The conversation never learns how the assistant decides, and the assistant
 * never reaches the clinical history: it asks for operations and the application
 * runs them.
 */
public interface ClinicalAgent {

    AgentTurn respond(String message, AgentContext context);
}
