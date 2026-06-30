package com.localmind.domain.mcp.port.in;

import java.util.List;
import java.util.Map;

/**
 * Input port for the agile-metrics tool group.
 * Provides velocity calculation, burndown generation, cycle time analysis,
 * forecast completion via Monte Carlo, risk prediction and factor correlation.
 */
public interface AgileMetricsUseCase {

    /**
     * Calculates velocity statistics from a list of sprint data.
     * Each sprint map should contain: name(String), completedPoints(Integer).
     *
     * @param sprints list of sprint data maps
     * @return result with averageVelocity, trend, highest, lowest, sprints
     */
    Map<String, Object> calculateVelocity(List<Map<String, Object>> sprints);

    /**
     * Generates a burndown chart dataset comparing ideal vs actual progress.
     *
     * @param totalPoints   total story points in the sprint
     * @param sprintDays    number of days in the sprint
     * @param dailyProgress list of daily progress maps, each with: day(Integer), remaining(Integer)
     * @return result with idealLine, actualLine, status, totalPoints, sprintDays
     */
    Map<String, Object> generateBurndown(int totalPoints, int sprintDays,
                                          List<Map<String, Object>> dailyProgress);

    /**
     * Calculates cycle time statistics from a list of tasks.
     * Each task map should contain: name(String), startDate(String ISO), endDate(String ISO).
     *
     * @param tasks list of task data maps
     * @return result with tasks (each with cycleTimeDays), average, median, p95
     */
    Map<String, Object> calculateCycleTime(List<Map<String, Object>> tasks);

    /**
     * Forecasts sprint completion using Monte Carlo simulation.
     * Runs 1000 simulations sampling randomly from velocity history.
     *
     * @param remainingPoints total remaining story points
     * @param velocityHistory list of historical velocity values (points per sprint)
     * @return result with remainingPoints, simulations, p50, p85, p95
     */
    Map<String, Object> forecastCompletion(int remainingPoints, List<Integer> velocityHistory);

    /**
     * Predicts risk level for a sprint by analyzing saved metrics.
     *
     * @param sprintId the sprint identifier to analyze
     * @return result with sprintId, riskLevel (low/medium/high), factors, recommendation
     */
    Map<String, Object> predictRisk(String sprintId);

    /**
     * Records or lists factor correlations.
     *
     * @param factorA     first factor name
     * @param factorB     second factor name
     * @param correlation correlation coefficient (-1.0 to 1.0)
     * @param sampleSize  number of data points
     * @param description human-readable description of the correlation
     * @return result with correlation data and interpretation
     */
    Map<String, Object> correlateFactors(String factorA, String factorB,
                                          double correlation, int sampleSize, String description);
}
