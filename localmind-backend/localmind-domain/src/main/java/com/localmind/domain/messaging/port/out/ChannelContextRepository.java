package com.localmind.domain.messaging.port.out;

import com.localmind.domain.messaging.model.ChannelContext;
import com.localmind.domain.messaging.model.MessagingPlatform;

import java.util.Optional;

public interface ChannelContextRepository {
    ChannelContext save(ChannelContext context);
    Optional<ChannelContext> findByPlatformAndUserAndChannel(
            MessagingPlatform platform, String platformUserId, String platformChannelId);
}
