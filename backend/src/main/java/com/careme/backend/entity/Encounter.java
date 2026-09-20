package com.careme.backend.entity;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * The conversation as a consultation: a record with its own identity, a life
 * cycle and the notes it collects while it is open.
 *
 * <p>A consultation opens with the conversation and closes when the person ends
 * it, when they start another one or when the process ends. A closed
 * consultation is never reopened: its notes are what the close registers, and
 * the summary it keeps is derived information, reconstructible from those
 * registered facts.
 */
public record Encounter(
        UUID id,
        String code,
        String conversationId,
        Status status,
        List<EncounterNote> notes,
        String motive,
        String summary,
        OffsetDateTime createdAt,
        OffsetDateTime closedAt) {

    private static final Pattern CODE_PATTERN = Pattern.compile("enc_[0-9]{3,}");

    public Encounter {
        if (id == null) {
            throw new IllegalArgumentException("Consultation id must not be null");
        }
        if (code == null || !CODE_PATTERN.matcher(code).matches()) {
            throw new IllegalArgumentException("Consultation code must match enc_NNN");
        }
        if (conversationId == null || conversationId.isBlank()) {
            throw new IllegalArgumentException("Consultation conversation must not be blank");
        }
        if (status == null) {
            throw new IllegalArgumentException("Consultation status must not be null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("Consultation creation time must not be null");
        }
        if (status == Status.CLOSED && closedAt == null) {
            throw new IllegalArgumentException("A closed consultation must record when it was closed");
        }
        if (status == Status.OPEN && closedAt != null) {
            throw new IllegalArgumentException("An open consultation must not record a close time");
        }
        notes = notes == null ? List.of() : List.copyOf(notes);
    }

    public enum Status {
        OPEN,
        CLOSED
    }

    /** A consultation that has just started, with no notes and no close. */
    public static Encounter opened(UUID id, String code, String conversationId, OffsetDateTime createdAt) {
        return new Encounter(id, code, conversationId, Status.OPEN, List.of(), null, null, createdAt, null);
    }

    /** The same consultation with one more note collected during the conversation. */
    public Encounter withNote(EncounterNote note) {
        requireOpen();
        List<EncounterNote> collected = new ArrayList<>(notes);
        collected.add(note);
        return new Encounter(id, code, conversationId, status, collected, motive, summary, createdAt, closedAt);
    }

    /**
     * The same consultation, closed, keeping the motive and summary the close
     * produced. The summary may be absent when it could not be stored: the
     * consultation stays closed with its notes either way.
     */
    public Encounter closed(String motive, String summary, OffsetDateTime closedAt) {
        requireOpen();
        return new Encounter(
                id, code, conversationId, Status.CLOSED, notes, motive, summary, createdAt, closedAt);
    }

    public boolean isOpen() {
        return status == Status.OPEN;
    }

    private void requireOpen() {
        if (status != Status.OPEN) {
            throw new IllegalStateException("The consultation is already closed: " + code);
        }
    }
}
