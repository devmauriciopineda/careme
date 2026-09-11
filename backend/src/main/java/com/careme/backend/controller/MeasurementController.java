package com.careme.backend.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.careme.backend.dto.ApiResponse;
import com.careme.backend.dto.ImportPreviewResponse;
import com.careme.backend.dto.ImportResultResponse;
import com.careme.backend.dto.MeasurementRequest;
import com.careme.backend.dto.MeasurementResponse;
import com.careme.backend.exception.MeasurementImportException;
import com.careme.backend.service.MeasurementImportService;
import com.careme.backend.service.MeasurementService;

import jakarta.validation.Valid;

/**
 * HTTP boundary for body measurements. Delegates every decision to the service.
 */
@RestController
@RequestMapping("/api/v1/measurements")
public class MeasurementController {

    private final MeasurementService measurementService;
    private final MeasurementImportService measurementImportService;

    public MeasurementController(
            MeasurementService measurementService,
            MeasurementImportService measurementImportService) {
        this.measurementService = measurementService;
        this.measurementImportService = measurementImportService;
    }

    @GetMapping
    public ApiResponse<List<MeasurementResponse>> findAll() {
        return ApiResponse.ok(measurementService.findAll());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MeasurementResponse>> register(
            @Valid @RequestBody MeasurementRequest request) {
        MeasurementResponse saved = measurementService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(saved));
    }

    /**
     * Checks a file and reports what loading it would do, without storing
     * anything.
     */
    @PostMapping(path = "/import/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ImportPreviewResponse> previewImport(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(measurementImportService.preview(contentOf(file)));
    }

    /**
     * Loads every measurement of a file, replacing the values of the days that
     * already had one.
     */
    @PostMapping(path = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ImportResultResponse> importMeasurements(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(measurementImportService.importMeasurements(contentOf(file)));
    }

    private static InputStream contentOf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new MeasurementImportException(
                    MeasurementImportException.EMPTY_FILE,
                    "No file was sent, or the sent one had no content",
                    List.of());
        }

        try {
            return file.getInputStream();
        } catch (IOException ex) {
            throw new MeasurementImportException(
                    MeasurementImportException.UNREADABLE_FILE,
                    "The sent file could not be opened",
                    List.of());
        }
    }
}
