"use client";

import { CartesianGrid, Line, LineChart, XAxis, YAxis } from "recharts";

import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "@/components/ui/card";
import {
    ChartContainer,
    ChartTooltip,
    ChartTooltipContent,
    type ChartConfig,
} from "@/components/ui/chart";
import { formatMeasurementDate } from "@/features/measurements/lib/metrics";
import { STRINGS } from "@/features/measurements/lib/strings";
import type { TrackingMetric, TrackingMetricCatalog } from "@/features/measurements/types";

type MetricTrendChartProps = {
    metric: TrackingMetric;
    definition: TrackingMetricCatalog;
};

/**
 * Trend line for a single metric. Recharts needs the browser, so this is the
 * only client island of the dashboard; every value it receives is a primitive
 * computed on the server.
 */
export function MetricTrendChart({
    metric,
    definition,
}: MetricTrendChartProps) {
    const series = metric.measurements.map((measurement) => ({
        label: formatMeasurementDate(measurement.date),
        ...Object.fromEntries(measurement.values.map((value) => [value.component, value.value])),
    }));
    const chartConfig = Object.fromEntries(
        definition.components.map((component, index) => [
            component.key,
            { label: STRINGS.charts.series(component.key), color: `var(--chart-${(index % 5) + 1})` },
        ])
    ) satisfies ChartConfig;
    const values = metric.measurements.flatMap((measurement) =>
        measurement.values.map((value) => value.value)
    );
    const min = Math.min(...values);
    const max = Math.max(...values);
    const padding = Math.max((max - min) * 0.15, 1);
    const domain: [number, number] = [min - padding, max + padding];

    return (
        <Card>
            <CardHeader>
                <CardTitle>{metric.label}</CardTitle>
                <CardDescription>{metric.unit}</CardDescription>
            </CardHeader>
            <CardContent>
                <figure className="m-0">
                    <ChartContainer
                        config={chartConfig}
                        className="aspect-auto h-64 w-full"
                    >
                        <LineChart
                            accessibilityLayer
                            data={series}
                            margin={{ top: 8, right: 12, bottom: 0, left: 4 }}
                        >
                            <CartesianGrid vertical={false} strokeDasharray="4 4" />
                            <XAxis
                                dataKey="label"
                                tickLine={false}
                                axisLine={false}
                                tickMargin={8}
                                minTickGap={24}
                            />
                            <YAxis
                                domain={domain}
                                tickLine={false}
                                axisLine={false}
                                width={48}
                                tickFormatter={(value: number) =>
                                    value.toLocaleString("es-ES", { maximumFractionDigits: 2 })
                                }
                            />
                            <ChartTooltip
                                content={
                                    <ChartTooltipContent
                                        formatter={(value) => `${Number(value).toLocaleString("es-ES", { maximumFractionDigits: 2 })} ${metric.unit}`}
                                    />
                                }
                            />
                            {definition.components.map((component, index) => (
                                <Line
                                    key={component.key}
                                    dataKey={component.key}
                                    name={STRINGS.charts.series(component.key)}
                                    type="monotone"
                                    stroke={`var(--chart-${(index % 5) + 1})`}
                                    strokeWidth={2}
                                    dot={false}
                                />
                            ))}
                        </LineChart>
                    </ChartContainer>
                    <figcaption className="sr-only">
                        {STRINGS.charts.trackingSummary(
                            metric.label,
                            metric.unit,
                            metric.measurements.length
                        )}{" "}
                        {STRINGS.charts.keyboardHint}
                    </figcaption>
                </figure>
            </CardContent>
        </Card>
    );
}
