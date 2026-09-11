package com.careme.backend.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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

    Optional<MeasurementEntity> findByDate(LocalDate date);

    /**
     * Reads several days at once, so a bulk write costs one query instead of one
     * per day.
     */
    List<MeasurementEntity> findByDateIn(Collection<LocalDate> dates);
}
