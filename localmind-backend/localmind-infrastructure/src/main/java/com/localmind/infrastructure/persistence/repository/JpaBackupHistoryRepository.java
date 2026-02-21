package com.localmind.infrastructure.persistence.repository;

import com.localmind.infrastructure.persistence.entity.BackupHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaBackupHistoryRepository extends JpaRepository<BackupHistoryEntity, UUID> {

    Optional<BackupHistoryEntity> findByFilename(String filename);

    void deleteByFilename(String filename);
}
