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
        assertThat(turn.message()).contains("perfil");
        verifyNoInteractions(executor);
    }

    @Test
    void letsThePersonSkipTheOptionalProfileInterview() {
        AgentTurn turn = agent.respond("No, gracias", CONTEXT);

        assertThat(turn.operations()).isEmpty();
        assertThat(turn.message()).contains("retomar");
        verifyNoInteractions(executor);
    }

    @Test
    void resumesTheInterviewByAskingOnlyForTheFirstMissingField() {
        AgentContext afterInvitation = new AgentContext(
                "conversation-1",
                CONTEXT.referenceDate(),
                CONTEXT.zoneId(),
                List.of("Usuario: Hola | Asistente: perfil personal"));

        AgentTurn turn = agent.respond("Quiero completar mi perfil", afterInvitation);

        assertThat(turn.operations()).isEmpty();
        assertThat(turn.message()).contains("cómo te llamas");
        verifyNoInteractions(executor);
    }

    @Test
    void asksForEachRemainingFieldInTurn() {
        assertThat(resumeWith(new com.careme.backend.entity.PatientProfile(
                "Ana", null, null, null, null, null, null))).contains("fecha de nacimiento");
        assertThat(resumeWith(new com.careme.backend.entity.PatientProfile(
                "Ana", LocalDate.of(1984, 2, 3), null, null, null, null, null))).contains("sexo");
        assertThat(resumeWith(new com.careme.backend.entity.PatientProfile(
                "Ana", LocalDate.of(1984, 2, 3), "mujer", null, null, null, null)))
                .contains("ocupación");
        assertThat(resumeWith(new com.careme.backend.entity.PatientProfile(
                "Ana", LocalDate.of(1984, 2, 3), "mujer", "Médica", null, null, null)))
                .contains("grupo sanguíneo");
        assertThat(resumeWith(new com.careme.backend.entity.PatientProfile(
                "Ana", LocalDate.of(1984, 2, 3), "mujer", "Médica", "O", "positivo", null)))
                .contains("estatura");
    }

    @Test
    void greetsWithoutInvitingWhenTheProfileIsAlreadyComplete() {
        AgentContext complete = new AgentContext(
                "conversation-1",
                CONTEXT.referenceDate(),
                CONTEXT.zoneId(),
                List.of(),
                new com.careme.backend.entity.PatientProfile(
                        "Ana", LocalDate.of(1984, 2, 3), "mujer", "Médica", "O", "positivo",
                        new java.math.BigDecimal("170")));

        AgentTurn turn = agent.respond("Hola", complete);

        assertThat(turn.message()).doesNotContain("perfil");
        verifyNoInteractions(executor);
    }

    @Test
    void recordsBirthDateHeightAndBloodGroupInOneMessage() {
        when(executor.execute(any(), any(), any())).thenReturn(profiled());

        agent.respond("Nací el 1984-02-03, mido 170 cm y mi grupo sanguíneo es O positivo.", CONTEXT);

        AgentOperationCall call = capturedCall();
        assertThat(call.operation()).isEqualTo(AgentOperation.RECORD_PROFILE);
        assertThat(call.arguments().path("date_of_birth").asText()).isEqualTo("1984-02-03");
        assertThat(call.arguments().path("height_cm").asText()).isEqualTo("170");
        assertThat(call.arguments().path("blood_group").asText()).isEqualTo("O");
        assertThat(call.arguments().path("rh").asText()).isEqualTo("positivo");
    }

    @Test
    void keepsANonProfileStatementOnTheClinicalChannel() {
        when(executor.execute(any(), any(), any())).thenReturn(noted());

        // The word "soy" points at the profile, but the message carries no profile datum,
        // so the turn must not invent one.
        agent.respond("Soy profesora desde hace diez años.", CONTEXT);

        assertThat(capturedCall().operation()).isEqualTo(AgentOperation.RECORD_NOTE);
    }

    @Test
    void readsAnotherNaturalNameFormulation() {
        when(executor.execute(any(), any(), any())).thenReturn(profiled());

        agent.respond("Mi nombre es Ana Pérez.", CONTEXT);

        AgentOperationCall call = capturedCall();
        assertThat(call.operation()).isEqualTo(AgentOperation.RECORD_PROFILE);
        assertThat(call.arguments().path("name").asText()).isEqualTo("Ana Pérez");
    }

    private String resumeWith(com.careme.backend.entity.PatientProfile profile) {
        AgentContext context = new AgentContext(
                "conversation-1",
                CONTEXT.referenceDate(),
                CONTEXT.zoneId(),
                List.of("Usuario: Hola | Asistente: perfil personal"),
                profile);
        return agent.respond("Sí, quiero completarlo", context).message();
    }

    @Test
    void declinesARequestForAdviceWithoutAskingForAnyOperation() {
        AgentTurn turn = agent.respond("¿Me recomiendas algo para el dolor de cabeza?", CONTEXT);

        assertThat(turn.operations()).isEmpty();
        assertThat(turn.message()).contains("diagnóstico");
        verifyNoInteractions(executor);
    }

    @Test
    void declinesAdviceAboutAMeasurementWhileConsultingItsOwnChannel() {
        when(executor.execute(any(), any(), any())).thenReturn(answered());

        agent.respond("¿Me recomiendas algo para la tensión?", CONTEXT);

        AgentOperationCall call = capturedCall();
        assertThat(call.operation()).isEqualTo(AgentOperation.CONSULT_MEASUREMENTS);
        assertThat(call.arguments().path("interpretation_requested").asBoolean()).isTrue();
        assertThat(call.arguments().path("metrics")).isNotEmpty();
    }

    @Test
    void answersAMeasurementQuestionByItsOwnChannel() {
        when(executor.execute(any(), any(), any())).thenReturn(answered());

        agent.respond("¿Cuál es mi peso?", CONTEXT);

        AgentOperationCall call = capturedCall();
        assertThat(call.operation()).isEqualTo(AgentOperation.CONSULT_MEASUREMENTS);
        assertThat(call.arguments().path("metrics")).hasSize(1);
        assertThat(call.arguments().path("metrics").get(0).asText()).isEqualTo("weight");
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
        void recordsAnExplicitConfirmedProfileValueThroughTheProfileChannel() {
            when(executor.execute(any(), any(), any())).thenReturn(profiled());

            agent.respond("Me llamo Ana.", CONTEXT);

            AgentOperationCall call = capturedCall();
            assertThat(call.operation()).isEqualTo(AgentOperation.RECORD_PROFILE);
            assertThat(call.arguments().path("name").asText()).isEqualTo("Ana");
            assertThat(call.arguments().path("confirmed").asBoolean()).isTrue();
        }

        @Test
        void keepsAClinicalFactOutOfTheProfileChannel() {
            when(executor.execute(any(), any(), any())).thenReturn(noted());

            agent.respond("Me diagnosticaron hipertensión.", CONTEXT);

            assertThat(capturedCall().operation()).isEqualTo(AgentOperation.RECORD_NOTE);
        }

    @Test
    void resolvesTodayAgainstTheReferenceDateOfTheTurn() {
        when(executor.execute(any(), any(), any())).thenReturn(noted());

        agent.respond("Hoy me tomaron la presión y fue 145/92.", CONTEXT);

        AgentOperationCall call = capturedCall();
        assertThat(call.operation()).isEqualTo(AgentOperation.RECORD_MEASUREMENT);
        assertThat(call.arguments().path("metric").asText()).isEqualTo("blood_pressure");
        assertThat(call.arguments().path("values").path("systolic").asInt()).isEqualTo(145);
        assertThat(call.arguments().path("values").path("diastolic").asInt()).isEqualTo(92);
        assertThat(call.arguments().path("date").asText()).isEqualTo("2026-09-19");
    }

    @Test
    void readsAWeightStatedInAnotherUnitThroughTheMeasurementChannel() {
        when(executor.execute(any(), any(), any())).thenReturn(noted());

        agent.respond("Mi peso hoy es 154 libras.", CONTEXT);

        AgentOperationCall call = capturedCall();
        assertThat(call.operation()).isEqualTo(AgentOperation.RECORD_MEASUREMENT);
        assertThat(call.arguments().path("metric").asText()).isEqualTo("weight");
        assertThat(call.arguments().path("values").path("value").asInt()).isEqualTo(154);
        assertThat(call.arguments().path("unit").asText()).isEqualTo("lb");
    }

    @Test
    void readsAWaistThroughTheMeasurementChannelWithoutInventingAUnit() {
        when(executor.execute(any(), any(), any())).thenReturn(noted());

        agent.respond("Mi circunferencia de cintura es 94 cm.", CONTEXT);

        AgentOperationCall call = capturedCall();
        assertThat(call.operation()).isEqualTo(AgentOperation.RECORD_MEASUREMENT);
        assertThat(call.arguments().path("metric").asText()).isEqualTo("waist");
        assertThat(call.arguments().path("values").path("value").asInt()).isEqualTo(94);
        assertThat(call.arguments().has("unit")).isFalse();
    }

    @Test
    void fallsBackToAFactNoteWhenAMeasurementWordCarriesNoValue() {
        when(executor.execute(any(), any(), any())).thenReturn(noted());

        agent.respond("Ayer me dijeron que tengo que vigilar la circunferencia abdominal.", CONTEXT);

        assertThat(capturedCall().operation()).isEqualTo(AgentOperation.RECORD_NOTE);
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

        private static AgentOperationResult profiled() {
        return AgentOperationResult.of(
            AgentOperation.RECORD_PROFILE,
            new PatientProfileResult(
                PatientProfileResult.Kind.COLLECTED,
                new com.careme.backend.entity.PatientProfileUpdate(
                    "Ana", null, null, null, null, null, null, true),
                "Lo he anotado."));
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
