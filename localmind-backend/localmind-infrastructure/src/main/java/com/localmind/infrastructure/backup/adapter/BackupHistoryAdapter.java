package com.localmind.infrastructure.backup.adapter;

import com.localmind.domain.common.model.BackupComponent;
import com.localmind.domain.common.model.BackupInfo;
import com.localmind.domain.common.port.out.BackupHistoryPort;
import com.localmind.infrastructure.persistence.entity.BackupHistoryEntity;
import com.localmind.infrastructure.persistence.repository.JpaBackupHistoryRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class BackupHistoryAdapter implements BackupHistoryPort {

    private final JpaBackupHistoryRepository jpaRepository;

    public BackupHistoryAdapter(JpaBackupHistoryRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public BackupInfo save(BackupInfo info) {
        BackupHistoryEntity entity = toEntity(info);
        BackupHistoryEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<BackupInfo> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void deleteByFilename(String filename) {
        jpaRepository.deleteByFilename(filename);
    }

    private BackupHistoryEntity toEntity(BackupInfo info) {
        BackupHistoryEntity.BackupHistoryEntityBuilder builder = BackupHistoryEntity.builder()
                .filename(info.getFilename())
                .sizeBytes(info.getSizeBytes())
                .backupType(info.getBackupType())
                .components(componentsToString(info.getComponents()))
                .createdAt(info.getCreatedAt());

        if (info.getId() != null) {
            builder.id(UUID.fromString(info.getId()));
        }

        return builder.build();
    }

    private BackupInfo toDomain(BackupHistoryEntity entity) {
        return BackupInfo.builder()
                .id(entity.getId().toString())
                .filename(entity.getFilename())
                .sizeBytes(entity.getSizeBytes())
                .backupType(entity.getBackupType())
                .components(stringToComponents(entity.getComponents()))
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private String componentsToString(Set<BackupComponent> components) {
        if (components == null || components.isEmpty()) {
            return "";
        }
        return components.stream()
                .map(BackupComponent::name)
                .sorted()
                .collect(Collectors.joining(","));
    }

    private Set<BackupComponent> stringToComponents(String components) {
        if (components == null || components.isBlank()) {
            return Collections.emptySet();
        }
        return Arrays.stream(components.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(BackupComponent::valueOf)
                .collect(Collectors.toSet());
    }
}
