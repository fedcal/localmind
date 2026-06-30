package com.localmind.api.llm.controller;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.api.common.advice.GlobalExceptionHandler;
import com.localmind.domain.common.exception.ResourceNotFoundException;
import com.localmind.domain.llm.model.LlmModel;
import com.localmind.domain.llm.model.LlmProvider;
import com.localmind.domain.llm.port.in.ModelManagementUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

class ModelControllerTest {

    private MockMvc mockMvc;
    private ModelManagementUseCase modelManagementUseCase;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        modelManagementUseCase = mock(ModelManagementUseCase.class);
        ModelController controller = new ModelController(modelManagementUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void listModels_shouldReturnAll() throws Exception {
        LlmModel model1 = LlmModel.builder()
                .id("llama3")
                .name("Llama 3")
                .provider(LlmProvider.OLLAMA)
                .contextWindow(8192)
                .available(true)
                .build();

        LlmModel model2 = LlmModel.builder()
                .id("gpt-4o")
                .name("GPT-4o")
                .provider(LlmProvider.OPENAI)
                .contextWindow(128000)
                .available(true)
                .build();

        when(modelManagementUseCase.listModels()).thenReturn(List.of(model1, model2));

        mockMvc.perform(get("/api/v1/models")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("llama3"))
                .andExpect(jsonPath("$[0].name").value("Llama 3"))
                .andExpect(jsonPath("$[0].provider").value("OLLAMA"))
                .andExpect(jsonPath("$[0].contextWindow").value(8192))
                .andExpect(jsonPath("$[0].available").value(true))
                .andExpect(jsonPath("$[1].id").value("gpt-4o"))
                .andExpect(jsonPath("$[1].name").value("GPT-4o"))
                .andExpect(jsonPath("$[1].provider").value("OPENAI"))
                .andExpect(jsonPath("$[1].contextWindow").value(128000))
                .andExpect(jsonPath("$[1].available").value(true));

        verify(modelManagementUseCase).listModels();
    }

    @Test
    void getModel_shouldReturnModel() throws Exception {
        LlmModel model = LlmModel.builder()
                .id("llama3")
                .name("Llama 3")
                .provider(LlmProvider.OLLAMA)
                .contextWindow(8192)
                .available(true)
                .build();

        when(modelManagementUseCase.getModel("llama3")).thenReturn(model);

        mockMvc.perform(get("/api/v1/models/llama3")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("llama3"))
                .andExpect(jsonPath("$.name").value("Llama 3"))
                .andExpect(jsonPath("$.provider").value("OLLAMA"))
                .andExpect(jsonPath("$.contextWindow").value(8192))
                .andExpect(jsonPath("$.available").value(true));

        verify(modelManagementUseCase).getModel("llama3");
    }

    @Test
    void getModel_shouldReturn404WhenNotFound() throws Exception {
        when(modelManagementUseCase.getModel("nonexistent"))
                .thenThrow(new ResourceNotFoundException("Model not found: nonexistent"));

        mockMvc.perform(get("/api/v1/models/nonexistent")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Model not found: nonexistent"));
    }
}
