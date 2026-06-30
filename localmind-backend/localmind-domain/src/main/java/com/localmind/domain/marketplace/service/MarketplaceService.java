package com.localmind.domain.marketplace.service;

import com.localmind.domain.common.exception.LocalMindException;
import com.localmind.domain.common.exception.ResourceNotFoundException;
import com.localmind.domain.marketplace.model.AgentCategory;
import com.localmind.domain.marketplace.model.MarketplaceAgent;
import com.localmind.domain.marketplace.model.MarketplaceReview;
import com.localmind.domain.marketplace.port.in.MarketplaceUseCase;
import com.localmind.domain.marketplace.port.out.MarketplaceAgentRepository;
import com.localmind.domain.marketplace.port.out.MarketplaceReviewRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MarketplaceService implements MarketplaceUseCase {

    private final MarketplaceAgentRepository agentRepository;
    private final MarketplaceReviewRepository reviewRepository;

    public MarketplaceService(MarketplaceAgentRepository agentRepository,
                              MarketplaceReviewRepository reviewRepository) {
        this.agentRepository = agentRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    public List<MarketplaceAgent> listAgents(AgentCategory category, String searchQuery) {
        if (category != null) {
            return agentRepository.findByCategory(category);
        }
        if (searchQuery != null && !searchQuery.isBlank()) {
            return agentRepository.searchByName(searchQuery);
        }
        return agentRepository.findAll();
    }

    @Override
    public Optional<MarketplaceAgent> getAgent(String id) {
        return agentRepository.findById(id);
    }

    @Override
    public MarketplaceAgent publishAgent(MarketplaceAgent agent) {
        if (agent.getName() == null || agent.getName().isBlank()) {
            throw new LocalMindException("Agent name is required");
        }
        if (agent.getCategory() == null) {
            throw new LocalMindException("Agent category is required");
        }
        if (agent.getId() == null || agent.getId().isBlank()) {
            agent.setId(UUID.randomUUID().toString());
        }
        if (agent.getCreatedAt() == null) {
            agent.setCreatedAt(Instant.now());
        }
        if (agent.getPublishedAt() == null) {
            agent.setPublishedAt(Instant.now());
        }
        return agentRepository.save(agent);
    }

    @Override
    public void installAgent(String agentId) {
        agentRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found: " + agentId));
        agentRepository.incrementDownloadCount(agentId);
    }

    @Override
    public MarketplaceReview addReview(String agentId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new LocalMindException("Rating must be between 1 and 5");
        }
        agentRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found: " + agentId));

        MarketplaceReview review = MarketplaceReview.builder()
                .id(UUID.randomUUID().toString())
                .agentId(agentId)
                .rating(rating)
                .comment(comment)
                .createdAt(Instant.now())
                .build();

        MarketplaceReview saved = reviewRepository.save(review);

        double avgRating = reviewRepository.calculateAverageRating(agentId);
        MarketplaceAgent agent = agentRepository.findById(agentId).get();
        agent.setAvgRating(avgRating);
        agentRepository.save(agent);

        return saved;
    }

    @Override
    public List<MarketplaceReview> getReviews(String agentId) {
        return reviewRepository.findByAgentId(agentId);
    }

    @Override
    public void deleteAgent(String id) {
        agentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found: " + id));
        agentRepository.deleteById(id);
    }
}
