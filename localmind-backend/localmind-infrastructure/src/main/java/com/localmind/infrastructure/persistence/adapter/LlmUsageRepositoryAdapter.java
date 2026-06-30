package com.localmind.infrastructure.persistence.adapter;

import com.localmind.domain.llm.model.LlmProvider;
import com.localmind.domain.llm.model.TokenUsage;
import com.localmind.domain.llm.model.UsageRecord;
import com.localmind.domain.llm.port.out.LlmUsageRepository;
import com.localmind.infrastructure.persistence.entity.LlmUsageEntity;
import com.localmind.infrastructure.persistence.repository.JpaLlmUsageRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class LlmUsageRepositoryAdapter implements LlmUsageRepository {

    private final JpaLlmUsageRepository jpaRepository;

    public LlmUsageRepositoryAdapter(JpaLlmUsageRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(UsageRecord record) {
        LlmUsageEntity entity = LlmUsageEntity.builder()
                .provider(record.getProvider().name())
                .model(record.getModel())
                .promptTokens(record.getTokenUsage() != null ? record.getTokenUsage().getPromptTokens() : 0)
                .completionTokens(record.getTokenUsage() != null ? record.getTokenUsage().getCompletionTokens() : 0)
                .totalTokens(record.getTokenUsage() != null ? record.getTokenUsage().getTotalTokens() : 0)
                .cost(record.getCost())
                .latencyMs(record.getLatencyMs())
                .timestamp(record.getTimestamp())
                .build();
        jpaRepository.save(entity);
    }

    @Override
    public List<UsageRecord> findByDateRange(Instant from, Instant to) {
        return jpaRepository.findByTimestampBetween(from, to).stream()
                .map(this::toUsageRecord)
                .toList();
    }

    @Override
    public List<UsageRecord> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toUsageRecord)
                .toList();
    }

    private UsageRecord toUsageRecord(LlmUsageEntity entity) {
        return UsageRecord.builder()
                .id(entity.getId().toString())
                .provider(LlmProvider.valueOf(entity.getProvider()))
                .model(entity.getModel())
                .tokenUsage(TokenUsage.builder()
                        .promptTokens(entity.getPromptTokens())
                        .completionTokens(entity.getCompletionTokens())
                        .totalTokens(entity.getTotalTokens())
                        .build())
                .cost(entity.getCost())
                .latencyMs(entity.getLatencyMs())
                .timestamp(entity.getTimestamp())
                .build();
    }
}
