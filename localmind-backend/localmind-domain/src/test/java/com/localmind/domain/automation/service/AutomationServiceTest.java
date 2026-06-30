package com.localmind.domain.automation.service;

import com.localmind.domain.automation.model.AutomationEvent;
import com.localmind.domain.automation.model.AutomationEventType;
import com.localmind.domain.automation.model.Webhook;
import com.localmind.domain.automation.port.out.WebhookClientPort;
import com.localmind.domain.automation.port.out.WebhookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutomationServiceTest {

    @Mock
    private WebhookRepository webhookRepository;

    @Mock
    private WebhookClientPort webhookClientPort;

    private AutomationService service;

    @BeforeEach
    void setUp() {
        service = new AutomationService(webhookRepository, webhookClientPort);
    }

    // ========== trigger Tests ==========

    @Test
    void trigger_shouldCallActiveWebhooksForEventType() {
        Webhook webhook1 = Webhook.builder()
                .id("wh-1")
                .name("Webhook 1")
                .url("https://example.com/hook1")
                .eventType(AutomationEventType.DOCUMENT_INDEXED)
                .active(true)
                .build();
        Webhook webhook2 = Webhook.builder()
                .id("wh-2")
                .name("Webhook 2")
                .url("https://example.com/hook2")
                .eventType(AutomationEventType.DOCUMENT_INDEXED)
                .active(true)
                .build();

        when(webhookRepository.findByEventType(AutomationEventType.DOCUMENT_INDEXED))
                .thenReturn(List.of(webhook1, webhook2));

        Map<String, Object> payload = Map.of("documentId", "doc-123");
        AutomationEvent event = AutomationEvent.builder()
                .type(AutomationEventType.DOCUMENT_INDEXED)
                .payload(payload)
                .build();

        service.trigger(event);

        verify(webhookClientPort).callWebhook("https://example.com/hook1", payload);
        verify(webhookClientPort).callWebhook("https://example.com/hook2", payload);
        verify(webhookClientPort, times(2)).callWebhook(anyString(), anyMap());
    }

    @Test
    void trigger_shouldContinueIfOneWebhookFails() {
        Webhook webhook1 = Webhook.builder()
                .id("wh-1")
                .name("Failing Webhook")
                .url("https://example.com/fail")
                .eventType(AutomationEventType.NEW_FILE)
                .active(true)
                .build();
        Webhook webhook2 = Webhook.builder()
                .id("wh-2")
                .name("Success Webhook")
                .url("https://example.com/success")
                .eventType(AutomationEventType.NEW_FILE)
                .active(true)
                .build();

        when(webhookRepository.findByEventType(AutomationEventType.NEW_FILE))
                .thenReturn(List.of(webhook1, webhook2));

        Map<String, Object> payload = Map.of("file", "test.pdf");
        doThrow(new RuntimeException("Connection refused"))
                .when(webhookClientPort).callWebhook(eq("https://example.com/fail"), anyMap());

        AutomationEvent event = AutomationEvent.builder()
                .type(AutomationEventType.NEW_FILE)
                .payload(payload)
                .build();

        service.trigger(event);

        verify(webhookClientPort).callWebhook("https://example.com/fail", payload);
        verify(webhookClientPort).callWebhook("https://example.com/success", payload);
    }

    @Test
    void trigger_shouldNotCallInactiveWebhooks() {
        Webhook inactiveWebhook = Webhook.builder()
                .id("wh-inactive")
                .name("Inactive Webhook")
                .url("https://example.com/inactive")
                .eventType(AutomationEventType.SCHEDULED)
                .active(false)
                .build();

        when(webhookRepository.findByEventType(AutomationEventType.SCHEDULED))
                .thenReturn(List.of(inactiveWebhook));

        AutomationEvent event = AutomationEvent.builder()
                .type(AutomationEventType.SCHEDULED)
                .payload(Map.of("schedule", "daily"))
                .build();

        service.trigger(event);

        verify(webhookClientPort, never()).callWebhook(anyString(), anyMap());
    }

    @Test
    void trigger_shouldSkipInactiveAndCallActive() {
        Webhook inactive = Webhook.builder()
                .id("wh-1")
                .name("Inactive")
                .url("https://example.com/inactive")
                .eventType(AutomationEventType.CHAT_COMPLETED)
                .active(false)
                .build();
        Webhook active = Webhook.builder()
                .id("wh-2")
                .name("Active")
                .url("https://example.com/active")
                .eventType(AutomationEventType.CHAT_COMPLETED)
                .active(true)
                .build();

        when(webhookRepository.findByEventType(AutomationEventType.CHAT_COMPLETED))
                .thenReturn(List.of(inactive, active));

        Map<String, Object> payload = Map.of("chatId", "chat-1");
        AutomationEvent event = AutomationEvent.builder()
                .type(AutomationEventType.CHAT_COMPLETED)
                .payload(payload)
                .build();

        service.trigger(event);

        verify(webhookClientPort, times(1)).callWebhook("https://example.com/active", payload);
        verify(webhookClientPort, never()).callWebhook(eq("https://example.com/inactive"), anyMap());
    }

    @Test
    void trigger_withNoWebhooksFound_shouldNotCallClient() {
        when(webhookRepository.findByEventType(AutomationEventType.DOCUMENT_FAILED))
                .thenReturn(List.of());

        AutomationEvent event = AutomationEvent.builder()
                .type(AutomationEventType.DOCUMENT_FAILED)
                .payload(Map.of("error", "parse failure"))
                .build();

        service.trigger(event);

        verify(webhookClientPort, never()).callWebhook(anyString(), anyMap());
    }

    // ========== listWebhooks Tests ==========

    @Test
    void listWebhooks_shouldReturnAll() {
        Webhook wh1 = Webhook.builder().id("wh-1").name("Hook 1").url("https://example.com/1")
                .eventType(AutomationEventType.NEW_FILE).active(true).build();
        Webhook wh2 = Webhook.builder().id("wh-2").name("Hook 2").url("https://example.com/2")
                .eventType(AutomationEventType.SCHEDULED).active(false).build();

        when(webhookRepository.findAll()).thenReturn(List.of(wh1, wh2));

        List<Webhook> result = service.listWebhooks();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Hook 1");
        assertThat(result.get(1).getName()).isEqualTo("Hook 2");
        verify(webhookRepository).findAll();
    }

    @Test
    void listWebhooks_withEmpty_shouldReturnEmptyList() {
        when(webhookRepository.findAll()).thenReturn(List.of());

        List<Webhook> result = service.listWebhooks();

        assertThat(result).isEmpty();
    }

    // ========== findById Tests ==========

    @Test
    void findById_shouldReturnWebhook() {
        Webhook webhook = Webhook.builder()
                .id("wh-1")
                .name("My Webhook")
                .url("https://example.com/hook")
                .eventType(AutomationEventType.DOCUMENT_INDEXED)
                .active(true)
                .build();

        when(webhookRepository.findById("wh-1")).thenReturn(Optional.of(webhook));

        Webhook result = service.findById("wh-1");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("wh-1");
        assertThat(result.getName()).isEqualTo("My Webhook");
        assertThat(result.getUrl()).isEqualTo("https://example.com/hook");
        assertThat(result.getEventType()).isEqualTo(AutomationEventType.DOCUMENT_INDEXED);
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        when(webhookRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById("non-existent"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Webhook not found with id: non-existent");
    }

    // ========== save Tests ==========

    @Test
    void save_shouldDelegateToRepository() {
        Webhook webhook = Webhook.builder()
                .name("New Webhook")
                .url("https://example.com/new")
                .eventType(AutomationEventType.NEW_FILE)
                .active(true)
                .build();

        Webhook saved = Webhook.builder()
                .id("generated-id")
                .name("New Webhook")
                .url("https://example.com/new")
                .eventType(AutomationEventType.NEW_FILE)
                .active(true)
                .build();

        when(webhookRepository.save(webhook)).thenReturn(saved);

        Webhook result = service.save(webhook);

        assertThat(result.getId()).isEqualTo("generated-id");
        assertThat(result.getName()).isEqualTo("New Webhook");
        verify(webhookRepository).save(webhook);
    }

    // ========== delete Tests ==========

    @Test
    void delete_shouldDelegateToRepository() {
        service.delete("wh-to-delete");

        verify(webhookRepository).deleteById("wh-to-delete");
    }

    // ========== testWebhook Tests ==========

    @Test
    void testWebhook_shouldCallWebhookWithTestPayload() {
        Webhook webhook = Webhook.builder()
                .id("wh-test")
                .name("Test Hook")
                .url("https://example.com/test-hook")
                .eventType(AutomationEventType.DOCUMENT_INDEXED)
                .active(true)
                .build();

        when(webhookRepository.findById("wh-test")).thenReturn(Optional.of(webhook));

        service.testWebhook("wh-test");

        verify(webhookClientPort).callWebhook(
                eq("https://example.com/test-hook"),
                eq(Map.of("event", "test", "webhookId", "wh-test"))
        );
    }

    @Test
    void testWebhook_shouldThrowWhenWebhookNotFound() {
        when(webhookRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.testWebhook("non-existent"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Webhook not found with id: non-existent");
    }
}
