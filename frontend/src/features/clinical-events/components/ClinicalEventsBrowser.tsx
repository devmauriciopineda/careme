"use client";

import { useMemo, useState } from "react";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { EVENT_STRINGS } from "@/features/clinical-events/lib/strings";
import type { ClinicalEvent, EventSort, EventType } from "@/features/clinical-events/types";

const types: EventType[] = ["diagnosis", "medication", "measurement", "note"];

function formatDate(value: string | null): string {
    if (!value) return EVENT_STRINGS.unknownDate;
    return new Intl.DateTimeFormat("es", { dateStyle: "long" }).format(
        new Date(`${value}T12:00:00Z`)
    );
}

export function ClinicalEventsBrowser({ events }: { events: ClinicalEvent[] }) {
    const [type, setType] = useState<EventType | "">("");
    const [from, setFrom] = useState("");
    const [to, setTo] = useState("");
    const [sort, setSort] = useState<EventSort>("recordDate");
    const [selected, setSelected] = useState<ClinicalEvent | null>(null);

    const visibleEvents = useMemo(() => events
        .filter((event) => !type || event.type === type)
        .filter((event) => !from || (event.occurrenceDate !== null && event.occurrenceDate >= from))
        .filter((event) => !to || (event.occurrenceDate !== null && event.occurrenceDate <= to))
        .toSorted((left, right) => {
            const leftDate = sort === "recordDate" ? left.recordDate : left.occurrenceDate;
            const rightDate = sort === "recordDate" ? right.recordDate : right.occurrenceDate;
            if (leftDate === null) return 1;
            if (rightDate === null) return -1;
            return rightDate.localeCompare(leftDate) || right.code.localeCompare(left.code);
        }), [events, type, from, to, sort]);

    if (selected) {
        return (
            <section aria-labelledby="event-detail-title" className="flex flex-col gap-5">
                <Button variant="ghost" className="self-start" onClick={() => setSelected(null)}>
                    ← {EVENT_STRINGS.back}
                </Button>
                <Card>
                    <CardHeader>
                        <CardTitle id="event-detail-title">{EVENT_STRINGS.detail}</CardTitle>
                        <p className="text-sm text-muted-foreground">{selected.code} · {EVENT_STRINGS.types[selected.type]}</p>
                    </CardHeader>
                    <CardContent className="flex flex-col gap-4">
                        <p className="whitespace-pre-wrap leading-7">{selected.content}</p>
                        <dl className="grid gap-3 text-sm sm:grid-cols-2">
                            <div><dt className="text-muted-foreground">{EVENT_STRINGS.occurrence}</dt><dd>{formatDate(selected.occurrenceDate)}{selected.occurrenceDatePrecision === "approximate" && ` · ${EVENT_STRINGS.approximate}`}</dd></div>
                            <div><dt className="text-muted-foreground">{EVENT_STRINGS.recorded}</dt><dd>{formatDate(selected.recordDate)}</dd></div>
                        </dl>
                    </CardContent>
                </Card>
            </section>
        );
    }

    return (
        <div className="flex flex-col gap-6">
            <Card>
                <CardHeader><CardTitle>{EVENT_STRINGS.filters}</CardTitle></CardHeader>
                <CardContent className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
                    <label className="flex flex-col gap-1 text-sm"><span>{EVENT_STRINGS.type}</span><select value={type} onChange={(event) => setType(event.target.value as EventType | "")} className="h-9 rounded-lg border border-input bg-transparent px-2.5"><option value="">{EVENT_STRINGS.allTypes}</option>{types.map((value) => <option key={value} value={value}>{EVENT_STRINGS.types[value]}</option>)}</select></label>
                    <label className="flex flex-col gap-1 text-sm"><span>{EVENT_STRINGS.from}</span><Input type="date" value={from} onChange={(event) => setFrom(event.target.value)} /></label>
                    <label className="flex flex-col gap-1 text-sm"><span>{EVENT_STRINGS.to}</span><Input type="date" value={to} onChange={(event) => setTo(event.target.value)} /></label>
                    <label className="flex flex-col gap-1 text-sm"><span>{EVENT_STRINGS.order}</span><select value={sort} onChange={(event) => setSort(event.target.value as EventSort)} className="h-9 rounded-lg border border-input bg-transparent px-2.5"><option value="recordDate">{EVENT_STRINGS.recordDate}</option><option value="occurrenceDate">{EVENT_STRINGS.occurrenceDate}</option></select></label>
                </CardContent>
            </Card>
            {visibleEvents.length === 0 ? <p role="status" className="text-sm text-muted-foreground">{EVENT_STRINGS.empty}</p> : <div className="grid gap-3">{visibleEvents.map((event) => <button key={event.id} type="button" className="rounded-xl border bg-card p-4 text-left transition-colors hover:bg-muted" onClick={() => setSelected(event)}><div className="flex flex-wrap items-baseline justify-between gap-2"><strong>{EVENT_STRINGS.types[event.type]}</strong><span className="text-xs text-muted-foreground">{event.code}</span></div><p className="mt-2 line-clamp-2 text-sm">{event.content}</p><dl className="mt-3 grid gap-1 text-xs text-muted-foreground sm:grid-cols-2"><div><dt className="inline">{EVENT_STRINGS.occurrence}: </dt><dd className="inline">{formatDate(event.occurrenceDate)}{event.occurrenceDatePrecision === "approximate" && ` · ${EVENT_STRINGS.approximate}`}</dd></div><div><dt className="inline">{EVENT_STRINGS.recorded}: </dt><dd className="inline">{formatDate(event.recordDate)}</dd></div></dl></button>)}</div>}
        </div>
    );
}