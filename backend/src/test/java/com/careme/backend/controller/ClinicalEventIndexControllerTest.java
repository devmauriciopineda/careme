package com.careme.backend.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.careme.backend.dto.ClinicalEventInspectionResponse;
import com.careme.backend.service.ClinicalEventIndexRebuilder;
import com.careme.backend.service.ClinicalEventInspectionService;
import com.careme.backend.service.EncounterIndexRebuilder;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ClinicalEventIndexController.class)
class ClinicalEventIndexControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClinicalEventIndexRebuilder rebuilder;

    @MockitoBean
    private EncounterIndexRebuilder encounterRebuilder;

    @MockitoBean
    private ClinicalEventInspectionService inspectionService;

    @Test
    void returnsBothDatesInTheDefaultListContract() throws Exception {
        when(inspectionService.findAll(null, null, null, ClinicalEventInspectionService.SortBy.RECORD_DATE))
                .thenReturn(List.of(event()));

        mockMvc.perform(get("/api/v1/clinical-events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].occurrenceDate").value("2026-09-01"))
                .andExpect(jsonPath("$.data[0].recordDate").value("2026-09-02"));
    }

    @Test
    void acceptsOccurrenceDateOrderingAndFilters() throws Exception {
        when(inspectionService.findAll(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(ClinicalEventInspectionService.SortBy.OCCURRENCE_DATE)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/clinical-events")
                .param("type", "diagnosis")
                .param("from", "2026-09-01")
                .param("to", "2026-09-30")
                .param("sort", "occurrenceDate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void rejectsInvalidSortInSpanishSafeErrorEnvelope() throws Exception {
        mockMvc.perform(get("/api/v1/clinical-events").param("sort", "createdAt"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_FILTER"));
    }

    @Test
    void rejectsInvalidTypeAndDatesInSpanishSafeErrorEnvelopes() throws Exception {
        mockMvc.perform(get("/api/v1/clinical-events").param("type", "unknown"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_FILTER"));
        mockMvc.perform(get("/api/v1/clinical-events").param("from", "not-a-date"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_FILTER"));
    }

    @Test
    void reindexesEventsAndReturnsTheIndexedCount() throws Exception {
        when(rebuilder.rebuild()).thenReturn(4);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/v1/clinical-events/reindex"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.indexedEvents").value(4));
    }

    @Test
    void returnsDetailOrNotFoundWithoutAPartialPayload() throws Exception {
        when(inspectionService.findByCode("evt_001")).thenReturn(Optional.of(event()));
        when(inspectionService.findByCode("evt_999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/clinical-events/evt_001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("evt_001"));
        mockMvc.perform(get("/api/v1/clinical-events/evt_999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("EVENT_NOT_FOUND"));
    }

    private static ClinicalEventInspectionResponse event() {
        return new ClinicalEventInspectionResponse(
                UUID.fromString("11111111-1111-4111-8111-111111111111"), "evt_001", "diagnosis",
                "Hipertensión", LocalDate.of(2026, 9, 1), "exact", LocalDate.of(2026, 9, 2));
    }
}