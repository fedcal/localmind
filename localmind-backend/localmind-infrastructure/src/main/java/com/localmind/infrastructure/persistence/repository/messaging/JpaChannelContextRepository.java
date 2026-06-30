package com.localmind.infrastructure.persistence.repository.messaging;

import com.localmind.domain.messaging.model.MessagingPlatform;
import com.localmind.infrastructure.persistence.entity.messaging.ChannelContextEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaChannelContextRepository extends JpaRepository<ChannelContextEntity, UUID> {
    Optional<ChannelContextEntity> findByPlatformAndPlatformUserIdAndPlatformChannelId(
            MessagingPlatform platform, String platformUserId, String platformChannelId);
}
