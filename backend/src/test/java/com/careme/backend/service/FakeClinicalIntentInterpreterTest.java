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
}