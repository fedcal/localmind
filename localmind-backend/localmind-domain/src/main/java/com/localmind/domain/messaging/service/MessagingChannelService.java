package com.localmind.domain.messaging.service;

import com.localmind.domain.common.exception.ResourceNotFoundException;
import com.localmind.domain.messaging.model.MessagingChannel;
import com.localmind.domain.messaging.port.in.MessagingChannelUseCase;
import com.localmind.domain.messaging.port.out.MessagingChannelRepository;
import com.localmind.domain.messaging.port.out.MessagingClientPort;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MessagingChannelService implements MessagingChannelUseCase {

    private final MessagingChannelRepository repository;
    private final Map<com.localmind.domain.messaging.model.MessagingPlatform, MessagingClientPort> clients;

    public MessagingChannelService(MessagingChannelRepository repository,
                                    List<MessagingClientPort> clientList) {
        this.repository = repository;
        this.clients = clientList != null
                ? clientList.stream().collect(Collectors.toMap(MessagingClientPort::getPlatform, c -> c))
                : Map.of();
    }

    @Override
    public MessagingChannel save(MessagingChannel channel) {
        return repository.save(channel);
    }

    @Override
    public List<MessagingChannel> listAll() {
        return repository.findAll();
    }

    @Override
    public MessagingChannel getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Messaging channel not found: " + id));
    }

    @Override
    public void deleteById(String id) {
        repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Messaging channel not found: " + id));
        repository.deleteById(id);
    }

    @Override
    public TestResult testConnection(String id) {
        MessagingChannel channel = getById(id);
        try {
            MessagingClientPort client = clients.get(channel.getPlatform());
            if (client == null) {
                return new TestResult("ERROR", "Platform " + channel.getPlatform().getDisplayName() + " non supportata o non abilitata.");
            }
            boolean available = client.isAvailable(channel.getBotToken());
            if (available) {
                return new TestResult("OK", "Bot connesso a " + channel.getPlatform().getDisplayName() + ".");
            } else {
                return new TestResult("ERROR", "Bot non raggiungibile. Verificare il token.");
            }
        } catch (Exception e) {
            return new TestResult("ERROR", "Connessione fallita: " + e.getMessage());
        }
    }
}
