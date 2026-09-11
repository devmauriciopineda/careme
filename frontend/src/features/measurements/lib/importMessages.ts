import { STRINGS } from "./strings";

/**
 * Turns the backend's rejection of a file into the sentence the user reads.
 *
 * The backend never writes user-facing prose: it reports a stable code and, for
 * a row, a machine-readable `line|field|reason`. The wording lives here, next to
 * the rest of the copy, so the two stay in step.
 */

/** Column codes the backend reports, named the way the user knows them. */
const FIELD_LABELS: Record<string, string> = {
    date: STRINGS.form.date,
    weight_kg: STRINGS.metrics.weightLabel,
    abdominal_circumference_cm: STRINGS.metrics.waistLabel,
};

/** Reasons the backend reports, said the way the user understands them. */
const REASON_TEXT: Record<string, string> = STRINGS.import.reasons;

function describeIssue(detail: string): string | null {
    const [line, field, reason] = detail.split("|");
    const lineNumber = Number(line);

    if (!Number.isInteger(lineNumber) || lineNumber < 1) {
        return null;
    }

    return STRINGS.import.rowIssue(
        lineNumber,
        FIELD_LABELS[field] ?? field,
        REASON_TEXT[reason] ?? STRINGS.import.reasons.unknown
    );
}

/** Every rejection is announced the same way: nothing was stored. */
function rejection(message: string): string {
    return [STRINGS.import.errors.header, message].join("\n");
}

export function describeImportError(
    code: string,
    details: readonly string[]
): string {
    if (code === "INVALID_IMPORT_VALUE") {
        const issues = details
            .map(describeIssue)
            .filter((issue): issue is string => issue !== null);

        if (issues.length === 0) {
            return rejection(STRINGS.import.errors.unknown);
        }

        return [
            STRINGS.import.errors.header,
            STRINGS.import.errors.invalidValues(issues.length),
            ...issues,
        ].join("\n");
    }

    switch (code) {
        case "INVALID_FILE_STRUCTURE":
            return rejection(STRINGS.import.errors.invalidStructure);
        case "IMPORT_TOO_LARGE":
            return rejection(STRINGS.import.errors.tooLarge);
        case "EMPTY_FILE":
            return rejection(STRINGS.import.errors.empty);
        case "UNREADABLE_FILE":
            return rejection(STRINGS.import.errors.unreadable);
        default:
            return rejection(STRINGS.import.errors.unknown);
    }
}
