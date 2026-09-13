package com.careme.backend.service;

import com.careme.backend.dto.ClinicalEventIntent;
import com.careme.backend.entity.ClinicalEvent;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ClinicalEventRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(ClinicalEventRegistrationService.class);

    private final ClinicalEventIntentValidator intentValidator;
    private final ClinicalEventDateNormalizer dateNormalizer;
    private final ClinicalEventMarkdownStore markdownStore;
    private final ClinicalEventIndexWriter indexWriter;
    private final ClinicalEventConversationRegistry conversationRegistry;

    public ClinicalEventRegistrationService(
            ClinicalEventIntentValidator intentValidator,
            ClinicalEventDateNormalizer dateNormalizer,
            ClinicalEventMarkdownStore markdownStore,
            ClinicalEventIndexWriter indexWriter,
            ClinicalEventConversationRegistry conversationRegistry) {
        this.intentValidator = intentValidator;
        this.dateNormalizer = dateNormalizer;
        this.markdownStore = markdownStore;
        this.indexWriter = indexWriter;
        this.conversationRegistry = conversationRegistry;
    }

    public ClinicalEventRegistrationResult register(
            String conversationId,
            ClinicalEventIntent intent,
            LocalDate referenceDate) {
        try {
            intentValidator.validate(intent);
            if (intent.kind() == ClinicalEventIntent.Kind.CLARIFICATION) {
                return new ClinicalEventRegistrationResult(
                        ClinicalEventRegistrationResult.Kind.CLARIFICATION, List.of(), intent.clarification());
            }
            if (intent.kind() == ClinicalEventIntent.Kind.CONVERSATION) {
                return new ClinicalEventRegistrationResult(
                        ClinicalEventRegistrationResult.Kind.CONVERSATION, List.of(), "Conversación general");
            }

            List<PendingEvent> pending = new ArrayList<>();
            List<ClinicalEvent> duplicates = new ArrayList<>();
            for (ClinicalEventIntent.Candidate candidate : intent.events()) {
                ClinicalEventDateNormalizer.NormalizedDate normalizedDate = dateNormalizer.normalize(
                        candidate.date(), candidate.datePrecision(), candidate.dateText(), referenceDate);
                String fingerprint = fingerprint(candidate, normalizedDate);
                ClinicalEvent existing = conversationRegistry.find(conversationId, fingerprint);
                if (existing != null) {
                    duplicates.add(existing);
                    continue;
                }
                pending.add(new PendingEvent(candidate, normalizedDate));
            }

            if (pending.isEmpty()) {
                return new ClinicalEventRegistrationResult(
                        ClinicalEventRegistrationResult.Kind.DUPLICATE, duplicates, "El hecho ya estaba registrado.");
            }

            // One reservation for the whole batch: nothing is published until
            // every document is written, so allocating per event would hand out
            // the same code twice.
            List<String> codes = markdownStore.reserveCodes(pending.size());
            List<ClinicalEvent> newEvents = new ArrayList<>(pending.size());
            for (int index = 0; index < pending.size(); index++) {
                PendingEvent entry = pending.get(index);
                newEvents.add(new ClinicalEvent(
                        UUID.randomUUID(),
                        codes.get(index),
                        entry.candidate().type(),
                        entry.date().date(),
                        entry.date().precision(),
                        entry.date().text(),
                        entry.candidate().content(),
                        ClinicalEvent.EventSource.PATIENT,
                        OffsetDateTime.now(ZoneOffset.UTC)));
            }

            try {
                markdownStore.writeAtomically(newEvents);
                indexWriter.writeAll(newEvents);
            } catch (Exception exception) {
                log.error("Could not persist clinical events conversationId={}", conversationId, exception);
                newEvents.forEach(event -> deleteQuietly(event.code()));
                indexWriter.deleteAll(newEvents);
                return new ClinicalEventRegistrationResult(
                        ClinicalEventRegistrationResult.Kind.FAILURE,
                        List.of(),
                        "No se pudo registrar el hecho. Puedes volver a intentarlo.");
            }

            newEvents.forEach(event -> conversationRegistry.add(conversationId, fingerprint(event), event));
            List<ClinicalEvent> registered = List.copyOf(newEvents);
            return new ClinicalEventRegistrationResult(
                    ClinicalEventRegistrationResult.Kind.REGISTERED,
                    registered,
                    confirmation(registered));
        } catch (RuntimeException exception) {
            log.error("Could not register clinical events conversationId={}", conversationId, exception);
            return new ClinicalEventRegistrationResult(
                    ClinicalEventRegistrationResult.Kind.FAILURE,
                    List.of(),
                    "No se pudo registrar el hecho. Puedes volver a intentarlo.");
        }
    }

    /** A candidate that survived deduplication and still awaits its code. */
    private record PendingEvent(
            ClinicalEventIntent.Candidate candidate,
            ClinicalEventDateNormalizer.NormalizedDate date) {
    }

    private static String fingerprint(ClinicalEventIntent.Candidate candidate,
            ClinicalEventDateNormalizer.NormalizedDate date) {
        return candidate.type() + "|" + candidate.content().trim().toLowerCase()
                + "|" + date.precision() + "|" + (date.text() == null ? "" : date.text().trim().toLowerCase());
    }

    private static String fingerprint(ClinicalEvent event) {
        return event.type() + "|" + event.content().trim().toLowerCase()
                + "|" + event.datePrecision() + "|" + (event.dateText() == null ? "" : event.dateText().trim().toLowerCase());
    }

    private String confirmation(List<ClinicalEvent> events) {
        return events.size() == 1
                ? "Hecho registrado en tu historia clínica."
                : events.size() + " hechos registrados en tu historia clínica.";
    }

    private void deleteQuietly(String code) {
        try {
            markdownStore.delete(code);
        } catch (Exception ignored) {
        }
    }
}
