import type { Metadata } from "next";

import { ClinicalEventsDashboard } from "@/features/clinical-events/components/ClinicalEventsDashboard";
import { clinicalEventService } from "@/services/clinicalEventService";

export const metadata: Metadata = {
    title: "Historia clínica | Careme",
    description: "Consulta de hechos clínicos registrados",
};

export default async function ClinicalEventsPage() {
    const events = await clinicalEventService.getEvents();
    return <main className="mx-auto flex w-full max-w-6xl flex-1 flex-col px-4 py-6 sm:px-6 lg:px-8 lg:py-10"><ClinicalEventsDashboard events={events} /></main>;
}