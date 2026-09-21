package com.careme.backend.repository;

import java.time.OffsetDateTime;
import java.util.Set;

import com.careme.backend.entity.Metric;

/**
 * Data access contract for the metrics the tracking admits.
 *
 * <p>The catalogue in code describes every metric; this contract holds which of
 * them are admitted, and admits one only when the person confirms it.
 */
public interface AdmittedMetricRepository {

    /**
     * @return the metrics the tracking admits today
     */
    Set<Metric> findAll();

    /**
     * @return whether the metric is admitted
     */
    boolean admits(Metric metric);

    /**
     * Admits a metric, keeping the moment it was admitted. Admitting a metric that
     * is already admitted changes nothing.
     *
     * @return whether the metric was not admitted before
     */
    boolean admit(Metric metric, OffsetDateTime admittedAt);
}
