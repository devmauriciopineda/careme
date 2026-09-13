package com.careme.backend.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS for the API.
 *
 * <p>The dashboard reaches the API from the server, so the browser does not need
 * this mapping. It stays explicit and origin-scoped so the API is not open to
 * every origin.
 *
 * <p>Every verb a browser client uses must be listed: the preflight of a verb
 * that is missing here is rejected, and the browser reports the rejection as an
 * opaque network failure rather than a readable response.
 */
@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer(
            @Value("${careme.cors.allowed-origins}") List<String> allowedOrigins) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(allowedOrigins.toArray(String[]::new))
                        .allowedMethods("GET", "POST");
            }
        };
    }
}
