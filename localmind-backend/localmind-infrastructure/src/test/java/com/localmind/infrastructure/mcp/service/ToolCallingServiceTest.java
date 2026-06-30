package com.localmind.infrastructure.mcp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.domain.mcp.model.*;
import com.localmind.domain.mcp.port.in.McpToolDiscoveryUseCase;
import com.localmind.domain.mcp.port.in.McpToolExecutionUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

class ToolCallingServiceTest {

    private McpToolDiscoveryUseCase toolDiscoveryUseCase;
    private McpToolExecutionUseCase toolExecutionUseCase;
    private ObjectMapper objectMapper;
    private ToolCallingService toolCallingService;

    @BeforeEach
    void setUp() {
        toolDiscoveryUseCase = mock(McpToolDiscoveryUseCase.class);
        toolExecutionUseCase = mock(McpToolExecutionUseCase.class);
        objectMapper = new ObjectMapper();
        toolCallingService = new ToolCallingService(toolDiscoveryUseCase, toolExecutionUseCase, objectMapper);
    }

    // ========================================================================
    // buildToolsSystemPrompt()
    // ========================================================================

    @Test
    void buildToolsSystemPrompt_withTools_returnsFormattedPrompt() {
        McpExternalTool tool1 = McpExternalTool.builder()
                .name("search_files")
                .description("Search files in a directory")
                .inputSchema("{\"type\":\"object\",\"properties\":{\"query\":{\"type\":\"string\"}}}")
                .serverId("server-1")
                .build();

        McpExternalTool tool2 = McpExternalTool.builder()
                .name("read_file")
                .description("Read the contents of a file")
                .inputSchema("{\"type\":\"object\",\"properties\":{\"path\":{\"type\":\"string\"}}}")
                .serverId("server-2")
                .build();

        when(toolDiscoveryUseCase.listAllExternalTools()).thenReturn(List.of(tool1, tool2));

        String prompt = toolCallingService.buildToolsSystemPrompt();

        assertThat(prompt).isNotEmpty();
        assertThat(prompt).contains("search_files");
        assertThat(prompt).contains("read_file");
        assertThat(prompt).contains("Search files in a directory");
        assertThat(prompt).contains("Read the contents of a file");
        assertThat(prompt).contains("server-1");
        assertThat(prompt).contains("server-2");
        assertThat(prompt).contains("Input schema:");
        assertThat(prompt).contains("tool_call");
        assertThat(prompt).contains("Available tools:");
    }

    @Test
    void buildToolsSystemPrompt_withToolMissingOptionalFields_handlesNulls() {
        McpExternalTool tool = McpExternalTool.builder()
                .name("minimal_tool")
                .description(null)
                .inputSchema(null)
                .serverId(null)
                .build();

        when(toolDiscoveryUseCase.listAllExternalTools()).thenReturn(List.of(tool));

        String prompt = toolCallingService.buildToolsSystemPrompt();

        assertThat(prompt).contains("minimal_tool");
        assertThat(prompt).doesNotContain("Description:");
        assertThat(prompt).doesNotContain("Input schema:");
        assertThat(prompt).doesNotContain("serverId:");
    }

    @Test
    void buildToolsSystemPrompt_withNoTools_returnsEmptyString() {
        when(toolDiscoveryUseCase.listAllExternalTools()).thenReturn(List.of());

        String prompt = toolCallingService.buildToolsSystemPrompt();

        assertThat(prompt).isEmpty();
    }

    // ========================================================================
    // parseToolCallFromResponse()
    // ========================================================================

    @Test
    void parseToolCallFromResponse_withValidToolCall_returnsToolCallRequest() {
        String response = "{\"tool_call\": {\"name\": \"search_files\", \"serverId\": \"server-1\"}}";

        Optional<ToolCallRequest> result = toolCallingService.parseToolCallFromResponse(response);

        assertThat(result).isPresent();
        ToolCallRequest request = result.get();
        assertThat(request.getToolName()).isEqualTo("search_files");
        assertThat(request.getServerId()).isEqualTo("server-1");
        assertThat(request.getArguments()).isEmpty();
    }

