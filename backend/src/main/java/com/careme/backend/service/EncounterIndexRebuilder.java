package com.careme.backend.service;

import com.careme.backend.entity.Encounter;
import java.io.IOException;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/** Rebuilds the derived index of consultations from the Markdown source documents. */
@Service
public class EncounterIndexRebuilder {

    private final EncounterMarkdownStore markdownStore;
    private final JdbcTemplate jdbcTemplate;

    public EncounterIndexRebuilder(EncounterMarkdownStore markdownStore, JdbcTemplate jdbcTemplate) {
        this.markdownStore = markdownStore;
        this.jdbcTemplate = jdbcTemplate;
    }

    public int rebuild() throws IOException {
        List<Encounter> encounters = markdownStore.readAll();
        jdbcTemplate.update("DELETE FROM encounter_index");
        encounters.forEach(this::insert);
        return encounters.size();
    }

    private void insert(Encounter encounter) {
        jdbcTemplate.update(
                "INSERT INTO encounter_index "
                        + "(id, code, status, motive, summary, created_at, closed_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                encounter.id(),
                encounter.code(),
                encounter.status().name().toLowerCase(),
                encounter.motive(),
                encounter.summary(),
                encounter.createdAt(),
                encounter.closedAt());
    }
}
