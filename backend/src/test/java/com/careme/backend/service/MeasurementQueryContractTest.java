package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.careme.backend.dto.AgentOperation;
import com.careme.backend.dto.MeasurementQueryIntent;
import com.careme.backend.repository.AdmittedMetricRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Contract of a question about a measurement: it belongs to the tracking channel, so the
 * clinical history neither answers it with measurement values nor accepts the retired
 * type as a filter.
 *
 * <p>The tracking answers the question even when the assistant routes it to the clinical
 * history: whichever operation the assistant chose, a value of a measurement is never
 * presented as a clinical fact.
 */
class MeasurementQueryContractTest {

    private static final String CONVERSATION_ID = "conversation-1";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ClinicalHistoryQueryService historyQueryService = mock(ClinicalHistoryQueryService.class);
    private final MeasurementQueryService measurementQueryService = mock(MeasurementQueryService.class);
    private final AgentOperationExecutor executor = new AgentOperationExecutor(
            historyQueryService,
            mock(EncounterService.class),
            new ClinicalEventIntentValidator(),
            new MetricCatalogService(mock(AdmittedMetricRepository.class)),
            measurementQueryService);

    private AgentOperationResult consult(String arguments) throws Exception {
        return executor.execute(
                CONVERSATION_ID,
                new AgentOperationCall(AgentOperation.CONSULT_HISTORY, objectMapper.readTree(arguments)),
                null);
    }

    @Test
    void attendsAMeasurementQuestionByItsOwnChannel() throws Exception {
        when(measurementQueryService.answer(any()))
                .thenReturn(MeasurementAnswerResult.answered(List.of(), "Estas son tus mediciones registradas."));

        AgentOperationResult result = consult("""
                {"question":"¿cuál es mi peso?","search_terms":["peso"],"scope":"measurements"}
                """);

        assertThat(result.operation()).isEqualTo(AgentOperation.CONSULT_MEASUREMENTS);
        assertThat(result.kind()).isEqualTo(AgentOperationResult.Kind.COMPLETED);
        verifyNoInteractions(historyQueryService);
    }

    @Test
    void passesTheQuestionAndThePeriodToTheMeasurementChannel() throws Exception {
        when(measurementQueryService.answer(any()))
                .thenReturn(MeasurementAnswerResult.answered(List.of(), "Estas son tus mediciones registradas."));

        consult("""
                {"question":"¿cuál es mi peso?","search_terms":["peso"],"scope":"measurements",
                 "date_from":"2026-01-01","date_to":"2026-03-31"}
                """);

        ArgumentCaptor<MeasurementQueryIntent> intent = ArgumentCaptor.forClass(MeasurementQueryIntent.class);
        verify(measurementQueryService).answer(intent.capture());
        assertThat(intent.getValue().question()).isEqualTo("¿cuál es mi peso?");
        assertThat(intent.getValue().fromDate()).hasToString("2026-01-01");
        assertThat(intent.getValue().toDate()).hasToString("2026-03-31");
    }

    @Test
    void neverAnswersAMeasurementQuestionFromTheClinicalHistory() throws Exception {
        when(measurementQueryService.answer(any()))
                .thenReturn(MeasurementAnswerResult.answered(List.of(), "Estas son tus mediciones registradas."));

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
