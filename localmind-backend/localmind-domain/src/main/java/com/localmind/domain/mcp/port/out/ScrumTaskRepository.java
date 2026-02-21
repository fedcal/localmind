package com.localmind.domain.mcp.port.out;

import com.localmind.domain.mcp.model.ScrumTask;

import java.util.List;
import java.util.Optional;

/**
 * Output port for persisting ScrumTask entities.
 */
public interface ScrumTaskRepository {

    /**
     * Saves a scrum task.
     *
     * @param task the task to persist
     * @return the saved task
     */
    ScrumTask save(ScrumTask task);

    /**
     * Finds all tasks belonging to a user story.
     *
     * @param storyId the user story id
     * @return list of tasks for the story
     */
    List<ScrumTask> findByStoryId(String storyId);

    /**
     * Finds a task by its id.
     *
     * @param id the task id
     * @return optional containing the task if found
     */
    Optional<ScrumTask> findById(String id);
}
