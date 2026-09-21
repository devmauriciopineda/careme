package com.careme.backend.repository;

import com.careme.backend.entity.MetricMeasurement;

/**
 * What happened to one measurement of a batch: the stored measurement and whether
 * it replaced the value of a metric and day that already had one.
 *
 * @param measurement the stored measurement
 * @param replaced    whether an existing measurement of that metric and day was updated
 */
public record MetricMeasurementOutcome(MetricMeasurement measurement, boolean replaced) {
}
