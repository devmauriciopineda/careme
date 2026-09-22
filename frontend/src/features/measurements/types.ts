/**
 * Metrics tracked by the application. Extend this union when a new
 * measurement is added, then register it in `METRICS`.
 */
export type MetricKey = "weightKg" | "waistCm";

/** Metric definition returned by the read-only tracking catalog. */
export type TrackingMetricCatalog = {
    code: string;
    label: string;
    referenceUnit: string;
    components: { key: string }[];
};

/** One component value of a stored metric measurement. */
export type TrackingMeasurementValue = {
    component: string;
    value: number;
};

/** One stored measurement, including all values of a composite metric. */
export type TrackingMeasurement = {
    id: string;
    date: string;
    values: TrackingMeasurementValue[];
};

/** Read model for one metric and its chronological measurements. */
export type TrackingMetric = {
    code: string;
    label: string;
    unit: string;
    measurements: TrackingMeasurement[];
};

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

/** A measurement read from a file, and what loading it would do. */
export type ImportPreviewRow = MeasurementInput & {
    /** `true` when that day already has a measurement, so it would be replaced. */
    replacesExisting: boolean;
};

/** What loading a file would do, without doing it. */
export type ImportPreview = {
    rows: ImportPreviewRow[];
    totalRows: number;
    newCount: number;
    replacedCount: number;
    ignoredCount: number;
};

/** What loading a file did. */
export type ImportResult = {
    createdCount: number;
    replacedCount: number;
    ignoredCount: number;
    totalRows: number;
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
