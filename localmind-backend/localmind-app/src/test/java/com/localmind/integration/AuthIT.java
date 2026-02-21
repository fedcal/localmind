package com.localmind.integration;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthIT extends AbstractIntegrationTest {

    private static final String INITIAL_PASSWORD = "testpass123";
    private static final String NEW_PASSWORD = "newpass456";

    @Test
    @Order(1)
    void shouldCheckAuthStatusBeforeSetup() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/v1/auth/status", Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @Order(2)
    void shouldSetupInitialPassword() {
        Map<String, String> setupBody = Map.of(
                "password", INITIAL_PASSWORD,
                "confirmPassword", INITIAL_PASSWORD
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/auth/setup", setupBody, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("token")).isNotNull();
        assertThat((Number) response.getBody().get("expiresIn")).isNotNull();
    }

    @Test
    @Order(3)
    void shouldLoginWithCorrectPassword() {
        // Assicurati che la password sia configurata
        Map<String, String> setupBody = Map.of(
                "password", INITIAL_PASSWORD,
                "confirmPassword", INITIAL_PASSWORD
        );
        restTemplate.postForEntity("/api/v1/auth/setup", setupBody, Map.class);

        Map<String, String> loginBody = Map.of("password", INITIAL_PASSWORD);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/auth/login", loginBody, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("token")).isNotNull();
    }

    @Test
    @Order(4)
    void shouldRejectWrongPassword() {
        // Assicurati che la password sia configurata
        Map<String, String> setupBody = Map.of(
                "password", INITIAL_PASSWORD,
                "confirmPassword", INITIAL_PASSWORD
        );
        restTemplate.postForEntity("/api/v1/auth/setup", setupBody, Map.class);

        Map<String, String> loginBody = Map.of("password", "wrong-password");
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/auth/login", loginBody, String.class);
        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }
}
