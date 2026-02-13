package com.localmind.domain.mcp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EnvironmentManagerServiceTest {

    private EnvironmentManagerService service;

    @BeforeEach
    void setUp() {
        service = new EnvironmentManagerService();
    }

    // ========== listEnvironments Tests ==========

    @Test
    @SuppressWarnings("unchecked")
    void listEnvironments_nonRecursive_returnsBaseFiles() {
        Map<String, Object> result = service.listEnvironments("/project", false);

        assertThat(result.get("directory")).isEqualTo("/project");
        List<Map<String, Object>> files = (List<Map<String, Object>>) result.get("files");
        assertThat(files).hasSize(3);
        assertThat(result.get("count")).isEqualTo(3);
    }

    @Test
    @SuppressWarnings("unchecked")
    void listEnvironments_recursive_returnsMoreFiles() {
        Map<String, Object> result = service.listEnvironments("/project", true);

        List<Map<String, Object>> files = (List<Map<String, Object>>) result.get("files");
        assertThat(files).hasSize(5);
        assertThat(result.get("count")).isEqualTo(5);
    }

    @Test
    void listEnvironments_withNullDirectory_returnsError() {
        Map<String, Object> result = service.listEnvironments(null, false);

        assertThat(result.get("error")).isEqualTo("Directory path is required");
    }

    @Test
    void listEnvironments_withBlankDirectory_returnsError() {
        Map<String, Object> result = service.listEnvironments("  ", false);

        assertThat(result.get("error")).isEqualTo("Directory path is required");
    }

    @Test
    void listEnvironments_containsSimulationNote() {
        Map<String, Object> result = service.listEnvironments("/project", false);

        assertThat(result.get("note")).isNotNull();
        assertThat(result.get("note").toString()).contains("Simulated");
    }

    // ========== getEnvVars Tests ==========

    @Test
    @SuppressWarnings("unchecked")
    void getEnvVars_parsesVariablesCorrectly() {
        String content = "DB_HOST=localhost\nDB_PORT=3306\nDB_NAME=mydb";

        Map<String, Object> result = service.getEnvVars("test.env", content, false, null);

        assertThat(result.get("file")).isEqualTo("test.env");
        assertThat(result.get("count")).isEqualTo(3);
        List<Map<String, Object>> vars = (List<Map<String, Object>>) result.get("variables");
        assertThat(vars).hasSize(3);
        assertThat(vars.get(0).get("key")).isEqualTo("DB_HOST");
        assertThat(vars.get(0).get("value")).isEqualTo("localhost");
        assertThat(vars.get(0).get("masked")).isEqualTo(false);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getEnvVars_masksSecretValues() {
        String content = "DB_HOST=localhost\nDB_PASSWORD=secret123\nAPI_KEY=abc-def-123";

        Map<String, Object> result = service.getEnvVars("test.env", content, false, null);

        List<Map<String, Object>> vars = (List<Map<String, Object>>) result.get("variables");
        // DB_HOST should not be masked
        assertThat(vars.get(0).get("value")).isEqualTo("localhost");
        assertThat(vars.get(0).get("masked")).isEqualTo(false);
        // DB_PASSWORD should be masked
        assertThat(vars.get(1).get("value")).isEqualTo("********");
        assertThat(vars.get(1).get("masked")).isEqualTo(true);
        // API_KEY should be masked
        assertThat(vars.get(2).get("value")).isEqualTo("********");
        assertThat(vars.get(2).get("masked")).isEqualTo(true);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getEnvVars_showSecretsWhenRequested() {
        String content = "DB_PASSWORD=secret123";

        Map<String, Object> result = service.getEnvVars("test.env", content, true, null);

        List<Map<String, Object>> vars = (List<Map<String, Object>>) result.get("variables");
        assertThat(vars.get(0).get("value")).isEqualTo("secret123");
        assertThat(vars.get(0).get("masked")).isEqualTo(false);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getEnvVars_appliesFilter() {
        String content = "DB_HOST=localhost\nDB_PORT=3306\nAPP_NAME=myapp";

        Map<String, Object> result = service.getEnvVars("test.env", content, false, "DB");

        List<Map<String, Object>> vars = (List<Map<String, Object>>) result.get("variables");
        assertThat(vars).hasSize(2);
        assertThat(vars.get(0).get("key")).isEqualTo("DB_HOST");
        assertThat(vars.get(1).get("key")).isEqualTo("DB_PORT");
    }

    @Test
    @SuppressWarnings("unchecked")
    void getEnvVars_skipsCommentsAndEmptyLines() {
        String content = "# This is a comment\n\nDB_HOST=localhost\n  # Another comment\nDB_PORT=3306";

        Map<String, Object> result = service.getEnvVars("test.env", content, false, null);

        assertThat(result.get("count")).isEqualTo(2);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getEnvVars_stripsQuotesFromValues() {
        String content = "VAR1=\"quoted-value\"\nVAR2='single-quoted'";

        Map<String, Object> result = service.getEnvVars("test.env", content, false, null);

        List<Map<String, Object>> vars = (List<Map<String, Object>>) result.get("variables");
        assertThat(vars.get(0).get("value")).isEqualTo("quoted-value");
        assertThat(vars.get(1).get("value")).isEqualTo("single-quoted");
    }

    @Test
    void getEnvVars_withNullContent_returnsError() {
        Map<String, Object> result = service.getEnvVars("test.env", null, false, null);

        assertThat(result.get("error")).isEqualTo("File content is required");
    }

    @Test
    void getEnvVars_withBlankContent_returnsError() {
        Map<String, Object> result = service.getEnvVars("test.env", "  ", false, null);

        assertThat(result.get("error")).isEqualTo("File content is required");
    }

    @Test
    void getEnvVars_withNullFilePath_usesUnknown() {
        String content = "VAR=value";

        Map<String, Object> result = service.getEnvVars(null, content, false, null);

        assertThat(result.get("file")).isEqualTo("unknown");
    }

    // ========== compareEnvironments Tests ==========

    @Test
    @SuppressWarnings("unchecked")
    void compareEnvironments_findsDifferences() {
        String contentA = "DB_HOST=localhost\nDB_PORT=3306\nONLY_A=value";
        String contentB = "DB_HOST=localhost\nDB_PORT=5432\nONLY_B=value";

        Map<String, Object> result = service.compareEnvironments(
                "envA", contentA, "envB", contentB, false);

        List<String> onlyInA = (List<String>) result.get("onlyInA");
        List<String> onlyInB = (List<String>) result.get("onlyInB");
        List<String> common = (List<String>) result.get("common");
        List<String> different = (List<String>) result.get("different");

        assertThat(onlyInA).containsExactly("ONLY_A");
        assertThat(onlyInB).containsExactly("ONLY_B");
        assertThat(common).containsExactly("DB_HOST");
        assertThat(different).containsExactly("DB_PORT");
    }

    @Test
    @SuppressWarnings("unchecked")
    void compareEnvironments_showValues_includesValues() {
        String contentA = "DB_HOST=localhost\nDB_PORT=3306";
        String contentB = "DB_HOST=localhost\nDB_PORT=5432";

        Map<String, Object> result = service.compareEnvironments(
                "envA", contentA, "envB", contentB, true);

        List<String> different = (List<String>) result.get("different");
        assertThat(different.get(0)).contains("A=3306");
        assertThat(different.get(0)).contains("B=5432");

        List<String> common = (List<String>) result.get("common");
        assertThat(common.get(0)).contains("DB_HOST=localhost");
    }

    @Test
    void compareEnvironments_withNullContentA_returnsError() {
        Map<String, Object> result = service.compareEnvironments(
                "envA", null, "envB", "KEY=val", false);

        assertThat(result.get("error")).isEqualTo("Content of file A is required");
    }

    @Test
    void compareEnvironments_withBlankContentB_returnsError() {
        Map<String, Object> result = service.compareEnvironments(
                "envA", "KEY=val", "envB", "  ", false);

        assertThat(result.get("error")).isEqualTo("Content of file B is required");
    }

    @Test
    @SuppressWarnings("unchecked")
    void compareEnvironments_identicalFiles_allCommon() {
        String content = "A=1\nB=2\nC=3";

        Map<String, Object> result = service.compareEnvironments(
                "envA", content, "envB", content, false);

        assertThat((List<String>) result.get("onlyInA")).isEmpty();
        assertThat((List<String>) result.get("onlyInB")).isEmpty();
        assertThat((List<String>) result.get("different")).isEmpty();
        assertThat((List<String>) result.get("common")).hasSize(3);
    }

    // ========== validateEnv Tests ==========

    @Test
    @SuppressWarnings("unchecked")
    void validateEnv_validFile_returnsValid() {
        String env = "DB_HOST=localhost\nDB_PORT=3306";
        String template = "DB_HOST=\nDB_PORT=";

        Map<String, Object> result = service.validateEnv(env, template, false);

        assertThat(result.get("valid")).isEqualTo(true);
        assertThat((List<String>) result.get("missing")).isEmpty();
        assertThat((List<String>) result.get("empty")).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void validateEnv_missingVariables_returnsInvalid() {
        String env = "DB_HOST=localhost";
        String template = "DB_HOST=\nDB_PORT=\nDB_NAME=";

        Map<String, Object> result = service.validateEnv(env, template, false);

        assertThat(result.get("valid")).isEqualTo(false);
        List<String> missing = (List<String>) result.get("missing");
        assertThat(missing).containsExactly("DB_PORT", "DB_NAME");
    }

    @Test
    @SuppressWarnings("unchecked")
    void validateEnv_emptyValues_returnsInvalid() {
        String env = "DB_HOST=\nDB_PORT=3306";
        String template = "DB_HOST=\nDB_PORT=";

        Map<String, Object> result = service.validateEnv(env, template, false);

        assertThat(result.get("valid")).isEqualTo(false);
        List<String> empty = (List<String>) result.get("empty");
        assertThat(empty).containsExactly("DB_HOST");
    }

    @Test
    @SuppressWarnings("unchecked")
    void validateEnv_extraVariables_nonStrictIsValid() {
        String env = "DB_HOST=localhost\nDB_PORT=3306\nEXTRA_VAR=value";
        String template = "DB_HOST=\nDB_PORT=";

        Map<String, Object> result = service.validateEnv(env, template, false);

        assertThat(result.get("valid")).isEqualTo(true);
        List<String> extra = (List<String>) result.get("extra");
        assertThat(extra).containsExactly("EXTRA_VAR");
    }

    @Test
    @SuppressWarnings("unchecked")
    void validateEnv_extraVariables_strictIsInvalid() {
        String env = "DB_HOST=localhost\nEXTRA_VAR=value";
        String template = "DB_HOST=";

        Map<String, Object> result = service.validateEnv(env, template, true);

        assertThat(result.get("valid")).isEqualTo(false);
        List<String> errors = (List<String>) result.get("errors");
        assertThat(errors).anyMatch(e -> e.contains("Extra variable"));
    }

    @Test
    void validateEnv_withNullEnvContent_returnsError() {
        Map<String, Object> result = service.validateEnv(null, "TEMPLATE=", false);

        assertThat(result.get("error")).isEqualTo("Environment content is required");
    }

    @Test
    void validateEnv_withNullTemplateContent_returnsError() {
        Map<String, Object> result = service.validateEnv("KEY=value", null, false);

        assertThat(result.get("error")).isEqualTo("Template content is required");
    }

    // ========== generateEnvTemplate Tests ==========

    @Test
    void generateEnvTemplate_stripsSecrets() {
        String source = "DB_HOST=localhost\nDB_PASSWORD=secret123\nAPI_KEY=abc-def";

        Map<String, Object> result = service.generateEnvTemplate(source, false);

        String template = (String) result.get("template");
        assertThat(template).contains("DB_HOST=");
        assertThat(template).contains("DB_PASSWORD=\n");
        assertThat(template).contains("API_KEY=\n");
        assertThat(template).doesNotContain("secret123");
        assertThat(template).doesNotContain("abc-def");
        assertThat(result.get("variableCount")).isEqualTo(3);
        assertThat(result.get("secretsMasked")).isEqualTo(2);
    }

    @Test
    void generateEnvTemplate_preserveDefaults_keepsNonSecrets() {
        String source = "DB_HOST=localhost\nDB_PORT=3306\nDB_PASSWORD=secret";

        Map<String, Object> result = service.generateEnvTemplate(source, true);

        String template = (String) result.get("template");
        assertThat(template).contains("DB_HOST=localhost");
        assertThat(template).contains("DB_PORT=3306");
        assertThat(template).contains("DB_PASSWORD=\n");
    }

    @Test
    void generateEnvTemplate_preservesComments() {
        String source = "# Database config\nDB_HOST=localhost\n\n# API config\nAPI_KEY=secret";

        Map<String, Object> result = service.generateEnvTemplate(source, false);

        String template = (String) result.get("template");
        assertThat(template).contains("# Database config");
        assertThat(template).contains("# API config");
    }

    @Test
    void generateEnvTemplate_withNullContent_returnsError() {
        Map<String, Object> result = service.generateEnvTemplate(null, false);

        assertThat(result.get("error")).isEqualTo("Source content is required");
    }

    @Test
    void generateEnvTemplate_withBlankContent_returnsError() {
        Map<String, Object> result = service.generateEnvTemplate("   ", false);

        assertThat(result.get("error")).isEqualTo("Source content is required");
    }

    // ========== Helper Method Tests ==========

    @Test
    void isSecretKey_detectsPasswordKeys() {
        assertThat(service.isSecretKey("DB_PASSWORD")).isTrue();
        assertThat(service.isSecretKey("password")).isTrue();
        assertThat(service.isSecretKey("MY_SECRET")).isTrue();
        assertThat(service.isSecretKey("API_KEY")).isTrue();
        assertThat(service.isSecretKey("AUTH_TOKEN")).isTrue();
        assertThat(service.isSecretKey("PRIVATE_KEY")).isTrue();
    }

    @Test
    void isSecretKey_doesNotFlagNonSecretKeys() {
        assertThat(service.isSecretKey("DB_HOST")).isFalse();
        assertThat(service.isSecretKey("DB_PORT")).isFalse();
        assertThat(service.isSecretKey("APP_NAME")).isFalse();
    }

    @Test
    void parseEnvContent_handlesWindowsLineEndings() {
        String content = "KEY1=val1\r\nKEY2=val2\r\n";

        Map<String, String> vars = service.parseEnvContent(content);

        assertThat(vars).hasSize(2);
        assertThat(vars.get("KEY1")).isEqualTo("val1");
        assertThat(vars.get("KEY2")).isEqualTo("val2");
    }

    @Test
    void parseEnvContent_withNull_returnsEmptyMap() {
        Map<String, String> vars = service.parseEnvContent(null);

        assertThat(vars).isEmpty();
    }
}
