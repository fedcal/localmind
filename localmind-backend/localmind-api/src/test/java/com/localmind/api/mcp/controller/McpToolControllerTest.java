package com.localmind.api.mcp.controller;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.api.common.advice.GlobalExceptionHandler;
import com.localmind.api.mcp.dto.McpToolExecutionRequestDto;
import com.localmind.domain.mcp.model.McpExternalTool;
import com.localmind.domain.mcp.model.McpToolExecutionRequest;
import com.localmind.domain.mcp.model.McpToolExecutionResult;
import com.localmind.domain.mcp.port.in.McpToolDiscoveryUseCase;
import com.localmind.domain.mcp.port.in.McpToolExecutionUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

class McpToolControllerTest {

    private MockMvc mockMvc;
    private McpToolDiscoveryUseCase discoveryUseCase;
    private McpToolExecutionUseCase executionUseCase;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        discoveryUseCase = mock(McpToolDiscoveryUseCase.class);
        executionUseCase = mock(McpToolExecutionUseCase.class);
        McpToolController controller = new McpToolController(discoveryUseCase, executionUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void listAllTools_shouldReturnExternalTools() throws Exception {
        McpExternalTool tool1 = McpExternalTool.builder()
                .name("web_search")
                .description("Search the web")
                .inputSchema("{\"type\":\"object\"}")
                .serverId("server-1")
                .build();
        McpExternalTool tool2 = McpExternalTool.builder()
                .name("file_reader")
                .description("Read files")
                .inputSchema("{\"type\":\"object\"}")
                .serverId("server-2")
                .build();

        when(discoveryUseCase.listAllExternalTools()).thenReturn(List.of(tool1, tool2));

        mockMvc.perform(get("/api/v1/mcp/tools"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("web_search"))
                .andExpect(jsonPath("$[0].description").value("Search the web"))
                .andExpect(jsonPath("$[0].serverId").value("server-1"))
                .andExpect(jsonPath("$[0].local").value(false))
                .andExpect(jsonPath("$[1].name").value("file_reader"))
                .andExpect(jsonPath("$[1].local").value(false));

        verify(discoveryUseCase).listAllExternalTools();
    }

    @Test
    void listServerTools_shouldReturnTools() throws Exception {
        McpExternalTool tool = McpExternalTool.builder()
                .name("execute_query")
                .description("Execute a database query")
                .inputSchema("{\"type\":\"object\",\"properties\":{\"query\":{\"type\":\"string\"}}}")
                .serverId("server-1")
                .build();

        when(discoveryUseCase.listExternalTools("server-1")).thenReturn(List.of(tool));

        mockMvc.perform(get("/api/v1/mcp/tools/servers/server-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("execute_query"))
                .andExpect(jsonPath("$[0].description").value("Execute a database query"))
                .andExpect(jsonPath("$[0].serverId").value("server-1"))
                .andExpect(jsonPath("$[0].local").value(false));

        verify(discoveryUseCase).listExternalTools("server-1");
    }

    @Test
    void listLocalTools_shouldReturn3Tools() throws Exception {
        mockMvc.perform(get("/api/v1/mcp/tools/local"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("document_search"))
                .andExpect(jsonPath("$[0].description").value("Search documents using RAG"))
                .andExpect(jsonPath("$[0].local").value(true))
                .andExpect(jsonPath("$[1].name").value("chat"))
                .andExpect(jsonPath("$[1].description").value("Send a message to an LLM"))
                .andExpect(jsonPath("$[1].local").value(true))
                .andExpect(jsonPath("$[2].name").value("list_models"))
                .andExpect(jsonPath("$[2].description").value("List available LLM providers"))
                .andExpect(jsonPath("$[2].local").value(true));

        verifyNoInteractions(discoveryUseCase);
        verifyNoInteractions(executionUseCase);
    }

    @Test
    void executeTool_shouldReturnResult() throws Exception {
        McpToolExecutionRequestDto request = McpToolExecutionRequestDto.builder()
                .toolName("web_search")
                .serverId("server-1")
                .arguments(Map.of("query", "test search"))
                .build();

        McpToolExecutionResult domainResult = McpToolExecutionResult.builder()
                .toolName("web_search")
                .result("Search results found")
                .success(true)
                .errorMessage(null)
                .executionTimeMs(150L)
                .build();

        when(executionUseCase.executeTool(any(McpToolExecutionRequest.class))).thenReturn(domainResult);

        mockMvc.perform(post("/api/v1/mcp/tools/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.toolName").value("web_search"))
                .andExpect(jsonPath("$.result").value("Search results found"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.errorMessage").doesNotExist())
                .andExpect(jsonPath("$.executionTimeMs").value(150));

        verify(executionUseCase).executeTool(any(McpToolExecutionRequest.class));
    }
}
