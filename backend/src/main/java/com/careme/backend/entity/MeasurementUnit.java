package com.careme.backend.entity;

import java.util.Arrays;

/**
 * Unit a measurement value can be expressed in.
 *
 * <p>Every metric has a reference unit, and a value expressed in another unit of
 * the same metric is converted to it before being stored. The {@code code} is the
 * value persisted in the database.
 */
public enum MeasurementUnit {

    KG("kg"),
    LB("lb"),
    CM("cm"),
    IN("in"),
    MMHG("mmHg"),
    MG_DL("mg/dL"),
    MMOL_L("mmol/L"),
    UMOL_L("umol/L");

    private final String code;

    MeasurementUnit(String code) {
        this.code = code;
    }

    /** @return the code persisted in the database and shown to the person */
    public String code() {
        return code;
    }

    /**
     * @return the unit whose code matches, or {@code null} when no unit does
     */
    public static MeasurementUnit fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return Arrays.stream(values())
                .filter(unit -> unit.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElse(null);
    }
}
