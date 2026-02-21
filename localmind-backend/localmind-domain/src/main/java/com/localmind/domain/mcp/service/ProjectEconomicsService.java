package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.CostEntry;
import com.localmind.domain.mcp.model.FeatureCost;
import com.localmind.domain.mcp.model.ProjectBudget;
import com.localmind.domain.mcp.port.in.ProjectEconomicsUseCase;
import com.localmind.domain.mcp.port.out.ProjectEconomicsRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Domain service implementing the project-economics tool group.
 * Provides budget management, cost logging, budget status, forecasting and per-feature cost tracking.
 * Pure Java, zero Spring dependencies - registered as a bean via DomainConfig.
 */
public class ProjectEconomicsService implements ProjectEconomicsUseCase {

    private final ProjectEconomicsRepository repository;

    public ProjectEconomicsService(ProjectEconomicsRepository repository) {
        this.repository = repository;
    }

    // ========================================================================
    // 1. setBudget
    // ========================================================================

    @Override
    public Map<String, Object> setBudget(String projectName, double totalBudget, String currency) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (projectName == null || projectName.isBlank()) {
            result.put("error", "Project name is required");
            return result;
        }

        if (totalBudget < 0) {
            result.put("error", "Total budget cannot be negative");
            return result;
        }

        String resolvedCurrency = (currency == null || currency.isBlank()) ? "EUR" : currency.toUpperCase();

        // Check if budget already exists for this project
        Optional<ProjectBudget> existing = repository.findBudgetByProjectName(projectName);

        ProjectBudget budget;
        if (existing.isPresent()) {
            // Update existing budget
            budget = ProjectBudget.builder()
                    .id(existing.get().getId())
                    .projectName(projectName)
                    .totalBudget(totalBudget)
                    .currency(resolvedCurrency)
                    .createdAt(existing.get().getCreatedAt())
                    .build();
        } else {
            // Create new budget
            budget = ProjectBudget.builder()
                    .id(UUID.randomUUID().toString())
                    .projectName(projectName)
                    .totalBudget(totalBudget)
                    .currency(resolvedCurrency)
                    .createdAt(Instant.now())
                    .build();
        }

        ProjectBudget saved = repository.saveBudget(budget);

