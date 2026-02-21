package com.localmind.api.mcp.controller;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.api.common.advice.GlobalExceptionHandler;
import com.localmind.api.mcp.dto.CreateMcpServerRequestDto;
import com.localmind.domain.mcp.model.McpServerConfig;
import com.localmind.domain.mcp.model.McpServerRegistration;
import com.localmind.domain.mcp.model.McpServerStatus;
import com.localmind.domain.mcp.model.McpServerType;
import com.localmind.domain.mcp.port.in.McpServerManagementUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

class McpServerControllerTest {

    private MockMvc mockMvc;
    private McpServerManagementUseCase useCase;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        useCase = mock(McpServerManagementUseCase.class);
        McpServerController controller = new McpServerController(useCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    private McpServerRegistration buildServer(String id, String name, McpServerType type,
                                               McpServerStatus status) {
        return McpServerRegistration.builder()
                .id(id)
                .name(name)
                .description("Test server description")
                .type(type)
                .config(McpServerConfig.builder()
                        .command("npx")
                        .args(List.of("-y", "@modelcontextprotocol/server"))
                        .url("http://localhost:3000")
                        .timeoutSeconds(30)
                        .autoReconnect(true)
                        .build())
                .status(status)
                .createdAt(LocalDateTime.of(2026, 2, 10, 10, 0, 0))
                .lastConnectedAt(LocalDateTime.of(2026, 2, 10, 10, 5, 0))
                .build();
    }

    @Test
    void register_shouldReturnServer() throws Exception {
        CreateMcpServerRequestDto request = CreateMcpServerRequestDto.builder()
                .name("My MCP Server")
                .description("Test server description")
                .type("STDIO")
                .command("npx")
                .args(List.of("-y", "@modelcontextprotocol/server"))
                .url("http://localhost:3000")
                .timeoutSeconds(30)
                .autoReconnect(true)
                .build();

        McpServerRegistration registered = buildServer("server-1", "My MCP Server",
                McpServerType.STDIO, McpServerStatus.CONNECTED);

        when(useCase.register(any(McpServerRegistration.class))).thenReturn(registered);

        mockMvc.perform(post("/api/v1/mcp/servers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("server-1"))
                .andExpect(jsonPath("$.name").value("My MCP Server"))
                .andExpect(jsonPath("$.type").value("STDIO"))
                .andExpect(jsonPath("$.status").value("CONNECTED"))
                .andExpect(jsonPath("$.config.command").value("npx"))
                .andExpect(jsonPath("$.config.args[0]").value("-y"))
                .andExpect(jsonPath("$.config.timeoutSeconds").value(30))
                .andExpect(jsonPath("$.config.autoReconnect").value(true));

        verify(useCase).register(any(McpServerRegistration.class));
    }

    @Test
    void listAll_shouldReturnServers() throws Exception {
        McpServerRegistration server1 = buildServer("server-1", "Server One",
                McpServerType.STDIO, McpServerStatus.CONNECTED);
        McpServerRegistration server2 = buildServer("server-2", "Server Two",
                McpServerType.SSE, McpServerStatus.DISCONNECTED);

        when(useCase.listAll()).thenReturn(List.of(server1, server2));

        mockMvc.perform(get("/api/v1/mcp/servers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("server-1"))
                .andExpect(jsonPath("$[0].name").value("Server One"))
                .andExpect(jsonPath("$[0].type").value("STDIO"))
                .andExpect(jsonPath("$[0].status").value("CONNECTED"))
                .andExpect(jsonPath("$[1].id").value("server-2"))
                .andExpect(jsonPath("$[1].name").value("Server Two"))
                .andExpect(jsonPath("$[1].type").value("SSE"))
                .andExpect(jsonPath("$[1].status").value("DISCONNECTED"));

        verify(useCase).listAll();
    }

    @Test
    void get_shouldReturnServer() throws Exception {
        McpServerRegistration server = buildServer("server-1", "My Server",
                McpServerType.STDIO, McpServerStatus.CONNECTED);

        when(useCase.get("server-1")).thenReturn(server);

        mockMvc.perform(get("/api/v1/mcp/servers/server-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("server-1"))
                .andExpect(jsonPath("$.name").value("My Server"))
                .andExpect(jsonPath("$.type").value("STDIO"))
                .andExpect(jsonPath("$.status").value("CONNECTED"))
                .andExpect(jsonPath("$.description").value("Test server description"));

        verify(useCase).get("server-1");
    }

    @Test
    void remove_shouldReturn204() throws Exception {
        doNothing().when(useCase).remove("server-1");

        mockMvc.perform(delete("/api/v1/mcp/servers/server-1"))
                .andExpect(status().isNoContent());

        verify(useCase).remove("server-1");
    }

    @Test
    void testConnection_shouldReturnUpdatedServer() throws Exception {
        McpServerRegistration tested = buildServer("server-1", "My Server",
                McpServerType.STDIO, McpServerStatus.CONNECTED);

        when(useCase.testConnection("server-1")).thenReturn(tested);

        mockMvc.perform(post("/api/v1/mcp/servers/server-1/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("server-1"))
                .andExpect(jsonPath("$.status").value("CONNECTED"));

        verify(useCase).testConnection("server-1");
    }
}
