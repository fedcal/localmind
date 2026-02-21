package com.localmind.infrastructure.backup.adapter;

import com.localmind.domain.common.exception.LocalMindException;
import com.localmind.domain.common.exception.ResourceNotFoundException;
import com.localmind.domain.common.model.BackupInfo;
import com.localmind.domain.common.port.out.BackupStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Component
public class LocalBackupStorageAdapter implements BackupStoragePort {

    private final Path backupDirectory;

    public LocalBackupStorageAdapter(@Value("${localmind.backup.directory:./backups}") String directory) {
        this.backupDirectory = Paths.get(directory);
    }

    @Override
    public void save(String filename, byte[] data) {
        try {
            Files.createDirectories(backupDirectory);
            Path filePath = backupDirectory.resolve(filename);
            Files.write(filePath, data);
        } catch (IOException e) {
            throw new LocalMindException("Failed to save backup file: " + filename, e);
        }
    }

    @Override
    public byte[] load(String filename) {
        Path filePath = backupDirectory.resolve(filename);
        if (!Files.exists(filePath)) {
            throw new ResourceNotFoundException("Backup file not found: " + filename);
        }
        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new LocalMindException("Failed to load backup file: " + filename, e);
        }
    }

    @Override
    public List<BackupInfo> list() {
        if (!Files.exists(backupDirectory)) {
            return Collections.emptyList();
        }
        try (Stream<Path> paths = Files.list(backupDirectory)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".zip"))
                    .map(this::toBackupInfo)
                    .toList();
        } catch (IOException e) {
            throw new LocalMindException("Failed to list backup files", e);
        }
    }

    @Override
    public void delete(String filename) {
        Path filePath = backupDirectory.resolve(filename);
        if (!Files.exists(filePath)) {
            throw new ResourceNotFoundException("Backup file not found: " + filename);
        }
        try {
            Files.delete(filePath);
        } catch (IOException e) {
            throw new LocalMindException("Failed to delete backup file: " + filename, e);
        }
    }

    private BackupInfo toBackupInfo(Path path) {
        try {
            return BackupInfo.builder()
                    .filename(path.getFileName().toString())
                    .sizeBytes(Files.size(path))
                    .createdAt(Files.getLastModifiedTime(path).toInstant())
                    .build();
        } catch (IOException e) {
            return BackupInfo.builder()
                    .filename(path.getFileName().toString())
                    .sizeBytes(0)
                    .createdAt(Instant.now())
                    .build();
        }
    }
}
