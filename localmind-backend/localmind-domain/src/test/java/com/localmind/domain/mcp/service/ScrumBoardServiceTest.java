package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.ScrumTask;
import com.localmind.domain.mcp.model.Sprint;
import com.localmind.domain.mcp.model.UserStory;
import com.localmind.domain.mcp.port.out.ScrumTaskRepository;
import com.localmind.domain.mcp.port.out.SprintRepository;
import com.localmind.domain.mcp.port.out.UserStoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ScrumBoardServiceTest {

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private UserStoryRepository userStoryRepository;

    @Mock
    private ScrumTaskRepository scrumTaskRepository;

    private ScrumBoardService service;

    private static final Instant NOW = Instant.parse("2026-02-12T10:00:00Z");

    @BeforeEach
    void setUp() {
        service = new ScrumBoardService(sprintRepository, userStoryRepository, scrumTaskRepository);

        when(sprintRepository.save(any(Sprint.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(userStoryRepository.save(any(UserStory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(scrumTaskRepository.save(any(ScrumTask.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    // ========== createSprint Tests ==========

    @Test
    void createSprint_withValidInput_createsSuccessfully() {
        List<String> goals = List.of("Complete login", "Deploy staging");

        Map<String, Object> result = service.createSprint("Sprint 1", "2026-02-12", "2026-02-26", goals);

        assertThat(result.get("name")).isEqualTo("Sprint 1");
        assertThat(result.get("startDate")).isEqualTo("2026-02-12");
        assertThat(result.get("endDate")).isEqualTo("2026-02-26");
        assertThat(result.get("goals")).isEqualTo(goals);
        assertThat(result.get("id")).isNotNull();
        assertThat(result.get("createdAt")).isNotNull();
        assertThat(result.get("message")).isEqualTo("Sprint 'Sprint 1' created successfully");

        verify(sprintRepository).save(any(Sprint.class));
    }

    @Test
    void createSprint_withNullName_returnsError() {
        Map<String, Object> result = service.createSprint(null, "2026-02-12", "2026-02-26", List.of());

        assertThat(result.get("error")).isEqualTo("Sprint name is required");
        verify(sprintRepository, never()).save(any());
    }

    @Test
    void createSprint_withBlankName_returnsError() {
        Map<String, Object> result = service.createSprint("   ", "2026-02-12", "2026-02-26", List.of());

        assertThat(result.get("error")).isEqualTo("Sprint name is required");
        verify(sprintRepository, never()).save(any());
    }

    @Test
    void createSprint_withEmptyName_returnsError() {
        Map<String, Object> result = service.createSprint("", "2026-02-12", "2026-02-26", List.of());

        assertThat(result.get("error")).isEqualTo("Sprint name is required");
        verify(sprintRepository, never()).save(any());
    }

    @Test
    void createSprint_withNullGoals_usesEmptyArray() {
        Map<String, Object> result = service.createSprint("Sprint 1", "2026-02-12", "2026-02-26", null);

        assertThat(result.get("goals")).isEqualTo(List.of());
        assertThat(result.get("name")).isEqualTo("Sprint 1");

        ArgumentCaptor<Sprint> captor = ArgumentCaptor.forClass(Sprint.class);
        verify(sprintRepository).save(captor.capture());
        assertThat(captor.getValue().getGoalsJson()).isEqualTo("[]");
    }

    @Test
    void createSprint_withEmptyGoals_usesEmptyArray() {
        Map<String, Object> result = service.createSprint("Sprint 1", "2026-02-12", "2026-02-26", List.of());

        ArgumentCaptor<Sprint> captor = ArgumentCaptor.forClass(Sprint.class);
        verify(sprintRepository).save(captor.capture());
        assertThat(captor.getValue().getGoalsJson()).isEqualTo("[]");
    }

    @Test
    void createSprint_withNullDates_createsSuccessfully() {
        Map<String, Object> result = service.createSprint("Sprint 1", null, null, List.of("Goal"));

        assertThat(result.get("name")).isEqualTo("Sprint 1");
        assertThat(result.get("startDate")).isNull();
        assertThat(result.get("endDate")).isNull();
        verify(sprintRepository).save(any(Sprint.class));
    }

    @Test
    void createSprint_trimsName() {
        Map<String, Object> result = service.createSprint("  Sprint 1  ", "2026-02-12", "2026-02-26", List.of());

        assertThat(result.get("name")).isEqualTo("Sprint 1");
    }

    @Test
    void createSprint_savesGoalsAsJsonArray() {
        List<String> goals = List.of("Goal 1", "Goal 2", "Goal 3");

        service.createSprint("Sprint 1", "2026-02-12", "2026-02-26", goals);

        ArgumentCaptor<Sprint> captor = ArgumentCaptor.forClass(Sprint.class);
        verify(sprintRepository).save(captor.capture());
        Sprint saved = captor.getValue();
        assertThat(saved.getGoalsJson()).contains("Goal 1");
        assertThat(saved.getGoalsJson()).contains("Goal 2");
        assertThat(saved.getGoalsJson()).contains("Goal 3");
        assertThat(saved.getGoalsJson()).startsWith("[");
        assertThat(saved.getGoalsJson()).endsWith("]");
    }

    @Test
    void createSprint_generatesUniqueId() {
        Map<String, Object> result1 = service.createSprint("Sprint 1", null, null, List.of());
        Map<String, Object> result2 = service.createSprint("Sprint 2", null, null, List.of());

        assertThat(result1.get("id")).isNotEqualTo(result2.get("id"));
    }

    // ========== createStory Tests ==========

    @Test
    void createStory_withValidInput_createsSuccessfully() {
        List<String> ac = List.of("User can login", "Error shown on failure");

        Map<String, Object> result = service.createStory("Login feature", "Implement login page",
                ac, 5, "high", "sprint-1");

        assertThat(result.get("title")).isEqualTo("Login feature");
        assertThat(result.get("description")).isEqualTo("Implement login page");
        assertThat(result.get("storyPoints")).isEqualTo(5);
        assertThat(result.get("priority")).isEqualTo("high");
        assertThat(result.get("sprintId")).isEqualTo("sprint-1");
        assertThat(result.get("id")).isNotNull();
        assertThat(result.get("message")).isEqualTo("User story 'Login feature' created successfully");

        verify(userStoryRepository).save(any(UserStory.class));
    }

    @Test
    void createStory_withNullTitle_returnsError() {
        Map<String, Object> result = service.createStory(null, "desc", List.of(), 3, "medium", null);

        assertThat(result.get("error")).isEqualTo("Story title is required");
        verify(userStoryRepository, never()).save(any());
    }

    @Test
    void createStory_withBlankTitle_returnsError() {
        Map<String, Object> result = service.createStory("  ", "desc", List.of(), 3, "medium", null);

        assertThat(result.get("error")).isEqualTo("Story title is required");
        verify(userStoryRepository, never()).save(any());
    }

    @Test
    void createStory_withEmptyTitle_returnsError() {
        Map<String, Object> result = service.createStory("", "desc", List.of(), 3, "medium", null);

        assertThat(result.get("error")).isEqualTo("Story title is required");
        verify(userStoryRepository, never()).save(any());
    }

    @Test
    void createStory_withInvalidPriority_defaultsToMedium() {
        Map<String, Object> result = service.createStory("Story", "desc", List.of(), 3, "invalid", null);

        assertThat(result.get("priority")).isEqualTo("medium");
    }

    @Test
    void createStory_withNullPriority_defaultsToMedium() {
        Map<String, Object> result = service.createStory("Story", "desc", List.of(), 3, null, null);

        assertThat(result.get("priority")).isEqualTo("medium");
    }

    @Test
    void createStory_withValidPriorities_acceptsAll() {
        for (String priority : List.of("low", "medium", "high", "critical")) {
            Map<String, Object> result = service.createStory("Story", "desc", List.of(), 3, priority, null);
            assertThat(result.get("priority")).isEqualTo(priority);
        }
    }

    @Test
    void createStory_withNegativeStoryPoints_usesZero() {
        Map<String, Object> result = service.createStory("Story", "desc", List.of(), -5, "medium", null);

        assertThat(result.get("storyPoints")).isEqualTo(0);
    }

    @Test
    void createStory_withNullSprintId_createsWithoutSprint() {
        Map<String, Object> result = service.createStory("Story", "desc", List.of(), 3, "medium", null);

        assertThat(result.get("sprintId")).isNull();
        verify(userStoryRepository).save(any(UserStory.class));
    }

    @Test
    void createStory_withNullAcceptanceCriteria_usesEmptyArray() {
        service.createStory("Story", "desc", null, 3, "medium", null);

        ArgumentCaptor<UserStory> captor = ArgumentCaptor.forClass(UserStory.class);
        verify(userStoryRepository).save(captor.capture());
        assertThat(captor.getValue().getAcceptanceCriteriaJson()).isEqualTo("[]");
    }

    @Test
    void createStory_trimsTitle() {
        Map<String, Object> result = service.createStory("  Story Title  ", "desc", List.of(), 3, "medium", null);

        assertThat(result.get("title")).isEqualTo("Story Title");
    }

    @Test
    void createStory_savesAcceptanceCriteriaAsJson() {
        List<String> ac = List.of("Criterion 1", "Criterion 2");
        service.createStory("Story", "desc", ac, 3, "medium", null);

        ArgumentCaptor<UserStory> captor = ArgumentCaptor.forClass(UserStory.class);
        verify(userStoryRepository).save(captor.capture());
        String json = captor.getValue().getAcceptanceCriteriaJson();
        assertThat(json).contains("Criterion 1");
        assertThat(json).contains("Criterion 2");
        assertThat(json).startsWith("[");
        assertThat(json).endsWith("]");
    }

    // ========== createTask Tests ==========

    @Test
    void createTask_withValidInput_createsSuccessfully() {
        Map<String, Object> result = service.createTask("Implement login form", "Build the HTML form",
                "story-1", "John");

        assertThat(result.get("title")).isEqualTo("Implement login form");
        assertThat(result.get("description")).isEqualTo("Build the HTML form");
        assertThat(result.get("storyId")).isEqualTo("story-1");
        assertThat(result.get("assignee")).isEqualTo("John");
        assertThat(result.get("status")).isEqualTo("todo");
        assertThat(result.get("id")).isNotNull();
        assertThat(result.get("message")).isEqualTo("Task 'Implement login form' created successfully");

        verify(scrumTaskRepository).save(any(ScrumTask.class));
    }

    @Test
    void createTask_withNullTitle_returnsError() {
        Map<String, Object> result = service.createTask(null, "desc", "story-1", "John");

        assertThat(result.get("error")).isEqualTo("Task title is required");
        verify(scrumTaskRepository, never()).save(any());
    }

    @Test
    void createTask_withBlankTitle_returnsError() {
        Map<String, Object> result = service.createTask("  ", "desc", "story-1", "John");

        assertThat(result.get("error")).isEqualTo("Task title is required");
        verify(scrumTaskRepository, never()).save(any());
    }

    @Test
    void createTask_withNullStoryId_returnsError() {
        Map<String, Object> result = service.createTask("Task", "desc", null, "John");

        assertThat(result.get("error")).isEqualTo("Story ID is required");
        verify(scrumTaskRepository, never()).save(any());
    }

    @Test
    void createTask_withBlankStoryId_returnsError() {
        Map<String, Object> result = service.createTask("Task", "desc", "  ", "John");

        assertThat(result.get("error")).isEqualTo("Story ID is required");
        verify(scrumTaskRepository, never()).save(any());
    }

    @Test
    void createTask_withNullAssignee_createsSuccessfully() {
        Map<String, Object> result = service.createTask("Task", "desc", "story-1", null);

        assertThat(result.get("assignee")).isNull();
        assertThat(result.get("status")).isEqualTo("todo");
        verify(scrumTaskRepository).save(any(ScrumTask.class));
    }

    @Test
    void createTask_trimsTitle() {
        Map<String, Object> result = service.createTask("  Task Title  ", "desc", "story-1", "John");

        assertThat(result.get("title")).isEqualTo("Task Title");
    }

    @Test
    void createTask_setsStatusToTodo() {
        service.createTask("Task", "desc", "story-1", "John");

        ArgumentCaptor<ScrumTask> captor = ArgumentCaptor.forClass(ScrumTask.class);
        verify(scrumTaskRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("todo");
    }

    @Test
    void createTask_setsCreatedAtAndUpdatedAt() {
        service.createTask("Task", "desc", "story-1", "John");

        ArgumentCaptor<ScrumTask> captor = ArgumentCaptor.forClass(ScrumTask.class);
        verify(scrumTaskRepository).save(captor.capture());
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
        assertThat(captor.getValue().getUpdatedAt()).isNotNull();
    }

    // ========== updateTaskStatus Tests ==========

    @Test
    void updateTaskStatus_withValidStatus_updatesSuccessfully() {
        ScrumTask existing = ScrumTask.builder()
                .id("task-1")
                .title("Task 1")
                .description("desc")
                .storyId("story-1")
                .assignee("John")
                .status("todo")
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();

        when(scrumTaskRepository.findById("task-1")).thenReturn(Optional.of(existing));

        Map<String, Object> result = service.updateTaskStatus("task-1", "in_progress");

        assertThat(result.get("previousStatus")).isEqualTo("todo");
        assertThat(result.get("status")).isEqualTo("in_progress");
        assertThat(result.get("title")).isEqualTo("Task 1");
        assertThat(result.get("message")).isEqualTo("Task 'Task 1' status changed from 'todo' to 'in_progress'");

        verify(scrumTaskRepository).save(any(ScrumTask.class));
    }

    @Test
    void updateTaskStatus_withAllValidStatuses_accepts() {
        for (String status : List.of("todo", "in_progress", "in_review", "done", "blocked")) {
            ScrumTask existing = ScrumTask.builder()
                    .id("task-1")
                    .title("Task 1")
                    .status("todo")
                    .storyId("story-1")
                    .createdAt(NOW)
                    .updatedAt(NOW)
                    .build();

            when(scrumTaskRepository.findById("task-1")).thenReturn(Optional.of(existing));

            Map<String, Object> result = service.updateTaskStatus("task-1", status);
            assertThat(result.get("status")).isEqualTo(status);
        }
    }

    @Test
    void updateTaskStatus_withInvalidStatus_returnsError() {
        Map<String, Object> result = service.updateTaskStatus("task-1", "invalid_status");

        assertThat((String) result.get("error")).contains("Invalid status");
        assertThat((String) result.get("error")).contains("invalid_status");
        verify(scrumTaskRepository, never()).save(any());
    }

    @Test
    void updateTaskStatus_withNullTaskId_returnsError() {
        Map<String, Object> result = service.updateTaskStatus(null, "done");

        assertThat(result.get("error")).isEqualTo("Task ID is required");
        verify(scrumTaskRepository, never()).save(any());
    }

    @Test
    void updateTaskStatus_withBlankTaskId_returnsError() {
        Map<String, Object> result = service.updateTaskStatus("  ", "done");

        assertThat(result.get("error")).isEqualTo("Task ID is required");
        verify(scrumTaskRepository, never()).save(any());
    }

    @Test
    void updateTaskStatus_withNullStatus_returnsError() {
        Map<String, Object> result = service.updateTaskStatus("task-1", null);

        assertThat(result.get("error")).isEqualTo("Status is required");
        verify(scrumTaskRepository, never()).save(any());
    }

    @Test
    void updateTaskStatus_withBlankStatus_returnsError() {
        Map<String, Object> result = service.updateTaskStatus("task-1", "  ");

        assertThat(result.get("error")).isEqualTo("Status is required");
        verify(scrumTaskRepository, never()).save(any());
    }

    @Test
    void updateTaskStatus_withTaskNotFound_returnsError() {
        when(scrumTaskRepository.findById("nonexistent")).thenReturn(Optional.empty());

        Map<String, Object> result = service.updateTaskStatus("nonexistent", "done");

        assertThat(result.get("error")).isEqualTo("Task not found with id: nonexistent");
        verify(scrumTaskRepository, never()).save(any());
    }

    @Test
    void updateTaskStatus_updatesUpdatedAtTimestamp() {
        ScrumTask existing = ScrumTask.builder()
                .id("task-1")
                .title("Task 1")
                .status("todo")
                .storyId("story-1")
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();

        when(scrumTaskRepository.findById("task-1")).thenReturn(Optional.of(existing));

        service.updateTaskStatus("task-1", "done");

        ArgumentCaptor<ScrumTask> captor = ArgumentCaptor.forClass(ScrumTask.class);
        verify(scrumTaskRepository).save(captor.capture());
        ScrumTask saved = captor.getValue();
        assertThat(saved.getUpdatedAt()).isAfterOrEqualTo(NOW);
        assertThat(saved.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void updateTaskStatus_preservesExistingFields() {
        ScrumTask existing = ScrumTask.builder()
                .id("task-1")
                .title("Task 1")
                .description("Original desc")
                .storyId("story-1")
                .assignee("John")
                .status("todo")
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();

        when(scrumTaskRepository.findById("task-1")).thenReturn(Optional.of(existing));

        service.updateTaskStatus("task-1", "in_review");

        ArgumentCaptor<ScrumTask> captor = ArgumentCaptor.forClass(ScrumTask.class);
        verify(scrumTaskRepository).save(captor.capture());
        ScrumTask saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("Task 1");
        assertThat(saved.getDescription()).isEqualTo("Original desc");
        assertThat(saved.getStoryId()).isEqualTo("story-1");
        assertThat(saved.getAssignee()).isEqualTo("John");
    }

    @Test
    void updateTaskStatus_normalizesStatusToLowercase() {
        ScrumTask existing = ScrumTask.builder()
                .id("task-1")
                .title("Task 1")
                .status("todo")
                .storyId("story-1")
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();

        when(scrumTaskRepository.findById("task-1")).thenReturn(Optional.of(existing));

        Map<String, Object> result = service.updateTaskStatus("task-1", "IN_PROGRESS");

        assertThat(result.get("status")).isEqualTo("in_progress");
    }

    // ========== getSprint Tests ==========

    @Test
    void getSprint_withValidId_returnsSprintWithStoriesAndTasks() {
        Sprint sprint = Sprint.builder()
                .id("sprint-1")
                .name("Sprint 1")
                .startDate("2026-02-12")
                .endDate("2026-02-26")
                .goalsJson("[\"Goal 1\"]")
                .createdAt(NOW)
                .build();

        UserStory story = UserStory.builder()
                .id("story-1")
                .title("Story 1")
                .description("desc")
                .storyPoints(5)
                .priority("high")
                .sprintId("sprint-1")
                .createdAt(NOW)
                .build();

        ScrumTask task = ScrumTask.builder()
                .id("task-1")
                .title("Task 1")
                .storyId("story-1")
                .status("todo")
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();

        when(sprintRepository.findById("sprint-1")).thenReturn(Optional.of(sprint));
        when(userStoryRepository.findBySprintId("sprint-1")).thenReturn(List.of(story));
        when(scrumTaskRepository.findByStoryId("story-1")).thenReturn(List.of(task));

        Map<String, Object> result = service.getSprint("sprint-1");

        assertThat(result.get("id")).isEqualTo("sprint-1");
        assertThat(result.get("name")).isEqualTo("Sprint 1");
        assertThat(result.get("storyCount")).isEqualTo(1);
        assertThat(result.get("totalPoints")).isEqualTo(5);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> stories = (List<Map<String, Object>>) result.get("stories");
        assertThat(stories).hasSize(1);
        assertThat(stories.get(0).get("title")).isEqualTo("Story 1");
        assertThat(stories.get(0).get("taskCount")).isEqualTo(1);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tasks = (List<Map<String, Object>>) stories.get(0).get("tasks");
        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).get("title")).isEqualTo("Task 1");
    }

    @Test
    void getSprint_withNullId_returnsError() {
        Map<String, Object> result = service.getSprint(null);

        assertThat(result.get("error")).isEqualTo("Sprint ID is required");
    }

    @Test
    void getSprint_withBlankId_returnsError() {
        Map<String, Object> result = service.getSprint("  ");

        assertThat(result.get("error")).isEqualTo("Sprint ID is required");
    }

    @Test
    void getSprint_withNonExistentId_returnsError() {
        when(sprintRepository.findById("nonexistent")).thenReturn(Optional.empty());

        Map<String, Object> result = service.getSprint("nonexistent");

        assertThat(result.get("error")).isEqualTo("Sprint not found with id: nonexistent");
    }

    @Test
    void getSprint_withMultipleStories_calculatesTotalPoints() {
        Sprint sprint = Sprint.builder()
                .id("sprint-1").name("Sprint 1").createdAt(NOW).build();

        UserStory story1 = UserStory.builder()
                .id("story-1").title("Story 1").storyPoints(3).priority("high")
                .sprintId("sprint-1").createdAt(NOW).build();
        UserStory story2 = UserStory.builder()
                .id("story-2").title("Story 2").storyPoints(8).priority("medium")
                .sprintId("sprint-1").createdAt(NOW).build();

        when(sprintRepository.findById("sprint-1")).thenReturn(Optional.of(sprint));
        when(userStoryRepository.findBySprintId("sprint-1")).thenReturn(List.of(story1, story2));
        when(scrumTaskRepository.findByStoryId(anyString())).thenReturn(List.of());

        Map<String, Object> result = service.getSprint("sprint-1");

        assertThat(result.get("totalPoints")).isEqualTo(11);
        assertThat(result.get("storyCount")).isEqualTo(2);
    }

    @Test
    void getSprint_withNoStories_returnsEmptyStoriesAndZeroPoints() {
        Sprint sprint = Sprint.builder()
                .id("sprint-1").name("Sprint 1").createdAt(NOW).build();

        when(sprintRepository.findById("sprint-1")).thenReturn(Optional.of(sprint));
        when(userStoryRepository.findBySprintId("sprint-1")).thenReturn(List.of());

        Map<String, Object> result = service.getSprint("sprint-1");

        assertThat(result.get("totalPoints")).isEqualTo(0);
        assertThat(result.get("storyCount")).isEqualTo(0);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> stories = (List<Map<String, Object>>) result.get("stories");
        assertThat(stories).isEmpty();
    }

    // ========== sprintBoard Tests ==========

    @Test
    void sprintBoard_organizesTasksByStatusColumns() {
        Sprint sprint = Sprint.builder()
                .id("sprint-1").name("Sprint 1").createdAt(NOW).build();

        UserStory story = UserStory.builder()
                .id("story-1").title("Story 1").storyPoints(5).priority("high")
                .sprintId("sprint-1").createdAt(NOW).build();

        ScrumTask todoTask = ScrumTask.builder()
                .id("task-1").title("Todo Task").storyId("story-1")
                .status("todo").createdAt(NOW).updatedAt(NOW).build();
        ScrumTask inProgressTask = ScrumTask.builder()
                .id("task-2").title("In Progress Task").storyId("story-1")
                .status("in_progress").createdAt(NOW).updatedAt(NOW).build();
        ScrumTask doneTask = ScrumTask.builder()
                .id("task-3").title("Done Task").storyId("story-1")
                .status("done").createdAt(NOW).updatedAt(NOW).build();

        when(sprintRepository.findById("sprint-1")).thenReturn(Optional.of(sprint));
        when(userStoryRepository.findBySprintId("sprint-1")).thenReturn(List.of(story));
        when(scrumTaskRepository.findByStoryId("story-1")).thenReturn(List.of(todoTask, inProgressTask, doneTask));

        Map<String, Object> result = service.sprintBoard("sprint-1");

        assertThat(result.get("sprintName")).isEqualTo("Sprint 1");
        assertThat(result.get("totalTasks")).isEqualTo(3);

        @SuppressWarnings("unchecked")
        Map<String, List<Map<String, Object>>> columns = (Map<String, List<Map<String, Object>>>) result.get("columns");
        assertThat(columns.get("todo")).hasSize(1);
        assertThat(columns.get("in_progress")).hasSize(1);
        assertThat(columns.get("done")).hasSize(1);
        assertThat(columns.get("in_review")).isEmpty();
        assertThat(columns.get("blocked")).isEmpty();

        @SuppressWarnings("unchecked")
        Map<String, Object> summary = (Map<String, Object>) result.get("summary");
        assertThat(summary.get("todo")).isEqualTo(1);
        assertThat(summary.get("in_progress")).isEqualTo(1);
        assertThat(summary.get("done")).isEqualTo(1);
        assertThat(summary.get("in_review")).isEqualTo(0);
        assertThat(summary.get("blocked")).isEqualTo(0);
    }

    @Test
    void sprintBoard_withNullId_returnsError() {
        Map<String, Object> result = service.sprintBoard(null);

        assertThat(result.get("error")).isEqualTo("Sprint ID is required");
    }

    @Test
    void sprintBoard_withBlankId_returnsError() {
        Map<String, Object> result = service.sprintBoard("  ");

        assertThat(result.get("error")).isEqualTo("Sprint ID is required");
    }

    @Test
    void sprintBoard_withNonExistentSprint_returnsError() {
        when(sprintRepository.findById("nonexistent")).thenReturn(Optional.empty());

        Map<String, Object> result = service.sprintBoard("nonexistent");

        assertThat(result.get("error")).isEqualTo("Sprint not found with id: nonexistent");
    }

    @Test
    void sprintBoard_includesStoryTitleAndPriorityInTasks() {
        Sprint sprint = Sprint.builder()
                .id("sprint-1").name("Sprint 1").createdAt(NOW).build();

        UserStory story = UserStory.builder()
                .id("story-1").title("Important Story").storyPoints(5).priority("critical")
                .sprintId("sprint-1").createdAt(NOW).build();

        ScrumTask task = ScrumTask.builder()
                .id("task-1").title("Task 1").storyId("story-1")
                .status("todo").createdAt(NOW).updatedAt(NOW).build();

        when(sprintRepository.findById("sprint-1")).thenReturn(Optional.of(sprint));
        when(userStoryRepository.findBySprintId("sprint-1")).thenReturn(List.of(story));
        when(scrumTaskRepository.findByStoryId("story-1")).thenReturn(List.of(task));

        Map<String, Object> result = service.sprintBoard("sprint-1");

        @SuppressWarnings("unchecked")
        Map<String, List<Map<String, Object>>> columns = (Map<String, List<Map<String, Object>>>) result.get("columns");
        Map<String, Object> taskMap = columns.get("todo").get(0);
        assertThat(taskMap.get("storyTitle")).isEqualTo("Important Story");
        assertThat(taskMap.get("storyPriority")).isEqualTo("critical");
    }

    @Test
    void sprintBoard_withNoTasks_returnsEmptyColumns() {
        Sprint sprint = Sprint.builder()
                .id("sprint-1").name("Sprint 1").createdAt(NOW).build();

        when(sprintRepository.findById("sprint-1")).thenReturn(Optional.of(sprint));
        when(userStoryRepository.findBySprintId("sprint-1")).thenReturn(List.of());

        Map<String, Object> result = service.sprintBoard("sprint-1");

        assertThat(result.get("totalTasks")).isEqualTo(0);

        @SuppressWarnings("unchecked")
        Map<String, List<Map<String, Object>>> columns = (Map<String, List<Map<String, Object>>>) result.get("columns");
        assertThat(columns).containsKeys("todo", "in_progress", "in_review", "done", "blocked");
        for (List<Map<String, Object>> column : columns.values()) {
            assertThat(column).isEmpty();
        }
    }

    @Test
    void sprintBoard_withUnknownStatus_fallsBackToTodo() {
        Sprint sprint = Sprint.builder()
                .id("sprint-1").name("Sprint 1").createdAt(NOW).build();

        UserStory story = UserStory.builder()
                .id("story-1").title("Story 1").storyPoints(5).priority("high")
                .sprintId("sprint-1").createdAt(NOW).build();

        ScrumTask taskWithBadStatus = ScrumTask.builder()
                .id("task-1").title("Task 1").storyId("story-1")
                .status("unknown_status").createdAt(NOW).updatedAt(NOW).build();

        when(sprintRepository.findById("sprint-1")).thenReturn(Optional.of(sprint));
        when(userStoryRepository.findBySprintId("sprint-1")).thenReturn(List.of(story));
        when(scrumTaskRepository.findByStoryId("story-1")).thenReturn(List.of(taskWithBadStatus));

        Map<String, Object> result = service.sprintBoard("sprint-1");

        @SuppressWarnings("unchecked")
        Map<String, List<Map<String, Object>>> columns = (Map<String, List<Map<String, Object>>>) result.get("columns");
        assertThat(columns.get("todo")).hasSize(1);
    }

    // ========== getBacklog Tests ==========

    @Test
    void getBacklog_returnsUnassignedStories() {
        UserStory story1 = UserStory.builder()
                .id("story-1").title("Backlog Story 1").description("desc1")
                .storyPoints(3).priority("medium").createdAt(NOW).build();
        UserStory story2 = UserStory.builder()
                .id("story-2").title("Backlog Story 2").description("desc2")
                .storyPoints(5).priority("high").createdAt(NOW).build();

        when(userStoryRepository.findUnassigned()).thenReturn(List.of(story1, story2));
        when(scrumTaskRepository.findByStoryId(anyString())).thenReturn(List.of());

        Map<String, Object> result = service.getBacklog();

        assertThat(result.get("storyCount")).isEqualTo(2);
        assertThat(result.get("totalPoints")).isEqualTo(8);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> stories = (List<Map<String, Object>>) result.get("stories");
        assertThat(stories).hasSize(2);
        assertThat(stories.get(0).get("title")).isEqualTo("Backlog Story 1");
        assertThat(stories.get(1).get("title")).isEqualTo("Backlog Story 2");
    }

    @Test
    void getBacklog_withNoStories_returnsEmptyResult() {
        when(userStoryRepository.findUnassigned()).thenReturn(List.of());

        Map<String, Object> result = service.getBacklog();

        assertThat(result.get("storyCount")).isEqualTo(0);
        assertThat(result.get("totalPoints")).isEqualTo(0);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> stories = (List<Map<String, Object>>) result.get("stories");
        assertThat(stories).isEmpty();
    }

    @Test
    void getBacklog_includesTasksForEachStory() {
        UserStory story = UserStory.builder()
                .id("story-1").title("Story 1").storyPoints(5).priority("high")
                .createdAt(NOW).build();

        ScrumTask task1 = ScrumTask.builder()
                .id("task-1").title("Task 1").storyId("story-1")
                .status("todo").createdAt(NOW).updatedAt(NOW).build();
        ScrumTask task2 = ScrumTask.builder()
                .id("task-2").title("Task 2").storyId("story-1")
                .status("in_progress").createdAt(NOW).updatedAt(NOW).build();

        when(userStoryRepository.findUnassigned()).thenReturn(List.of(story));
        when(scrumTaskRepository.findByStoryId("story-1")).thenReturn(List.of(task1, task2));

        Map<String, Object> result = service.getBacklog();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> stories = (List<Map<String, Object>>) result.get("stories");
        assertThat(stories.get(0).get("taskCount")).isEqualTo(2);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tasks = (List<Map<String, Object>>) stories.get(0).get("tasks");
        assertThat(tasks).hasSize(2);
    }

    @Test
    void getBacklog_calculatesTotalPointsCorrectly() {
        UserStory story1 = UserStory.builder()
                .id("story-1").title("Story 1").storyPoints(3).priority("low").createdAt(NOW).build();
        UserStory story2 = UserStory.builder()
                .id("story-2").title("Story 2").storyPoints(5).priority("medium").createdAt(NOW).build();
        UserStory story3 = UserStory.builder()
                .id("story-3").title("Story 3").storyPoints(13).priority("high").createdAt(NOW).build();

        when(userStoryRepository.findUnassigned()).thenReturn(List.of(story1, story2, story3));
        when(scrumTaskRepository.findByStoryId(anyString())).thenReturn(List.of());

        Map<String, Object> result = service.getBacklog();

        assertThat(result.get("totalPoints")).isEqualTo(21);
    }
}
