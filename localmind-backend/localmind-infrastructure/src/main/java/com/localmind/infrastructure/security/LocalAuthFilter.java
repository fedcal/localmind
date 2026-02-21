package com.localmind.infrastructure.security;

import com.localmind.domain.auth.port.in.LocalAuthUseCase;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class LocalAuthFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final LocalAuthUseCase localAuthUseCase;

    public LocalAuthFilter(LocalAuthUseCase localAuthUseCase) {
        this.localAuthUseCase = localAuthUseCase;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Skip auth endpoints
        if (path.startsWith("/api/v1/auth/") || path.equals("/api/v1/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skip health, actuator, swagger
        if (path.equals("/api/v1/dashboard/health") ||
                path.startsWith("/actuator") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        // If auth not configured, pass through
        if (!localAuthUseCase.isAuthConfigured()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check Authorization header
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            sendUnauthorized(response, "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        if (!localAuthUseCase.validateToken(token)) {
            sendUnauthorized(response, "Invalid or expired token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"status\":401,\"message\":\"" + message + "\"}"
        );
    }
}
