package com.careme.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.careme.backend.dto.ApiResponse;
import com.careme.backend.dto.MeasurementRequest;
import com.careme.backend.dto.MeasurementResponse;
import com.careme.backend.service.MeasurementService;

import jakarta.validation.Valid;

/**
 * HTTP boundary for body measurements. Delegates every decision to the service.
 */
@RestController
@RequestMapping("/api/v1/measurements")
public class MeasurementController {

    private final MeasurementService measurementService;

    public MeasurementController(MeasurementService measurementService) {
        this.measurementService = measurementService;
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
}
