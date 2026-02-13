package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.SchemaExploration;
import com.localmind.domain.mcp.port.out.SchemaExplorationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DbSchemaExplorerServiceTest {

    @Mock
    private SchemaExplorationRepository schemaExplorationRepository;

    @Mock
    private Connection connection;

    @Mock
    private DatabaseMetaData databaseMetaData;

    private DbSchemaExplorerService service;

    @BeforeEach
    void setUp() {
        when(schemaExplorationRepository.save(any(SchemaExploration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Function<String, Connection> connectionProvider = jdbcUrl -> connection;
        service = new DbSchemaExplorerService(schemaExplorationRepository, connectionProvider);
    }

    // ======================== exploreSchema ========================

    @Test
    @SuppressWarnings("unchecked")
    void exploreSchema_nullUrl_returnsError() {
        Map<String, Object> result = service.exploreSchema(null);

        assertThat(result.get("error")).isEqualTo("JDBC URL non fornito");
        assertThat(result.get("tableCount")).isEqualTo(0);
        assertThat((List<?>) result.get("tables")).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void exploreSchema_blankUrl_returnsError() {
        Map<String, Object> result = service.exploreSchema("   ");

        assertThat(result.get("error")).isEqualTo("JDBC URL non fornito");
        assertThat(result.get("tableCount")).isEqualTo(0);
    }

    @Test
    @SuppressWarnings("unchecked")
    void exploreSchema_singleTable_returnsSchemaWithColumnsAndPks() throws SQLException {
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(connection.getCatalog()).thenReturn("testdb");

        // Mock getTables -> one table "users"
        ResultSet tablesRs = mockResultSet(new String[]{"TABLE_NAME"}, new Object[][]{{"users"}});
        when(databaseMetaData.getTables(eq("testdb"), isNull(), eq("%"), any(String[].class)))
                .thenReturn(tablesRs);

        // Mock getPrimaryKeys -> "id" is PK
        ResultSet pkRs = mockResultSet(new String[]{"COLUMN_NAME"}, new Object[][]{{"id"}});
        when(databaseMetaData.getPrimaryKeys(eq("testdb"), isNull(), eq("users")))
                .thenReturn(pkRs);

        // Mock getColumns -> id (CHAR, not null), name (VARCHAR, nullable)
        ResultSet colsRs = mockResultSet(
                new String[]{"COLUMN_NAME", "TYPE_NAME", "IS_NULLABLE"},
                new Object[][]{
                        {"id", "CHAR", "NO"},
                        {"name", "VARCHAR", "YES"}
                }
        );
        when(databaseMetaData.getColumns(eq("testdb"), isNull(), eq("users"), eq("%")))
                .thenReturn(colsRs);

        Map<String, Object> result = service.exploreSchema("jdbc:mysql://localhost:3306/testdb");

        assertThat(result.get("tableCount")).isEqualTo(1);
        assertThat(result.get("jdbcUrl")).isNotNull();
        assertThat(result).doesNotContainKey("error");

        List<Map<String, Object>> tables = (List<Map<String, Object>>) result.get("tables");
        assertThat(tables).hasSize(1);
        assertThat(tables.get(0).get("name")).isEqualTo("users");

        List<Map<String, Object>> columns = (List<Map<String, Object>>) tables.get(0).get("columns");
        assertThat(columns).hasSize(2);
        assertThat(columns.get(0).get("name")).isEqualTo("id");
        assertThat(columns.get(0).get("type")).isEqualTo("CHAR");
        assertThat(columns.get(0).get("nullable")).isEqualTo(false);
        assertThat(columns.get(0).get("primaryKey")).isEqualTo(true);
        assertThat(columns.get(1).get("name")).isEqualTo("name");
        assertThat(columns.get(1).get("nullable")).isEqualTo(true);
        assertThat(columns.get(1).get("primaryKey")).isEqualTo(false);

        verify(schemaExplorationRepository).save(any(SchemaExploration.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void exploreSchema_multipleTables_returnsCorrectCount() throws SQLException {
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(connection.getCatalog()).thenReturn("testdb");

        ResultSet tablesRs = mockResultSet(new String[]{"TABLE_NAME"},
                new Object[][]{{"users"}, {"orders"}, {"products"}});
        when(databaseMetaData.getTables(eq("testdb"), isNull(), eq("%"), any(String[].class)))
                .thenReturn(tablesRs);

        // For each table call, return a fresh empty ResultSet
        when(databaseMetaData.getPrimaryKeys(eq("testdb"), isNull(), anyString()))
                .thenAnswer(inv -> mockResultSet(new String[]{"COLUMN_NAME"}, new Object[][]{}));
        when(databaseMetaData.getColumns(eq("testdb"), isNull(), anyString(), eq("%")))
                .thenAnswer(inv -> mockResultSet(new String[]{"COLUMN_NAME", "TYPE_NAME", "IS_NULLABLE"}, new Object[][]{}));

        Map<String, Object> result = service.exploreSchema("jdbc:mysql://localhost/testdb");

        assertThat(result.get("tableCount")).isEqualTo(3);
        List<Map<String, Object>> tables = (List<Map<String, Object>>) result.get("tables");
        assertThat(tables).hasSize(3);
    }

    @Test
    void exploreSchema_sanitizesUrlPassword() throws SQLException {
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(connection.getCatalog()).thenReturn("testdb");

        ResultSet emptyTablesRs = mockResultSet(new String[]{"TABLE_NAME"}, new Object[][]{});
        when(databaseMetaData.getTables(eq("testdb"), isNull(), eq("%"), any(String[].class)))
                .thenReturn(emptyTablesRs);

        Map<String, Object> result = service.exploreSchema("jdbc:mysql://user:secretpass@localhost:3306/testdb");

        String sanitizedUrl = (String) result.get("jdbcUrl");
        assertThat(sanitizedUrl).doesNotContain("secretpass");
        assertThat(sanitizedUrl).contains("***");
    }

    @Test
    void exploreSchema_savesResultToRepository() throws SQLException {
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(connection.getCatalog()).thenReturn("testdb");

        ResultSet emptyTablesRs = mockResultSet(new String[]{"TABLE_NAME"}, new Object[][]{});
        when(databaseMetaData.getTables(eq("testdb"), isNull(), eq("%"), any(String[].class)))
                .thenReturn(emptyTablesRs);

        service.exploreSchema("jdbc:mysql://localhost/testdb");

        ArgumentCaptor<SchemaExploration> captor = ArgumentCaptor.forClass(SchemaExploration.class);
        verify(schemaExplorationRepository).save(captor.capture());

        SchemaExploration saved = captor.getValue();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getExploredAt()).isNotNull();
        assertThat(saved.getTableCount()).isEqualTo(0);
    }

    // ======================== describeTable ========================

    @Test
    void describeTable_nullUrl_returnsError() {
        Map<String, Object> result = service.describeTable(null, "users");

        assertThat(result.get("error")).isEqualTo("JDBC URL non fornito");
    }

    @Test
    void describeTable_nullTableName_returnsError() {
        Map<String, Object> result = service.describeTable("jdbc:mysql://localhost/testdb", null);

        assertThat(result.get("error")).isEqualTo("Nome tabella non fornito");
    }

    @Test
    @SuppressWarnings("unchecked")
    void describeTable_returnsColumnsIndexesForeignKeysAndRowCount() throws SQLException {
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(connection.getCatalog()).thenReturn("testdb");

        // Primary keys
        ResultSet pkRs = mockResultSet(new String[]{"COLUMN_NAME"}, new Object[][]{{"id"}});
        when(databaseMetaData.getPrimaryKeys(eq("testdb"), isNull(), eq("orders")))
                .thenReturn(pkRs);

        // Columns with detailed info
        ResultSet colsRs = mockResultSet(
                new String[]{"COLUMN_NAME", "TYPE_NAME", "IS_NULLABLE", "COLUMN_DEF", "IS_AUTOINCREMENT", "COLUMN_SIZE"},
                new Object[][]{
                        {"id", "CHAR", "NO", null, "NO", 36},
                        {"user_id", "CHAR", "NO", null, "NO", 36},
                        {"total", "DECIMAL", "YES", "0.00", "NO", 10}
                }
        );
        when(databaseMetaData.getColumns(eq("testdb"), isNull(), eq("orders"), eq("%")))
                .thenReturn(colsRs);

        // Indexes
        ResultSet idxRs = mockResultSet(
                new String[]{"INDEX_NAME", "NON_UNIQUE", "COLUMN_NAME"},
                new Object[][]{
                        {"PRIMARY", false, "id"},
                        {"idx_user_id", true, "user_id"}
                }
        );
        when(databaseMetaData.getIndexInfo(eq("testdb"), isNull(), eq("orders"), eq(false), eq(true)))
                .thenReturn(idxRs);

        // Foreign keys
        ResultSet fkRs = mockResultSet(
                new String[]{"FKTABLE_NAME", "FKCOLUMN_NAME", "PKTABLE_NAME", "PKCOLUMN_NAME", "UPDATE_RULE", "DELETE_RULE"},
                new Object[][]{
                        {"orders", "user_id", "users", "id", (short) DatabaseMetaData.importedKeyCascade, (short) DatabaseMetaData.importedKeySetNull}
                }
        );
        when(databaseMetaData.getImportedKeys(eq("testdb"), isNull(), eq("orders")))
                .thenReturn(fkRs);

        // Row count
        Statement stmt = mock(Statement.class);
        ResultSet countRs = mock(ResultSet.class);
        when(connection.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(countRs);
        when(countRs.next()).thenReturn(true);
        when(countRs.getInt(1)).thenReturn(150);

        Map<String, Object> result = service.describeTable("jdbc:mysql://localhost/testdb", "orders");

        assertThat(result.get("tableName")).isEqualTo("orders");
        assertThat(result.get("rowCount")).isEqualTo(150);

        List<Map<String, Object>> columns = (List<Map<String, Object>>) result.get("columns");
        assertThat(columns).hasSize(3);
        assertThat(columns.get(0).get("name")).isEqualTo("id");
        assertThat(columns.get(0).get("primaryKey")).isEqualTo(true);
        assertThat(columns.get(0).get("size")).isEqualTo(36);
        assertThat(columns.get(1).get("name")).isEqualTo("user_id");
        assertThat(columns.get(2).get("defaultValue")).isEqualTo("0.00");

        List<Map<String, Object>> indexes = (List<Map<String, Object>>) result.get("indexes");
        assertThat(indexes).hasSize(2);

        List<Map<String, Object>> foreignKeys = (List<Map<String, Object>>) result.get("foreignKeys");
        assertThat(foreignKeys).hasSize(1);
        assertThat(foreignKeys.get(0).get("fkColumn")).isEqualTo("user_id");
        assertThat(foreignKeys.get(0).get("pkTable")).isEqualTo("users");
        assertThat(foreignKeys.get(0).get("updateRule")).isEqualTo("CASCADE");
        assertThat(foreignKeys.get(0).get("deleteRule")).isEqualTo("SET NULL");

        verify(schemaExplorationRepository).save(any(SchemaExploration.class));
    }

    // ======================== suggestIndexes ========================

    @Test
    @SuppressWarnings("unchecked")
    void suggestIndexes_nullUrl_returnsError() {
        Map<String, Object> result = service.suggestIndexes(null);

        assertThat(result.get("error")).isEqualTo("JDBC URL non fornito");
        assertThat(result.get("tablesAnalyzed")).isEqualTo(0);
        assertThat(result.get("suggestionsCount")).isEqualTo(0);
    }

    @Test
    @SuppressWarnings("unchecked")
    void suggestIndexes_fkWithoutIndex_suggestsIndex() throws SQLException {
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(connection.getCatalog()).thenReturn("testdb");

        // One table: orders
        ResultSet tablesRs = mockResultSet(new String[]{"TABLE_NAME"}, new Object[][]{{"orders"}});
        when(databaseMetaData.getTables(eq("testdb"), isNull(), eq("%"), any(String[].class)))
                .thenReturn(tablesRs);

        // No existing indexes on user_id
        ResultSet idxRs = mockResultSet(
                new String[]{"INDEX_NAME", "COLUMN_NAME"},
                new Object[][]{{"PRIMARY", "id"}}
        );
        // getIndexInfo called twice: once for indexed columns check, once for non-PK indexes check
        when(databaseMetaData.getIndexInfo(eq("testdb"), isNull(), eq("orders"), eq(false), eq(true)))
                .thenReturn(idxRs)
                .thenReturn(mockResultSet(new String[]{"INDEX_NAME", "COLUMN_NAME"}, new Object[][]{{"PRIMARY", "id"}}));

        // FK on user_id -> users.id, but user_id is NOT indexed
        ResultSet fkRs = mockResultSet(
                new String[]{"FKCOLUMN_NAME", "PKTABLE_NAME"},
                new Object[][]{{"user_id", "users"}}
        );
        when(databaseMetaData.getImportedKeys(eq("testdb"), isNull(), eq("orders")))
                .thenReturn(fkRs);

        // Row count: 50 (below 1000 threshold)
        Statement stmt = mock(Statement.class);
        ResultSet countRs = mock(ResultSet.class);
        when(connection.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(countRs);
        when(countRs.next()).thenReturn(true);
        when(countRs.getInt(1)).thenReturn(50);

        Map<String, Object> result = service.suggestIndexes("jdbc:mysql://localhost/testdb");

        assertThat(result.get("tablesAnalyzed")).isEqualTo(1);
        assertThat((int) result.get("suggestionsCount")).isGreaterThanOrEqualTo(1);

        List<Map<String, Object>> suggestions = (List<Map<String, Object>>) result.get("suggestions");
        assertThat(suggestions.stream()
                .anyMatch(s -> "user_id".equals(s.get("column")) && "orders".equals(s.get("table"))))
                .isTrue();

        // Verify suggestedSql contains CREATE INDEX
        Map<String, Object> fkSuggestion = suggestions.stream()
                .filter(s -> "user_id".equals(s.get("column")))
                .findFirst().orElseThrow();
        assertThat((String) fkSuggestion.get("suggestedSql")).contains("CREATE INDEX");

        verify(schemaExplorationRepository).save(any(SchemaExploration.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void suggestIndexes_largeTableWithoutUserIndexes_suggestsAnalysis() throws SQLException {
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(connection.getCatalog()).thenReturn("testdb");

        ResultSet tablesRs = mockResultSet(new String[]{"TABLE_NAME"}, new Object[][]{{"big_table"}});
        when(databaseMetaData.getTables(eq("testdb"), isNull(), eq("%"), any(String[].class)))
                .thenReturn(tablesRs);

        // Only PRIMARY index
        ResultSet idxRs = mockResultSet(
                new String[]{"INDEX_NAME", "COLUMN_NAME"},
                new Object[][]{{"PRIMARY", "id"}}
        );
        when(databaseMetaData.getIndexInfo(eq("testdb"), isNull(), eq("big_table"), eq(false), eq(true)))
                .thenReturn(idxRs)
                .thenReturn(mockResultSet(new String[]{"INDEX_NAME", "COLUMN_NAME"}, new Object[][]{{"PRIMARY", "id"}}));

        // No FK
        ResultSet emptyFkRs = mockResultSet(new String[]{"FKCOLUMN_NAME", "PKTABLE_NAME"}, new Object[][]{});
        when(databaseMetaData.getImportedKeys(eq("testdb"), isNull(), eq("big_table")))
                .thenReturn(emptyFkRs);

        // Row count: 5000 (> 1000)
        Statement stmt = mock(Statement.class);
        ResultSet countRs = mock(ResultSet.class);
        when(connection.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(countRs);
        when(countRs.next()).thenReturn(true);
        when(countRs.getInt(1)).thenReturn(5000);

        Map<String, Object> result = service.suggestIndexes("jdbc:mysql://localhost/testdb");

        List<Map<String, Object>> suggestions = (List<Map<String, Object>>) result.get("suggestions");
        assertThat(suggestions.stream()
                .anyMatch(s -> "big_table".equals(s.get("table")) && "*".equals(s.get("column"))))
                .isTrue();
    }

    // ======================== generateErd ========================

    @Test
    void generateErd_nullUrl_returnsError() {
        Map<String, Object> result = service.generateErd(null);

        assertThat(result.get("error")).isEqualTo("JDBC URL non fornito");
        assertThat(result.get("tableCount")).isEqualTo(0);
        assertThat(result.get("mermaid")).isEqualTo("");
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateErd_twoTablesWithRelationship_generatesMermaidDiagram() throws SQLException {
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(connection.getCatalog()).thenReturn("testdb");

        // Two tables: users, orders
        ResultSet tablesRs = mockResultSet(new String[]{"TABLE_NAME"},
                new Object[][]{{"users"}, {"orders"}});
        when(databaseMetaData.getTables(eq("testdb"), isNull(), eq("%"), any(String[].class)))
                .thenReturn(tablesRs);

        // users: PK = id
        ResultSet usersPkRs = mockResultSet(new String[]{"COLUMN_NAME"}, new Object[][]{{"id"}});
        when(databaseMetaData.getPrimaryKeys(eq("testdb"), isNull(), eq("users")))
                .thenReturn(usersPkRs);

        // users columns
        ResultSet usersColsRs = mockResultSet(
                new String[]{"COLUMN_NAME", "TYPE_NAME"},
                new Object[][]{{"id", "INT"}, {"name", "VARCHAR"}}
        );
        when(databaseMetaData.getColumns(eq("testdb"), isNull(), eq("users"), eq("%")))
                .thenReturn(usersColsRs);

        // users: no FK
        ResultSet usersNoFk = mockResultSet(new String[]{"FKTABLE_NAME", "PKTABLE_NAME", "FKCOLUMN_NAME"}, new Object[][]{});
        when(databaseMetaData.getImportedKeys(eq("testdb"), isNull(), eq("users")))
                .thenReturn(usersNoFk);

        // orders: PK = id
        ResultSet ordersPkRs = mockResultSet(new String[]{"COLUMN_NAME"}, new Object[][]{{"id"}});
        when(databaseMetaData.getPrimaryKeys(eq("testdb"), isNull(), eq("orders")))
                .thenReturn(ordersPkRs);

        // orders columns
        ResultSet ordersColsRs = mockResultSet(
                new String[]{"COLUMN_NAME", "TYPE_NAME"},
                new Object[][]{{"id", "INT"}, {"user_id", "INT"}, {"total", "DECIMAL"}}
        );
        when(databaseMetaData.getColumns(eq("testdb"), isNull(), eq("orders"), eq("%")))
                .thenReturn(ordersColsRs);

        // orders FK: user_id -> users.id
        ResultSet ordersFkRs = mockResultSet(
                new String[]{"FKTABLE_NAME", "PKTABLE_NAME", "FKCOLUMN_NAME"},
                new Object[][]{{"orders", "users", "user_id"}}
        );
        when(databaseMetaData.getImportedKeys(eq("testdb"), isNull(), eq("orders")))
                .thenReturn(ordersFkRs);

        Map<String, Object> result = service.generateErd("jdbc:mysql://localhost/testdb");

        assertThat(result.get("tableCount")).isEqualTo(2);
        assertThat(result.get("relationshipCount")).isEqualTo(1);

        String mermaid = (String) result.get("mermaid");
        assertThat(mermaid).startsWith("erDiagram");
        assertThat(mermaid).contains("users {");
        assertThat(mermaid).contains("orders {");
        assertThat(mermaid).contains("int id PK");
        assertThat(mermaid).contains("string name");
        assertThat(mermaid).contains("float total");
        assertThat(mermaid).contains("users ||--o{ orders");

        verify(schemaExplorationRepository).save(any(SchemaExploration.class));
    }

    // ======================== sanitizeUrl (static) ========================

    @Test
    void sanitizeUrl_removesPasswordParam() {
        String sanitized = DbSchemaExplorerService.sanitizeUrl("jdbc:mysql://localhost/db?password=secret123&user=admin");
        assertThat(sanitized).doesNotContain("secret123");
        assertThat(sanitized).contains("password=***");
    }

    @Test
    void sanitizeUrl_removesInlinePassword() {
        String sanitized = DbSchemaExplorerService.sanitizeUrl("jdbc:mysql://user:mypass@localhost:3306/db");
        assertThat(sanitized).doesNotContain("mypass");
        assertThat(sanitized).contains(":***@");
    }

    @Test
    void sanitizeUrl_nullReturnsEmpty() {
        String sanitized = DbSchemaExplorerService.sanitizeUrl(null);
        assertThat(sanitized).isEmpty();
    }

    // ======================== Helpers ========================

    /**
     * Creates a mock ResultSet that iterates over the given rows.
     * Each call to next() advances to the next row; getString/getInt/getShort/getBoolean
     * return the corresponding value from the current row.
     */
    private ResultSet mockResultSet(String[] columnNames, Object[][] rows) throws SQLException {
        ResultSet rs = mock(ResultSet.class);

        // Track current row index with an array (mutable in lambda)
        final int[] rowIndex = {-1};

        when(rs.next()).thenAnswer(invocation -> {
            rowIndex[0]++;
            return rowIndex[0] < rows.length;
        });

        for (int col = 0; col < columnNames.length; col++) {
            final int colIdx = col;
            when(rs.getString(columnNames[col])).thenAnswer(invocation -> {
                if (rowIndex[0] >= 0 && rowIndex[0] < rows.length) {
                    Object val = rows[rowIndex[0]][colIdx];
                    return val != null ? val.toString() : null;
                }
                return null;
            });
            when(rs.getInt(columnNames[col])).thenAnswer(invocation -> {
                if (rowIndex[0] >= 0 && rowIndex[0] < rows.length) {
                    Object val = rows[rowIndex[0]][colIdx];
                    if (val instanceof Number) return ((Number) val).intValue();
                    if (val instanceof String) {
                        try { return Integer.parseInt((String) val); } catch (NumberFormatException e) { return 0; }
                    }
                }
                return 0;
            });
            when(rs.getShort(columnNames[col])).thenAnswer(invocation -> {
                if (rowIndex[0] >= 0 && rowIndex[0] < rows.length) {
                    Object val = rows[rowIndex[0]][colIdx];
                    if (val instanceof Number) return ((Number) val).shortValue();
                }
                return (short) 0;
            });
            when(rs.getBoolean(columnNames[col])).thenAnswer(invocation -> {
                if (rowIndex[0] >= 0 && rowIndex[0] < rows.length) {
                    Object val = rows[rowIndex[0]][colIdx];
                    if (val instanceof Boolean) return (Boolean) val;
                    if (val instanceof String) return Boolean.parseBoolean((String) val);
                }
                return false;
            });
        }

        return rs;
    }
}
