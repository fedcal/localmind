package com.localmind.infrastructure.mcp.server;

import com.localmind.domain.mcp.port.in.CodeReviewUseCase;
import com.localmind.domain.mcp.port.in.DependencyAnalysisUseCase;
import com.localmind.domain.mcp.port.in.ProjectScaffoldingUseCase;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "localmind.mcp.server.enabled", havingValue = "true", matchIfMissing = true)
public class LocalMindCodeTools {

    private final CodeReviewUseCase codeReviewUseCase;
    private final DependencyAnalysisUseCase dependencyAnalysisUseCase;
    private final ProjectScaffoldingUseCase projectScaffoldingUseCase;

    public LocalMindCodeTools(CodeReviewUseCase codeReviewUseCase,
                              DependencyAnalysisUseCase dependencyAnalysisUseCase,
                              ProjectScaffoldingUseCase projectScaffoldingUseCase) {
        this.codeReviewUseCase = codeReviewUseCase;
        this.dependencyAnalysisUseCase = dependencyAnalysisUseCase;
        this.projectScaffoldingUseCase = projectScaffoldingUseCase;
    }

    // ==================== CODE REVIEW ====================

    @Tool(description = "Analyze a git diff string for common code issues such as console.log statements, TODO comments, debugger statements, hardcoded credentials, and empty catch blocks.")
    public Map<String, Object> analyzeDiff(
            @ToolParam(description = "The git diff string to analyze") String diff) {
        return codeReviewUseCase.analyzeDiff(diff);
    }

    @Tool(description = "Calculate the cyclomatic complexity of a code snippet. Returns a numeric score with a rating (low/moderate/high/very high) and a breakdown of decision points.")
    public Map<String, Object> checkComplexity(
            @ToolParam(description = "The source code to analyze") String code,
            @ToolParam(description = "Programming language: java, python, javascript, typescript, rust") String language) {
        return codeReviewUseCase.checkComplexity(code, language);
    }

    @Tool(description = "Suggest code improvements including magic numbers, long functions (>30 lines), deep nesting (>4 levels), duplicate patterns, and potentially unused variables.")
    public Map<String, Object> suggestImprovements(
            @ToolParam(description = "The source code to analyze") String code,
            @ToolParam(description = "Programming language: java, python, javascript, typescript") String language) {
        return codeReviewUseCase.suggestImprovements(code, language);
    }

    // ==================== DEPENDENCY MANAGER ====================

    @Tool(description = "Scan project dependencies for known vulnerabilities. Supports Maven (pom.xml) and npm (package.json) projects.")
    public Map<String, Object> checkVulnerabilities(
            @ToolParam(description = "Absolute path to the project root directory") String projectPath,
            @ToolParam(description = "Project type: 'maven' or 'npm'") String projectType) {
        return dependencyAnalysisUseCase.checkVulnerabilities(projectPath, projectType);
    }

    @Tool(description = "Find declared dependencies that are not imported or used in source code. Supports Maven (Java imports vs pom.xml) and npm (import/require vs package.json).")
    public Map<String, Object> findUnusedDependencies(
            @ToolParam(description = "Absolute path to the project root directory") String projectPath,
            @ToolParam(description = "Project type: 'maven' or 'npm'") String projectType) {
        return dependencyAnalysisUseCase.findUnusedDependencies(projectPath, projectType);
    }

    @Tool(description = "Audit project dependency licenses, flagging copyleft licenses (GPL, AGPL, LGPL, MPL-2.0, etc.) that may restrict commercial use.")
    public Map<String, Object> licenseAudit(
            @ToolParam(description = "Absolute path to the project root directory") String projectPath,
            @ToolParam(description = "Project type: 'maven' or 'npm'") String projectType) {
        return dependencyAnalysisUseCase.licenseAudit(projectPath, projectType);
    }

    // ==================== PROJECT SCAFFOLDING ====================

    @Tool(description = "List all available project templates for scaffolding. Includes spring-boot-api, angular-app, maven-multi-module, mcp-server, and react-app.")
    public List<Map<String, Object>> listProjectTemplates() {
        return projectScaffoldingUseCase.listProjectTemplates();
    }

    @Tool(description = "Generate a complete project structure from a template with placeholder substitution. Returns generated file contents without writing to disk.")
    public Map<String, Object> scaffoldProject(
            @ToolParam(description = "Template name: spring-boot-api, angular-app, maven-multi-module, mcp-server, react-app") String template,
            @ToolParam(description = "Name of the project") String projectName,
            @ToolParam(description = "Output directory path") String outputDir,
            @ToolParam(description = "Author name (optional)") String author,
            @ToolParam(description = "Project description (optional, defaults to template-based)") String description,
            @ToolParam(description = "License identifier (optional, defaults to MIT)") String license) {
        return projectScaffoldingUseCase.scaffoldProject(template, projectName, outputDir, author, description, license);
    }

    @Tool(description = "Generate a single component, service, controller, or model file. Returns generated code without writing to disk.")
    public Map<String, Object> scaffoldComponent(
            @ToolParam(description = "Name of the component (PascalCase recommended)") String name,
            @ToolParam(description = "Type: component, service, controller, model") String type,
            @ToolParam(description = "Language: java, typescript, javascript") String language,
            @ToolParam(description = "Output directory path") String outputDir) {
        return projectScaffoldingUseCase.scaffoldComponent(name, type, language, outputDir);
    }
}
