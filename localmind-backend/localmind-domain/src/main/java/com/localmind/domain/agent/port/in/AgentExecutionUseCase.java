package com.localmind.domain.agent.port.in;

import com.localmind.domain.agent.model.AgentExecutionResult;
import com.localmind.domain.agent.model.AgentType;

public interface AgentExecutionUseCase {
    AgentExecutionResult execute(AgentType type, String query, String conversationId);
}
