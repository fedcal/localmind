package com.localmind.domain.llm.port.in;

import com.localmind.domain.llm.model.LlmProviderConfig;
import com.localmind.domain.llm.model.OllamaStatus;
import com.localmind.domain.llm.model.PullProgress;

import java.util.List;
import java.util.function.Consumer;

public interface ProviderConfigUseCase {
    LlmProviderConfig save(LlmProviderConfig config);
    List<LlmProviderConfig> listAll();
    LlmProviderConfig getById(String id);
    void deleteById(String id);
    TestResult testConnection(String id);
    List<String> listOllamaModels(String baseUrl);
    void pullOllamaModel(String baseUrl, String modelName);
    void pullOllamaModelStreaming(String baseUrl, String modelName, Consumer<PullProgress> progressCallback);
    void deleteOllamaModel(String baseUrl, String modelName);
    LlmProviderConfig updateDefaultModel(String providerId, String modelName);
    OllamaStatus checkOllamaStatus(String baseUrl);
    void startOllama();

    record TestResult(String status, String message) {}
}
