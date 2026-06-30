package com.localmind.api.mcp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStoryRequest {
    private String title;
    private String description;
    private List<String> acceptanceCriteria;
    private int storyPoints;
    private String priority;
    private String sprintId;
}
