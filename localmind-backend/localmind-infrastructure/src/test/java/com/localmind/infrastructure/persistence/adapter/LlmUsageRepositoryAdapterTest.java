package com.localmind.infrastructure.persistence.adapter;

import com.localmind.domain.llm.model.LlmProvider;
import com.localmind.domain.llm.model.TokenUsage;
import com.localmind.domain.llm.model.UsageRecord;
import com.localmind.infrastructure.persistence.entity.LlmUsageEntity;
import com.localmind.infrastructure.persistence.repository.JpaLlmUsageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LlmUsageRepositoryAdapterTest {

    @Mock
    private JpaLlmUsageRepository jpaRepository;

    @InjectMocks
    private LlmUsageRepositoryAdapter adapter;

    private final UUID TEST_UUID = UUID.fromString("a7b8c9d0-e1f2-3456-0123-567890123456");
    private final Instant NOW = Instant.parse("2026-02-05T12:00:00Z");

    @Test
    void save_shouldMapAndDelegate() {
        // given
        UsageRecord record = UsageRecord.builder()
                .provider(LlmProvider.OLLAMA)
                .model("llama3")
                .tokenUsage(TokenUsage.builder()
                        .promptTokens(100)
                        .completionTokens(200)
                        .totalTokens(300)
                        .build())
                .cost(0.005)
                .latencyMs(1500L)
                .timestamp(NOW)
                .build();

        when(jpaRepository.save(any(LlmUsageEntity.class))).thenAnswer(invocation -> {
            LlmUsageEntity entity = invocation.getArgument(0);
            entity.setId(TEST_UUID);
            return entity;
        });

        // when
        adapter.save(record);

        // then
        ArgumentCaptor<LlmUsageEntity> captor = ArgumentCaptor.forClass(LlmUsageEntity.class);
        verify(jpaRepository).save(captor.capture());

        LlmUsageEntity captured = captor.getValue();
        assertThat(captured.getProvider()).isEqualTo("OLLAMA");
        assertThat(captured.getModel()).isEqualTo("llama3");
        assertThat(captured.getPromptTokens()).isEqualTo(100);
        assertThat(captured.getCompletionTokens()).isEqualTo(200);
        assertThat(captured.getTotalTokens()).isEqualTo(300);
        assertThat(captured.getCost()).isEqualTo(0.005);
        assertThat(captured.getLatencyMs()).isEqualTo(1500L);
        assertThat(captured.getTimestamp()).isEqualTo(NOW);
    }

    @Test
    void findByDateRange_shouldReturnMappedRecords() {
        // given
        Instant from = Instant.parse("2026-02-01T00:00:00Z");
        Instant to = Instant.parse("2026-02-28T23:59:59Z");

        LlmUsageEntity entity1 = LlmUsageEntity.builder()
                .id(TEST_UUID)
                .provider("OLLAMA")
                .model("llama3")
                .promptTokens(50)
                .completionTokens(150)
                .totalTokens(200)
                .cost(0.003)
                .latencyMs(800L)
                .timestamp(Instant.parse("2026-02-10T09:00:00Z"))
                .build();

        UUID secondUuid = UUID.fromString("b8c9d0e1-f2a3-4567-1234-678901234567");
        LlmUsageEntity entity2 = LlmUsageEntity.builder()
                .id(secondUuid)
                .provider("OPENAI")
                .model("gpt-4")
                .promptTokens(200)
                .completionTokens(500)
                .totalTokens(700)
                .cost(0.02)
                .latencyMs(2000L)
                .timestamp(Instant.parse("2026-02-15T14:30:00Z"))
                .build();

        when(jpaRepository.findByTimestampBetween(from, to)).thenReturn(List.of(entity1, entity2));

        // when
        List<UsageRecord> result = adapter.findByDateRange(from, to);

        // then
        assertThat(result).hasSize(2);

        UsageRecord first = result.get(0);
        assertThat(first.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(first.getProvider()).isEqualTo(LlmProvider.OLLAMA);
        assertThat(first.getModel()).isEqualTo("llama3");
        assertThat(first.getTokenUsage().getPromptTokens()).isEqualTo(50);
        assertThat(first.getTokenUsage().getCompletionTokens()).isEqualTo(150);
        assertThat(first.getTokenUsage().getTotalTokens()).isEqualTo(200);
        assertThat(first.getCost()).isEqualTo(0.003);
        assertThat(first.getLatencyMs()).isEqualTo(800L);

        UsageRecord second = result.get(1);
        assertThat(second.getProvider()).isEqualTo(LlmProvider.OPENAI);
        assertThat(second.getModel()).isEqualTo("gpt-4");
        assertThat(second.getTokenUsage().getTotalTokens()).isEqualTo(700);
    }

    @Test
    void findAll_shouldReturnAll() {
        // given
        LlmUsageEntity entity = LlmUsageEntity.builder()
                .id(TEST_UUID)
                .provider("ANTHROPIC")
                .model("claude-3")
                .promptTokens(300)
                .completionTokens(400)
                .totalTokens(700)
                .cost(0.01)
                .latencyMs(1200L)
                .timestamp(NOW)
                .build();

        when(jpaRepository.findAll()).thenReturn(List.of(entity));

        // when
        List<UsageRecord> result = adapter.findAll();

        // then
        assertThat(result).hasSize(1);
        UsageRecord record = result.get(0);
        assertThat(record.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(record.getProvider()).isEqualTo(LlmProvider.ANTHROPIC);
        assertThat(record.getModel()).isEqualTo("claude-3");
        assertThat(record.getTokenUsage()).isNotNull();
        assertThat(record.getTokenUsage().getPromptTokens()).isEqualTo(300);
        assertThat(record.getTokenUsage().getCompletionTokens()).isEqualTo(400);
        assertThat(record.getTokenUsage().getTotalTokens()).isEqualTo(700);
    }
}
