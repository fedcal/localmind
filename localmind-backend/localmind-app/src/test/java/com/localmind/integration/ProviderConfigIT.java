package com.localmind.integration;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ProviderConfigIT extends AbstractIntegrationTest {

    @Test
    void listProvidersShouldReturn200() {
        ResponseEntity<List<Map>> response = restTemplate.exchange(
                "/api/v1/settings/providers",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        // V8 migration seeds a default Ollama provider
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void shouldCreateProvider() {
        Map<String, Object> providerBody = Map.of(
                "name", "Test OpenAI",
                "type", "OPENAI",
                "baseUrl", "https://api.openai.com",
                "apiKey", "test-key-123",
                "defaultModel", "gpt-4o"
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/settings/providers", providerBody, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("name")).isEqualTo("Test OpenAI");
        assertThat(response.getBody().get("type")).isEqualTo("OPENAI");
        assertThat(response.getBody().get("id")).isNotNull();
    }

    @Test
    void shouldDeleteProvider() {
        // Crea provider
        Map<String, Object> providerBody = Map.of(
                "name", "To Delete Provider",
                "type", "OPENAI",
                "baseUrl", "https://api.openai.com",
                "apiKey", "delete-key"
        );
        ResponseEntity<Map> createResponse = restTemplate.postForEntity(
                "/api/v1/settings/providers", providerBody, Map.class);
        String providerId = (String) createResponse.getBody().get("id");

        // Elimina
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/api/v1/settings/providers/" + providerId,
                HttpMethod.DELETE,
                null,
                Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldUpdateDefaultModel() {
        // Crea provider
        Map<String, Object> providerBody = Map.of(
                "name", "Update Model Provider",
                "type", "OPENAI",
                "baseUrl", "https://api.openai.com",
                "apiKey", "update-key",
                "defaultModel", "gpt-3.5-turbo"
        );
        ResponseEntity<Map> createResponse = restTemplate.postForEntity(
                "/api/v1/settings/providers", providerBody, Map.class);
        String providerId = (String) createResponse.getBody().get("id");

        // Aggiorna default model
        Map<String, String> updateBody = Map.of("model", "gpt-4o");
        ResponseEntity<Map> updateResponse = restTemplate.exchange(
                "/api/v1/settings/providers/" + providerId + "/default-model",
                HttpMethod.PUT,
                new HttpEntity<>(updateBody),
                Map.class);
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody()).isNotNull();
        assertThat(updateResponse.getBody().get("defaultModel")).isEqualTo("gpt-4o");
    }
}
