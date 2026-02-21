package com.localmind.domain.llm.port.out;

import com.localmind.domain.llm.model.OllamaStatus;
import com.localmind.domain.llm.model.PullProgress;

import java.util.List;
import java.util.function.Consumer;

public interface OllamaModelPort {
    List<String> listAvailableModels(String baseUrl);
    void pullModel(String baseUrl, String modelName);
    void pullModelStreaming(String baseUrl, String modelName, Consumer<PullProgress> progressCallback);
    void deleteModel(String baseUrl, String modelName);
    OllamaStatus checkStatus(String baseUrl);
    void startOllama();
}
