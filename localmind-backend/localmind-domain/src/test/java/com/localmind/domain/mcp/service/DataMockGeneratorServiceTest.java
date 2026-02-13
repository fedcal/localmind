package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.GeneratedDataset;
import com.localmind.domain.mcp.port.out.DataMockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DataMockGeneratorServiceTest {

    @Mock
    private DataMockRepository repository;

    private DataMockGeneratorService service;

    private static final long FIXED_SEED = 42L;

    @BeforeEach
    void setUp() {
        when(repository.save(any(GeneratedDataset.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        service = new DataMockGeneratorService(repository, new Random(FIXED_SEED));
    }

    // ========================================================================
    // generateMockData tests
    // ========================================================================

    @Test
    @SuppressWarnings("unchecked")
    void generateMockData_withFirstNameType_generatesValidNames() {
        List<Map<String, String>> schema = List.of(Map.of("field", "name", "type", "firstName"));

        Map<String, Object> result = service.generateMockData(schema, 5, "test-names");

        assertThat(result.get("name")).isEqualTo("test-names");
        assertThat((int) result.get("count")).isEqualTo(5);
        List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
        assertThat(data).hasSize(5);
        for (Map<String, Object> row : data) {
            assertThat(row.get("name")).isNotNull();
            assertThat(row.get("name").toString()).isNotBlank();
        }
        verify(repository).save(any(GeneratedDataset.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateMockData_withEmailType_generatesValidEmails() {
        List<Map<String, String>> schema = List.of(Map.of("field", "email", "type", "email"));

        Map<String, Object> result = service.generateMockData(schema, 3, "emails");

        List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
        assertThat(data).hasSize(3);
        for (Map<String, Object> row : data) {
            String email = (String) row.get("email");
            assertThat(email).contains("@");
            assertThat(email).matches(".*[._].*\\d+@.+\\..+");
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateMockData_withPhoneType_generatesCorrectFormat() {
        List<Map<String, String>> schema = List.of(Map.of("field", "phone", "type", "phone"));

        Map<String, Object> result = service.generateMockData(schema, 3, "phones");

        List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
        for (Map<String, Object> row : data) {
            String phone = (String) row.get("phone");
            assertThat(phone).matches("\\(\\d{3}\\) \\d{3}-\\d{4}");
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateMockData_withIpv4Type_generatesValidIp() {
        List<Map<String, String>> schema = List.of(Map.of("field", "ip", "type", "ipv4"));

        Map<String, Object> result = service.generateMockData(schema, 5, "ips");

        List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
        for (Map<String, Object> row : data) {
            String ip = (String) row.get("ip");
            assertThat(ip).matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
            String[] octets = ip.split("\\.");
            assertThat(Integer.parseInt(octets[0])).isBetween(1, 255);
            assertThat(Integer.parseInt(octets[1])).isBetween(0, 255);
            assertThat(Integer.parseInt(octets[2])).isBetween(0, 255);
            assertThat(Integer.parseInt(octets[3])).isBetween(1, 254);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateMockData_withHexColorType_generatesValidHexColor() {
        List<Map<String, String>> schema = List.of(Map.of("field", "color", "type", "hexColor"));

        Map<String, Object> result = service.generateMockData(schema, 5, "colors");

        List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
        for (Map<String, Object> row : data) {
            String color = (String) row.get("color");
            assertThat(color).startsWith("#");
            assertThat(color).hasSize(7);
            assertThat(color.substring(1)).matches("[0-9A-F]{6}");
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateMockData_withMultipleTypes_generatesAllFields() {
        List<Map<String, String>> schema = List.of(
                Map.of("field", "id", "type", "uuid"),
                Map.of("field", "name", "type", "firstName"),
                Map.of("field", "surname", "type", "lastName"),
                Map.of("field", "company", "type", "company"),
                Map.of("field", "date", "type", "date"),
                Map.of("field", "active", "type", "boolean")
        );

        Map<String, Object> result = service.generateMockData(schema, 2, "mixed");

        List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
        assertThat(data).hasSize(2);
        for (Map<String, Object> row : data) {
            assertThat(row).containsKeys("id", "name", "surname", "company", "date", "active");
            assertThat(row.get("id").toString()).matches("[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}");
            assertThat(row.get("active")).isInstanceOf(Boolean.class);
            assertThat(row.get("date").toString()).matches("\\d{4}-\\d{2}-\\d{2}");
        }
    }

    @Test
    void generateMockData_withDefaultCount_generates10Rows() {
        List<Map<String, String>> schema = List.of(Map.of("field", "n", "type", "integer"));

        Map<String, Object> result = service.generateMockData(schema, 0, "default-count");

        assertThat((int) result.get("count")).isEqualTo(10);
    }

    @Test
    void generateMockData_withCountExceedingMax_clampedTo10000() {
        List<Map<String, String>> schema = List.of(Map.of("field", "n", "type", "integer"));

        Map<String, Object> result = service.generateMockData(schema, 99999, "big");

        assertThat((int) result.get("count")).isEqualTo(10000);
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateMockData_withEmptySchema_generatesEmptyRows() {
        Map<String, Object> result = service.generateMockData(List.of(), 3, "empty-schema");

        List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
        assertThat(data).hasSize(3);
        for (Map<String, Object> row : data) {
            assertThat(row).isEmpty();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateMockData_withInvalidType_generatesNull() {
        List<Map<String, String>> schema = List.of(Map.of("field", "x", "type", "invalidType"));

        Map<String, Object> result = service.generateMockData(schema, 2, "invalid");

        List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
        for (Map<String, Object> row : data) {
            assertThat(row.get("x")).isNull();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateMockData_withSentenceType_generatesCapitalizedSentenceWithPeriod() {
        List<Map<String, String>> schema = List.of(Map.of("field", "text", "type", "sentence"));

        Map<String, Object> result = service.generateMockData(schema, 3, "sentences");

        List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
        for (Map<String, Object> row : data) {
            String sentence = (String) row.get("text");
            assertThat(sentence).endsWith(".");
            assertThat(Character.isUpperCase(sentence.charAt(0))).isTrue();
            String[] words = sentence.replace(".", "").split("\\s+");
            assertThat(words.length).isBetween(5, 15);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateMockData_withAddressType_containsCityAndStreet() {
        List<Map<String, String>> schema = List.of(Map.of("field", "addr", "type", "address"));

        Map<String, Object> result = service.generateMockData(schema, 3, "addresses");

        List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
        for (Map<String, Object> row : data) {
            String addr = (String) row.get("addr");
            assertThat(addr).contains(",");
            assertThat(addr).matches("\\d+ .+, .+");
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateMockData_withFloatType_hasTwoDecimalPlaces() {
        List<Map<String, String>> schema = List.of(Map.of("field", "val", "type", "float"));

        Map<String, Object> result = service.generateMockData(schema, 5, "floats");

        List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
        for (Map<String, Object> row : data) {
            double val = (double) row.get("val");
            assertThat(val).isBetween(0.0, 10000.0);
            // Verify two decimal places by checking no extra precision
            String str = String.valueOf(val);
            int dotIndex = str.indexOf('.');
            if (dotIndex >= 0) {
                assertThat(str.substring(dotIndex + 1).length()).isLessThanOrEqualTo(2);
            }
        }
    }

    // ========================================================================
    // generateMockJson tests
    // ========================================================================

    @Test
    @SuppressWarnings("unchecked")
    void generateMockJson_withJsonSchema_generatesData() {
        Map<String, Object> jsonSchema = new LinkedHashMap<>();
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("name", Map.of("type", "string", "format", "firstName"));
        properties.put("age", Map.of("type", "integer"));
        properties.put("email", Map.of("type", "string", "format", "email"));
        jsonSchema.put("properties", properties);

        Map<String, Object> result = service.generateMockJson(jsonSchema, 3, "json-test");

        assertThat(result.get("name")).isEqualTo("json-test");
        assertThat((int) result.get("count")).isEqualTo(3);
        List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
        assertThat(data).hasSize(3);
        for (Map<String, Object> row : data) {
            assertThat(row).containsKeys("name", "age", "email");
            assertThat(row.get("name")).isNotNull();
            assertThat(row.get("age")).isInstanceOf(Integer.class);
            assertThat(row.get("email").toString()).contains("@");
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateMockJson_withMixedTypes_resolvesByFormatThenType() {
        Map<String, Object> jsonSchema = new LinkedHashMap<>();
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("id", Map.of("type", "string", "format", "uuid"));
        properties.put("ip", Map.of("type", "string", "format", "ipv4"));
        properties.put("price", Map.of("type", "number"));
        properties.put("active", Map.of("type", "boolean"));
        properties.put("website", Map.of("type", "string", "format", "uri"));
        jsonSchema.put("properties", properties);

        Map<String, Object> result = service.generateMockJson(jsonSchema, 2, "mixed-json");

        List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
        assertThat(data).hasSize(2);
        for (Map<String, Object> row : data) {
            assertThat(row.get("id").toString()).contains("-");
            assertThat(row.get("ip").toString()).matches("\\d+\\.\\d+\\.\\d+\\.\\d+");
            assertThat(row.get("price")).isInstanceOf(Double.class);
            assertThat(row.get("active")).isInstanceOf(Boolean.class);
            assertThat(row.get("website").toString()).startsWith("http");
        }
    }

    @Test
    void generateMockJson_withNullSchema_generatesEmptyRows() {
        Map<String, Object> result = service.generateMockJson(null, 2, "null-schema");

        assertThat((int) result.get("count")).isEqualTo(2);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
        assertThat(data).hasSize(2);
        for (Map<String, Object> row : data) {
            assertThat(row).isEmpty();
        }
    }

    // ========================================================================
    // generateMockCsv tests
    // ========================================================================

    @Test
    void generateMockCsv_withHeader_generatesCorrectHeader() {
        List<Map<String, String>> columns = List.of(
                Map.of("name", "Name", "type", "firstName"),
                Map.of("name", "Email", "type", "email")
        );

        Map<String, Object> result = service.generateMockCsv(columns, 2, ",", "csv-test");

        String csv = (String) result.get("csv");
        String[] lines = csv.split("\n");
        assertThat(lines[0]).isEqualTo("Name,Email");
        assertThat(lines).hasSize(3); // header + 2 rows
    }

    @Test
    void generateMockCsv_withCustomDelimiter_usesCorrectDelimiter() {
        List<Map<String, String>> columns = List.of(
                Map.of("name", "A", "type", "integer"),
                Map.of("name", "B", "type", "boolean")
        );

        Map<String, Object> result = service.generateMockCsv(columns, 2, ";", "semi-csv");

        String csv = (String) result.get("csv");
        String[] lines = csv.split("\n");
        assertThat(lines[0]).isEqualTo("A;B");
        for (int i = 1; i < lines.length; i++) {
            assertThat(lines[i]).contains(";");
        }
    }

    @Test
    void generateMockCsv_withValuesContainingDelimiter_escapesProperly() {
        // Using address type which generates values with commas (e.g., "123 Main St, New York")
        List<Map<String, String>> columns = List.of(
                Map.of("name", "Address", "type", "address")
        );

        Map<String, Object> result = service.generateMockCsv(columns, 3, ",", "escape-test");

        String csv = (String) result.get("csv");
        String[] lines = csv.split("\n");
        // Data rows should have their comma-containing values wrapped in quotes
        for (int i = 1; i < lines.length; i++) {
            // Address format always contains comma, so value should be quoted
            assertThat(lines[i]).startsWith("\"");
            assertThat(lines[i]).endsWith("\"");
        }
    }

    @Test
    void generateMockCsv_withDefaultDelimiter_usesComma() {
        List<Map<String, String>> columns = List.of(
                Map.of("name", "X", "type", "integer")
        );

        Map<String, Object> result = service.generateMockCsv(columns, 1, null, "default-delim");

        String csv = (String) result.get("csv");
        assertThat(csv).startsWith("X\n");
    }

    // ========================================================================
    // listMockGenerators tests
    // ========================================================================

    @Test
    void listMockGenerators_returns16Generators() {
        List<Map<String, Object>> generators = service.listMockGenerators();

        assertThat(generators).hasSize(16);
    }

    @Test
    void listMockGenerators_allHaveNameAndDescription() {
        List<Map<String, Object>> generators = service.listMockGenerators();

        for (Map<String, Object> gen : generators) {
            assertThat(gen).containsKeys("name", "description");
            assertThat(gen.get("name")).isNotNull();
            assertThat(gen.get("description").toString()).isNotBlank();
        }
    }

    @Test
    void listMockGenerators_containsExpectedNames() {
        List<Map<String, Object>> generators = service.listMockGenerators();

        List<String> names = generators.stream()
                .map(g -> (String) g.get("name"))
                .toList();

        assertThat(names).containsExactly(
                "firstName", "lastName", "email", "phone", "address", "company",
                "date", "integer", "float", "boolean", "uuid", "sentence",
                "paragraph", "url", "ipv4", "hexColor"
        );
    }

    @Test
    void listMockGenerators_doesNotPersist() {
        service.listMockGenerators();

        verify(repository, never()).save(any());
    }

    // ========================================================================
    // Format verification tests
    // ========================================================================

    @Test
    @SuppressWarnings("unchecked")
    void generateMockData_withUrlType_generatesValidUrl() {
        List<Map<String, String>> schema = List.of(Map.of("field", "site", "type", "url"));

        Map<String, Object> result = service.generateMockData(schema, 5, "urls");

        List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
        for (Map<String, Object> row : data) {
            String url = (String) row.get("site");
            assertThat(url).matches("https?://.+\\..+/.+");
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateMockData_withParagraphType_containsMultipleSentences() {
        List<Map<String, String>> schema = List.of(Map.of("field", "text", "type", "paragraph"));

        Map<String, Object> result = service.generateMockData(schema, 3, "paragraphs");

        List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
        for (Map<String, Object> row : data) {
            String paragraph = (String) row.get("text");
            // A paragraph has 3-7 sentences, each ending with a period
            long periodCount = paragraph.chars().filter(c -> c == '.').count();
            assertThat(periodCount).isBetween(3L, 7L);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateMockData_withIntegerType_generatesInRange() {
        List<Map<String, String>> schema = List.of(Map.of("field", "num", "type", "integer"));

        Map<String, Object> result = service.generateMockData(schema, 20, "integers");

        List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
        for (Map<String, Object> row : data) {
            int val = (int) row.get("num");
            assertThat(val).isBetween(0, 10000);
        }
    }

    // ========================================================================
    // Deterministic test with fixed seed
    // ========================================================================

    @Test
    @SuppressWarnings("unchecked")
    void generateMockData_withFixedSeed_producesDeterministicResults() {
        DataMockGeneratorService service1 = new DataMockGeneratorService(repository, new Random(123L));
        DataMockGeneratorService service2 = new DataMockGeneratorService(repository, new Random(123L));

        List<Map<String, String>> schema = List.of(
                Map.of("field", "name", "type", "firstName"),
                Map.of("field", "num", "type", "integer")
        );

        Map<String, Object> result1 = service1.generateMockData(schema, 5, "det");
        Map<String, Object> result2 = service2.generateMockData(schema, 5, "det");

        List<Map<String, Object>> data1 = (List<Map<String, Object>>) result1.get("data");
        List<Map<String, Object>> data2 = (List<Map<String, Object>>) result2.get("data");

        for (int i = 0; i < 5; i++) {
            assertThat(data1.get(i).get("name")).isEqualTo(data2.get(i).get("name"));
            assertThat(data1.get(i).get("num")).isEqualTo(data2.get(i).get("num"));
        }
    }
}
