package com.localmind.domain.mcp.port.out;

import com.localmind.domain.mcp.model.AccessAuditEntry;
import com.localmind.domain.mcp.model.AccessPolicy;

import java.util.List;
import java.util.Optional;

/**
 * Output port for persisting access policies and audit entries.
 */
public interface AccessPolicyRepository {

    /**
     * Saves an access policy.
     *
     * @param policy the policy to persist
     * @return the saved policy
     */
    AccessPolicy save(AccessPolicy policy);

    /**
     * Returns all access policies.
     *
     * @return list of all policies
     */
    List<AccessPolicy> findAll();

    /**
     * Finds an access policy by its identifier.
     *
     * @param id the policy id
     * @return an Optional containing the policy if found
     */
    Optional<AccessPolicy> findById(String id);

    /**
     * Saves an audit entry.
     *
     * @param entry the audit entry to persist
     * @return the saved audit entry
     */
    AccessAuditEntry saveAudit(AccessAuditEntry entry);

    /**
     * Finds all audit entries for a given user.
     *
     * @param userId the user identifier
     * @return list of audit entries for the user
     */
    List<AccessAuditEntry> findAuditByUserId(String userId);
}
