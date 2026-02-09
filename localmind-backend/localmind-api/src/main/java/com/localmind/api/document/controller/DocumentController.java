package com.localmind.api.document.controller;

import com.localmind.api.document.dto.DocumentDto;
import com.localmind.domain.document.model.Document;
import com.localmind.domain.document.port.in.DocumentIngestionUseCase;
import com.localmind.domain.document.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentIngestionUseCase ingestionUseCase;
    private final DocumentService documentService;

    public DocumentController(DocumentIngestionUseCase ingestionUseCase,
                              DocumentService documentService) {
        this.ingestionUseCase = ingestionUseCase;
        this.documentService = documentService;
    }

    @PostMapping("/upload")
    public ResponseEntity<DocumentDto> upload(@RequestParam("file") MultipartFile file) throws IOException {
        Document document = ingestionUseCase.ingest(
                file.getOriginalFilename(),
                file.getInputStream(),
                file.getContentType()
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
}
