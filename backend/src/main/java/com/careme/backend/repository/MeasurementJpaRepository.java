package com.careme.backend.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.careme.backend.entity.Measurement;
import com.careme.backend.entity.MeasurementEntity;

/**
 * Database-backed implementation of {@link MeasurementRepository}.
 *
 * <p>Its only job is translating rows to the domain type: it does not sort,
 * filter or validate. Reads are declared read-only so the connection is never
 * enlisted for a write; writes flush so the caller observes the stored row.
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

    @Override
    @Transactional(readOnly = true)
    public Optional<Measurement> findByDate(LocalDate date) {
        return measurementJpaDao.findByDate(date).map(MeasurementEntity::toDomain);
    }

    @Override
    @Transactional
    public Measurement create(LocalDate date, BigDecimal weightKg, BigDecimal waistCm) {
        MeasurementEntity saved = measurementJpaDao.saveAndFlush(
                new MeasurementEntity(date, weightKg, waistCm));
        return saved.toDomain();
    }

    @Override
    @Transactional
    public Measurement update(UUID id, BigDecimal weightKg, BigDecimal waistCm) {
        MeasurementEntity entity = measurementJpaDao.findById(id)
                .orElseThrow(() -> new IllegalStateException(
                        "Measurement " + id + " disappeared before it could be updated"));
        entity.updateValues(weightKg, waistCm);
        return measurementJpaDao.saveAndFlush(entity).toDomain();
    }
}
