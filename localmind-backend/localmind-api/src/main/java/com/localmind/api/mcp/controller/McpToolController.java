package com.localmind.api.mcp.controller;

import com.localmind.api.mcp.dto.*;
import com.localmind.domain.mcp.model.*;
import com.localmind.domain.mcp.port.in.McpToolDiscoveryUseCase;
import com.localmind.domain.mcp.port.in.McpToolExecutionUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/mcp/tools")
public class McpToolController {

    private final McpToolDiscoveryUseCase discoveryUseCase;
    private final McpToolExecutionUseCase executionUseCase;

    public McpToolController(McpToolDiscoveryUseCase discoveryUseCase, McpToolExecutionUseCase executionUseCase) {
        this.discoveryUseCase = discoveryUseCase;
        this.executionUseCase = executionUseCase;
    }

    @GetMapping
    public ResponseEntity<List<McpToolDto>> listAllTools() {
        return ResponseEntity.ok(discoveryUseCase.listAllExternalTools().stream()
                .map(this::toDto)
                .collect(Collectors.toList()));
    }

    @GetMapping("/servers/{serverId}")
    public ResponseEntity<List<McpToolDto>> listServerTools(@PathVariable String serverId) {
        return ResponseEntity.ok(discoveryUseCase.listExternalTools(serverId).stream()
                .map(this::toDto)
                .collect(Collectors.toList()));
    }

    @GetMapping("/local")
    public ResponseEntity<List<McpToolDto>> listLocalTools() {
        List<McpToolDto> localTools = List.of(
                McpToolDto.builder().name("document_search").description("Search documents using RAG").local(true).build(),
                McpToolDto.builder().name("chat").description("Send a message to an LLM").local(true).build(),
                McpToolDto.builder().name("list_models").description("List available LLM providers").local(true).build()
        );
        return ResponseEntity.ok(localTools);
    }

    @PostMapping("/execute")
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
