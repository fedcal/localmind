package com.localmind.api.document.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.api.common.advice.GlobalExceptionHandler;
import com.localmind.api.document.dto.CreateFolderRequestDto;
import com.localmind.domain.common.exception.DocumentProcessingException;
import com.localmind.domain.common.exception.ResourceNotFoundException;
import com.localmind.domain.document.model.FolderConfig;
import com.localmind.domain.document.port.in.FolderManagementUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

class FolderControllerTest {

    private MockMvc mockMvc;
    private FolderManagementUseCase folderManagement;
    private ObjectMapper objectMapper;

    private static final Instant NOW = Instant.parse("2026-01-15T10:30:00Z");

    @BeforeEach
    void setUp() {
        folderManagement = mock(FolderManagementUseCase.class);
        FolderController controller = new FolderController(folderManagement);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void listFolders_shouldReturnAllFolders() throws Exception {
        FolderConfig folder1 = FolderConfig.builder()
                .id("folder-001")
                .path("/home/user/documents")
                .recursive(true)
                .watchEnabled(true)
                .status("ACTIVE")
                .documentCount(15)
                .lastScanAt(NOW)
                .createdAt(NOW)
                .build();

        FolderConfig folder2 = FolderConfig.builder()
                .id("folder-002")
                .path("/home/user/projects")
                .recursive(false)
                .watchEnabled(false)
                .status("ACTIVE")
                .documentCount(3)
                .createdAt(NOW)
                .build();

        when(folderManagement.listFolders()).thenReturn(List.of(folder1, folder2));

        mockMvc.perform(get("/api/v1/folders")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("folder-001"))
                .andExpect(jsonPath("$[0].path").value("/home/user/documents"))
                .andExpect(jsonPath("$[0].recursive").value(true))
                .andExpect(jsonPath("$[0].watchEnabled").value(true))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].documentCount").value(15))
                .andExpect(jsonPath("$[1].id").value("folder-002"))
                .andExpect(jsonPath("$[1].path").value("/home/user/projects"))
                .andExpect(jsonPath("$[1].recursive").value(false))
                .andExpect(jsonPath("$[1].watchEnabled").value(false));

        verify(folderManagement).listFolders();
    }

    @Test
    void addFolder_shouldReturnCreatedFolder() throws Exception {
        CreateFolderRequestDto request = CreateFolderRequestDto.builder()
                .path("/home/user/new-folder")
                .recursive(true)
                .watchEnabled(false)
                .build();

        FolderConfig created = FolderConfig.builder()
                .id("folder-003")
                .path("/home/user/new-folder")
                .recursive(true)
                .watchEnabled(false)
                .status("ACTIVE")
                .documentCount(0)
                .createdAt(NOW)
                .build();

        when(folderManagement.addFolder("/home/user/new-folder", true, false))
                .thenReturn(created);

        mockMvc.perform(post("/api/v1/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("folder-003"))
                .andExpect(jsonPath("$.path").value("/home/user/new-folder"))
                .andExpect(jsonPath("$.recursive").value(true))
                .andExpect(jsonPath("$.watchEnabled").value(false))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.documentCount").value(0));

        verify(folderManagement).addFolder("/home/user/new-folder", true, false);
    }

    @Test
    void addFolder_shouldReturn400OnBlankPath() throws Exception {
        CreateFolderRequestDto request = CreateFolderRequestDto.builder()
                .path("")
                .recursive(true)
                .watchEnabled(false)
                .build();

        mockMvc.perform(post("/api/v1/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("path: Path is required"));

        verifyNoInteractions(folderManagement);
    }

    @Test
    void removeFolder_shouldReturn204() throws Exception {
        doNothing().when(folderManagement).removeFolder("folder-001");

        mockMvc.perform(delete("/api/v1/folders/folder-001"))
                .andExpect(status().isNoContent());

        verify(folderManagement).removeFolder("folder-001");
    }

    @Test
    void removeFolder_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Folder not found: nonexistent"))
                .when(folderManagement).removeFolder("nonexistent");

        mockMvc.perform(delete("/api/v1/folders/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Folder not found: nonexistent"));
    }

    @Test
    void triggerSync_shouldReturn200() throws Exception {
        when(folderManagement.triggerSync("folder-001")).thenReturn(List.of());

        mockMvc.perform(post("/api/v1/folders/folder-001/sync"))
                .andExpect(status().isOk());

        verify(folderManagement).triggerSync("folder-001");
    }

    @Test
    void triggerSync_shouldReturn404WhenNotFound() throws Exception {
        when(folderManagement.triggerSync("nonexistent"))
                .thenThrow(new ResourceNotFoundException("Folder not found: nonexistent"));

        mockMvc.perform(post("/api/v1/folders/nonexistent/sync"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Folder not found: nonexistent"));
    }

    @Test
    void addFolder_shouldReturn500OnInvalidPath() throws Exception {
        CreateFolderRequestDto request = CreateFolderRequestDto.builder()
                .path("/nonexistent/invalid/path")
                .recursive(true)
                .watchEnabled(false)
                .build();

        when(folderManagement.addFolder("/nonexistent/invalid/path", true, false))
                .thenThrow(new DocumentProcessingException("Invalid folder path: /nonexistent/invalid/path"));

        mockMvc.perform(post("/api/v1/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Invalid folder path: /nonexistent/invalid/path"));

        verify(folderManagement).addFolder("/nonexistent/invalid/path", true, false);
    }
}
