package com.localmind.domain.mcp.port.out;

import com.localmind.domain.mcp.model.DecisionLink;
import com.localmind.domain.mcp.model.DecisionRecord;

import java.util.List;
import java.util.Optional;

/**
 * Output port for persisting decision records and decision links.
 */
public interface DecisionLogRepository {

    /**
     * Saves a decision record.
     *
     * @param record the decision record to persist
     * @return the saved decision record
     */
    DecisionRecord save(DecisionRecord record);

    /**
     * Returns all decision records.
     *
     * @return list of all decision records
     */
    List<DecisionRecord> findAll();

    /**
     * Finds a decision record by its identifier.
     *
     * @param id the decision id
     * @return an Optional containing the decision if found
     */
    Optional<DecisionRecord> findById(String id);

    /**
     * Saves a decision link.
     *
     * @param link the decision link to persist
     * @return the saved decision link
     */
    DecisionLink saveLink(DecisionLink link);

    /**
     * Finds all links associated with a decision.
     *
     * @param decisionId the decision id
     * @return list of links for the decision
     */
    List<DecisionLink> findLinksByDecisionId(String decisionId);
}
