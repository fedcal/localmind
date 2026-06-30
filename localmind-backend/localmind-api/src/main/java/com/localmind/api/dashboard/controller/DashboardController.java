package com.localmind.api.dashboard.controller;

import com.localmind.api.dashboard.dto.HealthStatusDto;
import com.localmind.domain.common.model.AnalyticsOverview;
import com.localmind.domain.common.port.in.AnalyticsUseCase;
import com.localmind.domain.llm.model.LlmProvider;
import com.localmind.domain.llm.model.LlmProviderConfig;
import com.localmind.domain.llm.model.OllamaStatus;
import com.localmind.domain.llm.port.in.ProviderConfigUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "Health check e analytics / Health check and analytics")
public class DashboardController {

    private final ProviderConfigUseCase providerConfigUseCase;
    private final AnalyticsUseCase analyticsUseCase;

    public DashboardController(ProviderConfigUseCase providerConfigUseCase,
                               AnalyticsUseCase analyticsUseCase) {
        this.providerConfigUseCase = providerConfigUseCase;
        this.analyticsUseCase = analyticsUseCase;
    }

    @GetMapping("/health")
    @Operation(summary = "Controllo stato servizi / Health check of services")
    @ApiResponse(responseCode = "200", description = "Stato dei servizi / Services status")
    public ResponseEntity<HealthStatusDto> health() {
        Map<String, String> services = new HashMap<>();
        services.put("api", "UP");

        try {
            List<LlmProviderConfig> ollamaProviders = providerConfigUseCase.listAll().stream()
                    .filter(p -> p.getType() == LlmProvider.OLLAMA && p.isEnabled())
                    .toList();

            if (!ollamaProviders.isEmpty()) {
                OllamaStatus status = providerConfigUseCase.checkOllamaStatus(
                        ollamaProviders.get(0).getBaseUrl());
                services.put("ollama", status.isOnline() ? "UP" : "DOWN");
            } else {
                services.put("ollama", "N/A");
            }
        } catch (Exception e) {
            services.put("ollama", "DOWN");
        }

        String overallStatus = services.values().stream()
                .allMatch(s -> "UP".equals(s) || "N/A".equals(s)) ? "UP" : "DEGRADED";

        return ResponseEntity.ok(HealthStatusDto.builder()
                .status(overallStatus)
                .services(services)
                .build());
    }

    @GetMapping("/analytics")
    @Operation(summary = "Ottieni panoramica analytics / Get analytics overview")
    @ApiResponse(responseCode = "200", description = "Panoramica analytics / Analytics overview")
    public ResponseEntity<AnalyticsOverview> getAnalytics(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        Instant fromInstant = from != null ? Instant.parse(from) : Instant.now().minus(30, ChronoUnit.DAYS);
        Instant toInstant = to != null ? Instant.parse(to) : Instant.now();
        return ResponseEntity.ok(analyticsUseCase.getOverview(fromInstant, toInstant));
    }
}
