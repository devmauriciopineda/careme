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

    @Test
    void reportsAbsenceReasonAndSuggestedActionsOnANoRecordsTurn() throws Exception {
        when(orchestrator.process(any())).thenReturn(new ChatMessageResponse(
                "conversation-1", "message-1", ChatMessageResponse.Status.NO_RECORDS,
                "No encuentro registros en ese periodo de tu historia clínica.",
                List.of(),
                ChatMessageResponse.AbsenceReason.NO_EVENTS_IN_PERIOD,
                List.of(ChatMessageResponse.SuggestedAction.REFORMULATE,
                        ChatMessageResponse.SuggestedAction.REGISTER)));

        mockMvc.perform(post("/api/v1/chat/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"¿Qué me pasó el año pasado?\",\"messageId\":\"message-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("no_records"))
                .andExpect(jsonPath("$.absenceReason").value("no_events_in_period"))
                .andExpect(jsonPath("$.suggestedActions[0]").value("reformulate"))
                .andExpect(jsonPath("$.suggestedActions[1]").value("register"));
    }

    @Test
    void leavesAbsenceDetailOutOfAnAnsweredTurnWithoutChangingItsStatus() throws Exception {
        when(orchestrator.process(any())).thenReturn(new ChatMessageResponse(
                "conversation-1", "message-1", ChatMessageResponse.Status.ANSWERED,
                "Te la diagnosticaron en enero.",
                List.of(new ChatMessageResponse.EventSummary(
                        "evt_001", "diagnosis", "2026-01-10", "exact", "Hipertensión"))));

        mockMvc.perform(post("/api/v1/chat/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"¿Cuándo?\",\"messageId\":\"message-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("answered"))
                .andExpect(jsonPath("$.absenceReason").doesNotExist())
                .andExpect(jsonPath("$.suggestedActions").isEmpty())
                .andExpect(jsonPath("$.events[0].code").value("evt_001"));
    }

    @Test
    void leavesAbsenceDetailOutOfAFailedTurnWithoutChangingItsStatus() throws Exception {
        when(orchestrator.process(any())).thenReturn(new ChatMessageResponse(
                "conversation-1", "message-1", ChatMessageResponse.Status.FAILED,
                "No pude completar la búsqueda. Puedes volver a intentarlo.", List.of()));

        mockMvc.perform(post("/api/v1/chat/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"¿Cuándo?\",\"messageId\":\"message-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("failed"))
                .andExpect(jsonPath("$.absenceReason").doesNotExist())
                .andExpect(jsonPath("$.suggestedActions").isEmpty());
    }
}