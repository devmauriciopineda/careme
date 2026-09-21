package com.careme.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.transaction.annotation.Transactional;

import com.careme.backend.PostgresIntegrationTest;
import com.careme.backend.entity.Measurement;
import com.careme.backend.entity.MeasurementDraft;
import com.careme.backend.entity.Metric;
import com.careme.backend.entity.MetricMeasurementDraft;

/**
 * Verifies the legacy daily view over the metric model: which days it composes,
 * how it stores a day, and that it never loses or duplicates a measurement.
 */
@SpringBootTest
@Transactional
class MetricBackedMeasurementRepositoryTest extends PostgresIntegrationTest {

    @Autowired
    private MeasurementRepository measurementRepository;

    @Autowired
    private MetricMeasurementRepository metricMeasurementRepository;

    private static MeasurementDraft draft(String date, String weightKg, String waistCm) {
        return new MeasurementDraft(
                LocalDate.parse(date), new BigDecimal(weightKg), new BigDecimal(waistCm));
    }

    private void storeOnlyWeight(String date, String weightKg) {
        metricMeasurementRepository.upsertAll(List.of(new MetricMeasurementDraft(
                Metric.WEIGHT, LocalDate.parse(date), null, Map.of("value", new BigDecimal(weightKg)))));
    }

    @Test
    void returnsAnEmptyListWhenNothingIsStored() {
        assertThat(measurementRepository.findAll()).isEmpty();
    }

    @Test
    void composesTheDayFromBothMetrics() {
        measurementRepository.upsert(LocalDate.of(2026, 9, 6), new BigDecimal("81.1"), new BigDecimal("96.0"));

        Measurement measurement = measurementRepository.findAll().getFirst();

        assertThat(measurement.id()).isNotNull();
        assertThat(measurement.date()).isEqualTo(LocalDate.of(2026, 9, 6));
        assertThat(measurement.weightKg()).isEqualByComparingTo("81.1");
        assertThat(measurement.waistCm()).isEqualByComparingTo("96.0");
    }

    @Test
    void storesBothMetricsInTheMetricModel() {
        measurementRepository.upsert(LocalDate.of(2026, 9, 6), new BigDecimal("81.1"), new BigDecimal("96.0"));

        assertThat(metricMeasurementRepository.findByMetricAndDate(Metric.WEIGHT, LocalDate.of(2026, 9, 6)))
                .isPresent();
        assertThat(metricMeasurementRepository.findByMetricAndDate(Metric.WAIST, LocalDate.of(2026, 9, 6)))
                .isPresent();
    }

    @Test
    void omitsADayThatCarriesOnlyOneMetricWithoutLosingIt() {
        storeOnlyWeight("2026-09-06", "81.1");

        assertThat(measurementRepository.findAll()).isEmpty();
        assertThat(metricMeasurementRepository.findByMetricAndDate(Metric.WEIGHT, LocalDate.of(2026, 9, 6)))
                .isPresent();
    }

    @Test
    void returnsEveryDayItComposes() {
        measurementRepository.upsert(LocalDate.of(2026, 9, 6), new BigDecimal("81.1"), new BigDecimal("96.0"));
        measurementRepository.upsert(LocalDate.of(2026, 9, 7), new BigDecimal("80.8"), new BigDecimal("95.4"));

        assertThat(measurementRepository.findAll()).hasSize(2);
    }

    @Test
    void findsTheMeasurementRecordedOnAGivenDay() {
        measurementRepository.upsert(LocalDate.of(2026, 9, 6), new BigDecimal("81.1"), new BigDecimal("96.0"));

        Measurement found = measurementRepository.findByDate(LocalDate.of(2026, 9, 6)).orElseThrow();

        assertThat(found.weightKg()).isEqualByComparingTo("81.1");
    }

    @Test
    void returnsEmptyWhenNoMeasurementIsRecordedOnTheDay() {
        assertThat(measurementRepository.findByDate(LocalDate.of(2026, 9, 6))).isEmpty();
    }

    @Test
    void refusesToLookUpADayWithoutADate() {
        assertThatThrownBy(() -> measurementRepository.findByDate(null))
                .isInstanceOf(InvalidDataAccessApiUsageException.class)
                .hasMessageContaining("date must not be null");
    }

    @Test
    void leavesAMetricThatIsNotPartOfTheDailyViewOutOfIt() {
        metricMeasurementRepository.upsertAll(List.of(new MetricMeasurementDraft(
                Metric.CHOLESTEROL,
                LocalDate.of(2026, 9, 6),
                null,
                Map.of("value", new BigDecimal("200")))));

        assertThat(measurementRepository.findAll()).isEmpty();
        assertThat(metricMeasurementRepository
                .findByMetricAndDate(Metric.CHOLESTEROL, LocalDate.of(2026, 9, 6)))
                .isPresent();
    }

