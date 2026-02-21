package com.localmind.domain.common.port.in;

import com.localmind.domain.common.model.BackupComponent;
import com.localmind.domain.common.model.BackupInfo;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * Input port for the backup and restore feature.
 * Provides operations to create, list, download, restore and delete backups.
 */
public interface BackupUseCase {

    /**
     * Creates a new backup containing the specified components.
     *
     * @param components the set of components to include in the backup
     * @return backup metadata with filename, size, and creation timestamp
     */
    BackupInfo createBackup(Set<BackupComponent> components);

    /**
     * Lists all available backups.
     *
     * @return list of backup metadata ordered by creation date descending
     */
    List<BackupInfo> listBackups();

    /**
     * Downloads a backup file by filename.
     *
     * @param filename the backup filename
     * @return the raw backup bytes (ZIP format)
     */
    byte[] downloadBackup(String filename);

    /**
     * Restores a backup from the given input stream.
     * Detects components from the ZIP entries and restores each one.
     *
     * @param data the input stream containing the backup ZIP
     */
    void restoreBackup(InputStream data);

    /**
     * Deletes a backup by filename.
     *
     * @param filename the backup filename to delete
     */
    void deleteBackup(String filename);
}
