import type { TrackingMetric, TrackingMetricCatalog } from "../types";

/**
 * Read state of one metric in the tracking view.
 *
 * The server resolves the first read of every metric before the view renders, so
 * `loading` only appears while a retry of that metric is in flight. `error` is
 * per metric on purpose: one failed read must never hide the metrics that did
 * load, and must never be presented as an empty tracking.
 */
export type MetricState =
    | { status: "loading" }
    | { status: "success"; data: TrackingMetric }
    | { status: "empty"; data: TrackingMetric }
    | { status: "error" };

/** Read state of every metric of the catalogue, keyed by metric code. */
export type MetricStates = Record<string, MetricState>;

/** Metrics selected on the first render, in this order, when they have data. */
export const PREFERRED_CODES: readonly string[] = ["weight", "waist"];

/** The state a successful read produces: data, or the absence of measurements. */
export function metricStateOf(data: TrackingMetric): MetricState {
    return data.measurements.length > 0
        ? { status: "success", data }
        : { status: "empty", data };
}

/** The metrics the person may select: those with at least one measurement. */
export function availableMetrics(
    catalog: readonly TrackingMetricCatalog[],
    states: MetricStates
): TrackingMetricCatalog[] {
    return catalog.filter((entry) => states[entry.code]?.status === "success");
}

/**
 * The selection the view starts with: weight and abdominal circumference when
 * they have measurements, otherwise every metric that has them.
 */
export function defaultSelection(
    catalog: readonly TrackingMetricCatalog[],
    states: MetricStates
): string[] {
    const available = availableMetrics(catalog, states).map((entry) => entry.code);
    const preferred = available.filter((code) => PREFERRED_CODES.includes(code));

    return preferred.length > 0 ? preferred : available;
}
