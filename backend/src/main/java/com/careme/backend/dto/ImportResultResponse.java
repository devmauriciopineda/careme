package com.careme.backend.dto;

/**
 * Outcome of loading a file.
 *
 * @param createdCount  measurements stored for a day that had none
 * @param replacedCount measurements whose day already had one, replaced in place
 * @param ignoredCount  rows the file carried without a value for any day
 * @param totalRows     data rows read from the file
 */
public record ImportResultResponse(
        int createdCount,
        int replacedCount,
        int ignoredCount,
        int totalRows) {
}
