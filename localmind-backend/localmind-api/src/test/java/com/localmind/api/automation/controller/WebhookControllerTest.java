package com.localmind.api.automation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.api.automation.dto.WebhookRequestDto;
import com.localmind.api.common.advice.GlobalExceptionHandler;
import com.localmind.domain.automation.model.AutomationEventType;
import com.localmind.domain.automation.model.Webhook;
import com.localmind.domain.automation.port.in.AutomationUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class WebhookControllerTest {

    @Mock
    private AutomationUseCase automationUseCase;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new WebhookController(automationUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    // ========== listAll Tests ==========

    @Test
    void listAll_shouldReturnWebhooks() throws Exception {
        Webhook wh1 = Webhook.builder()
                .id("wh-1")
                .name("Hook 1")
                .url("https://example.com/hook1")
                .eventType(AutomationEventType.NEW_FILE)
                .active(true)
                .build();
        Webhook wh2 = Webhook.builder()
                .id("wh-2")
                .name("Hook 2")
                .url("https://example.com/hook2")
                .eventType(AutomationEventType.DOCUMENT_INDEXED)
                .active(false)
                .build();

        when(automationUseCase.listWebhooks()).thenReturn(List.of(wh1, wh2));

        mockMvc.perform(get("/api/v1/webhooks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("wh-1"))
                .andExpect(jsonPath("$[0].name").value("Hook 1"))
                .andExpect(jsonPath("$[0].url").value("https://example.com/hook1"))
                .andExpect(jsonPath("$[0].eventType").value("NEW_FILE"))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[1].id").value("wh-2"))
                .andExpect(jsonPath("$[1].name").value("Hook 2"))
                .andExpect(jsonPath("$[1].eventType").value("DOCUMENT_INDEXED"))
                .andExpect(jsonPath("$[1].active").value(false));

        verify(automationUseCase).listWebhooks();
    }

    @Test
    void listAll_withEmptyList_shouldReturnEmptyArray() throws Exception {
        when(automationUseCase.listWebhooks()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/webhooks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ========== getById Tests ==========

    @Test
    void getById_shouldReturnWebhook() throws Exception {
        Webhook webhook = Webhook.builder()
                .id("wh-1")
                .name("My Webhook")
                .url("https://example.com/hook")
                .eventType(AutomationEventType.SCHEDULED)
                .active(true)
                .build();

        when(automationUseCase.findById("wh-1")).thenReturn(webhook);

        mockMvc.perform(get("/api/v1/webhooks/wh-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("wh-1"))
                .andExpect(jsonPath("$.name").value("My Webhook"))
                .andExpect(jsonPath("$.url").value("https://example.com/hook"))
                .andExpect(jsonPath("$.eventType").value("SCHEDULED"))
                .andExpect(jsonPath("$.active").value(true));

        verify(automationUseCase).findById("wh-1");
    }

    @Test
    void getById_whenNotFound_shouldReturn500() throws Exception {
        when(automationUseCase.findById("non-existent"))
                .thenThrow(new RuntimeException("Webhook not found with id: non-existent"));

        mockMvc.perform(get("/api/v1/webhooks/non-existent"))
                .andExpect(status().isInternalServerError());
    }

    // ========== create Tests ==========

    @Test
    void create_shouldCreateAndReturnWebhook() throws Exception {
        WebhookRequestDto request = WebhookRequestDto.builder()
                .name("New Webhook")
                .url("https://example.com/new-hook")
                .eventType("DOCUMENT_INDEXED")
                .active(true)
                .build();

        Webhook saved = Webhook.builder()
                .id("wh-new")
                .name("New Webhook")
                .url("https://example.com/new-hook")
                .eventType(AutomationEventType.DOCUMENT_INDEXED)
                .active(true)
                .build();

        when(automationUseCase.save(any(Webhook.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/webhooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("wh-new"))
                .andExpect(jsonPath("$.name").value("New Webhook"))
                .andExpect(jsonPath("$.url").value("https://example.com/new-hook"))
                .andExpect(jsonPath("$.eventType").value("DOCUMENT_INDEXED"))
                .andExpect(jsonPath("$.active").value(true));

        verify(automationUseCase).save(any(Webhook.class));
    }

    @Test
    void create_withInvalidBody_shouldReturn400() throws Exception {
        WebhookRequestDto request = WebhookRequestDto.builder()
                .name("")
                .url("https://example.com/hook")
                .eventType("NEW_FILE")
                .active(true)
                .build();

        mockMvc.perform(post("/api/v1/webhooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_withBlankUrl_shouldReturn400() throws Exception {
        WebhookRequestDto request = WebhookRequestDto.builder()
                .name("Valid Name")
                .url("")
                .eventType("NEW_FILE")
                .active(true)
                .build();

        mockMvc.perform(post("/api/v1/webhooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_withBlankEventType_shouldReturn400() throws Exception {
        WebhookRequestDto request = WebhookRequestDto.builder()
                .name("Valid Name")
                .url("https://example.com/hook")
                .eventType("")
                .active(true)
                .build();

        mockMvc.perform(post("/api/v1/webhooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_withNameTooShort_shouldReturn400() throws Exception {
        WebhookRequestDto request = WebhookRequestDto.builder()
                .name("A")
                .url("https://example.com/hook")
                .eventType("NEW_FILE")
                .active(true)
                .build();

        mockMvc.perform(post("/api/v1/webhooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ========== update Tests ==========

    @Test
    void update_shouldUpdateAndReturn() throws Exception {
        WebhookRequestDto request = WebhookRequestDto.builder()
                .name("Updated Webhook")
                .url("https://example.com/updated")
                .eventType("CHAT_COMPLETED")
                .active(false)
                .build();

        Webhook updated = Webhook.builder()
                .id("wh-1")
                .name("Updated Webhook")
                .url("https://example.com/updated")
                .eventType(AutomationEventType.CHAT_COMPLETED)
                .active(false)
                .build();

        when(automationUseCase.save(any(Webhook.class))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/webhooks/wh-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("wh-1"))
                .andExpect(jsonPath("$.name").value("Updated Webhook"))
                .andExpect(jsonPath("$.url").value("https://example.com/updated"))
                .andExpect(jsonPath("$.eventType").value("CHAT_COMPLETED"))
                .andExpect(jsonPath("$.active").value(false));

        verify(automationUseCase).save(any(Webhook.class));
    }

    @Test
    void update_withInvalidBody_shouldReturn400() throws Exception {
        WebhookRequestDto request = WebhookRequestDto.builder()
                .name("")
                .url("https://example.com/hook")
                .eventType("NEW_FILE")
                .active(true)
                .build();

        mockMvc.perform(put("/api/v1/webhooks/wh-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ========== delete Tests ==========

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(automationUseCase).delete("wh-1");

        mockMvc.perform(delete("/api/v1/webhooks/wh-1"))
                .andExpect(status().isNoContent());

        verify(automationUseCase).delete("wh-1");
    }

    // ========== test Tests ==========

    @Test
    void test_shouldReturn200() throws Exception {
        doNothing().when(automationUseCase).testWebhook("wh-1");

        mockMvc.perform(post("/api/v1/webhooks/wh-1/test"))
                .andExpect(status().isOk());

        verify(automationUseCase).testWebhook("wh-1");
    }

    @Test
    void test_whenWebhookNotFound_shouldReturn500() throws Exception {
        doThrow(new RuntimeException("Webhook not found with id: wh-missing"))
                .when(automationUseCase).testWebhook("wh-missing");

        mockMvc.perform(post("/api/v1/webhooks/wh-missing/test"))
                .andExpect(status().isInternalServerError());
    }
}
