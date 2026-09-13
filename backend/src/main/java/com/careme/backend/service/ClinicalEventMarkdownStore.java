package com.careme.backend.service;

import com.careme.backend.entity.ClinicalEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Reads and writes the Markdown source of truth for clinical events. */
@Service
public class ClinicalEventMarkdownStore {

    private final Path eventsDirectory;

    public ClinicalEventMarkdownStore() {
        this(Path.of("data", "events"));
    }

    ClinicalEventMarkdownStore(Path eventsDirectory) {
        this.eventsDirectory = eventsDirectory;
    }

    public void write(ClinicalEvent event) throws IOException {
        Files.createDirectories(eventsDirectory);
        Path target = eventsDirectory.resolve(event.code() + ".md");
        Files.writeString(target, serialize(event), StandardCharsets.UTF_8);
    }

    public void writeAtomically(List<ClinicalEvent> events) throws IOException {
        Files.createDirectories(eventsDirectory);
        List<Path> temporaryFiles = new java.util.ArrayList<>();
        List<Path> publishedFiles = new java.util.ArrayList<>();
        try {
            for (ClinicalEvent event : events) {
                Path temporary = eventsDirectory.resolve("." + event.code() + "." + UUID.randomUUID() + ".tmp");
                Files.writeString(temporary, serialize(event), StandardCharsets.UTF_8);
                temporaryFiles.add(temporary);
            }
            for (int index = 0; index < events.size(); index++) {
                Path target = eventsDirectory.resolve(events.get(index).code() + ".md");
                Files.move(temporaryFiles.get(index), target, StandardCopyOption.ATOMIC_MOVE);
                publishedFiles.add(target);
            }
        } catch (IOException exception) {
            temporaryFiles.forEach(this::deleteQuietly);
            publishedFiles.forEach(this::deleteQuietly);
            throw exception;
        }
    }

    public void delete(String code) throws IOException {
        Files.deleteIfExists(eventsDirectory.resolve(code + ".md"));
    }

    public ClinicalEvent read(String code) throws IOException {
        Path path = eventsDirectory.resolve(code + ".md");
        return deserialize(Files.readString(path, StandardCharsets.UTF_8));
    }

    public List<ClinicalEvent> readAll() throws IOException {
        if (!Files.exists(eventsDirectory)) {
            return List.of();
        }
        try (var paths = Files.list(eventsDirectory)) {
            return paths
                    .filter(path -> path.getFileName().toString().endsWith(".md"))
                    .map(this::readUnchecked)
                    .toList();
        }
    }

    private ClinicalEvent readUnchecked(Path path) {
        try {
            return deserialize(Files.readString(path, StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read clinical event: " + path, exception);
        }
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }

    static String serialize(ClinicalEvent event) {
        String date = event.date() == null ? "" : event.date().toString();
        String dateText = event.dateText() == null ? "" : quote(event.dateText());
        return "---\n"
                + "id: " + event.id() + "\n"
                + "code: " + event.code() + "\n"
                + "type: " + event.type().name().toLowerCase() + "\n"
                + "date: " + date + "\n"
                + "date_precision: " + event.datePrecision().name().toLowerCase() + "\n"
                + "date_text: " + dateText + "\n"
                + "source: " + event.source().name().toLowerCase() + "\n"
                + "created_at: " + event.createdAt() + "\n"
                + "---\n\n"
                + "# " + event.type().name().toLowerCase() + "\n\n"
                + event.content() + "\n";
    }

    static ClinicalEvent deserialize(String markdown) {
        String[] sections = markdown.split("\\n---\\n", 2);
        if (sections.length != 2 || !sections[0].startsWith("---\n")) {
            throw new IllegalArgumentException("Clinical event Markdown front matter is invalid");
        }
        Map<String, String> metadata = parseMetadata(sections[0].substring(4));
        String body = sections[1];
        int contentStart = body.indexOf("\n\n");
        if (contentStart < 0) {
            throw new IllegalArgumentException("Clinical event Markdown body is invalid");
        }
        String content = body.substring(contentStart + 2).stripTrailing();
        return new ClinicalEvent(
                UUID.fromString(required(metadata, "id")),
                required(metadata, "code"),
                ClinicalEvent.ClinicalEventType.valueOf(required(metadata, "type").toUpperCase()),
                optional(metadata, "date").map(LocalDate::parse).orElse(null),
                ClinicalEvent.DatePrecision.valueOf(required(metadata, "date_precision").toUpperCase()),
                unquote(optional(metadata, "date_text").orElse("")),
                content,
                ClinicalEvent.EventSource.valueOf(required(metadata, "source").toUpperCase()),
                OffsetDateTime.parse(required(metadata, "created_at")));
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

    private static String required(Map<String, String> metadata, String key) {
        String value = metadata.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Clinical event metadata missing: " + key);
        }
        return value;
    }

    private static java.util.Optional<String> optional(Map<String, String> metadata, String key) {
        String value = metadata.get(key);
        return value == null || value.isBlank() ? java.util.Optional.empty() : java.util.Optional.of(value);
    }

    private static String quote(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static String unquote(String value) {
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\");
        }
        return value;
    }
}
