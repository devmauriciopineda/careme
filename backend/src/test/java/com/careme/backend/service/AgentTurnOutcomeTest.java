package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.careme.backend.dto.AgentOperation;
import com.careme.backend.dto.ChatMessageResponse;
import com.careme.backend.entity.ClinicalEvent;
import com.careme.backend.entity.EncounterNote;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * How what the agent did becomes the observable outcome of the turn: a status
 * that describes the turn as a whole, and the operations it went through.
 */
class AgentTurnOutcomeTest {

    @Test
    void reportsAnAnswerWhenTheTurnNotedAFactAndAnsweredAQuestion() {
        AgentTurn turn = AgentTurn.completed("He anotado el diagnóstico y esto es lo que consta.",
                List.of(noted(), answered()));

        assertThat(AgentTurnOutcome.status(turn)).isEqualTo(ChatMessageResponse.Status.ANSWERED);
    }

    @Test
    void reportsTheOutcomeOfTheOnlyOperationWhenTheTurnRanOne() {
        assertThat(AgentTurnOutcome.status(AgentTurn.completed("Listo.", List.of(noted()))))
                .isEqualTo(ChatMessageResponse.Status.NOTED);
        assertThat(AgentTurnOutcome.status(AgentTurn.completed("Ya estaba.", List.of(duplicate()))))
                .isEqualTo(ChatMessageResponse.Status.DUPLICATE);
        assertThat(AgentTurnOutcome.status(AgentTurn.completed("No consta.", List.of(noRecords()))))
                .isEqualTo(ChatMessageResponse.Status.NO_RECORDS);
        assertThat(AgentTurnOutcome.status(AgentTurn.completed("Vale.", List.of(rejectedOutOfReach()))))
                .isEqualTo(ChatMessageResponse.Status.GENERAL_CONVERSATION);
    }

    @Test
    void reportsAFailureWhenAnyOperationDidNotComplete() {
        AgentTurn turn = AgentTurn.failed("No pude buscar en tu historia.",
                List.of(noted(), failedAnswer()));

        assertThat(AgentTurnOutcome.status(turn)).isEqualTo(ChatMessageResponse.Status.FAILED);
        assertThat(AgentTurnOutcome.primary(turn)).isNotNull();
        assertThat(AgentTurnOutcome.primary(turn).answer().kind())
                .isEqualTo(ClinicalAnswerResult.Kind.FAILURE);
    }

    @Test
    void reportsAClarificationWhenAnOperationWasRejectedForMissingInformation() {
        AgentTurn turn = AgentTurn.completed(
                "¿Cuándo ocurrió y con qué palabras quieres que lo registre?", List.of(rejectedNotAdmissible()));

        assertThat(AgentTurnOutcome.status(turn)).isEqualTo(ChatMessageResponse.Status.CLARIFICATION_REQUIRED);
    }

    @Test
    void reportsConversationWhenTheTurnRanNoOperation() {
        assertThat(AgentTurnOutcome.status(AgentTurn.completed("Hola, ¿en qué te ayudo?", List.of())))
                .isEqualTo(ChatMessageResponse.Status.GENERAL_CONVERSATION);
    }

    @Test
    void reportsConversationWhenTheOperationAskedForWasNotAvailable() {
        AgentTurn turn = AgentTurn.completed(
                "No puedo borrar hechos: solo consulto tu historia y registro hechos médicos.",
                List.of(rejectedOutOfReach()));

        assertThat(AgentTurnOutcome.status(turn)).isEqualTo(ChatMessageResponse.Status.GENERAL_CONVERSATION);
    }

    @Test
    void reportsEveryOperationTheTurnWentThroughInOrder() {
        AgentTurn turn = AgentTurn.completed(
                "Listo.", List.of(noted(), answered(), rejectedOutOfReach()));

        List<ChatMessageResponse.OperationSummary> operations = AgentTurnOutcome.operations(turn);

        assertThat(operations).hasSize(3);
        assertThat(operations.get(0).status()).isEqualTo(ChatMessageResponse.Status.NOTED);
        assertThat(operations.get(1).status()).isEqualTo(ChatMessageResponse.Status.ANSWERED);
        assertThat(operations.get(2).status()).isEqualTo(ChatMessageResponse.Status.GENERAL_CONVERSATION);
    }

