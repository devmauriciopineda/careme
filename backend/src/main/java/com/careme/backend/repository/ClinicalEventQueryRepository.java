package com.careme.backend.repository;

import com.careme.backend.entity.ClinicalEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * Read-only access to the derived clinical event index for history queries.
 *
 * <p>It mirrors {@code ClinicalEventIndexWriter} but never writes: it only
 * selects from {@code clinical_event_index}, so answering a question can never
 * change the clinical history.
 */
@Repository
public class ClinicalEventQueryRepository {

    private static final String COLUMNS =
            "id, code, type, event_date, date_precision, date_text, content, source, created_at, encounter_code";

    private static final RowMapper<ClinicalEvent> ROW_MAPPER = (resultSet, rowNumber) -> mapEvent(resultSet);

    private final JdbcTemplate jdbcTemplate;
    private final int maxResults;

    public ClinicalEventQueryRepository(
            JdbcTemplate jdbcTemplate,
            @Value("${careme.chat.query.max-results:10}") int maxResults) {
        this.jdbcTemplate = jdbcTemplate;
        this.maxResults = maxResults;
    }

    /**
     * Retrieves the events that can answer a query: first by text and metadata,
     * and only when that returns nothing and a metadata filter exists, by
     * metadata alone. Filtering by metadata is what lets a question about a type
     * or a date range still find its facts when no term matches.
     */
    public List<ClinicalEvent> search(
            List<String> searchTerms,
            ClinicalEvent.ClinicalEventType type,
            LocalDate fromDate,
            LocalDate toDate) {
        List<ClinicalEvent> byText = searchByText(searchTerms, type, fromDate, toDate);
        if (!byText.isEmpty()) {
            return byText;
        }
        if (type == null && fromDate == null && toDate == null) {
            return List.of();
        }
        return searchByMetadata(type, fromDate, toDate);
    }

    /**
     * Counts every event in the history index. It is not limited, so unlike
     * {@link #search} it can prove that the history holds nothing at all: zero
     * means the absence is the whole history and not a missing match.
     */
    public long countAll() {
        return count("SELECT COUNT(*) FROM clinical_event_index WHERE 1 = 1", List.of());
    }

    /**
     * Counts the events of one type. Zero, on a history that is not empty, means
     * the absence is scoped to that type.
     */
    public long countByType(ClinicalEvent.ClinicalEventType type) {
        if (type == null) {
            return countAll();
        }
        return count("SELECT COUNT(*) FROM clinical_event_index WHERE type = ?",
                List.of(type.name().toLowerCase()));
    }

    /**
     * Counts the events inside a date range. A null bound is open on that side, so
     * a question that states only one end still gets a bounded count. An event
     * whose date is unknown is not inside any period.
     */
    public long countByPeriod(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null && toDate == null) {
            return countAll();
        }
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM clinical_event_index WHERE 1 = 1");
        List<Object> parameters = new ArrayList<>();
        appendFilters(sql, parameters, null, fromDate, toDate);
        return count(sql.toString(), parameters);
    }

    private long count(String sql, List<Object> parameters) {
        Long total = jdbcTemplate.queryForObject(sql, Long.class, parameters.toArray());
        return total == null ? 0L : total;
    }

    List<ClinicalEvent> searchByText(
            List<String> searchTerms,
            ClinicalEvent.ClinicalEventType type,
            LocalDate fromDate,
            LocalDate toDate) {
        String tsQuery = toTsQuery(searchTerms);
        if (tsQuery.isEmpty()) {
            return List.of();
        }
        StringBuilder sql = new StringBuilder("SELECT ").append(COLUMNS)
                .append(" FROM clinical_event_index WHERE search_vector @@ to_tsquery('simple', ?)");
        List<Object> parameters = new ArrayList<>();
        parameters.add(tsQuery);
        appendFilters(sql, parameters, type, fromDate, toDate);
        sql.append(" ORDER BY ts_rank(search_vector, to_tsquery('simple', ?)) DESC,")
                .append(" event_date DESC NULLS LAST LIMIT ?");
        parameters.add(tsQuery);
        parameters.add(maxResults);
        return jdbcTemplate.query(sql.toString(), ROW_MAPPER, parameters.toArray());
    }

    List<ClinicalEvent> searchByMetadata(
            ClinicalEvent.ClinicalEventType type,
            LocalDate fromDate,
            LocalDate toDate) {
        StringBuilder sql = new StringBuilder("SELECT ").append(COLUMNS)
                .append(" FROM clinical_event_index WHERE 1 = 1");
        List<Object> parameters = new ArrayList<>();
        appendFilters(sql, parameters, type, fromDate, toDate);
        sql.append(" ORDER BY event_date DESC NULLS LAST LIMIT ?");
        parameters.add(maxResults);
        return jdbcTemplate.query(sql.toString(), ROW_MAPPER, parameters.toArray());
    }

    private static void appendFilters(
            StringBuilder sql,
            List<Object> parameters,
            ClinicalEvent.ClinicalEventType type,
            LocalDate fromDate,
            LocalDate toDate) {
        if (type != null) {
            sql.append(" AND type = ?");
            parameters.add(type.name().toLowerCase());
        }
        if (fromDate != null) {
            sql.append(" AND event_date >= ?");
            parameters.add(fromDate);
        }
        if (toDate != null) {
            sql.append(" AND event_date <= ?");
            parameters.add(toDate);
        }
    }

    /**
     * Turns the user's own words into a safe {@code tsquery}. The index uses the
     * {@code simple} configuration, so the terms are kept as written and only
     * joined: words inside a term are required together and terms are alternatives.
     */
    static String toTsQuery(List<String> searchTerms) {
        if (searchTerms == null) {
            return "";
        }
        return searchTerms.stream()
                .filter(term -> term != null && !term.isBlank())
                .map(term -> term.trim().toLowerCase().replaceAll("[^\\p{L}\\p{N}]+", " & ").trim())
                .filter(term -> !term.isEmpty())
                .collect(Collectors.joining(" | "));
    }

    private static ClinicalEvent mapEvent(ResultSet resultSet) throws SQLException {
        return new ClinicalEvent(
                resultSet.getObject("id", UUID.class),
                resultSet.getString("code"),
                ClinicalEvent.ClinicalEventType.valueOf(resultSet.getString("type").toUpperCase()),
                resultSet.getObject("event_date", LocalDate.class),
                ClinicalEvent.DatePrecision.valueOf(resultSet.getString("date_precision").toUpperCase()),
                resultSet.getString("date_text"),
                resultSet.getString("content"),
                ClinicalEvent.EventSource.valueOf(resultSet.getString("source").toUpperCase()),
                resultSet.getObject("created_at", OffsetDateTime.class),
                resultSet.getString("encounter_code"));
    }
}
