package com.careme.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.careme.backend.dto.MeasurementRequest;
import com.careme.backend.dto.MeasurementResponse;
import com.careme.backend.service.MeasurementImportService;
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

    @MockitoBean
    private MeasurementImportService measurementImportService;

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

    @Test
    void registersAMeasurementAndReturnsItInTheSuccessEnvelope() throws Exception {
        when(measurementService.register(any(MeasurementRequest.class))).thenReturn(
                new MeasurementResponse(
                        OLDEST_ID,
                        LocalDate.of(2026, 9, 10),
                        new BigDecimal("80.1"),
                        new BigDecimal("94.8")));

        mockMvc.perform(post("/api/v1/measurements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"date":"2026-09-10","weightKg":80.1,"waistCm":94.8}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.date").value("2026-09-10"))
                .andExpect(jsonPath("$.data.weightKg").value(80.1))
                .andExpect(jsonPath("$.data.waistCm").value(94.8));
    }

    @Test
    void rejectsANonPositiveMetricWithAValidationError() throws Exception {
        mockMvc.perform(post("/api/v1/measurements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"date":"2026-09-10","weightKg":0,"waistCm":94.8}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.details[0]").value(
                        org.hamcrest.Matchers.containsString("weightKg")));
    }

    @Test
    void rejectsAMetricWithMoreThanOneDecimal() throws Exception {
        mockMvc.perform(post("/api/v1/measurements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"date":"2026-09-10","weightKg":80.12,"waistCm":94.8}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void rejectsAFutureDate() throws Exception {
        mockMvc.perform(post("/api/v1/measurements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"date":"2999-01-01","weightKg":80.1,"waistCm":94.8}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void rejectsAMalformedBody() throws Exception {
        mockMvc.perform(post("/api/v1/measurements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
    }
}
