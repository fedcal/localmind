package com.localmind.domain.mcp.port.out;

import com.localmind.domain.mcp.model.StandupNote;

import java.util.List;

/**
 * Output port for persisting standup notes.
 */
public interface StandupRepository {

    /**
     * Saves a standup note.
     *
     * @param standupNote the standup note to persist
     * @return the saved standup note (with generated id if new)
     */
    StandupNote save(StandupNote standupNote);

    /**
     * Finds all standup notes, ordered by standup date descending.
     *
     * @return list of all standup notes
     */
    List<StandupNote> findAll();
}
