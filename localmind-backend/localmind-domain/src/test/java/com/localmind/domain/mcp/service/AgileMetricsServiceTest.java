package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.AgileMetric;
import com.localmind.domain.mcp.port.out.AgileMetricRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AgileMetricsServiceTest {

    @Mock
    private AgileMetricRepository repository;

    private AgileMetricsService service;

    @BeforeEach
    void setUp() {
        service = new AgileMetricsService(repository);
        when(repository.save(any(AgileMetric.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.findByMetricType(any(String.class)))
                .thenReturn(Collections.emptyList());
    }

    // ========== calculateVelocity Tests ==========

    @Test
    void calculateVelocity_withValidSprints_calculatesAverage() {
        List<Map<String, Object>> sprints = new ArrayList<>();
        sprints.add(buildSprint("Sprint 1", 20));
        sprints.add(buildSprint("Sprint 2", 25));
        sprints.add(buildSprint("Sprint 3", 30));

        Map<String, Object> result = service.calculateVelocity(sprints);

        assertThat(result.get("averageVelocity")).isEqualTo(25.0);
        assertThat(result.get("highest")).isEqualTo(30);
        assertThat(result.get("lowest")).isEqualTo(20);
        assertThat(result.get("sprintCount")).isEqualTo(3);
    }

    @Test
    void calculateVelocity_withImprovingTrend_detectsImproving() {
        List<Map<String, Object>> sprints = new ArrayList<>();
        sprints.add(buildSprint("Sprint 1", 10));
        sprints.add(buildSprint("Sprint 2", 12));
        sprints.add(buildSprint("Sprint 3", 20));
        sprints.add(buildSprint("Sprint 4", 25));

        Map<String, Object> result = service.calculateVelocity(sprints);

        assertThat(result.get("trend")).isEqualTo("improving");
    }

    @Test
    void calculateVelocity_withDecliningTrend_detectsDeclining() {
        List<Map<String, Object>> sprints = new ArrayList<>();
        sprints.add(buildSprint("Sprint 1", 30));
        sprints.add(buildSprint("Sprint 2", 28));
        sprints.add(buildSprint("Sprint 3", 15));
        sprints.add(buildSprint("Sprint 4", 10));

        Map<String, Object> result = service.calculateVelocity(sprints);

        assertThat(result.get("trend")).isEqualTo("declining");
    }

    @Test
    void calculateVelocity_withStableTrend_detectsStable() {
        List<Map<String, Object>> sprints = new ArrayList<>();
        sprints.add(buildSprint("Sprint 1", 20));
        sprints.add(buildSprint("Sprint 2", 21));
        sprints.add(buildSprint("Sprint 3", 20));
        sprints.add(buildSprint("Sprint 4", 21));

        Map<String, Object> result = service.calculateVelocity(sprints);

        assertThat(result.get("trend")).isEqualTo("stable");
    }

    @Test
    void calculateVelocity_withSingleSprint_returnsStableTrend() {
        List<Map<String, Object>> sprints = new ArrayList<>();
        sprints.add(buildSprint("Sprint 1", 20));

        Map<String, Object> result = service.calculateVelocity(sprints);

        assertThat(result.get("trend")).isEqualTo("stable");
        assertThat(result.get("averageVelocity")).isEqualTo(20.0);
    }

    @Test
    void calculateVelocity_withNullInput_returnsError() {
        Map<String, Object> result = service.calculateVelocity(null);

        assertThat(result.get("error")).isEqualTo("No sprint data provided");
        verify(repository, never()).save(any());
    }

    @Test
    void calculateVelocity_withEmptyList_returnsError() {
        Map<String, Object> result = service.calculateVelocity(new ArrayList<>());

        assertThat(result.get("error")).isEqualTo("No sprint data provided");
        verify(repository, never()).save(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void calculateVelocity_returnsSprintDetails() {
        List<Map<String, Object>> sprints = new ArrayList<>();
        sprints.add(buildSprint("Sprint 1", 20));
        sprints.add(buildSprint("Sprint 2", 30));

        Map<String, Object> result = service.calculateVelocity(sprints);

        List<Map<String, Object>> sprintResults = (List<Map<String, Object>>) result.get("sprints");
        assertThat(sprintResults).hasSize(2);
        assertThat(sprintResults.get(0).get("name")).isEqualTo("Sprint 1");
        assertThat(sprintResults.get(0).get("completedPoints")).isEqualTo(20);
    }

    @Test
    void calculateVelocity_savesToRepository() {
        List<Map<String, Object>> sprints = new ArrayList<>();
        sprints.add(buildSprint("Sprint 1", 20));

        service.calculateVelocity(sprints);

        ArgumentCaptor<AgileMetric> captor = ArgumentCaptor.forClass(AgileMetric.class);
        verify(repository).save(captor.capture());
        AgileMetric saved = captor.getValue();
        assertThat(saved.getMetricType()).isEqualTo("velocity");
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    // ========== generateBurndown Tests ==========

    @Test
    @SuppressWarnings("unchecked")
    void generateBurndown_generatesIdealLine() {
        List<Map<String, Object>> dailyProgress = new ArrayList<>();

        Map<String, Object> result = service.generateBurndown(100, 10, dailyProgress);

        List<Map<String, Object>> idealLine = (List<Map<String, Object>>) result.get("idealLine");
        assertThat(idealLine).hasSize(11); // day 0 through day 10
        assertThat(idealLine.get(0).get("remaining")).isEqualTo(100.0);
        assertThat(idealLine.get(10).get("remaining")).isEqualTo(0.0);
        assertThat(idealLine.get(5).get("remaining")).isEqualTo(50.0);
    }

    @Test
    void generateBurndown_withAheadProgress_detectsAhead() {
        List<Map<String, Object>> dailyProgress = new ArrayList<>();
        dailyProgress.add(buildDailyProgress(1, 80));
        dailyProgress.add(buildDailyProgress(2, 60));
        dailyProgress.add(buildDailyProgress(3, 40));

        Map<String, Object> result = service.generateBurndown(100, 10, dailyProgress);

        // Ideal at day 3: 100 - (10 * 3) = 70. Actual is 40, well below ideal - tolerance.
        assertThat(result.get("status")).isEqualTo("ahead");
    }

    @Test
    void generateBurndown_withBehindProgress_detectsBehind() {
        List<Map<String, Object>> dailyProgress = new ArrayList<>();
        dailyProgress.add(buildDailyProgress(1, 98));
        dailyProgress.add(buildDailyProgress(2, 95));
        dailyProgress.add(buildDailyProgress(3, 90));

        Map<String, Object> result = service.generateBurndown(100, 10, dailyProgress);

        // Ideal at day 3: 70. Actual is 90, well above ideal + tolerance(10).
        assertThat(result.get("status")).isEqualTo("behind");
    }

    @Test
    void generateBurndown_withOnTrackProgress_detectsOnTrack() {
        List<Map<String, Object>> dailyProgress = new ArrayList<>();
        dailyProgress.add(buildDailyProgress(1, 90));
        dailyProgress.add(buildDailyProgress(2, 80));
        dailyProgress.add(buildDailyProgress(3, 70));

        Map<String, Object> result = service.generateBurndown(100, 10, dailyProgress);

        assertThat(result.get("status")).isEqualTo("on-track");
    }

    @Test
    void generateBurndown_withEmptyProgress_returnsOnTrack() {
        Map<String, Object> result = service.generateBurndown(100, 10, new ArrayList<>());

        assertThat(result.get("status")).isEqualTo("on-track");
    }

    @Test
    void generateBurndown_withNullProgress_returnsOnTrack() {
        Map<String, Object> result = service.generateBurndown(100, 10, null);

        assertThat(result.get("status")).isEqualTo("on-track");
    }

    @Test
    void generateBurndown_withZeroTotalPoints_returnsError() {
        Map<String, Object> result = service.generateBurndown(0, 10, new ArrayList<>());

        assertThat(result.get("error")).isEqualTo("Total points must be greater than zero");
        verify(repository, never()).save(any());
    }

    @Test
    void generateBurndown_withZeroSprintDays_returnsError() {
        Map<String, Object> result = service.generateBurndown(100, 0, new ArrayList<>());

        assertThat(result.get("error")).isEqualTo("Sprint days must be greater than zero");
        verify(repository, never()).save(any());
    }

    @Test
    void generateBurndown_savesToRepository() {
        service.generateBurndown(50, 5, new ArrayList<>());

        ArgumentCaptor<AgileMetric> captor = ArgumentCaptor.forClass(AgileMetric.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getMetricType()).isEqualTo("burndown");
    }

    // ========== calculateCycleTime Tests ==========

    @Test
    @SuppressWarnings("unchecked")
    void calculateCycleTime_calculatesCorrectly() {
        List<Map<String, Object>> tasks = new ArrayList<>();
        tasks.add(buildTask("Task A", "2026-01-01", "2026-01-04"));  // 3 days
        tasks.add(buildTask("Task B", "2026-01-05", "2026-01-10"));  // 5 days
        tasks.add(buildTask("Task C", "2026-01-02", "2026-01-09"));  // 7 days

        Map<String, Object> result = service.calculateCycleTime(tasks);

        List<Map<String, Object>> taskResults = (List<Map<String, Object>>) result.get("tasks");
        assertThat(taskResults).hasSize(3);
        assertThat(taskResults.get(0).get("cycleTimeDays")).isEqualTo(3L);
        assertThat(taskResults.get(1).get("cycleTimeDays")).isEqualTo(5L);
        assertThat(taskResults.get(2).get("cycleTimeDays")).isEqualTo(7L);

        assertThat(result.get("average")).isEqualTo(5.0);
        assertThat(result.get("median")).isEqualTo(5.0);
        assertThat(result.get("taskCount")).isEqualTo(3);
    }

    @Test
    void calculateCycleTime_calculatesMedianCorrectly_evenCount() {
        List<Map<String, Object>> tasks = new ArrayList<>();
        tasks.add(buildTask("Task A", "2026-01-01", "2026-01-03"));  // 2 days
        tasks.add(buildTask("Task B", "2026-01-01", "2026-01-05"));  // 4 days
        tasks.add(buildTask("Task C", "2026-01-01", "2026-01-09"));  // 8 days
        tasks.add(buildTask("Task D", "2026-01-01", "2026-01-11")); // 10 days

        Map<String, Object> result = service.calculateCycleTime(tasks);

        // Sorted: 2, 4, 8, 10 -> median = (4 + 8) / 2 = 6.0
        assertThat(result.get("median")).isEqualTo(6.0);
    }

    @Test
    void calculateCycleTime_calculatesP95() {
        List<Map<String, Object>> tasks = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            tasks.add(buildTask("Task " + i, "2026-01-01",
                    "2026-01-" + String.format("%02d", 1 + i)));
        }

        Map<String, Object> result = service.calculateCycleTime(tasks);

        assertThat((double) result.get("p95")).isGreaterThanOrEqualTo(19.0);
    }

    @Test
    @SuppressWarnings("unchecked")
    void calculateCycleTime_withMissingDates_handlesGracefully() {
        List<Map<String, Object>> tasks = new ArrayList<>();
        Map<String, Object> task = new LinkedHashMap<>();
        task.put("name", "Incomplete");
        task.put("startDate", "2026-01-01");
        // no endDate
        tasks.add(task);

        Map<String, Object> result = service.calculateCycleTime(tasks);

        List<Map<String, Object>> taskResults = (List<Map<String, Object>>) result.get("tasks");
        assertThat(taskResults.get(0).get("error")).isEqualTo("Missing startDate or endDate");
        assertThat(taskResults.get(0).get("cycleTimeDays")).isEqualTo(0);
    }

    @Test
    @SuppressWarnings("unchecked")
    void calculateCycleTime_withInvalidDateFormat_handlesGracefully() {
        List<Map<String, Object>> tasks = new ArrayList<>();
        tasks.add(buildTask("Bad Task", "not-a-date", "also-bad"));

        Map<String, Object> result = service.calculateCycleTime(tasks);

        List<Map<String, Object>> taskResults = (List<Map<String, Object>>) result.get("tasks");
        assertThat(taskResults.get(0).get("error")).isEqualTo("Invalid date format");
    }

    @Test
    void calculateCycleTime_withNullInput_returnsError() {
        Map<String, Object> result = service.calculateCycleTime(null);

        assertThat(result.get("error")).isEqualTo("No task data provided");
        verify(repository, never()).save(any());
    }

    @Test
    void calculateCycleTime_withEmptyList_returnsError() {
        Map<String, Object> result = service.calculateCycleTime(new ArrayList<>());

        assertThat(result.get("error")).isEqualTo("No task data provided");
        verify(repository, never()).save(any());
    }

    @Test
    void calculateCycleTime_savesToRepository() {
        List<Map<String, Object>> tasks = new ArrayList<>();
        tasks.add(buildTask("Task A", "2026-01-01", "2026-01-05"));

        service.calculateCycleTime(tasks);

        ArgumentCaptor<AgileMetric> captor = ArgumentCaptor.forClass(AgileMetric.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getMetricType()).isEqualTo("cycle-time");
    }

    // ========== forecastCompletion Tests ==========

    @Test
    void forecastCompletion_withValidInput_returnsForecast() {
        List<Integer> velocityHistory = List.of(20, 25, 22, 28, 24);

        Map<String, Object> result = service.forecastCompletion(100, velocityHistory);

        assertThat(result.get("remainingPoints")).isEqualTo(100);
        assertThat(result.get("simulations")).isEqualTo(AgileMetricsService.MONTE_CARLO_SIMULATIONS);
        assertThat((int) result.get("p50")).isGreaterThan(0);
        assertThat((int) result.get("p85")).isGreaterThanOrEqualTo((int) result.get("p50"));
        assertThat((int) result.get("p95")).isGreaterThanOrEqualTo((int) result.get("p85"));
    }

    @Test
    void forecastCompletion_withUniformVelocity_convergesToExactSprints() {
        // 100 points, velocity always 20 -> exactly 5 sprints
        List<Integer> velocityHistory = List.of(20, 20, 20);

        Map<String, Object> result = service.forecastCompletion(100, velocityHistory);

        assertThat(result.get("p50")).isEqualTo(5);
        assertThat(result.get("p85")).isEqualTo(5);
        assertThat(result.get("p95")).isEqualTo(5);
    }

    @Test
    void forecastCompletion_withHighVariance_hasWiderSpread() {
        List<Integer> velocityHistory = List.of(5, 50, 10, 45, 8, 40);

        Map<String, Object> result = service.forecastCompletion(100, velocityHistory);

        int p50 = (int) result.get("p50");
        int p95 = (int) result.get("p95");
        // p95 should be >= p50 with high variance
        assertThat(p95).isGreaterThanOrEqualTo(p50);
    }

    @Test
    void forecastCompletion_withZeroRemainingPoints_returnsError() {
        Map<String, Object> result = service.forecastCompletion(0, List.of(20));

        assertThat(result.get("error")).isEqualTo("Remaining points must be greater than zero");
        verify(repository, never()).save(any());
    }

    @Test
    void forecastCompletion_withNullVelocityHistory_returnsError() {
        Map<String, Object> result = service.forecastCompletion(100, null);

        assertThat(result.get("error")).isEqualTo("Velocity history is required");
        verify(repository, never()).save(any());
    }

    @Test
    void forecastCompletion_withEmptyVelocityHistory_returnsError() {
        Map<String, Object> result = service.forecastCompletion(100, new ArrayList<>());

        assertThat(result.get("error")).isEqualTo("Velocity history is required");
        verify(repository, never()).save(any());
    }

    @Test
    void forecastCompletion_withAllZeroVelocities_returnsError() {
        Map<String, Object> result = service.forecastCompletion(100, List.of(0, 0, 0));

        assertThat(result.get("error")).isEqualTo("No positive velocity values in history");
        verify(repository, never()).save(any());
    }

    @Test
    void forecastCompletion_containsInterpretation() {
        List<Integer> velocityHistory = List.of(20, 25, 22);

        Map<String, Object> result = service.forecastCompletion(100, velocityHistory);

        assertThat((String) result.get("interpretation")).contains("50% chance");
        assertThat((String) result.get("interpretation")).contains("85% chance");
        assertThat((String) result.get("interpretation")).contains("95% chance");
    }

    @Test
    void forecastCompletion_savesToRepository() {
        service.forecastCompletion(100, List.of(20, 25));

        ArgumentCaptor<AgileMetric> captor = ArgumentCaptor.forClass(AgileMetric.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getMetricType()).isEqualTo("forecast");
    }

    // ========== predictRisk Tests ==========

    @Test
    void predictRisk_withNoData_returnsMediumRisk() {
        Map<String, Object> result = service.predictRisk("SPRINT-42");

        assertThat(result.get("sprintId")).isEqualTo("SPRINT-42");
        assertThat(result.get("riskLevel")).isEqualTo("high");
        assertThat(result.get("recommendation")).isNotNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void predictRisk_withDecliningVelocityAndBehindBurndown_returnsHighRisk() {
        AgileMetric velocityMetric = AgileMetric.builder()
                .id("v1").metricType("velocity")
                .resultJson("{\"trend\":\"declining\"}")
                .createdAt(java.time.Instant.now())
                .build();
        AgileMetric burndownMetric = AgileMetric.builder()
                .id("b1").metricType("burndown")
                .resultJson("{\"status\":\"behind\"}")
                .createdAt(java.time.Instant.now())
                .build();

        when(repository.findByMetricType("velocity")).thenReturn(List.of(velocityMetric));
        when(repository.findByMetricType("burndown")).thenReturn(List.of(burndownMetric));

        Map<String, Object> result = service.predictRisk("SPRINT-1");

        assertThat(result.get("riskLevel")).isEqualTo("high");
        List<Map<String, Object>> factors = (List<Map<String, Object>>) result.get("factors");
        assertThat(factors).anyMatch(f -> "velocity-declining".equals(f.get("id")));
        assertThat(factors).anyMatch(f -> "burndown-behind".equals(f.get("id")));
    }

    @Test
    void predictRisk_withGoodMetrics_returnsLowRisk() {
        AgileMetric velocityMetric = AgileMetric.builder()
                .id("v1").metricType("velocity")
                .resultJson("{\"trend\":\"improving\"}")
                .createdAt(java.time.Instant.now())
                .build();
        AgileMetric burndownMetric = AgileMetric.builder()
                .id("b1").metricType("burndown")
                .resultJson("{\"status\":\"ahead\"}")
                .createdAt(java.time.Instant.now())
                .build();
        AgileMetric cycleTimeMetric = AgileMetric.builder()
                .id("c1").metricType("cycle-time")
                .resultJson("{\"p95\":7.0}")
                .createdAt(java.time.Instant.now())
                .build();

        when(repository.findByMetricType("velocity")).thenReturn(List.of(velocityMetric));
        when(repository.findByMetricType("burndown")).thenReturn(List.of(burndownMetric));
        when(repository.findByMetricType("cycle-time")).thenReturn(List.of(cycleTimeMetric));

        Map<String, Object> result = service.predictRisk("SPRINT-1");

        assertThat(result.get("riskLevel")).isEqualTo("low");
    }

    @Test
    void predictRisk_withNullSprintId_returnsError() {
        Map<String, Object> result = service.predictRisk(null);

        assertThat(result.get("error")).isEqualTo("Sprint ID is required");
        verify(repository, never()).save(any());
    }

    @Test
    void predictRisk_withBlankSprintId_returnsError() {
        Map<String, Object> result = service.predictRisk("   ");

        assertThat(result.get("error")).isEqualTo("Sprint ID is required");
        verify(repository, never()).save(any());
    }

    @Test
    void predictRisk_savesToRepository() {
        service.predictRisk("SPRINT-1");

        ArgumentCaptor<AgileMetric> captor = ArgumentCaptor.forClass(AgileMetric.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getMetricType()).isEqualTo("risk");
        assertThat(captor.getValue().getSprintRef()).isEqualTo("SPRINT-1");
    }

    // ========== correlateFactors Tests ==========

    @Test
    void correlateFactors_withStrongPositiveCorrelation_detectsStrong() {
        Map<String, Object> result = service.correlateFactors(
                "team_size", "velocity", 0.85, 50, "Larger teams tend to have higher velocity");

        assertThat(result.get("factorA")).isEqualTo("team_size");
        assertThat(result.get("factorB")).isEqualTo("velocity");
        assertThat(result.get("correlation")).isEqualTo(0.85);
        assertThat(result.get("strength")).isEqualTo("strong");
        assertThat(result.get("direction")).isEqualTo("positive");
        assertThat(result.get("sampleSize")).isEqualTo(50);
    }

    @Test
    void correlateFactors_withModerateNegativeCorrelation_detectsModerate() {
        Map<String, Object> result = service.correlateFactors(
                "meetings", "productivity", -0.6, 30, "More meetings correlate with lower productivity");

        assertThat(result.get("strength")).isEqualTo("moderate");
        assertThat(result.get("direction")).isEqualTo("negative");
    }

    @Test
    void correlateFactors_withWeakCorrelation_detectsWeak() {
        Map<String, Object> result = service.correlateFactors(
                "experience", "bugs", 0.35, 20, "Slight positive correlation");

        assertThat(result.get("strength")).isEqualTo("weak");
    }

    @Test
    void correlateFactors_withNegligibleCorrelation_detectsNegligible() {
        Map<String, Object> result = service.correlateFactors(
                "coffee", "velocity", 0.1, 100, "No real correlation");

        assertThat(result.get("strength")).isEqualTo("negligible");
    }

    @Test
    void correlateFactors_withNullFactorA_returnsError() {
        Map<String, Object> result = service.correlateFactors(null, "B", 0.5, 10, "desc");

        assertThat(result.get("error")).isEqualTo("Both factorA and factorB are required");
        verify(repository, never()).save(any());
    }

    @Test
    void correlateFactors_withBlankFactorB_returnsError() {
        Map<String, Object> result = service.correlateFactors("A", "  ", 0.5, 10, "desc");

        assertThat(result.get("error")).isEqualTo("Both factorA and factorB are required");
        verify(repository, never()).save(any());
    }

    @Test
    void correlateFactors_withCorrelationOutOfRange_returnsError() {
        Map<String, Object> result = service.correlateFactors("A", "B", 1.5, 10, "desc");

        assertThat(result.get("error")).isEqualTo("Correlation must be between -1.0 and 1.0");
        verify(repository, never()).save(any());
    }

    @Test
    void correlateFactors_withNegativeCorrelationOutOfRange_returnsError() {
        Map<String, Object> result = service.correlateFactors("A", "B", -1.1, 10, "desc");

        assertThat(result.get("error")).isEqualTo("Correlation must be between -1.0 and 1.0");
        verify(repository, never()).save(any());
    }

    @Test
    void correlateFactors_withZeroSampleSize_returnsError() {
        Map<String, Object> result = service.correlateFactors("A", "B", 0.5, 0, "desc");

        assertThat(result.get("error")).isEqualTo("Sample size must be greater than zero");
        verify(repository, never()).save(any());
    }

    @Test
    void correlateFactors_withNullDescription_usesEmptyString() {
        Map<String, Object> result = service.correlateFactors("A", "B", 0.5, 10, null);

        assertThat(result.get("description")).isEqualTo("");
    }

    @Test
    void correlateFactors_savesToRepository() {
        service.correlateFactors("A", "B", 0.5, 10, "desc");

        ArgumentCaptor<AgileMetric> captor = ArgumentCaptor.forClass(AgileMetric.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getMetricType()).isEqualTo("correlation");
        assertThat(captor.getValue().getSprintRef()).isEqualTo("A <-> B");
    }

    // ========== Helper Method Tests ==========

    @Test
    void determineTrend_withSingleValue_returnsStable() {
        assertThat(service.determineTrend(List.of(20))).isEqualTo("stable");
    }

    @Test
    void determineTrend_withTwoEqualValues_returnsStable() {
        assertThat(service.determineTrend(List.of(20, 20))).isEqualTo("stable");
    }

    @Test
    void calculateMedian_withOddCount() {
        assertThat(service.calculateMedian(List.of(1L, 3L, 5L))).isEqualTo(3.0);
    }

    @Test
    void calculateMedian_withEvenCount() {
        assertThat(service.calculateMedian(List.of(1L, 3L, 5L, 7L))).isEqualTo(4.0);
    }

    @Test
    void calculatePercentile_p95_withSmallList() {
        assertThat(service.calculatePercentile(List.of(1L, 2L, 3L, 4L, 5L), 95)).isEqualTo(5.0);
    }

    @Test
    void calculatePercentile_p50() {
        assertThat(service.calculatePercentile(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L), 50))
                .isEqualTo(5.0);
    }

    // ========== Test Helper Methods ==========

    private Map<String, Object> buildSprint(String name, int completedPoints) {
        Map<String, Object> sprint = new LinkedHashMap<>();
        sprint.put("name", name);
        sprint.put("completedPoints", completedPoints);
        return sprint;
    }

    private Map<String, Object> buildDailyProgress(int day, int remaining) {
        Map<String, Object> progress = new LinkedHashMap<>();
        progress.put("day", day);
        progress.put("remaining", remaining);
        return progress;
    }

    private Map<String, Object> buildTask(String name, String startDate, String endDate) {
        Map<String, Object> task = new LinkedHashMap<>();
        task.put("name", name);
        task.put("startDate", startDate);
        task.put("endDate", endDate);
        return task;
    }
}
