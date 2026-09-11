package com.careme.backend.service;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.careme.backend.dto.ImportPreviewResponse;
import com.careme.backend.dto.ImportPreviewRow;
import com.careme.backend.dto.ImportResultResponse;
import com.careme.backend.entity.Measurement;
import com.careme.backend.entity.MeasurementDraft;
import com.careme.backend.repository.MeasurementRepository;

/**
 * Loading several measurements from a file, and telling the user beforehand what
 * that would do.
 *
 * <p>The file is read and validated in full before anything is written, and the
 * write happens inside a single transaction: either every measurement of the
 * file is stored, or none is. The preview runs the very same validation and
 * never writes, so what it announces is what the load does.
 */
@Service
public class MeasurementImportService {

    private static final Logger log = LoggerFactory.getLogger(MeasurementImportService.class);

    private final MeasurementCsvParser csvParser;
    private final MeasurementRepository measurementRepository;

    public MeasurementImportService(
            MeasurementCsvParser csvParser,
            MeasurementRepository measurementRepository) {
        this.csvParser = csvParser;
        this.measurementRepository = measurementRepository;
    }

    /**
     * @return what loading the file would do, leaving the stored measurements
     *         untouched
     */
    @Transactional(readOnly = true)
    public ImportPreviewResponse preview(InputStream inputStream) {
        MeasurementCsvParser.ParsedCsv parsed = csvParser.parse(inputStream);
        Set<LocalDate> existingDates = existingDates(parsed);

        List<ImportPreviewRow> rows = parsed.measurements().stream()
                .map(measurement -> new ImportPreviewRow(
                        measurement.date(),
                        measurement.weightKg(),
                        measurement.waistCm(),
                        existingDates.contains(measurement.date())))
                .toList();

        int replacedCount = (int) rows.stream().filter(ImportPreviewRow::replacesExisting).count();
        int newCount = rows.size() - replacedCount;

        log.debug("Previewed {} measurement(s) from {} row(s)", rows.size(), parsed.totalRows());
        return new ImportPreviewResponse(
                rows, parsed.totalRows(), newCount, replacedCount, parsed.ignoredRows());
    }

    /**
     * Stores every measurement of the file, replacing the values of a day that
     * already had one.
     *
     * @return how many records were created, replaced and ignored
     */
    @Transactional
    public ImportResultResponse importMeasurements(InputStream inputStream) {
        MeasurementCsvParser.ParsedCsv parsed = csvParser.parse(inputStream);
        Set<LocalDate> existingDates = existingDates(parsed);

        List<Measurement> stored = measurementRepository.upsertAll(parsed.measurements());
        int replacedCount = (int) stored.stream()
                .filter(measurement -> existingDates.contains(measurement.date()))
                .count();
        int createdCount = stored.size() - replacedCount;

        log.debug(
                "Imported {} measurement(s): {} created, {} replaced, {} ignored",
                stored.size(), createdCount, replacedCount, parsed.ignoredRows());

        return new ImportResultResponse(
                createdCount, replacedCount, parsed.ignoredRows(), parsed.totalRows());
    }

    private Set<LocalDate> existingDates(MeasurementCsvParser.ParsedCsv parsed) {
        return measurementRepository.findExistingDates(
                parsed.measurements().stream().map(MeasurementDraft::date).toList());
    }
}
