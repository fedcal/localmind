package com.localmind.domain.mcp.port.in;

import java.util.Map;

/**
 * Input port for the standup-notes tool group.
 * Provides standup logging, history retrieval and status report generation.
 */
public interface StandupUseCase {

    /**
     * Logs a new standup entry with yesterday's work, today's plan and any blockers.
     *
     * @param yesterday what was done yesterday
     * @param today     what is planned for today
     * @param blockers  any blockers (nullable)
     * @return result with id, yesterday, today, blockers, date, itemCount
     */
    Map<String, Object> logStandup(String yesterday, String today, String blockers);

    /**
     * Retrieves standup history for the last N days.
     *
     * @param days number of days to look back
     * @return result with days, count, standups list
     */
    Map<String, Object> getHistory(int days);

    /**
     * Generates a status report aggregating standups over the last N days.
     *
     * @param days number of days to aggregate
     * @return result with period, totalStandups, blockerCount, summary
     */
    Map<String, Object> generateStatusReport(int days);
}
