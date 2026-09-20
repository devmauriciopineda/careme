package com.careme.backend.service;

import com.careme.backend.entity.ClinicalEvent;
import com.careme.backend.entity.Encounter;
import com.careme.backend.entity.EncounterNote;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Reads and writes the Markdown source of truth for consultations.
 *
 * <p>A consultation is created when its conversation starts and is rewritten as
 * it collects notes, so a document that already exists is replaced rather than
 * refused —unlike an event document, which is create-only because an event is
 * never rewritten. Both publications are atomic.
 */
@Service
public class EncounterMarkdownStore {

    private static final String NOTE_BLOCK = "\n## Nota\n\n";

    private final Path encountersDirectory;

    @Autowired
    public EncounterMarkdownStore(
            @Value("${careme.encounters.directory:data/encounters}") String directory) {
        this(Path.of(directory));
    }

    EncounterMarkdownStore(Path encountersDirectory) {
        this.encountersDirectory = encountersDirectory;
    }

    /** Publishes the document of a consultation that did not exist yet. */
    public void create(Encounter encounter) throws IOException {
        Files.createDirectories(encountersDirectory);
        Path target = encountersDirectory.resolve(encounter.code() + ".md");
        if (Files.exists(target)) {
            throw new IOException("Consultation document already exists: " + target.getFileName());
        }
        publish(encounter, target, false);
    }

    /** Republishes the document of a consultation that is still open. */
    public void replace(Encounter encounter) throws IOException {
        Files.createDirectories(encountersDirectory);
        publish(encounter, encountersDirectory.resolve(encounter.code() + ".md"), true);
    }

