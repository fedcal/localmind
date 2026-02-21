package com.localmind.infrastructure.mcp.server;

import com.localmind.domain.mcp.port.in.AccessPolicyUseCase;
import com.localmind.domain.mcp.port.in.DecisionLogUseCase;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnProperty(name = "localmind.mcp.server.enabled", havingValue = "true", matchIfMissing = true)
public class LocalMindGovernanceTools {

    private final AccessPolicyUseCase accessPolicyUseCase;
    private final DecisionLogUseCase decisionLogUseCase;

    public LocalMindGovernanceTools(AccessPolicyUseCase accessPolicyUseCase,
                                     DecisionLogUseCase decisionLogUseCase) {
        this.accessPolicyUseCase = accessPolicyUseCase;
        this.decisionLogUseCase = decisionLogUseCase;
    }

    // ==================== ACCESS POLICY ====================

    @Tool(description = "Create a new access policy with allow/deny effect and JSON rules. "
            + "Returns the created policy with id, name, effect, rules, createdAt.")
    public Map<String, Object> createPolicy(
            @ToolParam(description = "Policy name (e.g. 'admin-full-access')") String name,
            @ToolParam(description = "Effect: 'allow' or 'deny'") String effect,
            @ToolParam(description = "JSON string defining access rules (servers, tools)") String rulesJson) {
        return accessPolicyUseCase.createPolicy(name, effect, rulesJson);
    }

    @Tool(description = "Check whether a user has access to a specific MCP server/tool. "
            + "Returns userId, server, tool, allowed flag and matched policy.")
    public Map<String, Object> checkAccess(
            @ToolParam(description = "User identifier") String userId,
            @ToolParam(description = "MCP server name") String server,
            @ToolParam(description = "Tool name") String tool) {
        return accessPolicyUseCase.checkAccess(userId, server, tool);
    }

    @Tool(description = "List all defined access policies. "
            + "Returns array of policies with id, name, effect, rules.")
    public Map<String, Object> listPolicies() {
        return accessPolicyUseCase.listPolicies();
    }

    @Tool(description = "Assign a role to a user by creating an allow policy. "
            + "Returns userId, roleName, assigned flag.")
    public Map<String, Object> assignRole(
            @ToolParam(description = "User identifier") String userId,
            @ToolParam(description = "Role name to assign") String roleName) {
        return accessPolicyUseCase.assignRole(userId, roleName);
    }

    @Tool(description = "Retrieve access audit entries for a user, optionally filtered by server. "
            + "Returns entries array with userId, server, tool, allowed, policyId, createdAt.")
    public Map<String, Object> auditAccess(
            @ToolParam(description = "User identifier") String userId,
            @ToolParam(description = "MCP server name filter (null for all)") String server,
            @ToolParam(description = "Maximum number of entries to return") int limit) {
        return accessPolicyUseCase.auditAccess(userId, server, limit);
    }

    // ==================== DECISION LOG ====================

    @Tool(description = "Record a new architectural or technical decision. "
            + "Returns the decision with id, title, context, decision, status, createdAt.")
    public Map<String, Object> recordDecision(
            @ToolParam(description = "Decision title") String title,
            @ToolParam(description = "Context/background for the decision") String context,
            @ToolParam(description = "The decision text") String decision,
            @ToolParam(description = "JSON string of alternatives considered (nullable)") String alternativesJson,
            @ToolParam(description = "Consequences of the decision (nullable)") String consequences,
            @ToolParam(description = "Status: proposed, accepted, deprecated, superseded") String status) {
        return decisionLogUseCase.recordDecision(title, context, decision, alternativesJson, consequences, status);
    }

    @Tool(description = "List decisions, optionally filtered by status and/or search text. "
            + "Returns decisions array and count.")
    public Map<String, Object> listDecisions(
            @ToolParam(description = "Filter by status (null for all)") String status,
            @ToolParam(description = "Search text in title/context/decision (null for all)") String search,
            @ToolParam(description = "Maximum number of results") int limit) {
        return decisionLogUseCase.listDecisions(status, search, limit);
    }

    @Tool(description = "Retrieve a single decision with its associated links. "
            + "Returns decision object and links array.")
    public Map<String, Object> getDecision(
            @ToolParam(description = "Decision ID") String id) {
        return decisionLogUseCase.getDecision(id);
    }

    @Tool(description = "Mark a decision as superseded by another decision. "
            + "Returns id, status, supersededBy.")
    public Map<String, Object> supersedeDecision(
            @ToolParam(description = "ID of the decision to supersede") String id,
            @ToolParam(description = "ID of the superseding decision") String supersededById) {
        return decisionLogUseCase.supersedeDecision(id, supersededById);
    }

    @Tool(description = "Create a link between a decision and an external artifact. "
            + "Link types: ticket, commit, impact, related.")
    public Map<String, Object> linkDecision(
            @ToolParam(description = "Decision ID") String decisionId,
            @ToolParam(description = "Link type: ticket, commit, impact, related") String linkType,
            @ToolParam(description = "Target identifier (ticket ID, commit hash, etc.)") String targetId,
            @ToolParam(description = "Optional description of the link") String description) {
        return decisionLogUseCase.linkDecision(decisionId, linkType, targetId, description);
    }
}
