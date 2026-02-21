package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.CostEntry;
import com.localmind.domain.mcp.model.FeatureCost;
import com.localmind.domain.mcp.model.ProjectBudget;
import com.localmind.domain.mcp.port.out.ProjectEconomicsRepository;
import com.localmind.infrastructure.persistence.entity.mcp.CostEntryEntity;
import com.localmind.infrastructure.persistence.entity.mcp.FeatureCostEntity;
import com.localmind.infrastructure.persistence.entity.mcp.ProjectBudgetEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaCostEntryRepository;
import com.localmind.infrastructure.persistence.repository.mcp.JpaFeatureCostRepository;
import com.localmind.infrastructure.persistence.repository.mcp.JpaProjectBudgetRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter bridging the domain ProjectEconomicsRepository port to JPA persistence.
 */
@Component
public class ProjectEconomicsRepositoryAdapter implements ProjectEconomicsRepository {

    private final JpaProjectBudgetRepository budgetJpaRepository;
    private final JpaCostEntryRepository costEntryJpaRepository;
    private final JpaFeatureCostRepository featureCostJpaRepository;

    public ProjectEconomicsRepositoryAdapter(JpaProjectBudgetRepository budgetJpaRepository,
                                              JpaCostEntryRepository costEntryJpaRepository,
                                              JpaFeatureCostRepository featureCostJpaRepository) {
        this.budgetJpaRepository = budgetJpaRepository;
        this.costEntryJpaRepository = costEntryJpaRepository;
        this.featureCostJpaRepository = featureCostJpaRepository;
    }

    // ========== ProjectBudget ==========

    @Override
    public ProjectBudget saveBudget(ProjectBudget budget) {
        ProjectBudgetEntity entity = toBudgetEntity(budget);
        ProjectBudgetEntity saved = budgetJpaRepository.save(entity);
        return toBudgetDomain(saved);
    }

    @Override
    public Optional<ProjectBudget> findBudgetByProjectName(String projectName) {
        return budgetJpaRepository.findByProjectName(projectName)
                .map(this::toBudgetDomain);
    }

    // ========== CostEntry ==========

    @Override
    public CostEntry saveCostEntry(CostEntry costEntry) {
        CostEntryEntity entity = toCostEntryEntity(costEntry);
        CostEntryEntity saved = costEntryJpaRepository.save(entity);
        return toCostEntryDomain(saved);
    }

    @Override
    public List<CostEntry> findCostsByProjectName(String projectName) {
        return costEntryJpaRepository.findByProjectNameOrderByCreatedAtDesc(projectName)
                .stream()
                .map(this::toCostEntryDomain)
                .collect(Collectors.toList());
    }

    // ========== FeatureCost ==========

    @Override
    public FeatureCost saveFeatureCost(FeatureCost featureCost) {
        FeatureCostEntity entity = toFeatureCostEntity(featureCost);
        FeatureCostEntity saved = featureCostJpaRepository.save(entity);
        return toFeatureCostDomain(saved);
    }

    @Override
    public List<FeatureCost> findFeatureCostsByProjectName(String projectName) {
        return featureCostJpaRepository.findByProjectNameOrderByCreatedAtDesc(projectName)
                .stream()
                .map(this::toFeatureCostDomain)
                .collect(Collectors.toList());
    }

    // ========== ProjectBudget mapping ==========

    private ProjectBudgetEntity toBudgetEntity(ProjectBudget domain) {
        ProjectBudgetEntity entity = ProjectBudgetEntity.builder()
                .projectName(domain.getProjectName())
                .totalBudget(BigDecimal.valueOf(domain.getTotalBudget()))
                .currency(domain.getCurrency())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private ProjectBudget toBudgetDomain(ProjectBudgetEntity entity) {
        return ProjectBudget.builder()
                .id(entity.getId().toString())
                .projectName(entity.getProjectName())
                .totalBudget(entity.getTotalBudget().doubleValue())
                .currency(entity.getCurrency())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    // ========== CostEntry mapping ==========

    private CostEntryEntity toCostEntryEntity(CostEntry domain) {
        CostEntryEntity entity = CostEntryEntity.builder()
                .projectName(domain.getProjectName())
                .category(domain.getCategory())
                .amount(BigDecimal.valueOf(domain.getAmount()))
                .description(domain.getDescription())
                .entryDate(domain.getEntryDate())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private CostEntry toCostEntryDomain(CostEntryEntity entity) {
        return CostEntry.builder()
                .id(entity.getId().toString())
                .projectName(entity.getProjectName())
                .category(entity.getCategory())
                .amount(entity.getAmount().doubleValue())
                .description(entity.getDescription())
                .entryDate(entity.getEntryDate())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    // ========== FeatureCost mapping ==========

    private FeatureCostEntity toFeatureCostEntity(FeatureCost domain) {
        FeatureCostEntity entity = FeatureCostEntity.builder()
                .featureId(domain.getFeatureId())
                .projectName(domain.getProjectName())
                .hoursSpent(BigDecimal.valueOf(domain.getHoursSpent()))
                .hourlyRate(BigDecimal.valueOf(domain.getHourlyRate()))
                .totalCost(BigDecimal.valueOf(domain.getTotalCost()))
                .description(domain.getDescription())
                .currency(domain.getCurrency())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private FeatureCost toFeatureCostDomain(FeatureCostEntity entity) {
        return FeatureCost.builder()
                .id(entity.getId().toString())
                .featureId(entity.getFeatureId())
                .projectName(entity.getProjectName())
                .hoursSpent(entity.getHoursSpent().doubleValue())
                .hourlyRate(entity.getHourlyRate().doubleValue())
                .totalCost(entity.getTotalCost().doubleValue())
                .description(entity.getDescription())
                .currency(entity.getCurrency())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
