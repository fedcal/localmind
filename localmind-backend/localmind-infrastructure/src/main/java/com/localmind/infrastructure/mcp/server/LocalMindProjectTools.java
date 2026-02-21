package com.localmind.infrastructure.mcp.server;

import com.localmind.domain.mcp.port.in.AgileMetricsUseCase;
import com.localmind.domain.mcp.port.in.ProjectEconomicsUseCase;
import com.localmind.domain.mcp.port.in.RetrospectiveUseCase;
import com.localmind.domain.mcp.port.in.ScrumBoardUseCase;
import com.localmind.domain.mcp.port.in.TimeTrackingUseCase;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "localmind.mcp.server.enabled", havingValue = "true", matchIfMissing = true)
public class LocalMindProjectTools {

    private final ScrumBoardUseCase scrumBoardUseCase;
    private final AgileMetricsUseCase agileMetricsUseCase;
    private final TimeTrackingUseCase timeTrackingUseCase;
    private final ProjectEconomicsUseCase projectEconomicsUseCase;
    private final RetrospectiveUseCase retrospectiveUseCase;

    public LocalMindProjectTools(ScrumBoardUseCase scrumBoardUseCase,
                                  AgileMetricsUseCase agileMetricsUseCase,
                                  TimeTrackingUseCase timeTrackingUseCase,
                                  ProjectEconomicsUseCase projectEconomicsUseCase,
                                  RetrospectiveUseCase retrospectiveUseCase) {
        this.scrumBoardUseCase = scrumBoardUseCase;
        this.agileMetricsUseCase = agileMetricsUseCase;
        this.timeTrackingUseCase = timeTrackingUseCase;
        this.projectEconomicsUseCase = projectEconomicsUseCase;
        this.retrospectiveUseCase = retrospectiveUseCase;
    }

    // ==================== SCRUM BOARD ====================

    @Tool(description = "Create a new sprint with name, date range and goals. "
            + "Returns the created sprint with id, name, startDate, endDate, goals.")
    public Map<String, Object> createSprint(
            @ToolParam(description = "Sprint name (e.g. 'Sprint 12')") String name,
            @ToolParam(description = "Start date in ISO format YYYY-MM-DD") String startDate,
            @ToolParam(description = "End date in ISO format YYYY-MM-DD") String endDate,
            @ToolParam(description = "List of sprint goals") List<String> goals) {
        return scrumBoardUseCase.createSprint(name, startDate, endDate, goals);
    }

    @Tool(description = "Create a new user story, optionally assigning it to a sprint. "
            + "Returns the created story with id, title, storyPoints, priority.")
    public Map<String, Object> createStory(
            @ToolParam(description = "Story title") String title,
            @ToolParam(description = "Story description") String description,
            @ToolParam(description = "Acceptance criteria as list of strings") List<String> acceptanceCriteria,
            @ToolParam(description = "Story point estimate") int storyPoints,
            @ToolParam(description = "Priority level: high, medium, low") String priority,
            @ToolParam(description = "Sprint ID to assign to (null for backlog)") String sprintId) {
        return scrumBoardUseCase.createStory(title, description, acceptanceCriteria, storyPoints, priority, sprintId);
    }

    @Tool(description = "Create a new task under a user story. Task starts with 'todo' status. "
            + "Returns the created task with id, title, storyId, assignee, status.")
    public Map<String, Object> createTask(
            @ToolParam(description = "Task title") String title,
            @ToolParam(description = "Task description") String description,
            @ToolParam(description = "ID of the parent user story") String storyId,
            @ToolParam(description = "Person assigned to the task (optional)") String assignee) {
        return scrumBoardUseCase.createTask(title, description, storyId, assignee);
    }

    @Tool(description = "Update the status of a task on the scrum board. "
            + "Valid statuses: todo, in_progress, in_review, done, blocked.")
    public Map<String, Object> updateTaskStatus(
            @ToolParam(description = "ID of the task to update") String taskId,
            @ToolParam(description = "New status: todo, in_progress, in_review, done, blocked") String status) {
        return scrumBoardUseCase.updateTaskStatus(taskId, status);
    }

