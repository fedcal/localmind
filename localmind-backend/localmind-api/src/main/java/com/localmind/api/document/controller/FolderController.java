package com.localmind.api.document.controller;

import com.localmind.api.document.dto.CreateFolderRequestDto;
import com.localmind.api.document.dto.FolderConfigResponseDto;
import com.localmind.domain.document.model.FolderConfig;
import com.localmind.domain.document.port.in.FolderManagementUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/folders")
@Tag(name = "Folders", description = "Monitored folder configuration")
public class FolderController {

    private final FolderManagementUseCase folderManagement;

    public FolderController(FolderManagementUseCase folderManagement) {
        this.folderManagement = folderManagement;
    }

    @GetMapping
    public ResponseEntity<List<FolderConfigResponseDto>> listFolders() {
        List<FolderConfigResponseDto> folders = folderManagement.listFolders().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(folders);
    }

    @PostMapping
    public ResponseEntity<FolderConfigResponseDto> addFolder(@Valid @RequestBody CreateFolderRequestDto request) {
        FolderConfig config = folderManagement.addFolder(
                request.getPath(),
                request.isRecursive(),
                request.isWatchEnabled()
        );
        return ResponseEntity.ok(toDto(config));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeFolder(@PathVariable String id) {
        folderManagement.removeFolder(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/sync")
    public ResponseEntity<Void> triggerSync(@PathVariable String id) {
        folderManagement.triggerSync(id);
        return ResponseEntity.ok().build();
    }

    private FolderConfigResponseDto toDto(FolderConfig config) {
        return FolderConfigResponseDto.builder()
                .id(config.getId())
                .path(config.getPath())
                .recursive(config.isRecursive())
                .watchEnabled(config.isWatchEnabled())
                .status(config.getStatus())
                .documentCount(config.getDocumentCount())
                .lastSyncAt(config.getLastScanAt())
                .createdAt(config.getCreatedAt())
                .build();
    }
}
