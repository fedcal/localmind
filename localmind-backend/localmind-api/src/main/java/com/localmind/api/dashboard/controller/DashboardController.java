package com.localmind.api.dashboard.controller;

import com.localmind.api.dashboard.dto.HealthStatusDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    @GetMapping("/health")
    public ResponseEntity<HealthStatusDto> health() {
        return ResponseEntity.ok(HealthStatusDto.builder()
                .status("UP")
                .services(Map.of(
                        "api", "UP"
                ))
                .build());
    }
}
