package com.localmind.domain.mcp.port.in;

import java.util.Map;

/**
 * Input port for the insight-engine tool group.
 * Provides natural language insight queries, metric correlation,
 * trend explanation and health dashboard aggregation.
 */
public interface InsightEngineUseCase {

    /**
     * Queries an insight based on a natural language question.
     *
     * @param question the natural language question
     * @return insight result with confidence and suggested actions
     */
    Map<String, Object> queryInsight(String question);

    /**
     * Correlates a set of metrics over a time period.
     *
     * @param metricsJson JSON array of metric names
     * @param period      the time period for analysis (e.g. "last-sprint", "last-month")
     * @return correlation results between metric pairs
     */
    Map<String, Object> correlateMetrics(String metricsJson, String period);

    /**
     * Explains a trend observed in a specific metric.
     *
     * @param metric    the metric name
     * @param direction the trend direction (e.g. "increasing", "decreasing", "stable")
     * @param period    the time period for analysis
     * @return trend explanation with contributing factors
     */
    Map<String, Object> explainTrend(String metric, String direction, String period);

    /**
     * Generates an aggregated health dashboard.
     *
     * @return overall health status with area-level breakdowns
     */
    Map<String, Object> healthDashboard();
}
