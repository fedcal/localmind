package com.localmind.domain.document.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document {
    private String id;
    private String filename;
    private String filePath;
    private String mimeType;
    private long fileSize;
    private String fileHash;
    @Builder.Default
    private DocumentStatus status = DocumentStatus.PENDING;
    private DocumentMetadata metadata;
    @Builder.Default
    private Instant createdAt = Instant.now();
    private Instant updatedAt;
    private Instant indexedAt;
    private Double ocrConfidence;
}
