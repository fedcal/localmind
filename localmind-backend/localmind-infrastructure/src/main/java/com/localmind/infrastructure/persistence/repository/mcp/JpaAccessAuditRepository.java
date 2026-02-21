package com.localmind.infrastructure.persistence.repository.mcp;

import com.localmind.infrastructure.persistence.entity.mcp.AccessAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for AccessAuditEntity.
 */
public interface JpaAccessAuditRepository extends JpaRepository<AccessAuditEntity, UUID> {

    List<AccessAuditEntity> findByUserId(String userId);
}
