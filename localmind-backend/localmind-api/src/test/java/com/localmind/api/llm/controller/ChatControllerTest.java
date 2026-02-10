package com.localmind.api.llm.controller;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.api.common.advice.GlobalExceptionHandler;
import com.localmind.api.llm.dto.ChatRequestDto;
import com.localmind.domain.common.exception.LlmProviderException;
import com.localmind.domain.llm.model.LlmProvider;
import com.localmind.domain.llm.model.LlmResponse;
import com.localmind.domain.llm.model.TokenUsage;
import com.localmind.domain.llm.port.in.ChatUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ChatControllerTest {

    private MockMvc mockMvc;
    private ChatUseCase chatUseCase;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        chatUseCase = mock(ChatUseCase.class);
        ChatController controller = new ChatController(chatUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void chat_shouldReturnOkWithResponse() throws Exception {
        TokenUsage tokenUsage = TokenUsage.builder()
                .promptTokens(10)
                .completionTokens(20)
                .totalTokens(30)
                .build();

        LlmResponse llmResponse = LlmResponse.builder()
                .content("Hello from AI")
                .model("llama3")
                .provider(LlmProvider.OLLAMA)
                .tokenUsage(tokenUsage)
                .latencyMs(150L)
                .build();

        when(chatUseCase.chat(any())).thenReturn(llmResponse);

        ChatRequestDto request = ChatRequestDto.builder()
                .message("Hello")
                .provider("OLLAMA")
                .model("llama3")
                .temperature(0.7)
                .maxTokens(512)
                .build();

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello from AI"))
                .andExpect(jsonPath("$.model").value("llama3"))
                .andExpect(jsonPath("$.provider").value("OLLAMA"))
                .andExpect(jsonPath("$.latencyMs").value(150))
                .andExpect(jsonPath("$.tokenUsage.promptTokens").value(10))
                .andExpect(jsonPath("$.tokenUsage.completionTokens").value(20))
                .andExpect(jsonPath("$.tokenUsage.totalTokens").value(30));

        verify(chatUseCase).chat(any());
    }

    @Test
    void chat_shouldReturn502OnLlmProviderException() throws Exception {
        when(chatUseCase.chat(any())).thenThrow(new LlmProviderException("Connection refused"));

        ChatRequestDto request = ChatRequestDto.builder()
                .message("Hello")
                .provider("OLLAMA")
                .model("llama3")
                .build();

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.message").value("LLM provider error: Connection refused"));
    }
}
