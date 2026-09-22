package com.careme.backend.dto;

import java.util.List;

/** Public description of one admitted metric. */
public record MetricCatalogResponse(
        String code,
        String label,
        String referenceUnit,
        List<MetricComponentResponse> components) {

    public MetricCatalogResponse {
        components = components == null ? List.of() : List.copyOf(components);
    }
}