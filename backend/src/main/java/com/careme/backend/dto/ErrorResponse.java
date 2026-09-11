package com.careme.backend.dto;

import java.util.List;

/**
 * Error envelope returned by {@link com.careme.backend.exception.ApiExceptionHandler}.
 */
public record ErrorResponse(boolean success, ErrorDetail error) {

    /**
     * @param message human-readable description
     * @param code    stable machine-readable code
     * @param details optional field-level details
     */
    public record ErrorDetail(String message, String code, List<String> details) {
    }

    public static ErrorResponse of(String message, String code, List<String> details) {
        return new ErrorResponse(false, new ErrorDetail(message, code, List.copyOf(details)));
    }
}
