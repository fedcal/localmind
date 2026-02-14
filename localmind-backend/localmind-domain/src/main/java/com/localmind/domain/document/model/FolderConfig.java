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
public class FolderConfig {
    private String id;
    private String path;
    @Builder.Default
    private boolean recursive = true;
    @Builder.Default
    private boolean watchEnabled = false;
    @Builder.Default
    private String status = "ACTIVE";
    private Instant lastScanAt;
    private int documentCount;
    @Builder.Default
    private Instant createdAt = Instant.now();
}
