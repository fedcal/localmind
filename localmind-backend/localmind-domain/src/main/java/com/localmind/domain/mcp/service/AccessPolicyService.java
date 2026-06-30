package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.AccessAuditEntry;
import com.localmind.domain.mcp.model.AccessPolicy;
import com.localmind.domain.mcp.port.in.AccessPolicyUseCase;
import com.localmind.domain.mcp.port.out.AccessPolicyRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Domain service implementing the access-policy tool group.
 * Provides policy management, access checking, role assignment and audit logging.
 * No Spring annotations - registered as a bean via DomainConfig.
 */
public class AccessPolicyService implements AccessPolicyUseCase {

    private final AccessPolicyRepository repository;

    private static final Set<String> VALID_EFFECTS = Set.of("allow", "deny");

    public AccessPolicyService(AccessPolicyRepository repository) {
        this.repository = repository;
    }

    // ========== 1. createPolicy ==========

    @Override
    public Map<String, Object> createPolicy(String name, String effect, String rulesJson) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (name == null || name.isBlank()) {
            result.put("error", "Policy name cannot be blank");
            return result;
        }

        if (effect == null || !VALID_EFFECTS.contains(effect.toLowerCase())) {
            result.put("error", "Effect must be 'allow' or 'deny'");
            return result;
        }

        String normalizedEffect = effect.toLowerCase();

        AccessPolicy policy = AccessPolicy.builder()
                .id(UUID.randomUUID().toString())
                .name(name.trim())
                .effect(normalizedEffect)
                .rulesJson(rulesJson != null ? rulesJson : "{}")
                .createdAt(Instant.now())
                .build();

        AccessPolicy saved = repository.save(policy);

        result.put("id", saved.getId());
        result.put("name", saved.getName());
        result.put("effect", saved.getEffect());
        result.put("rules", saved.getRulesJson());
        result.put("createdAt", saved.getCreatedAt().toString());

        return result;
    }

    // ========== 2. checkAccess ==========

    @Override
    public Map<String, Object> checkAccess(String userId, String server, String tool) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (userId == null || userId.isBlank()) {
            result.put("error", "User ID cannot be blank");
            return result;
        }

        if (server == null || server.isBlank()) {
            result.put("error", "Server cannot be blank");
            return result;
        }

        if (tool == null || tool.isBlank()) {
            result.put("error", "Tool cannot be blank");
            return result;
        }

        List<AccessPolicy> policies = repository.findAll();
        boolean allowed = true;
        String matchedPolicyId = null;
        String matchedPolicyName = null;

        // Check deny policies first, then allow policies
        for (AccessPolicy policy : policies) {
            String rules = policy.getRulesJson().toLowerCase();
            boolean matchesServer = rules.contains(server.toLowerCase());
            boolean matchesTool = rules.contains(tool.toLowerCase());
            boolean matchesWildcard = rules.contains("\"*\"") || rules.contains("'*'");

            if (matchesServer && (matchesTool || matchesWildcard)) {
                matchedPolicyId = policy.getId();
                matchedPolicyName = policy.getName();
                if ("deny".equals(policy.getEffect())) {
                    allowed = false;
                    break;
                }
            }
        }

        // If no policy matched, default to allow
        if (matchedPolicyId == null) {
            allowed = true;
        }

        // Record audit entry
        AccessAuditEntry auditEntry = AccessAuditEntry.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .server(server)
                .tool(tool)
                .allowed(allowed)
                .policyId(matchedPolicyId)
                .createdAt(Instant.now())
                .build();
        repository.saveAudit(auditEntry);

        result.put("userId", userId);
        result.put("server", server);
        result.put("tool", tool);
        result.put("allowed", allowed);

        if (matchedPolicyId != null) {
            Map<String, Object> matchedPolicy = new LinkedHashMap<>();
            matchedPolicy.put("id", matchedPolicyId);
            matchedPolicy.put("name", matchedPolicyName);
            result.put("matchedPolicy", matchedPolicy);
        } else {
            result.put("matchedPolicy", null);
        }

        return result;
    }

    // ========== 3. listPolicies ==========

    @Override
    public Map<String, Object> listPolicies() {
        Map<String, Object> result = new LinkedHashMap<>();

        List<AccessPolicy> policies = repository.findAll();
        List<Map<String, Object>> policyList = new ArrayList<>();

        for (AccessPolicy policy : policies) {
            Map<String, Object> policyMap = new LinkedHashMap<>();
            policyMap.put("id", policy.getId());
            policyMap.put("name", policy.getName());
            policyMap.put("effect", policy.getEffect());
            policyMap.put("rules", policy.getRulesJson());
            policyMap.put("createdAt", policy.getCreatedAt().toString());
            policyList.add(policyMap);
        }

        result.put("policies", policyList);
        result.put("count", policyList.size());

        return result;
    }

    // ========== 4. assignRole ==========

    @Override
    public Map<String, Object> assignRole(String userId, String roleName) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (userId == null || userId.isBlank()) {
            result.put("error", "User ID cannot be blank");
            return result;
        }

        if (roleName == null || roleName.isBlank()) {
            result.put("error", "Role name cannot be blank");
            return result;
        }

        // Create a policy representing the role assignment
        String rulesJson = "{\"role\":\"" + roleName.trim() + "\",\"userId\":\"" + userId.trim() + "\"}";

        AccessPolicy policy = AccessPolicy.builder()
                .id(UUID.randomUUID().toString())
                .name("role-" + roleName.trim() + "-" + userId.trim())
                .effect("allow")
                .rulesJson(rulesJson)
                .createdAt(Instant.now())
                .build();

        repository.save(policy);

        result.put("userId", userId.trim());
        result.put("roleName", roleName.trim());
        result.put("assigned", true);

        return result;
    }

    // ========== 5. auditAccess ==========

    @Override
    public Map<String, Object> auditAccess(String userId, String server, int limit) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (userId == null || userId.isBlank()) {
            result.put("error", "User ID cannot be blank");
            return result;
        }

        int effectiveLimit = limit > 0 ? limit : 50;

        List<AccessAuditEntry> allEntries = repository.findAuditByUserId(userId);

        // Filter by server if provided
        List<AccessAuditEntry> filtered;
        if (server != null && !server.isBlank()) {
            filtered = new ArrayList<>();
            for (AccessAuditEntry entry : allEntries) {
                if (server.equalsIgnoreCase(entry.getServer())) {
                    filtered.add(entry);
                }
            }
        } else {
            filtered = allEntries;
        }

        // Apply limit
        List<AccessAuditEntry> limited = filtered.size() > effectiveLimit
                ? filtered.subList(0, effectiveLimit)
                : filtered;

        List<Map<String, Object>> entryList = new ArrayList<>();
        for (AccessAuditEntry entry : limited) {
            Map<String, Object> entryMap = new LinkedHashMap<>();
            entryMap.put("id", entry.getId());
            entryMap.put("userId", entry.getUserId());
            entryMap.put("server", entry.getServer());
            entryMap.put("tool", entry.getTool());
            entryMap.put("allowed", entry.isAllowed());
            entryMap.put("policyId", entry.getPolicyId());
            entryMap.put("createdAt", entry.getCreatedAt().toString());
            entryList.add(entryMap);
        }

        result.put("entries", entryList);
        result.put("count", entryList.size());

        return result;
    }
}
