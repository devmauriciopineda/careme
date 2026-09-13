package com.careme.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatMessageRequest(
        @Size(max = 4000, message = "El mensaje no puede superar 4000 caracteres")
        @NotBlank(message = "El mensaje no puede estar vacío")
        String message,
        String conversationId,
        @NotBlank(message = "El identificador del mensaje es obligatorio")
        String messageId) {
}