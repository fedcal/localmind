package com.localmind.domain.marketplace.port.out;

import com.localmind.domain.marketplace.model.AgentCategory;
import com.localmind.domain.marketplace.model.MarketplaceAgent;

import java.util.List;
import java.util.Optional;

public interface MarketplaceAgentRepository {

    MarketplaceAgent save(MarketplaceAgent agent);

    Optional<MarketplaceAgent> findById(String id);

    List<MarketplaceAgent> findAll();

    List<MarketplaceAgent> findByCategory(AgentCategory category);

    List<MarketplaceAgent> searchByName(String query);

    void deleteById(String id);

    void incrementDownloadCount(String id);
}
