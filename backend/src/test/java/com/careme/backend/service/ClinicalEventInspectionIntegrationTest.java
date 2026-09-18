package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.careme.backend.PostgresIntegrationTest;
import com.careme.backend.entity.ClinicalEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "careme.events.directory=target/test-events-inspection")
class ClinicalEventInspectionIntegrationTest extends PostgresIntegrationTest {

    private static final Path EVENTS_DIRECTORY = Path.of("target/test-events-inspection");

    @Autowired
    private ClinicalEventInspectionService inspectionService;

    @Autowired
    private ClinicalEventMarkdownStore markdownStore;

    @Autowired
    private ClinicalEventIndexWriter indexWriter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanInspectionData() throws IOException {
        jdbcTemplate.update("DELETE FROM clinical_event_index");
        if (Files.exists(EVENTS_DIRECTORY)) {
            try (var paths = Files.walk(EVENTS_DIRECTORY)) {
                paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException ignored) {
                    }
                });
            }
        }
    }

    @Test
    void readFailureLeavesMarkdownAndDerivedIndexUntouched() throws IOException {
        ClinicalEvent event = event("evt_001", LocalDate.of(2026, 9, 10),
                LocalDate.of(2026, 9, 11), "exact", "hace una semana");
        markdownStore.write(event);
        indexWriter.write(event);
        Files.writeString(EVENTS_DIRECTORY.resolve("evt_999.md"), "invalid markdown", StandardCharsets.UTF_8);

        String documentsBefore = documentsSnapshot();
        String indexBefore = indexSnapshot();

        assertThatThrownBy(() -> inspectionService.findAll(
                null, null, null, ClinicalEventInspectionService.SortBy.RECORD_DATE))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(documentsSnapshot()).isEqualTo(documentsBefore);
        assertThat(indexSnapshot()).isEqualTo(indexBefore);
    }

    @Test
    void listsFiltersAndReadsPersistedEventsWithoutChangingTheirStorage() throws IOException {
        ClinicalEvent older = event("evt_001", LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 3), "exact", "el 1 de septiembre");
        ClinicalEvent newer = event("evt_002", LocalDate.of(2026, 9, 12),
                LocalDate.of(2026, 9, 4), "exact", "hace cinco días");
        ClinicalEvent undated = event("evt_003", null,
                LocalDate.of(2026, 9, 5), "unknown", null);
        for (ClinicalEvent event : List.of(older, newer, undated)) {
            markdownStore.write(event);
            indexWriter.write(event);
        }
        String documentsBefore = documentsSnapshot();
        String indexBefore = indexSnapshot();

        assertThat(inspectionService.findAll(null, null, null, null))
                .extracting("code").containsExactly("evt_003", "evt_002", "evt_001");
        assertThat(inspectionService.findAll(null, null, null,
                ClinicalEventInspectionService.SortBy.OCCURRENCE_DATE))
                .extracting("code").containsExactly("evt_002", "evt_001", "evt_003");
        assertThat(inspectionService.findAll(ClinicalEvent.ClinicalEventType.DIAGNOSIS,
                LocalDate.of(2026, 9, 10), null, null))
                .extracting("code").containsExactly("evt_002");
        assertThat(inspectionService.findByCode("evt_002")).get()
                .satisfies(response -> {
                    assertThat(response.content()).isEqualTo("Evento de prueba");
                    assertThat(response.occurrenceDatePrecision()).isEqualTo("approximate");
                    assertThat(response.recordDate()).isEqualTo(LocalDate.of(2026, 9, 4));
                });
        assertThat(inspectionService.findAll(null, LocalDate.of(2030, 1, 1), null, null)).isEmpty();

        assertThat(documentsSnapshot()).isEqualTo(documentsBefore);
        assertThat(indexSnapshot()).isEqualTo(indexBefore);
    }

    private String indexSnapshot() {
        return jdbcTemplate.query(
                "SELECT code, type, event_date, date_precision, date_text, content, source, created_at "
                        + "FROM clinical_event_index ORDER BY code",
                (resultSet, rowNumber) -> resultSet.getString(1) + "|" + resultSet.getString(2) + "|"
                        + resultSet.getObject(3) + "|" + resultSet.getString(4) + "|"
                        + resultSet.getString(5) + "|" + resultSet.getString(6) + "|"
                        + resultSet.getString(7) + "|" + resultSet.getObject(8))
                .toString();
    }

    private String documentsSnapshot() throws IOException {
        if (!Files.exists(EVENTS_DIRECTORY)) {
            return "";
        }
        try (var paths = Files.list(EVENTS_DIRECTORY)) {
            return paths.filter(path -> path.getFileName().toString().endsWith(".md"))
                    .sorted()
                    .map(this::readDocument)
                    .collect(Collectors.joining("\n"));
        }
    }

    private String readDocument(Path path) {
        try {
            return path.getFileName() + ":" + Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new java.io.UncheckedIOException(exception);
        }
    }

    private static ClinicalEvent event(String code, LocalDate occurrenceDate, LocalDate recordDate,
            String precision, String dateText) {
        return new ClinicalEvent(
                UUID.randomUUID(), code, ClinicalEvent.ClinicalEventType.DIAGNOSIS,
                occurrenceDate, ClinicalEvent.DatePrecision.valueOf(precision.toUpperCase()), dateText,
                "Evento de prueba", ClinicalEvent.EventSource.PATIENT,
                recordDate.atStartOfDay().atOffset(ZoneOffset.UTC));
    }
}