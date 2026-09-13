package com.careme.backend.controller;

import com.careme.backend.service.ClinicalEventIndexRebuilder;
import java.io.IOException;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clinical-events")
public class ClinicalEventIndexController {

    private final ClinicalEventIndexRebuilder rebuilder;

    public ClinicalEventIndexController(ClinicalEventIndexRebuilder rebuilder) {
        this.rebuilder = rebuilder;
    }

    @PostMapping("/reindex")
    public ResponseEntity<Map<String, Integer>> reindex() throws IOException {
        return ResponseEntity.ok(Map.of("indexedEvents", rebuilder.rebuild()));
    }
}