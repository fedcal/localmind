package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.McpServerConfig;
import com.localmind.domain.mcp.model.McpServerRegistration;
import com.localmind.domain.mcp.model.McpServerStatus;
import com.localmind.domain.mcp.model.McpServerType;
import com.localmind.domain.mcp.port.out.McpClientPort;
import com.localmind.domain.mcp.port.out.McpServerRegistrationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class McpServerManagementServiceTest {

    @Mock
    private McpServerRegistrationRepository repository;

    @Mock
    private McpClientPort clientPort;

    @InjectMocks
    private McpServerManagementService service;

    private McpServerRegistration buildServer(String id, String name) {
        return McpServerRegistration.builder()
                .id(id)
                .name(name)
                .type(McpServerType.STDIO)
                .config(McpServerConfig.builder().command("npx").args(List.of("server")).build())
                .build();
    }

    @Test
    void register_shouldSetIdAndStatusDisconnected() {
        McpServerRegistration server = McpServerRegistration.builder()
                .name("Test Server")
                .type(McpServerType.STDIO)
                .config(McpServerConfig.builder().command("npx").build())
                .build();

        when(repository.save(any(McpServerRegistration.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(clientPort.connect(any(McpServerRegistration.class))).thenReturn(false);

        McpServerRegistration result = service.register(server);

        assertThat(result.getId()).isNotNull().isNotBlank();
        assertThat(result.getStatus()).isEqualTo(McpServerStatus.DISCONNECTED);
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    void register_withSuccessfulConnect_shouldSetConnected() {
        McpServerRegistration server = McpServerRegistration.builder()
                .name("Test Server")
                .type(McpServerType.STDIO)
                .config(McpServerConfig.builder().command("npx").build())
                .build();

        when(repository.save(any(McpServerRegistration.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(clientPort.connect(any(McpServerRegistration.class))).thenReturn(true);

        McpServerRegistration result = service.register(server);

        assertThat(result.getStatus()).isEqualTo(McpServerStatus.CONNECTED);
        assertThat(result.getLastConnectedAt()).isNotNull();
        // save is called twice: once for initial persist, once after status update
        verify(repository, times(2)).save(any(McpServerRegistration.class));
    }

    @Test
    void register_withFailedConnect_shouldStayDisconnected() {
        McpServerRegistration server = McpServerRegistration.builder()
                .name("Unreachable Server")
                .type(McpServerType.SSE)
                .config(McpServerConfig.builder().url("http://localhost:9999").build())
                .build();

        when(repository.save(any(McpServerRegistration.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(clientPort.connect(any(McpServerRegistration.class))).thenReturn(false);

        McpServerRegistration result = service.register(server);

        assertThat(result.getStatus()).isEqualTo(McpServerStatus.DISCONNECTED);
        assertThat(result.getLastConnectedAt()).isNull();
        // save is called only once (no second save for status update)
        verify(repository, times(1)).save(any(McpServerRegistration.class));
    }

    @Test
    void remove_shouldDisconnectAndDelete() {
        service.remove("server-1");

        verify(clientPort).disconnect("server-1");
        verify(repository).delete("server-1");
    }

    @Test
    void get_shouldReturnServer() {
        McpServerRegistration server = buildServer("server-1", "My Server");

        when(repository.findById("server-1")).thenReturn(Optional.of(server));

        McpServerRegistration result = service.get("server-1");

        assertThat(result.getId()).isEqualTo("server-1");
        assertThat(result.getName()).isEqualTo("My Server");
        verify(repository).findById("server-1");
    }

    @Test
    void get_shouldThrowWhenNotFound() {
        when(repository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get("nonexistent"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nonexistent");
    }

    @Test
    void listAll_shouldReturnAll() {
        List<McpServerRegistration> servers = List.of(
                buildServer("s1", "Server 1"),
                buildServer("s2", "Server 2")
        );

        when(repository.findAll()).thenReturn(servers);

        List<McpServerRegistration> result = service.listAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(McpServerRegistration::getName)
                .containsExactly("Server 1", "Server 2");
        verify(repository).findAll();
    }

    @Test
    void testConnection_success_shouldSetConnected() {
        McpServerRegistration server = buildServer("server-1", "My Server");
        server.setStatus(McpServerStatus.DISCONNECTED);

        when(repository.findById("server-1")).thenReturn(Optional.of(server));
        when(clientPort.testConnection("server-1")).thenReturn(true);
        when(repository.save(any(McpServerRegistration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        McpServerRegistration result = service.testConnection("server-1");

        assertThat(result.getStatus()).isEqualTo(McpServerStatus.CONNECTED);
        assertThat(result.getLastConnectedAt()).isNotNull();
        verify(repository).save(server);
    }

    @Test
    void testConnection_failure_shouldSetError() {
        McpServerRegistration server = buildServer("server-1", "My Server");
        server.setStatus(McpServerStatus.CONNECTED);

        when(repository.findById("server-1")).thenReturn(Optional.of(server));
        when(clientPort.testConnection("server-1")).thenReturn(false);
        when(repository.save(any(McpServerRegistration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        McpServerRegistration result = service.testConnection("server-1");

        assertThat(result.getStatus()).isEqualTo(McpServerStatus.ERROR);
        verify(repository).save(server);
    }

    @Test
    void reconnect_shouldDisconnectThenReconnect() {
        McpServerRegistration server = buildServer("server-1", "My Server");
        server.setStatus(McpServerStatus.ERROR);

        // Track the status at each save invocation (the object is mutated, so capture status eagerly)
        java.util.List<McpServerStatus> statusesAtSave = new java.util.ArrayList<>();
        when(repository.findById("server-1")).thenReturn(Optional.of(server));
        when(repository.save(any(McpServerRegistration.class))).thenAnswer(invocation -> {
            McpServerRegistration arg = invocation.getArgument(0);
            statusesAtSave.add(arg.getStatus());
            return arg;
        });
        when(clientPort.connect(any(McpServerRegistration.class))).thenReturn(true);

        service.reconnect("server-1");

        // Verify the sequence: disconnect, then connect
        verify(clientPort).disconnect("server-1");
        verify(clientPort).connect(server);

        // Verify save was called at least twice, first with CONNECTING then with CONNECTED
        assertThat(statusesAtSave).hasSizeGreaterThanOrEqualTo(2);
        assertThat(statusesAtSave.get(0)).isEqualTo(McpServerStatus.CONNECTING);
        assertThat(statusesAtSave.get(statusesAtSave.size() - 1)).isEqualTo(McpServerStatus.CONNECTED);
        assertThat(server.getLastConnectedAt()).isNotNull();
    }
}
