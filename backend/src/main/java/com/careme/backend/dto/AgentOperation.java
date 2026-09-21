package com.careme.backend.dto;

import com.careme.backend.entity.ClinicalEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * The closed set of operations the assistant may ask the application to run.
 *
 * <p>The set has exactly four members —consulting the clinical history, consulting the
measurement tracking, taking note of a clinical fact and taking note of a measurement— and
that is the structural
guarantee that the assistant never diagnoses and never recommends treatment: there
is no other operation it could ask for. Neither member names a path, a file or any
other storage detail, because the assistant is never told how the clinical history
or the tracking is stored. Registering is not an operation of the turn: the notes
are registered when the consultation is closed. Consulting the tracking is
read-only: it never changes a measurement.
 *
 * <p>The clinical-fact types offered are the admissible ones only: a measurement is
 * not a clinical fact, so it is not among them.
 */
public enum AgentOperation {

    CONSULT_HISTORY(
            "consult_history",
            "Consulta la historia clínica y devuelve los hechos registrados que pueden responder la pregunta.",
            Map.of(
                    "type", "object",
                    "properties", Map.of(
                            "question", Map.of(
                                    "type", "string",
                                    "description", "La pregunta de la persona, en sus propias palabras."),
                            "search_terms", Map.of(
                                    "type", "array",
                                    "items", Map.of("type", "string"),
                                    "description", "Términos con los que buscar en la historia."),
                            "type", Map.of(
                                    "type", "string",
                                    "enum", codes(ClinicalEvent.ClinicalEventType.admissible()),
                                    "description", "Tipo de hecho consultado, cuando la pregunta lo acota."),
                            "date_from", Map.of(
                                    "type", "string",
                                    "description", "Fecha inicial del periodo consultado, en formato ISO."),
                            "date_to", Map.of(
                                    "type", "string",
                                    "description", "Fecha final del periodo consultado, en formato ISO."),
                            "scope", Map.of(
                                    "type", "string",
                                    "enum", lowerCased(ClinicalEventIntent.Query.Scope.values()),
                                    "description", "Ámbito consultado: la historia clínica o el seguimiento corporal.")),
                    "required", List.of("question"))),

    RECORD_NOTE(
            "record_note",
            "Anota un hecho médico que la persona menciona, conservando sus palabras y su precisión temporal, para que quede registrado al cerrar la consulta.",
            Map.of(
                    "type", "object",
                    "properties", Map.of(
                            "type", Map.of(
                                    "type", "string",
                                    "enum", codes(ClinicalEvent.ClinicalEventType.admissible()),
                                    "description", "Tipo del hecho registrado."),
                            "content", Map.of(
                                    "type", "string",
                                    "description", "El hecho tal como lo expresó la persona."),
                            "date", Map.of(
                                    "type", "string",
                                    "description", "Fecha del hecho en formato ISO, cuando puede determinarse."),
                            "date_precision", Map.of(
                                    "type", "string",
                                    "enum", lowerCased(ClinicalEvent.DatePrecision.values()),
                                    "description", "Grado de precisión de la fecha."),
                            "date_text", Map.of(
                                    "type", "string",
                                    "description", "La expresión temporal original de la persona, cuando la hubo.")),
                    "required", List.of("type", "content", "date_precision"))),

    RECORD_MEASUREMENT(
            "record_measurement",
            "Anota una medición que la persona menciona —peso, circunferencia abdominal, presión arterial, colesterol u otra métrica admitida— con su valor, su unidad cuando la indicó y su fecha exacta, para que quede en su seguimiento al cerrar la consulta.",
            Map.of(
                    "type", "object",
                    "properties", Map.of(
                            "metric", Map.of(
                                    "type", "string",
                                    "description", "La métrica de la medición, por su código (weight, waist, blood_pressure, cholesterol)."),
                            "values", Map.of(
                                    "type", "object",
                                    "description", "Los valores, con el componente como clave: value para una métrica simple, systolic y diastolic para la presión arterial.",
                                    "additionalProperties", Map.of("type", "number")),
                            "unit", Map.of(
                                    "type", "string",
                                    "description", "La unidad que la persona indicó, cuando la indicó."),
                            "date", Map.of(
                                    "type", "string",
                                    "description", "Fecha exacta de la medición, en formato ISO."),
                            "date_text", Map.of(
                                    "type", "string",
                                    "description", "La expresión temporal original de la persona, cuando la hubo.")),
                    "required", List.of("metric", "values"))),

    CONSULT_MEASUREMENTS(
            "consult_measurements",
            "Consulta el seguimiento de mediciones y devuelve las mediciones registradas que pueden responder la pregunta, con su métrica, su valor en la unidad de referencia de esa métrica y su fecha.",
            Map.of(
                    "type", "object",
                    "properties", Map.of(
                            "question", Map.of(
                                    "type", "string",
                                    "description", "La pregunta de la persona, en sus propias palabras."),
                            "metrics", Map.of(
                                    "type", "array",
                                    "items", Map.of("type", "string"),
                                    "description", "Las métricas preguntadas, por su código (weight, waist, blood_pressure, cholesterol)."
                            + " Se deja vacío cuando la persona no concretó cuál."),
                            "date_from", Map.of(
                                    "type", "string",
                                    "description", "Fecha inicial del periodo preguntado, en formato ISO."),
                            "date_to", Map.of(
                                    "type", "string",
                                    "description", "Fecha final del periodo preguntado, en formato ISO."),
                            "interpretation_requested", Map.of(
                                    "type", "boolean",
                                    "description", "Cierto cuando la persona pide una interpretación o una recomendación sobre sus valores.")),
                    "required", List.of("question")));

    private final String operationName;
    private final String description;
    private final Map<String, Object> arguments;

    AgentOperation(String operationName, String description, Map<String, Object> arguments) {
        this.operationName = operationName;
        this.description = description;
        this.arguments = arguments;
    }

    /** The name the assistant uses to ask for this operation. */
    public String operationName() {
        return operationName;
    }

    /** What the operation does, in the assistant's own terms. */
    public String description() {
        return description;
    }

    /** The arguments the operation accepts, described without any storage detail. */
    public Map<String, Object> arguments() {
        return arguments;
    }

    /**
     * The operation declared under {@code operationName}, or empty when that name
     * is not one of the available operations. An empty result is what keeps a
     * requested operation outside the set from ever being executed.
     */
    public static Optional<AgentOperation> byName(String operationName) {
        if (operationName == null || operationName.isBlank()) {
            return Optional.empty();
        }
        String requested = operationName.trim();
        return Arrays.stream(values())
                .filter(operation -> operation.operationName.equals(requested))
                .findFirst();
    }

    private static <E extends Enum<E>> List<String> lowerCased(E[] values) {
        return Arrays.stream(values).map(value -> value.name().toLowerCase(Locale.ROOT)).toList();
    }

    private static List<String> codes(List<ClinicalEvent.ClinicalEventType> values) {
        return values.stream().map(value -> value.name().toLowerCase(Locale.ROOT)).toList();
    }
}
