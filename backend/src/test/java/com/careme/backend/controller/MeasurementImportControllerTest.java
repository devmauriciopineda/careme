package com.careme.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import com.careme.backend.dto.ImportPreviewResponse;
import com.careme.backend.dto.ImportPreviewRow;
import com.careme.backend.dto.ImportResultResponse;
import com.careme.backend.exception.MeasurementImportException;
import com.careme.backend.service.MeasurementImportService;
import com.careme.backend.service.MeasurementService;
import com.careme.backend.service.MetricTrackingService;

@WebMvcTest(MeasurementController.class)
@TestPropertySource(properties = "careme.cors.allowed-origins=http://localhost:3000")
class MeasurementImportControllerTest {

    private static final String PREVIEW_URL = "/api/v1/measurements/import/preview";
    private static final String IMPORT_URL = "/api/v1/measurements/import";
    private static final String FILE_CONTENT = """
            date,weight_kg,abdominal_circumference_cm
            2026-09-10,80.1,94.8
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MeasurementService measurementService;

    @MockitoBean
    private MeasurementImportService measurementImportService;

        @MockitoBean
        private MetricTrackingService metricTrackingService;

    private static MockMultipartFile file(String content) {
        return new MockMultipartFile(
                "file", "measurements.csv", "text/csv", content.getBytes(StandardCharsets.UTF_8));
    }

    /** A part that is present and non-empty but cannot be opened. */
    private static MultipartFile unreadableFile() {
        return new MockMultipartFile(
                "file", "measurements.csv", "text/csv", FILE_CONTENT.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public InputStream getInputStream() throws IOException {
                throw new IOException("the connection dropped");
            }
        };
    }

    private MeasurementController controller() {
        return new MeasurementController(measurementService, measurementImportService);
    }

    @Test
    void rejectsARequestWithoutAFilePartAtAll() {
        MeasurementImportException rejection = catchThrowableOfType(
                () -> controller().importMeasurements(null), MeasurementImportException.class);

        assertThat(rejection.code()).isEqualTo(MeasurementImportException.EMPTY_FILE);
    }

    @Test
    void rejectsAFileThatCannotBeOpened() {
        MeasurementImportException rejection = catchThrowableOfType(
                () -> controller().previewImport(unreadableFile()), MeasurementImportException.class);

        assertThat(rejection.code()).isEqualTo(MeasurementImportException.UNREADABLE_FILE);
    }

    @Test
    void reportsWhatLoadingAFileWouldDo() throws Exception {
        when(measurementImportService.preview(any(InputStream.class))).thenReturn(
                new ImportPreviewResponse(
                        List.of(new ImportPreviewRow(
                                LocalDate.of(2026, 9, 10),
                                new BigDecimal("80.1"),
                                new BigDecimal("94.8"),
                                true)),
                        1,
                        0,
                        1,
                        0));

        mockMvc.perform(multipart(PREVIEW_URL).file(file(FILE_CONTENT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalRows").value(1))
                .andExpect(jsonPath("$.data.newCount").value(0))
                .andExpect(jsonPath("$.data.replacedCount").value(1))
                .andExpect(jsonPath("$.data.ignoredCount").value(0))
                .andExpect(jsonPath("$.data.rows[0].date").value("2026-09-10"))
                .andExpect(jsonPath("$.data.rows[0].weightKg").value(80.1))
                .andExpect(jsonPath("$.data.rows[0].waistCm").value(94.8))
                .andExpect(jsonPath("$.data.rows[0].replacesExisting").value(true));
    }

    @Test
    void reportsWhatLoadingAFileDid() throws Exception {
        when(measurementImportService.importMeasurements(any(InputStream.class))).thenReturn(
                new ImportResultResponse(1, 1, 0, 2));

        mockMvc.perform(multipart(IMPORT_URL).file(file(FILE_CONTENT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.createdCount").value(1))
                .andExpect(jsonPath("$.data.replacedCount").value(1))
                .andExpect(jsonPath("$.data.ignoredCount").value(0))
                .andExpect(jsonPath("$.data.totalRows").value(2));
    }

    @Test
    void rejectsAFileWithoutContent() throws Exception {
        mockMvc.perform(multipart(IMPORT_URL)
                .file(new MockMultipartFile("file", "measurements.csv", "text/csv", new byte[0])))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(MeasurementImportException.EMPTY_FILE));

        verify(measurementImportService, never()).importMeasurements(any());
    }

    @Test
    void rejectsARequestThatCarriesNoFile() throws Exception {
        mockMvc.perform(multipart(IMPORT_URL))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(MeasurementImportException.EMPTY_FILE));
    }

    @Test
    void reportsARejectedFileWithItsCodeAndDetails() throws Exception {
        when(measurementImportService.preview(any(InputStream.class))).thenThrow(
                new MeasurementImportException(
                        MeasurementImportException.INVALID_IMPORT_VALUE,
                        "The file contains rows that are not valid measurements",
                        List.of("2|weight_kg|NOT_POSITIVE")));

        mockMvc.perform(multipart(PREVIEW_URL).file(file(FILE_CONTENT)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_IMPORT_VALUE"))
                .andExpect(jsonPath("$.error.details[0]").value("2|weight_kg|NOT_POSITIVE"));
    }

    @Test
    void reportsAFileWithTheWrongColumns() throws Exception {
        when(measurementImportService.importMeasurements(any(InputStream.class))).thenThrow(
                new MeasurementImportException(
                        MeasurementImportException.INVALID_FILE_STRUCTURE,
                        "The file does not carry exactly the required columns",
                        List.of("expected=date,weight_kg,abdominal_circumference_cm", "found=date")));

        mockMvc.perform(multipart(IMPORT_URL).file(file("date\n2026-09-10\n")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_FILE_STRUCTURE"))
                .andExpect(jsonPath("$.error.details[0]")
                        .value("expected=date,weight_kg,abdominal_circumference_cm"));
    }
}
