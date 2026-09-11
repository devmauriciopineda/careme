package com.careme.backend.repository;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.careme.backend.entity.Measurement;
import com.careme.backend.entity.MeasurementEntity;

/**
 * Database-backed implementation of {@link MeasurementRepository}.
 *
 * <p>Its only job is translating rows to the domain type: it does not sort,
 * filter or validate. The read is declared read-only so the connection is never
 * enlisted for a write.
 */
@Repository
public class MeasurementJpaRepository implements MeasurementRepository {

    private final MeasurementJpaDao measurementJpaDao;

    public MeasurementJpaRepository(MeasurementJpaDao measurementJpaDao) {
        this.measurementJpaDao = measurementJpaDao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Measurement> findAll() {
        return measurementJpaDao.findAll().stream()
                .map(MeasurementEntity::toDomain)
                .toList();
    }
}
