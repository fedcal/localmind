package com.localmind.domain.mcp.port.in;

import java.util.Map;

/**
 * Input port for the environment-manager tool group.
 * Provides environment file listing, variable inspection, comparison,
 * validation and template generation.
 */
public interface EnvironmentManagerUseCase {

    /**
     * Lists environment files in a directory.
     * Simulated result — the domain layer does not perform I/O.
     *
     * @param directory the directory to scan
     * @param recursive whether to scan recursively
     * @return result with directory, files list and count
     */
    Map<String, Object> listEnvironments(String directory, boolean recursive);

    /**
     * Parses environment variables from file content.
     * Masks sensitive values (PASSWORD, SECRET, KEY, TOKEN, API_KEY).
     *
     * @param filePath    label for the file being parsed
     * @param fileContent the raw .env file content to parse
     * @param showSecrets whether to show secret values unmasked
     * @param filter      optional key filter substring (nullable)
     * @return result with file, variables list and count
     */
    Map<String, Object> getEnvVars(String filePath, String fileContent, boolean showSecrets, String filter);

    /**
     * Compares two sets of environment variables provided as raw file content.
     *
     * @param filePathA    label for file A
     * @param contentA     raw content of file A
     * @param filePathB    label for file B
     * @param contentB     raw content of file B
     * @param showValues   whether to include values in the diff
     * @return result with fileA, fileB, onlyInA, onlyInB, different, common
     */
    Map<String, Object> compareEnvironments(String filePathA, String contentA,
                                             String filePathB, String contentB,
                                             boolean showValues);

    /**
     * Validates an env file content against a template content.
     *
     * @param envContent      the actual env file content
     * @param templateContent the template env content
     * @param strict          whether to flag extra variables as errors
     * @return result with valid, missing, extra, empty, errors
     */
    Map<String, Object> validateEnv(String envContent, String templateContent, boolean strict);

    /**
     * Generates an env template from source content, stripping secret values.
     *
     * @param sourceContent    the source env file content
     * @param preserveDefaults whether to preserve non-secret default values
     * @return result with template string, variableCount, secretsMasked
     */
    Map<String, Object> generateEnvTemplate(String sourceContent, boolean preserveDefaults);
}
