"use client";

import { useId, useRef, useState, useTransition } from "react";
import type { ChangeEvent } from "react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
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
    confirmMeasurementImport,
    previewMeasurementImport,
} from "@/features/measurements/actions";
import { describeImportError } from "@/features/measurements/lib/importMessages";
import { STRINGS } from "@/features/measurements/lib/strings";
import type { ImportPreview, ImportResult } from "@/features/measurements/types";

/** A file above this size is refused before it is uploaded. */
const MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024;

/** A long file is summarised: the user checks the shape, not every row. */
const MAX_PREVIEW_ROWS = 50;

/**
 * Loading several measurements from a file.
 *
 * The flow is always the same: choose, check, confirm. Checking and confirming
 * run the same validation on the server, and a rejected file leaves the stored
 * measurements exactly as they were, so the preview never has to be trusted
 * blindly. The outcome is announced in place, without interrupting the user.
 */
export function MeasurementImport() {
    const baseId = useId();
    const fileInputRef = useRef<HTMLInputElement>(null);
    const [file, setFile] = useState<File | null>(null);
    const [preview, setPreview] = useState<ImportPreview | null>(null);
    const [result, setResult] = useState<ImportResult | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [isPending, startTransition] = useTransition();

    const fileId = `${baseId}-file`;
    const fileHintId = `${baseId}-file-hint`;
    const rowsHeadingId = `${baseId}-rows-heading`;

    const isChecking = isPending && preview === null;
    const statusMessage = isPending
        ? isChecking
            ? STRINGS.import.previewing
            : STRINGS.import.importing
        : null;

    function clearInput() {
        if (fileInputRef.current !== null) {
            fileInputRef.current.value = "";
        }
    }

    function forgetOutcome() {
        setPreview(null);
        setResult(null);
        setError(null);
    }

    function handleFileChange(event: ChangeEvent<HTMLInputElement>) {
        const chosen = event.target.files?.[0] ?? null;

        forgetOutcome();

        if (chosen !== null && chosen.size > MAX_FILE_SIZE_BYTES) {
            setFile(null);
            setError(STRINGS.import.fileTooLarge);
            clearInput();
            return;
        }

        setFile(chosen);
    }

    function handlePreview() {
        if (file === null || isPending) {
            return;
        }

        const body = new FormData();
        body.append("file", file);
        forgetOutcome();

        startTransition(async () => {
            const outcome = await previewMeasurementImport(body);

            if (outcome.ok) {
                setPreview(outcome.preview);
                return;
            }

            setError(describeImportError(outcome.error.code, outcome.error.details));
        });
    }

    function handleConfirm() {
        if (file === null || isPending) {
            return;
        }

        const body = new FormData();
        body.append("file", file);

        startTransition(async () => {
            const outcome = await confirmMeasurementImport(body);

            if (!outcome.ok) {
                setError(describeImportError(outcome.error.code, outcome.error.details));
                return;
            }

            setPreview(null);
            setError(null);
            setResult(outcome.result);
            setFile(null);
            clearInput();
        });
    }

    function handleDiscard() {
        forgetOutcome();

        setFile(null);
        clearInput();
    }

    return (
        <section
            aria-labelledby={`${baseId}-heading`}
            className="flex flex-col gap-3 rounded-xl bg-card p-4 ring-1 ring-foreground/10 sm:p-5"
        >
            <div className="flex flex-col gap-1">
                <h2
                    id={`${baseId}-heading`}
                    className="font-heading text-lg font-medium"
                >
                    {STRINGS.import.title}
                </h2>
                <p className="text-sm text-muted-foreground">
                    {STRINGS.import.description}
                </p>
            </div>

            <div className="flex flex-col gap-1.5">
                <Label htmlFor={fileId}>{STRINGS.import.file}</Label>
                <Input
                    id={fileId}
                    name="file"
                    type="file"
                    accept=".csv,text/csv"
                    ref={fileInputRef}
                    disabled={isPending}
                    onChange={handleFileChange}
                    aria-describedby={file ? fileHintId : undefined}
                />
                {file ? (
                    <p
                        id={fileHintId}
                        className="text-sm text-muted-foreground"
                    >
                        {STRINGS.import.selected(file.name)}
                    </p>
                ) : null}
            </div>

            <div className="flex flex-wrap items-center gap-2">
                <Button
                    type="button"
                    onClick={handlePreview}
                    disabled={file === null || isPending}
                >
                    {isChecking ? STRINGS.import.previewing : STRINGS.import.preview}
                </Button>

                {preview ? (
                    <>
                        <Button
                            type="button"
                            onClick={handleConfirm}
                            disabled={isPending}
                        >
                            {isPending
                                ? STRINGS.import.importing
                                : STRINGS.import.confirm}
                        </Button>
                        <Button
                            type="button"
                            variant="ghost"
                            onClick={handleDiscard}
                            disabled={isPending}
                        >
                            {STRINGS.import.discard}
                        </Button>
                    </>
                ) : null}
            </div>

            <div aria-live="polite" className="min-h-5 text-sm">
                {statusMessage !== null ? (
                    <p role="status" className="text-muted-foreground">
                        {statusMessage}
                    </p>
                ) : null}

                {statusMessage === null && result !== null ? (
                    <p role="status" className="text-foreground">
                        {STRINGS.import.result(
                            result.createdCount,
                            result.replacedCount,
                            result.ignoredCount
                        )}
                    </p>
                ) : null}

                {error !== null ? (
                    <p
                        role="alert"
                        className="whitespace-pre-line text-destructive"
                    >
                        {error}
                    </p>
                ) : null}
            </div>

            {preview !== null ? (
                <div className="flex flex-col gap-3">
                    <p className="text-sm text-muted-foreground">
                        {STRINGS.import.previewSummary(
                            preview.totalRows,
                            preview.newCount,
                            preview.replacedCount,
                            preview.ignoredCount
                        )}
                    </p>

                    {preview.rows.length === 0 ? (
                        <p role="status" className="text-sm text-muted-foreground">
                            {STRINGS.import.noRows}
                        </p>
                    ) : (
                        <section
                            aria-labelledby={rowsHeadingId}
                            className="flex flex-col gap-2"
                        >
                            <h3
                                id={rowsHeadingId}
                                className="font-heading text-base font-medium"
                            >
                                {STRINGS.import.previewRowsTitle}
                            </h3>
                            <Table>
                                <TableCaption>
                                    {preview.rows.length > MAX_PREVIEW_ROWS
                                        ? `${STRINGS.import.previewRowsCaption} ${STRINGS.import.previewTruncated(
                                              MAX_PREVIEW_ROWS,
                                              preview.rows.length
                                          )}`
                                        : STRINGS.import.previewRowsCaption}
                                </TableCaption>
                                <TableHeader>
                                    <TableRow>
                                        <TableHead scope="col">
                                            {STRINGS.import.tableDate}
                                        </TableHead>
                                        <TableHead scope="col">
                                            {STRINGS.import.tableWeight}
                                        </TableHead>
                                        <TableHead scope="col">
                                            {STRINGS.import.tableWaist}
                                        </TableHead>
                                        <TableHead scope="col">
                                            {STRINGS.import.tableEffect}
                                        </TableHead>
                                    </TableRow>
                                </TableHeader>
                                <TableBody>
                                    {preview.rows
                                        .slice(0, MAX_PREVIEW_ROWS)
                                        .map((row) => (
                                            <TableRow key={row.date}>
                                                <TableCell>{row.date}</TableCell>
                                                <TableCell>
                                                    {row.weightKg}
                                                </TableCell>
                                                <TableCell>
                                                    {row.waistCm}
                                                </TableCell>
                                                <TableCell>
                                                    {row.replacesExisting
                                                        ? STRINGS.import
                                                              .effectReplaced
                                                        : STRINGS.import.effectNew}
                                                </TableCell>
                                            </TableRow>
                                        ))}
                                </TableBody>
                            </Table>
                        </section>
                    )}
                </div>
            ) : null}
        </section>
    );
}
