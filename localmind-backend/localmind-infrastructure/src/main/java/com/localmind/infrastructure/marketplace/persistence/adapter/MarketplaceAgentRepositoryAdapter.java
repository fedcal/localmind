package com.localmind.infrastructure.marketplace.persistence.adapter;

import com.localmind.domain.marketplace.model.AgentCategory;
import com.localmind.domain.marketplace.model.MarketplaceAgent;
import com.localmind.domain.marketplace.port.out.MarketplaceAgentRepository;
import com.localmind.infrastructure.marketplace.persistence.entity.MarketplaceAgentEntity;
import com.localmind.infrastructure.marketplace.persistence.repository.MarketplaceAgentJpaRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class MarketplaceAgentRepositoryAdapter implements MarketplaceAgentRepository {

    private final MarketplaceAgentJpaRepository jpaRepository;

    public MarketplaceAgentRepositoryAdapter(MarketplaceAgentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public MarketplaceAgent save(MarketplaceAgent agent) {
        MarketplaceAgentEntity entity = toEntity(agent);
        MarketplaceAgentEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<MarketplaceAgent> findById(String id) {
        return jpaRepository.findById(UUID.fromString(id)).map(this::toDomain);
    }

    @Override
    public List<MarketplaceAgent> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<MarketplaceAgent> findByCategory(AgentCategory category) {
        return jpaRepository.findByCategory(category.name()).stream().map(this::toDomain).toList();
    }

    @Override
    public List<MarketplaceAgent> searchByName(String query) {
        return jpaRepository.findByNameContainingIgnoreCase(query).stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(UUID.fromString(id));
    }

    @Override
    public void incrementDownloadCount(String id) {
        jpaRepository.incrementDownloadCount(UUID.fromString(id));
    }

    private MarketplaceAgentEntity toEntity(MarketplaceAgent agent) {
        MarketplaceAgentEntity.MarketplaceAgentEntityBuilder builder = MarketplaceAgentEntity.builder()
                .name(agent.getName())
                .description(agent.getDescription())
                .author(agent.getAuthor())
                .version(agent.getVersion())
                .category(agent.getCategory().name())
                .downloadUrl(agent.getDownloadUrl())
                .downloadCount(agent.getDownloadCount())
                .avgRating(agent.getAvgRating())
                .publishedAt(agent.getPublishedAt())
                .createdAt(agent.getCreatedAt() != null ? agent.getCreatedAt() : Instant.now());

        if (agent.getId() != null) {
            builder.id(UUID.fromString(agent.getId()));
        }

        return builder.build();
    }

    private MarketplaceAgent toDomain(MarketplaceAgentEntity entity) {
        return MarketplaceAgent.builder()
                .id(entity.getId().toString())
                .name(entity.getName())
                .description(entity.getDescription())
                .author(entity.getAuthor())
                .version(entity.getVersion())
                .category(AgentCategory.valueOf(entity.getCategory()))
                .downloadUrl(entity.getDownloadUrl())
                .downloadCount(entity.getDownloadCount())
                .avgRating(entity.getAvgRating())
                .publishedAt(entity.getPublishedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
