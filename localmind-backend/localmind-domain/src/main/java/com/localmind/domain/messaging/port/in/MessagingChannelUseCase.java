package com.localmind.domain.messaging.port.in;

import com.localmind.domain.messaging.model.MessagingChannel;
import com.localmind.domain.messaging.model.MessagingPlatform;

import java.util.List;

public interface MessagingChannelUseCase {
    MessagingChannel save(MessagingChannel channel);
    List<MessagingChannel> listAll();
    MessagingChannel getById(String id);
    void deleteById(String id);
    TestResult testConnection(String id);

    record TestResult(String status, String message) {}
}
