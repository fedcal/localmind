package com.localmind.api.document.controller;

import com.localmind.api.document.dto.DocumentDto;
import com.localmind.domain.document.model.Document;
import com.localmind.domain.document.port.in.DocumentIngestionPipelineUseCase;
import com.localmind.domain.document.service.DocumentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
@Tag(name = "Documents", description = "Document upload and management")
public class DocumentController {

    private final DocumentIngestionPipelineUseCase ingestionPipeline;
    private final DocumentService documentService;

    public DocumentController(DocumentIngestionPipelineUseCase ingestionPipeline,
                              DocumentService documentService) {
        this.ingestionPipeline = ingestionPipeline;
        this.documentService = documentService;
    }

    @PostMapping("/upload")
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
    public ResponseEntity<List<DocumentDto>> listDocuments() {
        List<DocumentDto> documents = documentService.findAll().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDto> getDocument(@PathVariable String id) {
        Document document = documentService.findById(id);
        return ResponseEntity.ok(toDto(document));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable String id) {
        documentService.deleteById(id);
        return ResponseEntity.noContent().build();
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
