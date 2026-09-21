package com.careme.backend.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.careme.backend.entity.MeasurementUnit;
import com.careme.backend.entity.Metric;
import com.careme.backend.repository.AdmittedMetricRepository;

/**
 * The catalogue of metrics as the application uses it: which metrics the tracking
 * admits, in which unit a mentioned value is stored, and whether a value is
 * admissible for its metric.
 *
 * <p>The catalogue in code describes every metric; admitting one is what the person
 * confirms. The service never guesses: a unit it cannot determine is reported as
 * needing clarification, and a value outside the metric's range is rejected.
 */
@Service
public class MetricCatalogService {

    private static final Logger log = LoggerFactory.getLogger(MetricCatalogService.class);

    private final AdmittedMetricRepository admittedMetricRepository;

    public MetricCatalogService(AdmittedMetricRepository admittedMetricRepository) {
        this.admittedMetricRepository = admittedMetricRepository;
    }

    /** @return the metrics the tracking admits today */
    public Set<Metric> admittedMetrics() {
        return admittedMetricRepository.findAll();
    }

    /** @return whether the tracking admits the metric */
    public boolean isAdmitted(Metric metric) {
        return metric != null && admittedMetricRepository.admits(metric);
    }

    /**
     * Admits a metric because the person confirmed it. A metric that is already
     * admitted, and a metric the catalogue does not know, are left as they are.
     *
     * @return whether the metric started to be admitted with this call
     */
    public boolean admit(Metric metric) {
        if (metric == null) {
            return false;
        }
        boolean admitted = admittedMetricRepository.admit(metric, OffsetDateTime.now());
        if (admitted) {
            log.info("Metric {} admitted after the person confirmed it", metric.code());
        }
        return admitted;
    }

    /**
     * Resolves the unit of a mentioned value. A unit the person stated must belong
     * to the metric; a value without a unit resolves to the metric's bare unit when
     * it has one. Anything else needs clarification: the unit is never assumed.
     */
    public UnitResolution resolveUnit(Metric metric, MeasurementUnit declaredUnit) {
        if (metric == null) {
            return UnitResolution.undetermined();
        }
        if (declaredUnit != null) {
            return metric.accepts(declaredUnit)
                    ? UnitResolution.resolved(declaredUnit)
                    : UnitResolution.undetermined();
        }
        return metric.bareUnit().map(UnitResolution::resolved).orElseGet(UnitResolution::undetermined);
    }

    /**
     * Converts a value expressed in one of the metric's units to its reference unit.
     *
     * @throws IllegalArgumentException when the unit does not belong to the metric
     */
    public BigDecimal toReferenceUnit(Metric metric, BigDecimal value, MeasurementUnit unit) {
        return metric.convertToReference(value, unit);
    }

    /**
     * @return whether every component of the value is inside the metric's range and
     *         none of the metric's components is missing
     */
    public boolean admits(Metric metric, Map<String, BigDecimal> values) {
        if (metric == null || values == null || values.isEmpty()) {
            return false;
        }
        return metric.components().stream().allMatch(component -> {
            BigDecimal value = values.get(component.key());
            return value != null && metric.admits(component.key(), value);
        }) && values.size() == metric.components().size();
    }

    /** @return the metric the catalogue knows by that code, if any */
    public Optional<Metric> metricByCode(String code) {
        return Metric.fromCode(code);
    }
}
