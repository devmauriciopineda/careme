package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.careme.backend.dto.ClinicalEventIntent;
import com.careme.backend.entity.ClinicalEvent;
import com.careme.backend.repository.ClinicalEventQueryRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ClinicalHistoryQueryServiceTest {

    private final ClinicalEventQueryRepository repository = mock(ClinicalEventQueryRepository.class);
    private final ClinicalEventIntentValidator validator = new ClinicalEventIntentValidator();

    private static ClinicalEventIntent.Query query() {
        return new ClinicalEventIntent.Query(
                "¿Cuándo me diagnosticaron hipertensión?",
                ClinicalEventIntent.Query.Scope.HISTORY,
                List.of("hipertension"),
                null,
                null,
                null);
    }

    private static ClinicalEvent event(String code, String content) {
        return new ClinicalEvent(
                UUID.randomUUID(), code, ClinicalEvent.ClinicalEventType.DIAGNOSIS,
                LocalDate.of(2026, 1, 10), ClinicalEvent.DatePrecision.EXACT, "el 10 de enero",
                content, ClinicalEvent.EventSource.PATIENT, OffsetDateTime.now(ZoneOffset.UTC));
    }

    @Test
    void answersFromRetrievedEventsUsingTheFakeComposer() {
        var event = event("evt_001", "Hipertensión");
        when(repository.search(any(), any(), any(), any())).thenReturn(List.of(event));
        var service = new ClinicalHistoryQueryService(
                repository,
                (question, events) -> new ClinicalAnswerComposer.ComposedAnswer("Te la diagnosticaron", List.of("evt_001")),
                validator);

        var result = service.answer(ClinicalEventIntent.query(query()));

        assertThat(result.kind()).isEqualTo(ClinicalAnswerResult.Kind.ANSWERED);
        assertThat(result.events()).containsExactly(event);
        assertThat(result.message()).isEqualTo("Te la diagnosticaron");
    }

    @Test
    void attachesTheWholeRetrievedSetWhenTheComposerCitesNothing() {
        var event = event("evt_001", "Hipertensión");
        when(repository.search(any(), any(), any(), any())).thenReturn(List.of(event));
        var service = new ClinicalHistoryQueryService(
                repository,
                (question, events) -> new ClinicalAnswerComposer.ComposedAnswer("Respuesta", List.of()),
                validator);

        var result = service.answer(ClinicalEventIntent.query(query()));

        assertThat(result.kind()).isEqualTo(ClinicalAnswerResult.Kind.ANSWERED);
        assertThat(result.events()).containsExactly(event);
    }

    @Test
    void declaresAbsenceWhenNothingIsRetrieved() {
        when(repository.search(any(), any(), any(), any())).thenReturn(List.of());
        var service = new ClinicalHistoryQueryService(
                repository,
                (question, events) -> new ClinicalAnswerComposer.ComposedAnswer("No debería", List.of()),
                validator);

        var result = service.answer(ClinicalEventIntent.query(query()));

        assertThat(result.kind()).isEqualTo(ClinicalAnswerResult.Kind.NO_RECORDS);
        assertThat(result.events()).isEmpty();
        assertThat(result.message()).contains("No encontré registros");
    }

    @Test
    void failsWhenTheComposerFails() {
        when(repository.search(any(), any(), any(), any())).thenReturn(List.of(event("evt_001", "Hipertensión")));
        var service = new ClinicalHistoryQueryService(
                repository,
                (question, events) -> {
                    throw new LlmIntegrationException("provider detail stays internal");
                },
                validator);

        var result = service.answer(ClinicalEventIntent.query(query()));

        assertThat(result.kind()).isEqualTo(ClinicalAnswerResult.Kind.FAILURE);
        assertThat(result.events()).isEmpty();
        assertThat(result.message()).contains("intentarlo");
    }

    @Test
    void failsWhenTheSearchFails() {
        when(repository.search(any(), any(), any(), any())).thenThrow(new RuntimeException("database unavailable"));
        var service = new ClinicalHistoryQueryService(
                repository,
                (question, events) -> new ClinicalAnswerComposer.ComposedAnswer("No debería", List.of()),
                validator);

        var result = service.answer(ClinicalEventIntent.query(query()));

        assertThat(result.kind()).isEqualTo(ClinicalAnswerResult.Kind.FAILURE);
    }

    @Test
    void rejectsAMalformedQueryBeforeSearching() {
        var malformed = ClinicalEventIntent.query(new ClinicalEventIntent.Query(
                "¿Cuándo?",
                ClinicalEventIntent.Query.Scope.HISTORY,
                List.of(),
                null,
                null,
                null));
        var service = new ClinicalHistoryQueryService(
                repository,
                (question, events) -> new ClinicalAnswerComposer.ComposedAnswer("No debería", List.of()),
                validator);

        var result = service.answer(malformed);

        assertThat(result.kind()).isEqualTo(ClinicalAnswerResult.Kind.FAILURE);
        org.mockito.Mockito.verifyNoInteractions(repository);
    }
}
