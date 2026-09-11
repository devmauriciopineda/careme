package com.careme.backend.repository;

import java.util.List;

import com.careme.backend.entity.Measurement;

/**
 * Data access contract for measurements.
 *
 * <p>The only implementation reads a JSON file today; replacing it with a
 * Spring Data JPA repository later leaves the service untouched.
 */
public interface MeasurementRepository {

    /**
     * @return every stored measurement, in no particular order
     */
    List<Measurement> findAll();
}
