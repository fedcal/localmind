package com.localmind.api.common.advice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.localmind.domain.common.exception.DocumentProcessingException;
import com.localmind.domain.common.exception.LlmProviderException;
import com.localmind.domain.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @RestController
    static class TestController {

        @GetMapping("/test/not-found")
        public void notFound() {
            throw new ResourceNotFoundException("Resource not found with id: 123");
        }

        @GetMapping("/test/llm-error")
        public void llmError() {
            throw new LlmProviderException("Connection to Ollama failed");
        }

        @GetMapping("/test/doc-error")
        public void docError() {
            throw new DocumentProcessingException("Failed to parse PDF document");
        }

        @GetMapping("/test/general-error")
        public void generalError() {
            throw new RuntimeException("Unexpected failure");
        }
    }

    @BeforeEach
    void setUp() {
        TestController testController = new TestController();
        mockMvc = MockMvcBuilders.standaloneSetup(testController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void handleNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Resource not found with id: 123"))
                .andExpect(jsonPath("$.path").value("/test/not-found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void handleLlmError_shouldReturn502() throws Exception {
        mockMvc.perform(get("/test/llm-error"))
                .andExpect(status().isBadGateway())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.message").value("LLM provider error: Connection to Ollama failed"))
                .andExpect(jsonPath("$.path").value("/test/llm-error"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void handleDocumentError_shouldReturn500() throws Exception {
        mockMvc.perform(get("/test/doc-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Failed to parse PDF document"))
                .andExpect(jsonPath("$.path").value("/test/doc-error"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void handleGeneralError_shouldReturn500WithGenericMessage() throws Exception {
        mockMvc.perform(get("/test/general-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Internal server error"))
                .andExpect(jsonPath("$.path").value("/test/general-error"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
