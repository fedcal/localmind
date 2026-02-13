package com.localmind.infrastructure.persistence.repository.mcp;

import com.localmind.infrastructure.persistence.entity.mcp.ActionItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for ActionItemEntity.
 */
public interface JpaActionItemRepository extends JpaRepository<ActionItemEntity, UUID> {

    List<ActionItemEntity> findByRetroId(String retroId);
}
