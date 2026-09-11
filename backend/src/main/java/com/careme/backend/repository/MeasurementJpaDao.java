package com.careme.backend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.careme.backend.entity.MeasurementEntity;

/**
 * Spring Data access to the {@code measurements} table.
 *
 * <p>Persistence detail: it speaks in {@link MeasurementEntity}, not in domain
 * types. {@link MeasurementJpaRepository} is what exposes the domain contract.
 */
public interface MeasurementJpaDao extends JpaRepository<MeasurementEntity, UUID> {
}
