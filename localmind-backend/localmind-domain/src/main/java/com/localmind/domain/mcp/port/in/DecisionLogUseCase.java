package com.localmind.domain.mcp.port.in;

import java.util.Map;

/**
 * Input port for the decision-log tool group.
 * Provides recording, querying, superseding and linking of architectural decision records.
 */
public interface DecisionLogUseCase {

    /**
     * Records a new decision.
     *
     * @param title            decision title
     * @param context          context/background for the decision
     * @param decision         the decision text
     * @param alternativesJson JSON string of alternatives considered (nullable)
     * @param consequences     consequences of the decision (nullable)
     * @param status           initial status (proposed, accepted, deprecated, superseded)
     * @return result with id, title, context, decision, alternatives, consequences, status, createdAt
     */
    Map<String, Object> recordDecision(String title, String context, String decision,
                                        String alternativesJson, String consequences, String status);

    /**
     * Lists decisions, optionally filtered by status and/or search text.
     *
     * @param status filter by status (nullable)
     * @param search search text in title/context/decision (nullable)
     * @param limit  maximum number of results
     * @return result with decisions array and count
     */
    Map<String, Object> listDecisions(String status, String search, int limit);

    /**
     * Retrieves a single decision with its associated links.
     *
     * @param id the decision id
     * @return result with decision object and links array
     */
    Map<String, Object> getDecision(String id);

    /**
     * Marks a decision as superseded by another decision.
     *
     * @param id             the decision id to supersede
     * @param supersededById the id of the superseding decision
     * @return result with id, status, supersededBy
     */
    Map<String, Object> supersedeDecision(String id, String supersededById);

    /**
     * Creates a link between a decision and an external artifact.
     *
     * @param decisionId  the decision id
     * @param linkType    the type of link: ticket, commit, impact, related
     * @param targetId    the target identifier
     * @param description optional description of the link
     * @return result with id, decisionId, linkType, targetId, description
     */
    Map<String, Object> linkDecision(String decisionId, String linkType, String targetId, String description);
}
