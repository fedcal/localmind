package com.localmind.domain.llm.service;

import com.localmind.domain.common.exception.ResourceNotFoundException;
import com.localmind.domain.llm.model.LlmModel;
import com.localmind.domain.llm.model.LlmProvider;
import com.localmind.domain.llm.port.in.ModelManagementUseCase;
import com.localmind.domain.llm.port.out.LlmClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModelManagementService implements ModelManagementUseCase {

    private final Map<LlmProvider, LlmClient> clients;

    public ModelManagementService(List<LlmClient> clientList) {
        this.clients = clientList.stream()
                .collect(Collectors.toMap(LlmClient::getProvider, c -> c));
    }

    @Override
    public List<LlmModel> listModels() {
        return clients.entrySet().stream()
                .map(entry -> toLlmModel(entry.getKey(), entry.getValue()))
                .toList();
    }

    @Override
    public List<LlmModel> listModelsByProvider(LlmProvider provider) {
        LlmClient client = clients.get(provider);
        if (client == null) {
            return List.of();
        }
        return List.of(toLlmModel(provider, client));
    }

    @Override
    public LlmModel getModel(String modelId) {
        return clients.entrySet().stream()
                .map(entry -> toLlmModel(entry.getKey(), entry.getValue()))
                .filter(m -> m.getId().equals(modelId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Model not found: " + modelId));
    }

    private LlmModel toLlmModel(LlmProvider provider, LlmClient client) {
        return LlmModel.builder()
                .id(provider.name().toLowerCase())
                .name(provider.getDisplayName())
                .provider(provider)
                .available(client.isAvailable())
                .build();
    }
}
