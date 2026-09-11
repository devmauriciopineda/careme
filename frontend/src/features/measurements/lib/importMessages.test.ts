import { describe, expect, it } from "vitest";

import { describeImportError } from "./importMessages";
import { STRINGS } from "./strings";

describe("describeImportError", () => {
    it("names the line, the field and the reason of every rejected row", () => {
        const message = describeImportError("INVALID_IMPORT_VALUE", [
            "4|weight_kg|NOT_POSITIVE",
        ]);

        expect(message).toContain(STRINGS.import.errors.header);
        expect(message).toContain(
            STRINGS.import.errors.invalidValues(1)
        );
        expect(message).toContain(
            STRINGS.import.rowIssue(
                4,
                STRINGS.metrics.weightLabel,
                STRINGS.import.reasons.NOT_POSITIVE
            )
        );
    });

    it("reports every rejected row it was given", () => {
        const message = describeImportError("INVALID_IMPORT_VALUE", [
            "2|date|INVALID_DATE",
            "3|abdominal_circumference_cm|MISSING_VALUE",
        ]);

        expect(message).toContain(
            STRINGS.import.errors.invalidValues(2)
        );
        expect(message).toContain("Fila 2");
        expect(message).toContain("Fila 3");
    });

    it("falls back to a generic wording when the details cannot be read", () => {
        expect(describeImportError("INVALID_IMPORT_VALUE", ["not-a-detail"])).toBe(
            `${STRINGS.import.errors.header}\n${STRINGS.import.errors.unknown}`
        );
    });

    it("keeps an unknown column and an unknown reason legible", () => {
        const message = describeImportError("INVALID_IMPORT_VALUE", [
            "2|height_cm|TOO_TALL",
        ]);

        expect(message).toContain("height_cm");
        expect(message).toContain(STRINGS.import.reasons.unknown);
    });

    it("explains a file whose columns are wrong", () => {
        expect(describeImportError("INVALID_FILE_STRUCTURE", [])).toBe(
            `${STRINGS.import.errors.header}\n${STRINGS.import.errors.invalidStructure}`
        );
    });

    it("explains a file over the accepted limits", () => {
        expect(describeImportError("IMPORT_TOO_LARGE", [])).toContain(
            STRINGS.import.errors.tooLarge
        );
    });

    it("explains an empty file", () => {
        expect(describeImportError("EMPTY_FILE", [])).toContain(
            STRINGS.import.errors.empty
        );
    });

    it("explains a file that could not be read", () => {
        expect(describeImportError("UNREADABLE_FILE", [])).toContain(
            STRINGS.import.errors.unreadable
        );
    });

    it("explains an unrecognised rejection", () => {
        expect(describeImportError("SOMETHING_ELSE", [])).toContain(
            STRINGS.import.errors.unknown
        );
    });
});
