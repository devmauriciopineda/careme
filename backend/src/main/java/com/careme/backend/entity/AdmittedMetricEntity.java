package com.careme.backend.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA mapping of the metrics the person has admitted.
 *
 * <p>The catalogue in code knows how to describe every metric; this table holds
 * which of them the tracking actually registers. A metric the person confirms
 * starts to be recorded by adding it here.
 */
@Entity
@Table(name = "admitted_metrics")
public class AdmittedMetricEntity {

    @Id
    @Column(name = "metric", nullable = false, updatable = false)
    private String metric;

    @Column(name = "admitted_at", nullable = false)
    private OffsetDateTime admittedAt;

    /** Required by JPA. Must not be used by application code. */
    protected AdmittedMetricEntity() {
    }

    public AdmittedMetricEntity(Metric metric, OffsetDateTime admittedAt) {
        this.metric = metric.code();
        this.admittedAt = admittedAt;
    }

    /** @return the admitted metric, or empty when the row carries an unknown code */
    public java.util.Optional<Metric> toDomain() {
        return Metric.fromCode(metric);
    }
}
