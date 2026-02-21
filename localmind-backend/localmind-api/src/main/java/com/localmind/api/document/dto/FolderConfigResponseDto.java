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
public class FolderConfigResponseDto {
    private String id;
    private String path;
    private boolean recursive;
    private boolean watchEnabled;
    private String status;
    private int documentCount;
    private Instant lastSyncAt;
    private Instant createdAt;
}
