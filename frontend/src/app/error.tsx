"use client";

import { Button } from "@/components/ui/button";
import { STRINGS } from "@/features/measurements/lib/strings";

/**
 * Route error boundary: shown when the measurements API cannot be reached.
 */
export default function HomeError({ reset }: { reset: () => void }) {
    return (
        <main className="mx-auto flex w-full max-w-6xl flex-1 flex-col items-start gap-3 px-4 py-6 sm:px-6 lg:px-8 lg:py-10">
            <h1 className="font-heading text-2xl font-semibold tracking-tight sm:text-3xl">
                {STRINGS.errors.title}
            </h1>
            <p className="text-sm text-muted-foreground">
                {STRINGS.errors.description}
            </p>
            <Button onClick={reset}>{STRINGS.errors.retry}</Button>
        </main>
    );
}
