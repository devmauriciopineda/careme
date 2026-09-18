package com.careme.backend.service;

import com.careme.backend.dto.ClinicalEventInspectionResponse;
import com.careme.backend.entity.ClinicalEvent;
import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/** Read-only listing and detail access for the clinical event history. */
@Service
public class ClinicalEventInspectionService {

    private static final Comparator<ClinicalEvent> CODE_ASC =
            Comparator.comparing(ClinicalEvent::code);

    private final ClinicalEventMarkdownStore markdownStore;

    public ClinicalEventInspectionService(ClinicalEventMarkdownStore markdownStore) {
        this.markdownStore = markdownStore;
    }

    public List<ClinicalEventInspectionResponse> findAll(
            ClinicalEvent.ClinicalEventType type,
            LocalDate fromDate,
            LocalDate toDate,
            SortBy sortBy) throws IOException {
        return markdownStore.readAll().stream()
                .filter(event -> type == null || event.type() == type)
                .filter(event -> inPeriod(event, fromDate, toDate))
                .sorted(comparator(sortBy == null ? SortBy.RECORD_DATE : sortBy))
                .map(ClinicalEventInspectionService::toResponse)
                .toList();
    }

    public Optional<ClinicalEventInspectionResponse> findByCode(String code) throws IOException {
        try {
            return Optional.of(toResponse(markdownStore.read(code)));
        } catch (java.nio.file.NoSuchFileException exception) {
            return Optional.empty();
        }
    }

    private static boolean inPeriod(ClinicalEvent event, LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null && toDate == null) {
            return true;
        }
        if (event.date() == null) {
            return false;
        }
        return (fromDate == null || !event.date().isBefore(fromDate))
                && (toDate == null || !event.date().isAfter(toDate));
    }

    private static Comparator<ClinicalEvent> comparator(SortBy sortBy) {
        Comparator<ClinicalEvent> byRecordDate = Comparator
                .comparing(ClinicalEvent::createdAt, Comparator.reverseOrder());
        Comparator<ClinicalEvent> byOccurrenceDate = Comparator
                .comparing(ClinicalEvent::date,
                        Comparator.nullsLast(Comparator.reverseOrder()));

        return (sortBy == SortBy.OCCURRENCE_DATE
                ? byOccurrenceDate.thenComparing(byRecordDate)
                : byRecordDate.thenComparing(byOccurrenceDate))
                .thenComparing(CODE_ASC);
    }

    private static ClinicalEventInspectionResponse toResponse(ClinicalEvent event) {
        return new ClinicalEventInspectionResponse(
                event.id(),
                event.code(),
                event.type().name().toLowerCase(),
                event.content(),
                event.date(),
                displayPrecision(event),
                event.createdAt().withOffsetSameInstant(java.time.ZoneOffset.UTC).toLocalDate());
    }

    private static String displayPrecision(ClinicalEvent event) {
        if (event.date() == null || event.datePrecision() == ClinicalEvent.DatePrecision.UNKNOWN) {
            return ClinicalEvent.DatePrecision.UNKNOWN.name().toLowerCase();
        }
        return isRelativeExpression(event.dateText())
                ? ClinicalEvent.DatePrecision.APPROXIMATE.name().toLowerCase()
                : event.datePrecision().name().toLowerCase();
    }

    private static boolean isRelativeExpression(String dateText) {
        if (dateText == null || dateText.isBlank()) {
            return false;
        }
        String normalized = dateText.trim().toLowerCase();
        return normalized.equals("hoy")
                || normalized.equals("ayer")
                || normalized.startsWith("hace ");
    }

    public enum SortBy {
        RECORD_DATE,
        OCCURRENCE_DATE
    }
}