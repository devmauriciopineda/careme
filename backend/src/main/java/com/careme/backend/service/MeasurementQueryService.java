package com.careme.backend.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.careme.backend.dto.ChatMessageResponse;
import com.careme.backend.dto.MeasurementQueryIntent;
import com.careme.backend.entity.Metric;
import com.careme.backend.entity.MetricMeasurement;
import com.careme.backend.repository.MetricMeasurementRepository;

/**
 * Answers a question about the person's own measurements: resolves which metrics
 * and which period the question covers, retrieves the measurements that match,
 * formats them and returns one typed outcome.
 *
 * <p>It is read-only: it never writes to the tracking and never changes a
 * measurement. It never assumes the metric either —when the question does not say
 * which one, it asks— and it declares a metric the tracking does not admit instead
 * of answering with the values of another.
 */
@Service
public class MeasurementQueryService {

    private static final Logger log = LoggerFactory.getLogger(MeasurementQueryService.class);

    /** Asks what the question did not say, so no metric is ever assumed. */
    private static final String ASK_METRIC =
            "¿De qué medición quieres que te hable? Dime cuál"
                    + " —peso, circunferencia abdominal, presión arterial, colesterol—"
                    + " y te doy sus valores registrados.";

    /** A metric the tracking does not admit is declared as such, never invented. */
    private static final String NOT_TRACKED =
            "Esa métrica no forma parte de tu seguimiento, así que no tengo valores suyos que darte.";

    private static final String CONTINUATION =
            "Puedes decírmela si quieres empezar a registrarla.";

    /** States what an absence does and does not mean, so it is never read as "it did not happen". */
    private static final String ABSENCE_SCOPE_NOTE =
            "Que no conste no significa que haya ocurrido ni que haya dejado de ocurrir:"
                    + " solo que no está registrado.";

    private static final String FAILURE =
            "No he podido consultar tus mediciones. Puedes reintentarlo.";

    private static final List<ChatMessageResponse.SuggestedAction> ABSENCE_ACTIONS =
            List.of(
                    ChatMessageResponse.SuggestedAction.REFORMULATE,
                    ChatMessageResponse.SuggestedAction.REGISTER);

    private static final List<ChatMessageResponse.SuggestedAction> NOT_TRACKED_ACTIONS =
            List.of(ChatMessageResponse.SuggestedAction.REGISTER);

    private final MetricMeasurementRepository measurementRepository;
    private final MetricCatalogService metricCatalogService;
    private final MeasurementAnswerComposer composer;

    public MeasurementQueryService(
            MetricMeasurementRepository measurementRepository,
            MetricCatalogService metricCatalogService,
            MeasurementAnswerComposer composer) {
        this.measurementRepository = measurementRepository;
        this.metricCatalogService = metricCatalogService;
        this.composer = composer;
    }

    public MeasurementAnswerResult answer(MeasurementQueryIntent intent) {
        Optional<List<Metric>> resolved;
        try {
            resolved = resolveMetrics(intent);
        } catch (RuntimeException exception) {
            log.error("Could not resolve the metrics of a measurement query", exception);
            return failure();
        }
        if (resolved.isEmpty()) {
            return MeasurementAnswerResult.askForMetric(ASK_METRIC);
        }
        List<Metric> metrics = resolved.get();
        if (metrics.isEmpty()) {
            return MeasurementAnswerResult.noRecords(
                    ChatMessageResponse.AbsenceReason.METRIC_NOT_TRACKED, NOT_TRACKED_ACTIONS, NOT_TRACKED);
        }

        List<MetricMeasurement> retrieved;
        try {
            retrieved = measurementRepository.findByMetricsAndDateBetween(
                    metrics, intent.fromDate(), intent.toDate());
        } catch (RuntimeException exception) {
            // A retrieval that could not complete is a failure, never an absence: an
            // absence can only be declared when it was verified.
            log.error("Could not retrieve the measurements of the question", exception);
            return failure();
        }

        List<MeasurementFact> facts = retrieved.stream().map(MeasurementFact::of).toList();
        if (facts.isEmpty()) {
            return declareAbsence(intent);
        }
        return answered(intent, facts);
    }

