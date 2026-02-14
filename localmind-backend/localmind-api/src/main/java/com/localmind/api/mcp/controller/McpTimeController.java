package com.localmind.api.mcp.controller;

import com.localmind.api.mcp.dto.*;
import com.localmind.domain.mcp.port.in.TimeTrackingUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/mcp/time")
@Tag(name = "MCP Time", description = "Time tracking")
public class McpTimeController {

    private final TimeTrackingUseCase timeTrackingUseCase;

    public McpTimeController(TimeTrackingUseCase timeTrackingUseCase) {
        this.timeTrackingUseCase = timeTrackingUseCase;
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startTimer(@RequestBody StartTimerRequest request) {
        return ResponseEntity.ok(timeTrackingUseCase.startTimer(
                request.getTaskId(),
                request.getDescription()));
    }

    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stopTimer() {
        return ResponseEntity.ok(timeTrackingUseCase.stopTimer());
    }

    @PostMapping("/log")
    public ResponseEntity<Map<String, Object>> logTime(@RequestBody LogTimeRequest request) {
        return ResponseEntity.ok(timeTrackingUseCase.logTime(
                request.getTaskId(),
                request.getDurationMinutes(),
                request.getDescription(),
                request.getDate()));
    }

    @GetMapping("/timesheet")
    public ResponseEntity<Map<String, Object>> getTimesheet(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String userId) {
        return ResponseEntity.ok(timeTrackingUseCase.getTimesheet(startDate, endDate, userId));
    }
}
