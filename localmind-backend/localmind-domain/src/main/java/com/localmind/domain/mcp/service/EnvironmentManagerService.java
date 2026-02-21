package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.port.in.EnvironmentManagerUseCase;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Stateless domain service implementing the environment-manager tool group.
 * Provides environment file listing (simulated), variable parsing, comparison,
 * validation and template generation.
 * No Spring annotations — registered as a bean via DomainConfig.
 * No persistence — purely computational logic.
 */
public class EnvironmentManagerService implements EnvironmentManagerUseCase {

    /**
     * Set of key substrings that indicate a secret value that should be masked.
     */
    static final Set<String> SECRET_PATTERNS = Set.of(
            "PASSWORD", "SECRET", "KEY", "TOKEN", "API_KEY", "CREDENTIAL",
            "PRIVATE", "AUTH"
    );

    static final String MASK_VALUE = "********";

    // ========== 1. listEnvironments ==========

    @Override
    public Map<String, Object> listEnvironments(String directory, boolean recursive) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (directory == null || directory.isBlank()) {
            result.put("error", "Directory path is required");
            return result;
        }

        // Simulated result — domain layer cannot access filesystem
        List<Map<String, Object>> files = new ArrayList<>();

        files.add(buildFileEntry(".env", directory + "/.env", 256));
        files.add(buildFileEntry(".env.example", directory + "/.env.example", 312));
        files.add(buildFileEntry(".env.local", directory + "/.env.local", 198));

        if (recursive) {
            files.add(buildFileEntry(".env", directory + "/backend/.env", 180));
            files.add(buildFileEntry(".env", directory + "/frontend/.env", 145));
        }

        result.put("directory", directory);
        result.put("files", files);
        result.put("count", files.size());
        result.put("note", "Simulated result — domain layer does not perform I/O");

