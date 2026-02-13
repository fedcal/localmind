package com.localmind.infrastructure.persistence.repository.mcp;

import com.localmind.infrastructure.persistence.entity.mcp.ScrumTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for ScrumTaskEntity.
 */
public interface JpaScrumTaskRepository extends JpaRepository<ScrumTaskEntity, UUID> {

    List<ScrumTaskEntity> findByStoryId(String storyId);
}
