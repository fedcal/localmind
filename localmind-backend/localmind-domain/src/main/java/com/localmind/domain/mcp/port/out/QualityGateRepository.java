package com.localmind.domain.mcp.port.out;

import com.localmind.domain.mcp.model.GateEvaluation;
import com.localmind.domain.mcp.model.QualityGate;

import java.util.List;
import java.util.Optional;

/**
 * Output port for persisting quality gates and their evaluations.
 */
public interface QualityGateRepository {

    /**
     * Saves a quality gate definition.
     *
     * @param gate the quality gate to persist
     * @return the saved quality gate
     */
    QualityGate saveGate(QualityGate gate);

    /**
     * Finds a quality gate by its ID.
     *
     * @param id the gate ID
     * @return optional containing the gate if found
     */
    Optional<QualityGate> findGateById(String id);

    /**
     * Returns all quality gates.
     *
     * @return list of all quality gates
     */
    List<QualityGate> findAllGates();

    /**
     * Saves a gate evaluation result.
     *
     * @param evaluation the evaluation to persist
     * @return the saved evaluation
     */
    GateEvaluation saveEvaluation(GateEvaluation evaluation);

    /**
     * Finds all evaluations for a specific gate.
     *
     * @param gateId the gate ID
     * @return list of evaluations for the gate
     */
    List<GateEvaluation> findEvaluationsByGateId(String gateId);
}
