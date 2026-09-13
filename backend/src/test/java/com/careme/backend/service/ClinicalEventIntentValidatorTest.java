package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.careme.backend.dto.ClinicalEventIntent;
import com.careme.backend.entity.ClinicalEvent;
import java.time.LocalDate;
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

        validator.validate(new ClinicalEventIntent(ClinicalEventIntent.Kind.EVENTS, java.util.List.of(candidate), null));
        validator.validate(ClinicalEventIntent.clarification("Que fecha tuvo el hecho?"));
        validator.validate(ClinicalEventIntent.conversation());
    }

    @Test
    void rejectsEmptyEventsAndInvalidCandidates() {
        assertThatThrownBy(() -> validator.validate(
                new ClinicalEventIntent(ClinicalEventIntent.Kind.EVENTS, java.util.List.of(), null)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> validator.validate(new ClinicalEventIntent(
                ClinicalEventIntent.Kind.EVENTS,
                java.util.List.of(new ClinicalEventIntent.Candidate(
                        ClinicalEvent.ClinicalEventType.NOTE,
                        " ",
                        null,
                        ClinicalEvent.DatePrecision.UNKNOWN,
                        null)),
                null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("content");
    }
}
