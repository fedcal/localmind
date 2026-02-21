package com.localmind.domain.mcp.port.in;

import java.util.Map;

/**
 * Input port for the retrospective-manager tool group.
 * Provides retrospective creation, item management, voting, action item generation,
 * pattern detection, and smart suggestions.
 */
public interface RetrospectiveUseCase {

    /**
     * Creates a new retrospective for a given sprint with the specified format.
     *
     * @param sprintId the sprint identifier
     * @param format   the retro format (mad-sad-glad, 4ls, start-stop-continue)
     * @return result with the created retrospective details
     */
    Map<String, Object> createRetro(String sprintId, String format);

    /**
     * Adds a new item to an existing retrospective in a given category.
     *
     * @param retroId  the retrospective identifier
     * @param category the category for the item (depends on the retro format)
     * @param content  the text content of the item
     * @return result with the created item details
     */
    Map<String, Object> addRetroItem(String retroId, String category, String content);

    /**
     * Increments the vote count for a specific retro item.
     *
     * @param itemId the retro item identifier
     * @return result with the updated item details
     */
    Map<String, Object> voteRetroItem(String itemId);

    /**
     * Generates action items from the top-voted items of a retrospective.
     *
     * @param retroId the retrospective identifier
     * @param topN    the number of top-voted items to generate action items from
     * @return result with the generated action items
     */
    Map<String, Object> generateActionItems(String retroId, int topN);

    /**
     * Retrieves a retrospective with all its items and action items.
     *
     * @param retroId the retrospective identifier
     * @return result with the full retrospective details
     */
    Map<String, Object> getRetro(String retroId);

    /**
     * Detects recurring patterns across all retrospective items.
     *
     * @return result with detected patterns and their frequencies
     */
    Map<String, Object> detectPatterns();

    /**
     * Suggests retro items based on common patterns and themes.
     *
     * @param limit the maximum number of suggestions to generate
     * @return result with suggested items
     */
    Map<String, Object> suggestItems(int limit);
}