    private void publish(Encounter encounter, Path target, boolean replaceExisting) throws IOException {
        Path temporary = encountersDirectory.resolve("." + encounter.code() + "." + UUID.randomUUID() + ".tmp");
        try {
            Files.writeString(temporary, serialize(encounter), StandardCharsets.UTF_8);
            if (replaceExisting) {
                try {
                    Files.move(
                            temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (FileSystemException notAtomic) {
                    // Windows refuses an atomic move onto a file that exists, so the
                    // replace falls back to a plain move within the same directory:
                    // the document is replaced, not appended to.
                    Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
                }
            } else {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE);
            }
        } catch (IOException exception) {
            deleteQuietly(temporary);
            throw exception;
        }
    }

    /**
     * Reserves the next free consultation codes.
     *
     * The code is the document name, so the persisted documents are the only
     * authority on what is still free; a counter in memory would restart with the
     * process and hand out a code that is already published.
     */
    public synchronized List<String> reserveCodes(int count) {
        long highest = highestCodeNumber();
        List<String> codes = new ArrayList<>(count);
        for (int offset = 1; offset <= count; offset++) {
            codes.add(formatCode(highest + offset));
        }
        return codes;
    }

    public Encounter read(String code) throws IOException {
        return deserialize(Files.readString(encountersDirectory.resolve(code + ".md"), StandardCharsets.UTF_8));
    }

    public List<Encounter> readAll() throws IOException {
        if (!Files.exists(encountersDirectory)) {
            return List.of();
        }
        try (var paths = Files.list(encountersDirectory)) {
            return paths
                    .filter(path -> path.getFileName().toString().endsWith(".md"))
                    .map(this::readUnchecked)
                    .toList();
        }
    }

    /**
     * The open consultation of one conversation, so a conversation that outlives a
     * restart keeps the notes it had already collected instead of starting again.
     */
    public java.util.Optional<Encounter> findOpenByConversation(String conversationId) throws IOException {
        return readAll().stream()
                .filter(Encounter::isOpen)
                .filter(encounter -> encounter.conversationId().equals(conversationId))
                .findFirst();
    }

    /** Every consultation that is still open and therefore has to be closed. */
    public List<Encounter> readOpen() throws IOException {
        return readAll().stream().filter(Encounter::isOpen).toList();
    }

    private long highestCodeNumber() {
        if (!Files.exists(encountersDirectory)) {
            return 0;
        }
        try (var paths = Files.list(encountersDirectory)) {
            return paths.mapToLong(path -> codeNumber(path.getFileName().toString())).max().orElse(0);
        } catch (IOException exception) {
            throw new UncheckedIOException("Cannot read the consultation directory", exception);
        }
    }

    /** The number of an `enc_NNN.md` document name, or 0 when it is not one. */
    private static long codeNumber(String fileName) {
        if (!fileName.startsWith("enc_") || !fileName.endsWith(".md")) {
            return 0;
        }
        String digits = fileName.substring("enc_".length(), fileName.length() - ".md".length());
        try {
            return Long.parseLong(digits);
        } catch (NumberFormatException notACode) {
            return 0;
        }
    }

    private static String formatCode(long number) {
        return "enc_" + String.format("%03d", number);
    }

    private Encounter readUnchecked(Path path) {
        try {
            return deserialize(Files.readString(path, StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read consultation: " + path, exception);
        }
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }

    static String serialize(Encounter encounter) {
        StringBuilder markdown = new StringBuilder("---\n")
                .append("id: ").append(encounter.id()).append('\n')
                .append("code: ").append(encounter.code()).append('\n')
                .append("conversation: ").append(encounter.conversationId()).append('\n')
                .append("status: ").append(encounter.status().name().toLowerCase()).append('\n')
                .append("motive: ").append(quoted(encounter.motive())).append('\n')
                .append("summary: ").append(quoted(encounter.summary())).append('\n')
                .append("created_at: ").append(encounter.createdAt()).append('\n')
                .append("closed_at: ")
                .append(encounter.closedAt() == null ? "" : encounter.closedAt().toString())
                .append('\n')
                .append("---\n\n")
                .append("# Consulta ").append(encounter.code()).append('\n');
        if (encounter.summary() != null) {
            // The summary is derived information: it is marked as such in the
            // document so it is never mistaken for the source of truth, which are
            // the facts the close registered.
            markdown.append("\n## Resumen (información derivada)\n\n")
                    .append(encounter.summary())
                    .append('\n');
        }
        for (EncounterNote note : encounter.notes()) {
            markdown.append(NOTE_BLOCK)
                    .append("- type: ").append(note.type().name().toLowerCase()).append('\n')
                    .append("- date: ").append(note.date() == null ? "" : note.date().toString()).append('\n')
                    .append("- date_precision: ")
                    .append(note.datePrecision().name().toLowerCase())
                    .append('\n')
                    .append("- date_text: ").append(quoted(note.dateText())).append('\n')
                    .append("- content: ").append(quoted(note.content())).append('\n');
        }
        return markdown.toString();
    }

    static Encounter deserialize(String markdown) {
        String[] sections = markdown.split("\\n---\\n", 2);
        if (sections.length != 2 || !sections[0].startsWith("---\n")) {
            throw new IllegalArgumentException("Consultation Markdown front matter is invalid");
        }
        Map<String, String> metadata = parseMetadata(sections[0].substring(4));
        return new Encounter(
                UUID.fromString(required(metadata, "id")),
                required(metadata, "code"),
                required(metadata, "conversation"),
                Encounter.Status.valueOf(required(metadata, "status").toUpperCase()),
                notes(sections[1]),
                optional(metadata, "motive").map(EncounterMarkdownStore::unquoted).orElse(null),
                optional(metadata, "summary").map(EncounterMarkdownStore::unquoted).orElse(null),
                OffsetDateTime.parse(required(metadata, "created_at")),
                optional(metadata, "closed_at").map(OffsetDateTime::parse).orElse(null));
    }

    /** The notes of the document body, in the order they were collected. */
    private static List<EncounterNote> notes(String body) {
        List<EncounterNote> notes = new ArrayList<>();
        String[] blocks = body.split(NOTE_BLOCK);
        for (int index = 1; index < blocks.length; index++) {
            Map<String, String> fields = parseFields(blocks[index]);
            String precision = required(fields, "date_precision");
            notes.add(new EncounterNote(
                    ClinicalEvent.ClinicalEventType.valueOf(required(fields, "type").toUpperCase()),
                    unquoted(required(fields, "content")),
                    optional(fields, "date").map(LocalDate::parse).orElse(null),
                    ClinicalEvent.DatePrecision.valueOf(precision.toUpperCase()),
                    optional(fields, "date_text").map(EncounterMarkdownStore::unquoted).orElse(null)));
        }
        return notes;
    }

    /** Reads the `- key: value` lines of one note block. */
    private static Map<String, String> parseFields(String block) {
        Map<String, String> fields = new HashMap<>();
        for (String line : block.split("\\n")) {
            if (!line.startsWith("- ")) {
                continue;
            }
            int separator = line.indexOf(':');
            if (separator > 0) {
                fields.put(line.substring(2, separator).trim(), line.substring(separator + 1).trim());
            }
        }
        return fields;
    }

    private static Map<String, String> parseMetadata(String frontMatter) {
        Map<String, String> metadata = new HashMap<>();
        for (String line : frontMatter.split("\\n")) {
            int separator = line.indexOf(':');
            if (separator > 0) {
                metadata.put(line.substring(0, separator).trim(), line.substring(separator + 1).trim());
            }
        }
        return metadata;
    }

    private static String required(Map<String, String> fields, String key) {
        String value = fields.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Consultation metadata missing: " + key);
        }
        return value;
    }

    private static java.util.Optional<String> optional(Map<String, String> fields, String key) {
        String value = fields.get(key);
        return value == null || value.isBlank() ? java.util.Optional.empty() : java.util.Optional.of(value);
    }

    /** Quotes a value, escaping newlines so a note never breaks the line format. */
    private static String quoted(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return "\""
                + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
                + "\"";
    }

    private static String unquoted(String value) {
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1)
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\")
                    .replace("\\n", "\n");
        }
        return value;
    }
}
