package com.localmind.domain.document.service;

import com.localmind.domain.common.exception.ResourceNotFoundException;
import com.localmind.domain.document.model.Document;
import com.localmind.domain.document.model.DocumentStatus;
import com.localmind.domain.document.port.in.DocumentIngestionUseCase;
import com.localmind.domain.document.port.out.DocumentRepository;
import com.localmind.domain.document.port.out.FolderConfigRepository;

import java.io.InputStream;
import java.util.List;

public class DocumentService implements DocumentIngestionUseCase {

    private final DocumentRepository documentRepository;
    private final FolderConfigRepository folderConfigRepository;

    public DocumentService(DocumentRepository documentRepository,
                           FolderConfigRepository folderConfigRepository) {
        this.documentRepository = documentRepository;
        this.folderConfigRepository = folderConfigRepository;
    }

    @Override
    public Document ingest(String filename, InputStream content, String mimeType) {
        Document document = Document.builder()
                .filename(filename)
                .mimeType(mimeType)
                .status(DocumentStatus.PENDING)
                .build();
        return documentRepository.save(document);
    }

    @Override
    public List<Document> ingestFromFolder(String folderId) {
        folderConfigRepository.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Folder config not found: " + folderId));
        return List.of();
    }

    public Document findById(String id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));
    }

    public List<Document> findAll() {
        return documentRepository.findAll();
    }

    public void deleteById(String id) {
        documentRepository.deleteById(id);
    }
}
