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

    /** How much of the question the retrieved events support. */
    enum Coverage {
        /** The retrieved events answer the whole question. */
        FULL,
        /** The retrieved events answer part of the question. */
        PARTIAL,
        /** The retrieved events do not answer the question. */
        NONE
    }

    /**
     * The composed answer.
     *
     * @param text        the Spanish answer, built only from the retrieved events
     * @param references  the codes of the events the answer relies on
     * @param coverage    how much of the question those events support
     * @param unsupported the part of the question the retrieved events do not
     *                    support, or {@code null} when they support it fully
     */
    record ComposedAnswer(String text, List<String> references, Coverage coverage, String unsupported) {
        public ComposedAnswer {
            references = references == null ? List.of() : List.copyOf(references);
            coverage = coverage == null ? Coverage.FULL : coverage;
        }

        /** An answer that covered the whole question. */
        public ComposedAnswer(String text, List<String> references) {
            this(text, references, Coverage.FULL, null);
        }

        /**
         * An answer that reports the part of the question it could not support. A
         * blank part means the retrieved events supported the whole question.
         */
        public ComposedAnswer(String text, List<String> references, String unsupported) {
            this(text, references, coverageOf(unsupported), blankToNull(unsupported));
        }

        private static Coverage coverageOf(String unsupported) {
            return unsupported == null || unsupported.isBlank() ? Coverage.FULL : Coverage.PARTIAL;
        }

        private static String blankToNull(String unsupported) {
            return unsupported == null || unsupported.isBlank() ? null : unsupported;
        }
    }
}
