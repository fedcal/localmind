package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.TimeEntry;
import com.localmind.domain.mcp.model.TimeEstimate;
import com.localmind.domain.mcp.port.in.TimeTrackingUseCase;
import com.localmind.domain.mcp.port.out.TimeTrackingRepository;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Domain service implementing time tracking tools.
 * Pure Java, zero Spring dependencies.
 */
public class TimeTrackingService implements TimeTrackingUseCase {

    private final TimeTrackingRepository repository;

    public TimeTrackingService(TimeTrackingRepository repository) {
        this.repository = repository;
    }

    // ========================================================================
    // 1. startTimer
    // ========================================================================

    @Override
    public Map<String, Object> startTimer(String taskId, String description) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (taskId == null || taskId.isBlank()) {
            result.put("error", "taskId is required");
            return result;
        }

        // Check if there's already an active timer
        Optional<TimeEntry> activeTimer = repository.findActiveTimer();
        if (activeTimer.isPresent()) {
            result.put("error", "A timer is already running");
            result.put("activeTaskId", activeTimer.get().getTaskId());
            result.put("activeTimerId", activeTimer.get().getId());
            return result;
        }

        Instant now = Instant.now();
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        TimeEntry entry = TimeEntry.builder()
                .id(UUID.randomUUID().toString())
                .taskId(taskId)
                .userId("default")
                .durationMinutes(0)
                .description(description)
                .entryDate(today)
                .timerStartedAt(now)
                .createdAt(now)
                .build();

        TimeEntry saved = repository.saveEntry(entry);