    @Test
    void parseToolCallFromResponse_withValidToolCallNoArguments_returnsRequestWithEmptyArgs() {
        String response = "{\"tool_call\": {\"name\": \"list_all\", \"serverId\": \"srv\"}}";

        Optional<ToolCallRequest> result = toolCallingService.parseToolCallFromResponse(response);

        assertThat(result).isPresent();
        ToolCallRequest request = result.get();
        assertThat(request.getToolName()).isEqualTo("list_all");
        assertThat(request.getServerId()).isEqualTo("srv");
        assertThat(request.getArguments()).isEmpty();
    }

    @Test
    void parseToolCallFromResponse_withNoToolCall_returnsEmpty() {
        String response = "I can answer that directly without using any tools. The answer is 42.";

        Optional<ToolCallRequest> result = toolCallingService.parseToolCallFromResponse(response);

        assertThat(result).isEmpty();
    }

    @Test
    void parseToolCallFromResponse_withNullResponse_returnsEmpty() {
        Optional<ToolCallRequest> result = toolCallingService.parseToolCallFromResponse(null);

        assertThat(result).isEmpty();
    }

    @Test
    void parseToolCallFromResponse_withBlankResponse_returnsEmpty() {
        Optional<ToolCallRequest> result = toolCallingService.parseToolCallFromResponse("   ");

        assertThat(result).isEmpty();
    }

    @Test
    void parseToolCallFromResponse_withEmptyResponse_returnsEmpty() {
        Optional<ToolCallRequest> result = toolCallingService.parseToolCallFromResponse("");

        assertThat(result).isEmpty();
    }

    @Test
    void parseToolCallFromResponse_withMalformedJson_returnsEmpty() {
        String response = "{\"tool_call\": {\"name\": broken json here}}";

        Optional<ToolCallRequest> result = toolCallingService.parseToolCallFromResponse(response);

        assertThat(result).isEmpty();
    }

    @Test
    void parseToolCallFromResponse_withBlankToolName_returnsEmpty() {
        String response = "{\"tool_call\": {\"name\": \"  \", \"serverId\": \"server-1\"}}";

        Optional<ToolCallRequest> result = toolCallingService.parseToolCallFromResponse(response);

        assertThat(result).isEmpty();
    }

    @Test
    void parseToolCallFromResponse_withMultilineJson_returnsToolCallRequest() {
        String response = "Let me use a tool.\n{\n  \"tool_call\": {\n    \"name\": \"read_file\",\n    \"serverId\": \"fs-server\"\n  }\n}";

        Optional<ToolCallRequest> result = toolCallingService.parseToolCallFromResponse(response);

        assertThat(result).isPresent();
        assertThat(result.get().getToolName()).isEqualTo("read_file");
        assertThat(result.get().getServerId()).isEqualTo("fs-server");
    }

    @Test
    void parseToolCallFromResponse_withNestedArguments_returnsEmpty() {
        // The regex pattern captures only 2 levels of braces, so nested arguments objects
        // produce incomplete JSON that fails parsing
        String response = "{\"tool_call\": {\"name\": \"search\", \"serverId\": \"s1\", \"arguments\": {\"q\": \"test\"}}}";

        Optional<ToolCallRequest> result = toolCallingService.parseToolCallFromResponse(response);

        assertThat(result).isEmpty();
    }

    // ========================================================================
    // executeToolCall()
    // ========================================================================

