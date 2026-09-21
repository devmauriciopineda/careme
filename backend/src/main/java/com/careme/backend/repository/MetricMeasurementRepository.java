package com.careme.backend.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.careme.backend.entity.Metric;
import com.careme.backend.entity.MetricMeasurement;
import com.careme.backend.entity.MetricMeasurementDraft;

/**
 * Data access contract for the metric measurement model.
 *
 * <p>The only implementation reads from and writes to the database; the services
 * depend on this interface, not on JPA.
 */
public interface MetricMeasurementRepository {

    /**
     * @return every stored measurement, in no particular order
     */
    List<MetricMeasurement> findAll();

    /**
     * @return every stored measurement of the given metrics, in no particular order
     */
    List<MetricMeasurement> findByMetrics(Collection<Metric> metrics);

    /**
     * @return the stored measurements of the given metrics whose day falls inside
     *         the period, in no particular order; a {@code null} bound leaves that
     *         end of the period open
     */
    List<MetricMeasurement> findByMetricsAndDateBetween(
            Collection<Metric> metrics, LocalDate from, LocalDate to);

    /**
     * @return the measurement of that metric on that day, if any
     */
    Optional<MetricMeasurement> findByMetricAndDate(Metric metric, LocalDate date);

    /**
     * @return the days among the given ones that already have a measurement of the metric
     */
    Set<LocalDate> findExistingDates(Metric metric, Collection<LocalDate> dates);

    /**
     * Stores the given measurements: a metric and day that already had one keeps
     * its identity and gets its values replaced, and one that had none gets a new
     * row. One metric and day never ends up with two measurements.
     *
     * <p>The whole batch is read with one query and flushed once, and a failure
     * rolls it back as a unit, so the tracking is never left half written.
     *
     * @return one outcome per draft, in the order of the drafts
     */
    List<MetricMeasurementOutcome> upsertAll(List<MetricMeasurementDraft> drafts);
}
