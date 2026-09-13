package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.careme.backend.dto.ClinicalEventIntent;
import com.careme.backend.entity.ClinicalEvent;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

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
                                ClinicalEvent.ClinicalEventType.MEASUREMENT,
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
}
