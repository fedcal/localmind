package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.SchemaExploration;
import com.localmind.domain.mcp.port.in.DbSchemaExplorerUseCase;
import com.localmind.domain.mcp.port.out.SchemaExplorationRepository;

import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

/**
 * Domain service implementing the db-schema-explorer tool group.
 * Pure Java, zero Spring dependencies.
 * Uses JDBC DatabaseMetaData to introspect database schemas.
 *
 * For testability, the constructor accepts a Function that provides a Connection given a JDBC URL,
 * allowing mocking of the JDBC layer in unit tests.
 */
public class DbSchemaExplorerService implements DbSchemaExplorerUseCase {

    private final SchemaExplorationRepository schemaExplorationRepository;
    private final Function<String, Connection> connectionProvider;

    /**
     * Production constructor: connects via DriverManager.
     */
    public DbSchemaExplorerService(SchemaExplorationRepository schemaExplorationRepository) {
        this(schemaExplorationRepository, jdbcUrl -> {
            try {
                return DriverManager.getConnection(jdbcUrl);
            } catch (SQLException e) {
                throw new RuntimeException("Impossibile connettersi al database: " + sanitizeUrl(jdbcUrl), e);
            }
        });
    }

    /**
     * Test constructor: accepts a custom connection provider for mocking.
     */
    public DbSchemaExplorerService(SchemaExplorationRepository schemaExplorationRepository,
                                   Function<String, Connection> connectionProvider) {
        this.schemaExplorationRepository = schemaExplorationRepository;
        this.connectionProvider = connectionProvider;
    }

    // ======================== exploreSchema ========================

