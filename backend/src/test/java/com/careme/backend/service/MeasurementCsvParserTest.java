package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.careme.backend.exception.MeasurementImportException;

import jakarta.validation.Validation;

/**
 * A file is accepted as a whole or rejected as a whole, and the reasons it
 * reports are stable codes the client can word in the user's language.
 */
class MeasurementCsvParserTest {

    private static final String HEADER = "date,weight_kg,abdominal_circumference_cm\n";

    private static MeasurementCsvParser parser() {
        return parser(10000);
    }

    private static MeasurementCsvParser parser(int maxRows) {
        return new MeasurementCsvParser(
                Validation.buildDefaultValidatorFactory().getValidator(), maxRows);
    }

    private static String file(String... rows) {
        return HEADER + String.join("\n", rows) + "\n";
    }

    private static InputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    private static MeasurementCsvParser.ParsedCsv parse(String content) {
        return parser().parse(stream(content));
    }

    private static MeasurementImportException rejected(String content) {
        return catchThrowableOfType(
                () -> parse(content), MeasurementImportException.class);
    }

    @Test
    void readsEveryRowOfTheFile() {
        MeasurementCsvParser.ParsedCsv parsed = parse(file(
                "2026-09-06,81.1,96.0",
                "2026-09-08,80.4,95.2"));

        assertThat(parsed.totalRows()).isEqualTo(2);
        assertThat(parsed.ignoredRows()).isZero();
        assertThat(parsed.measurements()).hasSize(2);
        assertThat(parsed.measurements().getFirst().date()).isEqualTo(LocalDate.of(2026, 9, 6));
        assertThat(parsed.measurements().getFirst().weightKg()).isEqualByComparingTo("81.1");
        assertThat(parsed.measurements().getFirst().waistCm()).isEqualByComparingTo("96.0");
    }

    @Test
    void readsTheColumnsWhateverTheirOrder() {
        String content = "weight_kg,abdominal_circumference_cm,date\n81.1,96.0,2026-09-06\n";

        assertThat(parse(content).measurements()).hasSize(1);
    }

    @Test
    void readsQuotedValuesAndCommaDecimals() {
        MeasurementCsvParser.ParsedCsv parsed = parse(file("2026-09-06,\"81,1\",96.0"));

        assertThat(parsed.measurements().getFirst().weightKg()).isEqualByComparingTo("81.1");
    }

    @Test
    void acceptsAFileWithNoDataRows() {
        MeasurementCsvParser.ParsedCsv parsed = parse(HEADER);

        assertThat(parsed.totalRows()).isZero();
        assertThat(parsed.measurements()).isEmpty();
    }

    @Test
    void rejectsAFileWithoutTheRequiredColumns() {
        MeasurementImportException rejection = rejected(
                "date,abdominal_circumference_cm\n2026-09-06,96.0\n");

        assertThat(rejection.code()).isEqualTo(MeasurementImportException.INVALID_FILE_STRUCTURE);
        assertThat(rejection.details()).containsExactly(
                "expected=date,weight_kg,abdominal_circumference_cm",
                "found=date,abdominal_circumference_cm");
    }

    @Test
    void rejectsAFileWithAnExtraColumn() {
        MeasurementImportException rejection = rejected(
                "date,weight_kg,abdominal_circumference_cm,notes\n2026-09-06,81.1,96.0,ok\n");

        assertThat(rejection.code()).isEqualTo(MeasurementImportException.INVALID_FILE_STRUCTURE);
        assertThat(rejection.details()).contains("found=date,weight_kg,abdominal_circumference_cm,notes");
    }

    @Test
    void rejectsAFileThatNamesAColumnDifferently() {
        MeasurementImportException rejection = rejected(
                "date,weight,abdominal_circumference_cm\n2026-09-06,81.1,96.0\n");

        assertThat(rejection.code()).isEqualTo(MeasurementImportException.INVALID_FILE_STRUCTURE);
        assertThat(rejection.details()).contains("found=date,weight,abdominal_circumference_cm");
    }

    @Test
    void rejectsAFileWithoutContent() {
        assertThat(rejected("").code())
                .isEqualTo(MeasurementImportException.INVALID_FILE_STRUCTURE);
    }

    @Test
    void rejectsARowWithAnUnreadableDate() {
        assertThat(rejected(file("06/09/2026,81.1,96.0")).details())
                .containsExactly("2|date|INVALID_DATE");
    }

