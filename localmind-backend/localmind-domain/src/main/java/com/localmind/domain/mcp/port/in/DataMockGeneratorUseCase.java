package com.localmind.domain.mcp.port.in;

import java.util.List;
import java.util.Map;

/**
 * Input port for the data-mock-generator tool group.
 * Provides mock data generation in various formats (structured, JSON Schema, CSV).
 */
public interface DataMockGeneratorUseCase {

    /**
     * Generates mock data based on a field schema with typed generators.
     *
     * @param schema list of field definitions, each with "field" and "type" keys
     * @param count  number of rows to generate (default 10, max 10000)
     * @param name   name for the generated dataset
     * @return result map with name, count, schema, and data array
     */
    Map<String, Object> generateMockData(List<Map<String, String>> schema, int count, String name);

    /**
     * Generates mock data from a JSON Schema definition.
     *
     * @param jsonSchema JSON Schema with "properties" defining field types and formats
     * @param count      number of rows to generate (default 10, max 10000)
     * @param name       name for the generated dataset
     * @return result map with name, count, and data array
     */
    Map<String, Object> generateMockJson(Map<String, Object> jsonSchema, int count, String name);

    /**
     * Generates mock data in CSV format.
     *
     * @param columns   list of column definitions, each with "name" and "type" keys
     * @param count     number of rows to generate (default 10, max 10000)
     * @param delimiter CSV delimiter (default ",")
     * @param name      name for the generated dataset
     * @return result map with name, count, and csv string
     */
    Map<String, Object> generateMockCsv(List<Map<String, String>> columns, int count, String delimiter, String name);

    /**
     * Lists all available mock data generators with name and description.
     *
     * @return list of generator descriptors
     */
    List<Map<String, Object>> listMockGenerators();
}