    @Test
    void carriesTheEventsAndTheAbsenceReasonOfEachOperation() {
        AgentTurn turn = AgentTurn.completed("Listo.", List.of(answered(), noRecords()));

        List<ChatMessageResponse.OperationSummary> operations = AgentTurnOutcome.operations(turn);

        assertThat(operations.get(0).events()).singleElement()
                .satisfies(event -> {
                    assertThat(event.code()).isEqualTo("evt_001");
                    assertThat(event.datePrecision()).isEqualTo("exact");
                });
        assertThat(operations.get(1).events()).isEmpty();
        assertThat(operations.get(1).absenceReason())
                .isEqualTo(ChatMessageResponse.AbsenceReason.EMPTY_HISTORY);
        assertThat(operations.get(1).suggestedActions()).isNotEmpty();
    }

    @Test
    void identifiesTheOperationThatCarriesTheTurnOutcome() {
        AgentTurn turn = AgentTurn.completed("Listo.", List.of(noted(), answered()));

        assertThat(AgentTurnOutcome.primary(turn)).isNotNull();
        assertThat(AgentTurnOutcome.primary(turn).operation()).isEqualTo(AgentOperation.CONSULT_HISTORY);
    }

    @Test
    void identifiesNoPrimaryOperationWhenNothingCompleted() {
        assertThat(AgentTurnOutcome.primary(AgentTurn.completed("Hola.", List.of()))).isNull();
    }

    @Test
    void reportsAnAnswerWhenTheTurnAnsweredFromTheMeasurements() {
        AgentTurn turn = AgentTurn.completed("Estas son tus mediciones.", List.of(measurementsAnswered()));

        ChatMessageResponse response = AgentTurnOutcome.respond("conversation-1", "message-1", turn);

        assertThat(response.status()).isEqualTo(ChatMessageResponse.Status.ANSWERED);
        assertThat(response.measurements()).singleElement().satisfies(measurement -> {
            assertThat(measurement.metric()).isEqualTo("weight");
            assertThat(measurement.label()).isEqualTo("peso");
            assertThat(measurement.unit()).isEqualTo("kg");
            assertThat(measurement.date()).isEqualTo("2026-01-10");
            assertThat(measurement.text()).isEqualTo("peso: 70.5 kg (2026-01-10)");
        });
        assertThat(response.events()).as("una medición no es un hecho clínico").isEmpty();
    }

    @Test
    void distinguishesAnAbsenceOfMeasurementsFromAnAbsenceOfFacts() {
        AgentTurn turn = AgentTurn.completed("No consta esa medición.", List.of(measurementsAbsent()));

        ChatMessageResponse response = AgentTurnOutcome.respond("conversation-1", "message-1", turn);

        assertThat(response.status()).isEqualTo(ChatMessageResponse.Status.NO_RECORDS);
        assertThat(response.absenceReason()).isEqualTo(ChatMessageResponse.AbsenceReason.NO_MEASUREMENTS);
        assertThat(response.measurements()).isEmpty();
        assertThat(response.events()).isEmpty();
    }

    @Test
    void keepsARetrievalFailureOutOfTheAbsenceOutcome() {
        AgentTurn turn = AgentTurn.failed(
                "No he podido consultar tus mediciones. Puedes reintentarlo.",
                List.of(measurementsFailed()));

        ChatMessageResponse response = AgentTurnOutcome.respond("conversation-1", "message-1", turn);

        assertThat(response.status()).isEqualTo(ChatMessageResponse.Status.FAILED);
        assertThat(response.absenceReason()).isNull();
        assertThat(response.measurements()).isEmpty();
    }

