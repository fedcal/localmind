package com.localmind.infrastructure.persistence.adapter;

import com.localmind.domain.llm.model.LlmProvider;
import com.localmind.domain.llm.model.LlmProviderConfig;
import com.localmind.infrastructure.persistence.entity.LlmProviderConfigEntity;
import com.localmind.infrastructure.persistence.repository.JpaProviderConfigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProviderConfigRepositoryAdapterTest {

    @Mock
    private JpaProviderConfigRepository jpaRepository;

    @InjectMocks
    private ProviderConfigRepositoryAdapter adapter;

    private final UUID TEST_UUID = UUID.fromString("c3d4e5f6-a7b8-9012-cdef-123456789012");
    private final Instant NOW = Instant.parse("2026-01-20T14:00:00Z");

    @Test
    void save_shouldMapAndDelegate() {
        // given
        LlmProviderConfig config = LlmProviderConfig.builder()
                .id(TEST_UUID.toString())
                .name("Ollama Local")
                .type(LlmProvider.OLLAMA)
                .baseUrl("http://localhost:11434")
                .apiKey(null)
                .defaultModel("llama3")
                .enabled(true)
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();

        LlmProviderConfigEntity savedEntity = LlmProviderConfigEntity.builder()
                .id(TEST_UUID)
                .name("Ollama Local")
                .type("OLLAMA")
                .baseUrl("http://localhost:11434")
                .apiKey(null)
                .defaultModel("llama3")
                .enabled(true)
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();

        when(jpaRepository.save(any(LlmProviderConfigEntity.class))).thenReturn(savedEntity);

        // when
        LlmProviderConfig result = adapter.save(config);

        // then
        ArgumentCaptor<LlmProviderConfigEntity> captor = ArgumentCaptor.forClass(LlmProviderConfigEntity.class);
        verify(jpaRepository).save(captor.capture());

        LlmProviderConfigEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getName()).isEqualTo("Ollama Local");
        assertThat(captured.getType()).isEqualTo("OLLAMA");
        assertThat(captured.getBaseUrl()).isEqualTo("http://localhost:11434");
        assertThat(captured.isEnabled()).isTrue();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getName()).isEqualTo("Ollama Local");
        assertThat(result.getType()).isEqualTo(LlmProvider.OLLAMA);
        assertThat(result.getDefaultModel()).isEqualTo("llama3");
        assertThat(result.isEnabled()).isTrue();
    }

    @Test
    void findById_shouldReturnMappedConfig() {
        // given
        LlmProviderConfigEntity entity = LlmProviderConfigEntity.builder()
                .id(TEST_UUID)
                .name("OpenAI Cloud")
                .type("OPENAI")
                .baseUrl("https://api.openai.com")
                .apiKey("sk-test-key")
                .defaultModel("gpt-4")
                .enabled(true)
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();

        when(jpaRepository.findById(TEST_UUID)).thenReturn(Optional.of(entity));

        // when
        Optional<LlmProviderConfig> result = adapter.findById(TEST_UUID.toString());

        // then
        assertThat(result).isPresent();
        LlmProviderConfig config = result.get();
        assertThat(config.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(config.getName()).isEqualTo("OpenAI Cloud");
        assertThat(config.getType()).isEqualTo(LlmProvider.OPENAI);
        assertThat(config.getApiKey()).isEqualTo("sk-test-key");
        assertThat(config.getDefaultModel()).isEqualTo("gpt-4");
    }

    @Test
    void findAll_shouldReturnAll() {
        // given
        LlmProviderConfigEntity entity1 = LlmProviderConfigEntity.builder()
                .id(TEST_UUID)
                .name("Ollama")
                .type("OLLAMA")
                .baseUrl("http://localhost:11434")
                .enabled(true)
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();

        UUID secondUuid = UUID.fromString("d4e5f6a7-b8c9-0123-def0-234567890123");
        LlmProviderConfigEntity entity2 = LlmProviderConfigEntity.builder()
                .id(secondUuid)
                .name("Anthropic")
                .type("ANTHROPIC")
                .baseUrl("https://api.anthropic.com")
                .apiKey("ant-key")
                .enabled(false)
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();

        when(jpaRepository.findAll()).thenReturn(List.of(entity1, entity2));

        // when
        List<LlmProviderConfig> result = adapter.findAll();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getType()).isEqualTo(LlmProvider.OLLAMA);
        assertThat(result.get(1).getType()).isEqualTo(LlmProvider.ANTHROPIC);
        assertThat(result.get(1).isEnabled()).isFalse();
    }

    @Test
    void findByType_shouldReturnMatchingConfigs() {
        // given
        LlmProviderConfigEntity entity = LlmProviderConfigEntity.builder()
                .id(TEST_UUID)
                .name("Ollama Local")
                .type("OLLAMA")
                .baseUrl("http://localhost:11434")
                .enabled(true)
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();

        when(jpaRepository.findByType("OLLAMA")).thenReturn(List.of(entity));

        // when
        List<LlmProviderConfig> result = adapter.findByType(LlmProvider.OLLAMA);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(LlmProvider.OLLAMA);
        assertThat(result.get(0).getName()).isEqualTo("Ollama Local");
        verify(jpaRepository).findByType("OLLAMA");
    }

    @Test
    void deleteById_shouldDelegate() {
        // when
        adapter.deleteById(TEST_UUID.toString());

        // then
        verify(jpaRepository).deleteById(TEST_UUID);
    }
}
