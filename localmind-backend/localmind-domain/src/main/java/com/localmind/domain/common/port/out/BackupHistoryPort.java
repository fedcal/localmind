package com.localmind.domain.common.port.out;

import com.localmind.domain.common.model.BackupInfo;

import java.util.List;

/**
 * Output port for backup history persistence.
 * Manages backup metadata records in the database.
 */
public interface BackupHistoryPort {

    /**
     * Saves backup metadata.
     *
     * @param info the backup metadata to persist
     * @return the persisted backup metadata with generated id
     */
    BackupInfo save(BackupInfo info);

    /**
     * Retrieves all backup history records.
     *
     * @return list of backup metadata ordered by creation date descending
     */
    List<BackupInfo> findAll();

    /**
     * Deletes a backup history record by filename.
     *
     * @param filename the backup filename
     */
    void deleteByFilename(String filename);
}
