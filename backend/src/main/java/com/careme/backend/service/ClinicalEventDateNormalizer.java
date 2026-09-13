package com.careme.backend.service;

import com.careme.backend.entity.ClinicalEvent;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/** Normalizes supported relative expressions without increasing temporal precision. */
@Service
public class ClinicalEventDateNormalizer {

    private static final Pattern RELATIVE_DAYS = Pattern.compile("hace\\s+([\\p{L}\\d]+)\\s+d[ií]as?", Pattern.CASE_INSENSITIVE);
    private static final Pattern RELATIVE_WEEKS = Pattern.compile("hace\\s+([\\p{L}\\d]+)\\s+semanas?", Pattern.CASE_INSENSITIVE);

    public NormalizedDate normalize(
            LocalDate date,
            ClinicalEvent.DatePrecision precision,
            String dateText,
            LocalDate referenceDate) {
        if (precision == null) {
            throw new IllegalArgumentException("Date precision must not be null");
        }
        if (referenceDate == null) {
            throw new IllegalArgumentException("Reference date must not be null");
        }
        if (dateText == null || dateText.isBlank()) {
            return new NormalizedDate(date, precision, null);
        }

        String expression = dateText.trim();
        if (expression.equalsIgnoreCase("hoy")) {
            return new NormalizedDate(referenceDate, ClinicalEvent.DatePrecision.EXACT, dateText);
        }
        if (expression.equalsIgnoreCase("ayer")) {
            return new NormalizedDate(referenceDate.minusDays(1), ClinicalEvent.DatePrecision.EXACT, dateText);
        }

        Matcher days = RELATIVE_DAYS.matcher(expression);
        if (days.matches()) {
            return new NormalizedDate(
                    referenceDate.minusDays(parseAmount(days.group(1))),
                    ClinicalEvent.DatePrecision.EXACT,
                    dateText);
        }
        Matcher weeks = RELATIVE_WEEKS.matcher(expression);
        if (weeks.matches()) {
            return new NormalizedDate(
                    referenceDate.minusWeeks(parseAmount(weeks.group(1))),
                    ClinicalEvent.DatePrecision.EXACT,
                    dateText);
        }

        return new NormalizedDate(date, precision, dateText);
    }

    private static long parseAmount(String value) {
        return switch (value.toLowerCase()) {
            case "uno", "una" -> 1;
            case "dos" -> 2;
            case "tres" -> 3;
            case "cuatro" -> 4;
            case "cinco" -> 5;
            default -> Long.parseLong(value);
        };
    }

    public record NormalizedDate(LocalDate date, ClinicalEvent.DatePrecision precision, String text) {
    }
}
