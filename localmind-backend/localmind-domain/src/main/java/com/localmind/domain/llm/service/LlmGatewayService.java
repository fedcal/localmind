package com.localmind.domain.llm.service;

import com.localmind.domain.common.event.ConversationCompletedEvent;
import com.localmind.domain.common.exception.LlmProviderException;
import com.localmind.domain.common.port.out.DomainEventPublisherPort;
import com.localmind.domain.llm.model.*;
import com.localmind.domain.llm.port.in.ChatUseCase;
import com.localmind.domain.llm.port.in.StreamingChatUseCase;
import com.localmind.domain.llm.port.out.LlmClient;
import com.localmind.domain.llm.port.out.LlmUsageRepository;
import com.localmind.domain.llm.port.out.ProviderConfigRepository;
import com.localmind.domain.llm.port.out.StreamingLlmClient;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class LlmGatewayService implements ChatUseCase, StreamingChatUseCase {

    private final Map<LlmProvider, LlmClient> clients;
    private final Map<LlmProvider, StreamingLlmClient> streamingClients;
    private final LlmUsageRepository usageRepository;
    private final ProviderConfigRepository providerConfigRepository;
    private final List<LlmProvider> fallbackOrder;
    private final LlmProvider defaultProvider;
    private final DomainEventPublisherPort eventPublisher;

    public LlmGatewayService(List<LlmClient> clientList,
                              List<StreamingLlmClient> streamingClientList,
                              LlmUsageRepository usageRepository,
                              ProviderConfigRepository providerConfigRepository,
                              List<LlmProvider> fallbackOrder,
                              LlmProvider defaultProvider) {
        this(clientList, streamingClientList, usageRepository, providerConfigRepository,
                fallbackOrder, defaultProvider, null);
    }

    public LlmGatewayService(List<LlmClient> clientList,
                              List<StreamingLlmClient> streamingClientList,
                              LlmUsageRepository usageRepository,
                              ProviderConfigRepository providerConfigRepository,
                              List<LlmProvider> fallbackOrder,
                              LlmProvider defaultProvider,
                              DomainEventPublisherPort eventPublisher) {
        this.clients = clientList.stream()
                .collect(Collectors.toMap(LlmClient::getProvider, c -> c));
        this.streamingClients = streamingClientList != null
                ? streamingClientList.stream()
                        .collect(Collectors.toMap(StreamingLlmClient::getProvider, c -> c))
                : Map.of();
        this.usageRepository = usageRepository;
        this.providerConfigRepository = providerConfigRepository;
        this.fallbackOrder = fallbackOrder;
        this.defaultProvider = defaultProvider;
        this.eventPublisher = eventPublisher;
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
                String model = resolveModel(request.getModel(), p);

                long start = System.currentTimeMillis();
                LlmRequest adjustedRequest = LlmRequest.builder()
                        .messages(request.getMessages())
                        .provider(p)
                        .model(model)
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
                publishConversationEvent(adjustedRequest, response, p);
                return response;
            } catch (Exception e) {
                lastException = new LlmProviderException(
                        "Provider " + p.getDisplayName() + " failed: " + e.getMessage(), e);
            }
        }

        throw lastException != null ? lastException
                : new LlmProviderException("No LLM provider available");
    }

    @Override
    public void streamChat(LlmRequest request,
                           Consumer<String> onToken,
                           Consumer<LlmResponse> onComplete,
                           Consumer<Exception> onError) {
        LlmProvider provider = request.getProvider() != null ? request.getProvider() : defaultProvider;
        List<LlmProvider> providersToTry = buildProviderChain(provider);

        LlmProviderException lastException = null;

        for (LlmProvider p : providersToTry) {
            StreamingLlmClient streamingClient = streamingClients.get(p);
            if (streamingClient == null || !streamingClient.isAvailable()) {
                continue;
            }
            try {
                String model = resolveModel(request.getModel(), p);
                long start = System.currentTimeMillis();

                LlmRequest adjustedRequest = LlmRequest.builder()
                        .messages(request.getMessages())
                        .provider(p)
                        .model(model)
                        .temperature(request.getTemperature())
                        .maxTokens(request.getMaxTokens())
                        .conversationId(request.getConversationId())
                        .stream(true)
                        .build();

                final LlmProvider finalProvider = p;
                streamingClient.streamResponse(adjustedRequest, onToken, tokenUsage -> {
                    long latency = System.currentTimeMillis() - start;
                    LlmResponse response = LlmResponse.builder()
                            .model(model)
                            .provider(finalProvider)
                            .tokenUsage(tokenUsage)
                            .latencyMs(latency)
                            .build();
                    trackUsage(response, finalProvider, latency);
                    onComplete.accept(response);
                }, onError);
                return;
            } catch (Exception e) {
                lastException = new LlmProviderException(
                        "Provider " + p.getDisplayName() + " failed: " + e.getMessage(), e);
            }
        }

        Exception error = lastException != null ? lastException
                : new LlmProviderException("No streaming LLM provider available");
        onError.accept(error);
    }

    private String resolveModel(String requestModel, LlmProvider provider) {
        if (requestModel != null && !requestModel.isBlank()) {
            return requestModel;
        }
        List<LlmProviderConfig> configs = providerConfigRepository.findByType(provider);
        return configs.stream()
                .filter(LlmProviderConfig::isEnabled)
                .map(LlmProviderConfig::getDefaultModel)
                .filter(m -> m != null && !m.isBlank())
                .findFirst()
                .orElse(null);
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

    private void publishConversationEvent(LlmRequest request, LlmResponse response, LlmProvider provider) {
        if (eventPublisher == null) {
            return;
        }
        long tokens = 0;
        if (response.getTokenUsage() != null) {
            tokens = response.getTokenUsage().getTotalTokens();
        }
        int messageCount = request.getMessages() != null ? request.getMessages().size() : 0;
        eventPublisher.publish(new ConversationCompletedEvent(
                request.getConversationId(),
                messageCount,
                provider.name(),
                response.getModel(),
                tokens));
    }
}
