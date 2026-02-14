package com.localmind.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RateLimitFilterTest {

    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        // 3 requests max, 60s window
        filter = new RateLimitFilter(3, 60);
    }

    @Test
    void requestsWithinLimit_shouldPassThrough() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/chat");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(filterChain.getRequest()).isNotNull();
    }

    @Test
    void requestsBeyondLimit_shouldReturn429() throws Exception {
        // Exhaust the 3 allowed requests
        for (int i = 0; i < 3; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/chat");
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilterInternal(req, res, new MockFilterChain());
            assertThat(res.getStatus()).isEqualTo(200);
        }

        // 4th request should be rate limited
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/chat");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getContentType()).isEqualTo("application/json");
        assertThat(response.getContentAsString()).contains("Too many requests");
        assertThat(filterChain.getRequest()).isNull();
    }

    @Test
    void afterWindowReset_requestsShouldPassAgain() throws Exception {
        // Use a very short window (1 second) for this test
        RateLimitFilter shortWindowFilter = new RateLimitFilter(1, 1);

        // First request passes
        MockHttpServletRequest req1 = new MockHttpServletRequest("GET", "/api/v1/chat");
        MockHttpServletResponse res1 = new MockHttpServletResponse();
        shortWindowFilter.doFilterInternal(req1, res1, new MockFilterChain());
        assertThat(res1.getStatus()).isEqualTo(200);

        // Second request blocked
        MockHttpServletRequest req2 = new MockHttpServletRequest("GET", "/api/v1/chat");
        MockHttpServletResponse res2 = new MockHttpServletResponse();
        shortWindowFilter.doFilterInternal(req2, res2, new MockFilterChain());
        assertThat(res2.getStatus()).isEqualTo(429);

        // Wait for window to expire
        Thread.sleep(1100);

        // After window reset, request should pass again
        MockHttpServletRequest req3 = new MockHttpServletRequest("GET", "/api/v1/chat");
        MockHttpServletResponse res3 = new MockHttpServletResponse();
        MockFilterChain chain3 = new MockFilterChain();
        shortWindowFilter.doFilterInternal(req3, res3, chain3);
        assertThat(res3.getStatus()).isEqualTo(200);
        assertThat(chain3.getRequest()).isNotNull();
    }

    @Test
    void rateLimitRemainingHeader_shouldBePresent() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/chat");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, new MockFilterChain());

        String remaining = response.getHeader("X-RateLimit-Remaining");
        assertThat(remaining).isNotNull();
        assertThat(Integer.parseInt(remaining)).isLessThanOrEqualTo(2);
    }

    @Test
    void xForwardedForHeader_shouldBeUsedAsClientIp() throws Exception {
        // Request with X-Forwarded-For from IP "10.0.0.1"
        MockHttpServletRequest req1 = new MockHttpServletRequest("GET", "/api/v1/chat");
        req1.addHeader("X-Forwarded-For", "10.0.0.1, 192.168.1.1");
        MockHttpServletResponse res1 = new MockHttpServletResponse();
        filter.doFilterInternal(req1, res1, new MockFilterChain());
        assertThat(res1.getStatus()).isEqualTo(200);

        // Requests from a different IP (default remoteAddr = 127.0.0.1) should have their own bucket
        for (int i = 0; i < 3; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/chat");
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilterInternal(req, res, new MockFilterChain());
            assertThat(res.getStatus()).isEqualTo(200);
        }

        // 127.0.0.1 bucket is exhausted, but 10.0.0.1 still has requests
        MockHttpServletRequest reqBlocked = new MockHttpServletRequest("GET", "/api/v1/chat");
        MockHttpServletResponse resBlocked = new MockHttpServletResponse();
        filter.doFilterInternal(reqBlocked, resBlocked, new MockFilterChain());
        assertThat(resBlocked.getStatus()).isEqualTo(429);

        // 10.0.0.1 should still pass (only used 1 of 3)
        MockHttpServletRequest reqXff = new MockHttpServletRequest("GET", "/api/v1/chat");
        reqXff.addHeader("X-Forwarded-For", "10.0.0.1");
        MockHttpServletResponse resXff = new MockHttpServletResponse();
        filter.doFilterInternal(reqXff, resXff, new MockFilterChain());
        assertThat(resXff.getStatus()).isEqualTo(200);
    }
}
