package com.localmind.api.settings.controller;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.api.common.advice.GlobalExceptionHandler;
import com.localmind.api.settings.dto.CreateProviderRequestDto;
import com.localmind.api.settings.dto.UpdateDefaultModelDto;
import com.localmind.domain.llm.model.LlmProvider;
import com.localmind.domain.llm.model.LlmProviderConfig;
import com.localmind.domain.llm.model.OllamaStatus;
import com.localmind.domain.llm.port.in.ProviderConfigUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

class SettingsControllerTest {

    private MockMvc mockMvc;
    private ProviderConfigUseCase useCase;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        useCase = mock(ProviderConfigUseCase.class);
        SettingsController controller = new SettingsController(useCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void listAll_shouldReturnProviders() throws Exception {
        LlmProviderConfig config = LlmProviderConfig.builder()
                .id("provider-1")
                .name("OpenAI Provider")
                .type(LlmProvider.OPENAI)
                .baseUrl("https://api.openai.com")
                .enabled(true)
                .defaultModel("gpt-4")
                .build();

        when(useCase.listAll()).thenReturn(List.of(config));

        mockMvc.perform(get("/api/v1/settings/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("provider-1"))
                .andExpect(jsonPath("$[0].name").value("OpenAI Provider"))
                .andExpect(jsonPath("$[0].type").value("OPENAI"))
                .andExpect(jsonPath("$[0].baseUrl").value("https://api.openai.com"))
                .andExpect(jsonPath("$[0].enabled").value(true))
                .andExpect(jsonPath("$[0].defaultModel").value("gpt-4"))
                .andExpect(jsonPath("$[0].models").isEmpty());

        verify(useCase).listAll();
    }

    @Test
    void create_shouldReturnCreatedProvider() throws Exception {
        CreateProviderRequestDto request = CreateProviderRequestDto.builder()
                .name("OpenAI")
                .type("OPENAI")
                .baseUrl("https://api.openai.com")
                .apiKey("sk-test")
                .defaultModel("gpt-4")
                .build();

        LlmProviderConfig saved = LlmProviderConfig.builder()
                .id("new-id")
                .name("OpenAI")
                .type(LlmProvider.OPENAI)
                .baseUrl("https://api.openai.com")
                .apiKey("sk-test")
                .defaultModel("gpt-4")
                .enabled(true)
                .build();

        when(useCase.save(any(LlmProviderConfig.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/settings/providers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("new-id"))
                .andExpect(jsonPath("$.name").value("OpenAI"))
                .andExpect(jsonPath("$.type").value("OPENAI"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.defaultModel").value("gpt-4"));

        verify(useCase).save(any(LlmProviderConfig.class));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(useCase).deleteById("provider-1");

        mockMvc.perform(delete("/api/v1/settings/providers/provider-1"))
                .andExpect(status().isNoContent());

        verify(useCase).deleteById("provider-1");
    }

    @Test
    void testConnection_shouldReturnResult() throws Exception {
        ProviderConfigUseCase.TestResult testResult =
                new ProviderConfigUseCase.TestResult("SUCCESS", "Connection established");

        when(useCase.testConnection("provider-1")).thenReturn(testResult);

        mockMvc.perform(post("/api/v1/settings/providers/provider-1/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Connection established"));

        verify(useCase).testConnection("provider-1");
    }

    @Test
    void listOllamaModels_shouldReturnModels() throws Exception {
        List<String> models = List.of("llama3", "codellama", "mistral");

        when(useCase.listOllamaModels("http://localhost:11434")).thenReturn(models);

        mockMvc.perform(get("/api/v1/settings/providers/ollama/models")
                        .param("baseUrl", "http://localhost:11434"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("llama3"))
                .andExpect(jsonPath("$[1]").value("codellama"))
                .andExpect(jsonPath("$[2]").value("mistral"));

        verify(useCase).listOllamaModels("http://localhost:11434");
    }

    @Test
    void getOllamaStatus_shouldReturnStatus() throws Exception {
        OllamaStatus status = OllamaStatus.builder()
                .online(true)
                .version("0.1.30")
                .models(List.of(
                        OllamaStatus.OllamaModelInfo.builder()
                                .name("llama3")
                                .sizeBytes(4_294_967_296L) // ~4.0 GB
                                .modifiedAt("2024-01-15T10:30:00Z")
                                .build()
                ))
                .build();

        when(useCase.checkOllamaStatus("http://localhost:11434")).thenReturn(status);

        mockMvc.perform(get("/api/v1/settings/providers/ollama/status")
                        .param("baseUrl", "http://localhost:11434"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.online").value(true))
                .andExpect(jsonPath("$.version").value("0.1.30"))
                .andExpect(jsonPath("$.models[0].name").value("llama3"))
                .andExpect(jsonPath("$.models[0].size").isString())
                .andExpect(jsonPath("$.models[0].modifiedAt").value("2024-01-15T10:30:00Z"))
                .andExpect(jsonPath("$.errorMessage").doesNotExist());

        verify(useCase).checkOllamaStatus("http://localhost:11434");
    }

    @Test
    void startOllama_shouldReturn200() throws Exception {
        doNothing().when(useCase).startOllama();

        mockMvc.perform(post("/api/v1/settings/providers/ollama/start"))
                .andExpect(status().isOk());

        verify(useCase).startOllama();
    }

    @Test
    void pullModelStream_shouldReturnSseEmitter() throws Exception {
        mockMvc.perform(get("/api/v1/settings/providers/ollama/models/pull/stream")
                        .param("baseUrl", "http://localhost:11434")
                        .param("modelName", "llama3"))
                .andExpect(status().isOk());
    }

    @Test
    void updateDefaultModel_shouldReturnUpdated() throws Exception {
        UpdateDefaultModelDto request = new UpdateDefaultModelDto("llama3");

        LlmProviderConfig updated = LlmProviderConfig.builder()
                .id("provider-1")
                .name("Ollama Local")
                .type(LlmProvider.OLLAMA)
                .baseUrl("http://localhost:11434")
                .defaultModel("llama3")
                .enabled(true)
                .build();

        when(useCase.updateDefaultModel("provider-1", "llama3")).thenReturn(updated);
        when(useCase.listOllamaModels("http://localhost:11434"))
                .thenReturn(List.of("llama3", "mistral"));

        mockMvc.perform(put("/api/v1/settings/providers/provider-1/default-model")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("provider-1"))
                .andExpect(jsonPath("$.defaultModel").value("llama3"))
                .andExpect(jsonPath("$.type").value("OLLAMA"))
                .andExpect(jsonPath("$.models[0]").value("llama3"))
                .andExpect(jsonPath("$.models[1]").value("mistral"));

        verify(useCase).updateDefaultModel("provider-1", "llama3");
        verify(useCase).listOllamaModels("http://localhost:11434");
    }
}
