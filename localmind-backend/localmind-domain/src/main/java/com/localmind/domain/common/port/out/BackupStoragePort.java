package com.localmind.domain.common.port.out;

import com.localmind.domain.common.model.BackupInfo;

import java.util.List;

/**
 * Output port for backup file storage.
 * Handles persistence of backup ZIP files on the filesystem.
 */
public interface BackupStoragePort {

    /**
     * Saves a backup file with the given filename and data.
     *
     * @param filename the backup filename
     * @param data the raw backup bytes
     */
    void save(String filename, byte[] data);

    /**
     * Loads a backup file by filename.
     *
     * @param filename the backup filename
     * @return the raw backup bytes
     */
    byte[] load(String filename);

    /**
     * Lists all backup files available in storage.
     *
     * @return list of backup metadata
     */
    List<BackupInfo> list();

    /**
     * Deletes a backup file by filename.
     *
     * @param filename the backup filename to delete
     */
    void delete(String filename);
}
