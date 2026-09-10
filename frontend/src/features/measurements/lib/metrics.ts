import { STRINGS } from "./strings";

import type {
    Measurement,
    MetricConfig,
    MetricKey,
    SeriesPoint,
} from "../types";

const DATE_FORMATTER = new Intl.DateTimeFormat("es-ES", {
    day: "2-digit",
    month: "short",
    // The domain stores calendar dates, so formatting must not shift with the
    // server or browser time zone.
    timeZone: "UTC",
});

const VALUE_FORMATTER_CACHE = new Map<number, Intl.NumberFormat>();

/**
 * Configuration for every tracked metric. Adding a metric means adding an entry
 * here plus a field on `Measurement`.
 */
export const METRICS: Record<MetricKey, MetricConfig> = {
    weightKg: {
        key: "weightKg",
        label: STRINGS.metrics.weightLabel,
        unit: STRINGS.metrics.weightUnit,
        decimals: 1,
        colorVar: "var(--chart-1)",
        domainPaddingRatio: 0.15,
    },
    waistCm: {
        key: "waistCm",
        label: STRINGS.metrics.waistLabel,
        unit: STRINGS.metrics.waistUnit,
        decimals: 1,
        colorVar: "var(--chart-2)",
        domainPaddingRatio: 0.15,
    },
};

/** Metrics in the order they should be presented. */
export const METRIC_KEYS: readonly MetricKey[] = ["weightKg", "waistCm"];

function getValueFormatter(decimals: number): Intl.NumberFormat {
    const cached = VALUE_FORMATTER_CACHE.get(decimals);
    if (cached) {
        return cached;
    }

    const formatter = new Intl.NumberFormat("es-ES", {
        minimumFractionDigits: decimals,
        maximumFractionDigits: decimals,
    });
    VALUE_FORMATTER_CACHE.set(decimals, formatter);
    return formatter;
}

function roundToSingleDecimal(value: number): number {
    return Math.round(value * 10) / 10;
}

/** Oldest measurement first. ISO dates sort chronologically as strings. */
export function sortByDateAsc(measurements: readonly Measurement[]): Measurement[] {
    return [...measurements].sort((a, b) => a.date.localeCompare(b.date));
}

/** Most recent measurement first. */
export function sortByDateDesc(measurements: readonly Measurement[]): Measurement[] {
    return [...measurements].sort((a, b) => b.date.localeCompare(a.date));
}

/** Converts measurements into the flat, serializable shape charts consume. */
export function buildSeries(
    measurements: readonly Measurement[],
    metric: MetricKey
): SeriesPoint[] {
    return sortByDateAsc(measurements).map((measurement) => ({
        date: measurement.date,
        label: formatMeasurementDate(measurement.date),
        value: measurement[metric],
    }));
}

/**
 * Builds a padded Y axis domain. The axis deliberately does not start at zero
 * so small day-to-day variations stay readable.
 */
export function computeDomain(
    values: readonly number[],
    paddingRatio: number
): [number, number] {
    if (values.length === 0) {
        return [0, 0];
    }

    const min = Math.min(...values);
    const max = Math.max(...values);
    const span = max - min;
    const padding =
        span === 0 ? Math.max(Math.abs(max) * paddingRatio, 1) : span * paddingRatio;

    return [
        roundToSingleDecimal(min - padding),
        roundToSingleDecimal(max + padding),
    ];
}

export function formatMetricValue(value: number, config: MetricConfig): string {
    return getValueFormatter(config.decimals).format(value);
}

export function formatMeasurementDate(isoDate: string): string {
    return DATE_FORMATTER.format(new Date(`${isoDate}T00:00:00Z`));
}

export function computeDateRange(measurements: readonly Measurement[]): {
    from: string;
    to: string;
} {
    if (measurements.length === 0) {
        return { from: "", to: "" };
    }

    const sorted = sortByDateAsc(measurements);
    return {
        from: formatMeasurementDate(sorted[0].date),
        to: formatMeasurementDate(sorted[sorted.length - 1].date),
    };
}

/** Textual description of a series, used as the accessible chart caption. */
export function buildChartSummary(
    series: readonly SeriesPoint[],
    config: MetricConfig
): string {
    const [first] = series;
    const last = series[series.length - 1];

    if (!first || !last) {
        return "";
    }

    return STRINGS.charts.summary(
        config.label,
        config.unit,
        series.length,
        formatMetricValue(first.value, config),
        formatMetricValue(last.value, config)
    );
}
