package com.careme.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.DefaultResourceLoader;

import com.careme.backend.entity.Measurement;
import com.careme.backend.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;

class MeasurementFileRepositoryTest {

    private static final String DATA_FILE_LOCATION = "classpath:data/measurements.json";

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private MeasurementFileRepository repositoryFor(String dataLocation) {
        return new MeasurementFileRepository(
                new DefaultResourceLoader(), objectMapper, dataLocation);
    }

    @Test
    void loadsEveryMeasurementFromTheDataFile() {
        List<Measurement> measurements = repositoryFor(DATA_FILE_LOCATION).findAll();

        assertThat(measurements).isNotEmpty();
        assertThat(measurements).allSatisfy(measurement -> {
            assertThat(measurement.id()).isNotBlank();
            assertThat(measurement.date()).isNotNull();
            assertThat(measurement.weightKg()).isPositive();
            assertThat(measurement.waistCm()).isPositive();
        });
    }

    @Test
    void failsWhenTheDataFileIsMissing() {
        MeasurementFileRepository repository = repositoryFor("classpath:data/nope.json");

        assertThatThrownBy(repository::findAll)
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("data/nope.json");
    }

    @Test
    void failsWhenTheDataFileIsNotReadableJson(@TempDir Path tempDir) throws IOException {
        Path dataFile = tempDir.resolve("broken.json");
        Files.writeString(dataFile, "{ not json");

        MeasurementFileRepository repository = repositoryFor(dataFile.toUri().toString());

        assertThatThrownBy(repository::findAll)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("broken.json");
    }

    @Test
    void servesTheCachedMeasurementsAfterTheDataFileDisappears(@TempDir Path tempDir)
            throws IOException {
        Path dataFile = tempDir.resolve("measurements.json");
        Files.writeString(dataFile, """
                [
                  {"id":"measurement-2026-09-06","date":"2026-09-06","weightKg":81.1,"waistCm":96.0}
                ]
                """);

        MeasurementFileRepository repository = repositoryFor(dataFile.toUri().toString());
        List<Measurement> firstRead = repository.findAll();

        Files.delete(dataFile);

        assertThat(repository.findAll()).isSameAs(firstRead);
        assertThat(firstRead).hasSize(1);
    }
}
