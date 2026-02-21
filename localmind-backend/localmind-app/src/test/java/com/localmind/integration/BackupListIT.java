package com.localmind.integration;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BackupListIT extends AbstractIntegrationTest {

    @Test
    void listBackupsShouldReturnEmptyList() {
        ResponseEntity<List<Map>> response = restTemplate.exchange(
                "/api/v1/backups",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void shouldCreateBackup() {
        Map<String, Object> backupBody = Map.of(
                "components", Set.of("CONFIGURATION")
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/backups", backupBody, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("id")).isNotNull();
        assertThat(response.getBody().get("filename")).isNotNull();
    }

    @Test
    void shouldListBackupsAfterCreation() {
        // Crea un backup
        Map<String, Object> backupBody = Map.of(
                "components", Set.of("CONFIGURATION")
        );
        restTemplate.postForEntity("/api/v1/backups", backupBody, Map.class);

        // Verifica che sia nella lista
        ResponseEntity<List<Map>> listResponse = restTemplate.exchange(
                "/api/v1/backups",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {});
        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResponse.getBody()).isNotNull();
        assertThat(listResponse.getBody()).isNotEmpty();
    }
}
