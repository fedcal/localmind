package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.ScrumTask;
import com.localmind.domain.mcp.model.Sprint;
import com.localmind.domain.mcp.model.UserStory;
import com.localmind.domain.mcp.port.in.ScrumBoardUseCase;
import com.localmind.domain.mcp.port.out.ScrumTaskRepository;
import com.localmind.domain.mcp.port.out.SprintRepository;
import com.localmind.domain.mcp.port.out.UserStoryRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Domain service implementing the scrum-board tool group.
 * Provides sprint management, user story creation, task tracking,
 * sprint board visualization, and backlog management.
 * No Spring annotations - registered as a bean via DomainConfig.
 */
public class ScrumBoardService implements ScrumBoardUseCase {

    private static final Set<String> VALID_STATUSES = Set.of(
            "todo", "in_progress", "in_review", "done", "blocked"
    );

    private static final Set<String> VALID_PRIORITIES = Set.of(
            "low", "medium", "high", "critical"
    );

    private final SprintRepository sprintRepository;
    private final UserStoryRepository userStoryRepository;
    private final ScrumTaskRepository scrumTaskRepository;

    public ScrumBoardService(SprintRepository sprintRepository,
                             UserStoryRepository userStoryRepository,
                             ScrumTaskRepository scrumTaskRepository) {
        this.sprintRepository = sprintRepository;
        this.userStoryRepository = userStoryRepository;
        this.scrumTaskRepository = scrumTaskRepository;
    }

    // ========== 1. createSprint ==========

    @Override
    public Map<String, Object> createSprint(String name, String startDate, String endDate, List<String> goals) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (name == null || name.isBlank()) {
            result.put("error", "Sprint name is required");
            return result;
        }

        String goalsJson = goals != null && !goals.isEmpty() ? listToJsonArray(goals) : "[]";

        Sprint sprint = Sprint.builder()
                .id(UUID.randomUUID().toString())
                .name(name.trim())
                .startDate(startDate)
                .endDate(endDate)
                .goalsJson(goalsJson)
                .createdAt(Instant.now())
                .build();

        Sprint saved = sprintRepository.save(sprint);

        result.put("id", saved.getId());
        result.put("name", saved.getName());
        result.put("startDate", saved.getStartDate());
        result.put("endDate", saved.getEndDate());
        result.put("goals", goals != null ? goals : List.of());
        result.put("createdAt", saved.getCreatedAt().toString());
        result.put("message", "Sprint '" + saved.getName() + "' created successfully");

