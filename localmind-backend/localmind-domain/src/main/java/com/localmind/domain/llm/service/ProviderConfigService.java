package com.localmind.domain.llm.service;

import com.localmind.domain.common.exception.ResourceNotFoundException;
import com.localmind.domain.llm.model.LlmProviderConfig;
import com.localmind.domain.llm.port.in.ProviderConfigUseCase;
import com.localmind.domain.llm.port.out.OllamaModelPort;
import com.localmind.domain.llm.port.out.ProviderConfigRepository;

import java.util.List;

public class ProviderConfigService implements ProviderConfigUseCase {

    private final ProviderConfigRepository repository;
    private final OllamaModelPort ollamaModelPort;

    public ProviderConfigService(ProviderConfigRepository repository, OllamaModelPort ollamaModelPort) {
        this.repository = repository;
        this.ollamaModelPort = ollamaModelPort;
    }

    @Override
    public LlmProviderConfig save(LlmProviderConfig config) {
        return repository.save(config);
    }

    @Override
    public List<LlmProviderConfig> listAll() {
        return repository.findAll();
    }

    @Override
    public LlmProviderConfig getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Provider config not found: " + id));
    }

    @Override
    public void deleteById(String id) {
        repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Provider config not found: " + id));
        repository.deleteById(id);
    }

    @Override
    public TestResult testConnection(String id) {
        LlmProviderConfig config = getById(id);
        try {
            switch (config.getType()) {
                case OLLAMA -> {
                    List<String> models = ollamaModelPort.listAvailableModels(config.getBaseUrl());
                    return new TestResult("OK", "Connesso. " + models.size() + " modelli disponibili.");
                }
                case OPENAI, ANTHROPIC, GOOGLE -> {
                    if (config.getApiKey() == null || config.getApiKey().isBlank()) {
                        return new TestResult("ERROR", "API Key non configurata.");
                    }
                    return new TestResult("OK", "Configurazione valida.");
                }
                default -> {
                    return new TestResult("ERROR", "Tipo provider sconosciuto.");
                }
            }
        } catch (Exception e) {
            return new TestResult("ERROR", "Connessione fallita: " + e.getMessage());
        }
    }

    @Override
    public List<String> listOllamaModels(String baseUrl) {
        return ollamaModelPort.listAvailableModels(baseUrl);
    }
}
