"use client";

import { Button } from "@/components/ui/button";
import { EVENT_STRINGS } from "@/features/clinical-events/lib/strings";

export default function Error({ reset }: { error: Error & { digest?: string }; reset: () => void }) {
    return <main className="mx-auto flex w-full max-w-6xl flex-1 flex-col gap-4 px-4 py-10 sm:px-6 lg:px-8"><h1 className="font-heading text-2xl font-semibold">{EVENT_STRINGS.failure}</h1><p role="alert" className="text-sm text-muted-foreground">No pudimos conectar con el servidor. Inténtalo de nuevo.</p><Button onClick={reset}>{EVENT_STRINGS.retry}</Button></main>;
}