package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.careme.backend.PostgresIntegrationTest;
import com.careme.backend.dto.ChatMessageResponse;
import com.careme.backend.dto.ClinicalEventIntent;
import com.careme.backend.entity.ClinicalEvent;
import com.careme.backend.entity.Encounter;
import com.careme.backend.entity.EncounterMeasurementNote;
import com.careme.backend.entity.Metric;
import com.careme.backend.entity.MetricMeasurementDraft;
import com.careme.backend.repository.MetricMeasurementRepository;

/**
 * End to end against real PostgreSQL and a real consultations directory: closing a
 * consultation writes the measurements it collected into the tracking, with the
 * consultation as their provenance, and a batch that cannot be stored leaves neither a
 * half-written measurement nor a closed consultation.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "careme.events.directory=target/test-events-measurements",
        "careme.encounters.directory=target/test-encounters-measurements"
})
class MeasurementCloseIntegrationTest extends PostgresIntegrationTest {

    private static final Path EVENTS_DIRECTORY = Path.of("target/test-events-measurements");
    private static final Path ENCOUNTERS_DIRECTORY = Path.of("target/test-encounters-measurements");
    private static final LocalDate DAY = LocalDate.of(2026, 9, 19);

    @Autowired
    private EncounterService encounterService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private MetricMeasurementRepository metricMeasurementRepository;

    @MockitoSpyBean
    private ClinicalEventMarkdownStore clinicalEventStore;

    @BeforeEach
    void cleanDirectories() throws IOException {
        deleteRecursively(EVENTS_DIRECTORY);
        deleteRecursively(ENCOUNTERS_DIRECTORY);
        // The derived index holds the codes the consultations used, so it goes with them.
        jdbcTemplate.execute("DELETE FROM encounter_index");
    }

    private static void deleteRecursively(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return;
        }
        try (var paths = Files.walk(directory)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    private static EncounterMeasurementNote weight(String value, LocalDate date) {
        return new EncounterMeasurementNote(
                "weight", Map.of("value", new BigDecimal(value)), null, date,
                ClinicalEvent.DatePrecision.EXACT, null);
    }

    @Test
    void registersTheMeasurementWithTheConsultationAsItsProvenance() {
        Encounter encounter = encounterService.current("conversation-close-1");
        encounterService.collectMeasurement("conversation-close-1", weight("70", DAY));

        ChatMessageResponse response = encounterService.close("conversation-close-1");

        assertThat(response.status()).isEqualTo(ChatMessageResponse.Status.REGISTERED);
        assertThat(response.message()).contains("una medición");
        assertThat(metricMeasurementRepository.findByMetricAndDate(Metric.WEIGHT, DAY))
                .hasValueSatisfying(stored -> {
                    assertThat(stored.encounterCode()).isEqualTo(encounter.code());
                    assertThat(stored.value("value").orElseThrow()).isEqualByComparingTo("70");
                });
    }

    @Test
    void replacesTheValueOfAMetricAndDayThatAlreadyHadOne() {
        metricMeasurementRepository.upsertAll(List.of(new MetricMeasurementDraft(
                Metric.WEIGHT, DAY, null, Map.of("value", new BigDecimal("81.1")))));

        encounterService.current("conversation-close-2");
        encounterService.collectMeasurement("conversation-close-2", weight("70", DAY));

        ChatMessageResponse response = encounterService.close("conversation-close-2");

        assertThat(metricMeasurementRepository.findByMetrics(List.of(Metric.WEIGHT))).hasSize(1);
        assertThat(metricMeasurementRepository.findByMetricAndDate(Metric.WEIGHT, DAY))
                .hasValueSatisfying(stored -> assertThat(stored.value("value").orElseThrow())
                        .isEqualByComparingTo("70"));
        assertThat(response.message()).contains("actualizado");
    }

    @Test
    void doesNotRegisterAMeasurementWhoseDateIsNotExact() {
        encounterService.current("conversation-close-3");
        encounterService.collectMeasurement("conversation-close-3", new EncounterMeasurementNote(
                "weight", Map.of("value", new BigDecimal("70")), null, null,
                ClinicalEvent.DatePrecision.APPROXIMATE, "el mes pasado"));

        ChatMessageResponse response = encounterService.close("conversation-close-3");

        assertThat(metricMeasurementRepository.findAll()).isEmpty();
        assertThat(response.status()).isEqualTo(ChatMessageResponse.Status.NOTHING_TO_REGISTER);
    }

    @Test
    void leavesTheTrackingAsItWasAndTheConsultationOpenWhenTheBatchCannotBeStored() throws IOException {
        encounterService.current("conversation-close-4");
        encounterService.collectMeasurement("conversation-close-4", weight("70", DAY));
        doThrow(new IllegalStateException("database is unreachable"))
                .when(metricMeasurementRepository).upsertAll(any());

        ChatMessageResponse response = encounterService.close("conversation-close-4");

        assertThat(response.status()).isEqualTo(ChatMessageResponse.Status.FAILED);
        assertThat(metricMeasurementRepository.findAll()).isEmpty();
        assertThat(new EncounterMarkdownStore(ENCOUNTERS_DIRECTORY)
                .findOpenByConversation("conversation-close-4"))
                .as("la consulta sigue abierta para poder reintentar el cierre")
                .isPresent();
    }

    @Test
    void registersAMeasurementMentionedTwiceOnlyOnce() {
        encounterService.current("conversation-close-5");
        encounterService.collectMeasurement("conversation-close-5", weight("70", DAY));
        EncounterMeasurementResult repeated =
                encounterService.collectMeasurement("conversation-close-5", weight("70", DAY));

        assertThat(repeated.kind()).isEqualTo(EncounterMeasurementResult.Kind.DUPLICATE);

        encounterService.close("conversation-close-5");

        assertThat(metricMeasurementRepository.findByMetrics(List.of(Metric.WEIGHT))).hasSize(1);
    }

    @Test
    void confirmsTheFactsAndTheMeasurementsInOneMessage() {
        encounterService.current("conversation-close-6");
        encounterService.collectMeasurement("conversation-close-6", weight("70", DAY));
        encounterService.collect("conversation-close-6", fact(), DAY);

        ChatMessageResponse response = encounterService.close("conversation-close-6");

        assertThat(response.status()).isEqualTo(ChatMessageResponse.Status.REGISTERED);
        assertThat(response.message()).contains("un hecho").contains("una medición");
    }

    @Test
    void doesNotExposeTheProvenanceInTheConfirmation() {
        Encounter encounter = encounterService.current("conversation-close-7");
        encounterService.collectMeasurement("conversation-close-7", weight("70", DAY));

        ChatMessageResponse response = encounterService.close("conversation-close-7");

        assertThat(response.message())
                .as("la confirmación no expone la procedencia ni sus identificadores")
                .doesNotContain(encounter.code())
                .doesNotContain(encounter.id().toString());
    }

    @Test
    void reportsWhatWasRegisteredWhenTheFactsCannotBeCompleted() throws IOException {
        encounterService.current("conversation-close-8");
        encounterService.collectMeasurement("conversation-close-8", weight("70", DAY));
        encounterService.collect("conversation-close-8", fact(), DAY);
        doThrow(new IOException("disk full")).when(clinicalEventStore).writeAtomically(anyList());

        ChatMessageResponse response = encounterService.close("conversation-close-8");

        assertThat(response.status()).isEqualTo(ChatMessageResponse.Status.FAILED);
        assertThat(response.message())
                .as("informa de lo registrado y de lo que no pudo completarse")
                .contains("mediciones")
                .contains("hechos");
        assertThat(metricMeasurementRepository.findByMetricAndDate(Metric.WEIGHT, DAY))
                .as("un registro completado no se revierte porque otro falle")
                .isPresent();
    }

    private static ClinicalEventIntent.Candidate fact() {
        return new ClinicalEventIntent.Candidate(
                ClinicalEvent.ClinicalEventType.NOTE,
                "me duele la espalda",
                null,
                ClinicalEvent.DatePrecision.UNKNOWN,
                null);
    }
}
