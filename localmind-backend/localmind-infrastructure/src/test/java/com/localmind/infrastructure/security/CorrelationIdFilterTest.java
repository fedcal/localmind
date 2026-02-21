package com.localmind.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

    private CorrelationIdFilter filter;

    @BeforeEach
    void setUp() {
        filter = new CorrelationIdFilter();
    }

    @Test
    void shouldGenerateCorrelationId_whenNotPresentInHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/chat");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilterInternal(request, response, filterChain);

        String correlationId = response.getHeader("X-Correlation-Id");
        assertThat(correlationId).isNotNull();
        assertThat(correlationId).hasSize(8);
        assertThat(filterChain.getRequest()).isNotNull();
    }

    @Test
    void shouldUseCorrelationId_whenPresentInHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/chat");
        request.addHeader("X-Correlation-Id", "my-corr-id");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilterInternal(request, response, filterChain);

        String correlationId = response.getHeader("X-Correlation-Id");
        assertThat(correlationId).isEqualTo("my-corr-id");
    }

    @Test
    void shouldSetResponseHeader_withCorrelationId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/documents");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getHeader("X-Correlation-Id")).isNotNull();
        assertThat(response.getHeader("X-Correlation-Id")).isNotEmpty();
    }

    @Test
    void shouldGenerateNewId_whenHeaderIsBlank() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/chat");
        request.addHeader("X-Correlation-Id", "   ");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilterInternal(request, response, filterChain);

        String correlationId = response.getHeader("X-Correlation-Id");
        assertThat(correlationId).isNotNull();
        assertThat(correlationId.trim()).isNotEmpty();
        assertThat(correlationId).isNotEqualTo("   ");
    }

    @Test
    void shouldCleanMdc_afterFilterExecution() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/chat");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(MDC.get("correlationId")).isNull();
    }
}
