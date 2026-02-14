package com.localmind.domain.document.service;

import com.localmind.domain.common.exception.DocumentProcessingException;
import com.localmind.domain.document.model.Document;
import com.localmind.domain.document.model.DocumentChunk;
import com.localmind.domain.document.model.DocumentStatus;
import com.localmind.domain.document.model.FolderConfig;
import com.localmind.domain.document.port.in.DocumentIngestionPipelineUseCase;
import com.localmind.domain.document.port.out.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DocumentIngestionPipelineService implements DocumentIngestionPipelineUseCase {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final TextExtractorPort textExtractorPort;
    private final ChunkingService chunkingService;
    private final VectorStorePort vectorStorePort;
    private final FileSystemScannerPort fileSystemScannerPort;
    private final FolderConfigRepository folderConfigRepository;

    public DocumentIngestionPipelineService(DocumentRepository documentRepository,
                                            DocumentChunkRepository documentChunkRepository,
                                            TextExtractorPort textExtractorPort,
                                            ChunkingService chunkingService,
                                            VectorStorePort vectorStorePort,
                                            FileSystemScannerPort fileSystemScannerPort,
                                            FolderConfigRepository folderConfigRepository) {
        this.documentRepository = documentRepository;
        this.documentChunkRepository = documentChunkRepository;
        this.textExtractorPort = textExtractorPort;
        this.chunkingService = chunkingService;
        this.vectorStorePort = vectorStorePort;
        this.fileSystemScannerPort = fileSystemScannerPort;
        this.folderConfigRepository = folderConfigRepository;
    }

    @Override
    public Document ingestFile(String filename, InputStream content, String mimeType,
                               long fileSize, String fileHash) {
        // Deduplication: check if already indexed
        Optional<Document> existing = documentRepository.findByFileHash(fileHash);
        if (existing.isPresent() && existing.get().getStatus() == DocumentStatus.INDEXED) {
            return existing.get();
        }

        // Save document as PENDING
        Document document = Document.builder()
                .filename(filename)
                .mimeType(mimeType)
                .fileSize(fileSize)
                .fileHash(fileHash)
                .status(DocumentStatus.PENDING)
                .build();
        document = documentRepository.save(document);

        try {
            // Set PROCESSING
            document.setStatus(DocumentStatus.PROCESSING);
            document = documentRepository.save(document);

            // Extract text
            String text = textExtractorPort.extract(content, mimeType);

            // Chunk
            List<DocumentChunk> chunks = chunkingService.chunk(document.getId(), text);

            // Assign UUIDs and filename to chunks
            for (DocumentChunk chunk : chunks) {
                chunk.setId(UUID.randomUUID().toString());
                chunk.setFilename(filename);
            }

            // Store chunks in MySQL
            documentChunkRepository.saveAll(chunks);

            // Store in vector store (with filename metadata for search results)
            vectorStorePort.store(chunks);

            // Set INDEXED
            document.setStatus(DocumentStatus.INDEXED);
            document.setIndexedAt(Instant.now());
            document = documentRepository.save(document);

            return document;
        } catch (Exception e) {
            document.setStatus(DocumentStatus.FAILED);
            documentRepository.save(document);
            throw new DocumentProcessingException(
                    "Failed to ingest file: " + filename + " - " + e.getMessage(), e);
        }
    }

    @Override
    public List<Document> ingestFromFolder(String folderId) {
        FolderConfig folderConfig = folderConfigRepository.findById(folderId)
                .orElseThrow(() -> new DocumentProcessingException("Folder config not found: " + folderId));

        List<FileSystemScannerPort.FileInfo> files =
                fileSystemScannerPort.scan(folderConfig.getPath(), folderConfig.isRecursive());

        List<Document> ingestedDocuments = new ArrayList<>();

        for (FileSystemScannerPort.FileInfo fileInfo : files) {
            // Deduplication by hash
            Optional<Document> existing = documentRepository.findByFileHash(fileInfo.hash());
            if (existing.isPresent() && existing.get().getStatus() == DocumentStatus.INDEXED) {
                continue;
            }

            String mimeType = fileInfo.mimeType();
            if (mimeType == null || mimeType.isBlank()) {
                mimeType = resolveMimeTypeByExtension(fileInfo.path().toString());
            }

            try (InputStream is = fileSystemScannerPort.openFile(fileInfo.path())) {
                Document doc = ingestFile(
                        fileInfo.path().getFileName().toString(),
                        is,
                        mimeType,
                        fileInfo.size(),
                        fileInfo.hash()
                );
                ingestedDocuments.add(doc);
            } catch (IOException e) {
                // Log and continue with next file
                System.err.println("Failed to open file: " + fileInfo.path() + " - " + e.getMessage());
            } catch (DocumentProcessingException e) {
                // Log and continue with next file
                System.err.println("Failed to ingest file: " + fileInfo.path() + " - " + e.getMessage());
            }
        }

        return ingestedDocuments;
    }

    private String resolveMimeTypeByExtension(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".txt")) return "text/plain";
        if (lower.endsWith(".md")) return "text/markdown";
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html";
        if (lower.endsWith(".doc")) return "application/msword";
        if (lower.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lower.endsWith(".xls")) return "application/vnd.ms-excel";
        if (lower.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (lower.endsWith(".csv")) return "text/csv";
        if (lower.endsWith(".json")) return "application/json";
        if (lower.endsWith(".xml")) return "application/xml";
        if (lower.endsWith(".rtf")) return "application/rtf";
        if (lower.endsWith(".odt")) return "application/vnd.oasis.opendocument.text";
        return "application/octet-stream";
    }
}
