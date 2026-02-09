package com.localmind.infrastructure.mcp.adapter;

import com.localmind.domain.mcp.model.*;
import com.localmind.domain.mcp.port.out.McpClientPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name = "localmind.mcp.client.enabled", havingValue = "true")
public class SpringAiMcpClientAdapter implements McpClientPort {

    private static final Logger log = LoggerFactory.getLogger(SpringAiMcpClientAdapter.class);
    private final Map<String, McpClientConnection> connections = new ConcurrentHashMap<>();

    @Override
    public boolean connect(McpServerRegistration server) {
        try {
            log.info("Connecting to MCP server: {} ({})", server.getName(), server.getType());
            McpClientConnection connection = new McpClientConnection(server);
            connections.put(server.getId(), connection);
            log.info("Connected to MCP server: {}", server.getName());
            return true;
        } catch (Exception e) {
            log.error("Failed to connect to MCP server: {}", server.getName(), e);
            return false;
        }
    }

    @Override
    public void disconnect(String serverId) {
        McpClientConnection connection = connections.remove(serverId);
        if (connection != null) {
            log.info("Disconnected from MCP server: {}", serverId);
            connection.close();
        }
    }

    @Override
    public boolean testConnection(String serverId) {
        McpClientConnection connection = connections.get(serverId);
        return connection != null && connection.isAlive();
    }

    @Override
    public List<McpExternalTool> discoverTools(String serverId) {
        McpClientConnection connection = connections.get(serverId);
        if (connection == null) {
            return List.of();
        }
        return connection.getTools();
    }

    @Override
    public McpToolExecutionResult executeTool(String serverId, McpToolExecutionRequest request) {
        McpClientConnection connection = connections.get(serverId);
        if (connection == null) {
            return McpToolExecutionResult.builder()
                    .toolName(request.getToolName())
                    .success(false)
                    .errorMessage("MCP server not connected: " + serverId)
                    .build();
        }

        long start = System.currentTimeMillis();
        try {
            Object result = connection.executeTool(request.getToolName(), request.getArguments());
            return McpToolExecutionResult.builder()
                    .toolName(request.getToolName())
                    .result(result)
                    .success(true)
                    .executionTimeMs(System.currentTimeMillis() - start)
                    .build();
        } catch (Exception e) {
            return McpToolExecutionResult.builder()
                    .toolName(request.getToolName())
                    .success(false)
                    .errorMessage(e.getMessage())
                    .executionTimeMs(System.currentTimeMillis() - start)
                    .build();
        }
    }

    /**
     * Internal holder for MCP client connections.
     * Wraps the actual MCP SDK client and provides lifecycle management.
     */
    private static class McpClientConnection {
        private final McpServerRegistration server;
        private volatile boolean alive = true;

        McpClientConnection(McpServerRegistration server) {
            this.server = server;
        }

        boolean isAlive() {
            return alive;
        }

        List<McpExternalTool> getTools() {
            // TODO: Use MCP SDK to discover tools from the connected server
            return List.of();
        }

        Object executeTool(String toolName, Map<String, Object> arguments) {
            // TODO: Use MCP SDK to execute tool on the connected server
            throw new UnsupportedOperationException("MCP tool execution not yet implemented for: " + toolName);
        }

        void close() {
            alive = false;
        }
    }
}
