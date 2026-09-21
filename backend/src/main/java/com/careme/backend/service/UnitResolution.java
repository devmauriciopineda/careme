package com.careme.backend.service;

import com.careme.backend.entity.MeasurementUnit;

/**
 * The unit a mentioned value resolves to, and whether the system could not
 * determine it and must ask the person instead of assuming one.
 *
 * @param unit                 the resolved unit, or {@code null} when undetermined
 * @param requiresClarification whether the person must state the unit
 */
public record UnitResolution(MeasurementUnit unit, boolean requiresClarification) {

    public static UnitResolution resolved(MeasurementUnit unit) {
        return new UnitResolution(unit, false);
    }

    public static UnitResolution undetermined() {
        return new UnitResolution(null, true);
    }
}
