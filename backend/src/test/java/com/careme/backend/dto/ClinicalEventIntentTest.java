package com.careme.backend.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.careme.backend.entity.ClinicalEvent;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClinicalEventIntentTest {

    private static ClinicalEventIntent.Query historyQuery() {
        return new ClinicalEventIntent.Query(
                "¿Cuándo me diagnosticaron hipertensión?",
                ClinicalEventIntent.Query.Scope.HISTORY,
                List.of("hipertension"),
                null,
                null,
                null);
    }

    @Test
    void queryIntentCarriesItsQuestionAndCriteriaWithoutCandidates() {
        var intent = ClinicalEventIntent.query(historyQuery());

        assertThat(intent.kind()).isEqualTo(ClinicalEventIntent.Kind.QUERY);
        assertThat(intent.query()).isEqualTo(historyQuery());
        assertThat(intent.events()).isEmpty();
    }

    @Test
    void queryIntentDoesNotAdmitEventCandidates() {
        var candidate = new ClinicalEventIntent.Candidate(
                ClinicalEvent.ClinicalEventType.NOTE,
                "algo",
                null,
                ClinicalEvent.DatePrecision.UNKNOWN,
                null);

        assertThatThrownBy(() -> new ClinicalEventIntent(
                ClinicalEventIntent.Kind.QUERY, List.of(candidate), null, historyQuery()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("event candidates");
    }

    @Test
    void onlyQueryIntentMayCarryAQuery() {
        assertThatThrownBy(() -> new ClinicalEventIntent(
                ClinicalEventIntent.Kind.CONVERSATION, List.of(), null, historyQuery()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only query intent");
    }

    @Test
    void keepsTheExistingVariantsBehavingAsBefore() {
        assertThat(ClinicalEventIntent.conversation().kind()).isEqualTo(ClinicalEventIntent.Kind.CONVERSATION);
        assertThat(ClinicalEventIntent.conversation().query()).isNull();

        assertThat(ClinicalEventIntent.clarification("¿Cuándo ocurrió?").clarification())
                .isEqualTo("¿Cuándo ocurrió?");
        assertThatThrownBy(() -> ClinicalEventIntent.clarification(" "))
                .isInstanceOf(IllegalArgumentException.class);

        var events = new ClinicalEventIntent(ClinicalEventIntent.Kind.EVENTS, List.of(), null);
        assertThat(events.events()).isEmpty();
        assertThat(events.query()).isNull();
    }

    @Test
    void queryIntentsCarryTheGeneralPartOnlyWhenTheyHaveOne() {
        assertThat(historyQuery().generalPart())
                .as("un mensaje que solo consulta la historia no tiene parte general")
                .isNull();

        var mixed = new ClinicalEventIntent.Query(
                "¿Cuándo me la diagnosticaron?",
                ClinicalEventIntent.Query.Scope.HISTORY,
                List.of("diagnosticaron"),
                null,
                null,
                null,
                "¿Qué es la hipertensión?");

        assertThat(mixed.generalPart()).isEqualTo("¿Qué es la hipertensión?");
        assertThat(mixed.searchTerms())
                .as("la parte general no es un criterio de recuperación")
                .doesNotContain("hipertension");
    }

    @Test
    void aBlankGeneralPartIsTheSameAsNoGeneralPart() {
        var query = new ClinicalEventIntent.Query(
                "¿Cuándo me la diagnosticaron?",
                ClinicalEventIntent.Query.Scope.HISTORY,
                List.of("diagnosticaron"),
                null,
                null,
                null,
                "   ");

        assertThat(query.generalPart()).isNull();
    }
}
