package com.localmind.api.mcp.controller;

import com.localmind.api.mcp.dto.*;
import com.localmind.domain.mcp.model.*;
import com.localmind.domain.mcp.port.in.McpServerManagementUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/mcp/servers")
@Tag(name = "MCP Servers", description = "Gestione server MCP / MCP server management")
public class McpServerController {

    private final McpServerManagementUseCase useCase;

    public McpServerController(McpServerManagementUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @Operation(summary = "Registra server MCP / Register MCP server")
    @ApiResponse(responseCode = "200", description = "Server registrato / Server registered")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida / Bad request")
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
    @Operation(summary = "Lista server MCP / List MCP servers")
    @ApiResponse(responseCode = "200", description = "Lista server / Server list")
    public ResponseEntity<List<McpServerDto>> listAll() {
        return ResponseEntity.ok(useCase.listAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{serverId}")
    @Operation(summary = "Dettaglio server MCP / Get MCP server detail")
    @ApiResponse(responseCode = "200", description = "Server trovato / Server found")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<McpServerDto> get(@PathVariable String serverId) {
        return ResponseEntity.ok(toDto(useCase.get(serverId)));
    }

    @DeleteMapping("/{serverId}")
    @Operation(summary = "Rimuovi server MCP / Remove MCP server")
    @ApiResponse(responseCode = "204", description = "Server rimosso / Server removed")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<Void> remove(@PathVariable String serverId) {
        useCase.remove(serverId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{serverId}/test")
    @Operation(summary = "Testa connessione server MCP / Test MCP server connection")
    @ApiResponse(responseCode = "200", description = "Risultato test / Test result")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<McpServerDto> testConnection(@PathVariable String serverId) {
        return ResponseEntity.ok(toDto(useCase.testConnection(serverId)));
    }

    @PostMapping("/{serverId}/reconnect")
    @Operation(summary = "Riconnetti server MCP / Reconnect MCP server")
    @ApiResponse(responseCode = "200", description = "Riconnessione avviata / Reconnection started")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
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
