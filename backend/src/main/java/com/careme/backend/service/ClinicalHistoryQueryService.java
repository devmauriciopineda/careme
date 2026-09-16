package com.careme.backend.service;

import com.careme.backend.dto.ChatMessageResponse;
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

    /**
     * Both actions are offered together: the reason alone does not decide which
     * one helps, because the user may have asked in other words or the fact may
     * never have been registered.
     */
    private static final List<ChatMessageResponse.SuggestedAction> SUGGESTED_ACTIONS =
            List.of(
                    ChatMessageResponse.SuggestedAction.REFORMULATE,
                    ChatMessageResponse.SuggestedAction.REGISTER);

    /**
     * States what an absence does and does not mean, so the user never reads it as
     * "this did not happen".
     */
    private static final String ABSENCE_SCOPE_NOTE =
            "Que no encuentre registros no significa que no haya ocurrido:"
                    + " solo que no consta en tu historia clínica.";

    private static final String CONTINUATION =
            "Puedes reformular la pregunta con otras palabras"
                    + " o contarme el hecho para registrarlo.";

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
            return declareAbsence(query);
        }

        ClinicalAnswerComposer.ComposedAnswer composed;
        try {
            composed = composer.compose(query.question(), retrieved);
        } catch (LlmIntegrationException exception) {
            log.warn("Could not compose a grounded answer for the clinical history query");
            return failure();
        }

        if (composed.coverage() == ClinicalAnswerComposer.Coverage.NONE) {
            // Retrieval returned events that do not answer the question, so showing
            // them as support would mislead. The absence is declared instead, and the
            // composer's own wording is dropped so no retrieved fact can reach the user.
            log.debug("The retrieved events do not support the clinical history question");
            return declareAbsence(query);
        }

        // A composer that cites nothing relied on the whole set, which is the only
        // material it was given, so the answer stays verifiable.
        List<ClinicalEvent> support = composed.references().isEmpty()
                ? retrieved
                : retrieved.stream()
                        .filter(event -> composed.references().contains(event.code()))
                        .toList();
        return ClinicalAnswerResult.answered(support, answerMessage(composed));
    }

    /**
     * Resolves why the history could not answer and offers a way to continue.
     *
     * <p>This runs only after a search completed and returned nothing, so an
     * absence is never declared on a search that failed. A count that cannot be
     * read is a failure to verify, and a failure is reported as a failure.
     */
    private ClinicalAnswerResult declareAbsence(ClinicalEventIntent.Query query) {
        ChatMessageResponse.AbsenceReason reason;
        try {
            reason = absenceReason(query);
        } catch (RuntimeException exception) {
            log.error("Could not verify why the clinical history holds no answer", exception);
            return failure();
        }
        return ClinicalAnswerResult.noRecords(reason, SUGGESTED_ACTIONS, absenceMessage(reason));
    }

    /**
     * Walks from the widest scope to the narrowest, so an absence is attributed to
     * the whole history only when the history really holds nothing, and to a type
     * or a period only when those were asked about. The counts are not limited
     * like {@code search}, so they can prove the absence.
     */
    private ChatMessageResponse.AbsenceReason absenceReason(ClinicalEventIntent.Query query) {
        if (queryRepository.countAll() == 0) {
            return ChatMessageResponse.AbsenceReason.EMPTY_HISTORY;
        }
        if (query.type() != null && queryRepository.countByType(query.type()) == 0) {
            return ChatMessageResponse.AbsenceReason.NO_EVENTS_OF_TYPE;
        }
        if ((query.fromDate() != null || query.toDate() != null)
                && queryRepository.countByPeriod(query.fromDate(), query.toDate()) == 0) {
            return ChatMessageResponse.AbsenceReason.NO_EVENTS_IN_PERIOD;
        }
        return ChatMessageResponse.AbsenceReason.NO_TERM_MATCH;
    }

    /**
     * Names the real scope of the absence and offers the next step. No message
     * states that the fact did not happen: an absence describes the record, not
     * the person's life.
     */
    private static String absenceMessage(ChatMessageResponse.AbsenceReason reason) {
        String scope = switch (reason) {
            case EMPTY_HISTORY -> "Todavía no hay hechos registrados en tu historia clínica.";
            case NO_EVENTS_OF_TYPE -> "No encuentro registros de ese tipo de hecho en tu historia clínica.";
            case NO_EVENTS_IN_PERIOD -> "No encuentro registros en ese periodo de tu historia clínica;"
                    + " puede haber registros en otras fechas.";
            case NO_TERM_MATCH -> "No encuentro registros en tu historia clínica que respondan a tu pregunta;"
                    + " puede haber registros escritos con otras palabras.";
        };
        return scope + " " + ABSENCE_SCOPE_NOTE + " " + CONTINUATION;
    }

    /**
     * Adds the declaration of the part of the question the answer could not
     * support. The declaration can only withdraw coverage: it names the unanswered
     * part and never introduces a fact or a reference of its own.
     */
    private static String answerMessage(ClinicalAnswerComposer.ComposedAnswer composed) {
        String unsupported = trailingPunctuationRemoved(composed.unsupported());
        if (unsupported.isEmpty()) {
            return composed.text();
        }
        return composed.text()
                + " Sobre esta parte no encuentro registros en tu historia clínica: "
                + unsupported
                + ". " + ABSENCE_SCOPE_NOTE + " " + CONTINUATION;
    }

    /**
     * Drops the marks a composer may already have closed its text with, so the
     * declaration adds its own full stop and never doubles it.
     */
    private static String trailingPunctuationRemoved(String unsupported) {
        if (unsupported == null) {
            return "";
        }
        return unsupported.trim().replaceAll("[.\\s]+$", "");
    }

    private static ClinicalAnswerResult failure() {
        return ClinicalAnswerResult.failure("No pude completar la búsqueda. Puedes volver a intentarlo.");
    }
}
