package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.McpServerRegistration;
import com.localmind.domain.mcp.model.McpServerStatus;
import com.localmind.domain.mcp.port.in.McpServerManagementUseCase;
import com.localmind.domain.mcp.port.out.McpClientPort;
import com.localmind.domain.mcp.port.out.McpServerRegistrationRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class McpServerManagementService implements McpServerManagementUseCase {

    private final McpServerRegistrationRepository repository;
    private final McpClientPort clientPort;

    public McpServerManagementService(McpServerRegistrationRepository repository, McpClientPort clientPort) {
        this.repository = repository;
        this.clientPort = clientPort;
    }

    @Override
    public McpServerRegistration register(McpServerRegistration server) {
        if (server.getId() == null) {
            server.setId(UUID.randomUUID().toString());
        }
        server.setCreatedAt(LocalDateTime.now());
        server.setStatus(McpServerStatus.DISCONNECTED);

        McpServerRegistration saved = repository.save(server);

        boolean connected = clientPort.connect(saved);
        if (connected) {
            saved.setStatus(McpServerStatus.CONNECTED);
            saved.setLastConnectedAt(LocalDateTime.now());
            return repository.save(saved);
        }

        return saved;
    }

    @Override
    public void remove(String serverId) {
        clientPort.disconnect(serverId);
        repository.delete(serverId);
    }

    @Override
    public McpServerRegistration get(String serverId) {
        return repository.findById(serverId)
                .orElseThrow(() -> new IllegalArgumentException("MCP Server not found: " + serverId));
    }

    @Override
    public List<McpServerRegistration> listAll() {
        return repository.findAll();
    }

    @Override
    public McpServerRegistration testConnection(String serverId) {
        McpServerRegistration server = get(serverId);
        boolean connected = clientPort.testConnection(serverId);
        server.setStatus(connected ? McpServerStatus.CONNECTED : McpServerStatus.ERROR);
        if (connected) {
            server.setLastConnectedAt(LocalDateTime.now());
        }
        return repository.save(server);
    }

    @Override
    public void reconnect(String serverId) {
        McpServerRegistration server = get(serverId);
        clientPort.disconnect(serverId);
        server.setStatus(McpServerStatus.CONNECTING);
        repository.save(server);

        boolean connected = clientPort.connect(server);
        server.setStatus(connected ? McpServerStatus.CONNECTED : McpServerStatus.ERROR);
        if (connected) {
            server.setLastConnectedAt(LocalDateTime.now());
        }
        repository.save(server);
    }
}
