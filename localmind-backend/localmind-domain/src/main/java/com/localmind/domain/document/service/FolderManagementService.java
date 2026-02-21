package com.localmind.domain.document.service;

import com.localmind.domain.common.exception.ResourceNotFoundException;
import com.localmind.domain.document.model.Document;
import com.localmind.domain.document.model.FolderConfig;
import com.localmind.domain.document.port.in.DocumentIngestionPipelineUseCase;
import com.localmind.domain.document.port.in.FolderManagementUseCase;
import com.localmind.domain.document.port.out.FolderConfigRepository;

import java.time.Instant;
import java.util.List;

public class FolderManagementService implements FolderManagementUseCase {

    private final FolderConfigRepository folderConfigRepository;
    private final DocumentIngestionPipelineUseCase ingestionPipeline;
    private final PathValidationService pathValidationService;

    public FolderManagementService(FolderConfigRepository folderConfigRepository,
                                   DocumentIngestionPipelineUseCase ingestionPipeline,
                                   PathValidationService pathValidationService) {
        this.folderConfigRepository = folderConfigRepository;
        this.ingestionPipeline = ingestionPipeline;
        this.pathValidationService = pathValidationService;
    }

    @Override
    public FolderConfig addFolder(String path, boolean recursive, boolean watchEnabled) {
        String normalizedPath = pathValidationService.validateAndNormalize(path);

        FolderConfig config = FolderConfig.builder()
                .path(normalizedPath)
                .recursive(recursive)
                .watchEnabled(watchEnabled)
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();

        return folderConfigRepository.save(config);
    }

    @Override
    public List<FolderConfig> listFolders() {
        return folderConfigRepository.findAll();
    }

    @Override
    public FolderConfig getFolderById(String id) {
        return folderConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Folder config not found: " + id));
    }

    @Override
    public void removeFolder(String id) {
        folderConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Folder config not found: " + id));
        folderConfigRepository.deleteById(id);
    }

    @Override
    public List<Document> triggerSync(String folderId) {
        FolderConfig config = getFolderById(folderId);

        config.setStatus("SYNCING");
        folderConfigRepository.save(config);

        try {
            List<Document> documents = ingestionPipeline.ingestFromFolder(folderId);

            config.setStatus("ACTIVE");
            config.setLastScanAt(Instant.now());
            config.setDocumentCount(config.getDocumentCount() + documents.size());
            folderConfigRepository.save(config);

            return documents;
        } catch (Exception e) {
            config.setStatus("ERROR");
            folderConfigRepository.save(config);
            throw e;
        }
    }
}
