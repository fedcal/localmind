package com.localmind.domain.mcp.port.out;

import com.localmind.domain.mcp.model.UserStory;

import java.util.List;

/**
 * Output port for persisting UserStory entities.
 */
public interface UserStoryRepository {

    /**
     * Saves a user story.
     *
     * @param userStory the user story to persist
     * @return the saved user story
     */
    UserStory save(UserStory userStory);

    /**
     * Finds all user stories assigned to a specific sprint.
     *
     * @param sprintId the sprint id
     * @return list of user stories in the sprint
     */
    List<UserStory> findBySprintId(String sprintId);

    /**
     * Finds all user stories not assigned to any sprint (backlog).
     *
     * @return list of unassigned user stories
     */
    List<UserStory> findUnassigned();
}
