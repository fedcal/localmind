package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.TimeEntry;
import com.localmind.domain.mcp.model.TimeEstimate;
import com.localmind.domain.mcp.port.out.TimeTrackingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
class TimeTrackingServiceTest {

    @Mock
    private TimeTrackingRepository repository;

    private TimeTrackingService service;

    @BeforeEach
    void setUp() {
        when(repository.saveEntry(any(TimeEntry.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.saveEstimate(any(TimeEstimate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.findActiveTimer()).thenReturn(Optional.empty());
        when(repository.findEntriesByUserId(anyString())).thenReturn(List.of());
        when(repository.findEntriesByDateRange(anyString(), anyString(), any())).thenReturn(List.of());
        when(repository.findEntriesByTaskId(anyString())).thenReturn(List.of());
        when(repository.findEstimateByTaskId(anyString())).thenReturn(Optional.empty());
        service = new TimeTrackingService(repository);
    }

    // ========================================================================
    // startTimer tests
    // ========================================================================

    @Test
    void startTimer_withValidTaskId_createsTimerEntry() {
        Map<String, Object> result = service.startTimer("TASK-001", "Working on feature X");

        assertThat(result.get("status")).isEqualTo("running");
        assertThat(result.get("taskId")).isEqualTo("TASK-001");
        assertThat(result.get("id")).isNotNull();
        assertThat(result.get("timerStartedAt")).isNotNull();
        assertThat((String) result.get("message")).contains("TASK-001");

        ArgumentCaptor<TimeEntry> captor = ArgumentCaptor.forClass(TimeEntry.class);
        verify(repository).saveEntry(captor.capture());

        TimeEntry saved = captor.getValue();
        assertThat(saved.getTaskId()).isEqualTo("TASK-001");
        assertThat(saved.getDescription()).isEqualTo("Working on feature X");
        assertThat(saved.getDurationMinutes()).isEqualTo(0);
        assertThat(saved.getTimerStartedAt()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo("default");
    }

    @Test
    void startTimer_withNullTaskId_returnsError() {
        Map<String, Object> result = service.startTimer(null, "desc");

        assertThat(result.get("error")).isEqualTo("taskId is required");
        verify(repository, never()).saveEntry(any());
    }

    @Test
    void startTimer_withBlankTaskId_returnsError() {
        Map<String, Object> result = service.startTimer("  ", "desc");

        assertThat(result.get("error")).isEqualTo("taskId is required");
        verify(repository, never()).saveEntry(any());
    }

    @Test
    void startTimer_withActiveTimerRunning_returnsError() {
        TimeEntry activeTimer = TimeEntry.builder()
                .id("timer-1")
                .taskId("TASK-EXISTING")
                .timerStartedAt(Instant.now())
                .build();

        when(repository.findActiveTimer()).thenReturn(Optional.of(activeTimer));

        Map<String, Object> result = service.startTimer("TASK-002", "new task");

        assertThat(result.get("error")).isEqualTo("A timer is already running");
        assertThat(result.get("activeTaskId")).isEqualTo("TASK-EXISTING");
        assertThat(result.get("activeTimerId")).isEqualTo("timer-1");
        verify(repository, never()).saveEntry(any());
    }

    @Test
    void startTimer_withNullDescription_savesSuccessfully() {
        Map<String, Object> result = service.startTimer("TASK-003", null);

        assertThat(result.get("status")).isEqualTo("running");
        assertThat(result.get("taskId")).isEqualTo("TASK-003");

        ArgumentCaptor<TimeEntry> captor = ArgumentCaptor.forClass(TimeEntry.class);
        verify(repository).saveEntry(captor.capture());
        assertThat(captor.getValue().getDescription()).isNull();
    }

    @Test
    void startTimer_generatesUniqueIds() {
        Map<String, Object> result1 = service.startTimer("TASK-A", "first");
        // Reset mock so second call also sees no active timer
        when(repository.findActiveTimer()).thenReturn(Optional.empty());
        Map<String, Object> result2 = service.startTimer("TASK-B", "second");

        assertThat(result1.get("id")).isNotEqualTo(result2.get("id"));
    }

    // ========================================================================
    // stopTimer tests
    // ========================================================================

    @Test
    void stopTimer_withActiveTimer_calculatesDuration() {
        Instant startTime = Instant.now().minus(45, ChronoUnit.MINUTES);
        TimeEntry activeTimer = TimeEntry.builder()
                .id("timer-1")
                .taskId("TASK-001")
                .userId("default")
                .durationMinutes(0)
                .description("Working")
                .entryDate("2026-02-12")
                .timerStartedAt(startTime)
                .createdAt(startTime)
                .build();

        when(repository.findActiveTimer()).thenReturn(Optional.of(activeTimer));

        Map<String, Object> result = service.stopTimer();

        assertThat(result.get("status")).isEqualTo("stopped");
        assertThat(result.get("id")).isEqualTo("timer-1");
        assertThat(result.get("taskId")).isEqualTo("TASK-001");
        assertThat((int) result.get("durationMinutes")).isGreaterThanOrEqualTo(44);
        assertThat((int) result.get("durationMinutes")).isLessThanOrEqualTo(46);
        assertThat(result.get("durationFormatted")).isNotNull();
        assertThat(result.get("stoppedAt")).isNotNull();
        assertThat((String) result.get("message")).contains("TASK-001");

        ArgumentCaptor<TimeEntry> captor = ArgumentCaptor.forClass(TimeEntry.class);
        verify(repository).saveEntry(captor.capture());

        TimeEntry stopped = captor.getValue();
        assertThat(stopped.getTimerStartedAt()).isNull();
        assertThat(stopped.getDurationMinutes()).isGreaterThanOrEqualTo(44);
    }

    @Test
    void stopTimer_withNoActiveTimer_returnsError() {
        when(repository.findActiveTimer()).thenReturn(Optional.empty());

        Map<String, Object> result = service.stopTimer();

        assertThat(result.get("error")).isEqualTo("No active timer found");
    }

    @Test
    void stopTimer_clearsTimerStartedAt() {
        Instant startTime = Instant.now().minus(10, ChronoUnit.MINUTES);
        TimeEntry activeTimer = TimeEntry.builder()
                .id("timer-2")
                .taskId("TASK-002")
                .userId("default")
                .durationMinutes(0)
                .description(null)
                .entryDate("2026-02-12")
                .timerStartedAt(startTime)
                .createdAt(startTime)
                .build();

        when(repository.findActiveTimer()).thenReturn(Optional.of(activeTimer));

        service.stopTimer();

        ArgumentCaptor<TimeEntry> captor = ArgumentCaptor.forClass(TimeEntry.class);
        verify(repository).saveEntry(captor.capture());
        assertThat(captor.getValue().getTimerStartedAt()).isNull();
    }

    // ========================================================================
    // logTime tests
    // ========================================================================

    @Test
    void logTime_withValidInput_savesEntry() {
        Map<String, Object> result = service.logTime("TASK-001", 120, "Code review", "2026-02-12");

        assertThat(result.get("saved")).isEqualTo(true);
        assertThat(result.get("taskId")).isEqualTo("TASK-001");
        assertThat(result.get("durationMinutes")).isEqualTo(120);
        assertThat(result.get("durationFormatted")).isEqualTo("2:00");
        assertThat(result.get("date")).isEqualTo("2026-02-12");
        assertThat(result.get("id")).isNotNull();
        assertThat((String) result.get("message")).contains("120");
        assertThat((String) result.get("message")).contains("TASK-001");

        ArgumentCaptor<TimeEntry> captor = ArgumentCaptor.forClass(TimeEntry.class);
        verify(repository).saveEntry(captor.capture());

        TimeEntry saved = captor.getValue();
        assertThat(saved.getTaskId()).isEqualTo("TASK-001");
        assertThat(saved.getDurationMinutes()).isEqualTo(120);
        assertThat(saved.getDescription()).isEqualTo("Code review");
        assertThat(saved.getEntryDate()).isEqualTo("2026-02-12");
        assertThat(saved.getTimerStartedAt()).isNull();
    }

    @Test
    void logTime_withNullTaskId_returnsError() {
        Map<String, Object> result = service.logTime(null, 60, "desc", "2026-02-12");

        assertThat(result.get("error")).isEqualTo("taskId is required");
        verify(repository, never()).saveEntry(any());
    }

    @Test
    void logTime_withBlankTaskId_returnsError() {
        Map<String, Object> result = service.logTime("", 60, "desc", "2026-02-12");

        assertThat(result.get("error")).isEqualTo("taskId is required");
        verify(repository, never()).saveEntry(any());
    }

    @Test
    void logTime_withZeroDuration_returnsError() {
        Map<String, Object> result = service.logTime("TASK-001", 0, "desc", "2026-02-12");

        assertThat(result.get("error")).isEqualTo("durationMinutes must be greater than 0");
        verify(repository, never()).saveEntry(any());
    }

    @Test
    void logTime_withNegativeDuration_returnsError() {
        Map<String, Object> result = service.logTime("TASK-001", -5, "desc", "2026-02-12");

        assertThat(result.get("error")).isEqualTo("durationMinutes must be greater than 0");
        verify(repository, never()).saveEntry(any());
    }

    @Test
    void logTime_withNullDate_usesToday() {
        Map<String, Object> result = service.logTime("TASK-001", 30, "quick fix", null);

        assertThat(result.get("saved")).isEqualTo(true);
        assertThat(result.get("date")).isNotNull();
        String date = (String) result.get("date");
        assertThat(date).matches("\\d{4}-\\d{2}-\\d{2}");
    }

    @Test
    void logTime_withBlankDate_usesToday() {
        Map<String, Object> result = service.logTime("TASK-001", 30, "quick fix", "  ");

        assertThat(result.get("saved")).isEqualTo(true);
        assertThat(result.get("date")).isNotNull();
        String date = (String) result.get("date");
        assertThat(date).matches("\\d{4}-\\d{2}-\\d{2}");
    }

    @Test
    void logTime_withNullDescription_savesSuccessfully() {
        Map<String, Object> result = service.logTime("TASK-001", 60, null, "2026-02-12");

        assertThat(result.get("saved")).isEqualTo(true);

        ArgumentCaptor<TimeEntry> captor = ArgumentCaptor.forClass(TimeEntry.class);
        verify(repository).saveEntry(captor.capture());
        assertThat(captor.getValue().getDescription()).isNull();
    }

    // ========================================================================
    // getTimesheet tests
    // ========================================================================

    @Test
    @SuppressWarnings("unchecked")
    void getTimesheet_withEntries_calculatesTotals() {
        TimeEntry entry1 = TimeEntry.builder()
                .id("e1").taskId("TASK-001").userId("user1")
                .durationMinutes(120).description("Feature A").entryDate("2026-02-10")
                .createdAt(Instant.now())
                .build();
        TimeEntry entry2 = TimeEntry.builder()
                .id("e2").taskId("TASK-002").userId("user1")
                .durationMinutes(90).description("Bug fix").entryDate("2026-02-11")
                .createdAt(Instant.now())
                .build();

        when(repository.findEntriesByDateRange("2026-02-10", "2026-02-12", "user1"))
                .thenReturn(List.of(entry1, entry2));

        Map<String, Object> result = service.getTimesheet("2026-02-10", "2026-02-12", "user1");

        assertThat(result.get("startDate")).isEqualTo("2026-02-10");
        assertThat(result.get("endDate")).isEqualTo("2026-02-12");
        assertThat(result.get("userId")).isEqualTo("user1");
        assertThat(result.get("entryCount")).isEqualTo(2);
        assertThat(result.get("totalMinutes")).isEqualTo(210);
        assertThat(result.get("totalFormatted")).isEqualTo("3:30");

        List<Map<String, Object>> entries = (List<Map<String, Object>>) result.get("entries");
        assertThat(entries).hasSize(2);
        assertThat(entries.get(0).get("id")).isEqualTo("e1");
        assertThat(entries.get(0).get("taskId")).isEqualTo("TASK-001");
        assertThat(entries.get(0).get("durationMinutes")).isEqualTo(120);
        assertThat(entries.get(0).get("durationFormatted")).isEqualTo("2:00");
    }

    @Test
    @SuppressWarnings("unchecked")
    void getTimesheet_withNoEntries_returnsEmptyList() {
        when(repository.findEntriesByDateRange("2026-01-01", "2026-01-31", null))
                .thenReturn(List.of());

        Map<String, Object> result = service.getTimesheet("2026-01-01", "2026-01-31", null);

        assertThat(result.get("entryCount")).isEqualTo(0);
        assertThat(result.get("totalMinutes")).isEqualTo(0);
        assertThat(result.get("totalFormatted")).isEqualTo("0:00");
        assertThat((List<?>) result.get("entries")).isEmpty();
    }

    @Test
    void getTimesheet_withNullStartDate_returnsError() {
        Map<String, Object> result = service.getTimesheet(null, "2026-02-12", null);

        assertThat(result.get("error")).isEqualTo("startDate and endDate are required");
    }

    @Test
    void getTimesheet_withBlankEndDate_returnsError() {
        Map<String, Object> result = service.getTimesheet("2026-02-01", "  ", null);

        assertThat(result.get("error")).isEqualTo("startDate and endDate are required");
    }

    @Test
    void getTimesheet_withNullUserId_doesNotIncludeUserIdInResult() {
        when(repository.findEntriesByDateRange("2026-02-01", "2026-02-28", null))
                .thenReturn(List.of());

        Map<String, Object> result = service.getTimesheet("2026-02-01", "2026-02-28", null);

        assertThat(result.containsKey("userId")).isFalse();
    }

    @Test
    void getTimesheet_withEmptyUserId_doesNotIncludeUserIdInResult() {
        when(repository.findEntriesByDateRange("2026-02-01", "2026-02-28", ""))
                .thenReturn(List.of());

        Map<String, Object> result = service.getTimesheet("2026-02-01", "2026-02-28", "");

        assertThat(result.containsKey("userId")).isFalse();
    }

    // ========================================================================
    // detectAnomalies tests
    // ========================================================================

    @Test
    @SuppressWarnings("unchecked")
    void detectAnomalies_withExcessiveHours_flagsAnomaly() {
        String today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        List<TimeEntry> entries = new ArrayList<>();
        // 11 hours on a single day (660 minutes)
        entries.add(TimeEntry.builder()
                .id("e1").taskId("T1").userId("user1")
                .durationMinutes(360).description("Morning").entryDate(today)
                .createdAt(Instant.now())
                .build());
        entries.add(TimeEntry.builder()
                .id("e2").taskId("T2").userId("user1")
                .durationMinutes(300).description("Afternoon").entryDate(today)
                .createdAt(Instant.now())
                .build());

        when(repository.findEntriesByUserId("user1")).thenReturn(entries);

        Map<String, Object> result = service.detectAnomalies("user1", 7);

        assertThat((int) result.get("anomalyCount")).isGreaterThanOrEqualTo(1);
        List<Map<String, Object>> anomalies = (List<Map<String, Object>>) result.get("anomalies");

        boolean hasExcessive = anomalies.stream()
                .anyMatch(a -> "excessive_hours".equals(a.get("type")));
        assertThat(hasExcessive).isTrue();

        Map<String, Object> summary = (Map<String, Object>) result.get("summary");
        assertThat((int) summary.get("excessiveDays")).isGreaterThanOrEqualTo(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void detectAnomalies_withWeekendWork_flagsAnomaly() {
        // Find the next Saturday
        java.time.LocalDate saturday = java.time.LocalDate.now();
        while (saturday.getDayOfWeek() != java.time.DayOfWeek.SATURDAY) {
            saturday = saturday.minusDays(1);
        }
        String saturdayStr = saturday.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);

        List<TimeEntry> entries = new ArrayList<>();
        entries.add(TimeEntry.builder()
                .id("e1").taskId("T1").userId("user1")
                .durationMinutes(240).description("Weekend work").entryDate(saturdayStr)
                .createdAt(Instant.now())
                .build());

        when(repository.findEntriesByUserId("user1")).thenReturn(entries);

        Map<String, Object> result = service.detectAnomalies("user1", 30);

        List<Map<String, Object>> anomalies = (List<Map<String, Object>>) result.get("anomalies");
        boolean hasWeekend = anomalies.stream()
                .anyMatch(a -> "weekend_work".equals(a.get("type")));
        assertThat(hasWeekend).isTrue();

        Map<String, Object> summary = (Map<String, Object>) result.get("summary");
        assertThat((int) summary.get("weekendDays")).isGreaterThanOrEqualTo(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void detectAnomalies_withNoAnomalies_returnsEmptyList() {
        java.time.LocalDate weekday = java.time.LocalDate.now();
        while (weekday.getDayOfWeek() == java.time.DayOfWeek.SATURDAY
                || weekday.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            weekday = weekday.minusDays(1);
        }
        String weekdayStr = weekday.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);

        List<TimeEntry> entries = new ArrayList<>();
        entries.add(TimeEntry.builder()
                .id("e1").taskId("T1").userId("user1")
                .durationMinutes(480).description("Normal day").entryDate(weekdayStr)
                .createdAt(Instant.now())
                .build());

        when(repository.findEntriesByUserId("user1")).thenReturn(entries);

        Map<String, Object> result = service.detectAnomalies("user1", 7);

        assertThat((int) result.get("anomalyCount")).isEqualTo(0);
        assertThat((List<?>) result.get("anomalies")).isEmpty();
    }

    @Test
    void detectAnomalies_withNullUserId_returnsError() {
        Map<String, Object> result = service.detectAnomalies(null, 7);

        assertThat(result.get("error")).isEqualTo("userId is required");
    }

    @Test
    void detectAnomalies_withBlankUserId_returnsError() {
        Map<String, Object> result = service.detectAnomalies("", 7);

        assertThat(result.get("error")).isEqualTo("userId is required");
    }

    @Test
    void detectAnomalies_withZeroDays_returnsError() {
        Map<String, Object> result = service.detectAnomalies("user1", 0);

        assertThat(result.get("error")).isEqualTo("days must be greater than 0");
    }

    @Test
    void detectAnomalies_withNegativeDays_returnsError() {
        Map<String, Object> result = service.detectAnomalies("user1", -5);

        assertThat(result.get("error")).isEqualTo("days must be greater than 0");
    }

    @Test
    @SuppressWarnings("unchecked")
    void detectAnomalies_filtersEntriesOutsideDateRange() {
        // Entry from 60 days ago - should be filtered out when asking for 7 days
        String oldDate = java.time.LocalDate.now().minusDays(60)
                .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);

        List<TimeEntry> entries = new ArrayList<>();
        entries.add(TimeEntry.builder()
                .id("e1").taskId("T1").userId("user1")
                .durationMinutes(720).description("Old excessive").entryDate(oldDate)
                .createdAt(Instant.now())
                .build());

        when(repository.findEntriesByUserId("user1")).thenReturn(entries);

        Map<String, Object> result = service.detectAnomalies("user1", 7);

        assertThat((int) result.get("anomalyCount")).isEqualTo(0);
        Map<String, Object> summary = (Map<String, Object>) result.get("summary");
        assertThat((int) summary.get("analyzedDays")).isEqualTo(0);
    }

    @Test
    @SuppressWarnings("unchecked")
    void detectAnomalies_calculatesCorrectSummary() {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate yesterday = today.minusDays(1);

        // Ensure both days are within range by checking they aren't too far back
        String todayStr = today.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        String yesterdayStr = yesterday.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);

        List<TimeEntry> entries = new ArrayList<>();
        entries.add(TimeEntry.builder()
                .id("e1").taskId("T1").userId("user1")
                .durationMinutes(300).description("Day 1").entryDate(yesterdayStr)
                .createdAt(Instant.now())
                .build());
        entries.add(TimeEntry.builder()
                .id("e2").taskId("T2").userId("user1")
                .durationMinutes(480).description("Day 2").entryDate(todayStr)
                .createdAt(Instant.now())
                .build());

        when(repository.findEntriesByUserId("user1")).thenReturn(entries);

        Map<String, Object> result = service.detectAnomalies("user1", 7);

        assertThat(result.get("userId")).isEqualTo("user1");
        assertThat(result.get("periodDays")).isEqualTo(7);

        Map<String, Object> summary = (Map<String, Object>) result.get("summary");
        assertThat((int) summary.get("analyzedDays")).isEqualTo(2);
        assertThat((int) summary.get("totalMinutes")).isEqualTo(780);
        assertThat((int) summary.get("averageMinutesPerDay")).isEqualTo(390);
    }

    // ========================================================================
    // estimateVsActual tests
    // ========================================================================

    @Test
    void estimateVsActual_withEstimateAndActual_calculatesVariance() {
        TimeEstimate estimate = TimeEstimate.builder()
                .id("est-1").taskId("TASK-001").estimateMinutes(120)
                .description("Feature estimate").createdAt(Instant.now())
                .build();

        TimeEntry entry1 = TimeEntry.builder()
                .id("e1").taskId("TASK-001").userId("user1")
                .durationMinutes(90).entryDate("2026-02-12").createdAt(Instant.now())
                .build();
        TimeEntry entry2 = TimeEntry.builder()
                .id("e2").taskId("TASK-001").userId("user1")
                .durationMinutes(60).entryDate("2026-02-12").createdAt(Instant.now())
                .build();

        when(repository.findEstimateByTaskId("TASK-001")).thenReturn(Optional.of(estimate));
        when(repository.findEntriesByTaskId("TASK-001")).thenReturn(List.of(entry1, entry2));

        Map<String, Object> result = service.estimateVsActual("TASK-001", 0, null);

        assertThat(result.get("taskId")).isEqualTo("TASK-001");
        assertThat(result.get("estimateMinutes")).isEqualTo(120);
        assertThat(result.get("estimateFormatted")).isEqualTo("2:00");
        assertThat(result.get("actualMinutes")).isEqualTo(150);
        assertThat(result.get("actualFormatted")).isEqualTo("2:30");
        assertThat(result.get("varianceMinutes")).isEqualTo(30);
        assertThat(result.get("varianceFormatted")).isEqualTo("0:30");
        assertThat((double) result.get("variancePercent")).isEqualTo(25.0);
        assertThat(result.get("status")).isEqualTo("over_estimate");
    }

    @Test
    void estimateVsActual_withActualUnderEstimate_returnsUnderEstimate() {
        TimeEstimate estimate = TimeEstimate.builder()
                .id("est-1").taskId("TASK-002").estimateMinutes(200)
                .description("Big task").createdAt(Instant.now())
                .build();

        TimeEntry entry = TimeEntry.builder()
                .id("e1").taskId("TASK-002").userId("user1")
                .durationMinutes(100).entryDate("2026-02-12").createdAt(Instant.now())
                .build();

        when(repository.findEstimateByTaskId("TASK-002")).thenReturn(Optional.of(estimate));
        when(repository.findEntriesByTaskId("TASK-002")).thenReturn(List.of(entry));

        Map<String, Object> result = service.estimateVsActual("TASK-002", 0, null);

        assertThat(result.get("varianceMinutes")).isEqualTo(-100);
        assertThat((double) result.get("variancePercent")).isEqualTo(-50.0);
        assertThat(result.get("status")).isEqualTo("under_estimate");
    }

    @Test
    void estimateVsActual_withExactMatch_returnsOnTarget() {
        TimeEstimate estimate = TimeEstimate.builder()
                .id("est-1").taskId("TASK-003").estimateMinutes(60)
                .description("Quick task").createdAt(Instant.now())
                .build();

        TimeEntry entry = TimeEntry.builder()
                .id("e1").taskId("TASK-003").userId("user1")
                .durationMinutes(60).entryDate("2026-02-12").createdAt(Instant.now())
                .build();

        when(repository.findEstimateByTaskId("TASK-003")).thenReturn(Optional.of(estimate));
        when(repository.findEntriesByTaskId("TASK-003")).thenReturn(List.of(entry));

        Map<String, Object> result = service.estimateVsActual("TASK-003", 0, null);

        assertThat(result.get("varianceMinutes")).isEqualTo(0);
        assertThat((double) result.get("variancePercent")).isEqualTo(0.0);
        assertThat(result.get("status")).isEqualTo("on_target");
    }

    @Test
    void estimateVsActual_withNoEstimate_returnsNoEstimateStatus() {
        when(repository.findEstimateByTaskId("TASK-NEW")).thenReturn(Optional.empty());
        when(repository.findEntriesByTaskId("TASK-NEW")).thenReturn(List.of());

        Map<String, Object> result = service.estimateVsActual("TASK-NEW", 0, null);

        assertThat(result.get("taskId")).isEqualTo("TASK-NEW");
        assertThat(result.get("actualMinutes")).isEqualTo(0);
        assertThat(result.get("estimateMinutes")).isEqualTo(0);
        assertThat(result.get("status")).isEqualTo("no_estimate");
        assertThat((String) result.get("message")).contains("No estimate found");
    }

    @Test
    void estimateVsActual_withEstimateMinutesProvided_savesNewEstimate() {
        when(repository.findEstimateByTaskId("TASK-004")).thenReturn(Optional.empty());
        when(repository.findEntriesByTaskId("TASK-004")).thenReturn(List.of());

        // Re-mock to return the estimate AFTER save
        when(repository.saveEstimate(any(TimeEstimate.class)))
                .thenAnswer(invocation -> {
                    TimeEstimate saved = invocation.getArgument(0);
                    // After saving, mock findEstimateByTaskId to return it
                    when(repository.findEstimateByTaskId("TASK-004"))
                            .thenReturn(Optional.of(saved));
                    return saved;
                });

        Map<String, Object> result = service.estimateVsActual("TASK-004", 180, "Estimated 3 hours");

        ArgumentCaptor<TimeEstimate> captor = ArgumentCaptor.forClass(TimeEstimate.class);
        verify(repository).saveEstimate(captor.capture());

        TimeEstimate saved = captor.getValue();
        assertThat(saved.getTaskId()).isEqualTo("TASK-004");
        assertThat(saved.getEstimateMinutes()).isEqualTo(180);
        assertThat(saved.getDescription()).isEqualTo("Estimated 3 hours");
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void estimateVsActual_withZeroEstimateMinutes_doesNotSaveEstimate() {
        when(repository.findEstimateByTaskId("TASK-005")).thenReturn(Optional.empty());
        when(repository.findEntriesByTaskId("TASK-005")).thenReturn(List.of());

        service.estimateVsActual("TASK-005", 0, null);

        verify(repository, never()).saveEstimate(any());
    }

    @Test
    void estimateVsActual_withNullTaskId_returnsError() {
        Map<String, Object> result = service.estimateVsActual(null, 60, "desc");

        assertThat(result.get("error")).isEqualTo("taskId is required");
    }

    @Test
    void estimateVsActual_withBlankTaskId_returnsError() {
        Map<String, Object> result = service.estimateVsActual("  ", 60, "desc");

        assertThat(result.get("error")).isEqualTo("taskId is required");
    }

    @Test
    void estimateVsActual_countsEntries() {
        TimeEstimate estimate = TimeEstimate.builder()
                .id("est-1").taskId("TASK-006").estimateMinutes(480)
                .description("Full day").createdAt(Instant.now())
                .build();

        List<TimeEntry> entries = List.of(
                TimeEntry.builder().id("e1").taskId("TASK-006").userId("u1").durationMinutes(120).entryDate("2026-02-10").createdAt(Instant.now()).build(),
                TimeEntry.builder().id("e2").taskId("TASK-006").userId("u1").durationMinutes(120).entryDate("2026-02-11").createdAt(Instant.now()).build(),
                TimeEntry.builder().id("e3").taskId("TASK-006").userId("u1").durationMinutes(120).entryDate("2026-02-12").createdAt(Instant.now()).build()
        );

        when(repository.findEstimateByTaskId("TASK-006")).thenReturn(Optional.of(estimate));
        when(repository.findEntriesByTaskId("TASK-006")).thenReturn(entries);

        Map<String, Object> result = service.estimateVsActual("TASK-006", 0, null);

        assertThat(result.get("entriesCount")).isEqualTo(3);
        assertThat(result.get("actualMinutes")).isEqualTo(360);
    }

    // ========================================================================
    // formatDuration helper tests
    // ========================================================================

    @Test
    void formatDuration_withZero_returnsZero() {
        assertThat(service.formatDuration(0)).isEqualTo("0:00");
    }

    @Test
    void formatDuration_withMinutesOnly_formatsCorrectly() {
        assertThat(service.formatDuration(45)).isEqualTo("0:45");
    }

    @Test
    void formatDuration_withHoursAndMinutes_formatsCorrectly() {
        assertThat(service.formatDuration(150)).isEqualTo("2:30");
    }

    @Test
    void formatDuration_withExactHours_formatsCorrectly() {
        assertThat(service.formatDuration(480)).isEqualTo("8:00");
    }

    @Test
    void formatDuration_withLargeValue_formatsCorrectly() {
        assertThat(service.formatDuration(1440)).isEqualTo("24:00");
    }
}
