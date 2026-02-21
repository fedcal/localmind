package com.localmind.integration;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FolderConfigIT extends AbstractIntegrationTest {

    @Test
    void listFoldersShouldReturnEmptyList() {
        ResponseEntity<List<Map>> response = restTemplate.exchange(
                "/api/v1/folders",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void shouldCreateFolder() {
        Map<String, Object> folderBody = Map.of(
                "path", "/tmp/localmind-test-folder",
                "recursive", true,
                "watchEnabled", false
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/folders", folderBody, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("id")).isNotNull();
        assertThat(response.getBody().get("path")).isEqualTo("/tmp/localmind-test-folder");
        assertThat(response.getBody().get("recursive")).isEqualTo(true);
    }

    @Test
    void shouldDeleteFolder() {
        // Crea cartella
        Map<String, Object> folderBody = Map.of(
                "path", "/tmp/localmind-delete-test",
                "recursive", false,
                "watchEnabled", false
        );
        ResponseEntity<Map> createResponse = restTemplate.postForEntity(
                "/api/v1/folders", folderBody, Map.class);
        String folderId = (String) createResponse.getBody().get("id");

        // Elimina cartella
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/api/v1/folders/" + folderId,
                HttpMethod.DELETE,
                null,
                Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verifica che non sia piu nella lista
        ResponseEntity<List<Map>> listResponse = restTemplate.exchange(
                "/api/v1/folders",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {});
        boolean found = listResponse.getBody().stream()
                .anyMatch(f -> folderId.equals(f.get("id")));
        assertThat(found).isFalse();
    }
}
