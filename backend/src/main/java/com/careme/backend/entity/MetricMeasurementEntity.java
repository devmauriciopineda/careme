package com.careme.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * JPA mapping of the metric model of the {@code measurements} table.
 *
 * <p>Deliberately separate from {@link MetricMeasurement}: the domain type stays
 * free of persistence annotations. The schema is owned by the Flyway migration,
 * so this mapping is validated against it at startup instead of generating it.
 */
@Entity
@Table(
        name = "measurements",
        uniqueConstraints = @UniqueConstraint(name = "uq_measurements_metric_date", columnNames = {"metric", "date"}))
public class MetricMeasurementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "metric", nullable = false, updatable = false)
    private String metric;

    @Column(name = "unit", nullable = false)
    private String unit;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "encounter_code")
    private String encounterCode;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @OneToMany(
            mappedBy = "measurement",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER)
    private List<MetricMeasurementValueEntity> values = new ArrayList<>();

    /** Required by JPA. Must not be used by application code. */
    protected MetricMeasurementEntity() {
    }

    /** A measurement that has no database identity yet. */
    public MetricMeasurementEntity(
            Metric metric, LocalDate date, String encounterCode, OffsetDateTime createdAt) {
        this.metric = metric.code();
        this.unit = metric.referenceUnit().code();
        this.date = date;
        this.encounterCode = encounterCode;
        this.createdAt = createdAt;
    }

    /**
     * Replaces the component values of a managed row, keeping its identity and metric.
     *
     * <p>A component the row already carries is updated in place: the schema keeps one
     * row per component, so replacing has to update rather than insert and delete.
     */
    public void replaceValues(List<MeasurementValue> replacement) {
        Map<String, BigDecimal> byComponent = new LinkedHashMap<>();
        replacement.forEach(value -> byComponent.put(value.component(), value.value()));

        values.removeIf(existing -> {
            BigDecimal value = byComponent.remove(existing.component());
            if (value == null) {
                return true;
            }
            existing.updateValue(value);
            return false;
        });

        byComponent.forEach((component, value) -> add(new MeasurementValue(component, value)));
    }

    private void add(MeasurementValue value) {
        values.add(new MetricMeasurementValueEntity(this, value.component(), value.value()));
    }

    /** @return the day this row belongs to, part of its identity within a metric */
    public LocalDate getDate() {
        return date;
    }

    /** @return the domain representation; the record's invariants reject a row that was never persisted */
    public MetricMeasurement toDomain() {
        return new MetricMeasurement(
                id,
                Metric.fromCode(metric).orElseThrow(() -> new IllegalStateException(
                        "Stored measurement carries the unknown metric " + metric)),
                MeasurementUnit.fromCode(unit),
                date,
                encounterCode,
                createdAt,
                values.stream().map(MetricMeasurementValueEntity::toDomain).toList());
    }
}
