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
public class AgentExecutionResult {
    private String response;
    private List<String> citations;
    private List<String> toolCalls;
    private long latencyMs;
}
