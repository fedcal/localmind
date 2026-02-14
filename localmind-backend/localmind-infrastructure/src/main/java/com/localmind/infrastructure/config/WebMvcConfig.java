package com.localmind.infrastructure.config;

import com.localmind.infrastructure.mcp.security.CachedBodyHttpServletRequest;
import com.localmind.infrastructure.mcp.security.McpAccessInterceptor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final McpAccessInterceptor mcpAccessInterceptor;

    public WebMvcConfig(McpAccessInterceptor mcpAccessInterceptor) {
        this.mcpAccessInterceptor = mcpAccessInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(mcpAccessInterceptor)
                .addPathPatterns("/api/v1/mcp/tools/execute");
    }

    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> cachedBodyFilter() {
        FilterRegistrationBean<OncePerRequestFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain filterChain) throws ServletException, IOException {
                if ("POST".equalsIgnoreCase(request.getMethod())
                        && request.getRequestURI().contains("/mcp/tools/execute")) {
                    CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
                    filterChain.doFilter(cachedRequest, response);
                } else {
                    filterChain.doFilter(request, response);
                }
            }
        });
        registration.addUrlPatterns("/api/v1/mcp/tools/execute");
        registration.setOrder(1);
        return registration;
    }
}
