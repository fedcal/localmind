package com.localmind.infrastructure.mcp.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.domain.mcp.model.*;
import com.localmind.domain.mcp.port.out.McpClientPort;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "localmind.mcp.client.enabled", havingValue = "true")
public class SpringAiMcpClientAdapter implements McpClientPort {

    private static final Logger log = LoggerFactory.getLogger(SpringAiMcpClientAdapter.class);
    private final Map<String, McpClientConnection> connections = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean connect(McpServerRegistration server) {
        try {
            log.info("Connecting to MCP server: {} ({})", server.getName(), server.getType());
            McpClientConnection connection = new McpClientConnection(server, objectMapper);
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
     * Wraps the MCP SDK McpSyncClient and provides lifecycle management.
     * Supports both SSE (HTTP Server-Sent Events) and STDIO transports.
     */
    private static class McpClientConnection {
        private final McpServerRegistration server;
        private final McpSyncClient client;
        private final ObjectMapper objectMapper;
        private volatile boolean alive;

        McpClientConnection(McpServerRegistration server, ObjectMapper objectMapper) {
            this.server = server;
            this.objectMapper = objectMapper;

            this.client = createClient(server);
            this.client.initialize();
            this.alive = true;
            log.info("MCP client initialized for server: {} (type: {})", server.getName(), server.getType());
        }

        private static McpSyncClient createClient(McpServerRegistration server) {
            McpServerConfig config = server.getConfig();
            int timeoutSeconds = config.getTimeoutSeconds() > 0 ? config.getTimeoutSeconds() : 30;
            Duration timeout = Duration.ofSeconds(timeoutSeconds);

            if (server.getType() == McpServerType.SSE) {
                return createSseClient(server, config, timeout);
            } else if (server.getType() == McpServerType.STDIO) {
                return createStdioClient(server, config, timeout);
            } else {
                throw new IllegalArgumentException(
                        "Unsupported MCP server type: " + server.getType());
            }
        }

        private static McpSyncClient createSseClient(McpServerRegistration server,
                                                       McpServerConfig config,
                                                       Duration timeout) {
            String url = config.getUrl();
            if (url == null || url.isBlank()) {
                throw new IllegalArgumentException(
                        "SSE server URL is required for server: " + server.getName());
            }
            log.info("Creating SSE transport for server: {} -> {}", server.getName(), url);

            HttpClientSseClientTransport transport = HttpClientSseClientTransport.builder(url).build();
            return McpClient.sync(transport)
                    .requestTimeout(timeout)
                    .build();
        }

        private static McpSyncClient createStdioClient(McpServerRegistration server,
                                                         McpServerConfig config,
                                                         Duration timeout) {
            String command = config.getCommand();
            if (command == null || command.isBlank()) {
                throw new IllegalArgumentException(
                        "STDIO command is required for server: " + server.getName());
            }

            ServerParameters.Builder paramsBuilder = ServerParameters.builder(command);
            List<String> args = config.getArgs();
            if (args != null && !args.isEmpty()) {
                paramsBuilder.args(args.toArray(new String[0]));
            }

            log.info("Creating STDIO transport for server: {} -> {} {}",
                    server.getName(), command,
                    args != null ? String.join(" ", args) : "");

            StdioClientTransport transport = new StdioClientTransport(paramsBuilder.build());
            return McpClient.sync(transport)
                    .requestTimeout(timeout)
                    .build();
        }

        boolean isAlive() {
            return alive;
        }

        List<McpExternalTool> getTools() {
            if (!alive) {
                return List.of();
            }
            try {
                McpSchema.ListToolsResult toolsResult = client.listTools();
                if (toolsResult == null || toolsResult.tools() == null) {
                    return List.of();
                }

                return toolsResult.tools().stream()
                        .map(this::mapToExternalTool)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                log.error("Failed to list tools from MCP server: {}", server.getName(), e);
                return List.of();
            }
        }

        private McpExternalTool mapToExternalTool(McpSchema.Tool tool) {
            String inputSchemaJson = null;
            if (tool.inputSchema() != null) {
                try {
                    inputSchemaJson = objectMapper.writeValueAsString(tool.inputSchema());
                } catch (JsonProcessingException e) {
                    log.warn("Failed to serialize inputSchema for tool: {}", tool.name(), e);
                    inputSchemaJson = "{}";
                }
            }

            return McpExternalTool.builder()
                    .name(tool.name())
                    .description(tool.description())
                    .inputSchema(inputSchemaJson)
                    .serverId(server.getId())
                    .build();
        }

        Object executeTool(String toolName, Map<String, Object> arguments) {
            if (!alive) {
                throw new IllegalStateException("MCP client is not connected for server: " + server.getName());
            }

            log.debug("Executing tool '{}' on MCP server: {} with arguments: {}",
                    toolName, server.getName(), arguments);

            McpSchema.CallToolResult result = client.callTool(
                    new McpSchema.CallToolRequest(toolName, arguments != null ? arguments : Map.of()));

            if (result == null) {
                throw new RuntimeException("Null result from MCP tool execution: " + toolName);
            }

            if (Boolean.TRUE.equals(result.isError())) {
                String errorText = extractTextContent(result);
                throw new RuntimeException("MCP tool '" + toolName + "' returned error: " + errorText);
            }

            return extractContent(result);
        }

        /**
         * Extracts text content from a CallToolResult for error messages.
         */
        private String extractTextContent(McpSchema.CallToolResult result) {
            if (result.content() == null || result.content().isEmpty()) {
                return "Unknown error";
            }
            List<String> texts = new ArrayList<>();
            for (McpSchema.Content content : result.content()) {
                if (content instanceof McpSchema.TextContent textContent) {
                    texts.add(textContent.text());
                }
            }
            return texts.isEmpty() ? "Unknown error" : String.join("\n", texts);
        }

        /**
         * Extracts content from a CallToolResult.
         * Returns the text if there is a single TextContent, otherwise returns the full content list.
         */
        private Object extractContent(McpSchema.CallToolResult result) {
            if (result.content() == null || result.content().isEmpty()) {
                return null;
            }

            // Single text content: return the text string directly
            if (result.content().size() == 1 && result.content().get(0) instanceof McpSchema.TextContent textContent) {
                return textContent.text();
            }

            // Multiple content items: return a list of maps with type and content
            List<Map<String, Object>> contentList = new ArrayList<>();
            for (McpSchema.Content content : result.content()) {
                if (content instanceof McpSchema.TextContent textContent) {
                    contentList.add(Map.of("type", "text", "text", textContent.text()));
                } else if (content instanceof McpSchema.ImageContent imageContent) {
                    contentList.add(Map.of(
                            "type", "image",
                            "data", imageContent.data(),
                            "mimeType", imageContent.mimeType()));
                } else if (content instanceof McpSchema.EmbeddedResource embeddedResource) {
                    contentList.add(Map.of(
                            "type", "resource",
                            "resource", embeddedResource.resource()));
                } else {
                    contentList.add(Map.of("type", content.type()));
                }
            }
            return contentList;
        }

        void close() {
            alive = false;
            try {
                client.closeGracefully();
                log.info("MCP client closed for server: {}", server.getName());
            } catch (Exception e) {
                log.warn("Error closing MCP client for server: {}", server.getName(), e);
            }
        }
    }
}
