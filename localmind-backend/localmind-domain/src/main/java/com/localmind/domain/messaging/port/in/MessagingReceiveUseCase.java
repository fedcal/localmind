package com.localmind.domain.messaging.port.in;

import com.localmind.domain.messaging.model.InboundMessage;

public interface MessagingReceiveUseCase {
    void receiveMessage(InboundMessage message);
}