    @Tool(description = "Retrieve a sprint with its user stories and tasks. "
            + "Returns sprint details, stories array with nested tasks.")
    public Map<String, Object> getSprint(
            @ToolParam(description = "Sprint ID to retrieve") String sprintId) {
        return scrumBoardUseCase.getSprint(sprintId);
    }

    @Tool(description = "Get the sprint board with tasks organized in columns by status "
            + "(todo, in_progress, in_review, done, blocked). Returns sprint info and columns.")
    public Map<String, Object> sprintBoard(
            @ToolParam(description = "Sprint ID (defaults to active sprint if null)") String sprintId) {
        return scrumBoardUseCase.sprintBoard(sprintId);
    }

    @Tool(description = "Get the product backlog: user stories not assigned to any sprint. "
            + "Returns array of unassigned stories.")
    public Map<String, Object> getBacklog() {
        return scrumBoardUseCase.getBacklog();
    }

    // ==================== AGILE METRICS ====================

    @Tool(description = "Calculate team velocity from sprint completion data. "
            + "Returns averageVelocity, trend (improving/declining/stable), "
            + "highestVelocity, lowestVelocity, and per-sprint details.")
    public Map<String, Object> calculateVelocity(
            @ToolParam(description = "List of sprint data. Each map: 'name' (string), "
                    + "'completedPoints' (number), 'totalPoints' (number)")
            List<Map<String, Object>> sprints) {
        return agileMetricsUseCase.calculateVelocity(sprints);
    }

    @Tool(description = "Generate burndown chart data with ideal vs actual lines. "
            + "Returns totalPoints, status (ahead/behind/on-track), burn rates, "
            + "and ideal/actual burndown arrays.")
    public Map<String, Object> generateBurndown(
            @ToolParam(description = "Total story points in the sprint") int totalPoints,
            @ToolParam(description = "Number of working days in the sprint") int sprintDays,
            @ToolParam(description = "Daily progress. Each map: 'date' (string), 'remaining' (number)")
            List<Map<String, Object>> dailyProgress) {
        return agileMetricsUseCase.generateBurndown(totalPoints, sprintDays, dailyProgress);
    }

    @Tool(description = "Calculate cycle time statistics from task start/completion dates. "
            + "Returns taskCount, averageDays, medianDays, p95Days, minDays, maxDays.")
    public Map<String, Object> calculateCycleTime(
            @ToolParam(description = "List of tasks. Each map: 'startedAt' (ISO 8601), 'completedAt' (ISO 8601)")
            List<Map<String, Object>> tasks) {
        return agileMetricsUseCase.calculateCycleTime(tasks);
    }

    @Tool(description = "Monte Carlo simulation to forecast how many sprints needed to complete "
            + "remaining work. Runs 1000 simulations. Returns p50, p85, p95 forecasts.")
    public Map<String, Object> forecastCompletion(
            @ToolParam(description = "Remaining story points to complete") int remainingPoints,
            @ToolParam(description = "Historical velocity values (points per sprint)")
            List<Integer> velocityHistory) {
        return agileMetricsUseCase.forecastCompletion(remainingPoints, velocityHistory);
    }

    @Tool(description = "Predict sprint risk level based on historical velocity and completion data. "
            + "Returns riskLevel (low/medium/high) and contributing factors.")
    public Map<String, Object> predictRisk(
            @ToolParam(description = "Sprint ID to analyze") String sprintId) {
        return agileMetricsUseCase.predictRisk(sprintId);
    }

    @Tool(description = "Record or view correlations between velocity and external factors. "
            + "If all parameters provided, saves correlation. Otherwise lists all correlations.")
    public Map<String, Object> correlateFactors(
            @ToolParam(description = "First factor name (e.g. 'team-size')") String factorA,
            @ToolParam(description = "Second factor name (e.g. 'velocity')") String factorB,
            @ToolParam(description = "Correlation coefficient (-1 to 1)") double correlation,
            @ToolParam(description = "Number of data points") int sampleSize,
            @ToolParam(description = "Description of the correlation") String description) {
        return agileMetricsUseCase.correlateFactors(factorA, factorB, correlation, sampleSize, description);
    }

