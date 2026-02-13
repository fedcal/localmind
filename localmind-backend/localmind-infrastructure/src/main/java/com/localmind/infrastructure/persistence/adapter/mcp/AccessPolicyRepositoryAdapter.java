package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.AccessAuditEntry;
import com.localmind.domain.mcp.model.AccessPolicy;
import com.localmind.domain.mcp.port.out.AccessPolicyRepository;
import com.localmind.infrastructure.persistence.entity.mcp.AccessAuditEntity;
import com.localmind.infrastructure.persistence.entity.mcp.AccessPolicyEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaAccessAuditRepository;
import com.localmind.infrastructure.persistence.repository.mcp.JpaAccessPolicyRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter bridging the domain AccessPolicyRepository port to JPA persistence.
 * Handles mapping between domain models and JPA entities for access policies and audit entries.
 */
@Component
public class AccessPolicyRepositoryAdapter implements AccessPolicyRepository {

    private final JpaAccessPolicyRepository policyJpaRepository;
    private final JpaAccessAuditRepository auditJpaRepository;

    public AccessPolicyRepositoryAdapter(JpaAccessPolicyRepository policyJpaRepository,
                                          JpaAccessAuditRepository auditJpaRepository) {
        this.policyJpaRepository = policyJpaRepository;
        this.auditJpaRepository = auditJpaRepository;
    }

    // --- AccessPolicy ---

    @Override
    public AccessPolicy save(AccessPolicy policy) {
        AccessPolicyEntity entity = toPolicyEntity(policy);
        AccessPolicyEntity saved = policyJpaRepository.save(entity);
        return toPolicyDomain(saved);
    }

    @Override
    public List<AccessPolicy> findAll() {
        return policyJpaRepository.findAll().stream()
                .map(this::toPolicyDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<AccessPolicy> findById(String id) {
        return policyJpaRepository.findById(UUID.fromString(id))
                .map(this::toPolicyDomain);
    }

    // --- AccessAuditEntry ---

    @Override
    public AccessAuditEntry saveAudit(AccessAuditEntry entry) {
        AccessAuditEntity entity = toAuditEntity(entry);
        AccessAuditEntity saved = auditJpaRepository.save(entity);
        return toAuditDomain(saved);
    }

    @Override
    public List<AccessAuditEntry> findAuditByUserId(String userId) {
        return auditJpaRepository.findByUserId(userId).stream()
                .map(this::toAuditDomain)
                .collect(Collectors.toList());
    }

    // --- AccessPolicy Mapping ---

    private AccessPolicyEntity toPolicyEntity(AccessPolicy domain) {
        AccessPolicyEntity entity = AccessPolicyEntity.builder()
                .name(domain.getName())
                .effect(domain.getEffect())
                .rulesJson(domain.getRulesJson())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private AccessPolicy toPolicyDomain(AccessPolicyEntity entity) {
        return AccessPolicy.builder()
                .id(entity.getId().toString())
                .name(entity.getName())
                .effect(entity.getEffect())
                .rulesJson(entity.getRulesJson())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    // --- AccessAuditEntry Mapping ---

    private AccessAuditEntity toAuditEntity(AccessAuditEntry domain) {
        AccessAuditEntity entity = AccessAuditEntity.builder()
                .userId(domain.getUserId())
                .server(domain.getServer())
                .tool(domain.getTool())
                .allowed(domain.isAllowed())
                .policyId(domain.getPolicyId())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private AccessAuditEntry toAuditDomain(AccessAuditEntity entity) {
        return AccessAuditEntry.builder()
                .id(entity.getId().toString())
                .userId(entity.getUserId())
                .server(entity.getServer())
                .tool(entity.getTool())
                .allowed(entity.isAllowed())
                .policyId(entity.getPolicyId())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
