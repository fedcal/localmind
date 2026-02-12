package com.localmind.infrastructure.mcp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.domain.mcp.model.*;
import com.localmind.domain.mcp.port.in.McpToolDiscoveryUseCase;
import com.localmind.domain.mcp.port.in.McpToolExecutionUseCase;
import com.localmind.domain.mcp.port.in.ToolCallingPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ToolCallingService implements ToolCallingPort {

    private final McpToolDiscoveryUseCase toolDiscoveryUseCase;
    private final McpToolExecutionUseCase toolExecutionUseCase;
    private final ObjectMapper objectMapper;

    private static final Pattern TOOL_CALL_PATTERN = Pattern.compile(
            "\\{\\s*\"tool_call\"\\s*:\\s*\\{.*?\"name\"\\s*:\\s*\"([^\"]+)\".*?\\}\\s*\\}",
            Pattern.DOTALL);

    public ToolCallingService(McpToolDiscoveryUseCase toolDiscoveryUseCase,
                              McpToolExecutionUseCase toolExecutionUseCase,
                              ObjectMapper objectMapper) {
        this.toolDiscoveryUseCase = toolDiscoveryUseCase;
        this.toolExecutionUseCase = toolExecutionUseCase;
        this.objectMapper = objectMapper;
    }

    /**
     * Genera il system prompt che descrive i tool disponibili al LLM.
     */
    public String buildToolsSystemPrompt() {
        List<McpExternalTool> tools = toolDiscoveryUseCase.listAllExternalTools();
        if (tools.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("You have access to the following tools. To use a tool, respond with a JSON object in this exact format:\n");
        sb.append("{\"tool_call\": {\"name\": \"tool_name\", \"serverId\": \"server_id\", \"arguments\": {\"param\": \"value\"}}}\n\n");
        sb.append("Available tools:\n\n");

        for (McpExternalTool tool : tools) {
            sb.append("- **").append(tool.getName()).append("**");
            if (tool.getServerId() != null) {
                sb.append(" (serverId: ").append(tool.getServerId()).append(")");
            }
            sb.append("\n");
            if (tool.getDescription() != null) {
                sb.append("  Description: ").append(tool.getDescription()).append("\n");
            }
            if (tool.getInputSchema() != null) {
                sb.append("  Input schema: ").append(tool.getInputSchema()).append("\n");
            }
            sb.append("\n");
        }

        sb.append("Only use a tool if the user's request requires it. If you can answer directly, do so without calling a tool.\n");
        sb.append("After receiving a tool result, provide a helpful response based on the result.\n");
        return sb.toString();
    }

    /**
     * Analizza la risposta del LLM per cercare un tool_call JSON.
     */
    public Optional<ToolCallRequest> parseToolCallFromResponse(String response) {
        if (response == null || response.isBlank()) {
            return Optional.empty();
        }

        Matcher matcher = TOOL_CALL_PATTERN.matcher(response);
        if (!matcher.find()) {
            return Optional.empty();
        }

        try {
            String jsonBlock = matcher.group(0);
            Map<String, Object> parsed = objectMapper.readValue(jsonBlock, new TypeReference<>() {});

            @SuppressWarnings("unchecked")
            Map<String, Object> toolCall = (Map<String, Object>) parsed.get("tool_call");
            if (toolCall == null) {
                return Optional.empty();
            }

            String name = (String) toolCall.get("name");
            String serverId = (String) toolCall.get("serverId");
            @SuppressWarnings("unchecked")
            Map<String, Object> arguments = (Map<String, Object>) toolCall.getOrDefault("arguments", Map.of());

            if (name == null || name.isBlank()) {
                return Optional.empty();
            }

            return Optional.of(ToolCallRequest.builder()
                    .toolName(name)
                    .serverId(serverId)
                    .arguments(arguments)
                    .build());
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }

    /**
     * Esegue un tool call e restituisce il risultato.
     */
    public ToolCallResponse executeToolCall(ToolCallRequest request) {
        try {
            McpToolExecutionResult result = toolExecutionUseCase.executeTool(
                    McpToolExecutionRequest.builder()
                            .toolName(request.getToolName())
                            .serverId(request.getServerId())
                            .arguments(request.getArguments())
                            .build());

            String resultStr;
            try {
                resultStr = objectMapper.writeValueAsString(result.getResult());
            } catch (JsonProcessingException e) {
                resultStr = String.valueOf(result.getResult());
            }

            return ToolCallResponse.builder()
                    .toolName(request.getToolName())
                    .result(resultStr)
                    .success(result.isSuccess())
                    .errorMessage(result.getErrorMessage())
                    .executionTimeMs(result.getExecutionTimeMs())
                    .build();
        } catch (Exception e) {
            return ToolCallResponse.builder()
                    .toolName(request.getToolName())
                    .result(null)
                    .success(false)
                    .errorMessage("Tool execution failed: " + e.getMessage())
                    .executionTimeMs(0)
                    .build();
        }
    }

    /**
     * Verifica se ci sono tool disponibili.
     */
    public boolean hasAvailableTools() {
        return !toolDiscoveryUseCase.listAllExternalTools().isEmpty();
    }
}
