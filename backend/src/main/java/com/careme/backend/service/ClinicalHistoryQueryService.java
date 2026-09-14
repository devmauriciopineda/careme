package com.careme.backend.service;

import com.careme.backend.dto.ClinicalEventIntent;
import com.careme.backend.entity.ClinicalEvent;
import com.careme.backend.repository.ClinicalEventQueryRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Answers a question about the clinical history: retrieves the facts, composes
 * the answer from them and returns one typed outcome. It never writes to the
 * history or the index.
 */
@Service
public class ClinicalHistoryQueryService {

    private static final Logger log = LoggerFactory.getLogger(ClinicalHistoryQueryService.class);

    private final ClinicalEventQueryRepository queryRepository;
    private final ClinicalAnswerComposer composer;
    private final ClinicalEventIntentValidator intentValidator;

    public ClinicalHistoryQueryService(
            ClinicalEventQueryRepository queryRepository,
            ClinicalAnswerComposer composer,
            ClinicalEventIntentValidator intentValidator) {
        this.queryRepository = queryRepository;
        this.composer = composer;
        this.intentValidator = intentValidator;
    }

    public ClinicalAnswerResult answer(ClinicalEventIntent intent) {
        try {
            intentValidator.validate(intent);
        } catch (RuntimeException exception) {
            log.warn("Rejected malformed clinical history query");
            return failure();
        }

        ClinicalEventIntent.Query query = intent.query();
        List<ClinicalEvent> retrieved;
        try {
            retrieved = queryRepository.search(
                    query.searchTerms(), query.type(), query.fromDate(), query.toDate());
        } catch (RuntimeException exception) {
            log.error("Could not search the clinical history index", exception);
            return failure();
        }

        if (retrieved.isEmpty()) {
            return new ClinicalAnswerResult(
                    ClinicalAnswerResult.Kind.NO_RECORDS,
                    List.of(),
                    "No encontré registros en tu historia clínica que respondan a tu pregunta.");
        }

        try {
            ClinicalAnswerComposer.ComposedAnswer composed = composer.compose(query.question(), retrieved);
            // A composer that cites nothing relied on the whole set, which is the
            // only material it was given, so the answer stays verifiable.
            List<ClinicalEvent> support = composed.references().isEmpty()
                    ? retrieved
                    : retrieved.stream()
                            .filter(event -> composed.references().contains(event.code()))
                            .toList();
            return new ClinicalAnswerResult(ClinicalAnswerResult.Kind.ANSWERED, support, composed.text());
        } catch (LlmIntegrationException exception) {
            log.warn("Could not compose a grounded answer for the clinical history query");
            return failure();
        }
    }

    private static ClinicalAnswerResult failure() {
        return new ClinicalAnswerResult(
                ClinicalAnswerResult.Kind.FAILURE,
                List.of(),
                "No pude completar la búsqueda. Puedes volver a intentarlo.");
    }
}
