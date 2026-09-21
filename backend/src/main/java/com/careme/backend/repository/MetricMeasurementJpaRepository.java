package com.careme.backend.repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.careme.backend.entity.Metric;
import com.careme.backend.entity.MetricMeasurement;
import com.careme.backend.entity.MetricMeasurementDraft;
import com.careme.backend.entity.MetricMeasurementEntity;
import com.careme.backend.entity.MeasurementValue;

/**
 * Database-backed implementation of {@link MetricMeasurementRepository}.
 *
 * <p>Its only job is translating rows to the domain type: it does not sort, filter
 * or validate. Reads are declared read-only so the connection is never enlisted for
 * a write; writes flush so the caller observes the stored rows.
 */
@Repository
public class MetricMeasurementJpaRepository implements MetricMeasurementRepository {

    private final MetricMeasurementJpaDao metricMeasurementJpaDao;

    public MetricMeasurementJpaRepository(MetricMeasurementJpaDao metricMeasurementJpaDao) {
        this.metricMeasurementJpaDao = metricMeasurementJpaDao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MetricMeasurement> findAll() {
        return metricMeasurementJpaDao.findAll().stream()
                .map(MetricMeasurementEntity::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MetricMeasurement> findByMetrics(Collection<Metric> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            return List.of();
        }
        return metricMeasurementJpaDao
                .findByMetricIn(metrics.stream().map(Metric::code).toList()).stream()
                .map(MetricMeasurementEntity::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MetricMeasurement> findByMetricsAndDateBetween(
            Collection<Metric> metrics, LocalDate from, LocalDate to) {
        if (metrics == null || metrics.isEmpty()) {
            return List.of();
        }
        return metricMeasurementJpaDao
                .findByMetricInAndDateBetween(metricCodes(metrics), lowerBound(from), upperBound(to)).stream()
                .map(MetricMeasurementEntity::toDomain)
                .toList();
    }

    private static List<String> metricCodes(Collection<Metric> metrics) {
        return metrics.stream().map(Metric::code).toList();
    }

    /** An open lower bound is the earliest day the model can hold. */
    private static LocalDate lowerBound(LocalDate from) {
        return from == null ? LocalDate.of(1, 1, 1) : from;
    }

    /** An open upper bound is the latest day the model can hold. */
    private static LocalDate upperBound(LocalDate to) {
        return to == null ? LocalDate.of(9999, 12, 31) : to;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MetricMeasurement> findByMetricAndDate(Metric metric, LocalDate date) {
        return metricMeasurementJpaDao.findByMetricAndDate(metric.code(), date)
                .map(MetricMeasurementEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<LocalDate> findExistingDates(Metric metric, Collection<LocalDate> dates) {
        if (dates == null || dates.isEmpty()) {
            return Set.of();
        }
        return metricMeasurementJpaDao.findByMetricAndDateIn(metric.code(), dates).stream()
                .map(MetricMeasurementEntity::getDate)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public List<MetricMeasurementOutcome> upsertAll(List<MetricMeasurementDraft> drafts) {
        if (drafts == null || drafts.isEmpty()) {
            return List.of();
        }

        Map<String, MetricMeasurementEntity> existing = loadExisting(drafts);
        Map<String, MetricMeasurementEntity> pending = new HashMap<>();
        Map<MetricMeasurementEntity, Boolean> replacedBy = new LinkedHashMap<>();
        List<MetricMeasurementEntity> ordered = new ArrayList<>();

        for (MetricMeasurementDraft draft : drafts) {
            String key = key(draft.metric(), draft.date());
            MetricMeasurementEntity entity = pending.get(key);
            if (entity == null) {
                entity = existing.get(key);
                boolean replaced = entity != null;
                if (entity == null) {
                    entity = new MetricMeasurementEntity(
                            draft.metric(), draft.date(), draft.encounterCode(), OffsetDateTime.now());
                }
                pending.put(key, entity);
                replacedBy.put(entity, replaced);
                ordered.add(entity);
            }
            entity.replaceValues(toValues(draft));
        }

        // One flush for the whole batch: the caller sees the stored rows and a
        // failure rolls the batch back as a unit.
        List<MetricMeasurementEntity> saved = metricMeasurementJpaDao.saveAllAndFlush(ordered);

        return saved.stream()
                .map(entity -> new MetricMeasurementOutcome(
                        entity.toDomain(), Boolean.TRUE.equals(replacedBy.get(entity))))
                .toList();
    }

    private Map<String, MetricMeasurementEntity> loadExisting(List<MetricMeasurementDraft> drafts) {
        Map<Metric, List<LocalDate>> datesByMetric = new HashMap<>();
        for (MetricMeasurementDraft draft : drafts) {
            datesByMetric.computeIfAbsent(draft.metric(), ignored -> new ArrayList<>()).add(draft.date());
        }

        Map<String, MetricMeasurementEntity> existing = new HashMap<>();
        datesByMetric.forEach((metric, dates) -> metricMeasurementJpaDao
                .findByMetricAndDateIn(metric.code(), dates)
                .forEach(entity -> existing.put(key(metric, entity.getDate()), entity)));
        return existing;
    }

    private static List<MeasurementValue> toValues(MetricMeasurementDraft draft) {
        return draft.values().entrySet().stream()
                .map(entry -> new MeasurementValue(entry.getKey(), entry.getValue()))
                .toList();
    }

    private static String key(Metric metric, LocalDate date) {
        return metric.code() + "|" + date;
    }
}
