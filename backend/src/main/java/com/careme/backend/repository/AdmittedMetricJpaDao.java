package com.careme.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.careme.backend.entity.AdmittedMetricEntity;

/**
 * Spring Data access to the {@code admitted_metrics} table.
 */
public interface AdmittedMetricJpaDao extends JpaRepository<AdmittedMetricEntity, String> {
}
