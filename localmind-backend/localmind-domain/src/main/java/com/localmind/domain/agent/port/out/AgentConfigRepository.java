package com.localmind.domain.agent.port.out;

import com.localmind.domain.agent.model.Agent;
import com.localmind.domain.agent.model.AgentType;

import java.util.List;
import java.util.Optional;

public interface AgentConfigRepository {
    Optional<Agent> findByType(AgentType type);
    List<Agent> findAll();
    Agent save(Agent agent);
}
