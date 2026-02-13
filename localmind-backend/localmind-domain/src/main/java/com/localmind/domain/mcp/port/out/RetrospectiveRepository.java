package com.localmind.domain.mcp.port.out;

import com.localmind.domain.mcp.model.ActionItem;
import com.localmind.domain.mcp.model.RetroItem;
import com.localmind.domain.mcp.model.Retrospective;

import java.util.List;
import java.util.Optional;

/**
 * Output port for persisting retrospective data.
 * Handles retrospectives, retro items, and action items.
 */
public interface RetrospectiveRepository {

    /**
     * Saves a retrospective.
     *
     * @param retrospective the retrospective to persist
     * @return the saved retrospective
     */
    Retrospective saveRetro(Retrospective retrospective);

    /**
     * Finds a retrospective by its identifier.
     *
     * @param id the retrospective id
     * @return an Optional containing the retrospective if found
     */
    Optional<Retrospective> findRetroById(String id);

    /**
     * Returns all retrospectives.
     *
     * @return list of all retrospectives
     */
    List<Retrospective> findAllRetros();

    /**
     * Saves a retro item.
     *
     * @param item the retro item to persist
     * @return the saved retro item
     */
    RetroItem saveItem(RetroItem item);

    /**
     * Finds all items belonging to a retrospective.
     *
     * @param retroId the retrospective id
     * @return list of retro items
     */
    List<RetroItem> findItemsByRetroId(String retroId);

    /**
     * Finds a retro item by its identifier.
     *
     * @param id the item id
     * @return an Optional containing the item if found
     */
    Optional<RetroItem> findItemById(String id);

    /**
     * Returns all retro items across all retrospectives.
     *
     * @return list of all retro items
     */
    List<RetroItem> findAllItems();

    /**
     * Saves an action item.
     *
     * @param actionItem the action item to persist
     * @return the saved action item
     */
    ActionItem saveActionItem(ActionItem actionItem);

    /**
     * Finds all action items belonging to a retrospective.
     *
     * @param retroId the retrospective id
     * @return list of action items
     */
    List<ActionItem> findActionItemsByRetroId(String retroId);
}
