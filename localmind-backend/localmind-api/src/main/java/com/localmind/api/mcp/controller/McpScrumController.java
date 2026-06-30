package com.localmind.api.mcp.controller;

import com.localmind.api.mcp.dto.*;
import com.localmind.domain.mcp.port.in.ScrumBoardUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/mcp/scrum")
@Tag(name = "MCP Scrum", description = "Scrum board management")
public class McpScrumController {

    private final ScrumBoardUseCase scrumBoardUseCase;

    public McpScrumController(ScrumBoardUseCase scrumBoardUseCase) {
        this.scrumBoardUseCase = scrumBoardUseCase;
    }

    @GetMapping("/backlog")
    @Operation(summary = "Ottieni backlog / Get backlog")
    @ApiResponse(responseCode = "200", description = "Backlog / Backlog data")
    public ResponseEntity<Map<String, Object>> getBacklog() {
        return ResponseEntity.ok(scrumBoardUseCase.getBacklog());
    }

    @GetMapping("/sprints/{sprintId}")
    @Operation(summary = "Dettaglio sprint / Get sprint detail")
    @ApiResponse(responseCode = "200", description = "Sprint trovato / Sprint found")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<Map<String, Object>> getSprint(@PathVariable String sprintId) {
        return ResponseEntity.ok(scrumBoardUseCase.getSprint(sprintId));
    }

    @GetMapping("/sprints/{sprintId}/board")
    @Operation(summary = "Board dello sprint / Sprint board")
    @ApiResponse(responseCode = "200", description = "Board sprint / Sprint board data")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<Map<String, Object>> sprintBoard(@PathVariable String sprintId) {
        return ResponseEntity.ok(scrumBoardUseCase.sprintBoard(sprintId));
    }

    @PostMapping("/sprints")
    @Operation(summary = "Crea sprint / Create sprint")
    @ApiResponse(responseCode = "200", description = "Sprint creato / Sprint created")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida / Bad request")
    public ResponseEntity<Map<String, Object>> createSprint(@RequestBody CreateSprintRequest request) {
        return ResponseEntity.ok(scrumBoardUseCase.createSprint(
                request.getName(),
                request.getStartDate(),
                request.getEndDate(),
                request.getGoals()));
    }

    @PostMapping("/stories")
    @Operation(summary = "Crea user story / Create user story")
    @ApiResponse(responseCode = "200", description = "Story creata / Story created")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida / Bad request")
    public ResponseEntity<Map<String, Object>> createStory(@RequestBody CreateStoryRequest request) {
        return ResponseEntity.ok(scrumBoardUseCase.createStory(
                request.getTitle(),
                request.getDescription(),
                request.getAcceptanceCriteria(),
                request.getStoryPoints(),
                request.getPriority(),
                request.getSprintId()));
    }

    @PostMapping("/tasks")
    @Operation(summary = "Crea task / Create task")
    @ApiResponse(responseCode = "200", description = "Task creato / Task created")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida / Bad request")
    public ResponseEntity<Map<String, Object>> createTask(@RequestBody CreateTaskRequest request) {
        return ResponseEntity.ok(scrumBoardUseCase.createTask(
                request.getTitle(),
                request.getDescription(),
                request.getStoryId(),
                request.getAssignee()));
    }

    @PutMapping("/tasks/{taskId}/status")
    @Operation(summary = "Aggiorna stato task / Update task status")
    @ApiResponse(responseCode = "200", description = "Stato aggiornato / Status updated")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida / Bad request")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<Map<String, Object>> updateTaskStatus(
            @PathVariable String taskId,
            @RequestBody UpdateTaskStatusRequest request) {
        return ResponseEntity.ok(scrumBoardUseCase.updateTaskStatus(taskId, request.getStatus()));
    }
}
