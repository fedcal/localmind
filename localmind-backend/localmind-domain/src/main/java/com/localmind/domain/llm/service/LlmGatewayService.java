package com.localmind.domain.llm.service;

import com.localmind.domain.common.exception.LlmProviderException;
import com.localmind.domain.llm.model.*;
import com.localmind.domain.llm.port.in.ChatUseCase;
import com.localmind.domain.llm.port.out.LlmClient;
import com.localmind.domain.llm.port.out.LlmUsageRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LlmGatewayService implements ChatUseCase {

    private final Map<LlmProvider, LlmClient> clients;
    private final LlmUsageRepository usageRepository;
    private final List<LlmProvider> fallbackOrder;
    private final LlmProvider defaultProvider;

    public LlmGatewayService(List<LlmClient> clientList,
                              LlmUsageRepository usageRepository,
                              List<LlmProvider> fallbackOrder,
                              LlmProvider defaultProvider) {
        this.clients = clientList.stream()
                .collect(Collectors.toMap(LlmClient::getProvider, c -> c));
        this.usageRepository = usageRepository;
        this.fallbackOrder = fallbackOrder;
        this.defaultProvider = defaultProvider;
    }

    @Override
    public LlmResponse chat(LlmRequest request) {
        LlmProvider provider = request.getProvider() != null ? request.getProvider() : defaultProvider;
        List<LlmProvider> providersToTry = buildProviderChain(provider);

        LlmProviderException lastException = null;

        for (LlmProvider p : providersToTry) {
            LlmClient client = clients.get(p);
            if (client == null || !client.isAvailable()) {
                continue;
            }
            try {
                long start = System.currentTimeMillis();
                LlmRequest adjustedRequest = LlmRequest.builder()
                        .messages(request.getMessages())
                        .provider(p)
                        .model(request.getModel())
                        .temperature(request.getTemperature())
                        .maxTokens(request.getMaxTokens())
                        .conversationId(request.getConversationId())
                        .stream(request.isStream())
                        .build();
                LlmResponse response = client.call(adjustedRequest);
                long latency = System.currentTimeMillis() - start;
                response.setLatencyMs(latency);
                response.setProvider(p);

                trackUsage(response, p, latency);
                return response;
            } catch (Exception e) {
                lastException = new LlmProviderException(
                        "Provider " + p.getDisplayName() + " failed: " + e.getMessage(), e);
            }
        }

        throw lastException != null ? lastException
                : new LlmProviderException("No LLM provider available");
    }

    private List<LlmProvider> buildProviderChain(LlmProvider preferred) {
        List<LlmProvider> chain = new java.util.ArrayList<>();
        chain.add(preferred);
        for (LlmProvider p : fallbackOrder) {
            if (!chain.contains(p)) {
                chain.add(p);
            }
        }
        return chain;
    }

    private void trackUsage(LlmResponse response, LlmProvider provider, long latencyMs) {
        if (response.getTokenUsage() != null) {
            UsageRecord record = UsageRecord.builder()
                    .provider(provider)
                    .model(response.getModel())
                    .tokenUsage(response.getTokenUsage())
                    .latencyMs(latencyMs)
                    .build();
            usageRepository.save(record);
        }
    }
}
