package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.CostEntry;
import com.localmind.domain.mcp.model.FeatureCost;
import com.localmind.domain.mcp.model.ProjectBudget;
import com.localmind.domain.mcp.port.out.ProjectEconomicsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProjectEconomicsServiceTest {

    @Mock
    private ProjectEconomicsRepository repository;

    private ProjectEconomicsService service;

    @BeforeEach
    void setUp() {
        service = new ProjectEconomicsService(repository);

        when(repository.saveBudget(any(ProjectBudget.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.saveCostEntry(any(CostEntry.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.saveFeatureCost(any(FeatureCost.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    // ========== 1. setBudget Tests ==========

    @Test
    void setBudget_withValidData_createsBudget() {
        when(repository.findBudgetByProjectName("MyProject")).thenReturn(Optional.empty());

        Map<String, Object> result = service.setBudget("MyProject", 50000.0, "EUR");

        assertThat(result.get("projectName")).isEqualTo("MyProject");
        assertThat(result.get("totalBudget")).isEqualTo(50000.0);
        assertThat(result.get("currency")).isEqualTo("EUR");
        assertThat(result.get("updated")).isEqualTo(false);
        assertThat((String) result.get("message")).contains("Budget created");
    }

    @Test
    void setBudget_withExistingBudget_updatesBudget() {
        ProjectBudget existing = ProjectBudget.builder()
                .id("existing-id")
                .projectName("MyProject")
                .totalBudget(30000.0)
                .currency("EUR")
                .createdAt(Instant.now())
                .build();
        when(repository.findBudgetByProjectName("MyProject")).thenReturn(Optional.of(existing));

        Map<String, Object> result = service.setBudget("MyProject", 50000.0, "USD");

        assertThat(result.get("totalBudget")).isEqualTo(50000.0);
        assertThat(result.get("currency")).isEqualTo("USD");
        assertThat(result.get("updated")).isEqualTo(true);
        assertThat((String) result.get("message")).contains("Budget updated");

        ArgumentCaptor<ProjectBudget> captor = ArgumentCaptor.forClass(ProjectBudget.class);
        verify(repository).saveBudget(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo("existing-id");
    }

    @Test
    void setBudget_withNullProjectName_returnsError() {
        Map<String, Object> result = service.setBudget(null, 50000.0, "EUR");

        assertThat(result.get("error")).isEqualTo("Project name is required");
        verify(repository, never()).saveBudget(any());
    }

    @Test
    void setBudget_withBlankProjectName_returnsError() {
        Map<String, Object> result = service.setBudget("  ", 50000.0, "EUR");

        assertThat(result.get("error")).isEqualTo("Project name is required");
        verify(repository, never()).saveBudget(any());
    }

    @Test
    void setBudget_withNegativeBudget_returnsError() {
        Map<String, Object> result = service.setBudget("MyProject", -100.0, "EUR");

        assertThat(result.get("error")).isEqualTo("Total budget cannot be negative");
        verify(repository, never()).saveBudget(any());
    }

    @Test
    void setBudget_withNullCurrency_defaultsToEUR() {
        when(repository.findBudgetByProjectName("MyProject")).thenReturn(Optional.empty());

        Map<String, Object> result = service.setBudget("MyProject", 50000.0, null);

        assertThat(result.get("currency")).isEqualTo("EUR");
    }

    @Test
    void setBudget_withBlankCurrency_defaultsToEUR() {
        when(repository.findBudgetByProjectName("MyProject")).thenReturn(Optional.empty());

        Map<String, Object> result = service.setBudget("MyProject", 50000.0, "");

        assertThat(result.get("currency")).isEqualTo("EUR");
    }

    @Test
    void setBudget_savesToRepository() {
        when(repository.findBudgetByProjectName("TestProject")).thenReturn(Optional.empty());

        service.setBudget("TestProject", 10000.0, "GBP");

        ArgumentCaptor<ProjectBudget> captor = ArgumentCaptor.forClass(ProjectBudget.class);
        verify(repository).saveBudget(captor.capture());
        ProjectBudget saved = captor.getValue();
        assertThat(saved.getProjectName()).isEqualTo("TestProject");
        assertThat(saved.getTotalBudget()).isEqualTo(10000.0);
        assertThat(saved.getCurrency()).isEqualTo("GBP");
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    // ========== 2. logCost Tests ==========

    @Test
    void logCost_withValidData_logsCostEntry() {
        Map<String, Object> result = service.logCost("MyProject", "development", 500.0,
                "Frontend work", "2026-02-12");

        assertThat(result.get("projectName")).isEqualTo("MyProject");
        assertThat(result.get("category")).isEqualTo("development");
        assertThat(result.get("amount")).isEqualTo(500.0);
        assertThat(result.get("description")).isEqualTo("Frontend work");
        assertThat(result.get("entryDate")).isEqualTo("2026-02-12");
        assertThat((String) result.get("message")).contains("Cost entry logged");
    }

    @Test
    void logCost_withNullProjectName_returnsError() {
        Map<String, Object> result = service.logCost(null, "development", 500.0, "desc", "2026-02-12");

        assertThat(result.get("error")).isEqualTo("Project name is required");
        verify(repository, never()).saveCostEntry(any());
    }

    @Test
    void logCost_withZeroAmount_returnsError() {
        Map<String, Object> result = service.logCost("MyProject", "development", 0.0, "desc", "2026-02-12");

        assertThat(result.get("error")).isEqualTo("Amount must be greater than zero");
        verify(repository, never()).saveCostEntry(any());
    }

    @Test
    void logCost_withNegativeAmount_returnsError() {
        Map<String, Object> result = service.logCost("MyProject", "development", -50.0, "desc", "2026-02-12");

        assertThat(result.get("error")).isEqualTo("Amount must be greater than zero");
        verify(repository, never()).saveCostEntry(any());
    }

    @Test
    void logCost_withNullDate_defaultsToToday() {
        Map<String, Object> result = service.logCost("MyProject", "infra", 100.0, "Server cost", null);

        assertThat(result.get("entryDate")).isNotNull();
        assertThat((String) result.get("entryDate")).matches("\\d{4}-\\d{2}-\\d{2}");
    }

    @Test
    void logCost_withBlankCategory_defaultsToGeneral() {
        Map<String, Object> result = service.logCost("MyProject", "", 100.0, "misc", "2026-02-12");

        assertThat(result.get("category")).isEqualTo("general");
    }

    @Test
    void logCost_withNullCategory_defaultsToGeneral() {
        Map<String, Object> result = service.logCost("MyProject", null, 100.0, "misc", "2026-02-12");

        assertThat(result.get("category")).isEqualTo("general");
    }

    @Test
    void logCost_savesToRepository() {
        service.logCost("TestProject", "testing", 250.0, "QA work", "2026-01-15");

        ArgumentCaptor<CostEntry> captor = ArgumentCaptor.forClass(CostEntry.class);
        verify(repository).saveCostEntry(captor.capture());
        CostEntry saved = captor.getValue();
        assertThat(saved.getProjectName()).isEqualTo("TestProject");
        assertThat(saved.getCategory()).isEqualTo("testing");
        assertThat(saved.getAmount()).isEqualTo(250.0);
        assertThat(saved.getDescription()).isEqualTo("QA work");
        assertThat(saved.getEntryDate()).isEqualTo("2026-01-15");
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    // ========== 3. getBudgetStatus Tests ==========

    @Test
    void getBudgetStatus_withBudgetAndCosts_returnsCorrectStatus() {
        ProjectBudget budget = ProjectBudget.builder()
                .id("budget-1")
                .projectName("MyProject")
                .totalBudget(10000.0)
                .currency("EUR")
                .createdAt(Instant.now())
                .build();
        when(repository.findBudgetByProjectName("MyProject")).thenReturn(Optional.of(budget));

        List<CostEntry> costs = List.of(
                CostEntry.builder().id("c1").projectName("MyProject").category("development")
                        .amount(3000.0).entryDate("2026-01-01").createdAt(Instant.now()).build(),
                CostEntry.builder().id("c2").projectName("MyProject").category("infra")
                        .amount(1500.0).entryDate("2026-01-15").createdAt(Instant.now()).build(),
                CostEntry.builder().id("c3").projectName("MyProject").category("development")
                        .amount(2000.0).entryDate("2026-02-01").createdAt(Instant.now()).build()
        );
        when(repository.findCostsByProjectName("MyProject")).thenReturn(costs);

        Map<String, Object> result = service.getBudgetStatus("MyProject");

        assertThat(result.get("projectName")).isEqualTo("MyProject");
        assertThat(result.get("totalBudget")).isEqualTo(10000.0);
        assertThat(result.get("currency")).isEqualTo("EUR");
        assertThat(result.get("totalSpent")).isEqualTo(6500.0);
        assertThat(result.get("remaining")).isEqualTo(3500.0);
        assertThat(result.get("percentageUsed")).isEqualTo(65.0);
        assertThat(result.get("costEntries")).isEqualTo(3);

        @SuppressWarnings("unchecked")
        Map<String, Double> breakdown = (Map<String, Double>) result.get("categoryBreakdown");
        assertThat(breakdown.get("development")).isEqualTo(5000.0);
        assertThat(breakdown.get("infra")).isEqualTo(1500.0);
    }

    @Test
    void getBudgetStatus_withNullProjectName_returnsError() {
        Map<String, Object> result = service.getBudgetStatus(null);

        assertThat(result.get("error")).isEqualTo("Project name is required");
    }

    @Test
    void getBudgetStatus_withNoBudget_returnsError() {
        when(repository.findBudgetByProjectName("Unknown")).thenReturn(Optional.empty());

        Map<String, Object> result = service.getBudgetStatus("Unknown");

        assertThat((String) result.get("error")).contains("No budget found");
    }

    @Test
    void getBudgetStatus_withNoCosts_returnsZeroSpent() {
        ProjectBudget budget = ProjectBudget.builder()
                .id("budget-1")
                .projectName("EmptyProject")
                .totalBudget(5000.0)
                .currency("USD")
                .createdAt(Instant.now())
                .build();
        when(repository.findBudgetByProjectName("EmptyProject")).thenReturn(Optional.of(budget));
        when(repository.findCostsByProjectName("EmptyProject")).thenReturn(List.of());

        Map<String, Object> result = service.getBudgetStatus("EmptyProject");

        assertThat(result.get("totalSpent")).isEqualTo(0.0);
        assertThat(result.get("remaining")).isEqualTo(5000.0);
        assertThat(result.get("percentageUsed")).isEqualTo(0.0);
        assertThat(result.get("costEntries")).isEqualTo(0);
    }

    @Test
    void getBudgetStatus_withNullCategory_usesGeneralAsDefault() {
        ProjectBudget budget = ProjectBudget.builder()
                .id("budget-1")
                .projectName("MyProject")
                .totalBudget(10000.0)
                .currency("EUR")
                .createdAt(Instant.now())
                .build();
        when(repository.findBudgetByProjectName("MyProject")).thenReturn(Optional.of(budget));

        List<CostEntry> costs = List.of(
                CostEntry.builder().id("c1").projectName("MyProject").category(null)
                        .amount(100.0).entryDate("2026-01-01").createdAt(Instant.now()).build()
        );
        when(repository.findCostsByProjectName("MyProject")).thenReturn(costs);

        Map<String, Object> result = service.getBudgetStatus("MyProject");

        @SuppressWarnings("unchecked")
        Map<String, Double> breakdown = (Map<String, Double>) result.get("categoryBreakdown");
        assertThat(breakdown).containsKey("general");
        assertThat(breakdown.get("general")).isEqualTo(100.0);
    }

    // ========== 4. forecastBudget Tests ==========

    @Test
    void forecastBudget_withNullProjectName_returnsError() {
        Map<String, Object> result = service.forecastBudget(null);

        assertThat(result.get("error")).isEqualTo("Project name is required");
    }

    @Test
    void forecastBudget_withNoBudget_returnsError() {
        when(repository.findBudgetByProjectName("Unknown")).thenReturn(Optional.empty());

        Map<String, Object> result = service.forecastBudget("Unknown");

        assertThat((String) result.get("error")).contains("No budget found");
    }

    @Test
    void forecastBudget_withNoCosts_returnsNoForecast() {
        ProjectBudget budget = ProjectBudget.builder()
                .id("budget-1")
                .projectName("NewProject")
                .totalBudget(10000.0)
                .currency("EUR")
                .createdAt(Instant.now())
                .build();
        when(repository.findBudgetByProjectName("NewProject")).thenReturn(Optional.of(budget));
        when(repository.findCostsByProjectName("NewProject")).thenReturn(List.of());

        Map<String, Object> result = service.forecastBudget("NewProject");

        assertThat(result.get("totalSpent")).isEqualTo(0.0);
        assertThat(result.get("remaining")).isEqualTo(10000.0);
        assertThat(result.get("dailyBurnRate")).isEqualTo(0.0);
        assertThat(result.get("daysUntilExhausted")).isEqualTo(-1);
        assertThat(result.get("projectedEndDate")).isEqualTo("N/A");
        assertThat((String) result.get("message")).contains("No cost entries found");
    }

    @Test
    void forecastBudget_withCosts_calculatesCorrectForecast() {
        ProjectBudget budget = ProjectBudget.builder()
                .id("budget-1")
                .projectName("ActiveProject")
                .totalBudget(10000.0)
                .currency("EUR")
                .createdAt(Instant.now())
                .build();
        when(repository.findBudgetByProjectName("ActiveProject")).thenReturn(Optional.of(budget));

        Instant tenDaysAgo = Instant.now().minus(10, ChronoUnit.DAYS);
        List<CostEntry> costs = List.of(
                CostEntry.builder().id("c1").projectName("ActiveProject").category("dev")
                        .amount(1000.0).entryDate("2026-02-02").createdAt(tenDaysAgo).build(),
                CostEntry.builder().id("c2").projectName("ActiveProject").category("infra")
                        .amount(500.0).entryDate("2026-02-07").createdAt(Instant.now().minus(5, ChronoUnit.DAYS)).build()
        );
        when(repository.findCostsByProjectName("ActiveProject")).thenReturn(costs);

        Map<String, Object> result = service.forecastBudget("ActiveProject");

        assertThat(result.get("projectName")).isEqualTo("ActiveProject");
        assertThat(result.get("totalBudget")).isEqualTo(10000.0);
        assertThat(result.get("totalSpent")).isEqualTo(1500.0);
        assertThat(result.get("remaining")).isEqualTo(8500.0);
        assertThat((double) result.get("dailyBurnRate")).isGreaterThan(0);
        assertThat((long) result.get("daysUntilExhausted")).isGreaterThan(0);
        assertThat((String) result.get("projectedEndDate")).matches("\\d{4}-\\d{2}-\\d{2}");
    }

    @Test
    void forecastBudget_withExhaustedBudget_returnsZeroDays() {
        ProjectBudget budget = ProjectBudget.builder()
                .id("budget-1")
                .projectName("OverProject")
                .totalBudget(1000.0)
                .currency("EUR")
                .createdAt(Instant.now())
                .build();
        when(repository.findBudgetByProjectName("OverProject")).thenReturn(Optional.of(budget));

        Instant fiveDaysAgo = Instant.now().minus(5, ChronoUnit.DAYS);
        List<CostEntry> costs = List.of(
                CostEntry.builder().id("c1").projectName("OverProject").category("dev")
                        .amount(1200.0).entryDate("2026-02-07").createdAt(fiveDaysAgo).build()
        );
        when(repository.findCostsByProjectName("OverProject")).thenReturn(costs);

        Map<String, Object> result = service.forecastBudget("OverProject");

        assertThat(result.get("remaining")).isEqualTo(-200.0);
        assertThat(result.get("daysUntilExhausted")).isEqualTo(0L);
        assertThat(result.get("projectedEndDate")).isEqualTo("Budget already exhausted");
    }

    // ========== 5. costPerFeature Tests ==========

    @Test
    void costPerFeature_withNullProjectName_returnsError() {
        Map<String, Object> result = service.costPerFeature("feat-1", null, 10.0, 50.0, "desc", "EUR");

        assertThat(result.get("error")).isEqualTo("Project name is required");
    }

    @Test
    void costPerFeature_withFeatureData_savesFeatureCost() {
        Map<String, Object> result = service.costPerFeature("FEAT-001", "MyProject",
                10.0, 50.0, "Login feature", "USD");

        assertThat(result.get("featureId")).isEqualTo("FEAT-001");
        assertThat(result.get("projectName")).isEqualTo("MyProject");
        assertThat(result.get("hoursSpent")).isEqualTo(10.0);
        assertThat(result.get("hourlyRate")).isEqualTo(50.0);
        assertThat(result.get("totalCost")).isEqualTo(500.0);
        assertThat(result.get("description")).isEqualTo("Login feature");
        assertThat(result.get("currency")).isEqualTo("USD");
        assertThat((String) result.get("message")).contains("Feature cost saved");
    }

    @Test
    void costPerFeature_calculatesTotalCostCorrectly() {
        Map<String, Object> result = service.costPerFeature("FEAT-002", "MyProject",
                8.5, 75.0, "Payment module", "EUR");

        assertThat(result.get("totalCost")).isEqualTo(637.5);
    }

    @Test
    void costPerFeature_withNullCurrency_defaultsToEUR() {
        Map<String, Object> result = service.costPerFeature("FEAT-003", "MyProject",
                5.0, 60.0, "API endpoint", null);

        assertThat(result.get("currency")).isEqualTo("EUR");
    }

    @Test
    void costPerFeature_savesToRepository() {
        service.costPerFeature("FEAT-004", "TestProject", 20.0, 100.0, "Dashboard", "GBP");

        ArgumentCaptor<FeatureCost> captor = ArgumentCaptor.forClass(FeatureCost.class);
        verify(repository).saveFeatureCost(captor.capture());
        FeatureCost saved = captor.getValue();
        assertThat(saved.getFeatureId()).isEqualTo("FEAT-004");
        assertThat(saved.getProjectName()).isEqualTo("TestProject");
        assertThat(saved.getHoursSpent()).isEqualTo(20.0);
        assertThat(saved.getHourlyRate()).isEqualTo(100.0);
        assertThat(saved.getTotalCost()).isEqualTo(2000.0);
        assertThat(saved.getCurrency()).isEqualTo("GBP");
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void costPerFeature_withNullFeatureId_listsAllFeatures() {
        List<FeatureCost> features = List.of(
                FeatureCost.builder().id("fc1").featureId("FEAT-001").projectName("MyProject")
                        .hoursSpent(10.0).hourlyRate(50.0).totalCost(500.0)
                        .description("Login").currency("EUR").createdAt(Instant.now()).build(),
                FeatureCost.builder().id("fc2").featureId("FEAT-002").projectName("MyProject")
                        .hoursSpent(20.0).hourlyRate(60.0).totalCost(1200.0)
                        .description("Dashboard").currency("EUR").createdAt(Instant.now()).build()
        );
        when(repository.findFeatureCostsByProjectName("MyProject")).thenReturn(features);

        Map<String, Object> result = service.costPerFeature(null, "MyProject", 0, 0, null, null);

        assertThat(result.get("projectName")).isEqualTo("MyProject");
        assertThat(result.get("featureCount")).isEqualTo(2);
        assertThat(result.get("totalCostAllFeatures")).isEqualTo(1700.0);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> featureList = (List<Map<String, Object>>) result.get("features");
        assertThat(featureList).hasSize(2);
        assertThat(featureList.get(0).get("featureId")).isEqualTo("FEAT-001");
        assertThat(featureList.get(1).get("featureId")).isEqualTo("FEAT-002");
    }

    @Test
    void costPerFeature_withZeroHoursSpent_listsAllFeatures() {
        when(repository.findFeatureCostsByProjectName("MyProject")).thenReturn(List.of());

        Map<String, Object> result = service.costPerFeature("FEAT-001", "MyProject", 0, 50.0, null, null);

        assertThat(result.get("featureCount")).isEqualTo(0);
        assertThat((String) result.get("message")).contains("No feature costs found");
    }

    @Test
    void costPerFeature_withZeroHourlyRate_listsAllFeatures() {
        when(repository.findFeatureCostsByProjectName("MyProject")).thenReturn(List.of());

        Map<String, Object> result = service.costPerFeature("FEAT-001", "MyProject", 10.0, 0, null, null);

        assertThat(result.get("featureCount")).isEqualTo(0);
        assertThat((String) result.get("message")).contains("No feature costs found");
    }

    @Test
    void costPerFeature_withBlankFeatureId_listsAllFeatures() {
        when(repository.findFeatureCostsByProjectName("MyProject")).thenReturn(List.of());

        Map<String, Object> result = service.costPerFeature("  ", "MyProject", 10.0, 50.0, null, null);

        assertThat(result.get("featureCount")).isEqualTo(0);
    }

    @Test
    void costPerFeature_listingWithNoFeatures_returnsEmptyResult() {
        when(repository.findFeatureCostsByProjectName("EmptyProject")).thenReturn(List.of());

        Map<String, Object> result = service.costPerFeature(null, "EmptyProject", 0, 0, null, null);

        assertThat(result.get("featureCount")).isEqualTo(0);
        assertThat(result.get("totalCostAllFeatures")).isEqualTo(0.0);
        assertThat((String) result.get("message")).contains("No feature costs found");
        verify(repository, never()).saveFeatureCost(any());
    }
}
