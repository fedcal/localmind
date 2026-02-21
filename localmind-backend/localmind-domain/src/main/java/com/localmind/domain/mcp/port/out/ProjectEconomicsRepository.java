package com.localmind.domain.mcp.port.out;

import com.localmind.domain.mcp.model.CostEntry;
import com.localmind.domain.mcp.model.FeatureCost;
import com.localmind.domain.mcp.model.ProjectBudget;

import java.util.List;
import java.util.Optional;

/**
 * Output port for persisting project economics data (budgets, cost entries, feature costs).
 */
public interface ProjectEconomicsRepository {

    /**
     * Saves or updates a project budget.
     *
     * @param budget the project budget to persist
     * @return the saved project budget
     */
    ProjectBudget saveBudget(ProjectBudget budget);

    /**
     * Finds a project budget by project name.
     *
     * @param projectName the project name
     * @return optional containing the budget if found
     */
    Optional<ProjectBudget> findBudgetByProjectName(String projectName);

    /**
     * Saves a cost entry.
     *
     * @param costEntry the cost entry to persist
     * @return the saved cost entry
     */
    CostEntry saveCostEntry(CostEntry costEntry);

    /**
     * Finds all cost entries for a given project, ordered by creation date descending.
     *
     * @param projectName the project name
     * @return list of cost entries
     */
    List<CostEntry> findCostsByProjectName(String projectName);

    /**
     * Saves a feature cost entry.
     *
     * @param featureCost the feature cost to persist
     * @return the saved feature cost
     */
    FeatureCost saveFeatureCost(FeatureCost featureCost);

    /**
     * Finds all feature costs for a given project, ordered by creation date descending.
     *
     * @param projectName the project name
     * @return list of feature costs
     */
    List<FeatureCost> findFeatureCostsByProjectName(String projectName);
}
