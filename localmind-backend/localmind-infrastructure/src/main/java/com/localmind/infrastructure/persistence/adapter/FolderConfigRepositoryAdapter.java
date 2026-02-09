package com.localmind.infrastructure.persistence.adapter;

import com.localmind.domain.document.model.FolderConfig;
import com.localmind.domain.document.port.out.FolderConfigRepository;
import com.localmind.infrastructure.persistence.entity.FolderConfigEntity;
import com.localmind.infrastructure.persistence.repository.JpaFolderConfigRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class FolderConfigRepositoryAdapter implements FolderConfigRepository {

    private final JpaFolderConfigRepository jpaRepository;

    public FolderConfigRepositoryAdapter(JpaFolderConfigRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public FolderConfig save(FolderConfig config) {
        FolderConfigEntity entity = toEntity(config);
        FolderConfigEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<FolderConfig> findById(String id) {
        return jpaRepository.findById(UUID.fromString(id)).map(this::toDomain);
    }

    @Override
    public List<FolderConfig> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(UUID.fromString(id));
    }

    private FolderConfigEntity toEntity(FolderConfig config) {
        FolderConfigEntity.FolderConfigEntityBuilder builder = FolderConfigEntity.builder()
                .path(config.getPath())
                .recursive(config.isRecursive())
                .watchEnabled(config.isWatchEnabled())
                .lastScanAt(config.getLastScanAt())
                .documentCount(config.getDocumentCount());

        if (config.getId() != null) {
            builder.id(UUID.fromString(config.getId()));
        }

        return builder.build();
    }

    private FolderConfig toDomain(FolderConfigEntity entity) {
        return FolderConfig.builder()
                .id(entity.getId().toString())
                .path(entity.getPath())
                .recursive(entity.isRecursive())
                .watchEnabled(entity.isWatchEnabled())
                .lastScanAt(entity.getLastScanAt())
                .documentCount(entity.getDocumentCount())
                .build();
    }
}
