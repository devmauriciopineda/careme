package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * UC-010: the offline mode answers a conversational message with its own reply and
 * applies the same limits the real prompt carries — it explains general concepts
 * without applying them, declines requests for advice, and never presents anything
 * as a recorded fact.
 */
class FakeClinicalConversationComposerTest {

    private final FakeClinicalConversationComposer composer = new FakeClinicalConversationComposer();

    @Test
    void explainsAGeneralConceptWithoutApplyingItToTheUsersCase() {
        String reply = composer.compose("¿Qué es la hipertensión?", List.of());

        assertThat(reply).contains("hipertensión");
        assertThat(reply).contains("sin aplicarlo a tu caso");
        assertThat(reply).contains("no interpreto tus datos");
        assertThat(reply)
                .as("no recomienda tratamiento ni interpreta el caso")
                .doesNotContain("debes")
                .doesNotContain("te recomiendo");
    }

    @Test
    void declinesARequestForARecommendation() {
        String reply = composer.compose("¿Qué me recomiendas para la hipertensión?", List.of());

        assertThat(reply).contains("no diagnostica ni recomienda");
        assertThat(reply).contains("No puedo darte un diagnóstico ni recomendarte un tratamiento");
    }

    @Test
    void declinesARequestForAnInterpretationOfTheUsersCase() {
        String reply = composer.compose("¿Es grave lo que me pasa?", List.of());

        assertThat(reply).contains("no diagnostica ni recomienda");
    }

    @Test
    void answersAGreetingBrieflyWithoutMentioningRecords() {
        String reply = composer.compose("Hola, ¿cómo estás?", List.of());

        assertThat(reply).isNotBlank();
        assertThat(reply).doesNotContain("registro");
        assertThat(reply).doesNotContain("historia clínica");
    }

    @Test
    void answersAGeneralQuestionWithoutConsultingTheHistory() {
        String reply = composer.compose("¿Qué se puede hacer un domingo en Madrid?", List.of());

        assertThat(reply).contains("un domingo en Madrid");
        assertThat(reply).doesNotContain("registro");
        assertThat(reply).doesNotContain("historia clínica");
    }

    @Test
    void refusesToComposeWithoutAMessage() {
        assertThatThrownBy(() -> composer.compose("   ", List.of()))
                .isInstanceOf(LlmIntegrationException.class)
                .hasMessage("There is nothing to reply to");
    }
}
