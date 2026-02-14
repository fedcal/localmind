package com.localmind.infrastructure.security;

import com.localmind.domain.auth.port.in.LocalAuthUseCase;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class LocalAuthFilterTest {

    private LocalAuthUseCase localAuthUseCase;
    private LocalAuthFilter filter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        localAuthUseCase = mock(LocalAuthUseCase.class);
        filter = new LocalAuthFilter(localAuthUseCase);
        filterChain = mock(FilterChain.class);
    }

    @Test
    void shouldSkipAuthEndpoints() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(localAuthUseCase);
    }

    @Test
    void shouldSkipHealthEndpoint() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/dashboard/health");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(localAuthUseCase);
    }

    @Test
    void shouldPassThrough_whenAuthNotConfigured() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/documents");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(localAuthUseCase.isAuthConfigured()).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(localAuthUseCase).isAuthConfigured();
    }

    @Test
    void shouldPassThrough_withValidToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/documents");
        request.addHeader("Authorization", "Bearer valid-token.signature");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(localAuthUseCase.isAuthConfigured()).thenReturn(true);
        when(localAuthUseCase.validateToken("valid-token.signature")).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldReturn401_whenTokenIsMissing() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/documents");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(localAuthUseCase.isAuthConfigured()).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Missing or invalid Authorization header");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void shouldReturn401_whenTokenIsInvalid() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/documents");
        request.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(localAuthUseCase.isAuthConfigured()).thenReturn(true);
        when(localAuthUseCase.validateToken("invalid-token")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Invalid or expired token");
        verify(filterChain, never()).doFilter(any(), any());
    }
}
