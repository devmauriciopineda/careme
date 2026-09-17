package com.careme.backend.service;

import com.careme.backend.dto.ClinicalEventIntent;
import com.careme.backend.entity.ClinicalEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "careme.llm.mode", havingValue = "fake", matchIfMissing = true)
public class FakeClinicalIntentInterpreter implements ClinicalIntentInterpreter {

    /**
     * A request for advice is not a question about what is recorded. It is
     * conversation, so the reply can decline it explicitly.
     */
    private static final Pattern ADVICE_REQUEST = Pattern.compile(
            "recomiend\\w*|\\breceta(r|me)\\b|\\bdeber[íi]a\\b|qu[ée] puedo tomar"
                    + "|\\bdiagn[óo]stica(me|r)?\\b|\\bes grave\\b|qu[ée] me pasa"
                    + "|\\bestoy bien\\b|qu[ée] me conviene");

    /**
     * Words that tie a message to what the patient has recorded, so a colloquial
     * reformulation stays in the clinical history channel.
     */
    private static final Pattern HISTORY_EVIDENCE = Pattern.compile(
            "\\b(mi|mis|me|m[íi]o|m[íi]os|tengo|tomo|tuve|ten[íi]a|padezco|consta|registr\\w*"
                    + "|historia|peso|circunferencia|cintura|abdomen|a[ñn]o|a[ñn]os|mes|meses"
                    + "|semana|ayer|hoy)\\b|he tenido");

    /** A request for a general explanation does not depend on the history. */
    private static final Pattern GENERAL_QUESTION = Pattern.compile(
            "\\bqu[ée]\\s+(es|son|significa)\\b|\\bes normal\\b|\\bc[óo]mo funciona\\b"
                    + "|\\bpara qu[ée] sirve\\b");

    /** One sentence of the message, kept whole so a part can be quoted back. */
    private static final Pattern SEGMENT = Pattern.compile("[^.!?]+[.!?]*");

    @Override
    public ClinicalEventIntent interpret(String message, InterpretationContext context) {
        String trimmed = message.trim();
        String normalized = trimmed.toLowerCase(Locale.ROOT);
        if (isGreeting(normalized)) {
            return ClinicalEventIntent.conversation();
        }
        if (ADVICE_REQUEST.matcher(normalized).find()) {
            return ClinicalEventIntent.conversation();
        }
        if (normalized.contains("creo que") || normalized.contains("podría tener")
                || normalized.contains("podria tener")) {
            return ClinicalEventIntent.conversation();
        }
        if (isQuestion(normalized)) {
            return classifyQuestion(trimmed, normalized);
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

    /**
     * Decides the channel of a question and separates the parts of a message that
     * mixes a general request with one that depends on the history.
     *
     * <p>A sentence is general only when nothing in it ties it to what the patient
     * has recorded, so a colloquial question about the history stays a query. A
     * question that is neither general nor tied to the history cannot be
     * classified, and the stand-in asks for a clarification instead of guessing.
     */
    private ClinicalEventIntent classifyQuestion(String message, String normalized) {
        List<String> generalSegments = new ArrayList<>();
        List<String> historySegments = new ArrayList<>();
        for (String segment : segments(message)) {
            String candidate = segment.toLowerCase(Locale.ROOT);
            if (GENERAL_QUESTION.matcher(candidate).find() && !hasHistoryEvidence(candidate)) {
                generalSegments.add(segment);
            } else {
                historySegments.add(segment);
            }
        }
        if (!generalSegments.isEmpty() && !historySegments.isEmpty()) {
            String historyPart = String.join(" ", historySegments).trim();
            return ClinicalEventIntent.query(
                    query(historyPart, historyPart.toLowerCase(Locale.ROOT), String.join(" ", generalSegments).trim()));
        }
        if (!generalSegments.isEmpty()) {
            return ClinicalEventIntent.conversation();
        }
        if (!hasHistoryEvidence(normalized)) {
            return ClinicalEventIntent.clarification(
                    "¿Tu pregunta se refiere a tu historia clínica o es una duda general?");
        }
        return ClinicalEventIntent.query(query(message, normalized, null));
    }

    /** A rough stand-in for the provider's query classification, for the fake mode. */
    private ClinicalEventIntent.Query query(String question, String normalized, String generalPart) {
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
                question, scope, searchTerms(normalized), type, null, null, generalPart);
    }

    private static List<String> segments(String message) {
        List<String> segments = new ArrayList<>();
        Matcher matcher = SEGMENT.matcher(message);
        while (matcher.find()) {
            String segment = matcher.group().trim();
            if (!segment.isEmpty()) {
                segments.add(segment);
            }
        }
        return segments;
    }

    private static boolean hasHistoryEvidence(String normalized) {
        return HISTORY_EVIDENCE.matcher(normalized).find();
    }

    private boolean isQuestion(String message) {
        if (message.contains("?")) {
            return true;
        }
        return List.of("que ", "qué ", "cual ", "cuál ", "cuando ", "cuándo ",
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
        Set<String> stopwords = Set.of(
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