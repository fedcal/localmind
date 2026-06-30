package com.localmind.integration;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentListIT extends AbstractIntegrationTest {

    @Test
    void listDocumentsShouldReturnEmptyList() {
        ResponseEntity<List<Map>> response = restTemplate.exchange(
                "/api/v1/documents",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void getDocumentStatsShouldReturn200() {
        ResponseEntity<List<Map>> response = restTemplate.exchange(
                "/api/v1/documents/stats",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void getDocumentDistributionShouldReturn200() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/v1/documents/stats/distribution", Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }
}