    @Test
    void asksWhichMetricWithoutAnswering() {
        AgentTurn turn = AgentTurn.completed(
                "¿De qué medición quieres que te hable?", List.of(measurementsClarify()));

        ChatMessageResponse response = AgentTurnOutcome.respond("conversation-1", "message-1", turn);

        assertThat(response.status()).isEqualTo(ChatMessageResponse.Status.CLARIFICATION_REQUIRED);
        assertThat(response.measurements()).isEmpty();
    }

    private static AgentOperationResult noted() {
        return AgentOperationResult.of(
                AgentOperation.RECORD_NOTE,
                new EncounterNoteResult(EncounterNoteResult.Kind.COLLECTED, note(), "Lo he anotado."));
    }

    private static AgentOperationResult duplicate() {
        return AgentOperationResult.of(
                AgentOperation.RECORD_NOTE,
                new EncounterNoteResult(
                        EncounterNoteResult.Kind.DUPLICATE, note(), "Ese hecho ya estaba anotado."));
    }

    private static EncounterNote note() {
        return new EncounterNote(
                ClinicalEvent.ClinicalEventType.DIAGNOSIS,
                "Hipertensión diagnosticada.",
                LocalDate.of(2024, 3, 1),
                ClinicalEvent.DatePrecision.EXACT,
                null);
    }

    private static AgentOperationResult answered() {
        return AgentOperationResult.of(
                AgentOperation.CONSULT_HISTORY,
                ClinicalAnswerResult.answered(List.of(event()), "Tienes un diagnóstico registrado."));
    }

    private static AgentOperationResult noRecords() {
        return AgentOperationResult.of(
                AgentOperation.CONSULT_HISTORY,
                ClinicalAnswerResult.noRecords(
                        ChatMessageResponse.AbsenceReason.EMPTY_HISTORY,
                        List.of(ChatMessageResponse.SuggestedAction.REGISTER),
                        "Todavía no hay registros en tu historia."));
    }

    private static AgentOperationResult failedAnswer() {
        return AgentOperationResult.of(
                AgentOperation.CONSULT_HISTORY, ClinicalAnswerResult.failure("No pude buscar en tu historia."));
    }

    private static AgentOperationResult rejectedNotAdmissible() {
        return AgentOperationResult.rejected(
                AgentOperationResult.Rejection.NOT_ADMISSIBLE, "No he podido completar esa operación.");
    }

    private static AgentOperationResult rejectedOutOfReach() {
        return AgentOperationResult.rejected(
                AgentOperationResult.Rejection.OUT_OF_REACH, "Esa operación no está a mi alcance.");
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

    private static AgentOperationResult measurementsAnswered() {
        return AgentOperationResult.of(
                AgentOperation.CONSULT_MEASUREMENTS,
                MeasurementAnswerResult.answered(List.of(fact()), "Estas son tus mediciones registradas."));
    }

    private static AgentOperationResult measurementsAbsent() {
        return AgentOperationResult.of(
                AgentOperation.CONSULT_MEASUREMENTS,
                MeasurementAnswerResult.noRecords(
                        ChatMessageResponse.AbsenceReason.NO_MEASUREMENTS,
                        List.of(ChatMessageResponse.SuggestedAction.REFORMULATE),
                        "No consta ninguna medición tuya de esa métrica."));
    }

    private static AgentOperationResult measurementsClarify() {
        return AgentOperationResult.of(
                AgentOperation.CONSULT_MEASUREMENTS,
                MeasurementAnswerResult.askForMetric("¿De qué medición quieres que te hable?"));
    }

    private static AgentOperationResult measurementsFailed() {
        return AgentOperationResult.of(
                AgentOperation.CONSULT_MEASUREMENTS,
                MeasurementAnswerResult.failure("No he podido consultar tus mediciones. Puedes reintentarlo."));
    }

    private static MeasurementFact fact() {
        return new MeasurementFact(
                "weight@2026-01-10",
                "weight",
                "peso",
                "kg",
                "2026-01-10",
                List.of(new MeasurementFact.MeasurementFactValue("value", "70.5")),
                "peso: 70.5 kg (2026-01-10)");
    }
}
