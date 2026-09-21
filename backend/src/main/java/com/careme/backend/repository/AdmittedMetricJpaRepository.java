package com.careme.backend.repository;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.careme.backend.entity.AdmittedMetricEntity;
import com.careme.backend.entity.Metric;

/**
 * Database-backed implementation of {@link AdmittedMetricRepository}.
 */
@Repository
public class AdmittedMetricJpaRepository implements AdmittedMetricRepository {

    private final AdmittedMetricJpaDao admittedMetricJpaDao;

    public AdmittedMetricJpaRepository(AdmittedMetricJpaDao admittedMetricJpaDao) {
        this.admittedMetricJpaDao = admittedMetricJpaDao;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Metric> findAll() {
        return admittedMetricJpaDao.findAll().stream()
                .map(AdmittedMetricEntity::toDomain)
                .flatMap(java.util.Optional::stream)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean admits(Metric metric) {
        return metric != null && admittedMetricJpaDao.existsById(metric.code());
    }

    @Override
    @Transactional
    public boolean admit(Metric metric, OffsetDateTime admittedAt) {
        if (admits(metric)) {
            return false;
        }
        admittedMetricJpaDao.saveAndFlush(new AdmittedMetricEntity(metric, admittedAt));
        return true;
    }
}
