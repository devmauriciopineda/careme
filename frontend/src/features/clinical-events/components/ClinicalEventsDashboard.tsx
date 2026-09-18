import { ClinicalEventsBrowser } from "./ClinicalEventsBrowser";
import { EVENT_STRINGS } from "@/features/clinical-events/lib/strings";
import type { ClinicalEvent } from "@/features/clinical-events/types";

export function ClinicalEventsDashboard({ events }: { events: ClinicalEvent[] }) {
    return (
        <div className="flex flex-col gap-6">
            <header className="flex flex-col gap-1">
                <h1 className="font-heading text-2xl font-semibold tracking-tight sm:text-3xl">{EVENT_STRINGS.title}</h1>
                <p className="text-sm text-muted-foreground">{EVENT_STRINGS.description}</p>
            </header>
            <ClinicalEventsBrowser events={events} />
        </div>
    );
}