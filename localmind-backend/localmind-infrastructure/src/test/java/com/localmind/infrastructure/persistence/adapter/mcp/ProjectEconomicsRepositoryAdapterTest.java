package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.CostEntry;
import com.localmind.domain.mcp.model.FeatureCost;
import com.localmind.domain.mcp.model.ProjectBudget;
import com.localmind.infrastructure.persistence.entity.mcp.CostEntryEntity;
import com.localmind.infrastructure.persistence.entity.mcp.FeatureCostEntity;
import com.localmind.infrastructure.persistence.entity.mcp.ProjectBudgetEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaCostEntryRepository;
import com.localmind.infrastructure.persistence.repository.mcp.JpaFeatureCostRepository;
import com.localmind.infrastructure.persistence.repository.mcp.JpaProjectBudgetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectEconomicsRepositoryAdapterTest {

    @Mock
    private JpaProjectBudgetRepository budgetJpaRepository;

    @Mock
    private JpaCostEntryRepository costEntryJpaRepository;

    @Mock
    private JpaFeatureCostRepository featureCostJpaRepository;

    private ProjectEconomicsRepositoryAdapter adapter;

    private final UUID TEST_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final Instant NOW = Instant.parse("2026-02-12T10:00:00Z");

    @BeforeEach
    void setUp() {
        adapter = new ProjectEconomicsRepositoryAdapter(budgetJpaRepository, costEntryJpaRepository, featureCostJpaRepository);
    }

    // ========== ProjectBudget Tests ==========

    @Test
    void saveBudget_mapsDomainToEntityAndBack() {
        ProjectBudget domain = ProjectBudget.builder()
                .id(TEST_UUID.toString())
                .projectName("MyProject")
                .totalBudget(50000.0)
                .currency("EUR")
                .createdAt(NOW)
                .build();

        ProjectBudgetEntity savedEntity = ProjectBudgetEntity.builder()
                .id(TEST_UUID)
                .projectName("MyProject")
                .totalBudget(BigDecimal.valueOf(50000.0))
                .currency("EUR")
                .createdAt(NOW)
                .build();

        when(budgetJpaRepository.save(any(ProjectBudgetEntity.class))).thenReturn(savedEntity);

        ProjectBudget result = adapter.saveBudget(domain);

        ArgumentCaptor<ProjectBudgetEntity> captor = ArgumentCaptor.forClass(ProjectBudgetEntity.class);
        verify(budgetJpaRepository).save(captor.capture());

        ProjectBudgetEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getProjectName()).isEqualTo("MyProject");
        assertThat(captured.getTotalBudget()).isEqualByComparingTo(BigDecimal.valueOf(50000.0));
        assertThat(captured.getCurrency()).isEqualTo("EUR");
        assertThat(captured.getCreatedAt()).isEqualTo(NOW);

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getProjectName()).isEqualTo("MyProject");
        assertThat(result.getTotalBudget()).isEqualTo(50000.0);
        assertThat(result.getCurrency()).isEqualTo("EUR");
        assertThat(result.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void findBudgetByProjectName_whenFound_returnsMappedDomain() {
        ProjectBudgetEntity entity = ProjectBudgetEntity.builder()
                .id(TEST_UUID)
                .projectName("MyProject")
                .totalBudget(BigDecimal.valueOf(25000.0))
                .currency("USD")
                .createdAt(NOW)
                .build();

        when(budgetJpaRepository.findByProjectName("MyProject")).thenReturn(Optional.of(entity));

        Optional<ProjectBudget> result = adapter.findBudgetByProjectName("MyProject");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.get().getProjectName()).isEqualTo("MyProject");
        assertThat(result.get().getTotalBudget()).isEqualTo(25000.0);
        assertThat(result.get().getCurrency()).isEqualTo("USD");
    }

    @Test
    void findBudgetByProjectName_whenNotFound_returnsEmpty() {
        when(budgetJpaRepository.findByProjectName("Unknown")).thenReturn(Optional.empty());

        Optional<ProjectBudget> result = adapter.findBudgetByProjectName("Unknown");

        assertThat(result).isEmpty();
    }

    // ========== CostEntry Tests ==========

    @Test
    void saveCostEntry_mapsDomainToEntityAndBack() {
        CostEntry domain = CostEntry.builder()
                .id(TEST_UUID.toString())
                .projectName("MyProject")
                .category("development")
                .amount(1500.0)
                .description("Backend work")
                .entryDate("2026-02-12")
                .createdAt(NOW)
                .build();

        CostEntryEntity savedEntity = CostEntryEntity.builder()
                .id(TEST_UUID)
                .projectName("MyProject")
                .category("development")
                .amount(BigDecimal.valueOf(1500.0))
                .description("Backend work")
                .entryDate("2026-02-12")
                .createdAt(NOW)
                .build();

        when(costEntryJpaRepository.save(any(CostEntryEntity.class))).thenReturn(savedEntity);

        CostEntry result = adapter.saveCostEntry(domain);

        ArgumentCaptor<CostEntryEntity> captor = ArgumentCaptor.forClass(CostEntryEntity.class);
        verify(costEntryJpaRepository).save(captor.capture());

        CostEntryEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getProjectName()).isEqualTo("MyProject");
        assertThat(captured.getCategory()).isEqualTo("development");
        assertThat(captured.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(1500.0));
        assertThat(captured.getDescription()).isEqualTo("Backend work");
        assertThat(captured.getEntryDate()).isEqualTo("2026-02-12");

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getProjectName()).isEqualTo("MyProject");
        assertThat(result.getAmount()).isEqualTo(1500.0);
    }

    @Test
    void findCostsByProjectName_mapsEntitiesCorrectly() {
        CostEntryEntity entity1 = CostEntryEntity.builder()
                .id(TEST_UUID)
                .projectName("MyProject")
                .category("development")
                .amount(BigDecimal.valueOf(1000.0))
                .description("Frontend")
                .entryDate("2026-02-10")
                .createdAt(NOW)
                .build();

        UUID uuid2 = UUID.fromString("b1b2c3d4-e5f6-7890-abcd-ef1234567890");
        CostEntryEntity entity2 = CostEntryEntity.builder()
                .id(uuid2)
                .projectName("MyProject")
                .category("infra")
                .amount(BigDecimal.valueOf(500.0))
                .description("Server")
                .entryDate("2026-02-11")
                .createdAt(NOW)
                .build();

        when(costEntryJpaRepository.findByProjectNameOrderByCreatedAtDesc("MyProject"))
                .thenReturn(List.of(entity1, entity2));

        List<CostEntry> result = adapter.findCostsByProjectName("MyProject");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.get(0).getCategory()).isEqualTo("development");
        assertThat(result.get(0).getAmount()).isEqualTo(1000.0);
        assertThat(result.get(1).getId()).isEqualTo(uuid2.toString());
        assertThat(result.get(1).getCategory()).isEqualTo("infra");
        assertThat(result.get(1).getAmount()).isEqualTo(500.0);
    }

    // ========== FeatureCost Tests ==========

    @Test
    void saveFeatureCost_mapsDomainToEntityAndBack() {
        FeatureCost domain = FeatureCost.builder()
                .id(TEST_UUID.toString())
                .featureId("FEAT-001")
                .projectName("MyProject")
                .hoursSpent(10.0)
                .hourlyRate(50.0)
                .totalCost(500.0)
                .description("Login feature")
                .currency("EUR")
                .createdAt(NOW)
                .build();

        FeatureCostEntity savedEntity = FeatureCostEntity.builder()
                .id(TEST_UUID)
                .featureId("FEAT-001")
                .projectName("MyProject")
                .hoursSpent(BigDecimal.valueOf(10.0))
                .hourlyRate(BigDecimal.valueOf(50.0))
                .totalCost(BigDecimal.valueOf(500.0))
                .description("Login feature")
                .currency("EUR")
                .createdAt(NOW)
                .build();

        when(featureCostJpaRepository.save(any(FeatureCostEntity.class))).thenReturn(savedEntity);

        FeatureCost result = adapter.saveFeatureCost(domain);

        ArgumentCaptor<FeatureCostEntity> captor = ArgumentCaptor.forClass(FeatureCostEntity.class);
        verify(featureCostJpaRepository).save(captor.capture());

        FeatureCostEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getFeatureId()).isEqualTo("FEAT-001");
        assertThat(captured.getProjectName()).isEqualTo("MyProject");
        assertThat(captured.getHoursSpent()).isEqualByComparingTo(BigDecimal.valueOf(10.0));
        assertThat(captured.getHourlyRate()).isEqualByComparingTo(BigDecimal.valueOf(50.0));
        assertThat(captured.getTotalCost()).isEqualByComparingTo(BigDecimal.valueOf(500.0));
        assertThat(captured.getDescription()).isEqualTo("Login feature");
        assertThat(captured.getCurrency()).isEqualTo("EUR");

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getFeatureId()).isEqualTo("FEAT-001");
        assertThat(result.getHoursSpent()).isEqualTo(10.0);
        assertThat(result.getHourlyRate()).isEqualTo(50.0);
        assertThat(result.getTotalCost()).isEqualTo(500.0);
    }

    @Test
    void findFeatureCostsByProjectName_mapsEntitiesCorrectly() {
        FeatureCostEntity entity1 = FeatureCostEntity.builder()
                .id(TEST_UUID)
                .featureId("FEAT-001")
                .projectName("MyProject")
                .hoursSpent(BigDecimal.valueOf(10.0))
                .hourlyRate(BigDecimal.valueOf(50.0))
                .totalCost(BigDecimal.valueOf(500.0))
                .description("Login")
                .currency("EUR")
                .createdAt(NOW)
                .build();

        UUID uuid2 = UUID.fromString("b1b2c3d4-e5f6-7890-abcd-ef1234567890");
        FeatureCostEntity entity2 = FeatureCostEntity.builder()
                .id(uuid2)
                .featureId("FEAT-002")
                .projectName("MyProject")
                .hoursSpent(BigDecimal.valueOf(20.0))
                .hourlyRate(BigDecimal.valueOf(60.0))
                .totalCost(BigDecimal.valueOf(1200.0))
                .description("Dashboard")
                .currency("USD")
                .createdAt(NOW)
                .build();

        when(featureCostJpaRepository.findByProjectNameOrderByCreatedAtDesc("MyProject"))
                .thenReturn(List.of(entity1, entity2));

        List<FeatureCost> result = adapter.findFeatureCostsByProjectName("MyProject");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFeatureId()).isEqualTo("FEAT-001");
        assertThat(result.get(0).getTotalCost()).isEqualTo(500.0);
        assertThat(result.get(0).getCurrency()).isEqualTo("EUR");
        assertThat(result.get(1).getFeatureId()).isEqualTo("FEAT-002");
        assertThat(result.get(1).getTotalCost()).isEqualTo(1200.0);
        assertThat(result.get(1).getCurrency()).isEqualTo("USD");
    }

    @Test
    void saveBudget_withNullId_doesNotSetIdOnEntity() {
        ProjectBudget domain = ProjectBudget.builder()
                .projectName("NewProject")
                .totalBudget(5000.0)
                .currency("EUR")
                .createdAt(NOW)
                .build();

        ProjectBudgetEntity savedEntity = ProjectBudgetEntity.builder()
                .id(TEST_UUID)
                .projectName("NewProject")
                .totalBudget(BigDecimal.valueOf(5000.0))
                .currency("EUR")
                .createdAt(NOW)
                .build();

        when(budgetJpaRepository.save(any(ProjectBudgetEntity.class))).thenReturn(savedEntity);

        adapter.saveBudget(domain);

        ArgumentCaptor<ProjectBudgetEntity> captor = ArgumentCaptor.forClass(ProjectBudgetEntity.class);
        verify(budgetJpaRepository).save(captor.capture());

        ProjectBudgetEntity captured = captor.getValue();
        assertThat(captured.getId()).isNull();
        assertThat(captured.getProjectName()).isEqualTo("NewProject");
    }
}
