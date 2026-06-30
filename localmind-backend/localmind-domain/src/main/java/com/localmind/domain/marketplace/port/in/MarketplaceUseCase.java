package com.localmind.domain.marketplace.port.in;

import com.localmind.domain.marketplace.model.AgentCategory;
import com.localmind.domain.marketplace.model.MarketplaceAgent;
import com.localmind.domain.marketplace.model.MarketplaceReview;

import java.util.List;
import java.util.Optional;

public interface MarketplaceUseCase {

    List<MarketplaceAgent> listAgents(AgentCategory category, String searchQuery);

    Optional<MarketplaceAgent> getAgent(String id);

    MarketplaceAgent publishAgent(MarketplaceAgent agent);

    void installAgent(String agentId);

    MarketplaceReview addReview(String agentId, int rating, String comment);

    List<MarketplaceReview> getReviews(String agentId);

    void deleteAgent(String id);
}
