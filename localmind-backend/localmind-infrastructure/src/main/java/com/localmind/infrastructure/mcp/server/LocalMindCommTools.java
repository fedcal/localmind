package com.localmind.infrastructure.mcp.server;

import com.localmind.domain.mcp.port.in.EnvironmentManagerUseCase;
import com.localmind.domain.mcp.port.in.StandupUseCase;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnProperty(name = "localmind.mcp.server.enabled", havingValue = "true", matchIfMissing = true)
public class LocalMindCommTools {

    private final StandupUseCase standupUseCase;
    private final EnvironmentManagerUseCase environmentManagerUseCase;

    public LocalMindCommTools(StandupUseCase standupUseCase,
                               EnvironmentManagerUseCase environmentManagerUseCase) {
        this.standupUseCase = standupUseCase;
        this.environmentManagerUseCase = environmentManagerUseCase;
    }

    // ==================== STANDUP NOTES ====================

    @Tool(description = "Log a daily standup entry with yesterday's work, today's plan and blockers. "
            + "Returns the saved standup with id, yesterday, today, blockers, date.")
    public Map<String, Object> logStandup(
            @ToolParam(description = "What was done yesterday") String yesterday,
            @ToolParam(description = "What is planned for today") String today,
            @ToolParam(description = "Any blockers (optional, can be empty)") String blockers) {
        return standupUseCase.logStandup(yesterday, today, blockers);
    }

    @Tool(description = "Get standup history for the last N days. "
            + "Returns a list of standup entries with date, yesterday, today, blockers.")
    public Map<String, Object> getStandupHistory(
            @ToolParam(description = "Number of days to look back (default 7)") int days) {
        return standupUseCase.getHistory(days > 0 ? days : 7);
    }

    @Tool(description = "Generate a status report aggregating standups over a period. "
            + "Returns period, totalStandups, blockerCount, themes and summary.")
    public Map<String, Object> generateStatusReport(
            @ToolParam(description = "Number of days to aggregate (default 7)") int days) {
        return standupUseCase.generateStatusReport(days > 0 ? days : 7);
    }

    // ==================== ENVIRONMENT MANAGER ====================

    @Tool(description = "List environment files in a directory. "
            + "Returns directory, files list and count.")
    public Map<String, Object> listEnvironments(
            @ToolParam(description = "Directory to scan for .env files") String directory,
            @ToolParam(description = "Whether to scan subdirectories recursively") boolean recursive) {
        return environmentManagerUseCase.listEnvironments(directory, recursive);
    }

    @Tool(description = "Parse and display environment variables from .env file content. "
            + "Masks sensitive values (PASSWORD, SECRET, KEY, TOKEN) by default.")
    public Map<String, Object> getEnvVars(
            @ToolParam(description = "File path label for display") String filePath,
            @ToolParam(description = "Raw .env file content to parse") String fileContent,
            @ToolParam(description = "Whether to show secret values unmasked") boolean showSecrets,
            @ToolParam(description = "Optional key filter substring (empty for all)") String filter) {
        return environmentManagerUseCase.getEnvVars(filePath, fileContent, showSecrets, filter);
    }

    @Tool(description = "Compare two sets of environment variables from .env file content. "
            + "Returns variables only in A, only in B, different values, and common variables.")
    public Map<String, Object> compareEnvironments(
            @ToolParam(description = "Label for first file") String filePathA,
            @ToolParam(description = "Raw content of first .env file") String contentA,
            @ToolParam(description = "Label for second file") String filePathB,
            @ToolParam(description = "Raw content of second .env file") String contentB,
            @ToolParam(description = "Whether to include values in the diff output") boolean showValues) {
        return environmentManagerUseCase.compareEnvironments(filePathA, contentA, filePathB, contentB, showValues);
    }

    @Tool(description = "Validate an .env file content against a template. "
            + "Returns valid status, missing variables, extra variables, empty values and errors.")
    public Map<String, Object> validateEnv(
            @ToolParam(description = "The actual .env file content to validate") String envContent,
            @ToolParam(description = "The template .env content to validate against") String templateContent,
            @ToolParam(description = "Whether to flag extra variables as errors") boolean strict) {
        return environmentManagerUseCase.validateEnv(envContent, templateContent, strict);
    }

    @Tool(description = "Generate an .env template from source content, stripping secret values. "
            + "Returns the template string, variable count and secrets masked count.")
    public Map<String, Object> generateEnvTemplate(
            @ToolParam(description = "Source .env file content") String sourceContent,
            @ToolParam(description = "Whether to preserve non-secret default values") boolean preserveDefaults) {
        return environmentManagerUseCase.generateEnvTemplate(sourceContent, preserveDefaults);
    }
}
