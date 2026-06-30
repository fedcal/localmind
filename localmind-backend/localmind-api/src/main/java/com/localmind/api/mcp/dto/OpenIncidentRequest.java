package com.localmind.api.mcp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenIncidentRequest {
    private String title;
    private String severity;
    private String description;
    private String affectedSystemsJson;
}
