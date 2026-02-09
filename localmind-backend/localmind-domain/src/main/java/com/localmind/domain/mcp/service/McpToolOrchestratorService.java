package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.*;
import com.localmind.domain.mcp.port.in.McpToolDiscoveryUseCase;
import com.localmind.domain.mcp.port.in.McpToolExecutionUseCase;
import com.localmind.domain.mcp.port.out.McpClientPort;
import com.localmind.domain.mcp.port.out.McpServerRegistrationRepository;
import java.util.ArrayList;
import java.util.List;

public class McpToolOrchestratorService implements McpToolDiscoveryUseCase, McpToolExecutionUseCase {

    private final McpServerRegistrationRepository serverRepository;
    private final McpClientPort clientPort;

    public McpToolOrchestratorService(McpServerRegistrationRepository serverRepository, McpClientPort clientPort) {
        this.serverRepository = serverRepository;
        this.clientPort = clientPort;
    }

    @Override
    public List<McpExternalTool> listExternalTools(String serverId) {
        return clientPort.discoverTools(serverId);
    }

    @Override
    public List<McpExternalTool> listAllExternalTools() {
        List<McpExternalTool> allTools = new ArrayList<>();
        serverRepository.findAll().stream()
                .filter(server -> server.getStatus() == McpServerStatus.CONNECTED)
                .forEach(server -> allTools.addAll(clientPort.discoverTools(server.getId())));
        return allTools;
    }

    @Override
    public McpToolExecutionResult executeTool(McpToolExecutionRequest request) {
        return clientPort.executeTool(request.getServerId(), request);
    }
}
