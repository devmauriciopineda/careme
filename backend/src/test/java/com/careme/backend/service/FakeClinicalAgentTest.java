package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.careme.backend.dto.AgentOperation;
import com.careme.backend.entity.ClinicalEvent;
import com.careme.backend.entity.EncounterNote;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * The simulated assistant: it must ask for the same operations as the real one,
 * so the whole path runs without network access.
 */
class FakeClinicalAgentTest {

    private static final AgentContext CONTEXT =
            new AgentContext("conversation-1", LocalDate.of(2026, 9, 19), ZoneId.of("Europe/Madrid"), List.of());

    private final AgentOperationExecutor executor = mock(AgentOperationExecutor.class);
    private final FakeClinicalAgent agent = new FakeClinicalAgent(executor);

    @Test
    void answersAGreetingWithoutAskingForAnyOperation() {
        AgentTurn turn = agent.respond("Hola", CONTEXT);

        assertThat(turn.failed()).isFalse();
        assertThat(turn.operations()).isEmpty();
        assertThat(turn.message()).isNotBlank();
        verifyNoInteractions(executor);
    }

    @Test
    void declinesARequestForAdviceWithoutAskingForAnyOperation() {
        AgentTurn turn = agent.respond("¿Me recomiendas algo para la tensión?", CONTEXT);

        assertThat(turn.operations()).isEmpty();
        assertThat(turn.message()).contains("diagnóstico");
        verifyNoInteractions(executor);
    }

    @Test
    void explainsAGeneralConceptWithoutConsultingTheHistory() {
        AgentTurn turn = agent.respond("¿Qué es la hipertensión?", CONTEXT);

        assertThat(turn.operations()).isEmpty();
        assertThat(turn.message()).isNotBlank();
        verifyNoInteractions(executor);
    }

    @Test
    void consultsTheHistoryForAQuestionAboutIt() {
        when(executor.execute(any(), any(), any())).thenReturn(answered());

        AgentTurn turn = agent.respond("¿Cuándo me diagnosticaron hipertensión?", CONTEXT);

        AgentOperationCall call = capturedCall();
        assertThat(call.operation()).isEqualTo(AgentOperation.CONSULT_HISTORY);
        assertThat(call.arguments().path("search_terms")).isNotEmpty();
        assertThat(turn.message()).isEqualTo("Hace unos dos años.");
        assertThat(turn.operations()).hasSize(1);
    }

    @Test
    void notesAFactThePersonTells() {
        when(executor.execute(any(), any(), any())).thenReturn(noted());

        agent.respond("Me diagnosticaron hipertensión hace unos dos años.", CONTEXT);

        AgentOperationCall call = capturedCall();
        assertThat(call.operation()).isEqualTo(AgentOperation.RECORD_NOTE);
        assertThat(call.arguments().path("type").asText()).isEqualTo("diagnosis");
        assertThat(call.arguments().path("content").asText())
                .isEqualTo("Me diagnosticaron hipertensión hace unos dos años.");
        assertThat(call.arguments().path("date_precision").asText()).isEqualTo("approximate");
        assertThat(call.arguments().path("date_text").asText()).isEqualTo("hace");
    }

    @Test
    void resolvesTodayAgainstTheReferenceDateOfTheTurn() {
        when(executor.execute(any(), any(), any())).thenReturn(noted());

        agent.respond("Hoy me tomaron la presión y fue 145/92.", CONTEXT);

        AgentOperationCall call = capturedCall();
        assertThat(call.operation()).isEqualTo(AgentOperation.RECORD_NOTE);
        assertThat(call.arguments().path("date_precision").asText()).isEqualTo("exact");
        assertThat(call.arguments().path("date").asText()).isEqualTo("2026-09-19");
        assertThat(call.arguments().path("type").asText()).isEqualTo("measurement");
    }

    @Test
    void asksNothingWhenTheMessageCarriesNoFactAndNoQuestion() {
        AgentTurn turn = agent.respond("Mmm", CONTEXT);

        assertThat(turn.operations()).isEmpty();
        assertThat(turn.message()).isNotBlank();
        verifyNoInteractions(executor);
    }

    @Test
    void failsTheTurnWhenTheOperationCouldNotComplete() {
        when(executor.execute(any(), any(), any())).thenReturn(
                AgentOperationResult.of(
                        AgentOperation.CONSULT_HISTORY,
                        ClinicalAnswerResult.failure("No pude buscar en tu historia.")));

        AgentTurn turn = agent.respond("¿Cuándo me diagnosticaron hipertensión?", CONTEXT);

        assertThat(turn.failed()).isTrue();
        assertThat(turn.message()).isEqualTo("No pude buscar en tu historia.");
    }

    @Test
    void reportsAnAbsenceAsTheAnswerOfTheTurn() {
        when(executor.execute(any(), any(), any())).thenReturn(
                AgentOperationResult.of(
                        AgentOperation.CONSULT_HISTORY,
                        ClinicalAnswerResult.noRecords(
                                com.careme.backend.dto.ChatMessageResponse.AbsenceReason.EMPTY_HISTORY,
                                List.of(),
                                "Todavía no hay registros en tu historia.")));

        AgentTurn turn = agent.respond("¿Cuándo me diagnosticaron hipertensión?", CONTEXT);

        assertThat(turn.failed()).isFalse();
        assertThat(turn.message()).isEqualTo("Todavía no hay registros en tu historia.");
    }

    private AgentOperationCall capturedCall() {
        ArgumentCaptor<AgentOperationCall> call = ArgumentCaptor.forClass(AgentOperationCall.class);
        verify(executor).execute(eq("conversation-1"), call.capture(), eq(LocalDate.of(2026, 9, 19)));
        return call.getValue();
    }

    private static AgentOperationResult answered() {
        return AgentOperationResult.of(
                AgentOperation.CONSULT_HISTORY,
                ClinicalAnswerResult.answered(List.of(event()), "Hace unos dos años."));
    }

    private static AgentOperationResult noted() {
        return AgentOperationResult.of(
                AgentOperation.RECORD_NOTE,
                new EncounterNoteResult(EncounterNoteResult.Kind.COLLECTED, note(), "Lo he anotado."));
    }

    private static EncounterNote note() {
        return new EncounterNote(
                ClinicalEvent.ClinicalEventType.DIAGNOSIS,
                "Me diagnosticaron hipertensión hace unos dos años.",
                LocalDate.of(2024, 3, 1),
                ClinicalEvent.DatePrecision.APPROXIMATE,
                "hace unos dos años");
    }

    private static ClinicalEvent event() {
        return new ClinicalEvent(
                UUID.randomUUID(),
                "evt_001",
                ClinicalEvent.ClinicalEventType.DIAGNOSIS,
                LocalDate.of(2024, 3, 1),
                ClinicalEvent.DatePrecision.EXACT,
                null,
                "Hipertensión diagnosticada.",
                ClinicalEvent.EventSource.PATIENT,
                OffsetDateTime.now(ZoneOffset.UTC),
                null);
    }
}
