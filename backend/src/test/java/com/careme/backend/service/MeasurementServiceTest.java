package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.careme.backend.dto.MeasurementRequest;
import com.careme.backend.dto.MeasurementResponse;
import com.careme.backend.entity.Measurement;
import com.careme.backend.repository.MeasurementRepository;

@ExtendWith(MockitoExtension.class)
class MeasurementServiceTest {

    private static final UUID ID = UUID.fromString("6f1d0f9a-1f5f-4a0e-9a4e-4f0f6a1c2b3d");

    @Mock
    private MeasurementRepository measurementRepository;

    @InjectMocks
    private MeasurementService measurementService;

    private static Measurement measurement(String date, String weightKg, String waistCm) {
        return new Measurement(
                ID,
                LocalDate.parse(date),
                new BigDecimal(weightKg),
                new BigDecimal(waistCm));
    }

    @Test
    void returnsTheMeasurementsOrderedByDateAscending() {
        when(measurementRepository.findAll()).thenReturn(List.of(
                measurement("2026-09-10", "80.1", "94.8"),
                measurement("2026-09-06", "81.1", "96.0"),
                measurement("2026-09-08", "80.4", "95.2")));

        List<MeasurementResponse> result = measurementService.findAll();

        assertThat(result).extracting(MeasurementResponse::date).containsExactly(
                LocalDate.of(2026, 9, 6),
                LocalDate.of(2026, 9, 8),
                LocalDate.of(2026, 9, 10));
    }

    @Test
    void mapsEveryFieldOfTheEntityToTheResponse() {
        when(measurementRepository.findAll()).thenReturn(List.of(
                measurement("2026-09-06", "81.1", "96.0")));

        MeasurementResponse response = measurementService.findAll().getFirst();

        assertThat(response.id()).isEqualTo(ID);
        assertThat(response.date()).isEqualTo(LocalDate.of(2026, 9, 6));
        assertThat(response.weightKg()).isEqualByComparingTo("81.1");
        assertThat(response.waistCm()).isEqualByComparingTo("96.0");
    }

    @Test
    void returnsAnEmptyListWhenNothingIsStored() {
        when(measurementRepository.findAll()).thenReturn(List.of());

        assertThat(measurementService.findAll()).isEmpty();
    }

    @Test
    void propagatesAFailureFromTheRepository() {
        when(measurementRepository.findAll())
                .thenThrow(new IllegalStateException("database is unreachable"));

        assertThatThrownBy(measurementService::findAll)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void createsTheMeasurementWhenTheDayHasNone() {
        MeasurementRequest request = request("2026-09-10", "80.1", "94.8");
        when(measurementRepository.findByDate(LocalDate.of(2026, 9, 10)))
                .thenReturn(Optional.empty());
        when(measurementRepository.create(
                LocalDate.of(2026, 9, 10), new BigDecimal("80.1"), new BigDecimal("94.8")))
                .thenReturn(measurement("2026-09-10", "80.1", "94.8"));

        MeasurementResponse response = measurementService.register(request);

        assertThat(response.date()).isEqualTo(LocalDate.of(2026, 9, 10));
        assertThat(response.weightKg()).isEqualByComparingTo("80.1");
        assertThat(response.waistCm()).isEqualByComparingTo("94.8");
        verify(measurementRepository, never()).update(any(), any(), any());
    }

    @Test
    void replacesTheValuesOfADayThatAlreadyHasAMeasurement() {
        MeasurementRequest request = request("2026-09-10", "79.5", "93.0");
        when(measurementRepository.findByDate(LocalDate.of(2026, 9, 10)))
                .thenReturn(Optional.of(measurement("2026-09-10", "80.1", "94.8")));
        when(measurementRepository.update(ID, new BigDecimal("79.5"), new BigDecimal("93.0")))
                .thenReturn(measurement("2026-09-10", "79.5", "93.0"));

        MeasurementResponse response = measurementService.register(request);

        assertThat(response.id()).isEqualTo(ID);
        assertThat(response.weightKg()).isEqualByComparingTo("79.5");
        assertThat(response.waistCm()).isEqualByComparingTo("93.0");
        verify(measurementRepository, never()).create(any(), any(), any());
    }

    @Test
    void propagatesAFailureWhileRegistering() {
        MeasurementRequest request = request("2026-09-10", "80.1", "94.8");
        when(measurementRepository.findByDate(LocalDate.of(2026, 9, 10)))
                .thenThrow(new IllegalStateException("database is unreachable"));

        assertThatThrownBy(() -> measurementService.register(request))
                .isInstanceOf(IllegalStateException.class);
    }

    private static MeasurementRequest request(String date, String weightKg, String waistCm) {
        return new MeasurementRequest(
                LocalDate.parse(date),
                new BigDecimal(weightKg),
                new BigDecimal(waistCm));
    }
}