        result.put("id", saved.getId());
        result.put("taskId", saved.getTaskId());
        result.put("timerStartedAt", saved.getTimerStartedAt().toString());
        result.put("status", "running");
        result.put("message", "Timer started for task " + taskId);
        return result;
    }

    // ========================================================================
    // 2. stopTimer
    // ========================================================================

    @Override
    public Map<String, Object> stopTimer() {
        Map<String, Object> result = new LinkedHashMap<>();

        Optional<TimeEntry> activeTimer = repository.findActiveTimer();
        if (activeTimer.isEmpty()) {
            result.put("error", "No active timer found");
            return result;
        }

        TimeEntry active = activeTimer.get();
        Instant now = Instant.now();
        long durationMinutes = Duration.between(active.getTimerStartedAt(), now).toMinutes();

        // Create updated entry with calculated duration and cleared timerStartedAt
        TimeEntry stopped = TimeEntry.builder()
                .id(active.getId())
                .taskId(active.getTaskId())
                .userId(active.getUserId())
                .durationMinutes((int) durationMinutes)
                .description(active.getDescription())
                .entryDate(active.getEntryDate())
                .timerStartedAt(null)
                .createdAt(active.getCreatedAt())
                .build();

        repository.saveEntry(stopped);

        result.put("id", stopped.getId());
        result.put("taskId", stopped.getTaskId());
        result.put("durationMinutes", (int) durationMinutes);
        result.put("durationFormatted", formatDuration((int) durationMinutes));
        result.put("stoppedAt", now.toString());
        result.put("status", "stopped");
        result.put("message", "Timer stopped for task " + stopped.getTaskId());
        return result;
    }

    // ========================================================================
    // 3. logTime
    // ========================================================================

    @Override
    public Map<String, Object> logTime(String taskId, int durationMinutes, String description, String date) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (taskId == null || taskId.isBlank()) {
            result.put("error", "taskId is required");
            return result;
        }

        if (durationMinutes <= 0) {
            result.put("error", "durationMinutes must be greater than 0");
            return result;
        }

        String entryDate = (date != null && !date.isBlank()) ? date
                : LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        Instant now = Instant.now();

        TimeEntry entry = TimeEntry.builder()
                .id(UUID.randomUUID().toString())
                .taskId(taskId)
                .userId("default")
                .durationMinutes(durationMinutes)
                .description(description)
                .entryDate(entryDate)
                .timerStartedAt(null)
                .createdAt(now)
                .build();

        TimeEntry saved = repository.saveEntry(entry);

        result.put("id", saved.getId());
        result.put("taskId", saved.getTaskId());
        result.put("durationMinutes", saved.getDurationMinutes());
        result.put("durationFormatted", formatDuration(saved.getDurationMinutes()));
        result.put("date", saved.getEntryDate());
        result.put("saved", true);
        result.put("message", "Logged " + durationMinutes + " minutes for task " + taskId);
        return result;
    }

    // ========================================================================
    // 4. getTimesheet
    // ========================================================================

    @Override
    public Map<String, Object> getTimesheet(String startDate, String endDate, String userId) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (startDate == null || startDate.isBlank() || endDate == null || endDate.isBlank()) {
            result.put("error", "startDate and endDate are required");
            return result;
        }

        List<TimeEntry> entries = repository.findEntriesByDateRange(startDate, endDate, userId);

        int totalMinutes = 0;
        List<Map<String, Object>> entryMaps = new ArrayList<>();
        for (TimeEntry entry : entries) {
            totalMinutes += entry.getDurationMinutes();
            entryMaps.add(entryToMap(entry));
        }

        result.put("startDate", startDate);
        result.put("endDate", endDate);
        if (userId != null && !userId.isBlank()) {
            result.put("userId", userId);
        }
        result.put("entryCount", entries.size());
        result.put("totalMinutes", totalMinutes);
        result.put("totalFormatted", formatDuration(totalMinutes));
        result.put("entries", entryMaps);
        return result;
    }

    // ========================================================================
    // 5. detectAnomalies
    // ========================================================================

    @Override
    public Map<String, Object> detectAnomalies(String userId, int days) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (userId == null || userId.isBlank()) {
            result.put("error", "userId is required");
            return result;
        }

        if (days <= 0) {
            result.put("error", "days must be greater than 0");
            return result;
        }

        List<TimeEntry> allEntries = repository.findEntriesByUserId(userId);

        // Calculate the cutoff date
        LocalDate cutoffDate = LocalDate.now().minusDays(days);
        String cutoffStr = cutoffDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

        // Filter entries within the date range
        List<TimeEntry> entries = allEntries.stream()
                .filter(e -> e.getEntryDate() != null && e.getEntryDate().compareTo(cutoffStr) >= 0)
                .collect(Collectors.toList());

        // Group entries by date
        Map<String, List<TimeEntry>> byDate = new LinkedHashMap<>();
        for (TimeEntry entry : entries) {
            String date = entry.getEntryDate();
            if (date != null) {
                byDate.computeIfAbsent(date, k -> new ArrayList<>()).add(entry);
            }
        }

        List<Map<String, Object>> anomalies = new ArrayList<>();
        int totalDays = byDate.size();
        int totalMinutes = 0;
        int excessiveDays = 0;
        int weekendDays = 0;

        for (Map.Entry<String, List<TimeEntry>> dateEntry : byDate.entrySet()) {
            String date = dateEntry.getKey();
            List<TimeEntry> dayEntries = dateEntry.getValue();

            int dayMinutes = dayEntries.stream()
                    .mapToInt(TimeEntry::getDurationMinutes)
                    .sum();
            totalMinutes += dayMinutes;

            // Check excessive hours (>10h = >600 minutes)
            if (dayMinutes > 600) {
                excessiveDays++;
                Map<String, Object> anomaly = new LinkedHashMap<>();
                anomaly.put("type", "excessive_hours");
                anomaly.put("date", date);
                anomaly.put("minutes", dayMinutes);
                anomaly.put("formatted", formatDuration(dayMinutes));
                anomaly.put("message", "Worked " + formatDuration(dayMinutes) + " on " + date + " (exceeds 10h limit)");
                anomalies.add(anomaly);
            }

            // Check weekend work
            try {
                LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
                DayOfWeek dow = localDate.getDayOfWeek();
                if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
                    weekendDays++;
                    Map<String, Object> anomaly = new LinkedHashMap<>();
                    anomaly.put("type", "weekend_work");
                    anomaly.put("date", date);
                    anomaly.put("dayOfWeek", dow.name());
                    anomaly.put("minutes", dayMinutes);
                    anomaly.put("formatted", formatDuration(dayMinutes));
                    anomaly.put("message", "Worked " + formatDuration(dayMinutes) + " on " + dow.name() + " " + date);
                    anomalies.add(anomaly);
                }
            } catch (Exception ignored) {
                // Skip unparseable dates
            }
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("analyzedDays", totalDays);
        summary.put("totalMinutes", totalMinutes);
        summary.put("totalFormatted", formatDuration(totalMinutes));
        summary.put("averageMinutesPerDay", totalDays > 0 ? totalMinutes / totalDays : 0);
        summary.put("excessiveDays", excessiveDays);
        summary.put("weekendDays", weekendDays);

        result.put("userId", userId);
        result.put("periodDays", days);
        result.put("anomalyCount", anomalies.size());
        result.put("anomalies", anomalies);
        result.put("summary", summary);
        return result;
    }

    // ========================================================================
    // 6. estimateVsActual
    // ========================================================================

    @Override
    public Map<String, Object> estimateVsActual(String taskId, int estimateMinutes, String description) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (taskId == null || taskId.isBlank()) {
            result.put("error", "taskId is required");
            return result;
        }

        // If estimateMinutes provided and > 0, save a new estimate
        if (estimateMinutes > 0) {
            TimeEstimate estimate = TimeEstimate.builder()
                    .id(UUID.randomUUID().toString())
                    .taskId(taskId)
                    .estimateMinutes(estimateMinutes)
                    .description(description)
                    .createdAt(Instant.now())
                    .build();
            repository.saveEstimate(estimate);
        }

        // Get the estimate for this task
        Optional<TimeEstimate> estimateOpt = repository.findEstimateByTaskId(taskId);

        // Get actual entries for this task
        List<TimeEntry> entries = repository.findEntriesByTaskId(taskId);
        int actualMinutes = entries.stream()
                .mapToInt(TimeEntry::getDurationMinutes)
                .sum();

        result.put("taskId", taskId);
        result.put("actualMinutes", actualMinutes);
        result.put("actualFormatted", formatDuration(actualMinutes));
        result.put("entriesCount", entries.size());

        if (estimateOpt.isPresent()) {
            TimeEstimate estimate = estimateOpt.get();
            int estMinutes = estimate.getEstimateMinutes();
            int varianceMinutes = actualMinutes - estMinutes;
            double variancePercent = estMinutes > 0
                    ? Math.round((double) varianceMinutes / estMinutes * 100 * 100.0) / 100.0
                    : 0.0;

            result.put("estimateMinutes", estMinutes);
            result.put("estimateFormatted", formatDuration(estMinutes));
            result.put("varianceMinutes", varianceMinutes);
            result.put("varianceFormatted", formatDuration(Math.abs(varianceMinutes)));
            result.put("variancePercent", variancePercent);
            result.put("status", varianceMinutes > 0 ? "over_estimate" : varianceMinutes < 0 ? "under_estimate" : "on_target");
        } else {
            result.put("estimateMinutes", 0);
            result.put("message", "No estimate found for task " + taskId);
            result.put("status", "no_estimate");
        }

        return result;
    }

    // ========================================================================
    // Helper methods
    // ========================================================================

    private Map<String, Object> entryToMap(TimeEntry entry) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entry.getId());
        map.put("taskId", entry.getTaskId());
        map.put("userId", entry.getUserId());
        map.put("durationMinutes", entry.getDurationMinutes());
        map.put("durationFormatted", formatDuration(entry.getDurationMinutes()));
        map.put("description", entry.getDescription());
        map.put("entryDate", entry.getEntryDate());
        map.put("createdAt", entry.getCreatedAt() != null ? entry.getCreatedAt().toString() : null);
        return map;
    }

    /**
     * Formats a duration in minutes to HH:mm format.
     */
    String formatDuration(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;
        return String.format("%d:%02d", hours, mins);
    }
}
