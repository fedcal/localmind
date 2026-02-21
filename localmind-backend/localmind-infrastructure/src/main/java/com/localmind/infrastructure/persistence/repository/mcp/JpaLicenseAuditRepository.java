package com.localmind.infrastructure.persistence.repository.mcp;

import com.localmind.infrastructure.persistence.entity.mcp.LicenseAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaLicenseAuditRepository extends JpaRepository<LicenseAuditEntity, UUID> {
}
