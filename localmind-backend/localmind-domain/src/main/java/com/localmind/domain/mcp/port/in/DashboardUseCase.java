package com.localmind.domain.mcp.port.in;

import java.util.Map;

/**
 * Input port for the dashboard-api tool group.
 * Provides aggregated project overview, server status,
 * recent activity and project summary.
 */
public interface DashboardUseCase {

    /**
     * Returns an aggregated dashboard overview.
     *
     * @return overview with sprint, velocity, budget sections
     */
    Map<String, Object> getOverview();

    /**
     * Returns the status of MCP servers.
     *
     * @param serverName optional server name filter
     * @return server status information
     */
    Map<String, Object> getServerStatus(String serverName);

    /**
     * Returns recent activity entries.
     *
     * @param limit maximum number of activities to return
     * @return list of recent activities with count
     */
    Map<String, Object> getRecentActivity(int limit);

    /**
     * Returns a complete project summary.
     *
     * @param project the project name
     * @return comprehensive project summary with all areas
     */
    Map<String, Object> getProjectSummary(String project);
}
