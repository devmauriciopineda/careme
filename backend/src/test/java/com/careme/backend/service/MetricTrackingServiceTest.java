package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.careme.backend.entity.MeasurementUnit;
import com.careme.backend.entity.MeasurementValue;
import com.careme.backend.entity.Metric;
import com.careme.backend.entity.MetricMeasurement;
import com.careme.backend.repository.AdmittedMetricRepository;
import com.careme.backend.repository.MetricMeasurementRepository;

class MetricTrackingServiceTest {

    private final AdmittedMetricRepository admittedRepository = mock(AdmittedMetricRepository.class);
    private final MetricMeasurementRepository measurementRepository = mock(MetricMeasurementRepository.class);
    private final MetricCatalogService catalogService = new MetricCatalogService(admittedRepository);
    private final MetricTrackingService trackingService = new MetricTrackingService(catalogService, measurementRepository);

    @Test
    void exposesAdmittedMetricsWithOrderedCompositeComponents() {
        when(admittedRepository.findAll()).thenReturn(Set.of(Metric.WEIGHT, Metric.BLOOD_PRESSURE));

        assertThat(trackingService.catalog()).extracting("code")
                .containsExactly("blood_pressure", "weight");
        assertThat(trackingService.catalog().getFirst().components()).extracting("key")
                .containsExactly("systolic", "diastolic");
    }

    @Test
    void mapsOneMetricAndSortsItsMeasurementsByDate() {
        when(admittedRepository.admits(Metric.BLOOD_PRESSURE)).thenReturn(true);
        MetricMeasurement newest = pressure("2026-09-10", "125", "82");
        MetricMeasurement oldest = pressure("2026-09-06", "120", "80");
        when(measurementRepository.findByMetric(Metric.BLOOD_PRESSURE)).thenReturn(List.of(newest, oldest));

        var result = trackingService.findByCode("blood_pressure");

        assertThat(result.unit()).isEqualTo("mmHg");
        assertThat(result.measurements()).extracting("date")
                .containsExactly(LocalDate.of(2026, 9, 6), LocalDate.of(2026, 9, 10));
        assertThat(result.measurements().getFirst().values()).extracting("component")
                .containsExactly("systolic", "diastolic");
    }

    @Test
    void rejectsAnUnknownOrUnadmittedMetric() {
        when(admittedRepository.admits(Metric.CHOLESTEROL)).thenReturn(false);

        assertThatThrownBy(() -> trackingService.findByCode("cholesterol"))
                .isInstanceOf(com.careme.backend.exception.MetricNotFoundException.class);
        assertThatThrownBy(() -> trackingService.findByCode("unknown"))
                .isInstanceOf(com.careme.backend.exception.MetricNotFoundException.class);
    }

    private static MetricMeasurement pressure(String date, String systolic, String diastolic) {
        return new MetricMeasurement(
                UUID.randomUUID(),
                Metric.BLOOD_PRESSURE,
                MeasurementUnit.MMHG,
                LocalDate.parse(date),
                null,
                OffsetDateTime.now(),
                List.of(
                        new MeasurementValue("systolic", new BigDecimal(systolic)),
                        new MeasurementValue("diastolic", new BigDecimal(diastolic))));
    }
}