package com.careme.backend.exception;

import java.util.List;

/**
 * Rejection of a whole import, before anything is stored.
 *
 * <p>Carries a stable {@link #code()} so the client can choose the wording, and
 * machine-readable {@link #details()} instead of prose. A row-level detail is
 * formatted as {@code line|field|reason} (for example {@code 4|weight_kg|NOT_POSITIVE}),
 * so the client can name the exact line and reason in the user's language
 * without the server ever writing a user-facing sentence.
 */
public class MeasurementImportException extends RuntimeException {

    /** The file does not carry the required columns or cannot be read as a table. */
    public static final String INVALID_FILE_STRUCTURE = "INVALID_FILE_STRUCTURE";
    /** At least one row holds a value that is not a valid measurement. */
    public static final String INVALID_IMPORT_VALUE = "INVALID_IMPORT_VALUE";
    /** The file carries more data rows than the accepted maximum. */
    public static final String IMPORT_TOO_LARGE = "IMPORT_TOO_LARGE";
    /** No file was sent, or the sent one had no content. */
    public static final String EMPTY_FILE = "EMPTY_FILE";
    /** The file could not be read while it was being parsed. */
    public static final String UNREADABLE_FILE = "UNREADABLE_FILE";

    private final String code;
    private final List<String> details;

    public MeasurementImportException(String code, String message, List<String> details) {
        super(message);
        this.code = code;
        this.details = List.copyOf(details);
    }

    public String code() {
        return code;
    }

    public List<String> details() {
        return details;
    }
}
