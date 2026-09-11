import type { Metadata } from "next";

import { MeasurementForm } from "@/features/measurements/components/MeasurementForm";
import { MeasurementImport } from "@/features/measurements/components/MeasurementImport";
import { MeasurementsDashboard } from "@/features/measurements/components/MeasurementsDashboard";
import { STRINGS } from "@/features/measurements/lib/strings";

export const metadata: Metadata = {
    title: STRINGS.app.title,
    description: STRINGS.app.description,
};

export default function Home() {
    return (
        <main className="mx-auto flex w-full max-w-6xl flex-1 flex-col gap-6 px-4 py-6 sm:px-6 lg:px-8 lg:py-10">
            <MeasurementForm />
            <MeasurementImport />
            <MeasurementsDashboard />
        </main>
    );
}
