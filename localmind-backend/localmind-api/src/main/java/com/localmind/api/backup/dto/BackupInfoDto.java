package com.localmind.api.backup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupInfoDto {
    private String id;
    private String filename;
    private Instant createdAt;
    private long sizeBytes;
    private String backupType;
    private List<String> components;
}