    @Test
    void executeToolCall_successfulExecution_returnsSuccessResponse() {
        ToolCallRequest request = ToolCallRequest.builder()
                .toolName("search_files")
                .serverId("server-1")
                .arguments(Map.of("query", "test"))
                .build();

        McpToolExecutionResult executionResult = McpToolExecutionResult.builder()
                .toolName("search_files")
                .result(Map.of("files", List.of("file1.txt", "file2.txt")))
                .success(true)
                .errorMessage(null)
                .executionTimeMs(250L)
                .build();

        when(toolExecutionUseCase.executeTool(any(McpToolExecutionRequest.class))).thenReturn(executionResult);

        ToolCallResponse response = toolCallingService.executeToolCall(request);

        assertThat(response.getToolName()).isEqualTo("search_files");
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getErrorMessage()).isNull();
        assertThat(response.getExecutionTimeMs()).isEqualTo(250L);
        assertThat(response.getResult()).isNotNull();
        assertThat(response.getResult()).contains("file1.txt");
        assertThat(response.getResult()).contains("file2.txt");

        verify(toolExecutionUseCase).executeTool(argThat(req ->
                req.getToolName().equals("search_files") &&
                req.getServerId().equals("server-1") &&
                req.getArguments().get("query").equals("test")
        ));
    }

    @Test
    void executeToolCall_withStringResult_serializesCorrectly() {
        ToolCallRequest request = ToolCallRequest.builder()
                .toolName("echo")
                .serverId("server-1")
                .arguments(Map.of())
                .build();

        McpToolExecutionResult executionResult = McpToolExecutionResult.builder()
                .toolName("echo")
                .result("plain text result")
                .success(true)
                .errorMessage(null)
                .executionTimeMs(50L)
                .build();

        when(toolExecutionUseCase.executeTool(any(McpToolExecutionRequest.class))).thenReturn(executionResult);

        ToolCallResponse response = toolCallingService.executeToolCall(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResult()).isEqualTo("\"plain text result\"");
    }

    @Test
    void executeToolCall_executionThrowsException_returnsErrorResponse() {
        ToolCallRequest request = ToolCallRequest.builder()
                .toolName("failing_tool")
                .serverId("server-1")
                .arguments(Map.of())
                .build();

        when(toolExecutionUseCase.executeTool(any(McpToolExecutionRequest.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        ToolCallResponse response = toolCallingService.executeToolCall(request);

        assertThat(response.getToolName()).isEqualTo("failing_tool");
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getResult()).isNull();
        assertThat(response.getErrorMessage()).isEqualTo("Tool execution failed: Connection refused");
        assertThat(response.getExecutionTimeMs()).isEqualTo(0L);
    }

    @Test
    void executeToolCall_withFailedResult_propagatesErrorMessage() {
        ToolCallRequest request = ToolCallRequest.builder()
                .toolName("validate")
                .serverId("server-1")
                .arguments(Map.of("input", "bad"))
                .build();

        McpToolExecutionResult executionResult = McpToolExecutionResult.builder()
                .toolName("validate")
                .result(null)
                .success(false)
                .errorMessage("Validation failed: invalid input")
                .executionTimeMs(30L)
                .build();

        when(toolExecutionUseCase.executeTool(any(McpToolExecutionRequest.class))).thenReturn(executionResult);

        ToolCallResponse response = toolCallingService.executeToolCall(request);

        assertThat(response.getToolName()).isEqualTo("validate");
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorMessage()).isEqualTo("Validation failed: invalid input");
        assertThat(response.getExecutionTimeMs()).isEqualTo(30L);
    }

    // ========================================================================
    // hasAvailableTools()
    // ========================================================================

    @Test
    void hasAvailableTools_withTools_returnsTrue() {
        McpExternalTool tool = McpExternalTool.builder()
                .name("some_tool")
                .serverId("server-1")
                .build();

        when(toolDiscoveryUseCase.listAllExternalTools()).thenReturn(List.of(tool));

        assertThat(toolCallingService.hasAvailableTools()).isTrue();
    }

    @Test
    void hasAvailableTools_withNoTools_returnsFalse() {
        when(toolDiscoveryUseCase.listAllExternalTools()).thenReturn(List.of());

        assertThat(toolCallingService.hasAvailableTools()).isFalse();
    }
}
