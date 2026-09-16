package com.careme.backend.service;

import com.careme.backend.entity.ClinicalEvent;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
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

    /**
     * Coverage the offline answer reports. It is `full` by default, so this mode
     * answers the whole question unless a test or a local run asks for another
     * shape on purpose.
     */
    private final Coverage coverage;

    /** Part of the question a `partial` answer reports as unsupported. */
    private final String unsupportedPart;

    public FakeClinicalAnswerComposer(
            @Value("${careme.llm.fake.coverage:full}") String coverage,
            @Value("${careme.llm.fake.unsupported-part:}") String unsupportedPart) {
        this.unsupportedPart = unsupportedPart == null ? "" : unsupportedPart.trim();
        this.coverage = coverageOf(coverage, this.unsupportedPart);
    }

    @Override
    public ComposedAnswer compose(String question, List<ClinicalEvent> events) {
        if (events == null || events.isEmpty()) {
            throw new LlmIntegrationException("There is nothing to answer with");
        }
        if (coverage == Coverage.NONE) {
            return new ComposedAnswer(
                    "Los hechos recuperados no responden a la pregunta.",
                    List.of(),
                    Coverage.NONE,
                    null);
        }
        String text = events.stream()
                .map(ClinicalEvent::content)
                .collect(Collectors.joining("; "));
        return new ComposedAnswer(
                text,
                events.stream().map(ClinicalEvent::code).toList(),
                coverage,
                coverage == Coverage.PARTIAL ? unsupportedPart : null);
    }

    /**
     * Reads the coverage this mode is configured to report. A `partial` request
     * without a part to declare is downgraded to `full`, so the reported coverage
     * never contradicts the answer it accompanies.
     */
    private static Coverage coverageOf(String configured, String unsupportedPart) {
        String value = configured == null ? "" : configured.trim().toLowerCase(java.util.Locale.ROOT);
        if (value.equals("partial") && unsupportedPart.isBlank()) {
            return Coverage.FULL;
        }
        return switch (value) {
            case "none" -> Coverage.NONE;
            case "partial" -> Coverage.PARTIAL;
            default -> Coverage.FULL;
        };
    }
}
