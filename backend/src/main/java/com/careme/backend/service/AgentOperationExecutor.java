package com.careme.backend.service;

import com.careme.backend.dto.AgentOperation;
import com.careme.backend.dto.ClinicalEventIntent;
import com.careme.backend.entity.ClinicalEvent;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Runs the operations the assistant asks for.
 *
 * <p>Both operations are executed through the application, which validates inside
 * the call before producing any effect, so no fact reaches the clinical history
 * without passing the same deterministic checks as before. An operation that is
 * not one of the available ones is never executed.
 */
@Service
public class AgentOperationExecutor {

    private static final Logger log = LoggerFactory.getLogger(AgentOperationExecutor.class);

    private static final String OUT_OF_REACH =
            "Esa operación no está a mi alcance: solo puedo consultar tu historia clínica"
                    + " y registrar hechos médicos.";

    private static final String NOT_ADMISSIBLE =
            "No he podido completar esa operación con lo que me has dicho.";

    /**
     * The measurement tracking has its own space, so a question about weight or
     * abdominal circumference is not answered from the clinical history.
     */
    private static final String MEASUREMENT_REDIRECT =
            "El peso y la circunferencia abdominal tienen su propio espacio de seguimiento"
                    + " y no forman parte de la historia clínica que consulto aquí.";

    private final ClinicalHistoryQueryService historyQueryService;
    private final ClinicalEventRegistrationService registrationService;
    private final ClinicalEventIntentValidator intentValidator;

    public AgentOperationExecutor(
            ClinicalHistoryQueryService historyQueryService,
            ClinicalEventRegistrationService registrationService,
            ClinicalEventIntentValidator intentValidator) {
        this.historyQueryService = historyQueryService;
        this.registrationService = registrationService;
        this.intentValidator = intentValidator;
    }

    /**
     * Runs one operation.
     *
     * <p>A call outside the available set —including a name that resolves to
     * nothing— is rejected without effect.
     */
    public AgentOperationResult execute(
            String conversationId, AgentOperationCall call, LocalDate referenceDate) {
        if (call == null) {
            log.warn("Rejected an operation that is not in the available set conversationId={}", conversationId);
            return AgentOperationResult.rejected(AgentOperationResult.Rejection.OUT_OF_REACH, OUT_OF_REACH);
        }
        return switch (call.operation()) {
            case CONSULT_HISTORY -> consult(conversationId, call.arguments());
            case REGISTER_EVENT -> register(conversationId, call.arguments(), referenceDate);
        };
    }

    private AgentOperationResult consult(String conversationId, JsonNode arguments) {
        ClinicalEventIntent intent;
        try {
            intent = consultationIntent(arguments);
        } catch (RuntimeException exception) {
            log.warn("Rejected a consultation that could not be read conversationId={}", conversationId);
            return AgentOperationResult.rejected(AgentOperationResult.Rejection.NOT_ADMISSIBLE, NOT_ADMISSIBLE);
        }
        if (intent.query().scope() == ClinicalEventIntent.Query.Scope.MEASUREMENTS) {
            return AgentOperationResult.rejected(AgentOperationResult.Rejection.ELSEWHERE, MEASUREMENT_REDIRECT);
        }
        try {
            intentValidator.validate(intent);
        } catch (RuntimeException exception) {
            log.warn("Rejected a consultation that is not admissible conversationId={}", conversationId);
            return AgentOperationResult.rejected(AgentOperationResult.Rejection.NOT_ADMISSIBLE, NOT_ADMISSIBLE);
        }
        return AgentOperationResult.of(AgentOperation.CONSULT_HISTORY, historyQueryService.answer(intent));
    }

    private AgentOperationResult register(String conversationId, JsonNode arguments, LocalDate referenceDate) {
        ClinicalEventIntent intent;
        try {
            intent = registrationIntent(arguments);
            intentValidator.validate(intent);
        } catch (RuntimeException exception) {
            log.warn(
                    "Rejected a registration that does not pass validation conversationId={}", conversationId);
            return AgentOperationResult.rejected(AgentOperationResult.Rejection.NOT_ADMISSIBLE, NOT_ADMISSIBLE);
        }
        return AgentOperationResult.of(
                AgentOperation.REGISTER_EVENT, registrationService.register(conversationId, intent, referenceDate));
    }

    private static ClinicalEventIntent consultationIntent(JsonNode arguments) {
        String question = text(arguments, "question");
        if (question == null) {
            throw new IllegalArgumentException("A consultation must contain the question");
        }
        return ClinicalEventIntent.query(new ClinicalEventIntent.Query(
                question,
                scope(arguments),
                terms(arguments.path("search_terms")),
                enumValue(ClinicalEvent.ClinicalEventType.class, text(arguments, "type")),
                date(arguments, "date_from"),
                date(arguments, "date_to")));
    }

    private static ClinicalEventIntent registrationIntent(JsonNode arguments) {
        ClinicalEvent.ClinicalEventType type =
                enumValue(ClinicalEvent.ClinicalEventType.class, text(arguments, "type"));
        String content = text(arguments, "content");
        ClinicalEvent.DatePrecision precision =
                enumValue(ClinicalEvent.DatePrecision.class, text(arguments, "date_precision"));
        if (type == null || content == null || precision == null) {
            throw new IllegalArgumentException("A registration must contain a type, a content and a precision");
        }
        return new ClinicalEventIntent(
                ClinicalEventIntent.Kind.EVENTS,
                List.of(new ClinicalEventIntent.Candidate(
                        type, content, date(arguments, "date"), precision, text(arguments, "date_text"))),
                null);
    }

    private static ClinicalEventIntent.Query.Scope scope(JsonNode arguments) {
        ClinicalEventIntent.Query.Scope parsed =
                enumValue(ClinicalEventIntent.Query.Scope.class, text(arguments, "scope"));
        return parsed == null ? ClinicalEventIntent.Query.Scope.HISTORY : parsed;
    }

    private static String text(JsonNode arguments, String field) {
        if (arguments == null) {
            return null;
        }
        JsonNode value = arguments.path(field);
        if (!value.isTextual()) {
            return null;
        }
        String text = value.asText().trim();
        return text.isEmpty() ? null : text;
    }

    private static LocalDate date(JsonNode arguments, String field) {
        String value = text(arguments, field);
        return value == null ? null : LocalDate.parse(value);
    }

    private static List<String> terms(JsonNode node) {
        List<String> terms = new ArrayList<>();
        if (node.isArray()) {
            node.forEach(term -> {
                if (term.isTextual() && !term.asText().isBlank()) {
                    terms.add(term.asText().trim());
                }
            });
        }
        return terms;
    }

    private static <E extends Enum<E>> E enumValue(Class<E> type, String value) {
        if (value == null) {
            return null;
        }
        try {
            return Enum.valueOf(type, value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unknown value for " + type.getSimpleName(), exception);
        }
    }
}
