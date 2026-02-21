package com.localmind.domain.agent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Agent {
    private String id;
    private String name;
    private AgentType type;
    private String systemPrompt;
    private List<AgentTool> tools;
}
