package com.careme.backend.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The catalogue of metrics the system knows how to describe.
 *
 * <p>Each metric declares its reference unit, the components a value of it has,
 * the admissible range of each component, the exact equivalences between its
 * units and, when the metric has one, the unit a value without an explicit unit
 * resolves to. The catalogue is the single place that decides all of that: the
 * conversion never depends on the assistant's judgement.
 *
 * <p>Knowing a metric is not the same as admitting it: the admitted ones live in
 * the {@code admitted_metrics} table, seeded with the initial four, and a metric
 * the person confirms is admitted there.
 */
public enum Metric {

    WEIGHT(
            "weight",
            "peso",
            MeasurementUnit.KG,
            MeasurementUnit.KG,
            List.of(new MetricComponent("value", new BigDecimal("0.1"), new BigDecimal("500"))),
            Map.of(MeasurementUnit.KG, BigDecimal.ONE, MeasurementUnit.LB, new BigDecimal("0.45359237"))),

    WAIST(
            "waist",
            "circunferencia abdominal",
            MeasurementUnit.CM,
            MeasurementUnit.CM,
            List.of(new MetricComponent("value", new BigDecimal("0.1"), new BigDecimal("400"))),
            Map.of(MeasurementUnit.CM, BigDecimal.ONE, MeasurementUnit.IN, new BigDecimal("2.54"))),

    BLOOD_PRESSURE(
            "blood_pressure",
            "presión arterial",
            MeasurementUnit.MMHG,
            MeasurementUnit.MMHG,
            List.of(
                    new MetricComponent("systolic", new BigDecimal("50"), new BigDecimal("300")),
                    new MetricComponent("diastolic", new BigDecimal("30"), new BigDecimal("200"))),
            Map.of(MeasurementUnit.MMHG, BigDecimal.ONE)),

    CHOLESTEROL(
            "cholesterol",
            "colesterol",
            MeasurementUnit.MG_DL,
            null,
            List.of(new MetricComponent("value", new BigDecimal("1"), new BigDecimal("1000"))),
            Map.of(MeasurementUnit.MG_DL, BigDecimal.ONE, MeasurementUnit.MMOL_L, new BigDecimal("38.67"))),

    CREATININE(
            "creatinine",
            "creatinina",
            MeasurementUnit.MG_DL,
            null,
            List.of(new MetricComponent("value", new BigDecimal("0.1"), new BigDecimal("50"))),
            Map.of(MeasurementUnit.MG_DL, BigDecimal.ONE, MeasurementUnit.UMOL_L, new BigDecimal("0.011312217"))),

    FASTING_GLUCOSE(
            "fasting_glucose",
            "glucosa en ayunas",
            MeasurementUnit.MG_DL,
            null,
            List.of(new MetricComponent("value", new BigDecimal("1"), new BigDecimal("1000"))),
            Map.of(MeasurementUnit.MG_DL, BigDecimal.ONE, MeasurementUnit.MMOL_L, new BigDecimal("18.0182"))),

    TRIGLYCERIDES(
            "triglycerides",
            "triglicéridos",
            MeasurementUnit.MG_DL,
            null,
            List.of(new MetricComponent("value", new BigDecimal("1"), new BigDecimal("3000"))),
            Map.of(MeasurementUnit.MG_DL, BigDecimal.ONE, MeasurementUnit.MMOL_L, new BigDecimal("88.57")));

    private static final int STORED_SCALE = 4;

    private final String code;
    private final String label;
    private final MeasurementUnit referenceUnit;
    private final MeasurementUnit bareUnit;
    private final List<MetricComponent> components;
    private final Map<MeasurementUnit, BigDecimal> factorsToReference;

    Metric(
            String code,
            String label,
            MeasurementUnit referenceUnit,
            MeasurementUnit bareUnit,
            List<MetricComponent> components,
            Map<MeasurementUnit, BigDecimal> factorsToReference) {
        this.code = code;
        this.label = label;
        this.referenceUnit = referenceUnit;
        this.bareUnit = bareUnit;
        this.components = components;
        this.factorsToReference = factorsToReference;
    }

    /** @return the code persisted in the database and used by the assistant */
    public String code() {
        return code;
    }

    /** @return the Spanish name of the metric, shown to the person */
    public String label() {
        return label;
    }

    /** @return the unit every measurement of this metric is stored in */
    public MeasurementUnit referenceUnit() {
        return referenceUnit;
    }

    /** @return the components a value of this metric has, in order */
    public List<MetricComponent> components() {
        return components;
    }

    /** @return whether the metric is composite, that is, it has more than one value */
    public boolean isComposite() {
        return components.size() > 1;
    }

    /**
     * @return the unit a value without an explicit unit resolves to, or empty when
     *         this metric has more than one plausible unit and the value alone
     *         does not determine it
     */
    public Optional<MeasurementUnit> bareUnit() {
        return Optional.ofNullable(bareUnit);
    }

    /** @return whether the unit is one of this metric's units */
    public boolean accepts(MeasurementUnit unit) {
        return unit != null && factorsToReference.containsKey(unit);
    }

    /**
     * Converts a value expressed in one of this metric's units to the reference
     * unit. The equivalence is single-valued and does not depend on any judgement.
     *
     * @return the value in the reference unit, or {@code null} when the value is null
     * @throws IllegalArgumentException when the unit does not belong to this metric
     */
    public BigDecimal convertToReference(BigDecimal value, MeasurementUnit unit) {
        if (value == null) {
            return null;
        }
        BigDecimal factor = factorsToReference.get(unit);
        if (factor == null) {
            throw new IllegalArgumentException(
                    "Metric " + code + " has no unit " + (unit == null ? "null" : unit.code()));
        }
        if (factor.compareTo(BigDecimal.ONE) == 0) {
            return value;
        }
        return value.multiply(factor).setScale(STORED_SCALE, RoundingMode.HALF_UP);
    }

    /** @return whether the value is inside the admissible range of the component */
    public boolean admits(String componentKey, BigDecimal value) {
        return components.stream()
                .filter(component -> component.key().equals(componentKey))
                .findFirst()
                .map(component -> component.admits(value))
                .orElse(false);
    }

    /**
     * @return the metric whose code matches, or empty when the catalogue does not
     *         know it
     */
    public static Optional<Metric> fromCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(metric -> metric.code.equalsIgnoreCase(code.trim()))
                .findFirst();
    }
}
