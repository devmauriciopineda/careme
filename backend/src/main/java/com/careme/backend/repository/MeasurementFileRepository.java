package com.careme.backend.repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Repository;

import com.careme.backend.entity.Measurement;
import com.careme.backend.exception.ResourceNotFoundException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Reads measurements from a JSON file and caches them in memory for the
 * lifetime of the application.
 *
 * <p>The file is read-only: this implementation has no write operations, so the
 * cache can never go stale. The file is loaded on first access instead of at
 * startup so a missing or broken file surfaces as a request error rather than
 * preventing the application from booting.
 */
@Repository
public class MeasurementFileRepository implements MeasurementRepository {

    private static final Logger log = LoggerFactory.getLogger(MeasurementFileRepository.class);

    private static final TypeReference<List<Measurement>> MEASUREMENT_LIST =
            new TypeReference<>() {
            };

    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    private final String dataLocation;

    private volatile List<Measurement> cachedMeasurements;

    public MeasurementFileRepository(
            ResourceLoader resourceLoader,
            ObjectMapper objectMapper,
            @Value("${careme.measurements.data-location}") String dataLocation) {
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
        this.dataLocation = dataLocation;
    }

    @Override
    public List<Measurement> findAll() {
        List<Measurement> measurements = cachedMeasurements;
        if (measurements == null) {
            measurements = loadFromDataFile();
            cachedMeasurements = measurements;
        }
        return measurements;
    }

    private List<Measurement> loadFromDataFile() {
        Resource resource = resourceLoader.getResource(dataLocation);
        if (!resource.exists()) {
            throw new ResourceNotFoundException("Measurements data file not found: " + dataLocation);
        }

        try (InputStream input = resource.getInputStream()) {
            List<Measurement> measurements = objectMapper.readValue(input, MEASUREMENT_LIST);
            log.info("Loaded {} measurements from {}", measurements.size(), dataLocation);
            return List.copyOf(measurements);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read measurements from " + dataLocation, ex);
        }
    }
}
