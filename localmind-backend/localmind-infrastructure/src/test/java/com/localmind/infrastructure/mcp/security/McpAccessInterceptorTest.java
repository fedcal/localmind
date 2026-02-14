package com.localmind.infrastructure.mcp.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.domain.mcp.port.in.AccessPolicyUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Map;

class McpAccessInterceptorTest {

    private McpAccessInterceptor interceptor;
    private AccessPolicyUseCase accessPolicyUseCase;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        accessPolicyUseCase = mock(AccessPolicyUseCase.class);
        interceptor = new McpAccessInterceptor(accessPolicyUseCase);
        objectMapper = new ObjectMapper();
    }

    @Test
    void nonPostRequest_shouldPassThrough() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/mcp/tools/execute");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        verifyNoInteractions(accessPolicyUseCase);
    }

    @Test
    void nonExecutePath_shouldPassThrough() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/mcp/tools");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        verifyNoInteractions(accessPolicyUseCase);
    }

    @Test
    void accessAllowed_shouldReturnTrue() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "toolName", "web_search",
                "serverId", "server-1"
        ));
        CachedBodyHttpServletRequest cachedRequest = createCachedRequest(
                "/api/v1/mcp/tools/execute", body, "user-42");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(accessPolicyUseCase.checkAccess("user-42", "server-1", "web_search"))
                .thenReturn(Map.of("allowed", true));

        boolean result = interceptor.preHandle(cachedRequest, response, new Object());

        assertThat(result).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
        verify(accessPolicyUseCase).checkAccess("user-42", "server-1", "web_search");
    }

    @Test
    void accessDenied_shouldReturn403WithJsonError() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "toolName", "dangerous_tool",
                "serverId", "server-2"
        ));
        CachedBodyHttpServletRequest cachedRequest = createCachedRequest(
                "/api/v1/mcp/tools/execute", body, "user-99");
        MockHttpServletResponse response = new MockHttpServletResponse();

        Map<String, Object> denyResult = Map.of(
                "allowed", false,
                "matchedPolicy", Map.of("name", "restrict-dangerous")
        );
        when(accessPolicyUseCase.checkAccess("user-99", "server-2", "dangerous_tool"))
                .thenReturn(denyResult);

        boolean result = interceptor.preHandle(cachedRequest, response, new Object());

        assertThat(result).isFalse();
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).isEqualTo("application/json");

        String responseBody = response.getContentAsString();
        Map<?, ?> errorMap = objectMapper.readValue(responseBody, Map.class);
        assertThat(errorMap.get("error")).isEqualTo("Access denied by policy: restrict-dangerous");
        assertThat(errorMap.get("userId")).isEqualTo("user-99");
        assertThat(errorMap.get("tool")).isEqualTo("dangerous_tool");
        assertThat(errorMap.get("server")).isEqualTo("server-2");
    }

    @Test
    void missingUserIdHeader_shouldDefaultToAnonymous() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "toolName", "some_tool",
                "serverId", "server-1"
        ));
        CachedBodyHttpServletRequest cachedRequest = createCachedRequest(
                "/api/v1/mcp/tools/execute", body, null);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(accessPolicyUseCase.checkAccess("anonymous", "server-1", "some_tool"))
                .thenReturn(Map.of("allowed", true));

        boolean result = interceptor.preHandle(cachedRequest, response, new Object());

        assertThat(result).isTrue();
        verify(accessPolicyUseCase).checkAccess("anonymous", "server-1", "some_tool");
    }

    @Test
    void blankUserIdHeader_shouldDefaultToAnonymous() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "toolName", "some_tool",
                "serverId", "server-1"
        ));
        CachedBodyHttpServletRequest cachedRequest = createCachedRequest(
                "/api/v1/mcp/tools/execute", body, "   ");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(accessPolicyUseCase.checkAccess("anonymous", "server-1", "some_tool"))
                .thenReturn(Map.of("allowed", true));

        boolean result = interceptor.preHandle(cachedRequest, response, new Object());

        assertThat(result).isTrue();
        verify(accessPolicyUseCase).checkAccess("anonymous", "server-1", "some_tool");
    }

    @Test
    void invalidJsonBody_shouldPassThroughWithoutCrash() throws Exception {
        CachedBodyHttpServletRequest cachedRequest = createCachedRequest(
                "/api/v1/mcp/tools/execute", "this is not json {{{", "user-1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(cachedRequest, response, new Object());

        assertThat(result).isTrue();
        verifyNoInteractions(accessPolicyUseCase);
    }

    @Test
    void missingToolNameInBody_shouldPassThrough() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("serverId", "server-1"));
        CachedBodyHttpServletRequest cachedRequest = createCachedRequest(
                "/api/v1/mcp/tools/execute", body, "user-1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(cachedRequest, response, new Object());

        assertThat(result).isTrue();
        verifyNoInteractions(accessPolicyUseCase);
    }

    /**
     * Helper to create a CachedBodyHttpServletRequest wrapping a MockHttpServletRequest.
     */
    private CachedBodyHttpServletRequest createCachedRequest(String uri, String body, String userId)
            throws Exception {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", uri);
        mockRequest.setContentType("application/json");
        mockRequest.setContent(body.getBytes());
        if (userId != null) {
            mockRequest.addHeader("X-User-Id", userId);
        }
        return new CachedBodyHttpServletRequest(mockRequest);
    }
}
