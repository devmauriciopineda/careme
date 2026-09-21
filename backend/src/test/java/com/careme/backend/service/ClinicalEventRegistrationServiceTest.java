package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.careme.backend.dto.ClinicalEventIntent;
import com.careme.backend.entity.ClinicalEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ClinicalEventRegistrationServiceTest {

    private final ClinicalEventMarkdownStore markdownStore = mock(ClinicalEventMarkdownStore.class);
    private final ClinicalEventIndexWriter indexWriter = mock(ClinicalEventIndexWriter.class);
    private final ClinicalEventConversationRegistry registry = new ClinicalEventConversationRegistry();
    private final ClinicalEventRegistrationService service = new ClinicalEventRegistrationService(
            new ClinicalEventIntentValidator(),
            new ClinicalEventDateNormalizer(),
            markdownStore,
            indexWriter,
            registry);

    @TempDir
    Path eventsDirectory;

    @BeforeEach
    void stubReservedCodes() {
        when(markdownStore.reserveCodes(anyInt())).thenAnswer(invocation -> {
            int count = invocation.getArgument(0);
            return IntStream.rangeClosed(1, count).mapToObj(index -> "evt_%03d".formatted(index)).toList();
        });
    }

    private static ClinicalEvent persistedEvent(String code) {
        return new ClinicalEvent(
                UUID.randomUUID(),
                code,
                ClinicalEvent.ClinicalEventType.NOTE,
                LocalDate.of(2026, 9, 1),
                ClinicalEvent.DatePrecision.EXACT,
                "el 1 de septiembre",
                "Nota previa",
                ClinicalEvent.EventSource.PATIENT,
                OffsetDateTime.parse("2026-09-01T10:00:00Z"),
                null);
    }

    private static ClinicalEventIntent noteIntent() {
        return new ClinicalEventIntent(
                ClinicalEventIntent.Kind.EVENTS,
                List.of(new ClinicalEventIntent.Candidate(
                        ClinicalEvent.ClinicalEventType.NOTE,
                        "Tuve fiebre",
                        null,
                        ClinicalEvent.DatePrecision.UNKNOWN,
                        null)),
                null);
    }

    @Test
        void registersMultipleEventsAndRejectsConversationDuplicate() throws Exception {
        var intent = new ClinicalEventIntent(
                ClinicalEventIntent.Kind.EVENTS,
                List.of(
                        new ClinicalEventIntent.Candidate(
                                ClinicalEvent.ClinicalEventType.DIAGNOSIS,
                                "Me diagnosticaron hipertension",
                                null,
                                ClinicalEvent.DatePrecision.UNKNOWN,
                                null),
                        new ClinicalEventIntent.Candidate(
                                ClinicalEvent.ClinicalEventType.NOTE,
                                "Presion 145/92",
                                LocalDate.of(2026, 9, 13),
                                ClinicalEvent.DatePrecision.EXACT,
                                "hoy")),
                null);

        var registered = service.register("conversation-1", intent, LocalDate.of(2026, 9, 13));
        var duplicate = service.register("conversation-1", intent, LocalDate.of(2026, 9, 13));

        assertThat(registered.kind()).isEqualTo(ClinicalEventRegistrationResult.Kind.REGISTERED);
        assertThat(registered.events()).hasSize(2);
        assertThat(duplicate.kind()).isEqualTo(ClinicalEventRegistrationResult.Kind.DUPLICATE);
        assertThat(duplicate.events()).hasSize(2);
        verify(markdownStore).writeAtomically(anyList());
        verify(indexWriter).writeAll(anyList());
    }

    @Test
    void leavesNoPublishedEventWhenIndexWriteFails() throws Exception {
        doThrow(new IllegalStateException("database unavailable")).when(indexWriter).writeAll(anyList());
        var intent = new ClinicalEventIntent(
                ClinicalEventIntent.Kind.EVENTS,
                List.of(new ClinicalEventIntent.Candidate(
                        ClinicalEvent.ClinicalEventType.NOTE,
                        "Tuve fiebre",
                        null,
                        ClinicalEvent.DatePrecision.UNKNOWN,
                        null)),
                null);

        var result = service.register("conversation-2", intent, LocalDate.of(2026, 9, 13));

        assertThat(result.kind()).isEqualTo(ClinicalEventRegistrationResult.Kind.FAILURE);
        assertThat(result.events()).isEmpty();
        verify(indexWriter).deleteAll(anyList());
    }

    @Test
    void allocatesFreeCodesFromThePersistedDocuments() throws Exception {
        ClinicalEventMarkdownStore store = new ClinicalEventMarkdownStore(eventsDirectory);
        store.write(persistedEvent("evt_001"));
        String persisted = Files.readString(eventsDirectory.resolve("evt_001.md"));
        var serviceWithRealStore = new ClinicalEventRegistrationService(
                new ClinicalEventIntentValidator(),
                new ClinicalEventDateNormalizer(),
                store,
                mock(ClinicalEventIndexWriter.class),
                new ClinicalEventConversationRegistry());

        var result = serviceWithRealStore.register("conversation-3", noteIntent(), LocalDate.of(2026, 9, 13));

        assertThat(result.kind()).isEqualTo(ClinicalEventRegistrationResult.Kind.REGISTERED);
        assertThat(result.events()).extracting(ClinicalEvent::code).containsExactly("evt_002");
        assertThat(Files.readString(eventsDirectory.resolve("evt_001.md"))).isEqualTo(persisted);
    }
}
