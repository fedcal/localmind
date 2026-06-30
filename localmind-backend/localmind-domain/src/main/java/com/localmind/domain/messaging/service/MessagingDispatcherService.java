package com.localmind.domain.messaging.service;

import com.localmind.domain.llm.model.ChatMessage;
import com.localmind.domain.llm.model.LlmProvider;
import com.localmind.domain.llm.model.LlmRequest;
import com.localmind.domain.llm.model.LlmResponse;
import com.localmind.domain.llm.port.in.ChatUseCase;
import com.localmind.domain.messaging.model.*;
import com.localmind.domain.messaging.port.in.MessagingReceiveUseCase;
import com.localmind.domain.messaging.port.out.ChannelContextRepository;
import com.localmind.domain.messaging.port.out.MessagingChannelRepository;
import com.localmind.domain.messaging.port.out.MessagingClientPort;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class MessagingDispatcherService implements MessagingReceiveUseCase {

    private final MessagingChannelRepository channelRepository;
    private final ChannelContextRepository contextRepository;
    private final ChatUseCase chatUseCase;
    private final Map<MessagingPlatform, MessagingClientPort> clients;

    public MessagingDispatcherService(MessagingChannelRepository channelRepository,
                                       ChannelContextRepository contextRepository,
                                       ChatUseCase chatUseCase,
                                       List<MessagingClientPort> clientList) {
        this.channelRepository = channelRepository;
        this.contextRepository = contextRepository;
        this.chatUseCase = chatUseCase;
        this.clients = clientList != null
                ? clientList.stream().collect(Collectors.toMap(MessagingClientPort::getPlatform, c -> c))
                : Map.of();
    }

    @Override
    public void receiveMessage(InboundMessage message) {
        MessagingChannel channel = channelRepository.findActivePlatform(message.getPlatform())
                .orElseThrow(() -> new IllegalStateException(
                        "No active channel for platform: " + message.getPlatform()));

        ChannelContext context = resolveOrCreateContext(message, channel);

        List<ChatMessage> messages = buildMessages(channel, message);

        LlmProvider provider = channel.getDefaultProvider() != null
                ? LlmProvider.valueOf(channel.getDefaultProvider())
                : null;

        LlmRequest llmRequest = LlmRequest.builder()
                .messages(messages)
                .provider(provider)
                .model(channel.getDefaultModel())
                .conversationId(context.getConversationId())
                .build();

        LlmResponse response = chatUseCase.chat(llmRequest);

        context.setLastActivity(LocalDateTime.now());
        if (context.getConversationId() == null && llmRequest.getConversationId() != null) {
            context.setConversationId(llmRequest.getConversationId());
        }
        contextRepository.save(context);

        MessagingClientPort client = clients.get(message.getPlatform());
        if (client != null) {
            OutboundMessage outbound = OutboundMessage.builder()
                    .platform(message.getPlatform())
                    .channelId(message.getPlatformChannelId())
                    .text(response.getContent())
                    .replyToMessageId(message.getMessageId())
                    .botToken(channel.getBotToken())
                    .build();
            client.sendMessage(outbound);
        }
    }

    private ChannelContext resolveOrCreateContext(InboundMessage message, MessagingChannel channel) {
        return contextRepository.findByPlatformAndUserAndChannel(
                message.getPlatform(), message.getPlatformUserId(), message.getPlatformChannelId())
                .orElseGet(() -> {
                    ChannelContext newContext = ChannelContext.builder()
                            .id(UUID.randomUUID().toString())
                            .channelId(channel.getId())
                            .platform(message.getPlatform())
                            .platformUserId(message.getPlatformUserId())
                            .platformChannelId(message.getPlatformChannelId())
                            .createdAt(LocalDateTime.now())
                            .lastActivity(LocalDateTime.now())
                            .build();
                    return contextRepository.save(newContext);
                });
    }

    private List<ChatMessage> buildMessages(MessagingChannel channel, InboundMessage message) {
        List<ChatMessage> messages = new ArrayList<>();
        if (channel.getDefaultSystemPrompt() != null && !channel.getDefaultSystemPrompt().isBlank()) {
            messages.add(ChatMessage.builder()
                    .role(ChatMessage.Role.SYSTEM)
                    .content(channel.getDefaultSystemPrompt())
                    .build());
        }
        messages.add(ChatMessage.builder()
                .role(ChatMessage.Role.USER)
                .content(message.getText())
                .build());
        return messages;
    }
}
