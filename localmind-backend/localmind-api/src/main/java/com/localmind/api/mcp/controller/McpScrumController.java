package com.localmind.api.mcp.controller;

import com.localmind.api.mcp.dto.*;
import com.localmind.domain.mcp.port.in.ScrumBoardUseCase;
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
    public ResponseEntity<Map<String, Object>> getBacklog() {
        return ResponseEntity.ok(scrumBoardUseCase.getBacklog());
    }

    @GetMapping("/sprints/{sprintId}")
    public ResponseEntity<Map<String, Object>> getSprint(@PathVariable String sprintId) {
        return ResponseEntity.ok(scrumBoardUseCase.getSprint(sprintId));
    }

    @GetMapping("/sprints/{sprintId}/board")
    public ResponseEntity<Map<String, Object>> sprintBoard(@PathVariable String sprintId) {
        return ResponseEntity.ok(scrumBoardUseCase.sprintBoard(sprintId));
    }

    @PostMapping("/sprints")
    public ResponseEntity<Map<String, Object>> createSprint(@RequestBody CreateSprintRequest request) {
        return ResponseEntity.ok(scrumBoardUseCase.createSprint(
                request.getName(),
                request.getStartDate(),
                request.getEndDate(),
                request.getGoals()));
    }

    @PostMapping("/stories")
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
    public ResponseEntity<Map<String, Object>> createTask(@RequestBody CreateTaskRequest request) {
        return ResponseEntity.ok(scrumBoardUseCase.createTask(
                request.getTitle(),
                request.getDescription(),
                request.getStoryId(),
                request.getAssignee()));
    }

    @PutMapping("/tasks/{taskId}/status")
    public ResponseEntity<Map<String, Object>> updateTaskStatus(
            @PathVariable String taskId,
            @RequestBody UpdateTaskStatusRequest request) {
        return ResponseEntity.ok(scrumBoardUseCase.updateTaskStatus(taskId, request.getStatus()));
    }
}
