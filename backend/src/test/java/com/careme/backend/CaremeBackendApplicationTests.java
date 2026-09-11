package com.careme.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Full-stack check of the only endpoint: real data file, real wiring.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CaremeBackendApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private JsonNode seedMeasurements() throws Exception {
        try (var input = new ClassPathResource("data/measurements.json").getInputStream()) {
            return objectMapper.readTree(input);
        }
    }

    @Test
    void servesEveryMeasurementFromTheDataFileInChronologicalOrder() throws Exception {
        int storedCount = seedMeasurements().size();
        assertThat(storedCount).isPositive();

        String body = mockMvc.perform(get("/api/v1/measurements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.messageCode").value("SUCCESS"))
                .andExpect(jsonPath("$.data.length()").value(storedCount))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode data = objectMapper.readTree(body).path("data");

        List<String> dates = new ArrayList<>();
        data.forEach(measurement -> dates.add(measurement.path("date").asText()));

        assertThat(dates).isSorted();
        assertThat(dates).allMatch(date -> date.matches("\\d{4}-\\d{2}-\\d{2}"));
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