    @Test
    void replacesTheValuesOfTheSameDayInsteadOfAddingARow() {
        Measurement created = measurementRepository.upsert(
                LocalDate.of(2026, 9, 6), new BigDecimal("81.1"), new BigDecimal("96.0"));

        measurementRepository.upsert(
                LocalDate.of(2026, 9, 6), new BigDecimal("79.9"), new BigDecimal("93.4"));

        List<Measurement> all = measurementRepository.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.getFirst().id()).isEqualTo(created.id());
        assertThat(all.getFirst().weightKg()).isEqualByComparingTo("79.9");
        assertThat(all.getFirst().waistCm()).isEqualByComparingTo("93.4");
    }

    @Test
    void createsTheDaysThatHaveNoMeasurementAndReplacesTheOnesThatHave() {
        measurementRepository.upsert(LocalDate.of(2026, 9, 6), new BigDecimal("81.1"), new BigDecimal("96.0"));

        List<Measurement> stored = measurementRepository.upsertAll(List.of(
                draft("2026-09-06", "79.9", "93.4"),
                draft("2026-09-08", "80.4", "95.2")));

        assertThat(stored).hasSize(2);
        assertThat(measurementRepository.findAll()).hasSize(2);

        Measurement replaced = measurementRepository.findByDate(LocalDate.of(2026, 9, 6)).orElseThrow();
        assertThat(replaced.weightKg()).isEqualByComparingTo("79.9");
        assertThat(replaced.waistCm()).isEqualByComparingTo("93.4");
    }

    @Test
    void keepsTheIdentityOfTheDayItReplaces() {
        Measurement created = measurementRepository.upsert(
                LocalDate.of(2026, 9, 6), new BigDecimal("81.1"), new BigDecimal("96.0"));

        List<Measurement> stored = measurementRepository.upsertAll(
                List.of(draft("2026-09-06", "79.9", "93.4")));

        assertThat(stored.getFirst().id()).isEqualTo(created.id());
    }

    @Test
    void storesNothingWhenThereAreNoMeasurementsToStore() {
        assertThat(measurementRepository.upsertAll(List.of())).isEmpty();
    }

    @Test
    void keepsOneMeasurementWhenABatchNamesTheSameDayTwice() {
        measurementRepository.upsertAll(List.of(
                draft("2026-09-06", "81.1", "96.0"),
                draft("2026-09-06", "79.9", "93.4")));

        List<Measurement> all = measurementRepository.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.getFirst().weightKg()).isEqualByComparingTo("79.9");
    }

    @Test
    void reportsWhichOfTheGivenDaysAreAlreadyRecorded() {
        measurementRepository.upsert(LocalDate.of(2026, 9, 6), new BigDecimal("81.1"), new BigDecimal("96.0"));

        Set<LocalDate> existing = measurementRepository.findExistingDates(List.of(
                LocalDate.of(2026, 9, 6),
                LocalDate.of(2026, 9, 8)));

        assertThat(existing).containsExactly(LocalDate.of(2026, 9, 6));
    }

    @Test
    void reportsADayRecordedByASingleMetricToo() {
        storeOnlyWeight("2026-09-07", "80.8");

        Set<LocalDate> existing = measurementRepository.findExistingDates(List.of(
                LocalDate.of(2026, 9, 6),
                LocalDate.of(2026, 9, 7)));

        assertThat(existing).containsExactly(LocalDate.of(2026, 9, 7));
    }

    @Test
    void reportsNoDayWhenNoneIsAsked() {
        assertThat(measurementRepository.findExistingDates(List.of())).isEmpty();
        assertThat(measurementRepository.findExistingDates(null)).isEmpty();
    }

    @Test
    void asksTheMetricModelForNothingWhenNothingIsAsked() {
        assertThat(metricMeasurementRepository.findByMetrics(List.of())).isEmpty();
        assertThat(metricMeasurementRepository.findExistingDates(Metric.WEIGHT, List.of())).isEmpty();
        assertThat(metricMeasurementRepository.upsertAll(List.of())).isEmpty();
        assertThat(measurementRepository.upsertAll(null)).isEmpty();
    }

    @Test
    void rejectsANonPositiveValue() {
        assertThatThrownBy(() -> measurementRepository.upsert(
                LocalDate.of(2026, 9, 6), new BigDecimal("0.0"), new BigDecimal("96.0")))
                .isInstanceOf(InvalidDataAccessApiUsageException.class)
                .hasMessageContaining("must be positive");
    }
}
