package com.careme.backend.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.careme.backend.entity.ClinicalEvent;
import com.careme.backend.entity.EncounterMeasurementNote;
import com.careme.backend.entity.Metric;
import com.careme.backend.entity.MetricMeasurementDraft;
import com.careme.backend.repository.MetricMeasurementOutcome;
import com.careme.backend.repository.MetricMeasurementRepository;

/**
 * Registers the measurements a consultation collected, when it closes.
 *
 * <p>The deterministic check runs here, inside the registration path, before anything
 * is written: a measurement is admissible only when the metric is admitted, the values
 * are inside its range and the date is exact. One that does not pass is left out, with
 * no half-written record of it, while the admissible ones are stored.
 *
 * <p>The whole batch is one write: either every admissible measurement of the
 * consultation is stored, or the tracking stays exactly as it was. Each stored
 * measurement declares the consultation it came from.
 */
@Service
public class MeasurementRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(MeasurementRegistrationService.class);

    private final MetricMeasurementRepository metricMeasurementRepository;
    private final MetricCatalogService metricCatalogService;

    public MeasurementRegistrationService(
            MetricMeasurementRepository metricMeasurementRepository,
            MetricCatalogService metricCatalogService) {
        this.metricMeasurementRepository = metricMeasurementRepository;
        this.metricCatalogService = metricCatalogService;
    }

    /**
     * Stores the admissible measurements of a consultation.
     *
     * @param encounterCode the consultation they came from, which becomes their provenance
     * @param notes         the measurements the consultation collected
     * @return what happened to the batch
     */
    public MeasurementRegistrationResult register(
            String encounterCode, List<EncounterMeasurementNote> notes) {
        List<MetricMeasurementDraft> drafts = admissible(notes, encounterCode);
        if (drafts.isEmpty()) {
            return new MeasurementRegistrationResult(
                    MeasurementRegistrationResult.Kind.NOTHING, List.of(), "");
        }

        try {
            List<MetricMeasurementOutcome> outcomes = metricMeasurementRepository.upsertAll(drafts);
            return new MeasurementRegistrationResult(
                    MeasurementRegistrationResult.Kind.REGISTERED, outcomes, "");
        } catch (Exception exception) {
            log.error("Could not register the measurements of {}", encounterCode, exception);
            return new MeasurementRegistrationResult(
                    MeasurementRegistrationResult.Kind.FAILURE, List.of(), "");
        }
    }

    /** Keeps only the measurements that pass the deterministic check. */
    private List<MetricMeasurementDraft> admissible(
            List<EncounterMeasurementNote> notes, String encounterCode) {
        List<MetricMeasurementDraft> drafts = new ArrayList<>();
        for (EncounterMeasurementNote note : notes) {
            Metric metric = Metric.fromCode(note.metricCode()).orElse(null);
            if (metric == null || !metricCatalogService.isAdmitted(metric)) {
                continue;
            }
            // A measurement only means something on a concrete day, so an approximate or
            // unknown date never becomes exact at the close either. The note's own
            // invariant already guarantees that an exact precision carries a date.
            if (note.datePrecision() != ClinicalEvent.DatePrecision.EXACT) {
                continue;
            }
            if (note.date() == null) {
                continue;
            }
            if (!metricCatalogService.admits(metric, note.values())) {
                continue;
            }
            drafts.add(new MetricMeasurementDraft(
                    metric, note.date(), encounterCode, new LinkedHashMap<>(note.values())));
        }
        return drafts;
    }
}