    @Test
    void rejectsARowWithAFutureDate() {
        assertThat(rejected(file("2999-01-01,81.1,96.0")).details())
                .containsExactly("2|date|FUTURE_DATE");
    }

    @Test
    void rejectsARowWithANonNumericValue() {
        assertThat(rejected(file("2026-09-06,abc,96.0")).details())
                .containsExactly("2|weight_kg|NOT_A_NUMBER");
    }

    @Test
    void rejectsARowWithANonPositiveValue() {
        assertThat(rejected(file("2026-09-06,0,96.0")).details())
                .containsExactly("2|weight_kg|NOT_POSITIVE");
    }

    @Test
    void rejectsARowWithMoreThanOneDecimal() {
        assertThat(rejected(file("2026-09-06,81.12,96.0")).details())
                .containsExactly("2|weight_kg|TOO_MANY_DECIMALS");
    }

    @Test
    void rejectsARowWithAValueOverTheLimit() {
        assertThat(rejected(file("2026-09-06,600.0,96.0")).details())
                .containsExactly("2|weight_kg|TOO_LARGE");
    }

    @Test
    void rejectsARowThatNamesADayButLeavesAValueEmpty() {
        assertThat(rejected(file("2026-09-06,81.1,")).details())
                .containsExactly("2|abdominal_circumference_cm|MISSING_VALUE");
    }

    @Test
    void rejectsARowWithValuesButNoDay() {
        assertThat(rejected(file(",81.1,96.0")).details())
                .containsExactly("2|date|MISSING_DATE");
    }

    @Test
    void rejectsARowWithNoDayAndOnlyOneValue() {
        assertThat(rejected(file(",,96.0")).details())
                .containsExactly("2|date|MISSING_DATE");
    }

    @Test
    void reportsEveryInvalidRowOfTheFile() {
        assertThat(rejected(file(
                "2026-09-06,0,96.0",
                "2026-09-07,81.1,-1"))
                .details())
                .containsExactly(
                        "2|weight_kg|NOT_POSITIVE",
                        "3|abdominal_circumference_cm|NOT_POSITIVE");
    }

    @Test
    void ignoresARowThatNamesNoDayAndCarriesNoValue() {
        MeasurementCsvParser.ParsedCsv parsed = parse(file("2026-09-06,,", ",,"));

        assertThat(parsed.ignoredRows()).isEqualTo(2);
        assertThat(parsed.measurements()).isEmpty();
    }

    @Test
    void keepsTheLastRowOfADayThatAppearsMoreThanOnce() {
        MeasurementCsvParser.ParsedCsv parsed = parse(file(
                "2026-09-06,81.1,96.0",
                "2026-09-06,79.9,93.4"));

        assertThat(parsed.totalRows()).isEqualTo(2);
        assertThat(parsed.measurements()).hasSize(1);
        assertThat(parsed.measurements().getFirst().weightKg()).isEqualByComparingTo("79.9");
        assertThat(parsed.measurements().getFirst().waistCm()).isEqualByComparingTo("93.4");
    }

    @Test
    void rejectsAFileWithMoreRowsThanAllowed() {
        MeasurementImportException rejection = catchThrowableOfType(
                () -> parser(2).parse(stream(file(
                        "2026-09-06,81.1,96.0",
                        "2026-09-07,80.8,95.4",
                        "2026-09-08,80.4,95.2"))),
                MeasurementImportException.class);

        assertThat(rejection.code()).isEqualTo(MeasurementImportException.IMPORT_TOO_LARGE);
        assertThat(rejection.details()).containsExactly("maxRows=2");
    }

    @Test
    void boundsHowManyProblemsItReports() {
        String[] rows = new String[60];
        for (int index = 0; index < rows.length; index++) {
            rows[index] = "not-a-date,abc,xyz";
        }

        MeasurementImportException rejection = rejected(file(rows));

        assertThat(rejection.code()).isEqualTo(MeasurementImportException.INVALID_IMPORT_VALUE);
        assertThat(rejection.details()).hasSize(50);
    }

    @Test
    void rejectsAStreamThatCannotBeRead() {
        InputStream failing = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("the connection dropped");
            }
        };

        MeasurementImportException rejection = catchThrowableOfType(
                () -> parser().parse(failing), MeasurementImportException.class);

        assertThat(rejection.code()).isEqualTo(MeasurementImportException.UNREADABLE_FILE);
        assertThat(rejection.details()).isEmpty();
    }
}
