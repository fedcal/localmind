package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.GeneratedDataset;
import com.localmind.domain.mcp.port.in.DataMockGeneratorUseCase;
import com.localmind.domain.mcp.port.out.DataMockRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Domain service implementing mock data generation tools.
 * Pure Java, zero Spring dependencies.
 * Accepts a java.util.Random for testability (deterministic with fixed seed).
 */
public class DataMockGeneratorService implements DataMockGeneratorUseCase {

    private final DataMockRepository repository;
    private final Random random;

    private static final int DEFAULT_COUNT = 10;
    private static final int MAX_COUNT = 10000;

    // --- Hardcoded lists ---

    private static final String[] FIRST_NAMES = {
            "Alice", "Bob", "Charlie", "Diana", "Edward", "Fiona", "George", "Hannah",
            "Ivan", "Julia", "Kevin", "Laura", "Marco", "Nina", "Oscar", "Paula",
            "Quentin", "Rachel", "Samuel", "Tina", "Ulysses", "Vera", "Walter", "Xena",
            "Yuri", "Zoe", "Adrian", "Bianca", "Carlo", "Daniela", "Emilio", "Francesca",
            "Giovanni", "Helena", "Igor", "Jasmine", "Klaus", "Lucia", "Matteo", "Nadia"
    };

    private static final String[] LAST_NAMES = {
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
            "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas",
            "Taylor", "Moore", "Jackson", "Martin", "Lee", "Perez", "Thompson", "White",
            "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson", "Walker", "Young",
            "Allen", "King", "Wright", "Scott", "Torres", "Nguyen", "Hill", "Flores"
    };

    private static final String[] COMPANIES = {
            "Acme Corp", "Globex Industries", "Initech", "Umbrella Corp", "Stark Industries",
            "Wayne Enterprises", "Cyberdyne Systems", "Soylent Corp", "Weyland-Yutani", "Tyrell Corp",
            "Massive Dynamic", "Oscorp Industries", "LexCorp", "Aperture Science", "Black Mesa",
            "Wonka Industries", "Prestige Worldwide", "Dunder Mifflin", "Pied Piper", "Hooli"
    };

    private static final String[] STREETS = {
            "Main St", "Oak Ave", "Elm St", "Park Blvd", "Cedar Ln",
            "Maple Dr", "Pine St", "Washington Ave", "Lake Rd", "Hill St"
    };

    private static final String[] CITIES = {
            "New York", "Los Angeles", "Chicago", "Houston", "Phoenix",
            "Philadelphia", "San Antonio", "San Diego", "Dallas", "Austin"
    };

    private static final String[] DOMAINS = {
            "gmail.com", "yahoo.com", "outlook.com", "mail.com", "proton.me",
            "fastmail.com", "icloud.com", "hotmail.com"
    };

    private static final String[] LOREM_WORDS = {
            "lorem", "ipsum", "dolor", "sit", "amet", "consectetur", "adipiscing", "elit",
            "sed", "do", "eiusmod", "tempor", "incididunt", "ut", "labore", "et",
            "dolore", "magna", "aliqua", "enim", "ad", "minim", "veniam", "quis",
            "nostrud", "exercitation", "ullamco", "laboris", "nisi", "aliquip"
    };

    private static final String[] URL_SUBDOMAINS = {"www", "api", "app", "dev", "staging", "cdn"};
    private static final String[] URL_DOMAINS = {"example.com", "test.org", "sample.net", "demo.io", "mock.dev"};
    private static final String[] URL_PATHS = {"home", "about", "products", "docs", "api", "users", "data"};

    // --- Generator descriptors ---

    private static final List<Map<String, Object>> GENERATORS;

