package com.localmind.infrastructure.persistence.adapter;

import com.localmind.domain.llm.model.LlmProvider;
import com.localmind.domain.llm.model.LlmProviderConfig;
import com.localmind.domain.llm.port.out.ProviderConfigRepository;
import com.localmind.infrastructure.persistence.entity.LlmProviderConfigEntity;
import com.localmind.infrastructure.persistence.repository.JpaProviderConfigRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ProviderConfigRepositoryAdapter implements ProviderConfigRepository {

    private final JpaProviderConfigRepository jpaRepository;

    public ProviderConfigRepositoryAdapter(JpaProviderConfigRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public LlmProviderConfig save(LlmProviderConfig config) {
        LlmProviderConfigEntity entity = toEntity(config);
        LlmProviderConfigEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<LlmProviderConfig> findById(String id) {
        return jpaRepository.findById(UUID.fromString(id)).map(this::toDomain);
    }

    @Override
    public List<LlmProviderConfig> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<LlmProviderConfig> findByType(LlmProvider type) {
        return jpaRepository.findByType(type.name()).stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(UUID.fromString(id));
    }

    private LlmProviderConfigEntity toEntity(LlmProviderConfig config) {
        LlmProviderConfigEntity.LlmProviderConfigEntityBuilder builder = LlmProviderConfigEntity.builder()
                .name(config.getName())
                .type(config.getType().name())
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .defaultModel(config.getDefaultModel())
                .enabled(config.isEnabled())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt());

        if (config.getId() != null) {
            builder.id(UUID.fromString(config.getId()));
        }

        return builder.build();
    }

    private LlmProviderConfig toDomain(LlmProviderConfigEntity entity) {
        return LlmProviderConfig.builder()
                .id(entity.getId().toString())
                .name(entity.getName())
                .type(LlmProvider.valueOf(entity.getType()))
                .baseUrl(entity.getBaseUrl())
                .apiKey(entity.getApiKey())
                .defaultModel(entity.getDefaultModel())
                .enabled(entity.isEnabled())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
