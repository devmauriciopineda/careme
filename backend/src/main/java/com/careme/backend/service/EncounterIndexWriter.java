package com.careme.backend.service;

import com.careme.backend.entity.Encounter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Writes the derived index of consultations.
 *
 * <p>Unlike an event, a consultation is republished as it collects notes, so the
 * write is an upsert: the index follows the document instead of refusing a code
 * that is already indexed.
 */
@Service
public class EncounterIndexWriter {

    private final JdbcTemplate jdbcTemplate;

    public EncounterIndexWriter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void write(Encounter encounter) {
        jdbcTemplate.update(
                "INSERT INTO encounter_index "
                        + "(id, code, status, motive, summary, created_at, closed_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?) "
                        + "ON CONFLICT (id) DO UPDATE SET code = EXCLUDED.code, status = EXCLUDED.status, "
                        + "motive = EXCLUDED.motive, summary = EXCLUDED.summary, "
                        + "created_at = EXCLUDED.created_at, closed_at = EXCLUDED.closed_at",
                encounter.id(),
                encounter.code(),
                encounter.status().name().toLowerCase(),
                encounter.motive(),
                encounter.summary(),
                encounter.createdAt(),
                encounter.closedAt());
    }

    public void delete(Encounter encounter) {
        jdbcTemplate.update("DELETE FROM encounter_index WHERE id = ?", encounter.id());
    }
}
