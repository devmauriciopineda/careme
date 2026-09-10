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
import type { Measurement } from "@/features/measurements/types";

type MeasurementsTableProps = {
    measurements: Measurement[];
};

/**
 * Daily measurements, most recent first. Rendered on the server; the table
 * primitive already provides the horizontal scroll container for narrow
 * viewports.
 */
export function MeasurementsTable({ measurements }: MeasurementsTableProps) {
    const rows = sortByDateDesc(measurements);
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
