package com.careme.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Test
    void reportsTheOperationsOfTheTurn() throws Exception {
        when(orchestrator.process(any())).thenReturn(new ChatMessageResponse(
                "conversation-1", "message-1", ChatMessageResponse.Status.ANSWERED,
                "He registrado el diagnóstico y esto es lo que consta.",
                List.of(new ChatMessageResponse.EventSummary(
                        "evt_002", "diagnosis", "2026-02-01", "exact", "Hipertensión")),
                null,
                List.of())
                .withOperations(List.of(
                        new ChatMessageResponse.OperationSummary(
                                ChatMessageResponse.Status.REGISTERED,
                                List.of(new ChatMessageResponse.EventSummary(
                                        "evt_001", "diagnosis", "2026-01-10", "exact", "Hipertensión")),
                                null,
                                List.of()),
                        new ChatMessageResponse.OperationSummary(
                                ChatMessageResponse.Status.ANSWERED,
                                List.of(new ChatMessageResponse.EventSummary(
                                        "evt_002", "diagnosis", "2026-02-01", "exact", "Hipertensión")),
                                null,
                                List.of()))));

        mockMvc.perform(post("/api/v1/chat/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Me diagnosticaron hipertensión. ¿Qué diagnósticos tengo?\",\"messageId\":\"message-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("answered"))
                .andExpect(jsonPath("$.message").value("He registrado el diagnóstico y esto es lo que consta."))
                .andExpect(jsonPath("$.operations[0].status").value("registered"))
                .andExpect(jsonPath("$.operations[0].events[0].code").value("evt_001"))
                .andExpect(jsonPath("$.operations[1].status").value("answered"))
                .andExpect(jsonPath("$.operations[1].events[0].code").value("evt_002"))
                .andExpect(jsonPath("$.operations[1].events[0].datePrecision").value("exact"))
                .andExpect(jsonPath("$.operations[2]").doesNotExist())
                .andExpect(jsonPath("$.events[0].code").value("evt_002"));
    }

    @Test
    void keepsTheAbsenceDetailAcrossTheOperationsOfTheTurn() throws Exception {
        when(orchestrator.process(any())).thenReturn(new ChatMessageResponse(
                "conversation-1", "message-1", ChatMessageResponse.Status.NO_RECORDS,
                "No encuentro registros que respondan a tu pregunta.",
                List.of(),
                ChatMessageResponse.AbsenceReason.NO_TERM_MATCH,
                List.of(ChatMessageResponse.SuggestedAction.REFORMULATE,
                        ChatMessageResponse.SuggestedAction.REGISTER))
                .withOperations(List.of(new ChatMessageResponse.OperationSummary(
                        ChatMessageResponse.Status.NO_RECORDS,
                        List.of(),
                        ChatMessageResponse.AbsenceReason.NO_TERM_MATCH,
                        List.of(ChatMessageResponse.SuggestedAction.REFORMULATE,
                                ChatMessageResponse.SuggestedAction.REGISTER)))));

        mockMvc.perform(post("/api/v1/chat/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"¿He tenido migrañas?\",\"messageId\":\"message-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("no_records"))
                .andExpect(jsonPath("$.absenceReason").value("no_term_match"))
                .andExpect(jsonPath("$.suggestedActions[0]").value("reformulate"))
                .andExpect(jsonPath("$.operations[0].status").value("no_records"))
                .andExpect(jsonPath("$.operations[0].absenceReason").value("no_term_match"))
                .andExpect(jsonPath("$.operations[0].suggestedActions[1]").value("register"));
    }

    @Test
    void leavesTheOperationsOutOfTurnsThatWentThroughNone() throws Exception {
        when(orchestrator.process(any())).thenReturn(new ChatMessageResponse(
                "conversation-1", "message-1", ChatMessageResponse.Status.GENERAL_CONVERSATION,
                "Es una condición que se mide en consulta.", List.of()));

        mockMvc.perform(post("/api/v1/chat/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"¿Qué es la hipertensión?\",\"messageId\":\"message-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("general_conversation"))
                .andExpect(jsonPath("$.operations").isEmpty())
                .andExpect(jsonPath("$.events").isEmpty());
    }

    @Test
    void keepsTheCompletedOperationWhenAnotherOneDidNotComplete() throws Exception {
        when(orchestrator.process(any())).thenReturn(new ChatMessageResponse(
                "conversation-1", "message-1", ChatMessageResponse.Status.FAILED,
                "No he podido completar lo que me pedías. Puedes reintentarlo.",
                List.of(),
                null,
                List.of())
                .withOperations(List.of(
                        new ChatMessageResponse.OperationSummary(
                                ChatMessageResponse.Status.REGISTERED,
                                List.of(new ChatMessageResponse.EventSummary(
                                        "evt_001", "diagnosis", "2026-01-10", "exact", "Hipertensión")),
                                null,
                                List.of()),
                        new ChatMessageResponse.OperationSummary(
                                ChatMessageResponse.Status.FAILED, List.of(), null, List.of()))));

        mockMvc.perform(post("/api/v1/chat/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Registra esto y dime qué consta.\",\"messageId\":\"message-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("failed"))
                .andExpect(jsonPath("$.message").value("No he podido completar lo que me pedías. Puedes reintentarlo."))
                .andExpect(jsonPath("$.operations[0].status").value("registered"))
                .andExpect(jsonPath("$.operations[0].events[0].code").value("evt_001"))
                .andExpect(jsonPath("$.operations[1].status").value("failed"))
                .andExpect(jsonPath("$.operations[1].events").isEmpty());
    }

    @Test
    void doesNotExposePromptsCredentialsOrInternalDetail() throws Exception {
        when(orchestrator.process(any())).thenReturn(new ChatMessageResponse(
                "conversation-1", "message-1", ChatMessageResponse.Status.GENERAL_CONVERSATION,
                "Es una condición que se mide en consulta.", List.of()));

        String body = mockMvc.perform(post("/api/v1/chat/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"¿Qué es la hipertensión?\",\"messageId\":\"message-1\"}"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(body)
                .doesNotContainIgnoringCase("prompt")
                .doesNotContainIgnoringCase("apikey")
                .doesNotContainIgnoringCase("credential")
                .doesNotContain("Bearer ")
                .doesNotContainIgnoringCase("stacktrace");
    }

    @Test
    void normalizesOptionalChatResponseCollections() {
                var response = new ChatMessageResponse(
                                "conversation-1", "message-1", ChatMessageResponse.Status.FAILED,
                                "No se pudo", null, null, null, null);

                assertThat(response.events()).isEmpty();
                assertThat(response.suggestedActions()).isEmpty();
                assertThat(response.operations()).isEmpty();
        }

    @Test
    void closesTheConsultationInProgress() throws Exception {
        when(orchestrator.closeConsultation("conversation-1")).thenReturn(new ChatMessageResponse(
                "conversation-1", null, ChatMessageResponse.Status.REGISTERED,
                "He registrado en tu historia clínica el hecho que hablamos. Ya puedes consultarlo.",
                List.of(new ChatMessageResponse.EventSummary(
                        "evt_001", "diagnosis", "2026-01-10", "exact", "Hipertensión"))));

        mockMvc.perform(post("/api/v1/chat/conversations/conversation-1/consultation/close"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("registered"))
                .andExpect(jsonPath("$.events[0].code").value("evt_001"));
    }

    @Test
    void repeatingTheCloseReturnsTheSameOutcome() throws Exception {
        ChatMessageResponse outcome = new ChatMessageResponse(
                "conversation-1", null, ChatMessageResponse.Status.NOTHING_TO_REGISTER,
                "Hemos cerrado la consulta. No había hechos médicos que registrar.", List.of());
        when(orchestrator.closeConsultation("conversation-1")).thenReturn(outcome);

        String first = mockMvc.perform(post("/api/v1/chat/conversations/conversation-1/consultation/close"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String second = mockMvc.perform(post("/api/v1/chat/conversations/conversation-1/consultation/close"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(first).isEqualTo(second);
        verify(orchestrator, times(2)).closeConsultation("conversation-1");
    }

    @Test
    void rejectsACloseRequestWithAnUnsupportedMethod() throws Exception {
        mockMvc.perform(get("/api/v1/chat/conversations/conversation-1/consultation/close"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void keepsTheConsultationInternalsOutOfTheCloseOutcome() throws Exception {
        when(orchestrator.closeConsultation("conversation-1")).thenReturn(new ChatMessageResponse(
                "conversation-1", null, ChatMessageResponse.Status.NOTHING_TO_REGISTER,
                "Hemos cerrado la consulta. No había hechos médicos que registrar.", List.of()));

        String body = mockMvc.perform(post("/api/v1/chat/conversations/conversation-1/consultation/close"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // The identifier, the summary and the motive of the consultation never cross the API.
        assertThat(body).doesNotContain("motive", "summary", "enc_");
    }
}