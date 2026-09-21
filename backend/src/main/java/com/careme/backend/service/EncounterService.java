package com.careme.backend.service;

import com.careme.backend.dto.ChatMessageResponse;
import com.careme.backend.dto.ClinicalEventIntent;
import com.careme.backend.entity.ClinicalEvent;
import com.careme.backend.entity.Encounter;
import com.careme.backend.entity.EncounterMeasurementNote;
import com.careme.backend.entity.EncounterNote;
import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Owns the life cycle of the consultation: it opens with the conversation, collects
 * in notes what the person mentions, and at its close registers the admissible
 * facts with their provenance.
 *
 * <p>The consultation is persisted from the moment it opens, so a restart keeps
 * what the person already told instead of starting again; only the close writes
 * clinical events.
 */
@Service
public class EncounterService {

    private static final Logger log = LoggerFactory.getLogger(EncounterService.class);

    private static final String COLLECTED =
            "Lo he anotado. Quedará registrado en tu historia cuando cierres la consulta.";
    private static final String ALREADY_COLLECTED = "Ese hecho ya estaba anotado en esta consulta.";
    private static final String MEASUREMENT_COLLECTED =
            "Lo he anotado. Quedará en tu seguimiento cuando cierres la consulta.";
    private static final String MEASUREMENT_ALREADY_COLLECTED =
            "Esa medición ya estaba anotada en esta consulta.";
    private static final String COULD_NOT_COLLECT =
            "No he podido anotar ese hecho. Puedes volver a intentarlo.";
    private static final String COULD_NOT_CLOSE =
            "No he podido cerrar la consulta ni registrar lo que hablamos. Puedes volver a intentarlo.";
    private static final String ALREADY_CLOSED =
            "La consulta ya estaba cerrada y no queda nada nuevo que registrar.";
    private static final String NOTHING_TO_REGISTER =
            "Hemos cerrado la consulta. No había hechos médicos que registrar.";

    private static final int MOTIVE_LIMIT = 60;

    private static final String PARTIAL_CLOSE =
            "He registrado las mediciones en tu seguimiento, pero no he podido completar el registro"
                    + " de los hechos. Puedes volver a intentarlo.";

    private final EncounterMarkdownStore markdownStore;
    private final EncounterIndexWriter indexWriter;
    private final ClinicalEventRegistrationService registrationService;
    private final ClinicalEventDateNormalizer dateNormalizer;
    private final MeasurementRegistrationService measurementRegistrationService;

    /** Close outcomes already produced, so repeating a close has no new effect. */
    private final Map<String, ChatMessageResponse> closeOutcomes = new HashMap<>();

    private final Map<String, String> codeByConversation = new HashMap<>();

    public EncounterService(
            EncounterMarkdownStore markdownStore,
            EncounterIndexWriter indexWriter,
            ClinicalEventRegistrationService registrationService,
            ClinicalEventDateNormalizer dateNormalizer,
            MeasurementRegistrationService measurementRegistrationService) {
        this.markdownStore = markdownStore;
        this.indexWriter = indexWriter;
        this.registrationService = registrationService;
        this.dateNormalizer = dateNormalizer;
        this.measurementRegistrationService = measurementRegistrationService;
    }

