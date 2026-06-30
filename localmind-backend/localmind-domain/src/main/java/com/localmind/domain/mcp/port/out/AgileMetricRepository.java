package com.localmind.domain.mcp.port.out;

import com.localmind.domain.mcp.model.AgileMetric;

import java.util.List;

/**
 * Output port for persisting agile metric results.
 */
public interface AgileMetricRepository {

    /**
     * Saves an agile metric result.
     *
     * @param agileMetric the metric to persist
     * @return the saved metric (with generated id if new)
     */
    AgileMetric save(AgileMetric agileMetric);

    /**
     * Finds all metrics of a given type, ordered by creation date descending.
     *
     * @param metricType the metric type to search for (velocity, burndown, cycle-time, etc.)
     * @return list of matching metrics
     */
    List<AgileMetric> findByMetricType(String metricType);
}
