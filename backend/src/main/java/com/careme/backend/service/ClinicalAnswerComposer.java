package com.careme.backend.service;

import com.careme.backend.entity.ClinicalEvent;
import java.util.List;

/**
 * Composes a Spanish answer from a retrieved set of clinical events.
 *
 * <p>Kept separate from the intent interpreter so the composition can be
 * replaced in tests and never depends on classification.
 */
public interface ClinicalAnswerComposer {

    /**
     * Answers {@code question} using only {@code events} and reports the codes of
     * the events the answer relies on. An empty list of references means the
     * answer relied on the whole retrieved set.
     */
    ComposedAnswer compose(String question, List<ClinicalEvent> events);

    record ComposedAnswer(String text, List<String> references) {
        public ComposedAnswer {
            references = references == null ? List.of() : List.copyOf(references);
        }
    }
}