    /**
     * The metrics the question is about, resolved against the admitted catalogue.
     *
     * <p>An empty result means the question named metrics that are not part of the
     * tracking. An empty {@code Optional} means it named none, and then the system
     * asks instead of assuming one.
     */
    private Optional<List<Metric>> resolveMetrics(MeasurementQueryIntent intent) {
        if (!intent.metricCodes().isEmpty()) {
            Set<Metric> named = new LinkedHashSet<>();
            intent.metricCodes().stream()
                    .map(Metric::fromCode)
                    .flatMap(Optional::stream)
                    .forEach(named::add);
            return Optional.of(admitted(named));
        }
        Set<Metric> inQuestion = metricsNamedIn(intent.question());
        return inQuestion.isEmpty() ? Optional.empty() : Optional.of(admitted(inQuestion));
    }

    private List<Metric> admitted(Set<Metric> metrics) {
        return metrics.stream().filter(metricCatalogService::isAdmitted).toList();
    }

    /**
     * The metrics the question names, read from the catalogue's own Spanish labels.
     * The catalogue is what decides how a metric is called, so nothing is inferred
     * and no synonym is invented.
     */
    private static Set<Metric> metricsNamedIn(String question) {
        if (question == null || question.isBlank()) {
            return Set.of();
        }
        String normalized = question.toLowerCase(Locale.ROOT);
        Set<Metric> named = new LinkedHashSet<>();
        for (Metric metric : Metric.values()) {
            if (normalized.contains(metric.label().toLowerCase(Locale.ROOT))) {
                named.add(metric);
            }
        }
        return named;
    }

    private MeasurementAnswerResult answered(MeasurementQueryIntent intent, List<MeasurementFact> facts) {
        String composed;
        try {
            composed = composer.compose(intent.question(), facts, intent.interpretationRequested());
        } catch (LlmIntegrationException exception) {
            log.warn("Could not compose an answer for the measurement query");
            return failure();
        }
        return MeasurementAnswerResult.answered(
                facts, faithful(composed, facts, intent.interpretationRequested()));
    }

    /**
     * The answer that reaches the person is the one the application formatted. A
     * composed answer that does not reproduce every measurement verbatim is dropped,
     * because a rounded or converted value would be an altered record.
     */
    private static String faithful(
            String composed, List<MeasurementFact> facts, boolean interpretationRequested) {
        boolean reproduces = composed != null
                && !composed.isBlank()
                && facts.stream().allMatch(fact -> composed.contains(fact.text()));
        if (reproduces) {
            return composed;
        }
        String answer = "Estas son tus mediciones registradas: "
                + facts.stream().map(MeasurementFact::text).collect(Collectors.joining("; "))
                + ".";
        return interpretationRequested ? answer + " " + MeasurementAnswerComposer.DECLINED : answer;
    }

    /** An absence reported only after the retrieval completed and found nothing. */
    private MeasurementAnswerResult declareAbsence(MeasurementQueryIntent intent) {
        ChatMessageResponse.AbsenceReason reason = absenceReason(intent);
        return MeasurementAnswerResult.noRecords(reason, ABSENCE_ACTIONS, absenceMessage(reason));
    }

    /**
     * Names the scope of the absence, so a period that holds no measurement is never
     * presented as an empty tracking.
     */
    private static ChatMessageResponse.AbsenceReason absenceReason(MeasurementQueryIntent intent) {
        return intent.fromDate() != null || intent.toDate() != null
                ? ChatMessageResponse.AbsenceReason.NO_MEASUREMENTS_IN_PERIOD
                : ChatMessageResponse.AbsenceReason.NO_MEASUREMENTS;
    }

    private static String absenceMessage(ChatMessageResponse.AbsenceReason reason) {
        String scope = reason == ChatMessageResponse.AbsenceReason.NO_MEASUREMENTS_IN_PERIOD
                ? "No consta ninguna medición tuya de esa métrica en ese periodo."
                : "No consta ninguna medición tuya de esa métrica.";
        return scope + " " + ABSENCE_SCOPE_NOTE + " " + CONTINUATION;
    }

    private static MeasurementAnswerResult failure() {
        return MeasurementAnswerResult.failure(FAILURE);
    }
}
