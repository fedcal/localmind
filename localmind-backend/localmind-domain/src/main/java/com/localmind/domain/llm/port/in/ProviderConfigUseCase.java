package com.localmind.domain.llm.port.in;

import com.localmind.domain.llm.model.LlmProviderConfig;

import java.util.List;

public interface ProviderConfigUseCase {
    LlmProviderConfig save(LlmProviderConfig config);
    List<LlmProviderConfig> listAll();
    LlmProviderConfig getById(String id);
    void deleteById(String id);
    TestResult testConnection(String id);
    List<String> listOllamaModels(String baseUrl);

    record TestResult(String status, String message) {}
}
