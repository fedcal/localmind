package com.localmind.api.document.controller;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.api.common.advice.GlobalExceptionHandler;
import com.localmind.domain.common.exception.ResourceNotFoundException;
import com.localmind.domain.document.model.Document;
import com.localmind.domain.document.model.DocumentStatus;
import com.localmind.domain.document.port.in.DocumentIngestionPipelineUseCase;
import com.localmind.domain.document.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;

class DocumentControllerTest {

    private MockMvc mockMvc;
    private DocumentIngestionPipelineUseCase ingestionPipeline;
    private DocumentService documentService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ingestionPipeline = mock(DocumentIngestionPipelineUseCase.class);
        documentService = mock(DocumentService.class);
        DocumentController controller = new DocumentController(ingestionPipeline, documentService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void upload_shouldReturnDocument() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test-document.pdf",
                "application/pdf",
                "PDF content bytes".getBytes()
        );

        Instant now = Instant.parse("2026-01-15T10:30:00Z");
        Document document = Document.builder()
                .id("doc-001")
                .filename("test-document.pdf")
                .filePath("/uploads/test-document.pdf")
                .mimeType("application/pdf")
                .fileSize(1024L)
                .status(DocumentStatus.INDEXED)
                .createdAt(now)
                .indexedAt(now)
                .build();

        when(ingestionPipeline.ingestFile(
                eq("test-document.pdf"), any(InputStream.class), eq("application/pdf"),
                anyLong(), anyString()))
                .thenReturn(document);

        mockMvc.perform(multipart("/api/v1/documents/upload").file(mockFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("doc-001"))
                .andExpect(jsonPath("$.filename").value("test-document.pdf"))
                .andExpect(jsonPath("$.filePath").value("/uploads/test-document.pdf"))
                .andExpect(jsonPath("$.mimeType").value("application/pdf"))
                .andExpect(jsonPath("$.fileSize").value(1024))
                .andExpect(jsonPath("$.status").value("INDEXED"));

        verify(ingestionPipeline).ingestFile(
                eq("test-document.pdf"), any(InputStream.class), eq("application/pdf"),
                anyLong(), anyString());
    }

    @Test
    void listDocuments_shouldReturnAll() throws Exception {
        Instant now = Instant.parse("2026-01-15T10:30:00Z");
        Document doc1 = Document.builder()
                .id("doc-001")
                .filename("report.pdf")
                .filePath("/uploads/report.pdf")
                .mimeType("application/pdf")
                .fileSize(2048L)
                .status(DocumentStatus.INDEXED)
                .createdAt(now)
                .indexedAt(now)
                .build();

        Document doc2 = Document.builder()
                .id("doc-002")
                .filename("notes.txt")
                .filePath("/uploads/notes.txt")
                .mimeType("text/plain")
                .fileSize(512L)
                .status(DocumentStatus.PENDING)
                .createdAt(now)
                .build();

        when(documentService.findAll()).thenReturn(List.of(doc1, doc2));

        mockMvc.perform(get("/api/v1/documents")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("doc-001"))
                .andExpect(jsonPath("$[0].filename").value("report.pdf"))
                .andExpect(jsonPath("$[0].status").value("INDEXED"))
                .andExpect(jsonPath("$[1].id").value("doc-002"))
                .andExpect(jsonPath("$[1].filename").value("notes.txt"))
                .andExpect(jsonPath("$[1].status").value("PENDING"));

        verify(documentService).findAll();
    }

    @Test
    void getDocument_shouldReturnDocument() throws Exception {
        Instant now = Instant.parse("2026-01-15T10:30:00Z");
        Document document = Document.builder()
                .id("doc-001")
                .filename("report.pdf")
                .filePath("/uploads/report.pdf")
                .mimeType("application/pdf")
                .fileSize(2048L)
                .status(DocumentStatus.INDEXED)
                .createdAt(now)
                .indexedAt(now)
                .build();

        when(documentService.findById("doc-001")).thenReturn(document);

        mockMvc.perform(get("/api/v1/documents/doc-001")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("doc-001"))
                .andExpect(jsonPath("$.filename").value("report.pdf"))
                .andExpect(jsonPath("$.mimeType").value("application/pdf"))
                .andExpect(jsonPath("$.fileSize").value(2048))
                .andExpect(jsonPath("$.status").value("INDEXED"));

        verify(documentService).findById("doc-001");
    }

    @Test
    void getDocument_shouldReturn404WhenNotFound() throws Exception {
        when(documentService.findById("nonexistent"))
                .thenThrow(new ResourceNotFoundException("Document not found: nonexistent"));

        mockMvc.perform(get("/api/v1/documents/nonexistent")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Document not found: nonexistent"));
    }

    @Test
    void deleteDocument_shouldReturn204() throws Exception {
        doNothing().when(documentService).deleteById("doc-001");

        mockMvc.perform(delete("/api/v1/documents/doc-001"))
                .andExpect(status().isNoContent());

        verify(documentService).deleteById("doc-001");
    }
}
