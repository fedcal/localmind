package com.localmind.infrastructure.persistence.repository;

import com.localmind.infrastructure.persistence.entity.FolderConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaFolderConfigRepository extends JpaRepository<FolderConfigEntity, UUID> {
    List<FolderConfigEntity> findByWatchEnabled(boolean watchEnabled);
}
