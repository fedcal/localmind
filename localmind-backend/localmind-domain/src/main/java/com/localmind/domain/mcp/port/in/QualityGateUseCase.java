package com.localmind.domain.mcp.port.in;

import java.util.Map;

/**
 * Input port for the quality-gate tool group.
 * Provides quality gate definition, evaluation, listing and history retrieval.
 */
public interface QualityGateUseCase {

    /**
     * Defines a new quality gate with a set of checks.
     *
     * @param name       the gate name
     * @param projectName the project name (nullable)
     * @param checksJson  JSON array of checks [{metric, operator, threshold}]
     * @return the created gate details
     */
    Map<String, Object> defineGate(String name, String projectName, String checksJson);

    /**
     * Evaluates a quality gate against provided metrics.
     *
     * @param gateId      the gate ID to evaluate
     * @param metricsJson JSON object mapping metric names to values
     * @return evaluation result with per-check pass/fail details
     */
    Map<String, Object> evaluateGate(String gateId, String metricsJson);

    /**
     * Lists all quality gates, optionally filtered by project name.
     *
     * @param projectName optional project name filter (nullable)
     * @return list of gates with count
     */
    Map<String, Object> listGates(String projectName);

    /**
     * Returns the evaluation history of a specific gate.
     *
     * @param gateId the gate ID
     * @param limit  maximum number of evaluations to return
     * @return list of evaluations ordered by date descending
     */
    Map<String, Object> getGateHistory(String gateId, int limit);
}
