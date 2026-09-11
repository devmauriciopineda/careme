package com.careme.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import com.careme.backend.PostgresIntegrationTest;
import com.careme.backend.entity.Measurement;
import com.careme.backend.entity.MeasurementEntity;

/**
 * Verifies that the adapter maps rows to the domain type and that the schema
 * actually enforces the invariants the domain declares.
 */
@SpringBootTest
@Transactional
class MeasurementJpaRepositoryTest extends PostgresIntegrationTest {

    @Autowired
    private MeasurementJpaRepository measurementJpaRepository;

    private static MeasurementEntity measurement(String date, String weightKg, String waistCm) {
        return new MeasurementEntity(
                LocalDate.parse(date), new BigDecimal(weightKg), new BigDecimal(waistCm));
    }

    @Test
    void returnsAnEmptyListWhenTheTableIsEmpty() {
        assertThat(measurementJpaRepository.findAll()).isEmpty();
    }

    @Test
    void mapsEveryColumnToTheDomainRecord() {
        measurementJpaDao.saveAndFlush(measurement("2026-09-06", "81.1", "96.0"));

        Measurement measurement = measurementJpaRepository.findAll().getFirst();

        assertThat(measurement.id()).isNotNull();
        assertThat(measurement.date()).isEqualTo(LocalDate.of(2026, 9, 6));
        assertThat(measurement.weightKg()).isEqualByComparingTo("81.1");
        assertThat(measurement.waistCm()).isEqualByComparingTo("96.0");
    }

    @Test
    void returnsEveryRowItFinds() {
        measurementJpaDao.saveAll(List.of(
                measurement("2026-09-06", "81.1", "96.0"),
                measurement("2026-09-07", "80.8", "95.4")));

        assertThat(measurementJpaRepository.findAll()).hasSize(2);
    }

    @Test
    void rejectsASecondMeasurementOnTheSameDate() {
        measurementJpaDao.saveAndFlush(measurement("2026-09-06", "81.1", "96.0"));

        assertThatThrownBy(() -> measurementJpaDao.saveAndFlush(
                measurement("2026-09-06", "80.8", "95.4")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsANonPositiveMetric() {
        assertThatThrownBy(() -> measurementJpaDao.saveAndFlush(
                measurement("2026-09-06", "0.0", "96.0")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findsTheMeasurementRecordedOnAGivenDay() {
        measurementJpaDao.saveAndFlush(measurement("2026-09-06", "81.1", "96.0"));

        Measurement found = measurementJpaRepository.findByDate(LocalDate.of(2026, 9, 6))
                .orElseThrow();

        assertThat(found.weightKg()).isEqualByComparingTo("81.1");
    }

    @Test
    void returnsEmptyWhenNoMeasurementIsRecordedOnTheDay() {
        assertThat(measurementJpaRepository.findByDate(LocalDate.of(2026, 9, 6))).isEmpty();
    }

    @Test
    void createsAMeasurementWhenTheDayHasNone() {
        Measurement created = measurementJpaRepository.create(
                LocalDate.of(2026, 9, 6), new BigDecimal("81.1"), new BigDecimal("96.0"));

        assertThat(created.id()).isNotNull();
        assertThat(measurementJpaRepository.findAll()).hasSize(1);
    }

    @Test
    void replacesTheValuesOfTheSameDayInsteadOfAddingARow() {
        Measurement created = measurementJpaRepository.create(
                LocalDate.of(2026, 9, 6), new BigDecimal("81.1"), new BigDecimal("96.0"));

        measurementJpaRepository.update(created.id(), new BigDecimal("79.9"), new BigDecimal("93.4"));

        List<Measurement> all = measurementJpaRepository.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.getFirst().id()).isEqualTo(created.id());
        assertThat(all.getFirst().weightKg()).isEqualByComparingTo("79.9");
        assertThat(all.getFirst().waistCm()).isEqualByComparingTo("93.4");
    }
}