    static {
        GENERATORS = new ArrayList<>();
        addGenerator("firstName", "Generates a random first name from a list of 40 names");
        addGenerator("lastName", "Generates a random last name from a list of 40 surnames");
        addGenerator("email", "Generates a random email address (firstName.lastName@domain)");
        addGenerator("phone", "Generates a random US phone number in (XXX) XXX-XXXX format");
        addGenerator("address", "Generates a random street address with city");
        addGenerator("company", "Generates a random company name from a list of 20 companies");
        addGenerator("date", "Generates a random date between 2000-01-01 and 2025-12-31 in yyyy-MM-dd format");
        addGenerator("integer", "Generates a random integer between 0 and 10000");
        addGenerator("float", "Generates a random float between 0 and 10000 with 2 decimal places");
        addGenerator("boolean", "Generates a random boolean value (true/false)");
        addGenerator("uuid", "Generates a random UUID v4");
        addGenerator("sentence", "Generates a random lorem ipsum sentence (5-15 words)");
        addGenerator("paragraph", "Generates a random paragraph with 3-7 lorem ipsum sentences");
        addGenerator("url", "Generates a random URL with subdomain, domain, and path");
        addGenerator("ipv4", "Generates a random IPv4 address (valid ranges)");
        addGenerator("hexColor", "Generates a random hex color code (#XXXXXX)");
    }

    private static void addGenerator(String name, String description) {
        Map<String, Object> gen = new LinkedHashMap<>();
        gen.put("name", name);
        gen.put("description", description);
        GENERATORS.add(gen);
    }

    public DataMockGeneratorService(DataMockRepository repository, Random random) {
        this.repository = repository;
        this.random = random;
    }

    public DataMockGeneratorService(DataMockRepository repository) {
        this(repository, new Random());
    }

    // ========================================================================
    // 1. generateMockData
    // ========================================================================

    @Override
    public Map<String, Object> generateMockData(List<Map<String, String>> schema, int count, String name) {
        int effectiveCount = normalizeCount(count);
        List<Map<String, String>> effectiveSchema = schema != null ? schema : List.of();

        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 0; i < effectiveCount; i++) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (Map<String, String> fieldDef : effectiveSchema) {
                String field = fieldDef.get("field");
                String type = fieldDef.get("type");
                if (field != null) {
                    row.put(field, generateValue(type));
                }
            }
            data.add(row);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", name != null ? name : "unnamed");
        result.put("count", effectiveCount);
        result.put("schema", effectiveSchema);
        result.put("data", data);

        // Persist
        GeneratedDataset dataset = GeneratedDataset.builder()
                .id(UUID.randomUUID().toString())
                .name(name != null ? name : "unnamed")
                .format("structured")
                .rowCount(effectiveCount)
                .schemaJson(listToJson(effectiveSchema))
                .sampleDataJson(listOfMapsToJson(data))
                .createdAt(Instant.now())
                .build();
        repository.save(dataset);

