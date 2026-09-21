package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.careme.backend.dto.AgentOperation;
import com.careme.backend.dto.ClinicalEventIntent;
import com.careme.backend.entity.ClinicalEvent;
import com.careme.backend.repository.AdmittedMetricRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Verifies that a measurement is no longer an admissible type of clinical fact: a fact
 * that names it is rejected before anything is written, and the provider is never
 * offered it.
 */
class RetiredFactTypeTest {

    private static final String CONVERSATION_ID = "conversation-1";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ClinicalEventIntentValidator validator = new ClinicalEventIntentValidator();
    private final EncounterService encounterService = mock(EncounterService.class);
    private final AgentOperationExecutor executor = new AgentOperationExecutor(
            mock(ClinicalHistoryQueryService.class),
            encounterService,
            validator,
            new MetricCatalogService(mock(AdmittedMetricRepository.class)),
            mock(MeasurementQueryService.class));

    private static ClinicalEventIntent measurementFact() {
        return new ClinicalEventIntent(
                ClinicalEventIntent.Kind.EVENTS,
                List.of(new ClinicalEventIntent.Candidate(
                        ClinicalEvent.ClinicalEventType.MEASUREMENT,
                        "Presion 145/92",
                        null,
                        ClinicalEvent.DatePrecision.UNKNOWN,
                        null)),
                null);
    }

    @Test
    void keepsMeasurementOutOfTheAdmissibleFactTypes() {
        assertThat(ClinicalEvent.ClinicalEventType.admissible())
                .containsExactlyInAnyOrder(
                        ClinicalEvent.ClinicalEventType.DIAGNOSIS,
                        ClinicalEvent.ClinicalEventType.MEDICATION,
                        ClinicalEvent.ClinicalEventType.NOTE);
        assertThat(ClinicalEvent.ClinicalEventType.MEASUREMENT.isAdmissible()).isFalse();
    }

    @Test
    void rejectsAFactThatNamesTheRetiredType() {
        assertThatThrownBy(() -> validator.validate(measurementFact()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsTheRetiredTypeWithoutCollectingAnything() throws Exception {
        AgentOperationResult result = executor.execute(
                CONVERSATION_ID,
                new AgentOperationCall(
                        AgentOperation.RECORD_NOTE,
                        objectMapper.readTree("""
                                {"type":"measurement","content":"Presion 145/92","date_precision":"unknown"}
                                """)),
                null);

        assertThat(result.kind()).isEqualTo(AgentOperationResult.Kind.REJECTED);
        verifyNoInteractions(encounterService);
    }

    @Test
    void keepsReadingADocumentThatCarriesTheRetiredType() {
        // A document already written with the retired type must still be readable, so
        // the catalogue keeps the constant even though nothing can register it.
        assertThat(ClinicalEvent.ClinicalEventType.valueOf("MEASUREMENT"))
                .isEqualTo(ClinicalEvent.ClinicalEventType.MEASUREMENT);
    }
}
