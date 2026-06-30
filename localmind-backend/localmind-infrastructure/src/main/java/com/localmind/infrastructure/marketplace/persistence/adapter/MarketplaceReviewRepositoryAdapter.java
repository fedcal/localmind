package com.localmind.infrastructure.marketplace.persistence.adapter;

import com.localmind.domain.marketplace.model.MarketplaceReview;
import com.localmind.domain.marketplace.port.out.MarketplaceReviewRepository;
import com.localmind.infrastructure.marketplace.persistence.entity.MarketplaceReviewEntity;
import com.localmind.infrastructure.marketplace.persistence.repository.MarketplaceReviewJpaRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class MarketplaceReviewRepositoryAdapter implements MarketplaceReviewRepository {

    private final MarketplaceReviewJpaRepository jpaRepository;

    public MarketplaceReviewRepositoryAdapter(MarketplaceReviewJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public MarketplaceReview save(MarketplaceReview review) {
        MarketplaceReviewEntity entity = toEntity(review);
        MarketplaceReviewEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<MarketplaceReview> findByAgentId(String agentId) {
        return jpaRepository.findByAgentId(agentId).stream().map(this::toDomain).toList();
    }

    @Override
    public double calculateAverageRating(String agentId) {
        List<MarketplaceReviewEntity> reviews = jpaRepository.findByAgentId(agentId);
        if (reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream()
                .mapToInt(MarketplaceReviewEntity::getRating)
                .average()
                .orElse(0.0);
    }

    private MarketplaceReviewEntity toEntity(MarketplaceReview review) {
        MarketplaceReviewEntity.MarketplaceReviewEntityBuilder builder = MarketplaceReviewEntity.builder()
                .agentId(review.getAgentId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt() != null ? review.getCreatedAt() : Instant.now());

        if (review.getId() != null) {
            builder.id(UUID.fromString(review.getId()));
        }

        return builder.build();
    }

    private MarketplaceReview toDomain(MarketplaceReviewEntity entity) {
        return MarketplaceReview.builder()
                .id(entity.getId().toString())
                .agentId(entity.getAgentId())
                .rating(entity.getRating())
                .comment(entity.getComment())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