    // ==================== TIME TRACKING ====================

    @Tool(description = "Start a timer for a task to track time spent. "
            + "Returns the active timer with taskId, startTime, description.")
    public Map<String, Object> startTimer(
            @ToolParam(description = "Task identifier") String taskId,
            @ToolParam(description = "Optional description of the work") String description) {
        return timeTrackingUseCase.startTimer(taskId, description);
    }

    @Tool(description = "Stop the active timer and save as a time entry. "
            + "Returns the completed entry with durationMinutes, taskId, date.")
    public Map<String, Object> stopTimer() {
        return timeTrackingUseCase.stopTimer();
    }

    @Tool(description = "Manually log time spent on a task. "
            + "Returns the created time entry with taskId, durationMinutes, date.")
    public Map<String, Object> logTime(
            @ToolParam(description = "Task identifier") String taskId,
            @ToolParam(description = "Duration in minutes") int durationMinutes,
            @ToolParam(description = "Description of the work") String description,
            @ToolParam(description = "Date in YYYY-MM-DD format (default: today)") String date) {
        return timeTrackingUseCase.logTime(taskId, durationMinutes, description, date);
    }

    @Tool(description = "Retrieve time entries and totals for a date range. "
            + "Returns totalMinutes, totalFormatted (hours:minutes), entryCount, entries.")
    public Map<String, Object> getTimesheet(
            @ToolParam(description = "Start date YYYY-MM-DD") String startDate,
            @ToolParam(description = "End date YYYY-MM-DD") String endDate,
            @ToolParam(description = "User ID (default: 'default')") String userId) {
        return timeTrackingUseCase.getTimesheet(startDate, endDate, userId);
    }

    @Tool(description = "Detect anomalous time tracking patterns like excessive hours, "
            + "weekend work, or duplicate entries. Returns anomalies array with type and details.")
    public Map<String, Object> detectAnomalies(
            @ToolParam(description = "User ID (default: 'default')") String userId,
            @ToolParam(description = "Number of days to analyze (default: 30)") int days) {
        return timeTrackingUseCase.detectAnomalies(userId, days);
    }

    @Tool(description = "Compare time estimates with actual time spent on a task. "
            + "If estimateMinutes provided, saves the estimate. Returns variance and status.")
    public Map<String, Object> estimateVsActual(
            @ToolParam(description = "Task identifier") String taskId,
            @ToolParam(description = "Estimate in minutes (0 to skip saving, just compare)") int estimateMinutes,
            @ToolParam(description = "Description of the estimate") String description) {
        return timeTrackingUseCase.estimateVsActual(taskId, estimateMinutes, description);
    }

    // ==================== PROJECT ECONOMICS ====================

    @Tool(description = "Set or update the total budget for a project. "
            + "Returns the budget with projectName, totalBudget, currency.")
    public Map<String, Object> setBudget(
            @ToolParam(description = "Project name") String projectName,
            @ToolParam(description = "Total budget amount") double totalBudget,
            @ToolParam(description = "Currency code (default: EUR)") String currency) {
        return projectEconomicsUseCase.setBudget(projectName, totalBudget, currency);
    }

    @Tool(description = "Log a cost entry against a project budget. "
            + "Categories: development, infrastructure, design, testing, management, other.")
    public Map<String, Object> logCost(
            @ToolParam(description = "Project name") String projectName,
            @ToolParam(description = "Cost category") String category,
            @ToolParam(description = "Cost amount") double amount,
            @ToolParam(description = "Cost description") String costDescription,
            @ToolParam(description = "Date YYYY-MM-DD (default: today)") String date) {
        return projectEconomicsUseCase.logCost(projectName, category, amount, costDescription, date);
    }

