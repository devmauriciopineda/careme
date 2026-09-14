package com.careme.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.careme.backend.PostgresIntegrationTest;
import com.careme.backend.entity.ClinicalEvent;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/** Verifies lexical and metadata retrieval against a real PostgreSQL index. */
@SpringBootTest
class ClinicalEventQueryRepositoryTest extends PostgresIntegrationTest {

    @Autowired
    private ClinicalEventQueryRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearIndex() {
        jdbcTemplate.update("DELETE FROM clinical_event_index");
    }

    @Test
    void retrievesByTextAndRank() {
        insert("evt_001", "diagnosis", "2026-01-10", "exact", "Hipertensión diagnosticada");
        insert("evt_002", "note", null, "unknown", "Dolor de cabeza");

        var found = repository.search(List.of("hipertensión"), null, null, null);

        assertThat(found).extracting(ClinicalEvent::code).containsExactly("evt_001");
    }

    @Test
    void fallsBackToMetadataWhenTheTextDoesNotMatch() {
        insert("evt_001", "diagnosis", "2026-01-10", "exact", "Hipertensión");
        insert("evt_002", "medication", "2026-02-10", "exact", "Enalapril");

        var found = repository.search(
                List.of("zanzibar"), ClinicalEvent.ClinicalEventType.MEDICATION,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 1));

        assertThat(found).extracting(ClinicalEvent::code).containsExactly("evt_002");
    }

    @Test
    void returnsNothingWhenNoTermMatchesAndThereIsNoFilter() {
        insert("evt_001", "note", null, "unknown", "Dolor de cabeza");

        assertThat(repository.search(List.of("zanzibar"), null, null, null)).isEmpty();
    }

    @Test
    void respectsTheConfiguredLimit() {
        for (int index = 1; index <= 12; index++) {
            insert(String.format("evt_%03d", index), "note", "2026-01-%02d".formatted(index), "exact",
                    "Control de rutina número " + index);
        }

        assertThat(repository.search(List.of("control"), null, null, null)).hasSize(10);
    }

    private void insert(String code, String type, String date, String precision, String content) {
        jdbcTemplate.update(
                "INSERT INTO clinical_event_index "
                        + "(id, code, type, event_date, date_precision, date_text, content, source, created_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                UUID.randomUUID(), code, type, date == null ? null : LocalDate.parse(date), precision,
                null, content, "patient", OffsetDateTime.now(ZoneOffset.UTC));
    }
}
