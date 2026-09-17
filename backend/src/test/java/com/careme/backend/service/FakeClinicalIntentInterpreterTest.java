package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.careme.backend.dto.ClinicalEventIntent;
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
}