        return result;
    }

    // ========================================================================
    // 2. generateMockJson
    // ========================================================================

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> generateMockJson(Map<String, Object> jsonSchema, int count, String name) {
        int effectiveCount = normalizeCount(count);
        Map<String, Object> properties = new LinkedHashMap<>();
        if (jsonSchema != null && jsonSchema.containsKey("properties")) {
            Object props = jsonSchema.get("properties");
            if (props instanceof Map) {
                properties = (Map<String, Object>) props;
            }
        }

        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 0; i < effectiveCount; i++) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                String fieldName = entry.getKey();
                String generatorType = resolveJsonSchemaType(entry.getValue());
                row.put(fieldName, generateValue(generatorType));
            }
            data.add(row);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", name != null ? name : "unnamed");
        result.put("count", effectiveCount);
        result.put("data", data);

        // Persist
        GeneratedDataset dataset = GeneratedDataset.builder()
                .id(UUID.randomUUID().toString())
                .name(name != null ? name : "unnamed")
                .format("json-schema")
                .rowCount(effectiveCount)
                .schemaJson(mapToJson(jsonSchema != null ? jsonSchema : Map.of()))
                .sampleDataJson(listOfMapsToJson(data))
                .createdAt(Instant.now())
                .build();
        repository.save(dataset);

        return result;
    }

    // ========================================================================
    // 3. generateMockCsv
    // ========================================================================

    @Override
    public Map<String, Object> generateMockCsv(List<Map<String, String>> columns, int count, String delimiter, String name) {
        int effectiveCount = normalizeCount(count);
        String effectiveDelimiter = (delimiter != null && !delimiter.isEmpty()) ? delimiter : ",";
        List<Map<String, String>> effectiveColumns = columns != null ? columns : List.of();

        StringBuilder csv = new StringBuilder();

        // Header
        for (int i = 0; i < effectiveColumns.size(); i++) {
            if (i > 0) csv.append(effectiveDelimiter);
            String colName = effectiveColumns.get(i).get("name");
            csv.append(escapeCsv(colName != null ? colName : "", effectiveDelimiter));
        }
        csv.append("\n");

        // Rows
        for (int r = 0; r < effectiveCount; r++) {
            for (int i = 0; i < effectiveColumns.size(); i++) {
                if (i > 0) csv.append(effectiveDelimiter);
                String type = effectiveColumns.get(i).get("type");
                Object value = generateValue(type);
                csv.append(escapeCsv(value != null ? value.toString() : "", effectiveDelimiter));
            }
            csv.append("\n");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", name != null ? name : "unnamed");
        result.put("count", effectiveCount);
        result.put("csv", csv.toString());

        // Persist
        GeneratedDataset dataset = GeneratedDataset.builder()
                .id(UUID.randomUUID().toString())
                .name(name != null ? name : "unnamed")
                .format("csv")
                .rowCount(effectiveCount)
                .schemaJson(listToJson(effectiveColumns))
                .sampleDataJson(csv.toString())
                .createdAt(Instant.now())
                .build();
        repository.save(dataset);

        return result;
    }

    // ========================================================================
    // 4. listMockGenerators
    // ========================================================================

    @Override
    public List<Map<String, Object>> listMockGenerators() {
        return GENERATORS;
    }

    // ========================================================================
    // Value generators
    // ========================================================================

    Object generateValue(String type) {
        if (type == null) {
            return null;
        }
        switch (type.toLowerCase()) {
            case "firstname":
                return generateFirstName();
            case "lastname":
                return generateLastName();
            case "email":
                return generateEmail();
            case "phone":
                return generatePhone();
            case "address":
                return generateAddress();
            case "company":
                return generateCompany();
            case "date":
                return generateDate();
            case "integer":
                return generateInteger();
            case "float":
                return generateFloat();
            case "boolean":
                return generateBoolean();
            case "uuid":
                return generateUuid();
            case "sentence":
                return generateSentence();
            case "paragraph":
                return generateParagraph();
            case "url":
                return generateUrl();
            case "ipv4":
                return generateIpv4();
            case "hexcolor":
                return generateHexColor();
            default:
                return null;
        }
    }

    private String generateFirstName() {
        return FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
    }

    private String generateLastName() {
        return LAST_NAMES[random.nextInt(LAST_NAMES.length)];
    }

    private String generateEmail() {
        String first = generateFirstName().toLowerCase();
        String last = generateLastName().toLowerCase();
        String separator = random.nextBoolean() ? "." : "_";
        int num = random.nextInt(99) + 1;
        String domain = DOMAINS[random.nextInt(DOMAINS.length)];
        return first + separator + last + num + "@" + domain;
    }

    private String generatePhone() {
        int area = 200 + random.nextInt(800);
        int prefix = 200 + random.nextInt(800);
        int line = random.nextInt(10000);
        return String.format("(%03d) %03d-%04d", area, prefix, line);
    }

    private String generateAddress() {
        int number = 1 + random.nextInt(9999);
        String street = STREETS[random.nextInt(STREETS.length)];
        String city = CITIES[random.nextInt(CITIES.length)];
        return number + " " + street + ", " + city;
    }

    private String generateCompany() {
        return COMPANIES[random.nextInt(COMPANIES.length)];
    }

    private String generateDate() {
        // Random date between 2000-01-01 and 2025-12-31
        LocalDate start = LocalDate.of(2000, 1, 1);
        LocalDate end = LocalDate.of(2025, 12, 31);
        long totalDays = end.toEpochDay() - start.toEpochDay();
        long randomDay = (long) (random.nextDouble() * totalDays);
        LocalDate date = start.plusDays(randomDay);
        return date.toString(); // yyyy-MM-dd format
    }

    private int generateInteger() {
        return random.nextInt(10001);
    }

    private double generateFloat() {
        double val = random.nextDouble() * 10000;
        return Math.round(val * 100.0) / 100.0;
    }

    private boolean generateBoolean() {
        return random.nextBoolean();
    }

    private String generateUuid() {
        // Use Random-based UUID generation for deterministic testing
        long msb = random.nextLong();
        long lsb = random.nextLong();
        // Set version 4 bits
        msb = (msb & 0xFFFFFFFFFFFF0FFFL) | 0x0000000000004000L;
        // Set variant bits
        lsb = (lsb & 0x3FFFFFFFFFFFFFFFL) | 0x8000000000000000L;
        return new UUID(msb, lsb).toString();
    }

    private String generateSentence() {
        int wordCount = 5 + random.nextInt(11); // 5-15 words
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < wordCount; i++) {
            if (i > 0) sb.append(" ");
            sb.append(LOREM_WORDS[random.nextInt(LOREM_WORDS.length)]);
        }
        // Capitalize first letter and add period
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        sb.append(".");
        return sb.toString();
    }

    private String generateParagraph() {
        int sentenceCount = 3 + random.nextInt(5); // 3-7 sentences
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sentenceCount; i++) {
            if (i > 0) sb.append(" ");
            sb.append(generateSentence());
        }
        return sb.toString();
    }

    private String generateUrl() {
        String protocol = random.nextBoolean() ? "https" : "http";
        String subdomain = URL_SUBDOMAINS[random.nextInt(URL_SUBDOMAINS.length)];
        String domain = URL_DOMAINS[random.nextInt(URL_DOMAINS.length)];
        String path = URL_PATHS[random.nextInt(URL_PATHS.length)];
        return protocol + "://" + subdomain + "." + domain + "/" + path;
    }

    private String generateIpv4() {
        int a = 1 + random.nextInt(255);      // 1-255
        int b = random.nextInt(256);           // 0-255
        int c = random.nextInt(256);           // 0-255
        int d = 1 + random.nextInt(254);       // 1-254
        return a + "." + b + "." + c + "." + d;
    }

    private String generateHexColor() {
        int color = random.nextInt(0x1000000); // 0x000000 to 0xFFFFFF
        return String.format("#%06X", color);
    }

    // ========================================================================
    // JSON Schema type resolution
    // ========================================================================

    @SuppressWarnings("unchecked")
    String resolveJsonSchemaType(Object propertyDef) {
        if (!(propertyDef instanceof Map)) {
            return "sentence";
        }
        Map<String, Object> prop = (Map<String, Object>) propertyDef;

        // Format has priority over type
        String format = prop.get("format") != null ? prop.get("format").toString().toLowerCase() : null;
        if (format != null) {
            switch (format) {
                case "email": return "email";
                case "uri": case "url": return "url";
                case "uuid": return "uuid";
                case "ipv4": return "ipv4";
                case "date": return "date";
                case "phone": return "phone";
                case "firstname": return "firstName";
                case "lastname": return "lastName";
                case "address": return "address";
                case "company": return "company";
                case "hexcolor": return "hexColor";
                case "paragraph": return "paragraph";
                default: break;
            }
        }

        // Fallback on type
        String type = prop.get("type") != null ? prop.get("type").toString().toLowerCase() : "string";
        switch (type) {
            case "string": return "sentence";
            case "number": return "float";
            case "integer": return "integer";
            case "boolean": return "boolean";
            default: return "sentence";
        }
    }

    // ========================================================================
    // CSV escaping
    // ========================================================================

    String escapeCsv(String value, String delimiter) {
        if (value == null) {
            return "";
        }
        boolean needsQuoting = value.contains(delimiter) || value.contains("\"") || value.contains("\n") || value.contains("\r");
        if (needsQuoting) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // ========================================================================
    // Count normalization
    // ========================================================================

    int normalizeCount(int count) {
        if (count <= 0) return DEFAULT_COUNT;
        return Math.min(count, MAX_COUNT);
    }

    // ========================================================================
    // JSON serialization utilities (no external dependencies)
    // ========================================================================

    private String mapToJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(escapeJson(entry.getKey())).append("\":");
            sb.append(valueToJson(entry.getValue()));
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private String listToJson(List<?> list) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Object item : list) {
            if (!first) sb.append(",");
            sb.append(valueToJson(item));
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }

    private String listOfMapsToJson(List<Map<String, Object>> list) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Map<String, Object> item : list) {
            if (!first) sb.append(",");
            sb.append(mapToJson(item));
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String valueToJson(Object value) {
        if (value == null) return "null";
        if (value instanceof String) return "\"" + escapeJson((String) value) + "\"";
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        if (value instanceof Map) return mapToJson((Map<String, Object>) value);
        if (value instanceof List) return listToJson((List<?>) value);
        return "\"" + escapeJson(value.toString()) + "\"";
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
