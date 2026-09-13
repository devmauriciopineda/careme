package com.careme.backend.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

/**
 * Guards the API's CORS contract.
 *
 * <p>The browser preflight of a verb that the mapping does not list is rejected,
 * and the browser surfaces that as an opaque network failure instead of a
 * readable response. These assertions keep every verb the interface uses listed.
 */
class CorsConfigTest {

    private static final String ORIGIN = "http://localhost:3000";
    private static final String API_MAPPING = "/api/**";

    /** `CorsRegistry` keeps its mappings protected, so the test exposes them. */
    private static final class InspectableCorsRegistry extends CorsRegistry {

        CorsConfiguration forPattern(String pathPattern) {
            return getCorsConfigurations().get(pathPattern);
        }
    }

    private CorsConfiguration configurationForApiMapping() {
        InspectableCorsRegistry registry = new InspectableCorsRegistry();
        new CorsConfig().corsConfigurer(List.of(ORIGIN)).addCorsMappings(registry);

        return registry.forPattern(API_MAPPING);
    }

    @Test
    void allowsTheDashboardOrigin() {
        assertEquals(ORIGIN, configurationForApiMapping().checkOrigin(ORIGIN));
    }

    @Test
    void allowsTheVerbsTheInterfaceUses() {
        CorsConfiguration configuration = configurationForApiMapping();

        assertNotNull(configuration.checkHttpMethod(HttpMethod.GET), "reading measurements uses GET");
        assertNotNull(configuration.checkHttpMethod(HttpMethod.POST), "sending a chat turn uses POST");
    }

    @Test
    void leavesUnusedVerbsClosed() {
        assertNull(configurationForApiMapping().checkHttpMethod(HttpMethod.DELETE));
    }
}
