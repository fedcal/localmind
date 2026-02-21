package com.localmind.api.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDto {
    private String id;
    private String filename;
    private String filePath;
    private String mimeType;
    private long fileSize;
    private String status;
    private Instant createdAt;
    private Instant indexedAt;
    private Double ocrConfidence;
}
