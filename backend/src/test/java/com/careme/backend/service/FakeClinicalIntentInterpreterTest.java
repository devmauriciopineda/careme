package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.careme.backend.dto.ClinicalEventIntent;
import com.careme.backend.entity.ClinicalEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class FakeClinicalIntentInterpreterTest {

    private final FakeClinicalIntentInterpreter interpreter = new FakeClinicalIntentInterpreter();
    private final ClinicalIntentInterpreter.InterpretationContext context =
            new ClinicalIntentInterpreter.InterpretationContext(
                    LocalDate.of(2026, 9, 13), ZoneId.of("Europe/Madrid"), "clinical-intent-v1");

    @Test
    void classifiesEventAndPreservesRelativeDate() {
        var result = interpreter.interpret("Ayer me diagnosticaron hipertensión", context);

        assertThat(result.kind()).isEqualTo(ClinicalEventIntent.Kind.EVENTS);
        assertThat(result.events()).singleElement().satisfies(event -> {
            assertThat(event.date()).isEqualTo(LocalDate.of(2026, 9, 12));
            assertThat(event.dateText()).isEqualTo("ayer");
        });
    }

    @Test
    void classifiesSuspicionAndGeneralConversationWithoutEvents() {
        assertThat(interpreter.interpret("Creo que podría tener hipertensión", context).kind())
                .isEqualTo(ClinicalEventIntent.Kind.CONVERSATION);
        assertThat(interpreter.interpret("Hola, ¿cómo estás?", context).kind())
                .isEqualTo(ClinicalEventIntent.Kind.CONVERSATION);
    }

    @Test
    void classifiesAGeneralQuestionAsConversation() {
        assertThat(interpreter.interpret("¿Qué es la hipertensión?", context).kind())
                .isEqualTo(ClinicalEventIntent.Kind.CONVERSATION);
        assertThat(interpreter.interpret("¿Para qué sirve la metformina?", context).kind())
                .isEqualTo(ClinicalEventIntent.Kind.CONVERSATION);
    }

    @Test
    void classifiesARequestForAdviceAsConversation() {
        assertThat(interpreter.interpret("¿Qué me recomiendas para la hipertensión?", context).kind())
                .isEqualTo(ClinicalEventIntent.Kind.CONVERSATION);
    }

    @Test
    void keepsAColloquialQuestionInTheClinicalHistoryChannel() {
        var result = interpreter.interpret("¿Qué me dijeron del azúcar?", context);

        assertThat(result.kind()).isEqualTo(ClinicalEventIntent.Kind.QUERY);
        assertThat(result.query().scope()).isEqualTo(ClinicalEventIntent.Query.Scope.HISTORY);
        assertThat(result.query().searchTerms()).isNotEmpty();
    }

    @Test
    void keepsMeasurementsQuestionsInTheMeasurementsScope() {
        var result = interpreter.interpret("¿Cuánto peso?", context);

        assertThat(result.kind()).isEqualTo(ClinicalEventIntent.Kind.QUERY);
        assertThat(result.query().scope()).isEqualTo(ClinicalEventIntent.Query.Scope.MEASUREMENTS);
    }

    @Test
    void separatesTheGeneralPartOfAMixedMessage() {
        var result = interpreter.interpret("¿Qué es la hipertensión? ¿Cuándo me la diagnosticaron?", context);

        assertThat(result.kind()).isEqualTo(ClinicalEventIntent.Kind.QUERY);
        assertThat(result.query().question()).isEqualTo("¿Cuándo me la diagnosticaron?");
        assertThat(result.query().generalPart()).isEqualTo("¿Qué es la hipertensión?");
        assertThat(result.query().searchTerms())
                .as("la parte general no es criterio de recuperación")
                .doesNotContain("hipertension");
    }

    @Test
    void asksForClarificationWhenTheChannelIsUndetermined() {
        var result = interpreter.interpret("¿Eso es bueno?", context);

        assertThat(result.kind()).isEqualTo(ClinicalEventIntent.Kind.CLARIFICATION);
        assertThat(result.clarification()).contains("historia clínica");
    }

    @Test
    void classifiesAllEventTypesAndTemporalPrecisions() {
        var medication = interpreter.interpret("Hoy tomo medicación", context).events().getFirst();
        var measurement = interpreter.interpret("Ayer medí la presión", context).events().getFirst();
        var diagnosis = interpreter.interpret("Me diagnosticaron anemia", context).events().getFirst();
        var note = interpreter.interpret("Tuve dolor y fiebre", context).events().getFirst();
        var approximate = interpreter.interpret("Hace dos semanas tuve dolor", context).events().getFirst();
        var unknown = interpreter.interpret("La operación fue complicada", context).events().getFirst();

        assertThat(medication.type()).isEqualTo(ClinicalEvent.ClinicalEventType.MEDICATION);
        assertThat(medication.date()).isEqualTo(context.referenceDate());
        assertThat(measurement.type()).isEqualTo(ClinicalEvent.ClinicalEventType.MEASUREMENT);
        assertThat(measurement.date()).isEqualTo(context.referenceDate().minusDays(1));
        assertThat(diagnosis.type()).isEqualTo(ClinicalEvent.ClinicalEventType.DIAGNOSIS);
        assertThat(note.type()).isEqualTo(ClinicalEvent.ClinicalEventType.NOTE);
        assertThat(approximate.datePrecision()).isEqualTo(ClinicalEvent.DatePrecision.APPROXIMATE);
        assertThat(unknown.datePrecision()).isEqualTo(ClinicalEvent.DatePrecision.UNKNOWN);
    }

    @Test
    void classifiesShortAndExplicitClarificationMessages() {
        assertThat(interpreter.interpret("aclara la fecha", context).kind())
                .isEqualTo(ClinicalEventIntent.Kind.CLARIFICATION);
        assertThat(interpreter.interpret("ok", context).kind())
                .isEqualTo(ClinicalEventIntent.Kind.CLARIFICATION);
        assertThat(interpreter.interpret("El clima de mañana es agradable", context).kind())
                .isEqualTo(ClinicalEventIntent.Kind.CONVERSATION);
    }

    @Test
    void classifiesGeneralQuestionWithHistoryEvidenceAsHistoryQuery() {
        var result = interpreter.interpret("¿Es normal mi peso?", context);

        assertThat(result.kind()).isEqualTo(ClinicalEventIntent.Kind.QUERY);
        assertThat(result.query().scope()).isEqualTo(ClinicalEventIntent.Query.Scope.MEASUREMENTS);
    }

    @Test
    void classifiesHistoryQueriesByTypeAndKeepsGeneralQuestionsSeparate() {
        var medication = interpreter.interpret("¿Qué medicación tomo?", context).query();
        var measurement = interpreter.interpret("¿Qué presión tengo?", context).query();
        var diagnosis = interpreter.interpret("¿Qué diagnostico consta?", context).query();
        var history = interpreter.interpret("¿Qué consta en mi historia?", context).query();

        assertThat(medication.type()).isEqualTo(ClinicalEvent.ClinicalEventType.MEDICATION);
        assertThat(measurement.type()).isEqualTo(ClinicalEvent.ClinicalEventType.MEASUREMENT);
        assertThat(diagnosis.type()).isEqualTo(ClinicalEvent.ClinicalEventType.DIAGNOSIS);
        assertThat(history.type()).isNull();
    }

    @Test
    void recognizesAdditionalGreetingsAndAdvicePatterns() {
        assertThat(interpreter.interpret("¿Qué puedes hacer?", context).kind())
                .isEqualTo(ClinicalEventIntent.Kind.CONVERSATION);
        assertThat(interpreter.interpret("¿Qué debería tomar?", context).kind())
                .isEqualTo(ClinicalEventIntent.Kind.CONVERSATION);
        assertThat(interpreter.interpret("¿Estoy bien?", context).kind())
                .isEqualTo(ClinicalEventIntent.Kind.CONVERSATION);
    }
}