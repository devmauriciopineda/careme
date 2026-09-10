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
import {
    METRICS,
    buildChartSummary,
    formatMetricValue,
} from "@/features/measurements/lib/metrics";
import { STRINGS } from "@/features/measurements/lib/strings";
import type { MetricKey, SeriesPoint } from "@/features/measurements/types";

type MetricTrendChartProps = {
    metric: MetricKey;
    /** Points already sorted and localized on the server. */
    series: SeriesPoint[];
    domain: [number, number];
    description: string;
};

/**
 * Trend line for a single metric. Recharts needs the browser, so this is the
 * only client island of the dashboard; every value it receives is a primitive
 * computed on the server.
 */
export function MetricTrendChart({
    metric,
    series,
    domain,
    description,
}: MetricTrendChartProps) {
    const config = METRICS[metric];
    const chartConfig = {
        value: { label: config.label, color: config.colorVar },
    } satisfies ChartConfig;
    const summary = buildChartSummary(series, config);

    return (
        <Card>
            <CardHeader>
                <CardTitle>{config.label}</CardTitle>
                <CardDescription>{description}</CardDescription>
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
                                    formatMetricValue(value, config)
                                }
                            />
                            <ChartTooltip
                                content={
                                    <ChartTooltipContent
                                        formatter={(value) =>
                                            `${formatMetricValue(Number(value), config)} ${config.unit}`
                                        }
                                    />
                                }
                            />
                            <Line
                                dataKey="value"
                                name={config.label}
                                type="monotone"
                                stroke="var(--color-value)"
                                strokeWidth={2}
                                dot={false}
                            />
                        </LineChart>
                    </ChartContainer>
                    <figcaption className="sr-only">
                        {summary} {STRINGS.charts.keyboardHint}
                    </figcaption>
                </figure>
            </CardContent>
        </Card>
    );
}
