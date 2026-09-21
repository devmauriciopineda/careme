package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;

import com.careme.backend.dto.AgentOperation;
import com.careme.backend.repository.AdmittedMetricRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Contract of a question about a measurement: it belongs to the tracking channel, so the
 * clinical history neither answers it with measurement values nor accepts the retired
 * type as a filter.
 *
 * <p>The tracking channel answers with a redirect today, because reading the tracking in
 * the conversation is `UC-014b`. That channel is already in place and is what `UC-014b`
 * fills in; what this contract fixes is that the clinical history never presents a
 * measurement value as one of its facts.
 */
class MeasurementQueryContractTest {

    private static final String CONVERSATION_ID = "conversation-1";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ClinicalHistoryQueryService historyQueryService = mock(ClinicalHistoryQueryService.class);
    private final AgentOperationExecutor executor = new AgentOperationExecutor(
            historyQueryService,
            mock(EncounterService.class),
            new ClinicalEventIntentValidator(),
            new MetricCatalogService(mock(AdmittedMetricRepository.class)));

    private AgentOperationResult consult(String arguments) throws Exception {
        return executor.execute(
                CONVERSATION_ID,
                new AgentOperationCall(AgentOperation.CONSULT_HISTORY, objectMapper.readTree(arguments)),
                null);
    }

    @Test
    void attendsAMeasurementQuestionByItsOwnChannel() throws Exception {
        AgentOperationResult result = consult("""
                {"question":"¿cuál es mi peso?","search_terms":["peso"],"scope":"measurements"}
                """);

        assertThat(result.kind()).isEqualTo(AgentOperationResult.Kind.REJECTED);
        assertThat(result.rejection()).isEqualTo(AgentOperationResult.Rejection.ELSEWHERE);
        assertThat(result.message()).contains("su propio espacio");
    }

    @Test
    void neverAnswersAMeasurementQuestionFromTheClinicalHistory() throws Exception {
        consult("""
                {"question":"¿cuál es mi peso?","search_terms":["peso"],"scope":"measurements"}
                """);

        verifyNoInteractions(historyQueryService);
    }

    @Test
    void refusesToQueryMeasurementsAsClinicalFacts() throws Exception {
        AgentOperationResult result = consult("""
                {"question":"¿qué mediciones tengo?","search_terms":["mediciones"],"type":"measurement"}
                """);

        assertThat(result.kind()).isEqualTo(AgentOperationResult.Kind.REJECTED);
        assertThat(result.rejection()).isEqualTo(AgentOperationResult.Rejection.NOT_ADMISSIBLE);
        verifyNoInteractions(historyQueryService);
    }
}
