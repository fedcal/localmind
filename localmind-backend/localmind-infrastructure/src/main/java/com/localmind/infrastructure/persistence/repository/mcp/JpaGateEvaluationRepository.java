package com.localmind.infrastructure.persistence.repository.mcp;

import com.localmind.infrastructure.persistence.entity.mcp.GateEvaluationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaGateEvaluationRepository extends JpaRepository<GateEvaluationEntity, UUID> {

    List<GateEvaluationEntity> findByGateId(String gateId);
}
