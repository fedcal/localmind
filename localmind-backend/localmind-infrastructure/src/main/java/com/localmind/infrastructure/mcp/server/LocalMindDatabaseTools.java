package com.localmind.infrastructure.mcp.server;

import com.localmind.domain.mcp.port.in.DataMockGeneratorUseCase;
import com.localmind.domain.mcp.port.in.DbSchemaExplorerUseCase;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "localmind.mcp.server.enabled", havingValue = "true", matchIfMissing = true)
public class LocalMindDatabaseTools {

    private final DbSchemaExplorerUseCase dbSchemaExplorerUseCase;
    private final DataMockGeneratorUseCase dataMockGeneratorUseCase;

    public LocalMindDatabaseTools(DbSchemaExplorerUseCase dbSchemaExplorerUseCase,
                                  DataMockGeneratorUseCase dataMockGeneratorUseCase) {
        this.dbSchemaExplorerUseCase = dbSchemaExplorerUseCase;
        this.dataMockGeneratorUseCase = dataMockGeneratorUseCase;
    }

    // ==================== DB SCHEMA EXPLORER ====================

    @Tool(description = "Explore the full database schema via JDBC metadata. Lists all tables with their "
            + "columns and primary keys. Supports MySQL, H2, and SQLite databases. "
            + "Returns the sanitized JDBC URL, tableCount, and tables array with column details.")
    public Map<String, Object> exploreSchema(
            @ToolParam(description = "The JDBC URL to connect to (e.g. 'jdbc:mysql://localhost:3306/mydb')")
            String jdbcUrl) {
        return dbSchemaExplorerUseCase.exploreSchema(jdbcUrl);
    }

    @Tool(description = "Describe a single database table in detail including columns with types and "
            + "nullability, indexes, foreign keys, and approximate row count. "
            + "Provides a comprehensive view of table structure for documentation or analysis.")
    public Map<String, Object> describeTable(
            @ToolParam(description = "The JDBC URL to connect to") String jdbcUrl,
            @ToolParam(description = "The name of the table to describe") String tableName) {
        return dbSchemaExplorerUseCase.describeTable(jdbcUrl, tableName);
    }

    @Tool(description = "Analyze the database schema and suggest missing indexes, particularly on "
            + "foreign key columns that are not indexed. Returns the number of tables analyzed, "
            + "suggestions count, and detailed suggestions with table name, column, and reasoning.")
    public Map<String, Object> suggestIndexes(
            @ToolParam(description = "The JDBC URL to connect to") String jdbcUrl) {
        return dbSchemaExplorerUseCase.suggestIndexes(jdbcUrl);
    }

    @Tool(description = "Generate a Mermaid erDiagram from the database schema, including tables, "
            + "columns with types, and foreign key relationships. The output can be rendered by "
            + "any Mermaid-compatible viewer. Returns tableCount, relationshipCount, and the "
            + "mermaid diagram string.")
    public Map<String, Object> generateErd(
            @ToolParam(description = "The JDBC URL to connect to") String jdbcUrl) {
        return dbSchemaExplorerUseCase.generateErd(jdbcUrl);
    }

    // ==================== DATA MOCK GENERATOR ====================

    @Tool(description = "Generate mock data based on a field schema with typed generators. "
            + "Available types: firstName, lastName, email, phone, address, company, date, "
            + "integer, float, boolean, uuid, sentence, paragraph, url, ipv4, hexColor. "
            + "Returns the dataset name, count, schema, and generated data array. "
            + "Maximum 10000 rows per request.")
    public Map<String, Object> generateMockData(
            @ToolParam(description = "List of field definitions. Each map must contain 'field' (column name) "
                    + "and 'type' (generator type, e.g. 'email', 'firstName', 'integer')")
            List<Map<String, String>> schema,
            @ToolParam(description = "Number of rows to generate (default 10, max 10000)") int count,
            @ToolParam(description = "Name for the generated dataset") String name) {
        return dataMockGeneratorUseCase.generateMockData(schema, count, name);
    }

    @Tool(description = "Generate mock data from a JSON Schema definition with 'properties' defining "
            + "field types and formats. Supports JSON Schema 'type' (string, number, integer, boolean) "
            + "and 'format' (email, uri, uuid, ipv4, date, phone, firstName, lastName, etc.). "
            + "Returns dataset name, count, and generated data array.")
    public Map<String, Object> generateMockJson(
            @ToolParam(description = "JSON Schema object with a 'properties' key. Each property should have "
                    + "'type' (string, number, integer, boolean) and optionally 'format' (email, uri, uuid, etc.)")
            Map<String, Object> jsonSchema,
            @ToolParam(description = "Number of rows to generate (default 10, max 10000)") int count,
            @ToolParam(description = "Name for the generated dataset") String name) {
        return dataMockGeneratorUseCase.generateMockJson(jsonSchema, count, name);
    }

    @Tool(description = "Generate mock data in CSV format with configurable columns, count, and delimiter. "
            + "Each column definition specifies a name and generator type. "
            + "Returns the dataset name, row count, and the CSV content as a string.")
    public Map<String, Object> generateMockCsv(
            @ToolParam(description = "List of column definitions. Each map must contain 'name' (column header) "
                    + "and 'type' (generator type, e.g. 'email', 'integer', 'date')")
            List<Map<String, String>> columns,
            @ToolParam(description = "Number of rows to generate (default 10, max 10000)") int count,
            @ToolParam(description = "CSV delimiter character (default ','). Use '\\t' for TSV, ';' for semicolon-separated")
            String delimiter,
            @ToolParam(description = "Name for the generated dataset") String name) {
        return dataMockGeneratorUseCase.generateMockCsv(columns, count, delimiter, name);
    }

    @Tool(description = "List all available mock data generators with their name and description. "
            + "Returns generators including firstName, lastName, email, phone, address, company, "
            + "date, integer, float, boolean, uuid, sentence, paragraph, url, ipv4, and hexColor.")
    public List<Map<String, Object>> listMockGenerators() {
        return dataMockGeneratorUseCase.listMockGenerators();
    }
}
