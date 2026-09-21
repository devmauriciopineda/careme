package com.careme.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.careme.backend.repository.MeasurementRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Full-stack check of the legacy body-tracking endpoint: real database, real Flyway
 * schema, real wiring. The endpoint composes its daily view from the metric model.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CaremeBackendApplicationTests extends PostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MeasurementRepository measurementRepository;

    private void store(String date, String weightKg, String waistCm) {
        measurementRepository.upsert(
                LocalDate.parse(date), new BigDecimal(weightKg), new BigDecimal(waistCm));
    }

    @Test
    void servesEveryStoredMeasurementInChronologicalOrder() throws Exception {
        store("2026-09-10", "80.1", "94.8");
        store("2026-09-06", "81.1", "96.0");
        store("2026-09-08", "80.4", "95.2");

        String body = mockMvc.perform(get("/api/v1/measurements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.messageCode").value("SUCCESS"))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode data = objectMapper.readTree(body).path("data");

        List<String> dates = new ArrayList<>();
        data.forEach(entry -> dates.add(entry.path("date").asText()));

        assertThat(dates).containsExactly("2026-09-06", "2026-09-08", "2026-09-10");

        JsonNode oldest = data.get(0);
        assertThat(oldest.path("id").asText()).matches("[0-9a-fA-F-]{36}");
        assertThat(oldest.path("weightKg").asDouble()).isEqualTo(81.1);
        assertThat(oldest.path("waistCm").asDouble()).isEqualTo(96.0);
    }

    @Test
    void servesAnEmptyPayloadWhenNothingIsStored() throws Exception {
        mockMvc.perform(get("/api/v1/measurements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void allowsCrossOriginReadsFromTheConfiguredFrontendOrigin() throws Exception {
        mockMvc.perform(options("/api/v1/measurements")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }
}
