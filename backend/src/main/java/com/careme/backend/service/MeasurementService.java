package com.careme.backend.service;

import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.careme.backend.dto.MeasurementResponse;
import com.careme.backend.entity.Measurement;
import com.careme.backend.repository.MeasurementRepository;

/**
 * Business logic for body measurements.
 */
@Service
public class MeasurementService {

    private static final Logger log = LoggerFactory.getLogger(MeasurementService.class);

    private final MeasurementRepository measurementRepository;

    public MeasurementService(MeasurementRepository measurementRepository) {
        this.measurementRepository = measurementRepository;
    }

    /**
     * @return every measurement ordered from the oldest to the most recent date
     */
    public List<MeasurementResponse> findAll() {
        List<MeasurementResponse> measurements = measurementRepository.findAll().stream()
                .sorted(Comparator.comparing(Measurement::date))
                .map(MeasurementService::toResponse)
                .toList();

        log.debug("Returning {} measurements", measurements.size());
        return measurements;
    }

    private static MeasurementResponse toResponse(Measurement measurement) {
        return new MeasurementResponse(
                measurement.id(),
                measurement.date(),
                measurement.weightKg(),
                measurement.waistCm());
    }
}
