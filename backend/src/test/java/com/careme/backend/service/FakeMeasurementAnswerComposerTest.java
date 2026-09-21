package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

class FakeMeasurementAnswerComposerTest {

    private final FakeMeasurementAnswerComposer composer = new FakeMeasurementAnswerComposer();

    @Test
    void framesTheMeasurementsTheApplicationFormatted() {
        String answer = composer.compose("¿cuánto peso?", List.of(fact()), false);

        assertThat(answer).contains("peso: 70.5 kg (2026-01-10)");
    }

    @Test
    void declinesAnInterpretationWhileOfferingTheRegisteredValues() {
        String answer = composer.compose("¿es grave mi peso?", List.of(fact()), true);

        assertThat(answer)
                .contains("peso: 70.5 kg (2026-01-10)")
                .contains("no diagnostica ni recomienda tratamiento");
    }

    @Test
    void refusesToAnswerWithoutMeasurements() {
        assertThatThrownBy(() -> composer.compose("¿cuánto peso?", List.of(), false))
                .isInstanceOf(LlmIntegrationException.class);
    }

    static MeasurementFact fact() {
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
