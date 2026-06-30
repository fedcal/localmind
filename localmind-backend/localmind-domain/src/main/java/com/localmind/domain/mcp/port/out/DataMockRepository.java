package com.localmind.domain.mcp.port.out;

import com.localmind.domain.mcp.model.GeneratedDataset;

/**
 * Output port for persisting generated mock datasets.
 */
public interface DataMockRepository {

    /**
     * Saves a generated dataset.
     *
     * @param dataset the dataset to persist
     * @return the saved dataset (with generated id if new)
     */
    GeneratedDataset save(GeneratedDataset dataset);
}
