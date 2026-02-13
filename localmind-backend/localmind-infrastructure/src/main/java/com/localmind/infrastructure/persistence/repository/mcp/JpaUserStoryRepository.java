package com.localmind.infrastructure.persistence.repository.mcp;

import com.localmind.infrastructure.persistence.entity.mcp.UserStoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for UserStoryEntity.
 */
public interface JpaUserStoryRepository extends JpaRepository<UserStoryEntity, UUID> {

    List<UserStoryEntity> findBySprintId(String sprintId);

    List<UserStoryEntity> findBySprintIdIsNull();
}