        return result;
    }

    // ========== 2. createStory ==========

    @Override
    public Map<String, Object> createStory(String title, String description, List<String> acceptanceCriteria,
                                           int storyPoints, String priority, String sprintId) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (title == null || title.isBlank()) {
            result.put("error", "Story title is required");
            return result;
        }

        String effectivePriority = priority != null && VALID_PRIORITIES.contains(priority.toLowerCase())
                ? priority.toLowerCase() : "medium";

        String acJson = acceptanceCriteria != null && !acceptanceCriteria.isEmpty()
                ? listToJsonArray(acceptanceCriteria) : "[]";

        UserStory story = UserStory.builder()
                .id(UUID.randomUUID().toString())
                .title(title.trim())
                .description(description)
                .acceptanceCriteriaJson(acJson)
                .storyPoints(Math.max(storyPoints, 0))
                .priority(effectivePriority)
                .sprintId(sprintId)
                .createdAt(Instant.now())
                .build();

        UserStory saved = userStoryRepository.save(story);

        result.put("id", saved.getId());
        result.put("title", saved.getTitle());
        result.put("description", saved.getDescription());
        result.put("acceptanceCriteria", acceptanceCriteria != null ? acceptanceCriteria : List.of());
        result.put("storyPoints", saved.getStoryPoints());
        result.put("priority", saved.getPriority());
        result.put("sprintId", saved.getSprintId());
        result.put("createdAt", saved.getCreatedAt().toString());
        result.put("message", "User story '" + saved.getTitle() + "' created successfully");

        return result;
    }

    // ========== 3. createTask ==========

    @Override
    public Map<String, Object> createTask(String title, String description, String storyId, String assignee) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (title == null || title.isBlank()) {
            result.put("error", "Task title is required");
            return result;
        }

        if (storyId == null || storyId.isBlank()) {
            result.put("error", "Story ID is required");
            return result;
        }

        ScrumTask task = ScrumTask.builder()
                .id(UUID.randomUUID().toString())
                .title(title.trim())
                .description(description)
                .storyId(storyId)
                .assignee(assignee)
                .status("todo")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        ScrumTask saved = scrumTaskRepository.save(task);

        result.put("id", saved.getId());
        result.put("title", saved.getTitle());
        result.put("description", saved.getDescription());
        result.put("storyId", saved.getStoryId());
        result.put("assignee", saved.getAssignee());
        result.put("status", saved.getStatus());
        result.put("createdAt", saved.getCreatedAt().toString());
        result.put("message", "Task '" + saved.getTitle() + "' created successfully");

        return result;
    }

    // ========== 4. updateTaskStatus ==========

    @Override
    public Map<String, Object> updateTaskStatus(String taskId, String status) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (taskId == null || taskId.isBlank()) {
            result.put("error", "Task ID is required");
            return result;
        }

        if (status == null || status.isBlank()) {
            result.put("error", "Status is required");
            return result;
        }

        String normalizedStatus = status.toLowerCase().trim();
        if (!VALID_STATUSES.contains(normalizedStatus)) {
            result.put("error", "Invalid status: '" + status + "'. Valid statuses: todo, in_progress, in_review, done, blocked");
            return result;
        }

        Optional<ScrumTask> optionalTask = scrumTaskRepository.findById(taskId);
        if (optionalTask.isEmpty()) {
            result.put("error", "Task not found with id: " + taskId);
            return result;
        }

        ScrumTask existingTask = optionalTask.get();
        String previousStatus = existingTask.getStatus();

        ScrumTask updatedTask = ScrumTask.builder()
                .id(existingTask.getId())
                .title(existingTask.getTitle())
                .description(existingTask.getDescription())
                .storyId(existingTask.getStoryId())
                .assignee(existingTask.getAssignee())
                .status(normalizedStatus)
                .createdAt(existingTask.getCreatedAt())
                .updatedAt(Instant.now())
                .build();

        ScrumTask saved = scrumTaskRepository.save(updatedTask);

        result.put("id", saved.getId());
        result.put("title", saved.getTitle());
        result.put("previousStatus", previousStatus);
        result.put("status", saved.getStatus());
        result.put("updatedAt", saved.getUpdatedAt().toString());
        result.put("message", "Task '" + saved.getTitle() + "' status changed from '" + previousStatus + "' to '" + saved.getStatus() + "'");

        return result;
    }

    // ========== 5. getSprint ==========

    @Override
    public Map<String, Object> getSprint(String sprintId) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (sprintId == null || sprintId.isBlank()) {
            result.put("error", "Sprint ID is required");
            return result;
        }

        Optional<Sprint> optionalSprint = sprintRepository.findById(sprintId);
        if (optionalSprint.isEmpty()) {
            result.put("error", "Sprint not found with id: " + sprintId);
            return result;
        }

        Sprint sprint = optionalSprint.get();

        result.put("id", sprint.getId());
        result.put("name", sprint.getName());
        result.put("startDate", sprint.getStartDate());
        result.put("endDate", sprint.getEndDate());
        result.put("goalsJson", sprint.getGoalsJson());
        result.put("createdAt", sprint.getCreatedAt() != null ? sprint.getCreatedAt().toString() : null);

        // Load stories for this sprint
        List<UserStory> stories = userStoryRepository.findBySprintId(sprintId);
        List<Map<String, Object>> storyMaps = new ArrayList<>();
        int totalPoints = 0;

        for (UserStory story : stories) {
            Map<String, Object> storyMap = new LinkedHashMap<>();
            storyMap.put("id", story.getId());
            storyMap.put("title", story.getTitle());
            storyMap.put("description", story.getDescription());
            storyMap.put("storyPoints", story.getStoryPoints());
            storyMap.put("priority", story.getPriority());

            totalPoints += story.getStoryPoints();

            // Load tasks for this story
            List<ScrumTask> tasks = scrumTaskRepository.findByStoryId(story.getId());
            List<Map<String, Object>> taskMaps = new ArrayList<>();
            for (ScrumTask task : tasks) {
                taskMaps.add(taskToMap(task));
            }
            storyMap.put("tasks", taskMaps);
            storyMap.put("taskCount", taskMaps.size());

            storyMaps.add(storyMap);
        }

        result.put("stories", storyMaps);
        result.put("storyCount", storyMaps.size());
        result.put("totalPoints", totalPoints);

        return result;
    }

    // ========== 6. sprintBoard ==========

    @Override
    public Map<String, Object> sprintBoard(String sprintId) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (sprintId == null || sprintId.isBlank()) {
            result.put("error", "Sprint ID is required");
            return result;
        }

        Optional<Sprint> optionalSprint = sprintRepository.findById(sprintId);
        if (optionalSprint.isEmpty()) {
            result.put("error", "Sprint not found with id: " + sprintId);
            return result;
        }

        Sprint sprint = optionalSprint.get();

        result.put("sprintId", sprint.getId());
        result.put("sprintName", sprint.getName());

        // Initialize columns
        Map<String, List<Map<String, Object>>> columns = new LinkedHashMap<>();
        columns.put("todo", new ArrayList<>());
        columns.put("in_progress", new ArrayList<>());
        columns.put("in_review", new ArrayList<>());
        columns.put("done", new ArrayList<>());
        columns.put("blocked", new ArrayList<>());

        // Load stories and tasks
        List<UserStory> stories = userStoryRepository.findBySprintId(sprintId);
        int totalTasks = 0;

        for (UserStory story : stories) {
            List<ScrumTask> tasks = scrumTaskRepository.findByStoryId(story.getId());
            for (ScrumTask task : tasks) {
                Map<String, Object> taskMap = taskToMap(task);
                taskMap.put("storyTitle", story.getTitle());
                taskMap.put("storyPriority", story.getPriority());

                String taskStatus = task.getStatus() != null ? task.getStatus() : "todo";
                if (columns.containsKey(taskStatus)) {
                    columns.get(taskStatus).add(taskMap);
                } else {
                    columns.get("todo").add(taskMap);
                }
                totalTasks++;
            }
        }

        result.put("columns", columns);
        result.put("totalTasks", totalTasks);

        // Summary counts
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("todo", columns.get("todo").size());
        summary.put("in_progress", columns.get("in_progress").size());
        summary.put("in_review", columns.get("in_review").size());
        summary.put("done", columns.get("done").size());
        summary.put("blocked", columns.get("blocked").size());
        result.put("summary", summary);

        return result;
    }

    // ========== 7. getBacklog ==========

    @Override
    public Map<String, Object> getBacklog() {
        Map<String, Object> result = new LinkedHashMap<>();

        List<UserStory> unassignedStories = userStoryRepository.findUnassigned();
        List<Map<String, Object>> storyMaps = new ArrayList<>();
        int totalPoints = 0;

        for (UserStory story : unassignedStories) {
            Map<String, Object> storyMap = new LinkedHashMap<>();
            storyMap.put("id", story.getId());
            storyMap.put("title", story.getTitle());
            storyMap.put("description", story.getDescription());
            storyMap.put("storyPoints", story.getStoryPoints());
            storyMap.put("priority", story.getPriority());
            storyMap.put("acceptanceCriteriaJson", story.getAcceptanceCriteriaJson());
            storyMap.put("createdAt", story.getCreatedAt() != null ? story.getCreatedAt().toString() : null);

            totalPoints += story.getStoryPoints();

            // Load tasks for this story
            List<ScrumTask> tasks = scrumTaskRepository.findByStoryId(story.getId());
            List<Map<String, Object>> taskMaps = new ArrayList<>();
            for (ScrumTask task : tasks) {
                taskMaps.add(taskToMap(task));
            }
            storyMap.put("tasks", taskMaps);
            storyMap.put("taskCount", taskMaps.size());

            storyMaps.add(storyMap);
        }

        result.put("stories", storyMaps);
        result.put("storyCount", storyMaps.size());
        result.put("totalPoints", totalPoints);

        return result;
    }

    // ========== Helper Methods ==========

    private Map<String, Object> taskToMap(ScrumTask task) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", task.getId());
        map.put("title", task.getTitle());
        map.put("description", task.getDescription());
        map.put("storyId", task.getStoryId());
        map.put("assignee", task.getAssignee());
        map.put("status", task.getStatus());
        map.put("createdAt", task.getCreatedAt() != null ? task.getCreatedAt().toString() : null);
        map.put("updatedAt", task.getUpdatedAt() != null ? task.getUpdatedAt().toString() : null);
        return map;
    }

    private String listToJsonArray(List<String> items) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escapeJson(items.get(i))).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    // ========== JSON Serialization (minimal, no external deps) ==========

    private String mapToJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(escapeJson(entry.getKey())).append("\":");
            sb.append(valueToJson(entry.getValue()));
        }
        sb.append("}");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String valueToJson(Object value) {
        if (value == null) return "null";
        if (value instanceof String) return "\"" + escapeJson((String) value) + "\"";
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        if (value instanceof Map) return mapToJson((Map<String, Object>) value);
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(valueToJson(list.get(i)));
            }
            sb.append("]");
            return sb.toString();
        }
        return "\"" + escapeJson(value.toString()) + "\"";
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
