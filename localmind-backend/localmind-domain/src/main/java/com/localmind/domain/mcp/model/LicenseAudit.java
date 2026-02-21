package com.localmind.domain.mcp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Domain model representing the result of a license audit
 * executed by the dependency-manager tool group.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicenseAudit {
    private String id;
    private String projectPath;
    private String projectType;
    private int totalChecked;
    private int copyleftCount;
    private String resultJson;
    private Instant createdAt;
}
