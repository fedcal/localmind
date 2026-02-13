package com.localmind.infrastructure.persistence.repository.mcp;

import com.localmind.infrastructure.persistence.entity.mcp.RetroItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for RetroItemEntity.
 */
public interface JpaRetroItemRepository extends JpaRepository<RetroItemEntity, UUID> {

    List<RetroItemEntity> findByRetroId(String retroId);
}
