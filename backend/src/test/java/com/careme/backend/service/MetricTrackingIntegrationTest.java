package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.careme.backend.PostgresIntegrationTest;
import com.careme.backend.entity.Metric;
import com.careme.backend.repository.MetricMeasurementRepository;

@SpringBootTest
class MetricTrackingIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private MetricMeasurementRepository repository;

    @Autowired
    private MetricTrackingService service;

    @BeforeEach
    void storeCompositeMeasurements() {
        repository.upsertAll(List.of(
                new com.careme.backend.entity.MetricMeasurementDraft(
                        Metric.BLOOD_PRESSURE,
                        LocalDate.of(2026, 9, 10),
                        null,
                        values(new BigDecimal("125"), new BigDecimal("82"))),
                new com.careme.backend.entity.MetricMeasurementDraft(
                        Metric.BLOOD_PRESSURE,
                        LocalDate.of(2026, 9, 6),
                        null,
                        values(new BigDecimal("120"), new BigDecimal("80")))));
    }

    /** Diastolic is written first on purpose: the read must re-order by component. */
    private static Map<String, BigDecimal> values(BigDecimal systolic, BigDecimal diastolic) {
        Map<String, BigDecimal> values = new LinkedHashMap<>();
        values.put("diastolic", diastolic);
        values.put("systolic", systolic);
        return values;
    }

    @Test
    void readsOneBloodPressureMetricWithBothComponentsInDateOrder() {
        var result = service.findByCode("blood_pressure");

        assertThat(result.code()).isEqualTo("blood_pressure");
        assertThat(result.unit()).isEqualTo("mmHg");
        assertThat(result.measurements()).hasSize(2);
        assertThat(result.measurements()).extracting("date")
                .containsExactly(LocalDate.of(2026, 9, 6), LocalDate.of(2026, 9, 10));
        assertThat(result.measurements().getFirst().values()).extracting("component")
                .containsExactly("systolic", "diastolic");
        var values = result.measurements().getFirst().values();
        assertThat(values.get(0).value()).isEqualByComparingTo("120");
        assertThat(values.get(1).value()).isEqualByComparingTo("80");
    }
}