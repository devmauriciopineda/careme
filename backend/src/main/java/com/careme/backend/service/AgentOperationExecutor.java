package com.careme.backend.service;

import com.careme.backend.dto.AgentOperation;
import com.careme.backend.dto.ClinicalEventIntent;
import com.careme.backend.entity.ClinicalEvent;
import com.careme.backend.entity.EncounterMeasurementNote;
import com.careme.backend.entity.MeasurementUnit;
import com.careme.backend.entity.Metric;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
                    + " y anotar hechos médicos.";

    private static final String NOT_ADMISSIBLE =
            "No he podido completar esa operación con lo que me has dicho.";

    /**
     * The measurement tracking has its own space, so a question about weight or
     * abdominal circumference is not answered from the clinical history.
     */
    private static final String MEASUREMENT_REDIRECT =
            "El peso y la circunferencia abdominal tienen su propio espacio de seguimiento"
                    + " y no forman parte de la historia clínica que consulto aquí.";

    /**
     * A metric the tracking does not admit yet. The catalogue knows how to describe it,
     * so what it needs is the person's confirmation to start recording it.
     */
    private static final String METRIC_NOT_ADMITTED =
            "Esa medición no está quedando anotada. ¿Quieres que empecemos a registrar esa métrica?";

    /** A metric the catalogue does not know: its unit and range cannot be invented. */
    private static final String METRIC_UNKNOWN =
            "Todavía no sé registrar esa métrica, así que esa medición no ha quedado anotada.";

    private static final String MEASUREMENT_NEEDS_UNIT =
            "¿En qué unidad es esa medición?";

    private static final String MEASUREMENT_NEEDS_EXACT_DATE =
            "¿Qué día exacto fue esa medición?";

    private static final String MEASUREMENT_NOT_ADMISSIBLE =
            "No he podido anotar esa medición con lo que me has dicho.";

    private final ClinicalHistoryQueryService historyQueryService;
    private final EncounterService encounterService;
    private final ClinicalEventIntentValidator intentValidator;
    private final MetricCatalogService metricCatalogService;

    public AgentOperationExecutor(
            ClinicalHistoryQueryService historyQueryService,
            EncounterService encounterService,
            ClinicalEventIntentValidator intentValidator,
            MetricCatalogService metricCatalogService) {
        this.historyQueryService = historyQueryService;
        this.encounterService = encounterService;
        this.intentValidator = intentValidator;
        this.metricCatalogService = metricCatalogService;
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
            case RECORD_NOTE -> collect(conversationId, call.arguments(), referenceDate);
            case RECORD_MEASUREMENT -> collectMeasurement(conversationId, call.arguments(), referenceDate);
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

    private AgentOperationResult collect(String conversationId, JsonNode arguments, LocalDate referenceDate) {
        ClinicalEventIntent intent;
        try {
            intent = registrationIntent(arguments);
            intentValidator.validate(intent);
        } catch (RuntimeException exception) {
            log.warn("Rejected a note that does not pass validation conversationId={}", conversationId);
            return AgentOperationResult.rejected(AgentOperationResult.Rejection.NOT_ADMISSIBLE, NOT_ADMISSIBLE);
        }
        return AgentOperationResult.of(
                AgentOperation.RECORD_NOTE,
                encounterService.collect(
                        conversationId, intent.events().getFirst(), referenceDate));
    }

    /**
     * Collects a measurement in the open consultation's notes, after turning what the
     * assistant understood into something the tracking can take: an admitted metric, a
     * value inside its range, an unambiguous unit and an exact date. Anything it cannot
     * turn into that is rejected with what has to be asked for, and nothing is written.
     */
    private AgentOperationResult collectMeasurement(
            String conversationId, JsonNode arguments, LocalDate referenceDate) {
        EncounterMeasurementNote note;
        try {
            note = measurementNote(arguments, referenceDate);
        } catch (MeasurementRejected rejection) {
            log.warn("Rejected a measurement that cannot be taken conversationId={}", conversationId);
            return AgentOperationResult.rejected(
                    AgentOperationResult.Rejection.NOT_ADMISSIBLE, rejection.getMessage());
        } catch (RuntimeException exception) {
            log.warn("Rejected a measurement that could not be read conversationId={}", conversationId);
            return AgentOperationResult.rejected(
                    AgentOperationResult.Rejection.NOT_ADMISSIBLE, MEASUREMENT_NOT_ADMISSIBLE);
        }
        return AgentOperationResult.of(
                AgentOperation.RECORD_MEASUREMENT,
                encounterService.collectMeasurement(conversationId, note));
    }

    private EncounterMeasurementNote measurementNote(JsonNode arguments, LocalDate referenceDate) {
        Metric metric = Metric.fromCode(text(arguments, "metric")).orElse(null);
        if (metric == null) {
            throw new MeasurementRejected(METRIC_UNKNOWN);
        }
        if (!metricCatalogService.isAdmitted(metric)) {
            throw new MeasurementRejected(METRIC_NOT_ADMITTED);
        }

        String statedUnit = text(arguments, "unit");
        MeasurementUnit declaredUnit = MeasurementUnit.fromCode(statedUnit);
        if (declaredUnit == null && statedUnit != null) {
            throw new MeasurementRejected(MEASUREMENT_NOT_ADMISSIBLE);
        }
        UnitResolution resolution = metricCatalogService.resolveUnit(metric, declaredUnit);
        if (resolution.requiresClarification()) {
            throw new MeasurementRejected(MEASUREMENT_NEEDS_UNIT);
        }

        Map<String, BigDecimal> reference = new LinkedHashMap<>();
        numbers(arguments.path("values")).forEach((component, value) -> reference.put(
                component, metricCatalogService.toReferenceUnit(metric, value, resolution.unit())));
        if (!metricCatalogService.admits(metric, reference)) {
            throw new MeasurementRejected(MEASUREMENT_NOT_ADMISSIBLE);
        }

        ClinicalEvent.DatePrecision precision =
                enumValue(ClinicalEvent.DatePrecision.class, text(arguments, "date_precision"));
        LocalDate date = date(arguments, "date");
        if (precision == ClinicalEvent.DatePrecision.APPROXIMATE
                || precision == ClinicalEvent.DatePrecision.UNKNOWN) {
            // A measurement only means something on a concrete day, so an approximate or
            // unknown date is asked for rather than recorded as if it were exact.
            throw new MeasurementRejected(MEASUREMENT_NEEDS_EXACT_DATE);
        }
        if (date == null) {
            date = referenceDate;
        }
        if (date == null) {
            throw new MeasurementRejected(MEASUREMENT_NEEDS_EXACT_DATE);
        }

        return new EncounterMeasurementNote(
                metric.code(),
                reference,
                statedUnit,
                date,
                ClinicalEvent.DatePrecision.EXACT,
                text(arguments, "date_text"));
    }

    private static Map<String, BigDecimal> numbers(JsonNode node) {
        Map<String, BigDecimal> values = new LinkedHashMap<>();
        if (node != null && node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                if (entry.getValue().isNumber()) {
                    values.put(entry.getKey(), entry.getValue().decimalValue());
                }
            });
        }
        if (values.isEmpty()) {
            throw new IllegalArgumentException("A measurement must carry its values");
        }
        return values;
    }

    /**
     * A measurement the application cannot take as it was asked for. The message says
     * what the person has to be asked, and nothing was written.
     */
    private static final class MeasurementRejected extends RuntimeException {

        private MeasurementRejected(String message) {
            super(message);
        }
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
