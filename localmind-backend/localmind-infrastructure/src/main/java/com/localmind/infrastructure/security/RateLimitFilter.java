package com.localmind.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final int maxRequests;
    private final long windowMs;
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public RateLimitFilter(
            @Value("${localmind.security.rate-limit.max-requests:100}") int maxRequests,
            @Value("${localmind.security.rate-limit.window-seconds:60}") int windowSeconds) {
        this.maxRequests = maxRequests;
        this.windowMs = windowSeconds * 1000L;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String clientIp = getClientIp(request);
        TokenBucket bucket = buckets.computeIfAbsent(clientIp, k -> new TokenBucket(maxRequests, windowMs));

        if (bucket.tryConsume()) {
            response.setHeader("X-RateLimit-Remaining", String.valueOf(bucket.getRemaining()));
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":429,\"message\":\"Too many requests\",\"retryAfterMs\":" + bucket.getRetryAfterMs() + "}");
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    static class TokenBucket {
        private final int maxTokens;
        private final long windowMs;
        private int tokens;
        private long windowStart;

        TokenBucket(int maxTokens, long windowMs) {
            this.maxTokens = maxTokens;
            this.windowMs = windowMs;
            this.tokens = maxTokens;
            this.windowStart = System.currentTimeMillis();
        }

        synchronized boolean tryConsume() {
            refill();
            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }

        synchronized int getRemaining() {
            refill();
            return tokens;
        }

        synchronized long getRetryAfterMs() {
            return Math.max(0, windowMs - (System.currentTimeMillis() - windowStart));
        }

        private void refill() {
            long now = System.currentTimeMillis();
            if (now - windowStart >= windowMs) {
                tokens = maxTokens;
                windowStart = now;
            }
        }
    }
}
