package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.careme.backend.entity.MeasurementUnit;
import com.careme.backend.entity.Metric;
import com.careme.backend.repository.AdmittedMetricRepository;

@ExtendWith(MockitoExtension.class)
class MetricCatalogServiceTest {

    @Mock
    private AdmittedMetricRepository admittedMetricRepository;

    @InjectMocks
    private MetricCatalogService metricCatalogService;

    @Test
    void reportsTheMetricsTheTrackingAdmits() {
        when(admittedMetricRepository.findAll()).thenReturn(Set.of(Metric.WEIGHT, Metric.WAIST));

        assertThat(metricCatalogService.admittedMetrics()).containsExactlyInAnyOrder(Metric.WEIGHT, Metric.WAIST);
    }

    @Test
    void reportsWhetherAMetricIsAdmitted() {
        when(admittedMetricRepository.admits(Metric.CHOLESTEROL)).thenReturn(true);

        assertThat(metricCatalogService.isAdmitted(Metric.CHOLESTEROL)).isTrue();
        assertThat(metricCatalogService.isAdmitted(null)).isFalse();
    }

    @Test
    void admitsAMetricWhenThePersonConfirmsIt() {
        when(admittedMetricRepository.admit(any(), any())).thenReturn(true);

        assertThat(metricCatalogService.admit(Metric.CREATININE)).isTrue();
    }

    @Test
    void reportsNothingAdmittedWhenThePersonDoesNotConfirm() {
        assertThat(metricCatalogService.admit(null)).isFalse();
    }

    @Test
    void resolvesTheUnitThePersonStatedWhenItBelongsToTheMetric() {
        UnitResolution resolution = metricCatalogService.resolveUnit(Metric.WEIGHT, MeasurementUnit.LB);

        assertThat(resolution.requiresClarification()).isFalse();
        assertThat(resolution.unit()).isEqualTo(MeasurementUnit.LB);
    }

    @Test
    void resolvesTheBareUnitWithoutAskingWhenTheMetricDeclaresOne() {
        UnitResolution resolution = metricCatalogService.resolveUnit(Metric.WEIGHT, null);

        assertThat(resolution.requiresClarification()).isFalse();
        assertThat(resolution.unit()).isEqualTo(MeasurementUnit.KG);
    }

    @Test
    void asksForClarificationWhenTheUnitCannotBeDeduced() {
        UnitResolution resolution = metricCatalogService.resolveUnit(Metric.CHOLESTEROL, null);

        assertThat(resolution.requiresClarification()).isTrue();
        assertThat(resolution.unit()).isNull();
    }

    @Test
    void asksForClarificationWhenTheStatedUnitDoesNotBelongToTheMetric() {
        UnitResolution resolution = metricCatalogService.resolveUnit(Metric.WEIGHT, MeasurementUnit.MMOL_L);

        assertThat(resolution.requiresClarification()).isTrue();
        assertThat(resolution.unit()).isNull();
    }

    @Test
    void convertsToTheReferenceUnitOfTheMetric() {
        assertThat(metricCatalogService.toReferenceUnit(Metric.WEIGHT, new BigDecimal("100"), MeasurementUnit.LB))
                .isEqualByComparingTo("45.3592");
    }

    @Test
    void resolvesNothingWhenTheMetricIsUnknown() {
        UnitResolution resolution = metricCatalogService.resolveUnit(null, MeasurementUnit.KG);

        assertThat(resolution.requiresClarification()).isTrue();
        assertThat(resolution.unit()).isNull();
    }

    @Test
    void admitsAValueOnlyWhenEveryComponentIsInsideItsRange() {
        assertThat(metricCatalogService.admits(Metric.WEIGHT, Map.of("value", new BigDecimal("70")))).isTrue();

        assertThat(metricCatalogService.admits(Metric.WEIGHT, Map.of("value", new BigDecimal("501")))).isFalse();
        assertThat(metricCatalogService.admits(Metric.WEIGHT, Map.of())).isFalse();
        assertThat(metricCatalogService.admits(Metric.WEIGHT, null)).isFalse();
        assertThat(metricCatalogService.admits(null, Map.of("value", new BigDecimal("70")))).isFalse();
        assertThat(metricCatalogService.admits(
                Metric.BLOOD_PRESSURE, Map.of("systolic", new BigDecimal("145")))).isFalse();
        assertThat(metricCatalogService.admits(Metric.BLOOD_PRESSURE, Map.of(
                "systolic", new BigDecimal("145"),
                "diastolic", new BigDecimal("92")))).isTrue();
    }

    @Test
    void knowsTheMetricsOfTheCatalogueByTheirCode() {
        assertThat(metricCatalogService.metricByCode("creatinine")).contains(Metric.CREATININE);
        assertThat(metricCatalogService.metricByCode("haemoglobin")).isEmpty();
    }
}
