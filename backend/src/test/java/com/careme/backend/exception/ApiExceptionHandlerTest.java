package com.careme.backend.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.careme.backend.dto.ErrorResponse;

/**
 * The upload limit is enforced before the request reaches the controller, so its
 * error contract is checked here rather than through a request.
 */
class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void reportsAFileLargerThanTheAcceptedMaximum() {
        ResponseEntity<ErrorResponse> response =
                handler.handleOversizedUpload(new MaxUploadSizeExceededException(1024));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().error().code())
                .isEqualTo(MeasurementImportException.IMPORT_TOO_LARGE);
        assertThat(response.getBody().error().details()).containsExactly("maxFileSize=10MB");
    }
}
