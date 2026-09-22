import {
    Table,
    TableBody,
    TableCaption,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table";
import {
    METRICS,
    formatMeasurementDate,
    formatMetricValue,
    sortByDateDesc,
} from "@/features/measurements/lib/metrics";
import { STRINGS } from "@/features/measurements/lib/strings";
import type {
    Measurement,
    TrackingMetric,
} from "@/features/measurements/types";

type MeasurementsTableProps = {
    measurements?: Measurement[];
    trackingMetrics?: TrackingMetric[];
};

/**
 * Daily measurements, most recent first. Rendered on the server; the table
 * primitive already provides the horizontal scroll container for narrow
 * viewports.
 */
export function MeasurementsTable({
    measurements,
    trackingMetrics = [],
}: MeasurementsTableProps) {
    if (trackingMetrics.length > 0) {
        const dates = [
            ...new Set(
                trackingMetrics.flatMap((metric) =>
                    metric.measurements.map((measurement) => measurement.date)
                )
            ),
        ].sort();

        return (
            <Table>
                <TableCaption className="sr-only">{STRINGS.table.trackingCaption}</TableCaption>
                <TableHeader>
                    <TableRow>
                        <TableHead scope="col">{STRINGS.table.date}</TableHead>
                        {trackingMetrics.map((metric) => (
                            <TableHead key={metric.code} scope="col" className="text-right">
                                {metric.label} ({metric.unit})
                            </TableHead>
                        ))}
                    </TableRow>
                </TableHeader>
                <TableBody>
                    {dates.map((date) => (
                        <TableRow key={date}>
                            <TableCell>
                                <time dateTime={date}>{formatMeasurementDate(date)}</time>
                            </TableCell>
                            {trackingMetrics.map((metric) => {
                                const measurement = metric.measurements.find(
                                    (item) => item.date === date
                                );
                                return (
                                    <TableCell key={metric.code} className="text-right tabular-nums">
                                        {measurement?.values
                                            .map(
                                                (value) =>
                                                    `${STRINGS.charts.series(value.component)}: ${value.value.toLocaleString(
                                                        "es-ES",
                                                        { maximumFractionDigits: 2 }
                                                    )}`
                                            )
                                            .join(" / ") ?? "—"}
                                    </TableCell>
                                );
                            })}
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        );
    }

    const rows = sortByDateDesc(measurements ?? []);
    const weight = METRICS.weightKg;
    const waist = METRICS.waistCm;

    return (
        <Table>
            <TableCaption className="sr-only">{STRINGS.table.caption}</TableCaption>
            <TableHeader>
                <TableRow>
                    <TableHead scope="col">{STRINGS.table.date}</TableHead>
                    <TableHead scope="col" className="text-right">
                        {STRINGS.table.weight}
                    </TableHead>
                    <TableHead scope="col" className="text-right">
                        {STRINGS.table.waist}
                    </TableHead>
                </TableRow>
            </TableHeader>
            <TableBody>
                {rows.map((measurement) => (
                    <TableRow key={measurement.id}>
                        <TableCell>
                            <time dateTime={measurement.date}>
                                {formatMeasurementDate(measurement.date)}
                            </time>
                        </TableCell>
                        <TableCell className="text-right tabular-nums">
                            {formatMetricValue(measurement.weightKg, weight)}
                        </TableCell>
                        <TableCell className="text-right tabular-nums">
                            {formatMetricValue(measurement.waistCm, waist)}
                        </TableCell>
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    );
}
