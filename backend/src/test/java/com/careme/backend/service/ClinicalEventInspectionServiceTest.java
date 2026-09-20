package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.careme.backend.entity.ClinicalEvent;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ClinicalEventInspectionServiceTest {

    private final ClinicalEventMarkdownStore markdownStore = mock(ClinicalEventMarkdownStore.class);
    private final ClinicalEventInspectionService service = new ClinicalEventInspectionService(markdownStore);

    @Test
    void exposesOccurrenceAndRecordDatesAndDefaultsToRecordDateOrder() throws Exception {
        var olderRecord = event("evt_001", LocalDate.of(2026, 9, 12),
                LocalDate.of(2026, 9, 1), "exact");
        var newerRecord = event("evt_002", LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 2), "exact");
        when(markdownStore.readAll()).thenReturn(List.of(olderRecord, newerRecord));

        var result = service.findAll(null, null, null, null);

        assertThat(result).extracting("code").containsExactly("evt_002", "evt_001");
        assertThat(result.get(0).occurrenceDate()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(result.get(0).recordDate()).isEqualTo(LocalDate.of(2026, 9, 2));
    }

    @Test
    void ordersByOccurrenceDateAndPlacesUnknownOccurrenceDatesLast() throws Exception {
        var olderOccurrence = event("evt_001", LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 3), "exact");
        var newerOccurrence = event("evt_002", LocalDate.of(2026, 9, 12),
                LocalDate.of(2026, 9, 1), "exact");
        var unknownOccurrence = event("evt_003", null,
                LocalDate.of(2026, 9, 14), "unknown");
        when(markdownStore.readAll()).thenReturn(List.of(unknownOccurrence, olderOccurrence, newerOccurrence));

        var result = service.findAll(null, null, null, ClinicalEventInspectionService.SortBy.OCCURRENCE_DATE);

        assertThat(result).extracting("code").containsExactly("evt_002", "evt_001", "evt_003");
        assertThat(result.get(2).occurrenceDatePrecision()).isEqualTo("unknown");
    }

    @Test
    void convertsRelativeOccurrenceTextToApproximatePrecisionWithoutExposingText() throws Exception {
        var event = event("evt_001", LocalDate.of(2026, 9, 3),
                LocalDate.of(2026, 9, 17), "exact", "hace dos semanas");
        when(markdownStore.readAll()).thenReturn(List.of(event));

        var result = service.findAll(null, null, null, null);

        assertThat(result.get(0).occurrenceDate()).isEqualTo(LocalDate.of(2026, 9, 3));
        assertThat(result.get(0).occurrenceDatePrecision()).isEqualTo("approximate");
        assertThat(result.get(0).recordDate()).isEqualTo(LocalDate.of(2026, 9, 17));
    }

    @Test
    void filtersByTypeAndInclusiveOccurrencePeriod() throws Exception {
        var included = event("evt_001", LocalDate.of(2026, 9, 10),
                LocalDate.of(2026, 9, 11), "exact");
        var wrongType = event("evt_002", LocalDate.of(2026, 9, 10),
                LocalDate.of(2026, 9, 12), "exact", ClinicalEvent.ClinicalEventType.NOTE);
        when(markdownStore.readAll()).thenReturn(List.of(included, wrongType));

        var result = service.findAll(
                ClinicalEvent.ClinicalEventType.DIAGNOSIS,
                LocalDate.of(2026, 9, 10),
                LocalDate.of(2026, 9, 10),
                null);

        assertThat(result).extracting("code").containsExactly("evt_001");
    }

    @Test
    void readsOneEventAndReturnsEmptyWhenTheDocumentDoesNotExist() throws Exception {
        var event = event("evt_001", LocalDate.of(2026, 9, 10),
                LocalDate.of(2026, 9, 11), "exact");
        when(markdownStore.read("evt_001")).thenReturn(event);
        when(markdownStore.read("evt_999"))
                .thenThrow(new java.nio.file.NoSuchFileException("evt_999.md"));

        assertThat(service.findByCode("evt_001")).containsInstanceOf(
                com.careme.backend.dto.ClinicalEventInspectionResponse.class);
        assertThat(service.findByCode("evt_999")).isEqualTo(Optional.empty());
    }

    @Test
    void preservesUnknownOccurrenceAndFiltersOutUndatedEventsInAPeriod() throws Exception {
        var unknown = event("evt_001", null, LocalDate.of(2026, 9, 11), "unknown");
        when(markdownStore.readAll()).thenReturn(List.of(unknown));

        assertThat(service.findAll(null, LocalDate.of(2026, 9, 1), null,
                ClinicalEventInspectionService.SortBy.OCCURRENCE_DATE)).isEmpty();
        when(markdownStore.readAll()).thenReturn(List.of(unknown));
        assertThat(service.findAll(null, null, null, ClinicalEventInspectionService.SortBy.RECORD_DATE))
                .singleElement().extracting("occurrenceDatePrecision").isEqualTo("unknown");
    }

    @Test
    void appliesOpenPeriodBoundsAndKeepsEventsOnTheBoundary() throws Exception {
        var before = event("evt_001", LocalDate.of(2026, 8, 31), LocalDate.of(2026, 9, 1), "exact");
        var included = event("evt_002", LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 2), "exact");
        var after = event("evt_003", LocalDate.of(2026, 9, 20), LocalDate.of(2026, 9, 3), "exact");
        when(markdownStore.readAll()).thenReturn(List.of(before, included, after));

        assertThat(service.findAll(null, LocalDate.of(2026, 9, 10), null, null))
                .extracting("code").containsExactly("evt_003", "evt_002");
        when(markdownStore.readAll()).thenReturn(List.of(before, included, after));
        assertThat(service.findAll(null, null, LocalDate.of(2026, 9, 10), null))
                .extracting("code").containsExactly("evt_002", "evt_001");
    }

    @Test
    void exposesExactPrecisionAndNormalizesRelativeDateText() throws Exception {
        var exact = event("evt_001", LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 11), "exact",
                "el 10 de septiembre");
        var today = event("evt_002", LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 12), "exact", "hoy");
        var yesterday = event("evt_003", LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 13), "exact", "ayer");
        var noText = event("evt_004", LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 14), "exact", (String) null);
        var blankText = event("evt_005", LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 15), "exact", " ");
        when(markdownStore.readAll()).thenReturn(List.of(exact, today, yesterday, noText, blankText));

        var result = service.findAll(null, null, null, null);

        assertThat(result).extracting("occurrenceDatePrecision")
                .containsExactly("exact", "exact", "approximate", "approximate", "exact");
    }

    @Test
    void doesNotWriteWhenReadingFails() throws Exception {
        doThrow(new java.io.IOException("read failed")).when(markdownStore).readAll();

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.findAll(null, null, null, null))
                .isInstanceOf(java.io.IOException.class);
    }

    private static ClinicalEvent event(String code, LocalDate occurrenceDate, LocalDate recordDate,
            String precision) {
        return event(code, occurrenceDate, recordDate, precision, "el 10 de septiembre");
    }

    private static ClinicalEvent event(String code, LocalDate occurrenceDate, LocalDate recordDate,
            String precision, String dateText) {
        return event(code, occurrenceDate, recordDate, precision, dateText,
                ClinicalEvent.ClinicalEventType.DIAGNOSIS);
    }

    private static ClinicalEvent event(String code, LocalDate occurrenceDate, LocalDate recordDate,
            String precision, ClinicalEvent.ClinicalEventType type) {
        return event(code, occurrenceDate, recordDate, precision, "el 10 de septiembre", type);
    }

    private static ClinicalEvent event(String code, LocalDate occurrenceDate, LocalDate recordDate,
            String precision, String dateText, ClinicalEvent.ClinicalEventType type) {
        return new ClinicalEvent(
                UUID.randomUUID(), code, type, occurrenceDate,
                ClinicalEvent.DatePrecision.valueOf(precision.toUpperCase()), dateText,
                "Evento de prueba", ClinicalEvent.EventSource.PATIENT,
                recordDate.atStartOfDay().atOffset(ZoneOffset.UTC),
                null);
    }
}