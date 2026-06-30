package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.*;
import com.localmind.domain.mcp.port.out.McpClientPort;
import com.localmind.domain.mcp.port.out.McpServerRegistrationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class McpToolOrchestratorServiceTest {

    @Mock
    private McpServerRegistrationRepository serverRepository;

    @Mock
    private McpClientPort clientPort;

    @InjectMocks
    private McpToolOrchestratorService service;

    private McpServerRegistration buildServer(String id, McpServerStatus status) {
        return McpServerRegistration.builder()
                .id(id)
                .name("Server " + id)
                .type(McpServerType.STDIO)
                .config(McpServerConfig.builder().command("npx").build())
                .status(status)
                .build();
    }

    private McpExternalTool buildTool(String name, String serverId) {
        return McpExternalTool.builder()
                .name(name)
                .description("Tool " + name)
                .inputSchema("{}")
                .serverId(serverId)
                .build();
    }

    @Test
    void listExternalTools_shouldDelegateToClientPort() {
        List<McpExternalTool> tools = List.of(
                buildTool("tool-a", "server-1"),
                buildTool("tool-b", "server-1")
        );

        when(clientPort.discoverTools("server-1")).thenReturn(tools);

        List<McpExternalTool> result = service.listExternalTools("server-1");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(McpExternalTool::getName)
                .containsExactly("tool-a", "tool-b");
        verify(clientPort).discoverTools("server-1");
    }

    @Test
    void listAllExternalTools_shouldOnlyQueryConnectedServers() {
        McpServerRegistration connectedServer = buildServer("s1", McpServerStatus.CONNECTED);
        McpServerRegistration disconnectedServer = buildServer("s2", McpServerStatus.DISCONNECTED);
        McpServerRegistration errorServer = buildServer("s3", McpServerStatus.ERROR);

        when(serverRepository.findAll()).thenReturn(List.of(connectedServer, disconnectedServer, errorServer));
        when(clientPort.discoverTools("s1")).thenReturn(List.of(buildTool("tool-a", "s1")));

        List<McpExternalTool> result = service.listAllExternalTools();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("tool-a");
        verify(clientPort).discoverTools("s1");
        verify(clientPort, never()).discoverTools("s2");
        verify(clientPort, never()).discoverTools("s3");
    }

    @Test
    void listAllExternalTools_shouldAggregateToolsFromAllServers() {
        McpServerRegistration server1 = buildServer("s1", McpServerStatus.CONNECTED);
        McpServerRegistration server2 = buildServer("s2", McpServerStatus.CONNECTED);

        when(serverRepository.findAll()).thenReturn(List.of(server1, server2));
        when(clientPort.discoverTools("s1")).thenReturn(List.of(
                buildTool("tool-a", "s1"),
                buildTool("tool-b", "s1")
        ));
        when(clientPort.discoverTools("s2")).thenReturn(List.of(
                buildTool("tool-c", "s2")
        ));

        List<McpExternalTool> result = service.listAllExternalTools();

        assertThat(result).hasSize(3);
        assertThat(result).extracting(McpExternalTool::getName)
                .containsExactlyInAnyOrder("tool-a", "tool-b", "tool-c");
        verify(clientPort).discoverTools("s1");
        verify(clientPort).discoverTools("s2");
    }

    @Test
    void executeTool_shouldDelegateToClientPort() {
        McpToolExecutionRequest request = McpToolExecutionRequest.builder()
                .toolName("calculator")
                .serverId("server-1")
                .arguments(Map.of("expression", "2+2"))
                .build();

        McpToolExecutionResult expectedResult = McpToolExecutionResult.builder()
                .toolName("calculator")
                .result("4")
                .success(true)
                .executionTimeMs(15)
                .build();

        when(clientPort.executeTool("server-1", request)).thenReturn(expectedResult);

        McpToolExecutionResult result = service.executeTool(request);

        assertThat(result.getToolName()).isEqualTo("calculator");
        assertThat(result.getResult()).isEqualTo("4");
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getExecutionTimeMs()).isEqualTo(15);
        verify(clientPort).executeTool("server-1", request);
    }
}
