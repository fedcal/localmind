package com.localmind.domain.common.port.out;

/**
 * Output port for database dump and restore operations.
 * Handles MySQL database export and import.
 */
public interface DatabaseDumpPort {

    /**
     * Dumps the entire database to a SQL byte array.
     *
     * @return the SQL dump as bytes
     */
    byte[] dumpDatabase();

    /**
     * Restores the database from a SQL byte array.
     *
     * @param data the SQL dump bytes to restore
     */
    void restoreDatabase(byte[] data);
}
