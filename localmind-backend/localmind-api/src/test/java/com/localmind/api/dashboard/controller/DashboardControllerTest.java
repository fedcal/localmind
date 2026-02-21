package com.localmind.api.dashboard.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.api.common.advice.GlobalExceptionHandler;
import com.localmind.domain.common.model.AnalyticsOverview;
import com.localmind.domain.common.port.in.AnalyticsUseCase;
import com.localmind.domain.llm.model.LlmProvider;
import com.localmind.domain.llm.model.LlmProviderConfig;
import com.localmind.domain.llm.model.OllamaStatus;
import com.localmind.domain.llm.port.in.ProviderConfigUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.Map;

class DashboardControllerTest {

    private MockMvc mockMvc;
    private ProviderConfigUseCase providerConfigUseCase;
    private AnalyticsUseCase analyticsUseCase;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        providerConfigUseCase = mock(ProviderConfigUseCase.class);
        analyticsUseCase = mock(AnalyticsUseCase.class);
        DashboardController controller = new DashboardController(providerConfigUseCase, analyticsUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void health_shouldReturnUpStatus() throws Exception {
        when(providerConfigUseCase.listAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/dashboard/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void health_shouldContainApiService() throws Exception {
        when(providerConfigUseCase.listAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/dashboard/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.services.api").value("UP"));
    }

    @Test
    void health_shouldContainOllamaUpWhenOnline() throws Exception {
        LlmProviderConfig ollamaConfig = LlmProviderConfig.builder()
                .id("ollama-1")
                .name("Ollama Local")
                .type(LlmProvider.OLLAMA)
                .baseUrl("http://localhost:11434")
                .enabled(true)
                .build();

        OllamaStatus onlineStatus = OllamaStatus.builder()
                .online(true)
                .version("0.1.30")
                .models(List.of(
                        OllamaStatus.OllamaModelInfo.builder()
                                .name("llama3")
                                .sizeBytes(4_000_000_000L)
                                .modifiedAt("2024-01-15T10:30:00Z")
                                .build()
                ))
                .build();

        when(providerConfigUseCase.listAll()).thenReturn(List.of(ollamaConfig));
        when(providerConfigUseCase.checkOllamaStatus("http://localhost:11434")).thenReturn(onlineStatus);

        mockMvc.perform(get("/api/v1/dashboard/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.services.ollama").value("UP"));
    }

    @Test
    void health_shouldContainOllamaDownWhenOffline() throws Exception {
        LlmProviderConfig ollamaConfig = LlmProviderConfig.builder()
                .id("ollama-1")
                .name("Ollama Local")
                .type(LlmProvider.OLLAMA)
                .baseUrl("http://localhost:11434")
                .enabled(true)
                .build();

        OllamaStatus offlineStatus = OllamaStatus.builder()
                .online(false)
                .errorMessage("Connection refused")
                .models(Collections.emptyList())
                .build();

        when(providerConfigUseCase.listAll()).thenReturn(List.of(ollamaConfig));
        when(providerConfigUseCase.checkOllamaStatus("http://localhost:11434")).thenReturn(offlineStatus);

        mockMvc.perform(get("/api/v1/dashboard/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DEGRADED"))
                .andExpect(jsonPath("$.services.ollama").value("DOWN"));
    }

    @Test
    void health_shouldContainOllamaNaWhenNoOllamaProvider() throws Exception {
        LlmProviderConfig openaiConfig = LlmProviderConfig.builder()
                .id("openai-1")
                .name("OpenAI")
                .type(LlmProvider.OPENAI)
                .enabled(true)
                .build();

        when(providerConfigUseCase.listAll()).thenReturn(List.of(openaiConfig));

        mockMvc.perform(get("/api/v1/dashboard/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.services.ollama").value("N/A"));
    }

    @Test
    void analytics_shouldReturnOverview() throws Exception {
        AnalyticsOverview overview = AnalyticsOverview.builder()
                .totalConversations(5)
                .totalDocuments(10)
                .totalTokens(5000)
                .totalCost(0.15)
                .avgResponseTimeMs(250.0)
                .tokensByProvider(Map.of("OLLAMA", 3000L, "OPENAI", 2000L))
                .costByProvider(Map.of("OLLAMA", 0.0, "OPENAI", 0.15))
                .documentsByStatus(Map.of("INDEXED", 8L, "PENDING", 2L))
                .usageTimeline(Collections.emptyList())
                .topModels(Collections.emptyList())
                .build();

        when(analyticsUseCase.getOverview(any(), any())).thenReturn(overview);

        mockMvc.perform(get("/api/v1/dashboard/analytics")
                        .param("from", "2026-01-01T00:00:00Z")
                        .param("to", "2026-02-21T00:00:00Z")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalConversations").value(5))
                .andExpect(jsonPath("$.totalDocuments").value(10))
                .andExpect(jsonPath("$.totalTokens").value(5000))
                .andExpect(jsonPath("$.totalCost").value(0.15))
                .andExpect(jsonPath("$.avgResponseTimeMs").value(250.0));
    }

    @Test
    void analytics_withoutParams_shouldUseDefaults() throws Exception {
        AnalyticsOverview overview = AnalyticsOverview.builder()
                .totalConversations(0)
                .totalDocuments(0)
                .totalTokens(0)
                .totalCost(0.0)
                .avgResponseTimeMs(0.0)
                .tokensByProvider(Collections.emptyMap())
                .costByProvider(Collections.emptyMap())
                .documentsByStatus(Collections.emptyMap())
                .usageTimeline(Collections.emptyList())
                .topModels(Collections.emptyList())
                .build();

        when(analyticsUseCase.getOverview(any(), any())).thenReturn(overview);

        mockMvc.perform(get("/api/v1/dashboard/analytics")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTokens").value(0));
    }
}
