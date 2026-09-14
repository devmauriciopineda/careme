package com.careme.backend.service;

import com.careme.backend.dto.ClinicalEventIntent;
import com.careme.backend.entity.ClinicalEvent;
import java.time.LocalDate;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "careme.llm.mode", havingValue = "fake", matchIfMissing = true)
public class FakeClinicalIntentInterpreter implements ClinicalIntentInterpreter {

    @Override
    public ClinicalEventIntent interpret(String message, InterpretationContext context) {
        String normalized = message.trim().toLowerCase();
        if (isGreeting(normalized)) {
            return ClinicalEventIntent.conversation();
        }
        if (isQuestion(normalized)) {
            return ClinicalEventIntent.query(query(message.trim(), normalized));
        }
        if (normalized.contains("creo que") || normalized.contains("podría tener")
                || normalized.contains("podria tener")) {
            return ClinicalEventIntent.conversation();
        }
        if (normalized.contains("aclara") || normalized.length() < 12) {
            return ClinicalEventIntent.clarification("¿Qué hecho médico quieres registrar y cuándo ocurrió?");
        }
        if (!containsClinicalSignal(normalized)) {
            return ClinicalEventIntent.conversation();
        }

        ClinicalEvent.ClinicalEventType type = normalized.contains("medic")
                ? ClinicalEvent.ClinicalEventType.MEDICATION
                : normalized.contains("presion") || normalized.contains("presión")
                        ? ClinicalEvent.ClinicalEventType.MEASUREMENT
                        : normalized.contains("diagnostic")
                                ? ClinicalEvent.ClinicalEventType.DIAGNOSIS
                                : ClinicalEvent.ClinicalEventType.NOTE;
        LocalDate date = normalized.contains("hoy") ? context.referenceDate()
                : normalized.contains("ayer") ? context.referenceDate().minusDays(1) : null;
        ClinicalEvent.DatePrecision precision = date != null
                ? ClinicalEvent.DatePrecision.EXACT
                : normalized.contains("hace")
                        ? ClinicalEvent.DatePrecision.APPROXIMATE
                        : ClinicalEvent.DatePrecision.UNKNOWN;
        String dateText = normalized.contains("hoy") ? "hoy"
                : normalized.contains("ayer") ? "ayer"
                : normalized.contains("hace") ? "hace" : null;
        return new ClinicalEventIntent(
                ClinicalEventIntent.Kind.EVENTS,
                List.of(new ClinicalEventIntent.Candidate(type, message.trim(), date, precision, dateText)),
                null);
    }

    /** A rough stand-in for the provider's query classification, for the fake mode. */
    private ClinicalEventIntent.Query query(String question, String normalized) {
        var scope = normalized.contains("peso") || normalized.contains("circunferencia")
                || normalized.contains("cintura") || normalized.contains("abdomen")
                ? ClinicalEventIntent.Query.Scope.MEASUREMENTS
                : ClinicalEventIntent.Query.Scope.HISTORY;
        ClinicalEvent.ClinicalEventType type = normalized.contains("medic")
                ? ClinicalEvent.ClinicalEventType.MEDICATION
                : normalized.contains("presion") || normalized.contains("presión")
                        ? ClinicalEvent.ClinicalEventType.MEASUREMENT
                        : normalized.contains("diagnostic")
                                ? ClinicalEvent.ClinicalEventType.DIAGNOSIS
                                : null;
        return new ClinicalEventIntent.Query(
                question, scope, searchTerms(normalized), type, null, null);
    }

    private boolean isQuestion(String message) {
        if (message.contains("?")) {
            return true;
        }
        return java.util.List.of("que ", "qué ", "cual ", "cuál ", "cuando ", "cuándo ",
                        "cuanto ", "cuánto ", "donde ", "dónde ", "quien ", "quién ")
                .stream().anyMatch(message::startsWith);
    }

    /** A greeting is general conversation, not a question about the clinical history. */
    private boolean isGreeting(String message) {
        return message.startsWith("hola") || message.contains("cómo estás")
                || message.contains("como estas") || message.contains("qué puedes hacer")
                || message.contains("que puedes hacer");
    }

    private static List<String> searchTerms(String message) {
        java.util.Set<String> stopwords = java.util.Set.of(
                "sobre", "para", "cual", "cuál", "cuando", "cuándo", "cuanto", "cuánto",
                "donde", "dónde", "tengo", "tiene", "esta", "está", "muestra", "dime");
        return java.util.Arrays.stream(message.replaceAll("[^\\p{L}\\p{N} ]", " ").split("\\s+"))
                .map(String::trim)
                .filter(word -> word.length() > 3 && !stopwords.contains(word))
                .distinct()
                .limit(8)
                .toList();
    }

    private boolean containsClinicalSignal(String message) {
        return message.contains("diagnostic") || message.contains("medic") || message.contains("dolor")
                || message.contains("fiebre") || message.contains("presion") || message.contains("presión")
                || message.contains("hospital") || message.contains("operación") || message.contains("operacion");
    }
}