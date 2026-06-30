package com.localmind.infrastructure.persistence.adapter.messaging;

import com.localmind.domain.messaging.model.MessagingChannel;
import com.localmind.domain.messaging.model.MessagingPlatform;
import com.localmind.domain.messaging.port.out.MessagingChannelRepository;
import com.localmind.infrastructure.persistence.entity.messaging.MessagingChannelEntity;
import com.localmind.infrastructure.persistence.repository.messaging.JpaMessagingChannelRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class MessagingChannelRepositoryAdapter implements MessagingChannelRepository {

    private final JpaMessagingChannelRepository jpaRepository;

    public MessagingChannelRepositoryAdapter(JpaMessagingChannelRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public MessagingChannel save(MessagingChannel channel) {
        MessagingChannelEntity entity = toEntity(channel);
        MessagingChannelEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<MessagingChannel> findById(String id) {
        return jpaRepository.findById(UUID.fromString(id)).map(this::toDomain);
    }

    @Override
    public List<MessagingChannel> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<MessagingChannel> findByPlatform(MessagingPlatform platform) {
        return jpaRepository.findByPlatform(platform).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<MessagingChannel> findActivePlatform(MessagingPlatform platform) {
        return jpaRepository.findByPlatformAndActiveTrue(platform).map(this::toDomain);
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(UUID.fromString(id));
    }

    private MessagingChannelEntity toEntity(MessagingChannel domain) {
        MessagingChannelEntity.MessagingChannelEntityBuilder builder = MessagingChannelEntity.builder()
                .name(domain.getName())
                .platform(domain.getPlatform())
                .botToken(domain.getBotToken())
                .webhookSecret(domain.getWebhookSecret())
                .active(domain.isActive())
                .defaultSystemPrompt(domain.getDefaultSystemPrompt())
                .defaultModel(domain.getDefaultModel())
                .defaultProvider(domain.getDefaultProvider())
                .enableRag(domain.isEnableRag())
                .enableToolCalling(domain.isEnableToolCalling())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt());
        if (domain.getId() != null) {
            builder.id(UUID.fromString(domain.getId()));
        }
        return builder.build();
    }

    private MessagingChannel toDomain(MessagingChannelEntity entity) {
        return MessagingChannel.builder()
                .id(entity.getId().toString())
                .name(entity.getName())
                .platform(entity.getPlatform())
                .botToken(entity.getBotToken())
                .webhookSecret(entity.getWebhookSecret())
                .active(entity.isActive())
                .defaultSystemPrompt(entity.getDefaultSystemPrompt())
                .defaultModel(entity.getDefaultModel())
                .defaultProvider(entity.getDefaultProvider())
                .enableRag(entity.isEnableRag())
                .enableToolCalling(entity.isEnableToolCalling())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
