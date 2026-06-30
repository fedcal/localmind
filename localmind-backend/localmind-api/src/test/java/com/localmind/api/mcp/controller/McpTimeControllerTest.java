package com.localmind.api.mcp.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.api.common.advice.GlobalExceptionHandler;
import com.localmind.api.mcp.dto.LogTimeRequest;
import com.localmind.api.mcp.dto.StartTimerRequest;
import com.localmind.domain.mcp.port.in.TimeTrackingUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

class McpTimeControllerTest {

    private MockMvc mockMvc;
    private TimeTrackingUseCase timeTrackingUseCase;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        timeTrackingUseCase = mock(TimeTrackingUseCase.class);
        McpTimeController controller = new McpTimeController(timeTrackingUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void startTimer_shouldReturnTimerInfo() throws Exception {
        StartTimerRequest request = StartTimerRequest.builder()
                .taskId("TASK-101")
                .description("Working on feature X")
                .build();

        Map<String, Object> serviceResult = Map.of(
                "id", "entry-1",
                "taskId", "TASK-101",
                "timerStartedAt", "2025-05-10T09:00:00",
                "status", "running"
        );
        when(timeTrackingUseCase.startTimer("TASK-101", "Working on feature X")).thenReturn(serviceResult);

        mockMvc.perform(post("/api/v1/mcp/time/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("entry-1"))
                .andExpect(jsonPath("$.taskId").value("TASK-101"))
                .andExpect(jsonPath("$.status").value("running"));

        verify(timeTrackingUseCase).startTimer("TASK-101", "Working on feature X");
    }

    @Test
    void stopTimer_shouldReturnStoppedTimerInfo() throws Exception {
        Map<String, Object> serviceResult = Map.of(
                "id", "entry-1",
                "taskId", "TASK-101",
                "durationMinutes", 45,
                "durationFormatted", "0h 45m",
                "stoppedAt", "2025-05-10T09:45:00"
        );
        when(timeTrackingUseCase.stopTimer()).thenReturn(serviceResult);

        mockMvc.perform(post("/api/v1/mcp/time/stop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("entry-1"))
                .andExpect(jsonPath("$.taskId").value("TASK-101"))
                .andExpect(jsonPath("$.durationMinutes").value(45))
                .andExpect(jsonPath("$.durationFormatted").value("0h 45m"))
                .andExpect(jsonPath("$.stoppedAt").value("2025-05-10T09:45:00"));

        verify(timeTrackingUseCase).stopTimer();
    }

    @Test
    void logTime_shouldReturnLoggedEntry() throws Exception {
        LogTimeRequest request = LogTimeRequest.builder()
                .taskId("TASK-202")
                .durationMinutes(120)
                .description("Code review")
                .date("2025-05-10")
                .build();

        Map<String, Object> serviceResult = Map.of(
                "id", "entry-2",
                "taskId", "TASK-202",
                "durationMinutes", 120,
                "date", "2025-05-10",
                "saved", true
        );
        when(timeTrackingUseCase.logTime("TASK-202", 120, "Code review", "2025-05-10")).thenReturn(serviceResult);

        mockMvc.perform(post("/api/v1/mcp/time/log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("entry-2"))
                .andExpect(jsonPath("$.taskId").value("TASK-202"))
                .andExpect(jsonPath("$.durationMinutes").value(120))
                .andExpect(jsonPath("$.date").value("2025-05-10"))
                .andExpect(jsonPath("$.saved").value(true));

        verify(timeTrackingUseCase).logTime("TASK-202", 120, "Code review", "2025-05-10");
    }

    @Test
    void getTimesheet_withAllParams_shouldReturnTimesheet() throws Exception {
        Map<String, Object> serviceResult = Map.of(
                "entries", List.of(
                        Map.of("taskId", "TASK-101", "durationMinutes", 45),
                        Map.of("taskId", "TASK-202", "durationMinutes", 120)
                ),
                "totalMinutes", 165,
                "totalFormatted", "2h 45m"
        );
        when(timeTrackingUseCase.getTimesheet("2025-05-01", "2025-05-10", "user-1")).thenReturn(serviceResult);

        mockMvc.perform(get("/api/v1/mcp/time/timesheet")
                        .param("startDate", "2025-05-01")
                        .param("endDate", "2025-05-10")
                        .param("userId", "user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMinutes").value(165))
                .andExpect(jsonPath("$.totalFormatted").value("2h 45m"))
                .andExpect(jsonPath("$.entries.length()").value(2));

        verify(timeTrackingUseCase).getTimesheet("2025-05-01", "2025-05-10", "user-1");
    }

    @Test
    void getTimesheet_withNoParams_shouldPassNullsToUseCase() throws Exception {
        Map<String, Object> serviceResult = Map.of(
                "entries", List.of(),
                "totalMinutes", 0,
                "totalFormatted", "0h 0m"
        );
        when(timeTrackingUseCase.getTimesheet(null, null, null)).thenReturn(serviceResult);

        mockMvc.perform(get("/api/v1/mcp/time/timesheet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMinutes").value(0))
                .andExpect(jsonPath("$.entries.length()").value(0));

        verify(timeTrackingUseCase).getTimesheet(null, null, null);
    }

    @Test
    void stopTimer_whenServiceThrows_shouldReturn500() throws Exception {
        when(timeTrackingUseCase.stopTimer()).thenThrow(new RuntimeException("No active timer"));

        mockMvc.perform(post("/api/v1/mcp/time/stop"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));

        verify(timeTrackingUseCase).stopTimer();
    }
}
