package com.localmind.api.backup.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.api.backup.dto.CreateBackupRequest;
import com.localmind.api.common.advice.GlobalExceptionHandler;
import com.localmind.domain.common.exception.LocalMindException;
import com.localmind.domain.common.exception.ResourceNotFoundException;
import com.localmind.domain.common.model.BackupComponent;
import com.localmind.domain.common.model.BackupInfo;
import com.localmind.domain.common.port.in.BackupUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.InputStream;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

class BackupControllerTest {

    private MockMvc mockMvc;
    private BackupUseCase backupUseCase;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        backupUseCase = mock(BackupUseCase.class);
        BackupController controller = new BackupController(backupUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void listBackups_shouldReturnAllBackups() throws Exception {
        BackupInfo b1 = BackupInfo.builder()
                .id("id-1")
                .filename("backup-2026-01-01.zip")
                .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
                .sizeBytes(1024)
                .backupType("manual")
                .components(EnumSet.of(BackupComponent.DATABASE))
                .build();

        BackupInfo b2 = BackupInfo.builder()
                .id("id-2")
                .filename("backup-2026-01-02.zip")
                .createdAt(Instant.parse("2026-01-02T00:00:00Z"))
                .sizeBytes(2048)
                .backupType("manual")
                .components(EnumSet.of(BackupComponent.DATABASE, BackupComponent.CONFIGURATION))
                .build();

        when(backupUseCase.listBackups()).thenReturn(List.of(b1, b2));

        mockMvc.perform(get("/api/v1/backups")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("id-1"))
                .andExpect(jsonPath("$[0].filename").value("backup-2026-01-01.zip"))
                .andExpect(jsonPath("$[0].sizeBytes").value(1024))
                .andExpect(jsonPath("$[0].backupType").value("manual"))
                .andExpect(jsonPath("$[0].components.length()").value(1))
                .andExpect(jsonPath("$[1].id").value("id-2"))
                .andExpect(jsonPath("$[1].filename").value("backup-2026-01-02.zip"))
                .andExpect(jsonPath("$[1].sizeBytes").value(2048));

        verify(backupUseCase).listBackups();
    }

    @Test
    void listBackups_shouldReturnEmptyList() throws Exception {
        when(backupUseCase.listBackups()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/backups")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(backupUseCase).listBackups();
    }

    @Test
    void createBackup_shouldReturnCreatedBackup() throws Exception {
        BackupInfo created = BackupInfo.builder()
                .id("id-new")
                .filename("backup-2026-02-21.zip")
                .createdAt(Instant.parse("2026-02-21T10:00:00Z"))
                .sizeBytes(4096)
                .backupType("manual")
                .components(EnumSet.of(BackupComponent.DATABASE, BackupComponent.CONFIGURATION))
                .build();

        when(backupUseCase.createBackup(any(Set.class))).thenReturn(created);

        CreateBackupRequest request = new CreateBackupRequest(Set.of("DATABASE", "CONFIGURATION"));

        mockMvc.perform(post("/api/v1/backups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("id-new"))
                .andExpect(jsonPath("$.filename").value("backup-2026-02-21.zip"))
                .andExpect(jsonPath("$.sizeBytes").value(4096))
                .andExpect(jsonPath("$.backupType").value("manual"))
                .andExpect(jsonPath("$.components.length()").value(2));

        verify(backupUseCase).createBackup(any(Set.class));
    }

    @Test
    void createBackup_shouldReturn500WhenServiceFails() throws Exception {
        when(backupUseCase.createBackup(any(Set.class)))
                .thenThrow(new LocalMindException("Dump failed"));

        CreateBackupRequest request = new CreateBackupRequest(Set.of("DATABASE"));

        mockMvc.perform(post("/api/v1/backups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        verify(backupUseCase).createBackup(any(Set.class));
    }

    @Test
    void downloadBackup_shouldReturnFileBytes() throws Exception {
        byte[] fileContent = "zip file content".getBytes();
        when(backupUseCase.downloadBackup("backup-test.zip")).thenReturn(fileContent);

        mockMvc.perform(get("/api/v1/backups/backup-test.zip/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"backup-test.zip\""))
                .andExpect(content().bytes(fileContent));

        verify(backupUseCase).downloadBackup("backup-test.zip");
    }

    @Test
    void downloadBackup_shouldReturn404WhenNotFound() throws Exception {
        when(backupUseCase.downloadBackup("nonexistent.zip"))
                .thenThrow(new ResourceNotFoundException("Backup file not found: nonexistent.zip"));

        mockMvc.perform(get("/api/v1/backups/nonexistent.zip/download")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Backup file not found: nonexistent.zip"));

        verify(backupUseCase).downloadBackup("nonexistent.zip");
    }

    @Test
    void restoreBackup_shouldReturn200() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "backup-restore.zip",
                "application/zip",
                "zip content bytes".getBytes()
        );

        doNothing().when(backupUseCase).restoreBackup(any(InputStream.class));

        mockMvc.perform(multipart("/api/v1/backups/restore").file(mockFile))
                .andExpect(status().isOk());

        verify(backupUseCase).restoreBackup(any(InputStream.class));
    }

    @Test
    void restoreBackup_shouldReturn500WhenRestoreFails() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "backup-bad.zip",
                "application/zip",
                "bad content".getBytes()
        );

        doThrow(new LocalMindException("Invalid backup archive"))
                .when(backupUseCase).restoreBackup(any(InputStream.class));

        mockMvc.perform(multipart("/api/v1/backups/restore").file(mockFile))
                .andExpect(status().isInternalServerError());

        verify(backupUseCase).restoreBackup(any(InputStream.class));
    }

    @Test
    void deleteBackup_shouldReturn204() throws Exception {
        doNothing().when(backupUseCase).deleteBackup("backup-to-delete.zip");

        mockMvc.perform(delete("/api/v1/backups/backup-to-delete.zip"))
                .andExpect(status().isNoContent());

        verify(backupUseCase).deleteBackup("backup-to-delete.zip");
    }

    @Test
    void deleteBackup_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Backup file not found: missing.zip"))
                .when(backupUseCase).deleteBackup("missing.zip");

        mockMvc.perform(delete("/api/v1/backups/missing.zip")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Backup file not found: missing.zip"));

        verify(backupUseCase).deleteBackup("missing.zip");
    }
}
