package com.localmind.domain.mcp.port.in;

import java.util.List;
import java.util.Map;

/**
 * Input port for the scrum-board tool group.
 * Provides sprint management, user story creation, task tracking,
 * sprint board visualization, and backlog management.
 */
public interface ScrumBoardUseCase {

    /**
     * Creates a new sprint with name, dates, and goals.
     *
     * @param name      the sprint name
     * @param startDate the start date (ISO format string)
     * @param endDate   the end date (ISO format string)
     * @param goals     list of sprint goals
     * @return result map with created sprint data
     */
    Map<String, Object> createSprint(String name, String startDate, String endDate, List<String> goals);

    /**
     * Creates a new user story with title, description, acceptance criteria, points, and priority.
     *
     * @param title              the story title
     * @param description        the story description
     * @param acceptanceCriteria list of acceptance criteria
     * @param storyPoints        estimated story points
     * @param priority           priority level (low, medium, high, critical)
     * @param sprintId           optional sprint id to assign the story to
     * @return result map with created story data
     */
    Map<String, Object> createStory(String title, String description, List<String> acceptanceCriteria,
                                    int storyPoints, String priority, String sprintId);

    /**
     * Creates a new task associated to a user story.
     *
     * @param title       the task title
     * @param description the task description
     * @param storyId     the user story id this task belongs to
     * @param assignee    optional assignee name
     * @return result map with created task data
     */
    Map<String, Object> createTask(String title, String description, String storyId, String assignee);

    /**
     * Updates the status of an existing task.
     *
     * @param taskId the task id
     * @param status the new status (todo, in_progress, in_review, done, blocked)
     * @return result map with updated task data
     */
    Map<String, Object> updateTaskStatus(String taskId, String status);

    /**
     * Retrieves a sprint with all its associated stories and tasks.
     *
     * @param sprintId the sprint id
     * @return result map with sprint data, stories, and tasks
     */
    Map<String, Object> getSprint(String sprintId);

    /**
     * Returns the sprint board organized by status columns.
     *
     * @param sprintId the sprint id
     * @return result map with tasks grouped by status columns
     */
    Map<String, Object> sprintBoard(String sprintId);

    /**
     * Returns all user stories not assigned to any sprint (backlog).
     *
     * @return result map with backlog stories
     */
    Map<String, Object> getBacklog();
}
