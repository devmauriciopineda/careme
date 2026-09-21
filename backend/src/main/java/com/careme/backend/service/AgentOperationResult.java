package com.careme.backend.service;

import com.careme.backend.dto.AgentOperation;
import com.careme.backend.entity.ClinicalEvent;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The outcome of one operation the assistant asked for.
 *
 * <p>{@link #payload()} is what is handed back to the assistant: the material it
 * may restate, and nothing else. It never carries a path, a file name or any
 * other storage detail, so the assistant cannot learn how the clinical history
 * is stored.
 */
public record AgentOperationResult(
        AgentOperation operation,
        Kind kind,
        Rejection rejection,
        String message,
        Map<String, Object> payload,
        ClinicalAnswerResult answer,
        ClinicalEventRegistrationResult registration,
        EncounterNoteResult note,
        EncounterMeasurementResult measurement) {

    public AgentOperationResult {
        payload = payload == null ? Map.of() : Collections.unmodifiableMap(payload);
    }

    public enum Kind {
        /** The operation ran and produced a result the assistant can use. */
        COMPLETED,
        /** The operation ran and found no records that answer the question. */
        NO_RECORDS,
        /** The operation did not run: it is not available or its request was not admissible. */
        REJECTED,
        /** The operation ran and could not complete. */
        FAILED
    }

    /** Why an operation was not run. It is set only when {@link Kind#REJECTED} applies. */
    public enum Rejection {
        /** The request was incomplete or contradicted the rules, so something has to be asked for. */
        NOT_ADMISSIBLE,
        /** It named an operation that is not available. */
        OUT_OF_REACH,
        /** What was asked belongs to another space, not to the clinical history. */
        ELSEWHERE
    }

    /** Whether the operation produced something the assistant may report. */
    public boolean completed() {
        return kind == Kind.COMPLETED || kind == Kind.NO_RECORDS;
    }

    /** The outcome of a consultation of the clinical history. */
    static AgentOperationResult of(AgentOperation operation, ClinicalAnswerResult answer) {
        Kind kind = switch (answer.kind()) {
            case ANSWERED -> Kind.COMPLETED;
            case NO_RECORDS -> Kind.NO_RECORDS;
            case FAILURE -> Kind.FAILED;
        };
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("answer", answer.message() == null ? "" : answer.message());
        payload.put("events", events(answer.events()));
        payload.put("absence_reason", answer.absenceReason() == null ? "" : answer.absenceReason().value());
        return new AgentOperationResult(operation, kind, null, answer.message(), payload, answer, null, null, null);
    }

    /** The outcome of collecting a fact in the open consultation's notes. */
    static AgentOperationResult of(AgentOperation operation, EncounterNoteResult note) {
        Kind kind = note.kind() == EncounterNoteResult.Kind.FAILURE ? Kind.FAILED : Kind.COMPLETED;
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("noted", note.kind() == EncounterNoteResult.Kind.COLLECTED);
        payload.put("duplicate", note.kind() == EncounterNoteResult.Kind.DUPLICATE);
        payload.put("content", note.note() == null ? "" : note.note().content());
        return new AgentOperationResult(operation, kind, null, note.message(), payload, null, null, note, null);
    }

    /** The outcome of collecting a measurement in the open consultation's notes. */
    static AgentOperationResult of(AgentOperation operation, EncounterMeasurementResult measurement) {
        Kind kind = measurement.kind() == EncounterMeasurementResult.Kind.FAILURE
                ? Kind.FAILED
                : Kind.COMPLETED;
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("noted", measurement.kind() == EncounterMeasurementResult.Kind.COLLECTED);
        payload.put("duplicate", measurement.kind() == EncounterMeasurementResult.Kind.DUPLICATE);
        payload.put("metric", measurement.note() == null ? "" : measurement.note().metricCode());
        return new AgentOperationResult(
                operation, kind, null, measurement.message(), payload, null, null, null, measurement);
    }

    /** The outcome of a registration of clinical facts. */
    static AgentOperationResult of(AgentOperation operation, ClinicalEventRegistrationResult registration) {
        Kind kind = registration.kind() == ClinicalEventRegistrationResult.Kind.FAILURE
                ? Kind.FAILED
                : Kind.COMPLETED;
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("registered", !registration.events().isEmpty());
        payload.put("events", events(registration.events()));
        return new AgentOperationResult(
                operation, kind, null, registration.message(), payload, null, registration, null, null);
    }

    /**
     * The outcome of an operation that was not run. It carries no operation,
     * because nothing was executed.
     */
    static AgentOperationResult rejected(Rejection rejection, String message) {
        return new AgentOperationResult(
                null, Kind.REJECTED, rejection, message, Map.of(), null, null, null, null);
    }

    private static List<Map<String, Object>> events(List<ClinicalEvent> events) {
        return events.stream().map(AgentOperationResult::event).toList();
    }

    private static Map<String, Object> event(ClinicalEvent event) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("code", event.code());
        payload.put("type", event.type().name().toLowerCase(Locale.ROOT));
        payload.put("content", event.content());
        payload.put("date", event.date() == null ? null : event.date().toString());
        payload.put("date_precision", event.datePrecision().name().toLowerCase(Locale.ROOT));
        payload.put("date_text", event.dateText());
        return payload;
    }
}
