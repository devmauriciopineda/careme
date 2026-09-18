package com.careme.backend.exception;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.careme.backend.exception.ClinicalEventInspectionException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import com.careme.backend.dto.ErrorResponse;

/**
 * Translates exceptions into the shared error envelope.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPayload(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(ApiExceptionHandler::describe)
                .toList();

        log.debug("Rejected a request with {} invalid field(s)", details.size());
        boolean chatRequest = ex.getParameter().getContainingClass().getName()
                .equals("com.careme.backend.controller.ChatController");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        chatRequest ? "El mensaje no es válido" : "Invalid measurement payload",
                        "VALIDATION_ERROR",
                        details));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadablePayload(HttpMessageNotReadableException ex) {
        log.debug("Rejected a request whose body could not be read", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        "Malformed request body",
                        "INVALID_REQUEST",
                        List.of("The request body is not valid JSON or has an unsupported format")));
    }

    /**
     * A rejected import is a bad request, not a server failure: the client gets
     * the stable code and the machine-readable details it needs to explain it.
     */
    @ExceptionHandler(MeasurementImportException.class)
    public ResponseEntity<ErrorResponse> handleRejectedImport(MeasurementImportException ex) {
        log.debug("Rejected an import with {} issue(s): {}", ex.details().size(), ex.code());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(ex.getMessage(), ex.code(), ex.details()));
    }

        @ExceptionHandler(ClinicalEventInspectionException.class)
        public ResponseEntity<ErrorResponse> handleInvalidInspectionFilter(ClinicalEventInspectionException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ErrorResponse.of(ex.getMessage(), "INVALID_FILTER", List.of()));
        }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleOversizedUpload(MaxUploadSizeExceededException ex) {
        log.debug("Rejected a file larger than the accepted maximum", ex);
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ErrorResponse.of(
                        "The sent file is larger than the accepted maximum",
                        MeasurementImportException.IMPORT_TOO_LARGE,
                        List.of("maxFileSize=10MB")));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingFilePart(MissingServletRequestPartException ex) {
        log.debug("Rejected a request that carried no file part", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        "The request carried no file",
                        MeasurementImportException.EMPTY_FILE,
                        List.of()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unexpected error while handling a request", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("Unexpected server error", "INTERNAL_ERROR", List.of()));
    }

    private static String describe(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }
}
