package com.careme.backend.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.careme.backend.dto.MeasurementRequest;
import com.careme.backend.entity.MeasurementDraft;
import com.careme.backend.exception.MeasurementImportException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

/**
 * Reads the measurements file and turns it into drafts the rest of the
 * application can store.
 *
 * <p>It validates every row through {@link MeasurementRequest}, so the rules of a
 * single measurement live in exactly one place: a file cannot accept a value that
 * the registration form would reject. Validation is complete before it returns,
 * which is what makes an all-or-nothing load possible.
 *
 * <p>It never writes a user-facing sentence. Problems are reported as
 * {@code line|field|reason}, and the client turns them into the user's language.
 */
@Component
public class MeasurementCsvParser {

    public static final String DATE_HEADER = "date";
    public static final String WEIGHT_HEADER = "weight_kg";
    public static final String WAIST_HEADER = "abdominal_circumference_cm";

    /** Required columns, in the order they are expected. */
    public static final List<String> REQUIRED_HEADERS = List.of(DATE_HEADER, WEIGHT_HEADER, WAIST_HEADER);

    public static final String MISSING_DATE = "MISSING_DATE";
    public static final String INVALID_DATE = "INVALID_DATE";
    public static final String FUTURE_DATE = "FUTURE_DATE";
    public static final String MISSING_VALUE = "MISSING_VALUE";
    public static final String NOT_A_NUMBER = "NOT_A_NUMBER";
    public static final String NOT_POSITIVE = "NOT_POSITIVE";
    public static final String TOO_MANY_DECIMALS = "TOO_MANY_DECIMALS";
    public static final String TOO_LARGE = "TOO_LARGE";
    /** A rule that changed shape upstream; reported rather than silently dropped. */
    public static final String INVALID_VALUE = "INVALID_VALUE";

    /** Bounds the error payload when a file is wrong from the first line to the last. */
    private static final int MAX_REPORTED_ISSUES = 50;

    /**
     * Which reason to report for a violated rule. Kept next to the rules it
     * mirrors, so adding a constraint without a reason is a compile-time visible
     * gap rather than a silently missing message.
     */
    private static final Map<Class<?>, String> REASONS_BY_CONSTRAINT = Map.of(
            PastOrPresent.class, FUTURE_DATE,
            DecimalMin.class, NOT_POSITIVE,
            DecimalMax.class, TOO_LARGE,
            Digits.class, TOO_MANY_DECIMALS,
            NotNull.class, MISSING_VALUE);

    private final Validator validator;
    private final int maxRows;

    public MeasurementCsvParser(Validator validator, @Value("${careme.import.max-rows:10000}") int maxRows) {
        this.validator = validator;
        this.maxRows = maxRows;
    }

