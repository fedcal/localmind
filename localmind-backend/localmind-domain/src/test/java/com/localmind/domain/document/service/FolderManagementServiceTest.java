package com.localmind.domain.document.service;

import com.localmind.domain.common.exception.ResourceNotFoundException;
import com.localmind.domain.document.model.Document;
import com.localmind.domain.document.model.FolderConfig;
import com.localmind.domain.document.port.in.DocumentIngestionPipelineUseCase;
import com.localmind.domain.document.port.out.FolderConfigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FolderManagementServiceTest {

    @Mock
    private FolderConfigRepository folderConfigRepository;

    @Mock
    private DocumentIngestionPipelineUseCase ingestionPipeline;

    @Mock
    private PathValidationService pathValidationService;

    @InjectMocks
    private FolderManagementService service;

    @Test
    void addFolder_shouldValidateAndSave() {
        String rawPath = "/home/user/docs";
        String normalizedPath = "/home/user/docs";
        when(pathValidationService.validateAndNormalize(rawPath)).thenReturn(normalizedPath);
        when(folderConfigRepository.save(any(FolderConfig.class))).thenAnswer(invocation -> {
            FolderConfig config = invocation.getArgument(0);
            config.setId("folder-1");
            return config;
        });

        FolderConfig result = service.addFolder(rawPath, true, false);

        assertThat(result.getId()).isEqualTo("folder-1");
        assertThat(result.getPath()).isEqualTo(normalizedPath);
        assertThat(result.isRecursive()).isTrue();
        assertThat(result.isWatchEnabled()).isFalse();
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        assertThat(result.getCreatedAt()).isNotNull();

        verify(pathValidationService).validateAndNormalize(rawPath);
        verify(folderConfigRepository).save(any(FolderConfig.class));
    }

    @Test
    void addFolder_shouldThrowOnInvalidPath() {
        String invalidPath = "/nonexistent/path";
        when(pathValidationService.validateAndNormalize(invalidPath))
                .thenThrow(new com.localmind.domain.common.exception.DocumentProcessingException(
                        "Path does not exist: " + invalidPath));

        assertThatThrownBy(() -> service.addFolder(invalidPath, true, false))
                .isInstanceOf(com.localmind.domain.common.exception.DocumentProcessingException.class)
                .hasMessageContaining("Path does not exist");

        verify(folderConfigRepository, never()).save(any());
    }

    @Test
    void listFolders_shouldReturnAll() {
        List<FolderConfig> folders = List.of(
                FolderConfig.builder().id("f1").path("/path1").build(),
                FolderConfig.builder().id("f2").path("/path2").build()
        );
        when(folderConfigRepository.findAll()).thenReturn(folders);

        List<FolderConfig> result = service.listFolders();

        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(folders);
        verify(folderConfigRepository).findAll();
    }

    @Test
    void getFolderById_shouldReturnFolder() {
        FolderConfig folder = FolderConfig.builder().id("f1").path("/path1").build();
        when(folderConfigRepository.findById("f1")).thenReturn(Optional.of(folder));

        FolderConfig result = service.getFolderById("f1");

        assertThat(result).isEqualTo(folder);
        verify(folderConfigRepository).findById("f1");
    }

    @Test
    void getFolderById_shouldThrowWhenNotFound() {
        when(folderConfigRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getFolderById("missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void removeFolder_shouldDeleteById() {
        FolderConfig folder = FolderConfig.builder().id("f1").path("/path1").build();
        when(folderConfigRepository.findById("f1")).thenReturn(Optional.of(folder));

        service.removeFolder("f1");

        verify(folderConfigRepository).findById("f1");
        verify(folderConfigRepository).deleteById("f1");
    }

    @Test
    void triggerSync_shouldSetStatusSyncing() {
        FolderConfig config = FolderConfig.builder()
                .id("f1")
                .path("/test/path")
                .documentCount(5)
                .build();
        when(folderConfigRepository.findById("f1")).thenReturn(Optional.of(config));

        Document doc = Document.builder().id("doc-1").filename("file.pdf").build();
        when(ingestionPipeline.ingestFromFolder("f1")).thenReturn(List.of(doc));

        // Track statuses at time of each save (ArgumentCaptor captures by reference)
        List<String> savedStatuses = new ArrayList<>();
        List<Integer> savedDocCounts = new ArrayList<>();
        when(folderConfigRepository.save(any(FolderConfig.class))).thenAnswer(inv -> {
            FolderConfig c = inv.getArgument(0);
            savedStatuses.add(c.getStatus());
            savedDocCounts.add(c.getDocumentCount());
            return c;
        });

        List<Document> result = service.triggerSync("f1");

        assertThat(result).hasSize(1);
        assertThat(savedStatuses).hasSize(2);
        assertThat(savedStatuses.get(0)).isEqualTo("SYNCING");
        assertThat(savedStatuses.get(1)).isEqualTo("ACTIVE");
        assertThat(savedDocCounts.get(1)).isEqualTo(6);
    }

    @Test
    void triggerSync_shouldSetStatusErrorOnFailure() {
        FolderConfig config = FolderConfig.builder()
                .id("f1")
                .path("/test/path")
                .build();
        when(folderConfigRepository.findById("f1")).thenReturn(Optional.of(config));

        List<String> savedStatuses = new ArrayList<>();
        when(folderConfigRepository.save(any(FolderConfig.class))).thenAnswer(inv -> {
            FolderConfig c = inv.getArgument(0);
            savedStatuses.add(c.getStatus());
            return c;
        });
        when(ingestionPipeline.ingestFromFolder("f1"))
                .thenThrow(new RuntimeException("Ingestion failed"));

        assertThatThrownBy(() -> service.triggerSync("f1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ingestion failed");

        // Last saved status should be ERROR
        assertThat(savedStatuses).isNotEmpty();
        assertThat(savedStatuses.get(savedStatuses.size() - 1)).isEqualTo("ERROR");
    }
}
