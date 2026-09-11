package com.careme.backend.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.careme.backend.dto.MeasurementResponse;
import com.careme.backend.service.MeasurementService;

@WebMvcTest(MeasurementController.class)
@TestPropertySource(properties = "careme.cors.allowed-origins=http://localhost:3000")
class MeasurementControllerTest {

    private static final UUID OLDEST_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final UUID NEWEST_ID = UUID.fromString("22222222-2222-4222-8222-222222222222");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MeasurementService measurementService;

    @Test
    void returnsTheMeasurementsInsideTheSuccessEnvelope() throws Exception {
        when(measurementService.findAll()).thenReturn(List.of(
                new MeasurementResponse(
                        OLDEST_ID,
                        LocalDate.of(2026, 9, 6),
                        new BigDecimal("81.1"),
                        new BigDecimal("96.0")),
                new MeasurementResponse(
                        NEWEST_ID,
                        LocalDate.of(2026, 9, 10),
                        new BigDecimal("80.1"),
                        new BigDecimal("94.8"))));

        mockMvc.perform(get("/api/v1/measurements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.messageCode").value("SUCCESS"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(OLDEST_ID.toString()))
                .andExpect(jsonPath("$.data[0].date").value("2026-09-06"))
                .andExpect(jsonPath("$.data[0].weightKg").value(81.1))
                .andExpect(jsonPath("$.data[0].waistCm").value(96.0));
    }

    @Test
    void returnsAnEmptyPayloadWhenNoMeasurementExists() throws Exception {
        when(measurementService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/measurements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void mapsAnUnexpectedFailureToA500ErrorEnvelope() throws Exception {
        when(measurementService.findAll()).thenThrow(new IllegalStateException("boom"));

        mockMvc.perform(get("/api/v1/measurements"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"));
    }
}
