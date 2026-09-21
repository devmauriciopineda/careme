package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.careme.backend.PostgresIntegrationTest;
import com.careme.backend.entity.Metric;
import com.careme.backend.entity.MetricMeasurement;
import com.careme.backend.entity.MetricMeasurementDraft;
import com.careme.backend.repository.MetricMeasurementRepository;

/**
 * Verifies the admitted set against a real database: the migration seeds the
 * initial metrics and admitting one is what the person confirms, without touching
 * the measurements already stored. Each test rolls its admissions back, so the
 * seeded set is the same for every one of them.
 */
@SpringBootTest
@Transactional
class MetricCatalogIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private MetricCatalogService metricCatalogService;

    @Autowired
    private MetricMeasurementRepository metricMeasurementRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void seedsTheInitialMetricsAsAdmitted() {
        assertThat(metricCatalogService.admittedMetrics())
                .contains(Metric.WEIGHT, Metric.WAIST, Metric.BLOOD_PRESSURE, Metric.CHOLESTEROL);
    }

    @Test
    void keepsAMetricThatWasNotConfirmedOutOfTheTracking() {
        assertThat(metricCatalogService.isAdmitted(Metric.CREATININE)).isFalse();
    }

    @Test
    void admitsAMetricThePersonConfirmedAndDoesNotRepeatTheAdmission() {
        assertThat(metricCatalogService.admit(Metric.CREATININE)).isTrue();
        assertThat(metricCatalogService.isAdmitted(Metric.CREATININE)).isTrue();
        assertThat(metricCatalogService.admit(Metric.CREATININE)).isFalse();
    }

    @Test
    void admitsAMetricWithoutAlteringTheMeasurementsAlreadyStored() {
        MetricMeasurementDraft draft = new MetricMeasurementDraft(
                Metric.WEIGHT, LocalDate.of(2026, 9, 6), null, Map.of("value", new BigDecimal("81.1")));
        metricMeasurementRepository.upsertAll(List.of(draft));

        List<MetricMeasurement> before = metricMeasurementRepository.findAll();

        metricCatalogService.admit(Metric.TRIGLYCERIDES);

        List<MetricMeasurement> after = metricMeasurementRepository.findAll();
        assertThat(after).hasSize(before.size());
        assertThat(after.getFirst().values()).isEqualTo(before.getFirst().values());
        assertThat(after.getFirst().metric()).isEqualTo(Metric.WEIGHT);
    }

    @Test
    void ignoresAStoredMetricTheCatalogueDoesNotKnow() {
        jdbcTemplate.update("INSERT INTO admitted_metrics (metric) VALUES ('haemoglobin')");

        assertThat(metricCatalogService.admittedMetrics()).contains(Metric.WEIGHT);
        assertThat(metricCatalogService.admittedMetrics())
                .allSatisfy(metric -> assertThat(metric).isInstanceOf(Metric.class));
    }
}
