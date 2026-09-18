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

    @Test
    void rejectsNullIntentAndConversationContainingEvents() {
        assertThatThrownBy(() -> validator.validate(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be null");

        var candidate = new ClinicalEventIntent.Candidate(
                ClinicalEvent.ClinicalEventType.NOTE,
                "Observación",
                null,
                ClinicalEvent.DatePrecision.UNKNOWN,
                null);
        assertThatThrownBy(() -> validator.validate(new ClinicalEventIntent(
                ClinicalEventIntent.Kind.CONVERSATION,
                List.of(candidate),
                null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Non-event");
    }

    @Test
    void rejectsMalformedCandidatesAndMissingExactDates() {
        assertThatThrownBy(() -> validator.validate(new ClinicalEventIntent(
                ClinicalEventIntent.Kind.EVENTS,
                List.of(new ClinicalEventIntent.Candidate(
                        null, "contenido", null, ClinicalEvent.DatePrecision.UNKNOWN, null)),
                null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("type");

        assertThatThrownBy(() -> validator.validate(new ClinicalEventIntent(
                ClinicalEventIntent.Kind.EVENTS,
                List.of(new ClinicalEventIntent.Candidate(
                        ClinicalEvent.ClinicalEventType.NOTE,
                        "contenido",
                        null,
                        null,
                        null)),
                null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("precision");

        assertThatThrownBy(() -> validator.validate(new ClinicalEventIntent(
                ClinicalEventIntent.Kind.EVENTS,
                List.of(new ClinicalEventIntent.Candidate(
                        ClinicalEvent.ClinicalEventType.NOTE,
                        "contenido",
                        null,
                        ClinicalEvent.DatePrecision.EXACT,
                        " ")),
                null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("needs a date");
    }

    @Test
    void acceptsExactCandidatesWithDateTextAndQueriesWithDateFilters() {
        validator.validate(new ClinicalEventIntent(
                ClinicalEventIntent.Kind.EVENTS,
                List.of(new ClinicalEventIntent.Candidate(
                        ClinicalEvent.ClinicalEventType.NOTE,
                        "contenido",
                        null,
                        ClinicalEvent.DatePrecision.EXACT,
                        "el lunes")),
                null));

        validator.validate(ClinicalEventIntent.query(new ClinicalEventIntent.Query(
                "¿Qué ocurrió en enero?",
                ClinicalEventIntent.Query.Scope.HISTORY,
                List.of(),
                null,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31))));
        validator.validate(ClinicalEventIntent.query(new ClinicalEventIntent.Query(
                "¿Qué ocurrió después de enero?",
                ClinicalEventIntent.Query.Scope.HISTORY,
                List.of(),
                null,
                LocalDate.of(2026, 1, 1),
                null)));
        validator.validate(ClinicalEventIntent.query(new ClinicalEventIntent.Query(
                "¿Qué ocurrió antes de enero?",
                ClinicalEventIntent.Query.Scope.HISTORY,
                List.of(),
                null,
                null,
                LocalDate.of(2026, 1, 31))));
    }

    @Test
    void rejectsNullCandidateContentAndQueriesWithoutCriteria() {
        assertThatThrownBy(() -> validator.validate(new ClinicalEventIntent(
                ClinicalEventIntent.Kind.EVENTS,
                List.of(new ClinicalEventIntent.Candidate(
                        ClinicalEvent.ClinicalEventType.NOTE, null, null,
                        ClinicalEvent.DatePrecision.UNKNOWN, null)),
                null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("content");

        assertThatThrownBy(() -> validator.validate(ClinicalEventIntent.query(new ClinicalEventIntent.Query(
                "¿Qué ocurrió?",
                ClinicalEventIntent.Query.Scope.HISTORY,
                null,
                null,
                null,
                null))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one");
    }

    @Test
    void acceptsAllDatePrecisionCombinationsThatHaveValidDateInformation() {
        validator.validate(new ClinicalEventIntent(
                ClinicalEventIntent.Kind.EVENTS,
                List.of(new ClinicalEventIntent.Candidate(
                        ClinicalEvent.ClinicalEventType.NOTE, "fecha exacta", LocalDate.of(2026, 1, 1),
                        ClinicalEvent.DatePrecision.EXACT, null)),
                null));
        validator.validate(new ClinicalEventIntent(
                ClinicalEventIntent.Kind.EVENTS,
                List.of(new ClinicalEventIntent.Candidate(
                        ClinicalEvent.ClinicalEventType.NOTE, "fecha textual", null,
                        ClinicalEvent.DatePrecision.EXACT, "hace unos días")),
                null));
        validator.validate(new ClinicalEventIntent(
                ClinicalEventIntent.Kind.EVENTS,
                List.of(new ClinicalEventIntent.Candidate(
                        ClinicalEvent.ClinicalEventType.NOTE, "fecha aproximada", null,
                        ClinicalEvent.DatePrecision.APPROXIMATE, null)),
                null));
    }
}
