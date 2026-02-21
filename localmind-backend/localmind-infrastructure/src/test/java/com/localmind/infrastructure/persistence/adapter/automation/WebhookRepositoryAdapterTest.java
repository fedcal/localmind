package com.localmind.infrastructure.persistence.adapter.automation;

import com.localmind.domain.automation.model.AutomationEventType;
import com.localmind.domain.automation.model.Webhook;
import com.localmind.infrastructure.persistence.entity.automation.WebhookEntity;
import com.localmind.infrastructure.persistence.repository.automation.JpaWebhookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebhookRepositoryAdapterTest {

    @Mock
    private JpaWebhookRepository jpaRepository;

    private WebhookRepositoryAdapter adapter;

    private static final UUID TEST_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID TEST_UUID_2 = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");

    @BeforeEach
    void setUp() {
        adapter = new WebhookRepositoryAdapter(jpaRepository);
    }

    // ========== save Tests ==========

    @Test
    void save_mapsDomainToEntityAndBack() {
        Webhook domain = Webhook.builder()
                .id(TEST_UUID.toString())
                .name("My Webhook")
                .url("https://example.com/hook")
                .eventType(AutomationEventType.DOCUMENT_INDEXED)
                .active(true)
                .build();

        WebhookEntity savedEntity = WebhookEntity.builder()
                .id(TEST_UUID)
                .name("My Webhook")
                .url("https://example.com/hook")
                .eventType("DOCUMENT_INDEXED")
                .active(true)
                .build();

        when(jpaRepository.save(any(WebhookEntity.class))).thenReturn(savedEntity);

        Webhook result = adapter.save(domain);

        ArgumentCaptor<WebhookEntity> captor = ArgumentCaptor.forClass(WebhookEntity.class);
        verify(jpaRepository).save(captor.capture());

        WebhookEntity capturedEntity = captor.getValue();
        assertThat(capturedEntity.getId()).isEqualTo(TEST_UUID);
        assertThat(capturedEntity.getName()).isEqualTo("My Webhook");
        assertThat(capturedEntity.getUrl()).isEqualTo("https://example.com/hook");
        assertThat(capturedEntity.getEventType()).isEqualTo("DOCUMENT_INDEXED");
        assertThat(capturedEntity.isActive()).isTrue();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getName()).isEqualTo("My Webhook");
        assertThat(result.getUrl()).isEqualTo("https://example.com/hook");
        assertThat(result.getEventType()).isEqualTo(AutomationEventType.DOCUMENT_INDEXED);
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void save_withNullId_shouldNotSetEntityId() {
        Webhook domain = Webhook.builder()
                .name("New Webhook")
                .url("https://example.com/new")
                .eventType(AutomationEventType.NEW_FILE)
                .active(true)
                .build();

        WebhookEntity savedEntity = WebhookEntity.builder()
                .id(TEST_UUID)
                .name("New Webhook")
                .url("https://example.com/new")
                .eventType("NEW_FILE")
                .active(true)
                .build();

        when(jpaRepository.save(any(WebhookEntity.class))).thenReturn(savedEntity);

        Webhook result = adapter.save(domain);

        ArgumentCaptor<WebhookEntity> captor = ArgumentCaptor.forClass(WebhookEntity.class);
        verify(jpaRepository).save(captor.capture());

        WebhookEntity capturedEntity = captor.getValue();
        assertThat(capturedEntity.getId()).isNull();
        assertThat(capturedEntity.getName()).isEqualTo("New Webhook");

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
    }

    // ========== findById Tests ==========

    @Test
    void findById_whenFound_returnsMappedDomain() {
        WebhookEntity entity = WebhookEntity.builder()
                .id(TEST_UUID)
                .name("Found Webhook")
                .url("https://example.com/found")
                .eventType("SCHEDULED")
                .active(false)
                .build();

        when(jpaRepository.findById(TEST_UUID)).thenReturn(Optional.of(entity));

        Optional<Webhook> result = adapter.findById(TEST_UUID.toString());

        assertThat(result).isPresent();
        Webhook webhook = result.get();
        assertThat(webhook.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(webhook.getName()).isEqualTo("Found Webhook");
        assertThat(webhook.getUrl()).isEqualTo("https://example.com/found");
        assertThat(webhook.getEventType()).isEqualTo(AutomationEventType.SCHEDULED);
        assertThat(webhook.isActive()).isFalse();
    }

    @Test
    void findById_whenNotFound_returnsEmpty() {
        when(jpaRepository.findById(TEST_UUID)).thenReturn(Optional.empty());

        Optional<Webhook> result = adapter.findById(TEST_UUID.toString());

        assertThat(result).isEmpty();
    }

    // ========== findAll Tests ==========

    @Test
    void findAll_mapsEntitiesCorrectly() {
        WebhookEntity entity1 = WebhookEntity.builder()
                .id(TEST_UUID)
                .name("Webhook 1")
                .url("https://example.com/1")
                .eventType("NEW_FILE")
                .active(true)
                .build();
        WebhookEntity entity2 = WebhookEntity.builder()
                .id(TEST_UUID_2)
                .name("Webhook 2")
                .url("https://example.com/2")
                .eventType("CHAT_COMPLETED")
                .active(false)
                .build();

        when(jpaRepository.findAll()).thenReturn(List.of(entity1, entity2));

        List<Webhook> result = adapter.findAll();

        assertThat(result).hasSize(2);

        assertThat(result.get(0).getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.get(0).getName()).isEqualTo("Webhook 1");
        assertThat(result.get(0).getUrl()).isEqualTo("https://example.com/1");
        assertThat(result.get(0).getEventType()).isEqualTo(AutomationEventType.NEW_FILE);
        assertThat(result.get(0).isActive()).isTrue();

        assertThat(result.get(1).getId()).isEqualTo(TEST_UUID_2.toString());
        assertThat(result.get(1).getName()).isEqualTo("Webhook 2");
        assertThat(result.get(1).getUrl()).isEqualTo("https://example.com/2");
        assertThat(result.get(1).getEventType()).isEqualTo(AutomationEventType.CHAT_COMPLETED);
        assertThat(result.get(1).isActive()).isFalse();
    }

    @Test
    void findAll_withEmpty_returnsEmptyList() {
        when(jpaRepository.findAll()).thenReturn(List.of());

        List<Webhook> result = adapter.findAll();

        assertThat(result).isEmpty();
    }

    // ========== findByEventType Tests ==========

    @Test
    void findByEventType_mapsCorrectly() {
        WebhookEntity entity = WebhookEntity.builder()
                .id(TEST_UUID)
                .name("Indexed Hook")
                .url("https://example.com/indexed")
                .eventType("DOCUMENT_INDEXED")
                .active(true)
                .build();

        when(jpaRepository.findByEventType("DOCUMENT_INDEXED")).thenReturn(List.of(entity));

        List<Webhook> result = adapter.findByEventType(AutomationEventType.DOCUMENT_INDEXED);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.get(0).getName()).isEqualTo("Indexed Hook");
        assertThat(result.get(0).getEventType()).isEqualTo(AutomationEventType.DOCUMENT_INDEXED);

        verify(jpaRepository).findByEventType("DOCUMENT_INDEXED");
    }

    @Test
    void findByEventType_withNoMatches_returnsEmptyList() {
        when(jpaRepository.findByEventType("DOCUMENT_FAILED")).thenReturn(List.of());

        List<Webhook> result = adapter.findByEventType(AutomationEventType.DOCUMENT_FAILED);

        assertThat(result).isEmpty();
    }

    // ========== deleteById Tests ==========

    @Test
    void deleteById_delegatesToJpa() {
        adapter.deleteById(TEST_UUID.toString());

        verify(jpaRepository).deleteById(TEST_UUID);
    }
}