        return result;
    }

    // ========== 2. getEnvVars ==========

    @Override
    public Map<String, Object> getEnvVars(String filePath, String fileContent,
                                           boolean showSecrets, String filter) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (fileContent == null || fileContent.isBlank()) {
            result.put("error", "File content is required");
            return result;
        }

        List<Map<String, Object>> variables = new ArrayList<>();
        String[] lines = fileContent.split("\\r?\\n");

        for (String line : lines) {
            String trimmed = line.trim();
            // Skip empty lines and comments
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }

            int equalsIndex = trimmed.indexOf('=');
            if (equalsIndex <= 0) {
                continue;
            }

            String key = trimmed.substring(0, equalsIndex).trim();
            String value = trimmed.substring(equalsIndex + 1).trim();
            // Remove surrounding quotes if present
            value = stripQuotes(value);

            // Apply filter if specified
            if (filter != null && !filter.isBlank()
                    && !key.toLowerCase().contains(filter.toLowerCase())) {
                continue;
            }

            boolean isSecret = isSecretKey(key);
            String displayValue = (!isSecret || showSecrets) ? value : MASK_VALUE;

            Map<String, Object> variable = new LinkedHashMap<>();
            variable.put("key", key);
            variable.put("value", displayValue);
            variable.put("masked", isSecret && !showSecrets);
            variables.add(variable);
        }

        result.put("file", filePath != null ? filePath : "unknown");
        result.put("variables", variables);
        result.put("count", variables.size());

        return result;
    }

    // ========== 3. compareEnvironments ==========

    @Override
    public Map<String, Object> compareEnvironments(String filePathA, String contentA,
                                                    String filePathB, String contentB,
                                                    boolean showValues) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (contentA == null || contentA.isBlank()) {
            result.put("error", "Content of file A is required");
            return result;
        }

        if (contentB == null || contentB.isBlank()) {
            result.put("error", "Content of file B is required");
            return result;
        }

        Map<String, String> varsA = parseEnvContent(contentA);
        Map<String, String> varsB = parseEnvContent(contentB);

        List<String> onlyInA = new ArrayList<>();
        List<String> onlyInB = new ArrayList<>();
        List<String> common = new ArrayList<>();
        List<String> different = new ArrayList<>();

        for (String key : varsA.keySet()) {
            if (!varsB.containsKey(key)) {
                onlyInA.add(showValues ? key + "=" + varsA.get(key) : key);
            } else {
                if (varsA.get(key).equals(varsB.get(key))) {
                    common.add(showValues ? key + "=" + varsA.get(key) : key);
                } else {
                    if (showValues) {
                        different.add(key + ": A=" + varsA.get(key) + " | B=" + varsB.get(key));
                    } else {
                        different.add(key);
                    }
                }
            }
        }

        for (String key : varsB.keySet()) {
            if (!varsA.containsKey(key)) {
                onlyInB.add(showValues ? key + "=" + varsB.get(key) : key);
            }
        }

        result.put("fileA", filePathA != null ? filePathA : "file-A");
        result.put("fileB", filePathB != null ? filePathB : "file-B");
        result.put("onlyInA", onlyInA);
        result.put("onlyInB", onlyInB);
        result.put("different", different);
        result.put("common", common);

        return result;
    }

    // ========== 4. validateEnv ==========

    @Override
    public Map<String, Object> validateEnv(String envContent, String templateContent, boolean strict) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (envContent == null || envContent.isBlank()) {
            result.put("error", "Environment content is required");
            return result;
        }

        if (templateContent == null || templateContent.isBlank()) {
            result.put("error", "Template content is required");
            return result;
        }

        Map<String, String> envVars = parseEnvContent(envContent);
        Map<String, String> templateVars = parseEnvContent(templateContent);

        List<String> missing = new ArrayList<>();
        List<String> extra = new ArrayList<>();
        List<String> empty = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // Check for missing variables (in template but not in env)
        for (String key : templateVars.keySet()) {
            if (!envVars.containsKey(key)) {
                missing.add(key);
                errors.add("Missing required variable: " + key);
            } else if (envVars.get(key).isEmpty()) {
                empty.add(key);
                errors.add("Variable has empty value: " + key);
            }
        }

        // Check for extra variables (in env but not in template)
        for (String key : envVars.keySet()) {
            if (!templateVars.containsKey(key)) {
                extra.add(key);
                if (strict) {
                    errors.add("Extra variable not in template: " + key);
                }
            }
        }

        boolean valid = missing.isEmpty() && empty.isEmpty() && (!strict || extra.isEmpty());

        result.put("valid", valid);
        result.put("missing", missing);
        result.put("extra", extra);
        result.put("empty", empty);
        result.put("errors", errors);

        return result;
    }

    // ========== 5. generateEnvTemplate ==========

    @Override
    public Map<String, Object> generateEnvTemplate(String sourceContent, boolean preserveDefaults) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (sourceContent == null || sourceContent.isBlank()) {
            result.put("error", "Source content is required");
            return result;
        }

        StringBuilder template = new StringBuilder();
        String[] lines = sourceContent.split("\\r?\\n");
        int variableCount = 0;
        int secretsMasked = 0;

        for (String line : lines) {
            String trimmed = line.trim();

            // Preserve comments and empty lines
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                template.append(line).append("\n");
                continue;
            }

            int equalsIndex = trimmed.indexOf('=');
            if (equalsIndex <= 0) {
                template.append(line).append("\n");
                continue;
            }

            String key = trimmed.substring(0, equalsIndex).trim();
            String value = trimmed.substring(equalsIndex + 1).trim();
            value = stripQuotes(value);
            variableCount++;

            if (isSecretKey(key)) {
                template.append(key).append("=").append("\n");
                secretsMasked++;
            } else if (preserveDefaults && !value.isEmpty()) {
                template.append(key).append("=").append(value).append("\n");
            } else {
                template.append(key).append("=").append("\n");
            }
        }

        result.put("template", template.toString());
        result.put("variableCount", variableCount);
        result.put("secretsMasked", secretsMasked);

        return result;
    }

    // ========== Helper Methods ==========

    /**
     * Parses raw .env file content into a key-value map.
     */
    Map<String, String> parseEnvContent(String content) {
        Map<String, String> vars = new LinkedHashMap<>();
        if (content == null) return vars;

        String[] lines = content.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            int equalsIndex = trimmed.indexOf('=');
            if (equalsIndex <= 0) {
                continue;
            }
            String key = trimmed.substring(0, equalsIndex).trim();
            String value = trimmed.substring(equalsIndex + 1).trim();
            value = stripQuotes(value);
            vars.put(key, value);
        }
        return vars;
    }

    /**
     * Checks if a key name indicates a secret value.
     */
    boolean isSecretKey(String key) {
        String upper = key.toUpperCase();
        for (String pattern : SECRET_PATTERNS) {
            if (upper.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes surrounding single or double quotes from a value.
     */
    private String stripQuotes(String value) {
        if (value.length() >= 2) {
            if ((value.startsWith("\"") && value.endsWith("\""))
                    || (value.startsWith("'") && value.endsWith("'"))) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }

    private Map<String, Object> buildFileEntry(String name, String path, int size) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("name", name);
        entry.put("path", path);
        entry.put("size", size);
        return entry;
    }
}
