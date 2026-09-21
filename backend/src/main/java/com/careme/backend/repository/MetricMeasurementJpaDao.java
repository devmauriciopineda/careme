package com.careme.backend.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.careme.backend.entity.MetricMeasurementEntity;

/**
 * Spring Data access to the {@code measurements} table of the metric model.
 */
public interface MetricMeasurementJpaDao extends JpaRepository<MetricMeasurementEntity, UUID> {

    List<MetricMeasurementEntity> findByMetric(String metric);

    List<MetricMeasurementEntity> findByMetricIn(Collection<String> metrics);

    Optional<MetricMeasurementEntity> findByMetricAndDate(String metric, LocalDate date);

    List<MetricMeasurementEntity> findByMetricInAndDateBetween(
            Collection<String> metrics, LocalDate from, LocalDate to);

    List<MetricMeasurementEntity> findByMetricAndDateIn(String metric, Collection<LocalDate> dates);
}
