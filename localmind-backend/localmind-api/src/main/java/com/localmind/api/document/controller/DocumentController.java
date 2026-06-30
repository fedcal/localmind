package com.localmind.api.document.controller;

import com.localmind.api.document.dto.DocumentDistributionDto;
import com.localmind.api.document.dto.DocumentDto;
import com.localmind.api.document.dto.DocumentStatsDto;
import com.localmind.domain.document.model.Document;
import com.localmind.domain.document.model.DocumentStats;
import com.localmind.domain.document.port.in.DocumentIngestionPipelineUseCase;
import com.localmind.domain.document.port.out.DocumentReadRepository;
import com.localmind.domain.document.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/documents")
@Tag(name = "Documents", description = "Document upload and management")
public class DocumentController {

    private final DocumentIngestionPipelineUseCase ingestionPipeline;
    private final DocumentService documentService;
    private final Optional<DocumentReadRepository> documentReadRepository;

    public DocumentController(DocumentIngestionPipelineUseCase ingestionPipeline,
                              DocumentService documentService,
                              Optional<DocumentReadRepository> documentReadRepository) {
        this.ingestionPipeline = ingestionPipeline;
        this.documentService = documentService;
        this.documentReadRepository = documentReadRepository;
    }

    @PostMapping("/upload")
    @Operation(summary = "Carica documento / Upload document")
    @ApiResponse(responseCode = "200", description = "Documento caricato / Document uploaded")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida / Bad request")
    public ResponseEntity<DocumentDto> upload(@RequestParam("file") MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        String fileHash = computeSha256(bytes);

        Document document = ingestionPipeline.ingestFile(
                file.getOriginalFilename(),
                file.getInputStream(),
                file.getContentType(),
                file.getSize(),
                fileHash
        );
        return ResponseEntity.ok(toDto(document));
    }

    @GetMapping
    @Operation(summary = "Lista documenti / List documents")
    @ApiResponse(responseCode = "200", description = "Lista documenti / Document list")
    public ResponseEntity<List<DocumentDto>> listDocuments() {
        List<DocumentDto> documents = documentService.findAll().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Dettaglio documento / Get document detail")
    @ApiResponse(responseCode = "200", description = "Documento trovato / Document found")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<DocumentDto> getDocument(@PathVariable String id) {
        Document document = documentService.findById(id);
        return ResponseEntity.ok(toDto(document));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina documento / Delete document")
    @ApiResponse(responseCode = "204", description = "Documento eliminato / Document deleted")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<Void> deleteDocument(@PathVariable String id) {
        documentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ===== CQRS Read Model Endpoints =====

    @GetMapping("/stats")
    @Operation(summary = "Statistiche documenti (CQRS) / Document statistics (CQRS)")
    @ApiResponse(responseCode = "200", description = "Statistiche documenti / Document statistics")
    public ResponseEntity<List<DocumentStatsDto>> getDocumentStats() {
        if (documentReadRepository.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        List<DocumentStatsDto> stats = documentReadRepository.get()
                .findAllWithStats().stream()
                .map(this::toStatsDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/distribution")
    @Operation(summary = "Distribuzione documenti per tipo e stato / Document distribution by type and status")
    @ApiResponse(responseCode = "200", description = "Distribuzione documenti / Document distribution")
    public ResponseEntity<DocumentDistributionDto> getDistribution() {
        if (documentReadRepository.isEmpty()) {
            return ResponseEntity.ok(DocumentDistributionDto.builder().build());
        }
        DocumentReadRepository repo = documentReadRepository.get();
        DocumentDistributionDto distribution = DocumentDistributionDto.builder()
                .typeDistribution(repo.getTypeDistribution())
                .statusDistribution(repo.getStatusDistribution())
                .build();
        return ResponseEntity.ok(distribution);
    }

    private DocumentStatsDto toStatsDto(DocumentStats stats) {
        return DocumentStatsDto.builder()
                .id(stats.getId())
                .title(stats.getTitle())
                .status(stats.getStatus())
                .chunkCount(stats.getChunkCount())
                .totalSize(stats.getTotalSize())
                .mimeType(stats.getMimeType())
                .ocrConfidence(stats.getOcrConfidence())
                .createdAt(stats.getCreatedAt())
                .build();
    }

    private DocumentDto toDto(Document doc) {
        return DocumentDto.builder()
                .id(doc.getId())
                .filename(doc.getFilename())
                .filePath(doc.getFilePath())
                .mimeType(doc.getMimeType())
                .fileSize(doc.getFileSize())
                .status(doc.getStatus().name())
                .createdAt(doc.getCreatedAt())
                .indexedAt(doc.getIndexedAt())
                .ocrConfidence(doc.getOcrConfidence())
                .build();
    }

    private String computeSha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
