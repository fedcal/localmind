package com.localmind.domain.document.service;

import com.localmind.domain.common.exception.ResourceNotFoundException;
import com.localmind.domain.document.model.Document;
import com.localmind.domain.document.port.out.DocumentChunkRepository;
import com.localmind.domain.document.port.out.DocumentRepository;
import com.localmind.domain.document.port.out.VectorStorePort;

import java.util.List;

public class DocumentService {

    private final DocumentRepository documentRepository;
    private final VectorStorePort vectorStorePort;
    private final DocumentChunkRepository documentChunkRepository;

    public DocumentService(DocumentRepository documentRepository,
                           VectorStorePort vectorStorePort,
                           DocumentChunkRepository documentChunkRepository) {
        this.documentRepository = documentRepository;
        this.vectorStorePort = vectorStorePort;
        this.documentChunkRepository = documentChunkRepository;
    }

    public Document findById(String id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));
    }

    public List<Document> findAll() {
        return documentRepository.findAll();
    }

    public void deleteById(String id) {
        documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));

        // Cascade delete: vector store -> MySQL chunks -> document
        vectorStorePort.deleteByDocumentId(id);
        documentChunkRepository.deleteByDocumentId(id);
        documentRepository.deleteById(id);
    }
}
