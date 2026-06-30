package com.localmind.infrastructure.persistence.adapter.messaging;

import com.localmind.domain.messaging.model.ChannelContext;
import com.localmind.domain.messaging.model.MessagingPlatform;
import com.localmind.domain.messaging.port.out.ChannelContextRepository;
import com.localmind.infrastructure.persistence.entity.messaging.ChannelContextEntity;
import com.localmind.infrastructure.persistence.repository.messaging.JpaChannelContextRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ChannelContextRepositoryAdapter implements ChannelContextRepository {

    private final JpaChannelContextRepository jpaRepository;

    public ChannelContextRepositoryAdapter(JpaChannelContextRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ChannelContext save(ChannelContext context) {
        ChannelContextEntity entity = toEntity(context);
        ChannelContextEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<ChannelContext> findByPlatformAndUserAndChannel(
            MessagingPlatform platform, String platformUserId, String platformChannelId) {
        return jpaRepository.findByPlatformAndPlatformUserIdAndPlatformChannelId(
                platform, platformUserId, platformChannelId).map(this::toDomain);
    }

    private ChannelContextEntity toEntity(ChannelContext domain) {
        ChannelContextEntity.ChannelContextEntityBuilder builder = ChannelContextEntity.builder()
                .channelId(domain.getChannelId())
                .platform(domain.getPlatform())
                .platformUserId(domain.getPlatformUserId())
                .platformChannelId(domain.getPlatformChannelId())
                .conversationId(domain.getConversationId())
                .createdAt(domain.getCreatedAt())
                .lastActivity(domain.getLastActivity());
        if (domain.getId() != null) {
            builder.id(UUID.fromString(domain.getId()));
        }
        return builder.build();
    }

    private ChannelContext toDomain(ChannelContextEntity entity) {
        return ChannelContext.builder()
                .id(entity.getId().toString())
                .channelId(entity.getChannelId())
                .platform(entity.getPlatform())
                .platformUserId(entity.getPlatformUserId())
                .platformChannelId(entity.getPlatformChannelId())
                .conversationId(entity.getConversationId())
                .createdAt(entity.getCreatedAt())
                .lastActivity(entity.getLastActivity())
                .build();
    }
}
