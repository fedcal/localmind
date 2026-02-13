package com.localmind.infrastructure.persistence.repository.mcp;

import com.localmind.infrastructure.persistence.entity.mcp.CostEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaCostEntryRepository extends JpaRepository<CostEntryEntity, UUID> {

    List<CostEntryEntity> findByProjectNameOrderByCreatedAtDesc(String projectName);
}
