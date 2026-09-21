package com.careme.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Deterministic composer for the fake provider mode.
 *
 * <p>It frames the measurements the application already formatted and declines an
 * interpretation when one was asked for, so the end-to-end cycle works offline
 * without inventing anything.
 */
@Service
@ConditionalOnProperty(name = "careme.llm.mode", havingValue = "fake")
public class FakeMeasurementAnswerComposer implements MeasurementAnswerComposer {

    @Override
    public String compose(String question, List<MeasurementFact> facts, boolean interpretationRequested) {
        if (facts == null || facts.isEmpty()) {
            throw new LlmIntegrationException("There is nothing to answer with");
        }
        String body = facts.stream().map(MeasurementFact::text).collect(Collectors.joining("; "));
        StringBuilder answer = new StringBuilder("Estas son tus mediciones registradas: ")
                .append(body)
                .append('.');
        if (interpretationRequested) {
            answer.append(' ').append(DECLINED);
        }
        return answer.toString();
    }
}
