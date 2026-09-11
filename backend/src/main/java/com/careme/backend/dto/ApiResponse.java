package com.careme.backend.dto;

/**
 * Response envelope shared by every endpoint.
 *
 * @param success     whether the operation completed
 * @param data        payload, {@code null} when the operation failed
 * @param messageCode stable machine-readable code
 * @param message     human-readable message
 * @param <T>         payload type
 */
public record ApiResponse<T>(boolean success, T data, String messageCode, String message) {

    private static final String SUCCESS_CODE = "SUCCESS";
    private static final String SUCCESS_MESSAGE = "Operation completed successfully";

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, SUCCESS_CODE, SUCCESS_MESSAGE);
    }
}
