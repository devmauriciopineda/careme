package com.careme.backend.controller;

import com.careme.backend.service.ClinicalEventIndexRebuilder;
import com.careme.backend.service.EncounterIndexRebuilder;
import com.careme.backend.dto.ApiResponse;
import com.careme.backend.dto.ClinicalEventInspectionResponse;
import com.careme.backend.dto.ErrorResponse;
import com.careme.backend.exception.ClinicalEventInspectionException;
import com.careme.backend.entity.ClinicalEvent;
import com.careme.backend.service.ClinicalEventInspectionService;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clinical-events")
public class ClinicalEventIndexController {

    private final ClinicalEventIndexRebuilder rebuilder;
    private final EncounterIndexRebuilder encounterRebuilder;
    private final ClinicalEventInspectionService inspectionService;

    public ClinicalEventIndexController(
            ClinicalEventIndexRebuilder rebuilder,
            EncounterIndexRebuilder encounterRebuilder,
            ClinicalEventInspectionService inspectionService) {
        this.rebuilder = rebuilder;
        this.encounterRebuilder = encounterRebuilder;
        this.inspectionService = inspectionService;
    }

    @GetMapping
    public ApiResponse<List<ClinicalEventInspectionResponse>> findAll(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false, defaultValue = "recordDate") String sort) throws IOException {
        return ApiResponse.ok(inspectionService.findAll(
                parseType(type), parseDate(from, "from"), parseDate(to, "to"), parseSort(sort)));
    }

    @GetMapping("/{code}")
        public ResponseEntity<?> findByCode(
            @PathVariable String code) throws IOException {
        Optional<ClinicalEventInspectionResponse> event = inspectionService.findByCode(code);
            if (event.isPresent()) {
                return ResponseEntity.ok(ApiResponse.ok(event.get()));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                    "No se encontró el hecho solicitado.",
                    "EVENT_NOT_FOUND",
                    List.of()));
    }

    @PostMapping("/reindex")
    public ResponseEntity<Map<String, Integer>> reindex() throws IOException {
        return ResponseEntity.ok(Map.of(
                "indexedEvents", rebuilder.rebuild(),
                "indexedEncounters", encounterRebuilder.rebuild()));
    }

    private static ClinicalEvent.ClinicalEventType parseType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return ClinicalEvent.ClinicalEventType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new ClinicalEventInspectionException("El tipo de evento no es válido.");
        }
    }

    private static LocalDate parseDate(String value, String parameter) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (RuntimeException exception) {
            throw new ClinicalEventInspectionException("La fecha " + parameter + " no es válida.");
        }
    }

    private static ClinicalEventInspectionService.SortBy parseSort(String value) {
        if (value == null || value.isBlank() || value.equalsIgnoreCase("recordDate")) {
            return ClinicalEventInspectionService.SortBy.RECORD_DATE;
        }
        if (value.equalsIgnoreCase("occurrenceDate")) {
            return ClinicalEventInspectionService.SortBy.OCCURRENCE_DATE;
        }
        throw new ClinicalEventInspectionException("El criterio de ordenación no es válido.");
    }
}