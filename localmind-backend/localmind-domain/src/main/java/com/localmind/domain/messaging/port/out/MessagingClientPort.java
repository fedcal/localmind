package com.localmind.domain.messaging.port.out;

import com.localmind.domain.messaging.model.MessagingPlatform;
import com.localmind.domain.messaging.model.OutboundMessage;

public interface MessagingClientPort {
    void sendMessage(OutboundMessage message);
    boolean isAvailable(String botToken);
    MessagingPlatform getPlatform();
}
