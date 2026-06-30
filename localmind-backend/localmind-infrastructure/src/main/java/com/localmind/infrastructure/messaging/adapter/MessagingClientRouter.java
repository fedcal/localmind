package com.localmind.infrastructure.messaging.adapter;

import com.localmind.domain.messaging.model.MessagingPlatform;
import com.localmind.domain.messaging.model.OutboundMessage;
import com.localmind.domain.messaging.port.out.MessagingClientPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MessagingClientRouter implements MessagingClientPort {

    private final Map<MessagingPlatform, MessagingClientPort> clients;

    public MessagingClientRouter(List<MessagingClientPort> clientList) {
        this.clients = clientList.stream()
                .filter(c -> !(c instanceof MessagingClientRouter))
                .collect(Collectors.toMap(MessagingClientPort::getPlatform, c -> c));
    }

    @Override
    public void sendMessage(OutboundMessage message) {
        MessagingClientPort client = clients.get(message.getPlatform());
        if (client != null) {
            client.sendMessage(message);
        } else {
            throw new IllegalStateException("No messaging client for platform: " + message.getPlatform());
        }
    }

    @Override
    public boolean isAvailable(String botToken) {
        return !clients.isEmpty();
    }

    @Override
    public MessagingPlatform getPlatform() {
        return null;
    }
}
