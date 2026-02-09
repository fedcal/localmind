package com.localmind.api.mcp.controller;

import com.localmind.api.mcp.dto.*;
import com.localmind.domain.mcp.model.*;
import com.localmind.domain.mcp.port.in.McpServerManagementUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/mcp/servers")
public class McpServerController {

    private final McpServerManagementUseCase useCase;

    public McpServerController(McpServerManagementUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<McpServerDto> register(@Valid @RequestBody CreateMcpServerRequestDto request) {
        McpServerRegistration server = McpServerRegistration.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(McpServerType.valueOf(request.getType()))
                .config(McpServerConfig.builder()
                        .command(request.getCommand())
                        .args(request.getArgs())
                        .url(request.getUrl())
                        .timeoutSeconds(request.getTimeoutSeconds() != null ? request.getTimeoutSeconds() : 30)
                        .autoReconnect(request.getAutoReconnect() != null ? request.getAutoReconnect() : true)
                        .build())
                .build();

        McpServerRegistration registered = useCase.register(server);
        return ResponseEntity.ok(toDto(registered));
    }

    @GetMapping
    public ResponseEntity<List<McpServerDto>> listAll() {
        return ResponseEntity.ok(useCase.listAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{serverId}")
    public ResponseEntity<McpServerDto> get(@PathVariable String serverId) {
        return ResponseEntity.ok(toDto(useCase.get(serverId)));
    }

    @DeleteMapping("/{serverId}")
    public ResponseEntity<Void> remove(@PathVariable String serverId) {
        useCase.remove(serverId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{serverId}/test")
    public ResponseEntity<McpServerDto> testConnection(@PathVariable String serverId) {
        return ResponseEntity.ok(toDto(useCase.testConnection(serverId)));
    }

    @PostMapping("/{serverId}/reconnect")
    public ResponseEntity<Void> reconnect(@PathVariable String serverId) {
        useCase.reconnect(serverId);
        return ResponseEntity.ok().build();
    }

    private McpServerDto toDto(McpServerRegistration server) {
        McpServerConfig config = server.getConfig();
        return McpServerDto.builder()
                .id(server.getId())
                .name(server.getName())
                .description(server.getDescription())
                .type(server.getType().name())
                .status(server.getStatus().name())
                .config(McpServerConfigDto.builder()
                        .command(config != null ? config.getCommand() : null)
                        .args(config != null ? config.getArgs() : null)
                        .url(config != null ? config.getUrl() : null)
                        .timeoutSeconds(config != null ? config.getTimeoutSeconds() : null)
                        .autoReconnect(config != null ? config.isAutoReconnect() : null)
                        .build())
                .createdAt(server.getCreatedAt())
                .lastConnectedAt(server.getLastConnectedAt())
                .build();
    }
}
