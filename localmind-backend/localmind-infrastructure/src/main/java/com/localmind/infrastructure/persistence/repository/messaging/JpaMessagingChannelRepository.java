package com.localmind.infrastructure.persistence.repository.messaging;

import com.localmind.domain.messaging.model.MessagingPlatform;
import com.localmind.infrastructure.persistence.entity.messaging.MessagingChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaMessagingChannelRepository extends JpaRepository<MessagingChannelEntity, UUID> {
    List<MessagingChannelEntity> findByPlatform(MessagingPlatform platform);
    Optional<MessagingChannelEntity> findByPlatformAndActiveTrue(MessagingPlatform platform);
}
