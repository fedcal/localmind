package com.localmind.domain.mcp.port.in;

import java.util.Map;

/**
 * Input port for the access-policy tool group.
 * Provides policy management, access checking, role assignment and audit logging.
 */
public interface AccessPolicyUseCase {

    /**
     * Creates a new access policy with the given name, effect and rules.
     *
     * @param name      the policy name
     * @param effect    the effect: "allow" or "deny"
     * @param rulesJson JSON string defining the access rules
     * @return result with id, name, effect, rules, createdAt
     */
    Map<String, Object> createPolicy(String name, String effect, String rulesJson);

    /**
     * Checks whether a user has access to a specific server/tool combination.
     *
     * @param userId the user identifier
     * @param server the MCP server name
     * @param tool   the tool name
     * @return result with userId, server, tool, allowed, matchedPolicy
     */
    Map<String, Object> checkAccess(String userId, String server, String tool);

    /**
     * Lists all defined access policies.
     *
     * @return result with policies array and count
     */
    Map<String, Object> listPolicies();

    /**
     * Assigns a role to a user by creating or updating a policy.
     *
     * @param userId   the user identifier
     * @param roleName the role name to assign
     * @return result with userId, roleName, assigned
     */
    Map<String, Object> assignRole(String userId, String roleName);

    /**
     * Retrieves audit entries for a user, optionally filtered by server.
     *
     * @param userId the user identifier
     * @param server the MCP server name (nullable, filters if present)
     * @param limit  maximum number of entries to return
     * @return result with entries array and count
     */
    Map<String, Object> auditAccess(String userId, String server, int limit);
}
