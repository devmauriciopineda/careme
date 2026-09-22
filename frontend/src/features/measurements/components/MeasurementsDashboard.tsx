import {
    metricStateOf,
    type MetricState,
    type MetricStates,
} from "@/features/measurements/lib/tracking";
import { measurementService } from "@/services/measurementService";

import { MeasurementsTrackingView } from "./MeasurementsTrackingView";

/**
 * Server Component: reads the admitted catalogue and one payload per metric at
 * request time, so the browser never needs the backend address.
 *
 * A metric that cannot be read becomes an isolated error state instead of
 * failing the page; only the catalogue read, which everything else depends on,
 * is allowed to reach the route error boundary.
 */
export async function MeasurementsDashboard() {
    const catalog = await measurementService.getTrackingCatalog();

    const states: MetricStates = Object.fromEntries(
        await Promise.all(
            catalog.map(async (entry): Promise<[string, MetricState]> => {
                try {
                    return [
                        entry.code,
                        metricStateOf(
                            await measurementService.getTrackingMetric(entry.code)
                        ),
                    ];
                } catch (error) {
                    console.error(`Failed to load metric ${entry.code}:`, error);
                    return [entry.code, { status: "error" }];
                }
            })
        )
    );

    return <MeasurementsTrackingView catalog={catalog} states={states} />;
}
