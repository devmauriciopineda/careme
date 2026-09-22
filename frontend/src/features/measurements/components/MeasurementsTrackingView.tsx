"use client";

import { useState } from "react";

import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { loadTrackingMetric } from "@/features/measurements/actions";
import { STRINGS } from "@/features/measurements/lib/strings";
import {
    availableMetrics,
    defaultSelection,
    type MetricStates,
} from "@/features/measurements/lib/tracking";
import type { TrackingMetricCatalog } from "@/features/measurements/types";

import { MetricTrendChart } from "./MetricTrendChart";
import { MeasurementsTable } from "./MeasurementsTable";

type MeasurementsTrackingViewProps = {
    catalog: TrackingMetricCatalog[];
    /** Read state of every metric, already resolved on the server. */
    states: MetricStates;
};

/**
 * Tracking view: a metric selector, one trend chart per selected metric and a
 * single detail table with a column per selected metric.
 *
 * It is the client island of the dashboard because the selection and the
 * per-metric retry need interactivity; the data itself arrives resolved, and the
 * retry goes back through the server action so the browser never needs the API
 * address.
 */
export function MeasurementsTrackingView({
    catalog,
    states: initialState,
}: MeasurementsTrackingViewProps) {
    const [states, setStates] = useState(initialState);
    const [selected, setSelected] = useState(() =>
        defaultSelection(catalog, initialState)
    );
    const [retrying, setRetrying] = useState<string | null>(null);

    const available = availableMetrics(catalog, states);
    const failed = Object.entries(states).filter(
        ([, state]) => state.status === "error" || state.status === "loading"
    );
    const selectedMetrics = selected.flatMap((code) => {
        const state = states[code];
        return state && "data" in state ? [state.data] : [];
    });

    async function retry(code: string) {
        setRetrying(code);
        setStates((current) => ({ ...current, [code]: { status: "loading" } }));
        const state = await loadTrackingMetric(code);
        setStates((current) => ({ ...current, [code]: state }));
        setRetrying(null);
    }

    return (
        <div className="flex flex-col gap-6">
            <header className="flex flex-col gap-1">
                <h1 className="font-heading text-2xl font-semibold tracking-tight sm:text-3xl">
                    {STRINGS.app.title}
                </h1>
                <p className="text-sm text-muted-foreground">
                    {STRINGS.app.description}
                </p>
            </header>

            {available.length > 0 && (
                <fieldset className="flex flex-col gap-3">
                    <legend className="font-medium">
                        {STRINGS.dashboard.metricSelector}
                    </legend>
                    <div className="flex flex-wrap gap-4">
                        {available.map((entry) => (
                            <label
                                key={entry.code}
                                className="inline-flex items-center gap-2 text-sm"
                            >
                                <input
                                    type="checkbox"
                                    checked={selected.includes(entry.code)}
                                    onChange={() =>
                                        setSelected((current) =>
                                            current.includes(entry.code)
                                                ? current.filter((code) => code !== entry.code)
                                                : [...current, entry.code]
                                        )
                                    }
                                />
                                <span>
                                    {entry.label} ({entry.referenceUnit})
                                </span>
                            </label>
                        ))}
                    </div>
                </fieldset>
            )}

            {failed.map(([code, state]) => {
                const label =
                    catalog.find((entry) => entry.code === code)?.label ?? code;

                if (state.status === "loading") {
                    return (
                        <p
                            key={code}
                            role="status"
                            className="text-sm text-muted-foreground"
                        >
                            {STRINGS.dashboard.loadingMetric(label)}
                        </p>
                    );
                }

                return (
                    <div
                        key={code}
                        role="alert"
                        className="flex items-center gap-3 text-sm text-destructive"
                    >
                        <span>{STRINGS.dashboard.metricError(label)}</span>
                        <Button
                            type="button"
                            variant="outline"
                            size="sm"
                            disabled={retrying === code}
                            onClick={() => retry(code)}
                        >
                            {STRINGS.dashboard.retryMetric}
                        </Button>
                    </div>
                );
            })}

            {available.length === 0 && failed.length === 0 ? (
                <p role="status" className="text-sm text-muted-foreground">
                    {STRINGS.dashboard.empty}
                </p>
            ) : selectedMetrics.length === 0 ? (
                <p role="status" className="text-sm text-muted-foreground">
                    {STRINGS.dashboard.noSelection}
                </p>
            ) : (
                <>
                    <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
                        {selectedMetrics.map((metric) => {
                            const definition = catalog.find(
                                (entry) => entry.code === metric.code
                            );
                            return definition ? (
                                <MetricTrendChart
                                    key={metric.code}
                                    metric={metric}
                                    definition={definition}
                                />
                            ) : null;
                        })}
                    </div>
                    <Separator />
                    <section
                        aria-labelledby="measurements-table-heading"
                        className="flex flex-col gap-3"
                    >
                        <h2
                            id="measurements-table-heading"
                            className="font-heading text-lg font-medium"
                        >
                            {STRINGS.table.title}
                        </h2>
                        <MeasurementsTable trackingMetrics={selectedMetrics} />
                    </section>
                </>
            )}
        </div>
    );
}
