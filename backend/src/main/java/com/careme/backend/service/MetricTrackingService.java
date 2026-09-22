package com.careme.backend.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.careme.backend.dto.MetricCatalogResponse;
import com.careme.backend.dto.MetricComponentResponse;
import com.careme.backend.dto.TrackingMeasurementResponse;
import com.careme.backend.dto.TrackingMetricResponse;
import com.careme.backend.entity.Metric;
import com.careme.backend.entity.MetricMeasurement;
import com.careme.backend.exception.MetricNotFoundException;
import com.careme.backend.repository.MetricMeasurementRepository;

/** Read-only application service for the extensible tracking view. */
@Service
public class MetricTrackingService {

    private final MetricCatalogService metricCatalogService;
    private final MetricMeasurementRepository measurementRepository;

    public MetricTrackingService(
            MetricCatalogService metricCatalogService,
            MetricMeasurementRepository measurementRepository) {
        this.metricCatalogService = metricCatalogService;
        this.measurementRepository = measurementRepository;
    }

    public List<MetricCatalogResponse> catalog() {
        return metricCatalogService.admittedMetrics().stream()
                .sorted(Comparator.comparing(Metric::code))
                .map(MetricTrackingService::toCatalogResponse)
                .toList();
    }

    public TrackingMetricResponse findByCode(String code) {
        Metric metric = metricCatalogService.metricByCode(code)
                .filter(metricCatalogService::isAdmitted)
                .orElseThrow(() -> new MetricNotFoundException(code));

        List<TrackingMeasurementResponse> measurements = measurementRepository.findByMetric(metric).stream()
                .sorted(Comparator.comparing(MetricMeasurement::date))
                .map(measurement -> toMeasurementResponse(metric, measurement))
                .toList();

        return new TrackingMetricResponse(metric.code(), metric.label(), metric.referenceUnit().code(), measurements);
    }

    private static MetricCatalogResponse toCatalogResponse(Metric metric) {
        return new MetricCatalogResponse(
                metric.code(),
                metric.label(),
                metric.referenceUnit().code(),
                metric.components().stream().map(component -> new MetricComponentResponse(component.key())).toList());
    }

    private static TrackingMeasurementResponse toMeasurementResponse(Metric metric, MetricMeasurement measurement) {
        // The stored values follow the order they were written in, so the response
        // re-orders them by the catalogue's component definition. A composite metric
        // then always reads systolic before diastolic, whatever the storage order was.
        List<TrackingMeasurementResponse.Value> values = metric.components().stream()
                .flatMap(component -> measurement.values().stream()
                        .filter(value -> value.component().equals(component.key())))
                .map(value -> new TrackingMeasurementResponse.Value(value.component(), value.value()))
                .toList();
        return new TrackingMeasurementResponse(measurement.id(), measurement.date(), values);
    }
}