package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.careme.backend.dto.ChatMessageResponse;
import com.careme.backend.dto.ClinicalEventIntent;
import com.careme.backend.entity.ClinicalEvent;
import com.careme.backend.entity.Encounter;
import com.careme.backend.entity.EncounterNote;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

/**
 * The life cycle of the consultation: it opens with the conversation, collects
 * notes without touching the history, and only the close registers.
 */
class EncounterServiceTest {

    private static final String CONVERSATION_ID = "conversation-1";
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 19);

    @TempDir
    Path encountersDirectory;

    private final EncounterIndexWriter indexWriter = mock(EncounterIndexWriter.class);
    private final ClinicalEventRegistrationService registrationService =
            mock(ClinicalEventRegistrationService.class);

    @Test
    void registersARepeatedNoteOnlyOnce() throws Exception {
        when(registrationService.register(anyString(), any(), any(), anyString()))
                .thenReturn(new ClinicalEventRegistrationResult(
                        ClinicalEventRegistrationResult.Kind.REGISTERED,
                        List.of(event("evt_001", "colesterol alto")),
                        "He registrado el hecho."));
        EncounterNote repeated = new EncounterNote(
                ClinicalEvent.ClinicalEventType.DIAGNOSIS,
                "colesterol alto",
                LocalDate.of(2025, 3, 1),
                ClinicalEvent.DatePrecision.APPROXIMATE,
                "hace unos dos años");
        store().create(Encounter.opened(UUID.randomUUID(), "enc_001", CONVERSATION_ID, OffsetDateTime.now())
                .withNote(repeated)
                .withNote(repeated));

        service().close(CONVERSATION_ID);

        ArgumentCaptor<ClinicalEventIntent> intent = ArgumentCaptor.forClass(ClinicalEventIntent.class);
        verify(registrationService).register(anyString(), intent.capture(), any(), anyString());
        assertThat(intent.getValue().events())
                .as("una nota repetida se registra una sola vez")
                .hasSize(1);
    }

    private EncounterService service() {
        return new EncounterService(
                new EncounterMarkdownStore(encountersDirectory),
                indexWriter,
                registrationService,
                new ClinicalEventDateNormalizer(),
                new MeasurementRegistrationService(
                        mock(com.careme.backend.repository.MetricMeasurementRepository.class),
                        mock(MetricCatalogService.class)));
    }

    private EncounterMarkdownStore store() {
        return new EncounterMarkdownStore(encountersDirectory);
    }

    @Test
    void opensAConsultationThatIsInProgressForTheConversation() throws Exception {
        Encounter encounter = service().current(CONVERSATION_ID);

        assertThat(encounter.code()).isEqualTo("enc_001");
        assertThat(encounter.conversationId()).isEqualTo(CONVERSATION_ID);
        assertThat(encounter.isOpen()).isTrue();
        assertThat(store().read("enc_001")).isEqualTo(encounter);
    }

    @Test
    void reusesTheOpenConsultationOfTheSameConversation() {
        EncounterService service = service();
        Encounter first = service.current(CONVERSATION_ID);

        Encounter again = service.current(CONVERSATION_ID);

        assertThat(again.code()).isEqualTo(first.code());
        verify(indexWriter).write(first);
    }

    @Test
    void recoversTheConsultationThatSurvivedARestart() throws Exception {
        Encounter opened = service().current(CONVERSATION_ID);

        // A new service over the same directory finds the consultation that was
        // already persisted instead of opening a second one.
        Encounter recovered = service().current(CONVERSATION_ID);

        assertThat(recovered.code()).isEqualTo(opened.code());
        assertThat(recovered.conversationId()).isEqualTo(CONVERSATION_ID);
    }

    @Test
    void opensTheNextConsultationOnceTheOpenOneIsClosed() throws Exception {
        EncounterService service = service();
        Encounter first = service.current(CONVERSATION_ID);
        service.close(CONVERSATION_ID);

        Encounter second = service.current(CONVERSATION_ID);

        assertThat(second.code()).isNotEqualTo(first.code());
        assertThat(second.isOpen()).isTrue();
    }

    @Test
    void collectsANotePreservingTheTemporalPrecision() {
        EncounterNoteResult result = service().collect(
                CONVERSATION_ID,
                candidate("colesterol alto", ClinicalEvent.DatePrecision.APPROXIMATE, LocalDate.of(2025, 3, 1)),
                TODAY);

        assertThat(result.kind()).isEqualTo(EncounterNoteResult.Kind.COLLECTED);
        assertThat(result.note().content()).isEqualTo("colesterol alto");
        assertThat(result.note().datePrecision()).isEqualTo(ClinicalEvent.DatePrecision.APPROXIMATE);
    }

    @Test
    void refusesToCollectTheSameFactTwiceInTheSameConsultation() throws Exception {
        EncounterService service = service();
        ClinicalEventIntent.Candidate candidate =
                candidate("colesterol alto", ClinicalEvent.DatePrecision.APPROXIMATE, LocalDate.of(2025, 3, 1));
        service.collect(CONVERSATION_ID, candidate, TODAY);

        EncounterNoteResult again = service.collect(CONVERSATION_ID, candidate, TODAY);

        assertThat(again.kind()).isEqualTo(EncounterNoteResult.Kind.DUPLICATE);
        assertThat(store().findOpenByConversation(CONVERSATION_ID).orElseThrow().notes()).hasSize(1);
    }

    @Test
    void closesAConsultationWithNoFactsWithoutRegisteringAnything() {
        EncounterService service = service();
        service.current(CONVERSATION_ID);

        ChatMessageResponse response = service.close(CONVERSATION_ID);

        assertThat(response.status()).isEqualTo(ChatMessageResponse.Status.NOTHING_TO_REGISTER);
        assertThat(response.events()).isEmpty();
        verify(registrationService, never()).register(anyString(), any(), any(), anyString());
    }

    @Test
    void reportsNothingToRegisterWhenNothingWasOpen() {
        ChatMessageResponse response = service().close("conversation-without-consultation");

        assertThat(response.status()).isEqualTo(ChatMessageResponse.Status.NOTHING_TO_REGISTER);
        assertThat(response.events()).isEmpty();
    }

    @Test
    void closesRegisteringTheAdmissibleNotesWithTheirProvenance() throws Exception {
        when(registrationService.register(anyString(), any(), any(), anyString()))
                .thenReturn(registered(event("evt_001", "Hipertensión diagnosticada.")));
        EncounterService service = service();
        service.collect(CONVERSATION_ID, candidate("Hipertensión", ClinicalEvent.DatePrecision.APPROXIMATE, null), TODAY);

        ChatMessageResponse response = service.close(CONVERSATION_ID);

        assertThat(response.status()).isEqualTo(ChatMessageResponse.Status.REGISTERED);
        assertThat(response.events()).singleElement()
                .satisfies(summary -> assertThat(summary.code()).isEqualTo("evt_001"));
        Encounter closed = store().read("enc_001");
        assertThat(closed.isOpen()).isFalse();
        assertThat(closed.closedAt()).isNotNull();
        assertThat(closed.summary()).contains("Hipertensión diagnosticada.");
    }

    @Test
    void closesRegisteringMoreThanOneFact() {
        when(registrationService.register(anyString(), any(), any(), anyString()))
                .thenReturn(new ClinicalEventRegistrationResult(
                        ClinicalEventRegistrationResult.Kind.REGISTERED,
                        List.of(event("evt_001", "Hipertensión diagnosticada."),
                                event("evt_002", "Colesterol alto.")),
                        "He registrado los hechos."));
        EncounterService service = service();
        service.collect(CONVERSATION_ID, candidate("Hipertensión", ClinicalEvent.DatePrecision.APPROXIMATE, null), TODAY);
        service.collect(CONVERSATION_ID, candidate("Colesterol", ClinicalEvent.DatePrecision.APPROXIMATE, null), TODAY);

        ChatMessageResponse response = service.close(CONVERSATION_ID);

        assertThat(response.status()).isEqualTo(ChatMessageResponse.Status.REGISTERED);
        assertThat(response.message()).contains("2 hechos");
    }

    @Test
    void reportsDuplicateWhenEverythingWasAlreadyRegistered() {
        when(registrationService.register(anyString(), any(), any(), anyString()))
                .thenReturn(new ClinicalEventRegistrationResult(
                        ClinicalEventRegistrationResult.Kind.DUPLICATE, List.of(), "Ya estaba registrado."));
        EncounterService service = service();
        service.collect(CONVERSATION_ID, candidate("Hipertensión", ClinicalEvent.DatePrecision.APPROXIMATE, null), TODAY);

        ChatMessageResponse response = service.close(CONVERSATION_ID);

        assertThat(response.status()).isEqualTo(ChatMessageResponse.Status.DUPLICATE);
        assertThat(response.message()).contains("ya estaba registrado");
    }

    @Test
    void reportsNothingToRegisterWhenTheRegistrationProducedNoFacts() {
        when(registrationService.register(anyString(), any(), any(), anyString()))
                .thenReturn(new ClinicalEventRegistrationResult(
                        ClinicalEventRegistrationResult.Kind.REGISTERED, List.of(), ""));
        EncounterService service = service();
        service.collect(CONVERSATION_ID, candidate("Hipertensión", ClinicalEvent.DatePrecision.APPROXIMATE, null), TODAY);

        ChatMessageResponse response = service.close(CONVERSATION_ID);

        assertThat(response.status()).isEqualTo(ChatMessageResponse.Status.NOTHING_TO_REGISTER);
    }

    @Test
    void repeatingACloseReturnsTheOutcomeItAlreadyProduced() {
        when(registrationService.register(anyString(), any(), any(), anyString()))
                .thenReturn(registered(event("evt_001", "Hipertensión diagnosticada.")));
        EncounterService service = service();
        service.collect(CONVERSATION_ID, candidate("Hipertensión", ClinicalEvent.DatePrecision.APPROXIMATE, null), TODAY);
        ChatMessageResponse first = service.close(CONVERSATION_ID);

        ChatMessageResponse second = service.close(CONVERSATION_ID);

        assertThat(second).isEqualTo(first);
        verify(registrationService, times(1)).register(anyString(), any(), any(), anyString());
    }

    @Test
    void aCloseThatCannotCompleteLeavesTheConsultationOpenToRetry() throws Exception {
        when(registrationService.register(anyString(), any(), any(), anyString()))
                .thenReturn(new ClinicalEventRegistrationResult(
                        ClinicalEventRegistrationResult.Kind.FAILURE, List.of(), "No pude registrar el hecho."));
        EncounterService service = service();
        service.collect(CONVERSATION_ID, candidate("Hipertensión", ClinicalEvent.DatePrecision.APPROXIMATE, null), TODAY);

        ChatMessageResponse failed = service.close(CONVERSATION_ID);

        assertThat(failed.status()).isEqualTo(ChatMessageResponse.Status.FAILED);
        assertThat(store().findOpenByConversation(CONVERSATION_ID)).isPresent();

        // Retrying the close keeps what was collected and can now register it.
        when(registrationService.register(anyString(), any(), any(), anyString()))
                .thenReturn(registered(event("evt_001", "Hipertensión diagnosticada.")));
        ChatMessageResponse retried = service.close(CONVERSATION_ID);

        assertThat(retried.status()).isEqualTo(ChatMessageResponse.Status.REGISTERED);
        assertThat(store().findOpenByConversation(CONVERSATION_ID)).isEmpty();
    }

    @Test
    void openingANewConversationClosesThePreviousConsultation() throws Exception {
        when(registrationService.register(anyString(), any(), any(), anyString()))
                .thenReturn(registered(event("evt_001", "Hipertensión diagnosticada.")));
        EncounterService service = service();
        service.collect(CONVERSATION_ID, candidate("Hipertensión", ClinicalEvent.DatePrecision.APPROXIMATE, null), TODAY);

        Encounter second = service.current("conversation-2");

        assertThat(second.conversationId()).isEqualTo("conversation-2");
        Encounter previous = store().read("enc_001");
        assertThat(previous.isOpen()).isFalse();
        assertThat(previous.closedAt()).isNotNull();
    }

    @Test
    void closesEveryConsultationLeftOpenBeforeTheProcessEnded() throws Exception {
        when(registrationService.register(anyString(), any(), any(), anyString()))
                .thenReturn(registered(event("evt_001", "Hipertensión diagnosticada.")));
        EncounterService service = service();
        service.collect(CONVERSATION_ID, candidate("Hipertensión", ClinicalEvent.DatePrecision.APPROXIMATE, null), TODAY);

        int closed = service.closeAbandoned();

        assertThat(closed).isEqualTo(1);
        assertThat(store().findOpenByConversation(CONVERSATION_ID)).isEmpty();
        assertThat(store().read("enc_001").closedAt()).isNotNull();
    }

    @Test
    void derivesAMotiveNoLongerThanTheConfiguredLimit() throws Exception {
        String longContent = "a".repeat(90);
        when(registrationService.register(anyString(), any(), any(), anyString()))
                .thenReturn(registered(event("evt_001", longContent)));
        EncounterService service = service();
        service.collect(CONVERSATION_ID, candidate("Hipertensión", ClinicalEvent.DatePrecision.APPROXIMATE, null), TODAY);

        service.close(CONVERSATION_ID);

        Encounter closed = store().read("enc_001");
        assertThat(closed.motive()).hasSize(60).endsWith("...");
        assertThat(closed.summary()).contains(longContent);
    }

    private static ClinicalEventRegistrationResult registered(ClinicalEvent event) {
        return new ClinicalEventRegistrationResult(
                ClinicalEventRegistrationResult.Kind.REGISTERED, List.of(event), "He registrado el hecho.");
    }

    private static ClinicalEventIntent.Candidate candidate(
            String content, ClinicalEvent.DatePrecision precision, LocalDate date) {
        return new ClinicalEventIntent.Candidate(
                ClinicalEvent.ClinicalEventType.DIAGNOSIS, content, date, precision, null);
    }

    private static ClinicalEvent event(String code, String content) {
        return new ClinicalEvent(
                UUID.randomUUID(),
                code,
                ClinicalEvent.ClinicalEventType.DIAGNOSIS,
                LocalDate.of(2026, 1, 10),
                ClinicalEvent.DatePrecision.EXACT,
                null,
                content,
                ClinicalEvent.EventSource.PATIENT,
                OffsetDateTime.now(ZoneOffset.UTC),
                null);
    }
}