        result.put("id", saved.getId());
        result.put("projectName", saved.getProjectName());
        result.put("totalBudget", saved.getTotalBudget());
        result.put("currency", saved.getCurrency());
        result.put("updated", existing.isPresent());
        result.put("message", existing.isPresent()
                ? "Budget updated for project '" + projectName + "'"
                : "Budget created for project '" + projectName + "'");
        return result;
    }

    // ========================================================================
    // 2. logCost
    // ========================================================================

    @Override
    public Map<String, Object> logCost(String projectName, String category, double amount,
                                        String description, String date) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (projectName == null || projectName.isBlank()) {
            result.put("error", "Project name is required");
            return result;
        }

        if (amount <= 0) {
            result.put("error", "Amount must be greater than zero");
            return result;
        }

        String resolvedDate = (date == null || date.isBlank())
                ? LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                : date;

        String resolvedCategory = (category == null || category.isBlank()) ? "general" : category;

        CostEntry costEntry = CostEntry.builder()
                .id(UUID.randomUUID().toString())
                .projectName(projectName)
                .category(resolvedCategory)
                .amount(amount)
                .description(description)
                .entryDate(resolvedDate)
                .createdAt(Instant.now())
                .build();

        CostEntry saved = repository.saveCostEntry(costEntry);

        result.put("id", saved.getId());
        result.put("projectName", saved.getProjectName());
        result.put("category", saved.getCategory());
        result.put("amount", saved.getAmount());
        result.put("description", saved.getDescription());
        result.put("entryDate", saved.getEntryDate());
        result.put("message", "Cost entry logged for project '" + projectName + "'");
        return result;
    }

    // ========================================================================
    // 3. getBudgetStatus
    // ========================================================================

    @Override
    public Map<String, Object> getBudgetStatus(String projectName) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (projectName == null || projectName.isBlank()) {
            result.put("error", "Project name is required");
            return result;
        }

        Optional<ProjectBudget> optionalBudget = repository.findBudgetByProjectName(projectName);
        if (optionalBudget.isEmpty()) {
            result.put("error", "No budget found for project '" + projectName + "'");
            return result;
        }

        ProjectBudget budget = optionalBudget.get();
        List<CostEntry> costs = repository.findCostsByProjectName(projectName);

        double totalSpent = 0.0;
        Map<String, Double> categoryBreakdown = new LinkedHashMap<>();

        for (CostEntry cost : costs) {
            totalSpent += cost.getAmount();
            String cat = cost.getCategory() != null ? cost.getCategory() : "general";
            categoryBreakdown.merge(cat, cost.getAmount(), Double::sum);
        }

        double remaining = budget.getTotalBudget() - totalSpent;
        double percentageUsed = budget.getTotalBudget() > 0
                ? Math.round((totalSpent / budget.getTotalBudget()) * 10000.0) / 100.0
                : 0.0;

        result.put("projectName", projectName);
        result.put("totalBudget", budget.getTotalBudget());
        result.put("currency", budget.getCurrency());
        result.put("totalSpent", Math.round(totalSpent * 100.0) / 100.0);
        result.put("remaining", Math.round(remaining * 100.0) / 100.0);
        result.put("percentageUsed", percentageUsed);
        result.put("costEntries", costs.size());
        result.put("categoryBreakdown", categoryBreakdown);
        return result;
    }

    // ========================================================================
    // 4. forecastBudget
    // ========================================================================

    @Override
    public Map<String, Object> forecastBudget(String projectName) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (projectName == null || projectName.isBlank()) {
            result.put("error", "Project name is required");
            return result;
        }

        Optional<ProjectBudget> optionalBudget = repository.findBudgetByProjectName(projectName);
        if (optionalBudget.isEmpty()) {
            result.put("error", "No budget found for project '" + projectName + "'");
            return result;
        }

        ProjectBudget budget = optionalBudget.get();
        List<CostEntry> costs = repository.findCostsByProjectName(projectName);

        if (costs.isEmpty()) {
            result.put("projectName", projectName);
            result.put("totalBudget", budget.getTotalBudget());
            result.put("currency", budget.getCurrency());
            result.put("totalSpent", 0.0);
            result.put("remaining", budget.getTotalBudget());
            result.put("dailyBurnRate", 0.0);
            result.put("daysUntilExhausted", -1);
            result.put("projectedEndDate", "N/A");
            result.put("message", "No cost entries found. Cannot forecast without spending data.");
            return result;
        }

        double totalSpent = 0.0;
        for (CostEntry cost : costs) {
            totalSpent += cost.getAmount();
        }

        double remaining = budget.getTotalBudget() - totalSpent;

        // Calculate days since first cost entry
        Instant earliestCreatedAt = costs.stream()
                .map(CostEntry::getCreatedAt)
                .filter(java.util.Objects::nonNull)
                .min(Instant::compareTo)
                .orElse(Instant.now());

        long daysSinceFirst = ChronoUnit.DAYS.between(earliestCreatedAt, Instant.now());
        if (daysSinceFirst < 1) {
            daysSinceFirst = 1; // Minimum 1 day to avoid division by zero
        }

        double dailyBurnRate = Math.round((totalSpent / daysSinceFirst) * 100.0) / 100.0;

        long daysUntilExhausted;
        String projectedEndDate;

        if (dailyBurnRate <= 0 || remaining <= 0) {
            daysUntilExhausted = remaining <= 0 ? 0 : -1;
            projectedEndDate = remaining <= 0 ? "Budget already exhausted" : "N/A";
        } else {
            daysUntilExhausted = Math.round(remaining / dailyBurnRate);
            LocalDate endDate = LocalDate.now().plusDays(daysUntilExhausted);
            projectedEndDate = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }

        result.put("projectName", projectName);
        result.put("totalBudget", budget.getTotalBudget());
        result.put("currency", budget.getCurrency());
        result.put("totalSpent", Math.round(totalSpent * 100.0) / 100.0);
        result.put("remaining", Math.round(remaining * 100.0) / 100.0);
        result.put("daysSinceFirstEntry", daysSinceFirst);
        result.put("dailyBurnRate", dailyBurnRate);
        result.put("daysUntilExhausted", daysUntilExhausted);
        result.put("projectedEndDate", projectedEndDate);
        return result;
    }

    // ========================================================================
    // 5. costPerFeature
    // ========================================================================

    @Override
    public Map<String, Object> costPerFeature(String featureId, String projectName, double hoursSpent,
                                               double hourlyRate, String description, String currency) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (projectName == null || projectName.isBlank()) {
            result.put("error", "Project name is required");
            return result;
        }

        // If featureId, hoursSpent and hourlyRate are provided, save a new feature cost
        if (featureId != null && !featureId.isBlank() && hoursSpent > 0 && hourlyRate > 0) {
            String resolvedCurrency = (currency == null || currency.isBlank()) ? "EUR" : currency.toUpperCase();
            double totalCost = Math.round(hoursSpent * hourlyRate * 100.0) / 100.0;

            FeatureCost featureCost = FeatureCost.builder()
                    .id(UUID.randomUUID().toString())
                    .featureId(featureId)
                    .projectName(projectName)
                    .hoursSpent(hoursSpent)
                    .hourlyRate(hourlyRate)
                    .totalCost(totalCost)
                    .description(description)
                    .currency(resolvedCurrency)
                    .createdAt(Instant.now())
                    .build();

            FeatureCost saved = repository.saveFeatureCost(featureCost);

            result.put("id", saved.getId());
            result.put("featureId", saved.getFeatureId());
            result.put("projectName", saved.getProjectName());
            result.put("hoursSpent", saved.getHoursSpent());
            result.put("hourlyRate", saved.getHourlyRate());
            result.put("totalCost", saved.getTotalCost());
            result.put("description", saved.getDescription());
            result.put("currency", saved.getCurrency());
            result.put("message", "Feature cost saved for feature '" + featureId + "' in project '" + projectName + "'");
            return result;
        }

        // Otherwise, list all feature costs for the project
        List<FeatureCost> featureCosts = repository.findFeatureCostsByProjectName(projectName);

        if (featureCosts.isEmpty()) {
            result.put("projectName", projectName);
            result.put("featureCount", 0);
            result.put("features", List.of());
            result.put("totalCostAllFeatures", 0.0);
            result.put("message", "No feature costs found for project '" + projectName + "'");
            return result;
        }

        List<Map<String, Object>> featureList = new ArrayList<>();
        double totalCostAllFeatures = 0.0;

        for (FeatureCost fc : featureCosts) {
            Map<String, Object> featureMap = new LinkedHashMap<>();
            featureMap.put("id", fc.getId());
            featureMap.put("featureId", fc.getFeatureId());
            featureMap.put("hoursSpent", fc.getHoursSpent());
            featureMap.put("hourlyRate", fc.getHourlyRate());
            featureMap.put("totalCost", fc.getTotalCost());
            featureMap.put("description", fc.getDescription());
            featureMap.put("currency", fc.getCurrency());
            featureList.add(featureMap);
            totalCostAllFeatures += fc.getTotalCost();
        }

        result.put("projectName", projectName);
        result.put("featureCount", featureCosts.size());
        result.put("features", featureList);
        result.put("totalCostAllFeatures", Math.round(totalCostAllFeatures * 100.0) / 100.0);
        return result;
    }

    // ========================================================================
    // JSON Serialization (minimal, no external deps)
    // ========================================================================

    private String mapToJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(escapeJson(entry.getKey())).append("\":");
            sb.append(valueToJson(entry.getValue()));
        }
        sb.append("}");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String valueToJson(Object value) {
        if (value == null) return "null";
        if (value instanceof String) return "\"" + escapeJson((String) value) + "\"";
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        if (value instanceof Map) return mapToJson((Map<String, Object>) value);
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(valueToJson(list.get(i)));
            }
            sb.append("]");
            return sb.toString();
        }
        if (value instanceof Set) {
            return valueToJson(new ArrayList<>((Set<?>) value));
        }
        return "\"" + escapeJson(value.toString()) + "\"";
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
