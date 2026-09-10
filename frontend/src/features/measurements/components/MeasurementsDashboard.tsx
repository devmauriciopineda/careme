import { MetricTrendChart } from "./MetricTrendChart";
import { MeasurementsTable } from "./MeasurementsTable";

import { Separator } from "@/components/ui/separator";
import {
    METRICS,
    METRIC_KEYS,
    buildSeries,
    computeDateRange,
    computeDomain,
} from "@/features/measurements/lib/metrics";
import { STRINGS } from "@/features/measurements/lib/strings";
import type { MetricKey } from "@/features/measurements/types";
import { measurementService } from "@/services/measurementService";

const CHART_DESCRIPTIONS: Record<MetricKey, string> = {
    weightKg: STRINGS.charts.weightDescription,
    waistCm: STRINGS.charts.waistDescription,
};

/**
 * Server Component: loads the measurements once and derives every chart series
 * and axis domain here, so the client only renders primitives.
 */
export async function MeasurementsDashboard() {
    const measurements = await measurementService.getMeasurements();

    if (measurements.length === 0) {
        return (
            <p role="status" className="text-sm text-muted-foreground">
                {STRINGS.dashboard.empty}
            </p>
        );
    }

    const charts = METRIC_KEYS.map((metric) => {
        const series = buildSeries(measurements, metric);

        return {
            metric,
            series,
            domain: computeDomain(
                series.map((point) => point.value),
                METRICS[metric].domainPaddingRatio
            ),
        };
    });

    const range = computeDateRange(measurements);

    return (
        <div className="flex flex-col gap-6">
            <header className="flex flex-col gap-1">
                <h1 className="font-heading text-2xl font-semibold tracking-tight sm:text-3xl">
                    {STRINGS.app.title}
                </h1>
                <p className="text-sm text-muted-foreground">
                    {STRINGS.app.description}
                </p>
                <p className="text-sm text-muted-foreground">
                    {STRINGS.dashboard.dateRange(range.from, range.to)} ·{" "}
                    {STRINGS.dashboard.registeredCount(measurements.length)}
                </p>
            </header>

            <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
                {charts.map(({ metric, series, domain }) => (
                    <MetricTrendChart
                        key={metric}
                        metric={metric}
                        series={series}
                        domain={domain}
                        description={CHART_DESCRIPTIONS[metric]}
                    />
                ))}
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
                <MeasurementsTable measurements={measurements} />
            </section>
        </div>
    );
}
