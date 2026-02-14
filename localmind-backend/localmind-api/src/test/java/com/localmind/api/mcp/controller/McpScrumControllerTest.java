package com.localmind.api.mcp.controller;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.api.common.advice.GlobalExceptionHandler;
import com.localmind.api.mcp.dto.CreateSprintRequest;
import com.localmind.api.mcp.dto.CreateStoryRequest;
import com.localmind.api.mcp.dto.CreateTaskRequest;
import com.localmind.api.mcp.dto.UpdateTaskStatusRequest;
import com.localmind.domain.common.exception.ResourceNotFoundException;
import com.localmind.domain.mcp.port.in.ScrumBoardUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

class McpScrumControllerTest {

    private MockMvc mockMvc;
    private ScrumBoardUseCase scrumBoardUseCase;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        scrumBoardUseCase = mock(ScrumBoardUseCase.class);
        McpScrumController controller = new McpScrumController(scrumBoardUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getBacklog_shouldReturnBacklogStories() throws Exception {
        Map<String, Object> backlog = Map.of(
                "stories", List.of(
                        Map.of("id", "story-1", "title", "Implement login", "storyPoints", 5),
                        Map.of("id", "story-2", "title", "Add search feature", "storyPoints", 8)
                ),
                "totalPoints", 13
        );
        when(scrumBoardUseCase.getBacklog()).thenReturn(backlog);

        mockMvc.perform(get("/api/v1/mcp/scrum/backlog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPoints").value(13))
                .andExpect(jsonPath("$.stories.length()").value(2))
                .andExpect(jsonPath("$.stories[0].title").value("Implement login"))
                .andExpect(jsonPath("$.stories[1].title").value("Add search feature"));

        verify(scrumBoardUseCase).getBacklog();
    }

    @Test
    void getSprint_shouldReturnSprintData() throws Exception {
        Map<String, Object> sprintData = Map.of(
                "id", "sprint-1",
                "name", "Sprint 1",
                "startDate", "2026-02-10",
                "endDate", "2026-02-24",
                "stories", List.of()
        );
        when(scrumBoardUseCase.getSprint("sprint-1")).thenReturn(sprintData);

        mockMvc.perform(get("/api/v1/mcp/scrum/sprints/sprint-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("sprint-1"))
                .andExpect(jsonPath("$.name").value("Sprint 1"))
                .andExpect(jsonPath("$.startDate").value("2026-02-10"))
                .andExpect(jsonPath("$.endDate").value("2026-02-24"));

        verify(scrumBoardUseCase).getSprint("sprint-1");
    }

    @Test
    void getSprint_notFound_shouldReturn404() throws Exception {
        when(scrumBoardUseCase.getSprint("nonexistent"))
                .thenThrow(new ResourceNotFoundException("Sprint not found: nonexistent"));

        mockMvc.perform(get("/api/v1/mcp/scrum/sprints/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Sprint not found: nonexistent"));

        verify(scrumBoardUseCase).getSprint("nonexistent");
    }

    @Test
    void sprintBoard_shouldReturnBoardColumns() throws Exception {
        Map<String, Object> board = Map.of(
                "sprintId", "sprint-1",
                "columns", Map.of(
                        "todo", List.of(Map.of("id", "task-1", "title", "Setup DB")),
                        "in_progress", List.of(),
                        "done", List.of()
                )
        );
        when(scrumBoardUseCase.sprintBoard("sprint-1")).thenReturn(board);

        mockMvc.perform(get("/api/v1/mcp/scrum/sprints/sprint-1/board"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sprintId").value("sprint-1"))
                .andExpect(jsonPath("$.columns.todo[0].title").value("Setup DB"));

        verify(scrumBoardUseCase).sprintBoard("sprint-1");
    }

    @Test
    void createSprint_shouldReturnCreatedSprint() throws Exception {
        CreateSprintRequest request = CreateSprintRequest.builder()
                .name("Sprint 2")
                .startDate("2026-02-24")
                .endDate("2026-03-10")
                .goals(List.of("Complete auth module", "Setup CI/CD"))
                .build();

        Map<String, Object> result = Map.of(
                "id", "sprint-2",
                "name", "Sprint 2",
                "startDate", "2026-02-24",
                "endDate", "2026-03-10",
                "goals", List.of("Complete auth module", "Setup CI/CD")
        );
        when(scrumBoardUseCase.createSprint("Sprint 2", "2026-02-24", "2026-03-10",
                List.of("Complete auth module", "Setup CI/CD"))).thenReturn(result);

        mockMvc.perform(post("/api/v1/mcp/scrum/sprints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("sprint-2"))
                .andExpect(jsonPath("$.name").value("Sprint 2"))
                .andExpect(jsonPath("$.goals.length()").value(2));

        verify(scrumBoardUseCase).createSprint("Sprint 2", "2026-02-24", "2026-03-10",
                List.of("Complete auth module", "Setup CI/CD"));
    }

    @Test
    void createStory_shouldReturnCreatedStory() throws Exception {
        CreateStoryRequest request = CreateStoryRequest.builder()
                .title("User authentication")
                .description("Implement JWT-based authentication")
                .acceptanceCriteria(List.of("Login works", "Token refresh works"))
                .storyPoints(8)
                .priority("high")
                .sprintId("sprint-1")
                .build();

        Map<String, Object> result = Map.of(
                "id", "story-1",
                "title", "User authentication",
                "storyPoints", 8,
                "priority", "high",
                "sprintId", "sprint-1"
        );
        when(scrumBoardUseCase.createStory("User authentication",
                "Implement JWT-based authentication",
                List.of("Login works", "Token refresh works"),
                8, "high", "sprint-1")).thenReturn(result);

        mockMvc.perform(post("/api/v1/mcp/scrum/stories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("story-1"))
                .andExpect(jsonPath("$.title").value("User authentication"))
                .andExpect(jsonPath("$.storyPoints").value(8))
                .andExpect(jsonPath("$.priority").value("high"))
                .andExpect(jsonPath("$.sprintId").value("sprint-1"));

        verify(scrumBoardUseCase).createStory("User authentication",
                "Implement JWT-based authentication",
                List.of("Login works", "Token refresh works"),
                8, "high", "sprint-1");
    }

    @Test
    void createStory_withoutSprintId_shouldReturnCreatedStory() throws Exception {
        CreateStoryRequest request = CreateStoryRequest.builder()
                .title("Backlog item")
                .description("A story in the backlog")
                .acceptanceCriteria(List.of("Criteria 1"))
                .storyPoints(3)
                .priority("low")
                .sprintId(null)
                .build();

        Map<String, Object> result = Map.of(
                "id", "story-2",
                "title", "Backlog item",
                "storyPoints", 3,
                "priority", "low"
        );
        when(scrumBoardUseCase.createStory("Backlog item",
                "A story in the backlog",
                List.of("Criteria 1"),
                3, "low", null)).thenReturn(result);

        mockMvc.perform(post("/api/v1/mcp/scrum/stories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("story-2"))
                .andExpect(jsonPath("$.title").value("Backlog item"))
                .andExpect(jsonPath("$.priority").value("low"));

        verify(scrumBoardUseCase).createStory("Backlog item",
                "A story in the backlog",
                List.of("Criteria 1"),
                3, "low", null);
    }

    @Test
    void createTask_shouldReturnCreatedTask() throws Exception {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Write unit tests")
                .description("Add tests for auth service")
                .storyId("story-1")
                .assignee("Federico")
                .build();

        Map<String, Object> result = Map.of(
                "id", "task-1",
                "title", "Write unit tests",
                "storyId", "story-1",
                "assignee", "Federico",
                "status", "todo"
        );
        when(scrumBoardUseCase.createTask("Write unit tests",
                "Add tests for auth service", "story-1", "Federico")).thenReturn(result);

        mockMvc.perform(post("/api/v1/mcp/scrum/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("task-1"))
                .andExpect(jsonPath("$.title").value("Write unit tests"))
                .andExpect(jsonPath("$.storyId").value("story-1"))
                .andExpect(jsonPath("$.assignee").value("Federico"))
                .andExpect(jsonPath("$.status").value("todo"));

        verify(scrumBoardUseCase).createTask("Write unit tests",
                "Add tests for auth service", "story-1", "Federico");
    }

    @Test
    void updateTaskStatus_shouldReturnUpdatedTask() throws Exception {
        UpdateTaskStatusRequest request = UpdateTaskStatusRequest.builder()
                .status("in_progress")
                .build();

        Map<String, Object> result = Map.of(
                "id", "task-1",
                "title", "Write unit tests",
                "status", "in_progress",
                "previousStatus", "todo"
        );
        when(scrumBoardUseCase.updateTaskStatus("task-1", "in_progress")).thenReturn(result);

        mockMvc.perform(put("/api/v1/mcp/scrum/tasks/task-1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("task-1"))
                .andExpect(jsonPath("$.status").value("in_progress"))
                .andExpect(jsonPath("$.previousStatus").value("todo"));

        verify(scrumBoardUseCase).updateTaskStatus("task-1", "in_progress");
    }

    @Test
    void updateTaskStatus_notFound_shouldReturn404() throws Exception {
        UpdateTaskStatusRequest request = UpdateTaskStatusRequest.builder()
                .status("done")
                .build();

        when(scrumBoardUseCase.updateTaskStatus("nonexistent", "done"))
                .thenThrow(new ResourceNotFoundException("Task not found: nonexistent"));

        mockMvc.perform(put("/api/v1/mcp/scrum/tasks/nonexistent/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Task not found: nonexistent"));

        verify(scrumBoardUseCase).updateTaskStatus("nonexistent", "done");
    }

    @Test
    void getBacklog_serviceThrowsException_shouldReturn500() throws Exception {
        when(scrumBoardUseCase.getBacklog())
                .thenThrow(new RuntimeException("Database connection failed"));

        mockMvc.perform(get("/api/v1/mcp/scrum/backlog"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Internal server error"));

        verify(scrumBoardUseCase).getBacklog();
    }
}
