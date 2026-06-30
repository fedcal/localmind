package com.localmind.api.backup.controller;

import com.localmind.api.backup.dto.BackupInfoDto;
import com.localmind.api.backup.dto.CreateBackupRequest;
import com.localmind.domain.common.model.BackupComponent;
import com.localmind.domain.common.model.BackupInfo;
import com.localmind.domain.common.port.in.BackupUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/backups")
@Tag(name = "Backups", description = "Backup and restore operations")
public class BackupController {

    private final BackupUseCase backupUseCase;

    public BackupController(BackupUseCase backupUseCase) {
        this.backupUseCase = backupUseCase;
    }

    @GetMapping
    @Operation(summary = "Lista backup / List backups")
    @ApiResponse(responseCode = "200", description = "Lista backup / Backup list")
    public ResponseEntity<List<BackupInfoDto>> listBackups() {
        List<BackupInfoDto> backups = backupUseCase.listBackups().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(backups);
    }

    @PostMapping
    @Operation(summary = "Crea backup / Create backup")
    @ApiResponse(responseCode = "200", description = "Backup creato / Backup created")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida / Bad request")
    public ResponseEntity<BackupInfoDto> createBackup(@RequestBody CreateBackupRequest request) {
        Set<BackupComponent> components = request.getComponents().stream()
                .map(BackupComponent::valueOf)
                .collect(Collectors.toSet());

        BackupInfo backupInfo = backupUseCase.createBackup(components);
        return ResponseEntity.ok(toDto(backupInfo));
    }

    @GetMapping("/{filename}/download")
    @Operation(summary = "Scarica backup / Download backup")
    @ApiResponse(responseCode = "200", description = "File backup / Backup file")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<byte[]> downloadBackup(@PathVariable String filename) {
        byte[] data = backupUseCase.downloadBackup(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(data.length)
                .body(data);
    }

    @PostMapping("/restore")
    @Operation(summary = "Ripristina backup / Restore backup")
    @ApiResponse(responseCode = "200", description = "Backup ripristinato / Backup restored")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida / Bad request")
    public ResponseEntity<Void> restoreBackup(@RequestParam("file") MultipartFile file) throws IOException {
        backupUseCase.restoreBackup(file.getInputStream());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{filename}")
    @Operation(summary = "Elimina backup / Delete backup")
    @ApiResponse(responseCode = "204", description = "Backup eliminato / Backup deleted")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<Void> deleteBackup(@PathVariable String filename) {
        backupUseCase.deleteBackup(filename);
        return ResponseEntity.noContent().build();
    }

    private BackupInfoDto toDto(BackupInfo info) {
        List<String> componentNames = info.getComponents() != null
                ? info.getComponents().stream().map(BackupComponent::name).sorted().toList()
                : List.of();

        return BackupInfoDto.builder()
                .id(info.getId())
                .filename(info.getFilename())
                .createdAt(info.getCreatedAt())
                .sizeBytes(info.getSizeBytes())
                .backupType(info.getBackupType())
                .components(componentNames)
                .build();
    }
}
