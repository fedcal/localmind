package com.localmind.domain.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupInfo {
    private String id;
    private String filename;
    private Instant createdAt;
    private long sizeBytes;
    private String backupType;
    private Set<BackupComponent> components;
}
