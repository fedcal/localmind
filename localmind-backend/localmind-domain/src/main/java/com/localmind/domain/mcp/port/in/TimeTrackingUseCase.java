package com.localmind.domain.mcp.port.in;

import java.util.Map;

/**
 * Input port for the time-tracking tool group.
 * Provides timer management, manual time logging, timesheet retrieval,
 * anomaly detection and estimate vs actual comparison.
 */
public interface TimeTrackingUseCase {

    /**
     * Starts a timer for the given task.
     * Creates a TimeEntry with timerStartedAt = now and durationMinutes = 0.
     *
     * @param taskId      the task identifier
     * @param description optional description of the work being done
     * @return result with id, taskId, timerStartedAt and status
     */
    Map<String, Object> startTimer(String taskId, String description);

    /**
     * Stops the currently active timer.
     * Finds the active timer, calculates duration from timerStartedAt to now,
     * and clears timerStartedAt.
     *
     * @return result with id, taskId, durationMinutes, durationFormatted, stoppedAt
     */
    Map<String, Object> stopTimer();

    /**
     * Logs time manually without using the timer.
     *
     * @param taskId          the task identifier
     * @param durationMinutes the duration in minutes
     * @param description     optional description
     * @param date            the entry date (YYYY-MM-DD format)
     * @return result with id, taskId, durationMinutes, date, saved confirmation
     */
    Map<String, Object> logTime(String taskId, int durationMinutes, String description, String date);

    /**
     * Retrieves a timesheet for a date range and optional user.
     *
     * @param startDate start date (YYYY-MM-DD)
     * @param endDate   end date (YYYY-MM-DD)
     * @param userId    optional user filter
     * @return result with entries, totalMinutes, totalFormatted (HH:mm)
     */
    Map<String, Object> getTimesheet(String startDate, String endDate, String userId);

    /**
     * Detects anomalies in time entries for a given user over the last N days.
     * Flags excessive hours (>10h/day) and weekend work.
     *
     * @param userId the user identifier
     * @param days   number of days to analyze
     * @return result with anomalies list, summary statistics
     */
    Map<String, Object> detectAnomalies(String userId, int days);

    /**
     * Compares estimated time vs actual time for a task.
     * If estimateMinutes is provided and > 0, saves a new estimate.
     * Then calculates variance between estimate and actual logged time.
     *
     * @param taskId          the task identifier
     * @param estimateMinutes the estimated duration in minutes (0 to skip saving)
     * @param description     optional description for the estimate
     * @return result with taskId, estimateMinutes, actualMinutes, varianceMinutes, variancePercent
     */
    Map<String, Object> estimateVsActual(String taskId, int estimateMinutes, String description);
}