    /**
     * The open consultation of a conversation, opening one when there is none.
     *
     * <p>Only one consultation is in progress: opening a new one closes the
     * previous one with whatever it had collected.
     */
    public synchronized Encounter current(String conversationId) {
        try {
            String known = codeByConversation.get(conversationId);
            if (known != null) {
                Optional<Encounter> open = openConsultation(known);
                if (open.isPresent()) {
                    return open.get();
                }
                codeByConversation.remove(conversationId);
            }
            Optional<Encounter> existing = markdownStore.findOpenByConversation(conversationId);
            if (existing.isPresent()) {
                codeByConversation.put(conversationId, existing.get().code());
                return existing.get();
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read the open consultation", exception);
        }

        closeOtherConversations(conversationId);
        try {
            Encounter opened = Encounter.opened(
                    UUID.randomUUID(),
                    markdownStore.reserveCodes(1).getFirst(),
                    conversationId,
                    OffsetDateTime.now());
            markdownStore.create(opened);
            indexWriter.write(opened);
            codeByConversation.put(conversationId, opened.code());
            return opened;
        } catch (IOException exception) {
            log.error("Could not open the consultation conversationId={}", conversationId, exception);
            throw new IllegalStateException("Could not open the consultation", exception);
        }
    }

    /** Collects one fact the person mentioned in the notes of the open consultation. */
    public synchronized EncounterNoteResult collect(
            String conversationId, ClinicalEventIntent.Candidate candidate, LocalDate referenceDate) {
        Encounter encounter = current(conversationId);
        ClinicalEventDateNormalizer.NormalizedDate normalized = dateNormalizer.normalize(
                candidate.date(), candidate.datePrecision(), candidate.dateText(), referenceDate);
        String fingerprint = fingerprint(normalized, candidate);
        Optional<EncounterNote> already = encounter.notes().stream()
                .filter(note -> fingerprint(note).equals(fingerprint))
                .findFirst();
        if (already.isPresent()) {
            return new EncounterNoteResult(EncounterNoteResult.Kind.DUPLICATE, already.get(), ALREADY_COLLECTED);
        }

        EncounterNote note = new EncounterNote(
                candidate.type(), candidate.content(), normalized.date(), normalized.precision(),
                normalized.text());
        try {
            Encounter updated = encounter.withNote(note);
            markdownStore.replace(updated);
            indexWriter.write(updated);
        } catch (Exception exception) {
            log.error("Could not collect the note conversationId={}", conversationId, exception);
            return new EncounterNoteResult(EncounterNoteResult.Kind.FAILURE, null, COULD_NOT_COLLECT);
        }
        return new EncounterNoteResult(EncounterNoteResult.Kind.COLLECTED, note, COLLECTED);
    }

    /**
     * Collects one measurement the person mentioned in the notes of the open
     * consultation. It is not registered here: the close is what writes into the
     * tracking, exactly as it does for clinical facts.
     */
    public synchronized EncounterMeasurementResult collectMeasurement(
            String conversationId, EncounterMeasurementNote note) {
        Encounter encounter = current(conversationId);
        Optional<EncounterMeasurementNote> already = encounter.measurementNotes().stream()
                .filter(collected -> fingerprint(collected).equals(fingerprint(note)))
                .findFirst();
        if (already.isPresent()) {
            return new EncounterMeasurementResult(
                    EncounterMeasurementResult.Kind.DUPLICATE, already.get(), MEASUREMENT_ALREADY_COLLECTED);
        }

        try {
            Encounter updated = encounter.withMeasurementNote(note);
            markdownStore.replace(updated);
            indexWriter.write(updated);
        } catch (Exception exception) {
            log.error("Could not collect the measurement conversationId={}", conversationId, exception);
            return new EncounterMeasurementResult(
                    EncounterMeasurementResult.Kind.FAILURE, null, COULD_NOT_COLLECT);
        }
        return new EncounterMeasurementResult(
                EncounterMeasurementResult.Kind.COLLECTED, note, MEASUREMENT_COLLECTED);
    }

    /** Two mentions of the same metric, day and values are the same measurement. */
    private static String fingerprint(EncounterMeasurementNote note) {
        return note.metricCode() + "|" + note.date() + "|" + new TreeMap<>(note.values());
    }

    /**
     * Closes the consultation of a conversation, registering the facts it collected
     * with their provenance and keeping the derived summary.
     *
     * <p>Repeating the close returns the outcome it already produced and registers
     * nothing new. A close that cannot complete leaves the consultation open so the
     * person can retry it.
     */
    public synchronized ChatMessageResponse close(String conversationId) {
        ChatMessageResponse previous = closeOutcomes.get(conversationId);
        if (previous != null) {
            return previous;
        }
        Optional<Encounter> open;
        try {
            open = markdownStore.findOpenByConversation(conversationId);
        } catch (IOException exception) {
            log.error("Could not read the consultation to close conversationId={}", conversationId, exception);
            return failure(conversationId, COULD_NOT_CLOSE);
        }
        if (open.isEmpty()) {
            ChatMessageResponse outcome = ChatMessageResponse.of(
                    conversationId, null, ChatMessageResponse.Status.NOTHING_TO_REGISTER, ALREADY_CLOSED,
                    List.of());
            closeOutcomes.put(conversationId, outcome);
            return outcome;
        }

        ChatMessageResponse outcome = closeEncounter(open.get(), LocalDate.now());
        if (outcome.status() != ChatMessageResponse.Status.FAILED) {
            closeOutcomes.put(conversationId, outcome);
        }
        return outcome;
    }

    /** Closes every consultation that was left open and registers what it collected. */
    public synchronized int closeAbandoned() {
        int closed = 0;
        try {
            for (Encounter encounter : markdownStore.readOpen()) {
                closeEncounter(encounter, LocalDate.now());
                closed++;
            }
        } catch (IOException exception) {
            log.error("Could not close the consultations that were left open", exception);
        }
        return closed;
    }

    private ChatMessageResponse closeEncounter(Encounter encounter, LocalDate referenceDate) {
        // A note only reaches the consultation after passing the same deterministic
        // checks the registration path runs, and a repeated note is purged here, so
        // neither the registered events nor the summary can hold the same fact twice.
        List<EncounterNote> distinct = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (EncounterNote note : encounter.notes()) {
            if (seen.add(fingerprint(note))) {
                distinct.add(note);
            }
        }
        List<ClinicalEventIntent.Candidate> candidates = distinct.stream()
                .map(note -> new ClinicalEventIntent.Candidate(
                        note.type(), note.content(), note.date(), note.datePrecision(), note.dateText()))
                .toList();

        // The tracking is written first: if the batch cannot be stored, nothing of the
        // close has been written yet and the consultation stays open so it can be retried.
        MeasurementRegistrationResult measurements = measurementRegistrationService.register(
                encounter.code(), distinctMeasurements(encounter));
        if (measurements.kind() == MeasurementRegistrationResult.Kind.FAILURE) {
            return failure(encounter.conversationId(), COULD_NOT_CLOSE);
        }

        ClinicalEventRegistrationResult registration = candidates.isEmpty()
                ? new ClinicalEventRegistrationResult(
                        ClinicalEventRegistrationResult.Kind.REGISTERED, List.of(), "")
                : registrationService.register(
                        encounter.conversationId(),
                        new ClinicalEventIntent(ClinicalEventIntent.Kind.EVENTS, candidates, null),
                        referenceDate,
                        encounter.code());
        if (registration.kind() == ClinicalEventRegistrationResult.Kind.FAILURE) {
            // A registration that completed is never reverted because a later one failed,
            // so the person is told what was registered and what was not.
            return failure(
                    encounter.conversationId(),
                    measurements.registered() > 0 ? PARTIAL_CLOSE : COULD_NOT_CLOSE);
        }

        List<ClinicalEvent> registered = registration.events();
        try {
            markdownStore.replace(encounter.closed(
                    motive(encounter, registered), summary(encounter, registered), OffsetDateTime.now()));
            indexWriter.write(markdownStore.read(encounter.code()));
        } catch (Exception exception) {
            log.error("Could not close the consultation code={}", encounter.code(), exception);
            return failure(
                    encounter.conversationId(),
                    measurements.registered() > 0 ? PARTIAL_CLOSE : COULD_NOT_CLOSE);
        }

        return outcome(encounter.conversationId(), registered, registration, measurements);
    }

    /** The measurements the consultation collected, with a repeated mention counted once. */
    private static List<EncounterMeasurementNote> distinctMeasurements(Encounter encounter) {
        List<EncounterMeasurementNote> distinct = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (EncounterMeasurementNote note : encounter.measurementNotes()) {
            if (seen.add(fingerprint(note))) {
                distinct.add(note);
            }
        }
        return distinct;
    }

    private ChatMessageResponse outcome(
            String conversationId,
            List<ClinicalEvent> registered,
            ClinicalEventRegistrationResult registration,
            MeasurementRegistrationResult measurements) {
        ChatMessageResponse.Status status;
        if (registered.isEmpty() && measurements.registered() == 0) {
            status = registration.kind() == ClinicalEventRegistrationResult.Kind.DUPLICATE
                    ? ChatMessageResponse.Status.DUPLICATE
                    : ChatMessageResponse.Status.NOTHING_TO_REGISTER;
        } else {
            status = ChatMessageResponse.Status.REGISTERED;
        }

        List<ChatMessageResponse.OperationSummary> operations = new ArrayList<>();
        if (!registered.isEmpty()) {
            operations.add(ChatMessageResponse.OperationSummary.of(
                    ChatMessageResponse.Status.REGISTERED, registered, null, List.of()));
        }
        if (measurements.registered() > 0) {
            operations.add(ChatMessageResponse.OperationSummary.of(
                    ChatMessageResponse.Status.REGISTERED, List.of(), null, List.of()));
        }

        return ChatMessageResponse.of(
                        conversationId, null, status, message(status, registered, measurements),
                        registered, null, List.of())
                .withOperations(operations);
    }

    private static String message(
            ChatMessageResponse.Status status,
            List<ClinicalEvent> registered,
            MeasurementRegistrationResult measurements) {
        if (status == ChatMessageResponse.Status.DUPLICATE) {
            return "Lo que hablamos ya estaba registrado en tu historia clínica.";
        }
        if (status != ChatMessageResponse.Status.REGISTERED) {
            return NOTHING_TO_REGISTER;
        }

        List<String> registeredThings = new ArrayList<>();
        if (!registered.isEmpty()) {
            registeredThings.add(registered.size() == 1
                    ? "un hecho en tu historia clínica"
                    : registered.size() + " hechos en tu historia clínica");
        }
        if (measurements.registered() > 0) {
            registeredThings.add(measurements.registered() == 1
                    ? "una medición en tu seguimiento"
                    : measurements.registered() + " mediciones en tu seguimiento");
        }

        String confirmation = "He registrado " + String.join(" y ", registeredThings)
                + ". Ya queda disponible para consulta.";
        if (measurements.replaced() > 0) {
            confirmation += " Esa medición de ese día ya la tenías: he actualizado su valor.";
        }
        return confirmation;
    }

    private static ChatMessageResponse failure(String conversationId, String message) {
        return ChatMessageResponse.of(
                conversationId, null, ChatMessageResponse.Status.FAILED, message, List.of());
    }

    /** The motive is a title derived from what the consultation is about. */
    private static String motive(Encounter encounter, List<ClinicalEvent> registered) {
        String basis = registered.isEmpty()
                ? encounter.notes().stream().map(EncounterNote::content).findFirst().orElse(null)
                : registered.getFirst().content();
        if (basis == null) {
            return "Consulta sin hechos médicos";
        }
        String trimmed = basis.trim();
        return trimmed.length() <= MOTIVE_LIMIT ? trimmed : trimmed.substring(0, MOTIVE_LIMIT - 3) + "...";
    }

    /** The summary is derived information: it only restates the registered facts. */
    private static String summary(Encounter encounter, List<ClinicalEvent> registered) {
        if (registered.isEmpty()) {
            return encounter.notes().isEmpty()
                    ? "No se mencionaron hechos médicos durante la consulta."
                    : "No se registró ningún hecho de los mencionados en la consulta.";
        }
        StringBuilder summary = new StringBuilder("Hechos registrados en esta consulta:")
                .append(' ').append(registered.size()).append('.');
        for (ClinicalEvent event : registered) {
            summary.append(' ').append(event.type().name().toLowerCase()).append(": ")
                    .append(event.content());
            if (event.date() != null) {
                summary.append(" (").append(event.date()).append(')');
            }
            summary.append('.');
        }
        return summary.toString();
    }

    private void closeOtherConversations(String conversationId) {
        try {
            for (Encounter open : markdownStore.readOpen()) {
                if (!open.conversationId().equals(conversationId)) {
                    closeEncounter(open, LocalDate.now());
                }
            }
        } catch (IOException exception) {
            log.error("Could not close the previous consultation", exception);
        }
    }

    private Optional<Encounter> openConsultation(String code) {
        try {
            Encounter encounter = markdownStore.read(code);
            return encounter.isOpen() ? Optional.of(encounter) : Optional.empty();
        } catch (IOException missing) {
            return Optional.empty();
        }
    }

    private static String fingerprint(
            ClinicalEventDateNormalizer.NormalizedDate date, ClinicalEventIntent.Candidate candidate) {
        return candidate.type() + "|" + candidate.content().trim().toLowerCase()
                + "|" + date.precision() + "|"
                + (date.text() == null ? "" : date.text().trim().toLowerCase());
    }

    private static String fingerprint(EncounterNote note) {
        return note.type() + "|" + note.content().trim().toLowerCase()
                + "|" + note.datePrecision() + "|"
                + (note.dateText() == null ? "" : note.dateText().trim().toLowerCase());
    }
}
