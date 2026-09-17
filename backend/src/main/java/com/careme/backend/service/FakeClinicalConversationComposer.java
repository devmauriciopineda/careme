package com.careme.backend.service;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Deterministic conversational composer for the fake provider mode.
 *
 * <p>It keeps the offline mode able to answer a conversational turn without a
 * network call, and it applies the same limits the real prompt carries: it never
 * presents anything as a recorded fact, never applies an explanation to the
 * user's own case and never gives clinical advice.
 */
@Service
@ConditionalOnProperty(name = "careme.llm.mode", havingValue = "fake", matchIfMissing = true)
public class FakeClinicalConversationComposer implements ClinicalConversationComposer {

    /** A request for advice is declined instead of answered. */
    private static final Pattern ADVICE_REQUEST = Pattern.compile(
            "recomiend\\w*|recetar|recétame|recetame|\\bdeber[íi]a\\b"
                    + "|qu[ée] puedo tomar|\\bdiagn[óo]stica(me|r)?\\b|\\bes grave\\b"
                    + "|qu[ée] me pasa|\\bestoy bien\\b|qu[ée] me conviene");

    /** Greetings, farewells and courtesies. */
    private static final Pattern COURTESY = Pattern.compile(
            "^hola|\\bhola\\b|c[óo]mo est[áa]s|qu[ée] puedes hacer|adi[óo]s|hasta luego|\\bgracias\\b");

    /** Questions that ask for a general explanation of a concept. */
    private static final Pattern CONCEPT_QUESTION = Pattern.compile(
            "qu[ée] es|que es|qu[ée] son|que son|qu[ée] significa|que significa");

    private static final String DECLINED_REPLY =
            "No puedo darte un diagnóstico ni recomendarte un tratamiento: este asistente"
                    + " registra y consulta tu historia clínica, pero no diagnostica ni recomienda."
                    + " Puedo explicarte un concepto en términos generales o registrar un hecho"
                    + " que quieras dejar constancia.";

    private static final String COURTESY_REPLY =
            "Hola. Cuéntame qué quieres dejar registrado o pregúntame lo que necesites.";

    private static final String CONCEPT_REPLY_TEMPLATE =
            "En términos generales, %s es un concepto que puedo explicarte sin aplicarlo a tu caso:"
                    + " no interpreto tus datos ni recomiendo tratamiento.";

    private static final String GENERAL_REPLY_TEMPLATE =
            "Te respondo como conversación general sobre esto: %s."
                    + " No aplico nada a tu caso ni interpreto tus datos.";

    @Override
    public String compose(String message, List<String> recentTurns) {
        if (message == null || message.isBlank()) {
            throw new LlmIntegrationException("There is nothing to reply to");
        }
        String trimmed = message.trim();
        String normalized = trimmed.toLowerCase(Locale.ROOT);
        if (ADVICE_REQUEST.matcher(normalized).find()) {
            return DECLINED_REPLY;
        }
        if (COURTESY.matcher(normalized).find()) {
            return COURTESY_REPLY;
        }
        String concept = conceptOf(trimmed, normalized);
        if (concept != null) {
            return CONCEPT_REPLY_TEMPLATE.formatted(concept);
        }
        return GENERAL_REPLY_TEMPLATE.formatted(trimmed);
    }

    /**
     * Reads the concept the question asks about, or {@code null} when the message
     * is not a request for a general explanation.
     */
    private static String conceptOf(String message, String normalized) {
        Matcher matcher = CONCEPT_QUESTION.matcher(normalized);
        if (!matcher.find()) {
            return null;
        }
        String concept = message.substring(matcher.end())
                .replaceAll("^[\\s¿?¡!.,:;-]+", "")
                .replaceAll("[\\s¿?¡!.,:;-]+$", "");
        return concept.isEmpty() ? null : concept;
    }
}