    @Override
    public Map<String, Object> exploreSchema(String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "JDBC URL non fornito");
            error.put("jdbcUrl", "");
            error.put("tableCount", 0);
            error.put("tables", List.of());
            return error;
        }

        try (Connection conn = connectionProvider.apply(jdbcUrl)) {
            DatabaseMetaData metaData = conn.getMetaData();
            String catalog = conn.getCatalog();

            List<Map<String, Object>> tables = new ArrayList<>();

            try (ResultSet tablesRs = metaData.getTables(catalog, null, "%", new String[]{"TABLE"})) {
                while (tablesRs.next()) {
                    String tableName = tablesRs.getString("TABLE_NAME");

                    // Collect primary keys for this table
                    Set<String> primaryKeys = new HashSet<>();
                    try (ResultSet pkRs = metaData.getPrimaryKeys(catalog, null, tableName)) {
                        while (pkRs.next()) {
                            primaryKeys.add(pkRs.getString("COLUMN_NAME"));
                        }
                    }

                    // Collect columns
                    List<Map<String, Object>> columns = new ArrayList<>();
                    try (ResultSet colsRs = metaData.getColumns(catalog, null, tableName, "%")) {
                        while (colsRs.next()) {
                            Map<String, Object> column = new LinkedHashMap<>();
                            String colName = colsRs.getString("COLUMN_NAME");
                            column.put("name", colName);
                            column.put("type", colsRs.getString("TYPE_NAME"));
                            column.put("nullable", "YES".equals(colsRs.getString("IS_NULLABLE")));
                            column.put("primaryKey", primaryKeys.contains(colName));
                            columns.add(column);
                        }
                    }

                    Map<String, Object> table = new LinkedHashMap<>();
                    table.put("name", tableName);
                    table.put("columns", columns);
                    tables.add(table);
                }
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("jdbcUrl", sanitizeUrl(jdbcUrl));
            result.put("tableCount", tables.size());
            result.put("tables", tables);

            saveResult(jdbcUrl, tables.size(), result);
            return result;

        } catch (SQLException e) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Errore durante l'esplorazione dello schema: " + e.getMessage());
            error.put("jdbcUrl", sanitizeUrl(jdbcUrl));
            error.put("tableCount", 0);
            error.put("tables", List.of());
            return error;
        }
    }

    // ======================== describeTable ========================

    @Override
    public Map<String, Object> describeTable(String jdbcUrl, String tableName) {
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "JDBC URL non fornito");
            error.put("tableName", tableName);
            return error;
        }
        if (tableName == null || tableName.isBlank()) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Nome tabella non fornito");
            error.put("tableName", "");
            return error;
        }

        try (Connection conn = connectionProvider.apply(jdbcUrl)) {
            DatabaseMetaData metaData = conn.getMetaData();
            String catalog = conn.getCatalog();

            // Primary keys
            Set<String> primaryKeys = new HashSet<>();
            try (ResultSet pkRs = metaData.getPrimaryKeys(catalog, null, tableName)) {
                while (pkRs.next()) {
                    primaryKeys.add(pkRs.getString("COLUMN_NAME"));
                }
            }

            // Columns
            List<Map<String, Object>> columns = new ArrayList<>();
            try (ResultSet colsRs = metaData.getColumns(catalog, null, tableName, "%")) {
                while (colsRs.next()) {
                    Map<String, Object> column = new LinkedHashMap<>();
                    String colName = colsRs.getString("COLUMN_NAME");
                    column.put("name", colName);
                    column.put("type", colsRs.getString("TYPE_NAME"));
                    column.put("nullable", "YES".equals(colsRs.getString("IS_NULLABLE")));
                    column.put("defaultValue", colsRs.getString("COLUMN_DEF"));
                    column.put("autoIncrement", "YES".equals(colsRs.getString("IS_AUTOINCREMENT")));
                    column.put("size", colsRs.getInt("COLUMN_SIZE"));
                    column.put("primaryKey", primaryKeys.contains(colName));
                    columns.add(column);
                }
            }

            // Indexes
            List<Map<String, Object>> indexes = new ArrayList<>();
            Map<String, Map<String, Object>> indexMap = new LinkedHashMap<>();
            try (ResultSet idxRs = metaData.getIndexInfo(catalog, null, tableName, false, true)) {
                while (idxRs.next()) {
                    String indexName = idxRs.getString("INDEX_NAME");
                    if (indexName == null) continue;

                    String columnName = idxRs.getString("COLUMN_NAME");
                    boolean unique = !idxRs.getBoolean("NON_UNIQUE");

                    Map<String, Object> idx = indexMap.get(indexName);
                    if (idx == null) {
                        idx = new LinkedHashMap<>();
                        idx.put("name", indexName);
                        idx.put("unique", unique);
                        idx.put("columns", new ArrayList<String>());
                        indexMap.put(indexName, idx);
                    }
                    @SuppressWarnings("unchecked")
                    List<String> cols = (List<String>) idx.get("columns");
                    if (columnName != null) {
                        cols.add(columnName);
                    }
                }
            }
            indexes.addAll(indexMap.values());

            // Foreign Keys
            List<Map<String, Object>> foreignKeys = new ArrayList<>();
            try (ResultSet fkRs = metaData.getImportedKeys(catalog, null, tableName)) {
                while (fkRs.next()) {
                    Map<String, Object> fk = new LinkedHashMap<>();
                    fk.put("fkTable", fkRs.getString("FKTABLE_NAME"));
                    fk.put("fkColumn", fkRs.getString("FKCOLUMN_NAME"));
                    fk.put("pkTable", fkRs.getString("PKTABLE_NAME"));
                    fk.put("pkColumn", fkRs.getString("PKCOLUMN_NAME"));
                    fk.put("updateRule", mapFkRule(fkRs.getShort("UPDATE_RULE")));
                    fk.put("deleteRule", mapFkRule(fkRs.getShort("DELETE_RULE")));
                    foreignKeys.add(fk);
                }
            }

            // Row count
            int rowCount = 0;
            try (Statement stmt = conn.createStatement();
                 ResultSet countRs = stmt.executeQuery("SELECT COUNT(*) FROM " + escapeTableName(tableName))) {
                if (countRs.next()) {
                    rowCount = countRs.getInt(1);
                }
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("tableName", tableName);
            result.put("rowCount", rowCount);
            result.put("columns", columns);
            result.put("indexes", indexes);
            result.put("foreignKeys", foreignKeys);

            saveResult(jdbcUrl, 1, result);
            return result;

        } catch (SQLException e) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Errore durante la descrizione della tabella: " + e.getMessage());
            error.put("tableName", tableName);
            return error;
        }
    }

    // ======================== suggestIndexes ========================

    @Override
    public Map<String, Object> suggestIndexes(String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "JDBC URL non fornito");
            error.put("tablesAnalyzed", 0);
            error.put("suggestionsCount", 0);
            error.put("suggestions", List.of());
            return error;
        }

        try (Connection conn = connectionProvider.apply(jdbcUrl)) {
            DatabaseMetaData metaData = conn.getMetaData();
            String catalog = conn.getCatalog();

            List<String> tableNames = new ArrayList<>();
            try (ResultSet tablesRs = metaData.getTables(catalog, null, "%", new String[]{"TABLE"})) {
                while (tablesRs.next()) {
                    tableNames.add(tablesRs.getString("TABLE_NAME"));
                }
            }

            List<Map<String, Object>> suggestions = new ArrayList<>();

            for (String tableName : tableNames) {
                // Collect existing indexed columns
                Set<String> indexedColumns = new HashSet<>();
                try (ResultSet idxRs = metaData.getIndexInfo(catalog, null, tableName, false, true)) {
                    while (idxRs.next()) {
                        String colName = idxRs.getString("COLUMN_NAME");
                        if (colName != null) {
                            indexedColumns.add(colName);
                        }
                    }
                }

                // Check FK columns that are not indexed
                try (ResultSet fkRs = metaData.getImportedKeys(catalog, null, tableName)) {
                    while (fkRs.next()) {
                        String fkColumn = fkRs.getString("FKCOLUMN_NAME");
                        if (fkColumn != null && !indexedColumns.contains(fkColumn)) {
                            String pkTable = fkRs.getString("PKTABLE_NAME");
                            Map<String, Object> suggestion = new LinkedHashMap<>();
                            suggestion.put("table", tableName);
                            suggestion.put("column", fkColumn);
                            suggestion.put("reason", "Chiave esterna verso " + pkTable + " non indicizzata");
                            suggestion.put("suggestedSql",
                                    "CREATE INDEX idx_" + tableName + "_" + fkColumn + " ON " + escapeTableName(tableName) + " (" + fkColumn + ")");
                            suggestions.add(suggestion);
                        }
                    }
                }

                // Check for large tables with no user indexes (only PK)
                int rowCount = 0;
                try (Statement stmt = conn.createStatement();
                     ResultSet countRs = stmt.executeQuery("SELECT COUNT(*) FROM " + escapeTableName(tableName))) {
                    if (countRs.next()) {
                        rowCount = countRs.getInt(1);
                    }
                }

                if (rowCount > 1000) {
                    // Check if the only index is the PRIMARY key
                    Set<String> nonPkIndexNames = new HashSet<>();
                    try (ResultSet idxRs = metaData.getIndexInfo(catalog, null, tableName, false, true)) {
                        while (idxRs.next()) {
                            String idxName = idxRs.getString("INDEX_NAME");
                            if (idxName != null && !"PRIMARY".equalsIgnoreCase(idxName)) {
                                nonPkIndexNames.add(idxName);
                            }
                        }
                    }

                    if (nonPkIndexNames.isEmpty()) {
                        Map<String, Object> suggestion = new LinkedHashMap<>();
                        suggestion.put("table", tableName);
                        suggestion.put("column", "*");
                        suggestion.put("reason", "Tabella con " + rowCount + " righe senza indici utente. Analisi consigliata.");
                        suggestion.put("suggestedSql", "-- Analizzare le query piu' frequenti su " + tableName + " per creare indici mirati");
                        suggestions.add(suggestion);
                    }
                }
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("tablesAnalyzed", tableNames.size());
            result.put("suggestionsCount", suggestions.size());
            result.put("suggestions", suggestions);

            saveResult(jdbcUrl, tableNames.size(), result);
            return result;

        } catch (SQLException e) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Errore durante l'analisi degli indici: " + e.getMessage());
            error.put("tablesAnalyzed", 0);
            error.put("suggestionsCount", 0);
            error.put("suggestions", List.of());
            return error;
        }
    }

    // ======================== generateErd ========================

    @Override
    public Map<String, Object> generateErd(String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "JDBC URL non fornito");
            error.put("tableCount", 0);
            error.put("relationshipCount", 0);
            error.put("mermaid", "");
            return error;
        }

        try (Connection conn = connectionProvider.apply(jdbcUrl)) {
            DatabaseMetaData metaData = conn.getMetaData();
            String catalog = conn.getCatalog();

            // Collect all tables and their columns
            List<String> tableNames = new ArrayList<>();
            Map<String, List<String[]>> tableColumns = new LinkedHashMap<>(); // tableName -> List of [colName, typeName]
            Map<String, Set<String>> tablePrimaryKeys = new LinkedHashMap<>();

            try (ResultSet tablesRs = metaData.getTables(catalog, null, "%", new String[]{"TABLE"})) {
                while (tablesRs.next()) {
                    String tableName = tablesRs.getString("TABLE_NAME");
                    tableNames.add(tableName);

                    // Primary keys
                    Set<String> pks = new HashSet<>();
                    try (ResultSet pkRs = metaData.getPrimaryKeys(catalog, null, tableName)) {
                        while (pkRs.next()) {
                            pks.add(pkRs.getString("COLUMN_NAME"));
                        }
                    }
                    tablePrimaryKeys.put(tableName, pks);

                    // Columns
                    List<String[]> cols = new ArrayList<>();
                    try (ResultSet colsRs = metaData.getColumns(catalog, null, tableName, "%")) {
                        while (colsRs.next()) {
                            cols.add(new String[]{
                                    colsRs.getString("COLUMN_NAME"),
                                    colsRs.getString("TYPE_NAME")
                            });
                        }
                    }
                    tableColumns.put(tableName, cols);
                }
            }

            // Collect all foreign key relationships
            List<String[]> relationships = new ArrayList<>(); // [fkTable, pkTable, fkColumn]
            for (String tableName : tableNames) {
                try (ResultSet fkRs = metaData.getImportedKeys(catalog, null, tableName)) {
                    while (fkRs.next()) {
                        String fkTable = fkRs.getString("FKTABLE_NAME");
                        String pkTable = fkRs.getString("PKTABLE_NAME");
                        String fkColumn = fkRs.getString("FKCOLUMN_NAME");
                        relationships.add(new String[]{fkTable, pkTable, fkColumn});
                    }
                }
            }

            // Build Mermaid erDiagram
            StringBuilder mermaid = new StringBuilder("erDiagram\n");

            for (String tableName : tableNames) {
                mermaid.append("    ").append(tableName).append(" {\n");
                List<String[]> cols = tableColumns.get(tableName);
                Set<String> pks = tablePrimaryKeys.get(tableName);
                if (cols != null) {
                    for (String[] col : cols) {
                        String mermaidType = mapToMermaidType(col[1]);
                        String pkMarker = pks != null && pks.contains(col[0]) ? " PK" : "";
                        mermaid.append("        ").append(mermaidType).append(" ").append(col[0]).append(pkMarker).append("\n");
                    }
                }
                mermaid.append("    }\n");
            }

            // Add relationships
            for (String[] rel : relationships) {
                mermaid.append("    ").append(rel[1]).append(" ||--o{ ").append(rel[0])
                        .append(" : \"").append(rel[2]).append("\"\n");
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("tableCount", tableNames.size());
            result.put("relationshipCount", relationships.size());
            result.put("mermaid", mermaid.toString());

            saveResult(jdbcUrl, tableNames.size(), result);
            return result;

        } catch (SQLException e) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Errore durante la generazione dell'ERD: " + e.getMessage());
            error.put("tableCount", 0);
            error.put("relationshipCount", 0);
            error.put("mermaid", "");
            return error;
        }
    }

    // ======================== Helper Methods ========================

    /**
     * Sanitizes a JDBC URL by removing password information.
     */
    static String sanitizeUrl(String jdbcUrl) {
        if (jdbcUrl == null) return "";
        // Remove password= parameter
        return jdbcUrl.replaceAll("password=[^&;]*", "password=***")
                .replaceAll(":[^:/@]*@", ":***@");
    }

    /**
     * Maps a SQL type name to a simplified Mermaid type.
     */
    private String mapToMermaidType(String sqlType) {
        if (sqlType == null) return "string";
        String upper = sqlType.toUpperCase();
        if (upper.contains("INT") || upper.contains("SERIAL")) return "int";
        if (upper.contains("FLOAT") || upper.contains("DOUBLE") || upper.contains("DECIMAL") || upper.contains("NUMERIC") || upper.contains("REAL")) return "float";
        if (upper.contains("BOOL") || upper.contains("BIT") || upper.contains("TINYINT(1)")) return "boolean";
        if (upper.contains("DATE") || upper.contains("TIME") || upper.contains("TIMESTAMP")) return "datetime";
        if (upper.contains("BLOB") || upper.contains("BINARY") || upper.contains("BYTEA")) return "blob";
        return "string";
    }

    /**
     * Maps a foreign key rule constant to a human-readable string.
     */
    private String mapFkRule(short rule) {
        switch (rule) {
            case DatabaseMetaData.importedKeyCascade: return "CASCADE";
            case DatabaseMetaData.importedKeySetNull: return "SET NULL";
            case DatabaseMetaData.importedKeySetDefault: return "SET DEFAULT";
            case DatabaseMetaData.importedKeyRestrict: return "RESTRICT";
            case DatabaseMetaData.importedKeyNoAction: return "NO ACTION";
            default: return "UNKNOWN";
        }
    }

    /**
     * Escapes a table name with backticks for MySQL compatibility.
     */
    private String escapeTableName(String tableName) {
        return "`" + tableName.replace("`", "``") + "`";
    }

    /**
     * Persists an exploration result.
     */
    private void saveResult(String jdbcUrl, int tableCount, Map<String, Object> result) {
        SchemaExploration exploration = SchemaExploration.builder()
                .id(UUID.randomUUID().toString())
                .jdbcUrl(sanitizeUrl(jdbcUrl))
                .tableCount(tableCount)
                .resultJson(result.toString())
                .exploredAt(Instant.now())
                .build();
        schemaExplorationRepository.save(exploration);
    }
}
