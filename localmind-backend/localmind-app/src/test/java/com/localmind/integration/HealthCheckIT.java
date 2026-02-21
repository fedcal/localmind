package com.localmind.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class HealthCheckIT extends AbstractIntegrationTest {

    @Test
    void dashboardHealthShouldReturn200() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/dashboard/health", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void actuatorHealthShouldReturn200() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/actuator/health", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void dashboardHealthShouldContainApiService() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/dashboard/health", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("api");
    }
}
