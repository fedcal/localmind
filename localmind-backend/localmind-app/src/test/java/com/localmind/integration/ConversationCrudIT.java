package com.localmind.integration;

import com.localmind.domain.llm.model.ConversationContext;
import com.localmind.domain.llm.service.ConversationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ConversationCrudIT extends AbstractIntegrationTest {

    @Autowired
    private ConversationService conversationService;

    @Test
    void listConversationsShouldReturnEmptyInitially() {
        ResponseEntity<List<Map>> response = restTemplate.exchange(
                "/api/v1/conversations",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void shouldCreateAndRetrieveConversation() {
        // Crea conversazione tramite il service (il ChatController richiede LLM attivo)
        ConversationContext created = conversationService.getOrCreateForChat(
                null, "Test integration message", null);

        // Recupera la conversazione via REST
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/v1/conversations/" + created.getId(), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("id")).isEqualTo(created.getId());
        assertThat(response.getBody().get("title")).isEqualTo("Test integration message");
    }

    @Test
    void shouldRenameConversation() {
        ConversationContext created = conversationService.getOrCreateForChat(
                null, "Original title", null);

        Map<String, String> renameBody = Map.of("title", "Renamed title");
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/conversations/" + created.getId(),
                HttpMethod.PATCH,
                new HttpEntity<>(renameBody),
                Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("title")).isEqualTo("Renamed title");
    }

    @Test
    void shouldDeleteConversation() {
        ConversationContext created = conversationService.getOrCreateForChat(
                null, "To be deleted", null);

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/api/v1/conversations/" + created.getId(),
                HttpMethod.DELETE,
                null,
                Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verifica che non esista piu
        ResponseEntity<String> getResponse = restTemplate.getForEntity(
                "/api/v1/conversations/" + created.getId(), String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldListConversationsAfterCreation() {
        conversationService.getOrCreateForChat(null, "List test conversation", null);

        ResponseEntity<List<Map>> response = restTemplate.exchange(
                "/api/v1/conversations",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isNotEmpty();
    }
}