    @Tool(description = "Get current budget status including total, spent, remaining, "
            + "percentage used, and breakdown by category.")
    public Map<String, Object> getBudgetStatus(
            @ToolParam(description = "Project name") String projectName) {
        return projectEconomicsUseCase.getBudgetStatus(projectName);
    }

    @Tool(description = "Forecast when the project budget will be exhausted based on "
            + "historical burn rate. Returns daysUntilExhausted, dailyBurnRate, projectedEndDate.")
    public Map<String, Object> forecastBudget(
            @ToolParam(description = "Project name") String projectName) {
        return projectEconomicsUseCase.forecastBudget(projectName);
    }

    @Tool(description = "Track cost per feature/ticket. If hoursSpent and hourlyRate provided, "
            + "saves the feature cost. Otherwise lists all feature costs for the project.")
    public Map<String, Object> costPerFeature(
            @ToolParam(description = "Feature or ticket ID (null to list all)") String featureId,
            @ToolParam(description = "Project name") String projectName,
            @ToolParam(description = "Hours spent (0 to list instead of save)") double hoursSpent,
            @ToolParam(description = "Hourly rate") double hourlyRate,
            @ToolParam(description = "Work description") String featureDescription,
            @ToolParam(description = "Currency code (default: EUR)") String currency) {
        return projectEconomicsUseCase.costPerFeature(featureId, projectName, hoursSpent, hourlyRate, featureDescription, currency);
    }

    // ==================== RETROSPECTIVE MANAGER ====================

    @Tool(description = "Create a new retrospective session. "
            + "Formats: mad-sad-glad, 4ls, start-stop-continue.")
    public Map<String, Object> createRetro(
            @ToolParam(description = "Sprint ID (optional)") String sprintId,
            @ToolParam(description = "Retro format: mad-sad-glad, 4ls, start-stop-continue") String format) {
        return retrospectiveUseCase.createRetro(sprintId, format);
    }

    @Tool(description = "Add an item to a retrospective in a specific category. "
            + "Category must match the retro format (e.g. 'mad', 'sad', 'glad').")
    public Map<String, Object> addRetroItem(
            @ToolParam(description = "Retrospective ID") String retroId,
            @ToolParam(description = "Category matching the retro format") String category,
            @ToolParam(description = "Item content") String content) {
        return retrospectiveUseCase.addRetroItem(retroId, category, content);
    }

    @Tool(description = "Vote on a retrospective item to increase its priority. "
            + "Returns the updated item with incremented vote count.")
    public Map<String, Object> voteRetroItem(
            @ToolParam(description = "Retro item ID to vote on") String itemId) {
        return retrospectiveUseCase.voteRetroItem(itemId);
    }

    @Tool(description = "Generate action items from the top-voted retrospective items. "
            + "Returns array of action items with description, assignee, dueDate, status.")
    public Map<String, Object> generateActionItems(
            @ToolParam(description = "Retrospective ID") String retroId,
            @ToolParam(description = "Number of top-voted items to convert (default 3)") int topN) {
        return retrospectiveUseCase.generateActionItems(retroId, topN);
    }

    @Tool(description = "Retrieve the complete retrospective with all items and action items. "
            + "Returns retro details, items sorted by votes, and action items.")
    public Map<String, Object> getRetro(
            @ToolParam(description = "Retrospective ID") String retroId) {
        return retrospectiveUseCase.getRetro(retroId);
    }

    @Tool(description = "Analyze recurring themes and patterns across all retrospectives. "
            + "Returns patterns with occurrence count and retro IDs where they appeared.")
    public Map<String, Object> detectPatterns() {
        return retrospectiveUseCase.detectPatterns();
    }

    @Tool(description = "Get auto-generated retrospective item suggestions based on "
            + "common themes like communication, process, technical debt.")
    public Map<String, Object> suggestItems(
            @ToolParam(description = "Maximum number of suggestions (default 10)") int limit) {
        return retrospectiveUseCase.suggestItems(limit);
    }
}
