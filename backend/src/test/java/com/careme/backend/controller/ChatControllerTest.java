package com.careme.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.careme.backend.dto.ChatMessageResponse;
import com.careme.backend.service.ChatOrchestrator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class ChatControllerTest {

    private final ChatOrchestrator orchestrator = mock(ChatOrchestrator.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ChatController(orchestrator))
                .setValidator(new LocalValidatorFactoryBean())
                .build();
    }

    @Test
    void returnsStableChatEnvelope() throws Exception {
        when(orchestrator.process(any())).thenReturn(new ChatMessageResponse(
                "conversation-1", "message-1", ChatMessageResponse.Status.GENERAL_CONVERSATION,
                "Puedo ayudarte.", List.of()));

        mockMvc.perform(post("/api/v1/chat/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Hola\",\"messageId\":\"message-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value("conversation-1"))
                .andExpect(jsonPath("$.status").value("general_conversation"));
    }

    @Test
    void rejectsBlankMessageBeforeOrchestrator() throws Exception {
        mockMvc.perform(post("/api/v1/chat/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\" \",\"messageId\":\"message-1\"}"))
                .andExpect(status().isBadRequest());
    }
}