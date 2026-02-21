package com.localmind.infrastructure.persistence.repository.mcp;

import com.localmind.infrastructure.persistence.entity.mcp.AccessPolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data JPA repository for AccessPolicyEntity.
 */
public interface JpaAccessPolicyRepository extends JpaRepository<AccessPolicyEntity, UUID> {
}
