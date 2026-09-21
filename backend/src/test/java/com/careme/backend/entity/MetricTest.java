package com.careme.backend.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Verifies the catalogue: the reference unit of each metric, the composite metric,
 * the exact unit equivalences, the admissible ranges and the unit a bare value
 * resolves to.
 */
class MetricTest {

    @Test
    void storesEveryMetricInItsReferenceUnit() {
        assertThat(Metric.WEIGHT.referenceUnit()).isEqualTo(MeasurementUnit.KG);
        assertThat(Metric.WAIST.referenceUnit()).isEqualTo(MeasurementUnit.CM);
        assertThat(Metric.BLOOD_PRESSURE.referenceUnit()).isEqualTo(MeasurementUnit.MMHG);
        assertThat(Metric.CHOLESTEROL.referenceUnit()).isEqualTo(MeasurementUnit.MG_DL);
    }

    @Test
    void describesBloodPressureAsTheOnlyCompositeMetricOfTheInitialCatalogue() {
        assertThat(Metric.BLOOD_PRESSURE.isComposite()).isTrue();
        assertThat(Metric.BLOOD_PRESSURE.components())
                .extracting(MetricComponent::key)
                .containsExactly("systolic", "diastolic");
        assertThat(Metric.WEIGHT.isComposite()).isFalse();
        assertThat(Metric.WEIGHT.components()).extracting(MetricComponent::key).containsExactly("value");
    }

    @Test
    void convertsOtherUnitsOfTheSameMetricToTheReferenceUnit() {
        assertThat(Metric.WEIGHT.convertToReference(new BigDecimal("100"), MeasurementUnit.LB))
                .isEqualByComparingTo("45.3592");
        assertThat(Metric.WAIST.convertToReference(new BigDecimal("40"), MeasurementUnit.IN))
                .isEqualByComparingTo("101.6000");
        assertThat(Metric.CHOLESTEROL.convertToReference(new BigDecimal("5"), MeasurementUnit.MMOL_L))
                .isEqualByComparingTo("193.3500");
    }

    @Test
    void appliesTheSameEquivalenceEveryTime() {
        BigDecimal first = Metric.CHOLESTEROL.convertToReference(new BigDecimal("5"), MeasurementUnit.MMOL_L);
        BigDecimal second = Metric.CHOLESTEROL.convertToReference(new BigDecimal("5"), MeasurementUnit.MMOL_L);

        assertThat(first).isEqualByComparingTo(second);
    }

    @Test
    void leavesAValueInTheReferenceUnitUntouched() {
        assertThat(Metric.WEIGHT.convertToReference(new BigDecimal("70.5"), MeasurementUnit.KG))
                .isEqualByComparingTo("70.5");
    }

    @Test
    void refusesAUnitThatDoesNotBelongToTheMetric() {
        assertThatThrownBy(() -> Metric.WEIGHT.convertToReference(new BigDecimal("70"), MeasurementUnit.MMOL_L))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(Metric.WEIGHT.accepts(MeasurementUnit.MMOL_L)).isFalse();
        assertThat(Metric.WEIGHT.accepts(MeasurementUnit.LB)).isTrue();
    }

    @Test
    void admitsOnlyValuesInsideTheRangeOfTheComponent() {
        assertThat(Metric.WEIGHT.admits("value", new BigDecimal("70"))).isTrue();
        assertThat(Metric.WEIGHT.admits("value", new BigDecimal("501"))).isFalse();
        assertThat(Metric.WEIGHT.admits("value", new BigDecimal("0.05"))).isFalse();
        assertThat(Metric.WEIGHT.admits("systolic", new BigDecimal("70"))).isFalse();
    }

    @Test
    void declaresTheUnitABareValueResolvesToOnlyWhenItIsUnambiguous() {
        assertThat(Metric.WEIGHT.bareUnit()).contains(MeasurementUnit.KG);
        assertThat(Metric.WAIST.bareUnit()).contains(MeasurementUnit.CM);
        assertThat(Metric.BLOOD_PRESSURE.bareUnit()).contains(MeasurementUnit.MMHG);
        assertThat(Metric.CHOLESTEROL.bareUnit()).isEmpty();
    }

    @Test
    void knowsEveryCataloguedMetricByItsCode() {
        assertThat(Metric.fromCode("weight")).contains(Metric.WEIGHT);
        assertThat(Metric.fromCode(" WAIST ")).contains(Metric.WAIST);
        assertThat(Metric.fromCode("haemoglobin")).isEmpty();
        assertThat(Metric.fromCode(null)).isEmpty();
        assertThat(Metric.fromCode("  ")).isEmpty();
    }

    @Test
    void keepsNoValueWhenThereIsNoneToConvert() {
        assertThat(Metric.WEIGHT.convertToReference(null, MeasurementUnit.KG)).isNull();
    }

    @Test
    void knowsTheUnitsOfTheCatalogueByTheirCode() {
        assertThat(MeasurementUnit.fromCode("kg")).isEqualTo(MeasurementUnit.KG);
        assertThat(MeasurementUnit.fromCode(" MG/DL ")).isEqualTo(MeasurementUnit.MG_DL);
        assertThat(MeasurementUnit.fromCode("stone")).isNull();
        assertThat(MeasurementUnit.fromCode(null)).isNull();
        assertThat(MeasurementUnit.fromCode("  ")).isNull();
    }

    @Test
    void keepsTheCatalogueExtensibleBeyondTheInitialMetrics() {
        List<Metric> known = List.of(Metric.values());

        assertThat(known).contains(Metric.CREATININE, Metric.FASTING_GLUCOSE, Metric.TRIGLYCERIDES);
        assertThat(Metric.CREATININE.referenceUnit()).isEqualTo(MeasurementUnit.MG_DL);
    }
}