    /**
     * @param inputStream the file content
     * @return the valid measurements, deduplicated by date, plus the row counts
     * @throws MeasurementImportException when the file cannot be read, does not
     *                                    carry the required columns, exceeds the
     *                                    row limit, or holds an invalid row
     */
    public ParsedCsv parse(InputStream inputStream) {
        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                CSVParser csvParser = csvFormat().parse(reader)) {

            validateHeaders(csvParser.getHeaderNames());

            Map<LocalDate, MeasurementDraft> byDate = new LinkedHashMap<>();
            List<String> issues = new ArrayList<>();
            int dataRows = 0;
            int ignoredRows = 0;

            for (CSVRecord record : csvParser) {
                dataRows++;
                if (dataRows > maxRows) {
                    throw tooLarge();
                }

                // The header occupies the first line, so a data row at index n sits on line n + 1.
                int line = dataRows + 1;
                String rawDate = record.get(DATE_HEADER);
                String rawWeight = record.get(WEIGHT_HEADER);
                String rawWaist = record.get(WAIST_HEADER);

                if (rawDate.isEmpty() && rawWeight.isEmpty() && rawWaist.isEmpty()) {
                    ignoredRows++;
                    continue;
                }
                if (rawDate.isEmpty()) {
                    issues.add(issue(line, DATE_HEADER, MISSING_DATE));
                    continue;
                }
                // A date without any value is not a measurement; it is not an error either.
                if (rawWeight.isEmpty() && rawWaist.isEmpty()) {
                    ignoredRows++;
                    continue;
                }

                List<String> rowIssues = new ArrayList<>();
                LocalDate date = parseDate(rawDate, line, rowIssues);
                BigDecimal weightKg = parseMetric(rawWeight, WEIGHT_HEADER, line, rowIssues);
                BigDecimal waistCm = parseMetric(rawWaist, WAIST_HEADER, line, rowIssues);

                if (rowIssues.isEmpty()) {
                    MeasurementRequest request = new MeasurementRequest(date, weightKg, waistCm);
                    rowIssues.addAll(describeViolations(request, line));
                }

                if (!rowIssues.isEmpty()) {
                    issues.addAll(rowIssues);
                    continue;
                }

                // At most one measurement per day: a repeated date replaces the previous row.
                byDate.put(date, new MeasurementDraft(date, weightKg, waistCm));
            }

            if (!issues.isEmpty()) {
                throw invalidValues(issues);
            }

            return new ParsedCsv(List.copyOf(byDate.values()), dataRows, ignoredRows);
        } catch (IOException ex) {
            throw new MeasurementImportException(
                    MeasurementImportException.UNREADABLE_FILE,
                    "The file could not be read while it was being parsed",
                    List.of());
        }
    }

    private static CSVFormat csvFormat() {
        return CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .build();
    }

    /** The file must carry exactly the required columns; a missing or extra one aborts the load. */
    private static void validateHeaders(List<String> headerNames) {
        Set<String> found = new LinkedHashSet<>(headerNames);

        if (found.size() != REQUIRED_HEADERS.size() || !found.containsAll(REQUIRED_HEADERS)) {
            throw new MeasurementImportException(
                    MeasurementImportException.INVALID_FILE_STRUCTURE,
                    "The file does not carry exactly the required columns",
                    List.of(
                            "expected=" + String.join(",", REQUIRED_HEADERS),
                            "found=" + String.join(",", headerNames)));
        }
    }

    private static LocalDate parseDate(String raw, int line, List<String> rowIssues) {
        try {
            return LocalDate.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ex) {
            rowIssues.add(issue(line, DATE_HEADER, INVALID_DATE));
            return null;
        }
    }

    private static BigDecimal parseMetric(String raw, String header, int line, List<String> rowIssues) {
        if (raw.isEmpty()) {
            rowIssues.add(issue(line, header, MISSING_VALUE));
            return null;
        }
        try {
            // Both separators are accepted; a comma only survives inside a quoted value.
            return new BigDecimal(raw.replace(',', '.'));
        } catch (NumberFormatException ex) {
            rowIssues.add(issue(line, header, NOT_A_NUMBER));
            return null;
        }
    }

    /**
     * Turns the Bean Validation outcome into the reasons the client knows how to
     * word, so limits stay declared once, on {@link MeasurementRequest}.
     */
    private List<String> describeViolations(MeasurementRequest request, int line) {
        List<String> issues = new ArrayList<>();

        for (ConstraintViolation<MeasurementRequest> violation : validator.validate(request)) {
            String header = headerOf(violation.getPropertyPath().toString());
            issues.add(issue(line, header, reasonOf(violation)));
        }

        return issues;
    }

    private static String headerOf(String propertyPath) {
        return switch (propertyPath) {
            case "weightKg" -> WEIGHT_HEADER;
            case "waistCm" -> WAIST_HEADER;
            default -> DATE_HEADER;
        };
    }

    private static String reasonOf(ConstraintViolation<MeasurementRequest> violation) {
        return REASONS_BY_CONSTRAINT.getOrDefault(
                violation.getConstraintDescriptor().getAnnotation().annotationType(),
                INVALID_VALUE);
    }

    private static String issue(int line, String header, String reason) {
        return line + "|" + header + "|" + reason;
    }

    private MeasurementImportException tooLarge() {
        return new MeasurementImportException(
                MeasurementImportException.IMPORT_TOO_LARGE,
                "The file carries more rows than the accepted maximum",
                List.of("maxRows=" + maxRows));
    }

    private static MeasurementImportException invalidValues(List<String> issues) {
        List<String> reported = issues.size() > MAX_REPORTED_ISSUES
                ? issues.subList(0, MAX_REPORTED_ISSUES)
                : issues;

        return new MeasurementImportException(
                MeasurementImportException.INVALID_IMPORT_VALUE,
                "The file contains rows that are not valid measurements",
                reported);
    }

    /**
     * What the file held, once read and validated.
     *
     * @param measurements the measurements to store, at most one per date
     * @param totalRows    data rows read from the file
     * @param ignoredRows  rows that named no day, and so carry nothing to store
     */
    public record ParsedCsv(List<MeasurementDraft> measurements, int totalRows, int ignoredRows) {
    }
}
