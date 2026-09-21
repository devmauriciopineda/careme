package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.careme.backend.dto.ChatMessageResponse;
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

    /** Composer that must never run, for the paths that declare an absence. */
    private static final ClinicalAnswerComposer NEVER_COMPOSES =
            (question, events) -> new ClinicalAnswerComposer.ComposedAnswer("No debería", List.of());

    private ClinicalHistoryQueryService service(ClinicalAnswerComposer composer) {
        return new ClinicalHistoryQueryService(repository, composer, validator);
    }

    private static ClinicalEventIntent.Query query() {
        return new ClinicalEventIntent.Query(
                "¿Cuándo me diagnosticaron hipertensión?",
                ClinicalEventIntent.Query.Scope.HISTORY,
                List.of("hipertension"),
                null,
                null,
                null);
    }

    private static ClinicalEventIntent.Query queryWithType() {
        return new ClinicalEventIntent.Query(
                "¿Qué medicación tomo?",
                ClinicalEventIntent.Query.Scope.HISTORY,
                List.of("medicacion"),
                ClinicalEvent.ClinicalEventType.MEDICATION,
                null,
                null);
    }

    private static ClinicalEventIntent.Query queryWithPeriod() {
        return new ClinicalEventIntent.Query(
                "¿Qué me pasó el año pasado?",
                ClinicalEventIntent.Query.Scope.HISTORY,
                List.of("paso"),
                null,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31));
    }

    /**
     * Drives one absence reason by stubbing only what the ladder needs to reach
     * it, so each reason is produced the way the service produces it.
     */
    private ClinicalAnswerResult absenceFor(ChatMessageResponse.AbsenceReason reason) {
        ClinicalEventIntent.Query question = switch (reason) {
            case EMPTY_HISTORY -> query();
            case NO_EVENTS_OF_TYPE -> queryWithType();
            case NO_EVENTS_IN_PERIOD -> queryWithPeriod();
            case NO_TERM_MATCH -> query();
            case NO_MEASUREMENTS, NO_MEASUREMENTS_IN_PERIOD, METRIC_NOT_TRACKED ->
                    // The history channel never declares an absence of measurements: the
                    // tracking is what answers a question about them.
                    throw new IllegalArgumentException(
                            "An absence of measurements belongs to the tracking, not to the history");
        };
        when(repository.search(any(), any(), any(), any())).thenReturn(List.of());
        switch (reason) {
            case EMPTY_HISTORY -> when(repository.countAll()).thenReturn(0L);
            case NO_EVENTS_OF_TYPE -> {
                when(repository.countAll()).thenReturn(3L);
                when(repository.countByType(ClinicalEvent.ClinicalEventType.MEDICATION)).thenReturn(0L);
            }
            case NO_EVENTS_IN_PERIOD -> {
                when(repository.countAll()).thenReturn(3L);
                when(repository.countByPeriod(any(), any())).thenReturn(0L);
            }
            case NO_TERM_MATCH -> when(repository.countAll()).thenReturn(3L);
            case NO_MEASUREMENTS, NO_MEASUREMENTS_IN_PERIOD, METRIC_NOT_TRACKED ->
                    throw new IllegalArgumentException(
                            "An absence of measurements belongs to the tracking, not to the history");
        }
        return service(NEVER_COMPOSES).answer(ClinicalEventIntent.query(question));
    }

    private static ClinicalEvent event(String code, String content) {
        return new ClinicalEvent(
                UUID.randomUUID(), code, ClinicalEvent.ClinicalEventType.DIAGNOSIS,
                LocalDate.of(2026, 1, 10), ClinicalEvent.DatePrecision.EXACT, "el 10 de enero",
                content, ClinicalEvent.EventSource.PATIENT, OffsetDateTime.now(ZoneOffset.UTC), null);
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
    void declaresAnEmptyHistoryWhenTheHistoryHoldsNothing() {
        var result = absenceFor(ChatMessageResponse.AbsenceReason.EMPTY_HISTORY);

        assertThat(result.kind()).isEqualTo(ClinicalAnswerResult.Kind.NO_RECORDS);
        assertThat(result.absenceReason()).isEqualTo(ChatMessageResponse.AbsenceReason.EMPTY_HISTORY);
        assertThat(result.events()).isEmpty();
    }

    @Test
    void attributesTheAbsenceToTheTypeBeforeThePeriod() {
        var typedAndDated = new ClinicalEventIntent.Query(
                "¿Qué medicación tomé el año pasado?",
                ClinicalEventIntent.Query.Scope.HISTORY,
                List.of("medicacion"),
                ClinicalEvent.ClinicalEventType.MEDICATION,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31));
        when(repository.search(any(), any(), any(), any())).thenReturn(List.of());
        when(repository.countAll()).thenReturn(4L);
        when(repository.countByType(ClinicalEvent.ClinicalEventType.MEDICATION)).thenReturn(0L);

        var result = service(NEVER_COMPOSES).answer(ClinicalEventIntent.query(typedAndDated));

        assertThat(result.absenceReason()).isEqualTo(ChatMessageResponse.AbsenceReason.NO_EVENTS_OF_TYPE);
        verify(repository, never()).countByPeriod(any(), any());
    }

    @Test
    void scopesTheAbsenceToThePeriodWhenTheTypeHasEvents() {
        var result = absenceFor(ChatMessageResponse.AbsenceReason.NO_EVENTS_IN_PERIOD);

        assertThat(result.absenceReason()).isEqualTo(ChatMessageResponse.AbsenceReason.NO_EVENTS_IN_PERIOD);
    }

    @Test
    void scopesTheAbsenceToTheTermsWhenTheHistoryHasEvents() {
        var result = absenceFor(ChatMessageResponse.AbsenceReason.NO_TERM_MATCH);

        assertThat(result.absenceReason()).isEqualTo(ChatMessageResponse.AbsenceReason.NO_TERM_MATCH);
        verify(repository, never()).countByPeriod(any(), any());
    }

    /** The absences the clinical history declares. The measurement tracking has its own. */
    private static final List<ChatMessageResponse.AbsenceReason> HISTORY_ABSENCES = List.of(
            ChatMessageResponse.AbsenceReason.EMPTY_HISTORY,
            ChatMessageResponse.AbsenceReason.NO_EVENTS_OF_TYPE,
            ChatMessageResponse.AbsenceReason.NO_EVENTS_IN_PERIOD,
            ChatMessageResponse.AbsenceReason.NO_TERM_MATCH);

    @Test
    void namesTheScopeOfEachAbsenceAndLeavesTheFactUndecided() {
        for (ChatMessageResponse.AbsenceReason reason : HISTORY_ABSENCES) {
            var result = absenceFor(reason);

            assertThat(result.absenceReason()).as("motivo de %s", reason).isEqualTo(reason);
            assertThat(result.message().toLowerCase(java.util.Locale.ROOT))
                    .as("mensaje de %s", reason)
                    .contains("no significa que no haya ocurrido")
                    .doesNotContain("error", "fallo", "problema");
        }
    }

    @Test
    void presentsAnEmptyHistoryAsTheInitialStateAndNotAsAFailure() {
        var result = absenceFor(ChatMessageResponse.AbsenceReason.EMPTY_HISTORY);

        assertThat(result.kind()).isEqualTo(ClinicalAnswerResult.Kind.NO_RECORDS);
        assertThat(result.message()).contains("Todavía no hay hechos registrados");
    }

    @Test
    void offersBothActionsOnEveryAbsenceWithoutCarryingAnEvent() {
        for (ChatMessageResponse.AbsenceReason reason : HISTORY_ABSENCES) {
            var result = absenceFor(reason);

            assertThat(result.suggestedActions())
                    .as("acciones de %s", reason)
                    .containsExactlyInAnyOrder(
                            ChatMessageResponse.SuggestedAction.REFORMULATE,
                            ChatMessageResponse.SuggestedAction.REGISTER);
            assertThat(result.events()).as("eventos de %s", reason).isEmpty();
        }
    }

    @Test
    void repeatsTheSameAbsenceForTheSameQuestion() {
        var first = absenceFor(ChatMessageResponse.AbsenceReason.NO_TERM_MATCH);
        var again = absenceFor(ChatMessageResponse.AbsenceReason.NO_TERM_MATCH);

        assertThat(again.absenceReason()).isEqualTo(first.absenceReason());
        assertThat(again.message()).isEqualTo(first.message());
    }

    @Test
    void failsInsteadOfDeclaringAnAbsenceItCannotVerify() {
        when(repository.search(any(), any(), any(), any())).thenReturn(List.of());
        when(repository.countAll()).thenThrow(new RuntimeException("database unavailable"));

        var result = service(NEVER_COMPOSES).answer(ClinicalEventIntent.query(query()));

        assertThat(result.kind()).isEqualTo(ClinicalAnswerResult.Kind.FAILURE);
        assertThat(result.absenceReason()).isNull();
        assertThat(result.suggestedActions()).isEmpty();
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
        assertThat(result.absenceReason()).as("un fallo no declara ausencia").isNull();
        assertThat(result.suggestedActions()).isEmpty();
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

    @Test
    void theShortComposedAnswerEquatesToFullCoverage() {
        var full = new ClinicalAnswerComposer.ComposedAnswer("Respuesta", List.of("evt_001"));

        assertThat(full.coverage()).isEqualTo(ClinicalAnswerComposer.Coverage.FULL);
        assertThat(full.unsupported()).isNull();
    }

    @Test
    void anUnsupportedPartEquatesToPartialCoverage() {
        var partial = new ClinicalAnswerComposer.ComposedAnswer("Respuesta", List.of("evt_001"), "lo demás");

        assertThat(partial.coverage()).isEqualTo(ClinicalAnswerComposer.Coverage.PARTIAL);
        assertThat(partial.unsupported()).isEqualTo("lo demás");
    }

    @Test
    void declaresAbsenceWhenTheRetrievedEventsDoNotAnswerTheQuestion() {
        when(repository.search(any(), any(), any(), any())).thenReturn(List.of(event("evt_001", "Hipertensión")));
        when(repository.countAll()).thenReturn(1L);

        var result = service((question, events) -> new ClinicalAnswerComposer.ComposedAnswer(
                "Los hechos recuperados no responden a la pregunta.",
                List.of(),
                ClinicalAnswerComposer.Coverage.NONE,
                null))
                .answer(ClinicalEventIntent.query(query()));

        assertThat(result.kind()).isEqualTo(ClinicalAnswerResult.Kind.NO_RECORDS);
        assertThat(result.absenceReason()).isEqualTo(ChatMessageResponse.AbsenceReason.NO_TERM_MATCH);
        assertThat(result.events()).as("ningún hecho se presenta como apoyo").isEmpty();
        assertThat(result.message())
                .as("la declaración es del servicio, no la negativa del compositor")
                .doesNotContain("Los hechos recuperados no responden")
                .contains("no significa que no haya ocurrido");
        assertThat(result.suggestedActions()).containsExactlyInAnyOrder(
                ChatMessageResponse.SuggestedAction.REFORMULATE,
                ChatMessageResponse.SuggestedAction.REGISTER);
    }

    @Test
    void treatsAMissingUnsupportedPartAsAFullAnswer() {
        when(repository.search(any(), any(), any(), any())).thenReturn(List.of(event("evt_001", "Hipertensión")));

        var result = service((question, events) -> new ClinicalAnswerComposer.ComposedAnswer(
                "Te la diagnosticaron en enero.", List.of("evt_001")))
                .answer(ClinicalEventIntent.query(query()));

        assertThat(result.kind()).isEqualTo(ClinicalAnswerResult.Kind.ANSWERED);
        assertThat(result.message()).isEqualTo("Te la diagnosticaron en enero.");
        assertThat(result.absenceReason()).isNull();
    }

    @Test
    void answersTheSupportedPartAndDeclaresTheUnsupportedOne() {
        when(repository.search(any(), any(), any(), any())).thenReturn(List.of(event("evt_001", "Hipertensión")));

        var result = service((question, events) -> new ClinicalAnswerComposer.ComposedAnswer(
                "Te la diagnosticaron en enero.", List.of("evt_001"), "lo que pasó el año pasado"))
                .answer(ClinicalEventIntent.query(query()));

        assertThat(result.kind()).isEqualTo(ClinicalAnswerResult.Kind.ANSWERED);
        assertThat(result.message())
                .startsWith("Te la diagnosticaron en enero.")
                .contains("Sobre esta parte no encuentro registros")
                .contains("lo que pasó el año pasado")
                .contains("no significa que no haya ocurrido");
    }

    @Test
    void thePartialDeclarationAddsNoEventAndNoReferenceOfItsOwn() {
        var retrieved = event("evt_001", "Hipertensión");
        when(repository.search(any(), any(), any(), any())).thenReturn(List.of(retrieved));

        var result = service((question, events) -> new ClinicalAnswerComposer.ComposedAnswer(
                "Te la diagnosticaron en enero.", List.of("evt_001"), "el año pasado"))
                .answer(ClinicalEventIntent.query(query()));

        assertThat(result.events()).containsExactly(retrieved);
        assertThat(result.message()).doesNotContain("evt_");
    }

    @Test
    void keepsASingleSentenceEndWhenTheUnsupportedPartAlreadyEndsWithAPeriod() {
        when(repository.search(any(), any(), any(), any())).thenReturn(List.of(event("evt_001", "Hipertensión")));

        var result = service((question, events) -> new ClinicalAnswerComposer.ComposedAnswer(
                "Te la diagnosticaron en enero.", List.of("evt_001"), "qué medicación tomas."))
                .answer(ClinicalEventIntent.query(query()));

        assertThat(result.message())
                .contains("qué medicación tomas. Que no encuentre registros")
                .doesNotContain("..");
    }
}
