package com.localmind.domain.mcp.port.out;

import com.localmind.domain.mcp.model.TimeEntry;
import com.localmind.domain.mcp.model.TimeEstimate;

import java.util.List;
import java.util.Optional;

/**
 * Output port for persisting time tracking data.
 */
public interface TimeTrackingRepository {

    /**
     * Saves a time entry.
     *
     * @param entry the time entry to persist
     * @return the saved time entry (with generated id if new)
     */
    TimeEntry saveEntry(TimeEntry entry);

    /**
     * Saves a time estimate.
     *
     * @param estimate the time estimate to persist
     * @return the saved time estimate (with generated id if new)
     */
    TimeEstimate saveEstimate(TimeEstimate estimate);

    /**
     * Finds the currently active timer (entry with non-null timerStartedAt).
     *
     * @return optional containing the active timer entry if found
     */
    Optional<TimeEntry> findActiveTimer();

    /**
     * Finds all time entries for a given user.
     *
     * @param userId the user identifier
     * @return list of time entries for the user
     */
    List<TimeEntry> findEntriesByUserId(String userId);

    /**
     * Finds time entries within a date range for an optional user.
     *
     * @param startDate start date (YYYY-MM-DD)
     * @param endDate   end date (YYYY-MM-DD)
     * @param userId    optional user filter (may be null or empty)
     * @return list of time entries matching the criteria
     */
    List<TimeEntry> findEntriesByDateRange(String startDate, String endDate, String userId);

    /**
     * Finds the time estimate for a given task.
     *
     * @param taskId the task identifier
     * @return optional containing the estimate if found
     */
    Optional<TimeEstimate> findEstimateByTaskId(String taskId);

    /**
     * Finds all time entries for a given task.
     *
     * @param taskId the task identifier
     * @return list of time entries for the task
     */
    List<TimeEntry> findEntriesByTaskId(String taskId);
}
