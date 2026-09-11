package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.careme.backend.dto.ImportPreviewResponse;
import com.careme.backend.dto.ImportResultResponse;
import com.careme.backend.entity.Measurement;
import com.careme.backend.entity.MeasurementDraft;
import com.careme.backend.exception.MeasurementImportException;
import com.careme.backend.repository.MeasurementRepository;

@ExtendWith(MockitoExtension.class)
class MeasurementImportServiceTest {

    private static final UUID ID = UUID.fromString("6f1d0f9a-1f5f-4a0e-9a4e-4f0f6a1c2b3d");
    private static final LocalDate REPLACED_DAY = LocalDate.of(2026, 9, 10);
    private static final LocalDate NEW_DAY = LocalDate.of(2026, 9, 8);

    private static final InputStream FILE = InputStream.nullInputStream();

    @Mock
    private MeasurementCsvParser csvParser;

    @Mock
    private MeasurementRepository measurementRepository;

    @InjectMocks
    private MeasurementImportService measurementImportService;

    private static MeasurementDraft draft(LocalDate date, String weightKg, String waistCm) {
        return new MeasurementDraft(date, new BigDecimal(weightKg), new BigDecimal(waistCm));
    }

    private static Measurement measurement(LocalDate date, String weightKg, String waistCm) {
        return new Measurement(ID, date, new BigDecimal(weightKg), new BigDecimal(waistCm));
    }

    private void parserReturns(int totalRows, int ignoredRows, MeasurementDraft... drafts) {
        when(csvParser.parse(FILE)).thenReturn(
                new MeasurementCsvParser.ParsedCsv(List.of(drafts), totalRows, ignoredRows));
    }

    @Test
    void previewsWhichDaysWouldBeCreatedAndWhichReplaced() {
        parserReturns(3, 1,
                draft(REPLACED_DAY, "79.5", "93.0"),
                draft(NEW_DAY, "80.4", "95.2"));
        when(measurementRepository.findExistingDates(any())).thenReturn(Set.of(REPLACED_DAY));

        ImportPreviewResponse preview = measurementImportService.preview(FILE);

        assertThat(preview.totalRows()).isEqualTo(3);
        assertThat(preview.ignoredCount()).isEqualTo(1);
        assertThat(preview.newCount()).isEqualTo(1);
        assertThat(preview.replacedCount()).isEqualTo(1);
        assertThat(preview.rows()).hasSize(2);
        assertThat(preview.rows().getFirst().replacesExisting()).isTrue();
        assertThat(preview.rows().getFirst().weightKg()).isEqualByComparingTo("79.5");
        assertThat(preview.rows().getLast().replacesExisting()).isFalse();
    }

    @Test
    void previewsAFileThatHoldsNothing() {
        parserReturns(1, 1);
        when(measurementRepository.findExistingDates(any())).thenReturn(Set.of());

        ImportPreviewResponse preview = measurementImportService.preview(FILE);

        assertThat(preview.rows()).isEmpty();
        assertThat(preview.newCount()).isZero();
        assertThat(preview.replacedCount()).isZero();
    }

    @Test
    void previewsWithoutWritingAnything() {
        parserReturns(1, 0, draft(NEW_DAY, "80.4", "95.2"));
        when(measurementRepository.findExistingDates(any())).thenReturn(Set.of());

        measurementImportService.preview(FILE);

        verify(measurementRepository, never()).upsertAll(any());
    }

    @Test
    void previewsWithoutTouchingTheStoredDataWhenTheFileIsRejected() {
        when(csvParser.parse(FILE)).thenThrow(new MeasurementImportException(
                MeasurementImportException.INVALID_FILE_STRUCTURE, "wrong columns", List.of()));

        assertThatThrownBy(() -> measurementImportService.preview(FILE))
                .isInstanceOf(MeasurementImportException.class);

        verify(measurementRepository, never()).findExistingDates(any());
        verify(measurementRepository, never()).upsertAll(any());
    }

    @Test
    void loadsEveryMeasurementAndReportsWhatItDid() {
        List<MeasurementDraft> drafts = List.of(
                draft(REPLACED_DAY, "79.5", "93.0"),
                draft(NEW_DAY, "80.4", "95.2"));
        parserReturns(3, 1, drafts.toArray(MeasurementDraft[]::new));
        when(measurementRepository.findExistingDates(any())).thenReturn(Set.of(REPLACED_DAY));
        when(measurementRepository.upsertAll(drafts)).thenReturn(List.of(
                measurement(REPLACED_DAY, "79.5", "93.0"),
                measurement(NEW_DAY, "80.4", "95.2")));

        ImportResultResponse result = measurementImportService.importMeasurements(FILE);

        assertThat(result.totalRows()).isEqualTo(3);
        assertThat(result.ignoredCount()).isEqualTo(1);
        assertThat(result.createdCount()).isEqualTo(1);
        assertThat(result.replacedCount()).isEqualTo(1);
        verify(measurementRepository).upsertAll(drafts);
    }

    @Test
    void loadsNothingWhenTheFileHoldsNothing() {
        parserReturns(0, 0);
        when(measurementRepository.findExistingDates(any())).thenReturn(Set.of());
        when(measurementRepository.upsertAll(List.of())).thenReturn(List.of());

        ImportResultResponse result = measurementImportService.importMeasurements(FILE);

        assertThat(result.createdCount()).isZero();
        assertThat(result.replacedCount()).isZero();
    }

    @Test
    void storesNothingWhenTheFileIsRejected() {
        when(csvParser.parse(FILE)).thenThrow(new MeasurementImportException(
                MeasurementImportException.INVALID_IMPORT_VALUE, "bad rows", List.of("2|weight_kg|NOT_POSITIVE")));

        assertThatThrownBy(() -> measurementImportService.importMeasurements(FILE))
                .isInstanceOf(MeasurementImportException.class);

        verify(measurementRepository, never()).upsertAll(any());
    }

    @Test
    void propagatesAFailureWhileStoring() {
        List<MeasurementDraft> drafts = List.of(draft(NEW_DAY, "80.4", "95.2"));
        parserReturns(1, 0, drafts.toArray(MeasurementDraft[]::new));
        when(measurementRepository.findExistingDates(any())).thenReturn(Set.of());
        when(measurementRepository.upsertAll(drafts))
                .thenThrow(new IllegalStateException("database is unreachable"));

        assertThatThrownBy(() -> measurementImportService.importMeasurements(FILE))
                .isInstanceOf(IllegalStateException.class);
    }
}
