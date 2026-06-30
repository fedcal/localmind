package com.localmind.api.document.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderConfigDto {
    private String id;
    @NotBlank(message = "Path is required")
    private String path;
    @Builder.Default
    private boolean recursive = true;
    @Builder.Default
    private boolean watchEnabled = false;
    private Instant lastScanAt;
    private int documentCount;
}
