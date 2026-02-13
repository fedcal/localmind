package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.CodebaseKnowledge;
import com.localmind.domain.mcp.port.out.CodebaseKnowledgeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CodebaseKnowledgeServiceTest {

    @Mock
    private CodebaseKnowledgeRepository repository;

    private CodebaseKnowledgeService service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        service = new CodebaseKnowledgeService(repository);
        when(repository.save(any(CodebaseKnowledge.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.findByTargetPath(any())).thenReturn(new ArrayList<>());
    }

    // ========== searchCode Tests ==========

    @Test
    void searchCode_withNullDirectory_returnsError() {
        Map<String, Object> result = service.searchCode(null, "test", null, 20);
        assertThat(result.get("error")).isEqualTo("directory is empty or null");
    }

    @Test
    void searchCode_withNullPattern_returnsError() {
        Map<String, Object> result = service.searchCode(tempDir.toString(), null, null, 20);
        assertThat(result.get("error")).isEqualTo("pattern is empty or null");
    }

    @Test
    void searchCode_withNonExistentDir_returnsError() {
        Map<String, Object> result = service.searchCode("/nonexistent", "test", null, 20);
        assertThat(result.get("error")).asString().contains("does not exist");
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchCode_findsMatchingLines() throws Exception {
        Files.writeString(tempDir.resolve("test.java"), "public class Test {\n    String hello = \"world\";\n}");

        Map<String, Object> result = service.searchCode(tempDir.toString(), "hello", null, 20);

        assertThat((int) result.get("totalMatches")).isEqualTo(1);
        List<Map<String, Object>> matches = (List<Map<String, Object>>) result.get("matches");
        assertThat(matches.get(0).get("line")).isEqualTo(2);
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchCode_withExtensionFilter_onlyMatchesFilteredFiles() throws Exception {
        Files.writeString(tempDir.resolve("test.java"), "hello world");
        Files.writeString(tempDir.resolve("test.txt"), "hello world");

        Map<String, Object> result = service.searchCode(tempDir.toString(), "hello", List.of(".java"), 20);

        List<Map<String, Object>> matches = (List<Map<String, Object>>) result.get("matches");
        assertThat(matches).hasSize(1);
        assertThat(matches.get(0).get("relativePath").toString()).endsWith(".java");
    }

    @Test
    void searchCode_respectsMaxResults() throws Exception {
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            content.append("line with pattern\n");
        }
        Files.writeString(tempDir.resolve("big.txt"), content.toString());

        Map<String, Object> result = service.searchCode(tempDir.toString(), "pattern", null, 5);

        assertThat((int) result.get("totalMatches")).isEqualTo(5);
    }

    @Test
    void searchCode_savesToRepository() throws Exception {
        Files.writeString(tempDir.resolve("test.txt"), "content");

        service.searchCode(tempDir.toString(), "content", null, 20);

        ArgumentCaptor<CodebaseKnowledge> captor = ArgumentCaptor.forClass(CodebaseKnowledge.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getAnalysisType()).isEqualTo("search-code");
    }

    // ========== explainModule Tests ==========

    @Test
    void explainModule_withNullPath_returnsError() {
        Map<String, Object> result = service.explainModule(null);
        assertThat(result.get("error")).isEqualTo("filePath is empty or null");
    }

    @Test
    void explainModule_withNonExistentFile_returnsError() {
        Map<String, Object> result = service.explainModule("/nonexistent/file.ts");
        assertThat(result.get("error")).asString().contains("does not exist");
    }

    @Test
    @SuppressWarnings("unchecked")
    void explainModule_extractsImportsAndExports() throws Exception {
        String content = """
                import { Component } from '@angular/core';
                import * as fs from 'fs';
                export function myFunc() {}
                export class MyClass {}
                export interface MyInterface {}
                export type MyType = string;
                """;
        Path file = tempDir.resolve("module.ts");
        Files.writeString(file, content);

        Map<String, Object> result = service.explainModule(file.toString());

        assertThat((List<String>) result.get("imports")).hasSize(2);
        assertThat((List<String>) result.get("exports")).hasSize(4);
        assertThat((List<String>) result.get("functions")).contains("myFunc");
        assertThat((List<String>) result.get("classes")).contains("MyClass");
        assertThat((List<String>) result.get("interfaces")).contains("MyInterface");
        assertThat((List<String>) result.get("typeAliases")).contains("MyType");
    }

    @Test
    void explainModule_returnsFileMetadata() throws Exception {
        Path file = tempDir.resolve("test.java");
        Files.writeString(file, "public class Test {\n}\n");

        Map<String, Object> result = service.explainModule(file.toString());

        assertThat(result.get("fileName")).isEqualTo("test.java");
        assertThat(result.get("extension")).isEqualTo(".java");
        assertThat((int) result.get("lineCount")).isEqualTo(2);
    }

    // ========== architectureMap Tests ==========

    @Test
    void architectureMap_withNullDir_returnsError() {
        Map<String, Object> result = service.architectureMap(null, 3);
        assertThat(result.get("error")).isEqualTo("directory is empty or null");
    }

    @Test
    void architectureMap_withNonExistentDir_returnsError() {
        Map<String, Object> result = service.architectureMap("/nonexistent", 3);
        assertThat(result.get("error")).asString().contains("does not exist");
    }

    @Test
    @SuppressWarnings("unchecked")
    void architectureMap_generatesTreeWithFiles() throws Exception {
        Path subDir = tempDir.resolve("src");
        Files.createDirectories(subDir);
        Files.writeString(subDir.resolve("App.java"), "class App {}");
        Files.writeString(subDir.resolve("Config.java"), "class Config {}");
        Files.writeString(tempDir.resolve("README.md"), "# Readme");

        Map<String, Object> result = service.architectureMap(tempDir.toString(), 3);

        assertThat((String) result.get("tree")).contains("src/");
        assertThat((int) result.get("totalFiles")).isGreaterThanOrEqualTo(3);
        Map<String, Integer> extCounts = (Map<String, Integer>) result.get("extensionCounts");
        assertThat(extCounts).containsKey(".java");
    }

    @Test
    void architectureMap_defaultDepth() throws Exception {
        Map<String, Object> result = service.architectureMap(tempDir.toString(), 0);
        // Should default to 3
        assertThat(result).containsKey("tree");
    }

    // ========== dependencyGraph Tests ==========

    @Test
    void dependencyGraph_withNullDir_returnsError() {
        Map<String, Object> result = service.dependencyGraph(null);
        assertThat(result.get("error")).isEqualTo("directory is empty or null");
    }

    @Test
    void dependencyGraph_withNonExistentDir_returnsError() {
        Map<String, Object> result = service.dependencyGraph("/nonexistent");
        assertThat(result.get("error")).asString().contains("does not exist");
    }

    @Test
    @SuppressWarnings("unchecked")
    void dependencyGraph_detectsImports() throws Exception {
        Path srcDir = tempDir.resolve("src");
        Files.createDirectories(srcDir);
        Files.writeString(srcDir.resolve("index.ts"), "import { foo } from './utils';\nimport bar from './helper';");
        Files.writeString(srcDir.resolve("utils.ts"), "export function foo() {}");
        Files.writeString(srcDir.resolve("helper.ts"), "export default function bar() {}");

        Map<String, Object> result = service.dependencyGraph(tempDir.toString());

        assertThat((int) result.get("totalFiles")).isGreaterThanOrEqualTo(3);
        assertThat((int) result.get("totalDependencyEdges")).isGreaterThanOrEqualTo(2);
        assertThat((String) result.get("mermaidDiagram")).contains("graph TD");
    }

    @Test
    void dependencyGraph_withEmptyDir_returnsZeroEdges() throws Exception {
        Map<String, Object> result = service.dependencyGraph(tempDir.toString());

        assertThat((int) result.get("totalDependencyEdges")).isEqualTo(0);
    }

    // ========== trackChanges Tests ==========

    @Test
    void trackChanges_withNullModule_returnsError() {
        Map<String, Object> result = service.trackChanges(null, "feature", "desc", 1, "author", null, 20);
        assertThat(result.get("error")).isEqualTo("modulePath is empty or null");
    }

    @Test
    @SuppressWarnings("unchecked")
    void trackChanges_recordsChange() {
        Map<String, Object> result = service.trackChanges("src/auth", "feature", "Added login",
                3, "dev", "abc123", 20);

        assertThat(result.get("action")).isEqualTo("recorded");
        Map<String, Object> change = (Map<String, Object>) result.get("change");
        assertThat(change.get("modulePath")).isEqualTo("src/auth");
        assertThat(change.get("changeType")).isEqualTo("feature");
        assertThat(change.get("description")).isEqualTo("Added login");
        assertThat(change.get("author")).isEqualTo("dev");
    }

    @Test
    void trackChanges_returnsHistoryWhenNoChangeType() {
        Map<String, Object> result = service.trackChanges("src/auth", null, null, 0, null, null, 20);

        assertThat(result.get("action")).isEqualTo("history");
        assertThat(result.get("modulePath")).isEqualTo("src/auth");
    }

    @Test
    void trackChanges_recordSavesToRepository() {
        service.trackChanges("src/core", "bugfix", "Fixed crash", 1, "dev", "def456", 20);

        ArgumentCaptor<CodebaseKnowledge> captor = ArgumentCaptor.forClass(CodebaseKnowledge.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getAnalysisType()).isEqualTo("track-changes");
        assertThat(captor.getValue().getTargetPath()).isEqualTo("src/core");
    }
}
