package com.localmind.domain.llm.port.out;

import com.localmind.domain.llm.model.LlmProvider;
import com.localmind.domain.llm.model.LlmProviderConfig;

import java.util.List;
import java.util.Optional;

public interface ProviderConfigRepository {
    LlmProviderConfig save(LlmProviderConfig config);
    Optional<LlmProviderConfig> findById(String id);
    List<LlmProviderConfig> findAll();
    List<LlmProviderConfig> findByType(LlmProvider type);
    void deleteById(String id);
}
