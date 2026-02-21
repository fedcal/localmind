package com.localmind.api.document.controller;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.api.common.advice.GlobalExceptionHandler;
import com.localmind.api.document.dto.SearchRequestDto;
import com.localmind.domain.document.model.SearchResult;
import com.localmind.domain.document.port.in.DocumentSearchUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

class DocumentSearchControllerTest {

    private MockMvc mockMvc;
    private DocumentSearchUseCase searchUseCase;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        searchUseCase = mock(DocumentSearchUseCase.class);
        DocumentSearchController controller = new DocumentSearchController(searchUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void search_shouldReturnResults() throws Exception {
        SearchResult result1 = SearchResult.builder()
                .documentId("doc-001")
                .filename("report.pdf")
                .content("Machine learning overview")
                .score(0.95)
                .chunkIndex(0)
                .build();

        SearchResult result2 = SearchResult.builder()
                .documentId("doc-002")
                .filename("notes.txt")
                .content("AI concepts and terminology")
                .score(0.82)
                .chunkIndex(3)
                .build();

        when(searchUseCase.search("machine learning", 5)).thenReturn(List.of(result1, result2));

        SearchRequestDto request = SearchRequestDto.builder()
                .query("machine learning")
                .topK(5)
                .build();

        mockMvc.perform(post("/api/v1/documents/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].documentId").value("doc-001"))
                .andExpect(jsonPath("$[0].filename").value("report.pdf"))
                .andExpect(jsonPath("$[1].documentId").value("doc-002"));

        verify(searchUseCase).search("machine learning", 5);
    }

    @Test
    void search_shouldMapResultsCorrectly() throws Exception {
        SearchResult result = SearchResult.builder()
                .documentId("doc-042")
                .filename("architecture.md")
                .content("Hexagonal architecture separates domain from infrastructure")
                .score(0.91)
                .chunkIndex(7)
                .build();

        when(searchUseCase.search("hexagonal architecture", 3)).thenReturn(List.of(result));

        SearchRequestDto request = SearchRequestDto.builder()
                .query("hexagonal architecture")
                .topK(3)
                .build();

        mockMvc.perform(post("/api/v1/documents/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].documentId").value("doc-042"))
                .andExpect(jsonPath("$[0].filename").value("architecture.md"))
                .andExpect(jsonPath("$[0].content").value("Hexagonal architecture separates domain from infrastructure"))
                .andExpect(jsonPath("$[0].score").value(0.91))
                .andExpect(jsonPath("$[0].chunkIndex").value(7));

        verify(searchUseCase).search("hexagonal architecture", 3);
    }
}
