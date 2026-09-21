package com.careme.backend.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.careme.backend.entity.Measurement;
import com.careme.backend.entity.MeasurementDraft;
import com.careme.backend.entity.Metric;
import com.careme.backend.entity.MetricMeasurement;
import com.careme.backend.entity.MetricMeasurementDraft;

/**
 * The legacy body-tracking contract —one measurement per day, weight and abdominal
 * circumference together— as a derived view over the metric model.
 *
 * <p>It exists so `UC-002`, `UC-003` and the `/measurements` screen keep working
 * unchanged while there is a single tracking store. A day is part of the view only
 * when it carries both metrics: the legacy contract requires both values, so a day
 * with a single metric stays in the tracking and is not shown here.
 *
 * <p>The component key of a simple metric such as weight or circumference is
 * {@code value}.
 */
@Repository
public class MetricBackedMeasurementRepository implements MeasurementRepository {

    private static final String VALUE = "value";

    private final MetricMeasurementRepository metricMeasurementRepository;

    public MetricBackedMeasurementRepository(MetricMeasurementRepository metricMeasurementRepository) {
        this.metricMeasurementRepository = metricMeasurementRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Measurement> findAll() {
        return compose(metricMeasurementRepository.findByMetrics(List.of(Metric.WEIGHT, Metric.WAIST)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Measurement> findByDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Measurement date must not be null");
        }
        List<MetricMeasurement> found = new ArrayList<>(2);
        metricMeasurementRepository.findByMetricAndDate(Metric.WEIGHT, date).ifPresent(found::add);
        metricMeasurementRepository.findByMetricAndDate(Metric.WAIST, date).ifPresent(found::add);
        return compose(found).stream().findFirst();
    }

    @Override
    @Transactional
    public Measurement upsert(LocalDate date, BigDecimal weightKg, BigDecimal waistCm) {
        List<MetricMeasurementOutcome> outcomes = metricMeasurementRepository.upsertAll(
                List.of(draft(Metric.WEIGHT, date, weightKg), draft(Metric.WAIST, date, waistCm)));

        MetricMeasurement stored = outcomes.stream()
                .map(MetricMeasurementOutcome::measurement)
                .filter(measurement -> measurement.metric() == Metric.WEIGHT)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Weight measurement was not stored"));

        return toLegacy(stored, waistCm, date);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<LocalDate> findExistingDates(Collection<LocalDate> dates) {
        if (dates == null || dates.isEmpty()) {
            return Set.of();
        }
        Set<LocalDate> existing = new HashSet<>(
                metricMeasurementRepository.findExistingDates(Metric.WEIGHT, dates));
        existing.addAll(metricMeasurementRepository.findExistingDates(Metric.WAIST, dates));
        return existing;
    }

    @Override
    @Transactional
    public List<Measurement> upsertAll(List<MeasurementDraft> drafts) {
        if (drafts == null || drafts.isEmpty()) {
            return List.of();
        }

        List<MetricMeasurementDraft> metricDrafts = new ArrayList<>(drafts.size() * 2);
        for (MeasurementDraft draft : drafts) {
            metricDrafts.add(draft(Metric.WEIGHT, draft.date(), draft.weightKg()));
            metricDrafts.add(draft(Metric.WAIST, draft.date(), draft.waistCm()));
        }

        Map<LocalDate, MetricMeasurement> storedWeight = new HashMap<>();
        metricMeasurementRepository.upsertAll(metricDrafts).stream()
                .map(MetricMeasurementOutcome::measurement)
                .filter(measurement -> measurement.metric() == Metric.WEIGHT)
                .forEach(measurement -> storedWeight.put(measurement.date(), measurement));

        return drafts.stream()
                .map(draft -> toLegacy(storedWeight.get(draft.date()), draft.waistCm(), draft.date()))
                .toList();
    }

    private static List<Measurement> compose(List<MetricMeasurement> measurements) {
        Map<LocalDate, MetricMeasurement> weight = new HashMap<>();
        Map<LocalDate, MetricMeasurement> waist = new HashMap<>();
        for (MetricMeasurement measurement : measurements) {
            if (measurement == null) {
                continue;
            }
            if (measurement.metric() == Metric.WEIGHT) {
                weight.put(measurement.date(), measurement);
            } else if (measurement.metric() == Metric.WAIST) {
                waist.put(measurement.date(), measurement);
            }
        }

        return weight.keySet().stream()
                .filter(waist::containsKey)
                .map(date -> toLegacy(weight.get(date), waist.get(date).value(VALUE).orElse(null), date))
                .toList();
    }

    private static Measurement toLegacy(
            MetricMeasurement stored, BigDecimal waistCm, LocalDate date) {
        if (stored == null) {
            throw new IllegalStateException("Weight measurement was not stored");
        }
        BigDecimal weightKg = stored.value(VALUE)
                .orElseThrow(() -> new IllegalStateException("Weight measurement carries no value"));
        return new Measurement(stored.id(), date, weightKg, waistCm);
    }

    private static MetricMeasurementDraft draft(Metric metric, LocalDate date, BigDecimal value) {
        return new MetricMeasurementDraft(metric, date, null, Map.of(VALUE, value));
    }
}
