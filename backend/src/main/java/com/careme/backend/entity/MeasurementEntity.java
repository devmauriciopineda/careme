package com.careme.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA mapping of the {@code measurements} table.
 *
 * <p>Deliberately separate from the {@link Measurement} record: the domain type
 * stays free of persistence annotations and the HTTP contract stays free of the
 * column layout. The schema itself is owned by the Flyway migration, so this
 * mapping is validated against it at startup instead of generating it.
 */
@Entity
@Table(name = "measurements")
public class MeasurementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "date", nullable = false, unique = true)
    private LocalDate date;

    @Column(name = "weight_kg", nullable = false, precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "waist_cm", nullable = false, precision = 5, scale = 2)
    private BigDecimal waistCm;

    /** Required by JPA. Must not be used by application code. */
    protected MeasurementEntity() {
    }

    /** A measurement that has no database identity yet. */
    public MeasurementEntity(LocalDate date, BigDecimal weightKg, BigDecimal waistCm) {
        this.date = date;
        this.weightKg = weightKg;
        this.waistCm = waistCm;
    }

    /**
     * Replaces both metric values on a managed row. The date is the row's
     * identity within a day and deliberately cannot change.
     */
    public void updateValues(BigDecimal weightKg, BigDecimal waistCm) {
        this.weightKg = weightKg;
        this.waistCm = waistCm;
    }

    /** @return the day this row belongs to, its identity within the table */
    public LocalDate getDate() {
        return date;
    }

    /**
     * @return the domain representation; the record's invariants reject a row that
     *         was never persisted
     */
    public Measurement toDomain() {
        return new Measurement(id, date, weightKg, waistCm);
    }
}
