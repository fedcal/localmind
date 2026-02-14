package com.localmind.infrastructure.mcp.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.domain.mcp.port.in.AccessPolicyUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Map;

/**
 * Intercepts MCP tool execution requests and enforces access policies.
 * Reads X-User-Id header and checks access via AccessPolicyUseCase.
 * If no policies exist or no policy matches, access is allowed by default.
 */
@Component
public class McpAccessInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(McpAccessInterceptor.class);
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String DEFAULT_USER = "anonymous";

    private final AccessPolicyUseCase accessPolicyUseCase;
    private final ObjectMapper objectMapper;

    public McpAccessInterceptor(AccessPolicyUseCase accessPolicyUseCase) {
        this.accessPolicyUseCase = accessPolicyUseCase;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        if (!request.getRequestURI().endsWith("/mcp/tools/execute")) {
            return true;
        }

        String userId = request.getHeader(USER_ID_HEADER);
        if (userId == null || userId.isBlank()) {
            userId = DEFAULT_USER;
        }

        String toolName = null;
        String serverId = null;

        try {
            if (request instanceof CachedBodyHttpServletRequest cachedRequest) {
                byte[] body = cachedRequest.getCachedBody();
                if (body != null && body.length > 0) {
                    JsonNode node = objectMapper.readTree(body);
                    toolName = node.has("toolName") ? node.get("toolName").asText() : null;
                    serverId = node.has("serverId") ? node.get("serverId").asText() : null;
                }
            }
        } catch (IOException e) {
            log.warn("Failed to parse request body for access check", e);
        }

        if (toolName == null || toolName.isBlank()) {
            return true;
        }

        String serverForPolicy = serverId != null && !serverId.isBlank() ? serverId : "local";

        Map<String, Object> checkResult = accessPolicyUseCase.checkAccess(userId, serverForPolicy, toolName);

        Boolean allowed = (Boolean) checkResult.get("allowed");
        if (allowed != null && !allowed) {
            log.info("Access denied for user '{}' to tool '{}' on server '{}'", userId, toolName, serverForPolicy);

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");

            String policyName = "unknown";
            Object matchedPolicy = checkResult.get("matchedPolicy");
            if (matchedPolicy instanceof Map<?, ?> policyMap) {
                Object name = policyMap.get("name");
                if (name != null) {
                    policyName = name.toString();
                }
            }

            String errorJson = objectMapper.writeValueAsString(Map.of(
                    "error", "Access denied by policy: " + policyName,
                    "userId", userId,
                    "tool", toolName,
                    "server", serverForPolicy
            ));
            response.getWriter().write(errorJson);
            return false;
        }

        return true;
    }
}
