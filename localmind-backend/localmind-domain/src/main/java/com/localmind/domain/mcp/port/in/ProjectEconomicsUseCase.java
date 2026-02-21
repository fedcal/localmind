package com.localmind.domain.mcp.port.in;

import java.util.Map;

/**
 * Input port for the project-economics tool group.
 * Provides budget management, cost logging, budget status, forecasting and per-feature cost tracking.
 */
public interface ProjectEconomicsUseCase {

    /**
     * Sets or updates the budget for a project.
     *
     * @param projectName the project name
     * @param totalBudget the total budget amount
     * @param currency    the currency code (defaults to EUR if null/blank)
     * @return save confirmation with budget details
     */
    Map<String, Object> setBudget(String projectName, double totalBudget, String currency);

    /**
     * Logs a cost entry for a project.
     *
     * @param projectName the project name
     * @param category    the cost category (e.g. development, infrastructure)
     * @param amount      the cost amount
     * @param description a description of the cost
     * @param date        the entry date (defaults to today if null/blank)
     * @return save confirmation with cost entry details
     */
    Map<String, Object> logCost(String projectName, String category, double amount,
                                 String description, String date);

    /**
     * Retrieves the budget status for a project, including total spent, remaining,
     * percentage used and category breakdown.
     *
     * @param projectName the project name
     * @return budget status with totalBudget, totalSpent, remaining, percentageUsed, categoryBreakdown
     */
    Map<String, Object> getBudgetStatus(String projectName);

    /**
     * Forecasts the budget for a project based on current spending rate.
     * Calculates daily burn rate, days until exhausted and projected end date.
     *
     * @param projectName the project name
     * @return forecast with dailyBurnRate, daysUntilExhausted, projectedEndDate
     */
    Map<String, Object> forecastBudget(String projectName);

    /**
     * Tracks or lists per-feature costs.
     * If featureId, hoursSpent and hourlyRate are provided, saves a new feature cost entry.
     * Otherwise, lists all feature costs for the project.
     *
     * @param featureId   the feature identifier (null to list all)
     * @param projectName the project name
     * @param hoursSpent  hours spent on the feature (0 to list all)
     * @param hourlyRate  hourly rate (0 to list all)
     * @param description a description of the feature cost
     * @param currency    the currency code (defaults to EUR if null/blank)
     * @return feature cost details or list of all feature costs
     */
    Map<String, Object> costPerFeature(String featureId, String projectName, double hoursSpent,
                                        double hourlyRate, String description, String currency);
}
