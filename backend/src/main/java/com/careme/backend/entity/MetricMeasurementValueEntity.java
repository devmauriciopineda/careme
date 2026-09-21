package com.careme.backend.entity;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * JPA mapping of one component value of a measurement.
 */
@Entity
@Table(name = "measurement_values")
public class MetricMeasurementValueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "measurement_id", nullable = false, updatable = false)
    private MetricMeasurementEntity measurement;

    @Column(name = "component", nullable = false, updatable = false)
    private String component;

    @Column(name = "value", nullable = false, precision = 12, scale = 4)
    private BigDecimal value;

    /** Required by JPA. Must not be used by application code. */
    protected MetricMeasurementValueEntity() {
    }

    public MetricMeasurementValueEntity(
            MetricMeasurementEntity measurement, String component, BigDecimal value) {
        this.measurement = measurement;
        this.component = component;
        this.value = value;
    }

    public MeasurementValue toDomain() {
        return new MeasurementValue(component, value);
    }

    public String component() {
        return component;
    }

    /** Sets the value of a component the row already carries. */
    public void updateValue(BigDecimal value) {
        this.value = value;
    }
}
