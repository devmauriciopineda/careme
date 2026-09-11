package com.careme.backend.repository;

import java.util.List;

import com.careme.backend.entity.Measurement;

/**
 * Data access contract for measurements.
 *
 * <p>The only implementation reads from the database; the service depends on this
 * interface, not on JPA.
 */
public interface MeasurementRepository {

    /**
     * @return every stored measurement, in no particular order
     */
    List<Measurement> findAll();
}
