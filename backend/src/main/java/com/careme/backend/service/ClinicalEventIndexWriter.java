package com.careme.backend.service;

import com.careme.backend.entity.ClinicalEvent;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ClinicalEventIndexWriter {

    private final JdbcTemplate jdbcTemplate;

    public ClinicalEventIndexWriter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void writeAll(List<ClinicalEvent> events) {
        events.forEach(this::write);
    }

    public void write(ClinicalEvent event) {
        jdbcTemplate.update(
                "INSERT INTO clinical_event_index "
                        + "(id, code, type, event_date, date_precision, date_text, content, source, created_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                event.id(), event.code(), event.type().name().toLowerCase(), event.date(),
                event.datePrecision().name().toLowerCase(), event.dateText(), event.content(),
                event.source().name().toLowerCase(), event.createdAt());
    }

    public void deleteAll(List<ClinicalEvent> events) {
        events.forEach(event -> jdbcTemplate.update(
                "DELETE FROM clinical_event_index WHERE id = ?", event.id()));
    }
}
