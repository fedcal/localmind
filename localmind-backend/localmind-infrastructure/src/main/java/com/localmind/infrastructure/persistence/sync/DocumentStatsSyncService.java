package com.localmind.infrastructure.persistence.sync;

import com.localmind.domain.common.event.DocumentIndexedEvent;
import com.localmind.domain.document.model.Document;
import com.localmind.domain.document.port.out.DocumentChunkRepository;
import com.localmind.domain.document.port.out.DocumentRepository;
import com.localmind.infrastructure.persistence.entity.DocumentStatsCacheEntity;
import com.localmind.infrastructure.persistence.repository.DocumentStatsCacheJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Servizio di sincronizzazione per il modello di lettura delle statistiche documenti (CQRS).
 * Ascolta gli eventi di indicizzazione documenti e sincronizza periodicamente.
 *
 * Sync service for the document statistics read model (CQRS).
 * Listens to document indexed events and synchronizes periodically.
 */
@Component
public class DocumentStatsSyncService {

    private static final Logger log = LoggerFactory.getLogger(DocumentStatsSyncService.class);

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final DocumentStatsCacheJpaRepository statsCacheJpaRepository;

    public DocumentStatsSyncService(DocumentRepository documentRepository,
                                     DocumentChunkRepository documentChunkRepository,
                                     DocumentStatsCacheJpaRepository statsCacheJpaRepository) {
        this.documentRepository = documentRepository;
        this.documentChunkRepository = documentChunkRepository;
        this.statsCacheJpaRepository = statsCacheJpaRepository;
    }

    /**
     * Ascolta l'evento DocumentIndexedEvent e aggiorna le statistiche.
     * Listens to DocumentIndexedEvent and updates the stats.
     */
    @EventListener
    public void onDocumentIndexed(DocumentIndexedEvent event) {
        try {
            syncSingleDocument(event.getDocumentId());
            log.debug("Synced document stats for documentId={}", event.getDocumentId());
        } catch (Exception e) {
            log.warn("Failed to sync document stats for documentId={}: {}",
                    event.getDocumentId(), e.getMessage());
        }
    }

    /**
     * Sincronizzazione periodica di tutti i documenti (ogni ora).
     * Periodic sync of all documents (every hour).
     */
    @Scheduled(fixedRate = 3600000)
    public void syncAll() {
        try {
            List<Document> documents = documentRepository.findAll();
            int synced = 0;
            for (Document document : documents) {
                upsertStats(document);
                synced++;
            }
            log.info("CQRS document stats sync completed: {} documents synced", synced);
        } catch (Exception e) {
            log.error("Failed to sync document stats: {}", e.getMessage(), e);
        }
    }

    /**
     * Sincronizza un singolo documento per ID.
     * Syncs a single document by ID.
     */
    public void syncSingleDocument(String documentId) {
        Optional<Document> document = documentRepository.findById(documentId);
        document.ifPresent(this::upsertStats);
    }

    private void upsertStats(Document document) {
        UUID uuid = UUID.fromString(document.getId());
        int chunkCount = documentChunkRepository.findByDocumentId(document.getId()).size();

        DocumentStatsCacheEntity entity = statsCacheJpaRepository.findById(uuid)
                .orElse(DocumentStatsCacheEntity.builder()
                        .id(uuid)
                        .createdAt(document.getCreatedAt() != null ? document.getCreatedAt() : Instant.now())
                        .build());

        entity.setTitle(document.getFilename());
        entity.setStatus(document.getStatus() != null ? document.getStatus().name() : "UNKNOWN");
        entity.setChunkCount(chunkCount);
        entity.setTotalSize(document.getFileSize());
        entity.setMimeType(document.getMimeType());
        entity.setOcrConfidence(document.getOcrConfidence());

        statsCacheJpaRepository.save(entity);
    }
}
