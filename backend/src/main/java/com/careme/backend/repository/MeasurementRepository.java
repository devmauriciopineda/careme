package com.careme.backend.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.careme.backend.entity.Measurement;

/**
 * Data access contract for measurements.
 *
 * <p>The only implementation reads from and writes to the database; the service
 * depends on this interface, not on JPA.
 */
public interface MeasurementRepository {

    /**
     * @return every stored measurement, in no particular order
     */
    List<Measurement> findAll();

    /**
     * @return the measurement recorded on the given day, if any
     */
    Optional<Measurement> findByDate(LocalDate date);

    /**
     * Stores a new measurement for a day that had none.
     */
    Measurement create(LocalDate date, BigDecimal weightKg, BigDecimal waistCm);

    /**
     * Replaces the metric values of an existing measurement, keeping its identity.
     */
    Measurement update(UUID id, BigDecimal weightKg, BigDecimal waistCm);
}
