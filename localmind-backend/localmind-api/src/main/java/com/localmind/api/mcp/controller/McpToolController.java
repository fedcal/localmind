package com.localmind.api.mcp.controller;

import com.localmind.api.mcp.dto.*;
import com.localmind.domain.mcp.model.*;
import com.localmind.domain.mcp.port.in.McpToolDiscoveryUseCase;
import com.localmind.domain.mcp.port.in.McpToolExecutionUseCase;
import com.localmind.domain.mcp.port.out.LocalToolDiscoveryPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/mcp/tools")
@Tag(name = "MCP Tools", description = "MCP tool discovery and execution")
public class McpToolController {

    private final McpToolDiscoveryUseCase discoveryUseCase;
    private final McpToolExecutionUseCase executionUseCase;
    private final LocalToolDiscoveryPort localToolDiscoveryService;

    public McpToolController(McpToolDiscoveryUseCase discoveryUseCase,
                             McpToolExecutionUseCase executionUseCase,
                             LocalToolDiscoveryPort localToolDiscoveryService) {
        this.discoveryUseCase = discoveryUseCase;
        this.executionUseCase = executionUseCase;
        this.localToolDiscoveryService = localToolDiscoveryService;
    }

    @GetMapping
    @Operation(summary = "Lista tutti i tool MCP esterni / List all external MCP tools")
    @ApiResponse(responseCode = "200", description = "Lista tool / Tool list")
    public ResponseEntity<List<McpToolDto>> listAllTools() {
        return ResponseEntity.ok(discoveryUseCase.listAllExternalTools().stream()
                .map(this::toDto)
                .collect(Collectors.toList()));
    }

    @GetMapping("/servers/{serverId}")
    @Operation(summary = "Lista tool di un server MCP / List tools of an MCP server")
    @ApiResponse(responseCode = "200", description = "Lista tool del server / Server tool list")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<List<McpToolDto>> listServerTools(@PathVariable String serverId) {
        return ResponseEntity.ok(discoveryUseCase.listExternalTools(serverId).stream()
                .map(this::toDto)
                .collect(Collectors.toList()));
    }

    @GetMapping("/local")
    @Operation(summary = "Lista tool locali / List local tools")
    @ApiResponse(responseCode = "200", description = "Lista tool locali / Local tool list")
    public ResponseEntity<List<McpToolDto>> listLocalTools() {
        List<McpToolDto> localTools = localToolDiscoveryService.discoverLocalTools().stream()
                .map(tool -> McpToolDto.builder()
                        .name((String) tool.get("name"))
                        .description((String) tool.get("description"))
                        .local(true)
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(localTools);
    }

    @PostMapping("/execute")
    @Operation(summary = "Esegui tool MCP / Execute MCP tool")
    @ApiResponse(responseCode = "200", description = "Risultato esecuzione / Execution result")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida / Bad request")
    public ResponseEntity<McpToolExecutionResultDto> executeTool(@Valid @RequestBody McpToolExecutionRequestDto request) {
        McpToolExecutionRequest domainRequest = McpToolExecutionRequest.builder()
                .toolName(request.getToolName())
                .serverId(request.getServerId())
                .arguments(request.getArguments())
                .build();

        McpToolExecutionResult result = executionUseCase.executeTool(domainRequest);
        return ResponseEntity.ok(McpToolExecutionResultDto.builder()
                .toolName(result.getToolName())
                .result(result.getResult())
                .success(result.isSuccess())
                .errorMessage(result.getErrorMessage())
                .executionTimeMs(result.getExecutionTimeMs())
                .build());
    }

    private McpToolDto toDto(McpExternalTool tool) {
        return McpToolDto.builder()
                .name(tool.getName())
                .description(tool.getDescription())
                .inputSchema(tool.getInputSchema())
                .serverId(tool.getServerId())
                .local(false)
                .build();
    }
}
