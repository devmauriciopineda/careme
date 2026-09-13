package com.careme.backend.service;

public class LlmIntegrationException extends RuntimeException {

    public LlmIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}