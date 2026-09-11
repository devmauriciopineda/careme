package com.careme.backend.dto;

import java.util.List;

/**
 * What loading a file would do, without doing it.
 *
 * @param rows          the measurements the file would load, deduplicated by date
 * @param totalRows     data rows read from the file
 * @param newCount      rows that would create a measurement
 * @param replacedCount rows that would replace the values of an existing day
 * @param ignoredCount  rows the file may contain without a value for any day
 */
public record ImportPreviewResponse(
        List<ImportPreviewRow> rows,
        int totalRows,
        int newCount,
        int replacedCount,
        int ignoredCount) {
}
