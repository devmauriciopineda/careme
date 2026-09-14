package com.careme.backend.service;

import com.careme.backend.entity.ClinicalEvent;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Deterministic composer for the fake provider mode.
 *
 * <p>It answers with the retrieved events' own content and cites all of them, so
 * the end-to-end cycle works offline without inventing anything.
 */
@Service
@ConditionalOnProperty(name = "careme.llm.mode", havingValue = "fake", matchIfMissing = true)
public class FakeClinicalAnswerComposer implements ClinicalAnswerComposer {

    @Override
    public ComposedAnswer compose(String question, List<ClinicalEvent> events) {
        if (events == null || events.isEmpty()) {
            throw new LlmIntegrationException("There is nothing to answer with");
        }
        String text = events.stream()
                .map(ClinicalEvent::content)
                .collect(Collectors.joining("; "));
        return new ComposedAnswer(text, events.stream().map(ClinicalEvent::code).toList());
    }
}
