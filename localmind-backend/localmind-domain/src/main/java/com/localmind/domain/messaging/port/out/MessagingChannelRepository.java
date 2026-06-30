package com.localmind.domain.messaging.port.out;

import com.localmind.domain.messaging.model.MessagingChannel;
import com.localmind.domain.messaging.model.MessagingPlatform;

import java.util.List;
import java.util.Optional;

public interface MessagingChannelRepository {
    MessagingChannel save(MessagingChannel channel);
    Optional<MessagingChannel> findById(String id);
    List<MessagingChannel> findAll();
    List<MessagingChannel> findByPlatform(MessagingPlatform platform);
    Optional<MessagingChannel> findActivePlatform(MessagingPlatform platform);
    void deleteById(String id);
}
