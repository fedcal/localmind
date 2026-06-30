package com.localmind.domain.common.port.in;

import com.localmind.domain.common.model.AnalyticsOverview;
import java.time.Instant;

/**
 * Input port for the analytics dashboard feature.
 * Provides aggregated usage, cost, and document statistics.
 */
public interface AnalyticsUseCase {

    /**
     * Computes analytics overview for the given time range.
     *
     * @param from start of the time range (inclusive)
     * @param to   end of the time range (inclusive)
     * @return aggregated analytics data
     */
    AnalyticsOverview getOverview(Instant from, Instant to);
}
