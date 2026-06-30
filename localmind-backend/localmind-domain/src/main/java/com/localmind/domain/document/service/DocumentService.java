package com.localmind.domain.document.service;

import com.localmind.domain.common.event.DocumentDeletedEvent;
import com.localmind.domain.common.exception.ResourceNotFoundException;
import com.localmind.domain.common.port.out.DomainEventPublisherPort;
import com.localmind.domain.document.model.Document;
import com.localmind.domain.document.port.out.DocumentChunkRepository;
import com.localmind.domain.document.port.out.DocumentRepository;
import com.localmind.domain.document.port.out.VectorStorePort;

import java.util.List;

public class DocumentService {

    private final DocumentRepository documentRepository;
    private final VectorStorePort vectorStorePort;
    private final DocumentChunkRepository documentChunkRepository;
    private final DomainEventPublisherPort eventPublisher;

    public DocumentService(DocumentRepository documentRepository,
                           VectorStorePort vectorStorePort,
                           DocumentChunkRepository documentChunkRepository) {
        this(documentRepository, vectorStorePort, documentChunkRepository, null);
    }

    public DocumentService(DocumentRepository documentRepository,
                           VectorStorePort vectorStorePort,
                           DocumentChunkRepository documentChunkRepository,
                           DomainEventPublisherPort eventPublisher) {
        this.documentRepository = documentRepository;
        this.vectorStorePort = vectorStorePort;
        this.documentChunkRepository = documentChunkRepository;
        this.eventPublisher = eventPublisher;
    }

    public Document findById(String id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));
    }

    public List<Document> findAll() {
        return documentRepository.findAll();
    }

    public void deleteById(String id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));

        // Cascade delete: vector store -> MySQL chunks -> document
        vectorStorePort.deleteByDocumentId(id);
        documentChunkRepository.deleteByDocumentId(id);
        documentRepository.deleteById(id);

        // Pubblica evento di cancellazione / Publish deletion event
        if (eventPublisher != null) {
            eventPublisher.publish(new DocumentDeletedEvent(id, document.getFilename()));
        }
    }
}
