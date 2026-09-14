package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.careme.backend.dto.ClinicalEventIntent;
import com.careme.backend.entity.ClinicalEvent;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClinicalEventIntentValidatorTest {

    private final ClinicalEventIntentValidator validator = new ClinicalEventIntentValidator();

    @Test
    void acceptsEventsClarificationsAndConversation() {
        var candidate = new ClinicalEventIntent.Candidate(
                ClinicalEvent.ClinicalEventType.DIAGNOSIS,
                "Me diagnosticaron hipertension",
                LocalDate.of(2026, 8, 10),
                ClinicalEvent.DatePrecision.EXACT,
                "el 10 de agosto");

        validator.validate(new ClinicalEventIntent(ClinicalEventIntent.Kind.EVENTS, List.of(candidate), null));
        validator.validate(ClinicalEventIntent.clarification("Que fecha tuvo el hecho?"));
        validator.validate(ClinicalEventIntent.conversation());
    }

    @Test
    void rejectsEmptyEventsAndInvalidCandidates() {
        assertThatThrownBy(() -> validator.validate(
                new ClinicalEventIntent(ClinicalEventIntent.Kind.EVENTS, List.of(), null)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> validator.validate(new ClinicalEventIntent(
                ClinicalEventIntent.Kind.EVENTS,
                List.of(new ClinicalEventIntent.Candidate(
                        ClinicalEvent.ClinicalEventType.NOTE,
                        " ",
                        null,
                        ClinicalEvent.DatePrecision.UNKNOWN,
                        null)),
                null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("content");
    }

    @Test
    void acceptsQueriesWithTermsOrFilters() {
        validator.validate(ClinicalEventIntent.query(new ClinicalEventIntent.Query(
                "¿Cuándo me diagnosticaron hipertensión?",
                ClinicalEventIntent.Query.Scope.HISTORY,
                List.of("hipertension"),
                null,
                null,
                null)));
        validator.validate(ClinicalEventIntent.query(new ClinicalEventIntent.Query(
                "¿Qué medicamentos tomé?",
                ClinicalEventIntent.Query.Scope.HISTORY,
                List.of(),
                ClinicalEvent.ClinicalEventType.MEDICATION,
                null,
                null)));
    }

    @Test
    void rejectsQueriesWithoutAQuestionOrCriteria() {
        assertThatThrownBy(() -> validator.validate(ClinicalEventIntent.query(new ClinicalEventIntent.Query(
                " ",
                ClinicalEventIntent.Query.Scope.HISTORY,
                List.of("hipertension"),
                null,
                null,
                null))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("question");

        assertThatThrownBy(() -> validator.validate(ClinicalEventIntent.query(new ClinicalEventIntent.Query(
                "¿Cuándo ocurrió?",
                ClinicalEventIntent.Query.Scope.HISTORY,
                List.of("   "),
                null,
                null,
                null))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one");
    }

    @Test
    void rejectsAQueryWithoutScope() {
        assertThatThrownBy(() -> validator.validate(ClinicalEventIntent.query(new ClinicalEventIntent.Query(
                "¿Cuándo ocurrió?",
                null,
                List.of("hipertension"),
                null,
                null,
                null))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("scope");
    }
}
