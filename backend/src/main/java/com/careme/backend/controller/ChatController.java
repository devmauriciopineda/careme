package com.careme.backend.controller;

import com.careme.backend.dto.ChatMessageRequest;
import com.careme.backend.dto.ChatMessageResponse;
import com.careme.backend.service.ChatOrchestrator;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatOrchestrator orchestrator;

    public ChatController(ChatOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping("/messages")
    public ResponseEntity<ChatMessageResponse> send(@Valid @RequestBody ChatMessageRequest request) {
        return ResponseEntity.ok(orchestrator.process(request));
    }
}