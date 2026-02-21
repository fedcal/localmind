package com.localmind.domain.document.port.in;

import com.localmind.domain.document.model.Document;
import com.localmind.domain.document.model.FolderConfig;

import java.util.List;

public interface FolderManagementUseCase {
    FolderConfig addFolder(String path, boolean recursive, boolean watchEnabled);
    List<FolderConfig> listFolders();
    FolderConfig getFolderById(String id);
    void removeFolder(String id);
    List<Document> triggerSync(String folderId);
}
