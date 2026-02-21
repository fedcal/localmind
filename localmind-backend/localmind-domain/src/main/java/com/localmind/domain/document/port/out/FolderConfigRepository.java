package com.localmind.domain.document.port.out;

import com.localmind.domain.document.model.FolderConfig;

import java.util.List;
import java.util.Optional;

public interface FolderConfigRepository {
    FolderConfig save(FolderConfig config);
    Optional<FolderConfig> findById(String id);
    List<FolderConfig> findAll();
    List<FolderConfig> findByWatchEnabled(boolean watchEnabled);
    void deleteById(String id);
}
