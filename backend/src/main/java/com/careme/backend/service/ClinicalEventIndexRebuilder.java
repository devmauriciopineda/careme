package com.careme.backend.service;

import com.careme.backend.entity.ClinicalEvent;
import java.io.IOException;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/** Rebuilds the derived PostgreSQL index from Markdown source documents. */
@Service
public class ClinicalEventIndexRebuilder {

    private final ClinicalEventMarkdownStore markdownStore;
    private final JdbcTemplate jdbcTemplate;

    public ClinicalEventIndexRebuilder(ClinicalEventMarkdownStore markdownStore, JdbcTemplate jdbcTemplate) {
        this.markdownStore = markdownStore;
        this.jdbcTemplate = jdbcTemplate;
    }

    public int rebuild() throws IOException {
        List<ClinicalEvent> events = markdownStore.readAll();
        jdbcTemplate.update("DELETE FROM clinical_event_index");
        events.forEach(this::insert);
        return events.size();
    }

    private void insert(ClinicalEvent event) {
        jdbcTemplate.update(
                "INSERT INTO clinical_event_index "
                        + "(id, code, type, event_date, date_precision, date_text, content, source, created_at, "
                        + "encounter_code) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                event.id(),
                event.code(),
                event.type().name().toLowerCase(),
                event.date(),
                event.datePrecision().name().toLowerCase(),
                event.dateText(),
                event.content(),
                event.source().name().toLowerCase(),
                event.createdAt(),
                event.encounterCode());
    }
}