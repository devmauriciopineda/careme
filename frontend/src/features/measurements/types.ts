/**
 * Metrics tracked by the application. Extend this union when a new
 * measurement is added, then register it in `METRICS`.
 */
export type MetricKey = "weightKg" | "waistCm";

/** A single body measurement recorded on a given day. */
export type Measurement = {
    id: string;
    /** Calendar date in ISO `yyyy-MM-dd` format. */
    date: string;
    weightKg: number;
    waistCm: number;
};

/** The values needed to register or replace the measurement of a day. */
export type MeasurementInput = {
    /** Calendar date in ISO `yyyy-MM-dd` format. */
    date: string;
    weightKg: number;
    waistCm: number;
};

/** Presentation and scale configuration for a tracked metric. */
export type MetricConfig = {
    key: MetricKey;
    label: string;
    unit: string;
    decimals: number;
    /** CSS custom property holding the series color. */
    colorVar: string;
    /** Fraction of the value span added above and below the chart domain. */
    domainPaddingRatio: number;
};

/** A single point ready to be rendered by a chart. */
export type SeriesPoint = {
    date: string;
    /** Date label already localized, so charts never format on the client. */
    label: string;
    value: number;
};
