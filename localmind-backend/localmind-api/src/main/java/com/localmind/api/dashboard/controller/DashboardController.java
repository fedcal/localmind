package com.localmind.api.dashboard.controller;

import com.localmind.api.dashboard.dto.HealthStatusDto;
import com.localmind.domain.llm.model.LlmProvider;
import com.localmind.domain.llm.model.LlmProviderConfig;
import com.localmind.domain.llm.model.OllamaStatus;
import com.localmind.domain.llm.port.in.ProviderConfigUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final ProviderConfigUseCase providerConfigUseCase;

    public DashboardController(ProviderConfigUseCase providerConfigUseCase) {
        this.providerConfigUseCase = providerConfigUseCase;
    }

    @GetMapping("/health")
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
}
