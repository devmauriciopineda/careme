package com.careme.backend.service;

import com.careme.backend.entity.PatientProfile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Reads and atomically publishes the current patient profile document. */
@Service
public class PatientProfileMarkdownStore {

    private static final String PROFILE_FILE = "profile.md";
    private final Path profileDirectory;

    @Autowired
    public PatientProfileMarkdownStore(
            @Value("${careme.patient-profile.directory:data/patient}") String directory) {
        this(Path.of(directory));
    }

    PatientProfileMarkdownStore(Path profileDirectory) {
        this.profileDirectory = profileDirectory;
    }

    public PatientProfile read() throws IOException {
        Path target = profileDirectory.resolve(PROFILE_FILE);
        return Files.exists(target) ? deserialize(Files.readString(target, StandardCharsets.UTF_8))
                : PatientProfile.empty();
    }

    public void write(PatientProfile profile) throws IOException {
        Files.createDirectories(profileDirectory);
        Path target = profileDirectory.resolve(PROFILE_FILE);
        Path temporary = profileDirectory.resolve("." + PROFILE_FILE + "." + UUID.randomUUID() + ".tmp");
        try {
            Files.writeString(temporary, serialize(profile), StandardCharsets.UTF_8);
            try {
                move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (FileSystemException notAtomic) {
                move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            Files.deleteIfExists(temporary);
            throw exception;
        }
    }

    void move(Path source, Path target, StandardCopyOption... options) throws IOException {
        Files.move(source, target, options);
    }

    static String serialize(PatientProfile profile) {
        return "---\n"
                + "name: " + quote(profile.name()) + "\n"
                + "date_of_birth: " + value(profile.dateOfBirth()) + "\n"
                + "sex: " + quote(profile.sex()) + "\n"
                + "occupation: " + quote(profile.occupation()) + "\n"
                + "blood_group: " + quote(profile.bloodGroup()) + "\n"
                + "rh: " + quote(profile.rh()) + "\n"
                + "height_cm: " + value(profile.heightCm()) + "\n"
                + "---\n\n# Perfil del paciente\n";
    }

    static PatientProfile deserialize(String markdown) {
        String[] sections = markdown.split("\\n---\\n", 2);
        if (sections.length != 2 || !sections[0].startsWith("---\n")) {
            throw new IllegalArgumentException("Patient profile Markdown front matter is invalid");
        }
        Map<String, String> metadata = new HashMap<>();
        for (String line : sections[0].substring(4).split("\\n")) {
            int separator = line.indexOf(':');
            if (separator > 0) {
                metadata.put(line.substring(0, separator).trim(), line.substring(separator + 1).trim());
            }
        }
        return new PatientProfile(
                unquote(metadata.get("name")),
                date(metadata.get("date_of_birth")),
                unquote(metadata.get("sex")),
                unquote(metadata.get("occupation")),
                unquote(metadata.get("blood_group")),
                unquote(metadata.get("rh")),
                decimal(metadata.get("height_cm")));
    }

    private static String value(Object value) {
        return value == null ? "" : value.toString();
    }

    private static String quote(String value) {
        return value == null ? "" : "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static String unquote(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")
                ? value.substring(1, value.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\")
                : value;
    }

    private static java.time.LocalDate date(String value) {
        return value == null || value.isBlank() ? null : java.time.LocalDate.parse(value);
    }

    private static java.math.BigDecimal decimal(String value) {
        return value == null || value.isBlank() ? null : new java.math.BigDecimal(value);
    }
}