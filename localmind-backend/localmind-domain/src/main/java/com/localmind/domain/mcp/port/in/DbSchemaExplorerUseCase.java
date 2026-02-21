package com.localmind.domain.mcp.port.in;

import java.util.Map;

/**
 * Input port for the db-schema-explorer tool group.
 * Provides database schema exploration, table description, index suggestions, and ERD generation.
 */
public interface DbSchemaExplorerUseCase {

    /**
     * Explores the full database schema via JDBC metadata.
     * Lists all tables with their columns and primary keys.
     *
     * @param jdbcUrl the JDBC URL to connect to (supports MySQL, H2, SQLite)
     * @return schema overview with jdbcUrl (sanitized), tableCount, and tables with columns
     */
    Map<String, Object> exploreSchema(String jdbcUrl);

    /**
     * Describes a single table in detail: columns, indexes, foreign keys, row count.
     *
     * @param jdbcUrl   the JDBC URL to connect to
     * @param tableName the name of the table to describe
     * @return detailed table description with columns, indexes, foreignKeys, and rowCount
     */
    Map<String, Object> describeTable(String jdbcUrl, String tableName);

    /**
     * Analyzes the database schema and suggests missing indexes,
     * particularly on foreign key columns that are not indexed.
     *
     * @param jdbcUrl the JDBC URL to connect to
     * @return suggestions with tablesAnalyzed, suggestionsCount, and detailed suggestions
     */
    Map<String, Object> suggestIndexes(String jdbcUrl);

    /**
     * Generates a Mermaid erDiagram from the database schema,
     * including tables, columns, and foreign key relationships.
     *
     * @param jdbcUrl the JDBC URL to connect to
     * @return ERD with tableCount, relationshipCount, and mermaid diagram string
     */
    Map<String, Object> generateErd(String jdbcUrl);
}
