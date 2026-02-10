package com.localmind.api.dashboard.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.api.common.advice.GlobalExceptionHandler;
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

class DashboardControllerTest {

    private MockMvc mockMvc;
    private ProviderConfigUseCase providerConfigUseCase;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        providerConfigUseCase = mock(ProviderConfigUseCase.class);
        DashboardController controller = new DashboardController(providerConfigUseCase);
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
}
