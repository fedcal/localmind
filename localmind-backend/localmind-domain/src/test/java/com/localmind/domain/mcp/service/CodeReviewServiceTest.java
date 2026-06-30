package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.CodeReviewResult;
import com.localmind.domain.mcp.port.out.CodeReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodeReviewServiceTest {

    @Mock
    private CodeReviewRepository codeReviewRepository;

    private CodeReviewService codeReviewService;

    @BeforeEach
    void setUp() {
        when(codeReviewRepository.save(any(CodeReviewResult.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        codeReviewService = new CodeReviewService(codeReviewRepository);
    }

    // ======================== analyzeDiff ========================

    @Test
    @SuppressWarnings("unchecked")
    void analyzeDiff_emptyDiff_returnsEmptyResult() {
        Map<String, Object> result = codeReviewService.analyzeDiff("");

        assertThat(result.get("totalIssues")).isEqualTo(0);
        Map<String, Object> stats = (Map<String, Object>) result.get("stats");
        assertThat(stats.get("filesChanged")).isEqualTo(0);
        assertThat(stats.get("linesAdded")).isEqualTo(0);
        assertThat(stats.get("linesRemoved")).isEqualTo(0);
        assertThat((List<?>) result.get("files")).isEmpty();
        assertThat((List<?>) result.get("issues")).isEmpty();
        verify(codeReviewRepository).save(any(CodeReviewResult.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void analyzeDiff_nullDiff_returnsEmptyResult() {
        Map<String, Object> result = codeReviewService.analyzeDiff(null);

        assertThat(result.get("totalIssues")).isEqualTo(0);
        Map<String, Object> stats = (Map<String, Object>) result.get("stats");
        assertThat(stats.get("filesChanged")).isEqualTo(0);
    }

    @Test
    @SuppressWarnings("unchecked")
    void analyzeDiff_detectsConsoleLog() {
        String diff = "--- a/app.js\n+++ b/app.js\n+  console.log('debug info');";

        Map<String, Object> result = codeReviewService.analyzeDiff(diff);

        assertThat(result.get("totalIssues")).isEqualTo(1);
        List<Map<String, Object>> issues = (List<Map<String, Object>>) result.get("issues");
        assertThat(issues).hasSize(1);
        assertThat(issues.get(0).get("type")).isEqualTo("console-statement");
        assertThat(issues.get(0).get("severity")).isEqualTo("warning");
    }

    @Test
    @SuppressWarnings("unchecked")
    void analyzeDiff_detectsConsoleError() {
        String diff = "+++ b/app.js\n+  console.error('something went wrong');";

        Map<String, Object> result = codeReviewService.analyzeDiff(diff);

        List<Map<String, Object>> issues = (List<Map<String, Object>>) result.get("issues");
        assertThat(issues).hasSize(1);
        assertThat(issues.get(0).get("type")).isEqualTo("console-statement");
    }

    @Test
    @SuppressWarnings("unchecked")
    void analyzeDiff_detectsTodoComment() {
        String diff = "+++ b/Service.java\n+  // TODO: refactor this method";

        Map<String, Object> result = codeReviewService.analyzeDiff(diff);

        List<Map<String, Object>> issues = (List<Map<String, Object>>) result.get("issues");
        assertThat(issues).hasSize(1);
        assertThat(issues.get(0).get("type")).isEqualTo("todo-comment");
        assertThat(issues.get(0).get("severity")).isEqualTo("info");
        assertThat(issues.get(0).get("message")).asString().contains("TODO");
    }

    @Test
    @SuppressWarnings("unchecked")
    void analyzeDiff_detectsFixmeComment() {
        String diff = "+++ b/Service.java\n+  // FIXME: urgent fix needed";

        Map<String, Object> result = codeReviewService.analyzeDiff(diff);

        List<Map<String, Object>> issues = (List<Map<String, Object>>) result.get("issues");
        assertThat(issues).hasSize(1);
        assertThat(issues.get(0).get("type")).isEqualTo("todo-comment");
        assertThat(issues.get(0).get("message")).asString().contains("FIXME");
    }

    @Test
    @SuppressWarnings("unchecked")
    void analyzeDiff_detectsDebuggerStatement() {
        String diff = "+++ b/app.js\n+  debugger;";

        Map<String, Object> result = codeReviewService.analyzeDiff(diff);

        List<Map<String, Object>> issues = (List<Map<String, Object>>) result.get("issues");
        assertThat(issues).hasSize(1);
        assertThat(issues.get(0).get("type")).isEqualTo("debugger-statement");
        assertThat(issues.get(0).get("severity")).isEqualTo("error");
    }

    @Test
    @SuppressWarnings("unchecked")
    void analyzeDiff_detectsAlertCall() {
        String diff = "+++ b/app.js\n+  alert('warning message');";

        Map<String, Object> result = codeReviewService.analyzeDiff(diff);

        List<Map<String, Object>> issues = (List<Map<String, Object>>) result.get("issues");
        assertThat(issues).hasSize(1);
        assertThat(issues.get(0).get("type")).isEqualTo("alert-call");
        assertThat(issues.get(0).get("severity")).isEqualTo("warning");
    }

    @Test
    @SuppressWarnings("unchecked")
    void analyzeDiff_detectsHardcodedCredentials() {
        String diff = "+++ b/config.js\n+  password: 'my-secret-pass'";

        Map<String, Object> result = codeReviewService.analyzeDiff(diff);

        List<Map<String, Object>> issues = (List<Map<String, Object>>) result.get("issues");
        assertThat(issues).hasSize(1);
        assertThat(issues.get(0).get("type")).isEqualTo("hardcoded-credentials");
        assertThat(issues.get(0).get("severity")).isEqualTo("error");
    }

    @Test
    @SuppressWarnings("unchecked")
    void analyzeDiff_detectsApiKeyCredentials() {
        String diff = "+++ b/config.js\n+  api_key: 'abc123xyz'";

        Map<String, Object> result = codeReviewService.analyzeDiff(diff);

        List<Map<String, Object>> issues = (List<Map<String, Object>>) result.get("issues");
        assertThat(issues.stream()
                .anyMatch(i -> "hardcoded-credentials".equals(i.get("type")))).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void analyzeDiff_detectsEmptyCatch() {
        String diff = "+++ b/Service.java\n+  catch (Exception e) {}";

        Map<String, Object> result = codeReviewService.analyzeDiff(diff);

        List<Map<String, Object>> issues = (List<Map<String, Object>>) result.get("issues");
        assertThat(issues).hasSize(1);
        assertThat(issues.get(0).get("type")).isEqualTo("empty-catch");
        assertThat(issues.get(0).get("severity")).isEqualTo("warning");
    }

    @Test
    @SuppressWarnings("unchecked")
    void analyzeDiff_cleanDiff_noIssues() {
        String diff = "--- a/Service.java\n+++ b/Service.java\n+  return repository.findAll();\n-  return null;";

        Map<String, Object> result = codeReviewService.analyzeDiff(diff);

        assertThat(result.get("totalIssues")).isEqualTo(0);
        List<Map<String, Object>> issues = (List<Map<String, Object>>) result.get("issues");
        assertThat(issues).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void analyzeDiff_countsLinesAndFiles() {
        String diff = "--- a/file1.java\n+++ b/file1.java\n+line1\n+line2\n-removed1\n--- a/file2.java\n+++ b/file2.java\n+line3";

        Map<String, Object> result = codeReviewService.analyzeDiff(diff);

        Map<String, Object> stats = (Map<String, Object>) result.get("stats");
        assertThat(stats.get("filesChanged")).isEqualTo(2);
        assertThat(stats.get("linesAdded")).isEqualTo(3);
        assertThat(stats.get("linesRemoved")).isEqualTo(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void analyzeDiff_multipleIssues_buildsIssuesByType() {
        String diff = "+++ b/app.js\n+  console.log('test');\n+  // TODO fix\n+  debugger;";

        Map<String, Object> result = codeReviewService.analyzeDiff(diff);

        assertThat(result.get("totalIssues")).isEqualTo(3);
        Map<String, Integer> issuesByType = (Map<String, Integer>) result.get("issuesByType");
        assertThat(issuesByType).containsEntry("console-statement", 1);
        assertThat(issuesByType).containsEntry("todo-comment", 1);
        assertThat(issuesByType).containsEntry("debugger-statement", 1);

        Map<String, Integer> issuesBySeverity = (Map<String, Integer>) result.get("issuesBySeverity");
        assertThat(issuesBySeverity).containsEntry("warning", 1);
        assertThat(issuesBySeverity).containsEntry("info", 1);
        assertThat(issuesBySeverity).containsEntry("error", 1);
    }

    @Test
    void analyzeDiff_savesResultToRepository() {
        String diff = "+++ b/app.js\n+  console.log('debug');";

        codeReviewService.analyzeDiff(diff);

        ArgumentCaptor<CodeReviewResult> captor = ArgumentCaptor.forClass(CodeReviewResult.class);
        verify(codeReviewRepository).save(captor.capture());

        CodeReviewResult saved = captor.getValue();
        assertThat(saved.getReviewType()).isEqualTo("analyzeDiff");
        assertThat(saved.getIssuesFound()).isEqualTo(1);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    // ======================== checkComplexity ========================

    @Test
    @SuppressWarnings("unchecked")
    void checkComplexity_emptyCode_returnsBase() {
        Map<String, Object> result = codeReviewService.checkComplexity("", "java");

        assertThat(result.get("totalComplexity")).isEqualTo(1);
        assertThat(result.get("rating")).isEqualTo("low");
        assertThat((List<?>) result.get("breakdown")).isEmpty();
        assertThat(result.get("lineCount")).isEqualTo(0);
        assertThat(result.get("language")).isEqualTo("java");
    }

    @Test
    @SuppressWarnings("unchecked")
    void checkComplexity_simpleCode_lowRating() {
        String code = "public void doSomething() {\n  if (x > 0) {\n    return x;\n  }\n}";

        Map<String, Object> result = codeReviewService.checkComplexity(code, "java");

        // Base 1 + 1 if = 2
        assertThat((int) result.get("totalComplexity")).isLessThanOrEqualTo(5);
        assertThat(result.get("rating")).isEqualTo("low");
        List<Map<String, Object>> breakdown = (List<Map<String, Object>>) result.get("breakdown");
        assertThat(breakdown).isNotEmpty();
        assertThat(breakdown.stream().anyMatch(b -> "if".equals(b.get("pattern")))).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void checkComplexity_moderateCode_moderateRating() {
        // Complexity: base(1) + 3 if + 1 for + 1 while + 1 && = 7
        String code = "public void process() {\n" +
                "  if (a) { doA(); }\n" +
                "  if (b) { doB(); }\n" +
                "  if (c && d) { doC(); }\n" +
                "  for (int i = 0; i < 10; i++) { work(); }\n" +
                "  while (running) { loop(); }\n" +
                "}";

        Map<String, Object> result = codeReviewService.checkComplexity(code, "java");

        int complexity = (int) result.get("totalComplexity");
        assertThat(complexity).isBetween(6, 10);
        assertThat(result.get("rating")).isEqualTo("moderate");
    }

    @Test
    @SuppressWarnings("unchecked")
    void checkComplexity_complexCode_highRating() {
        // Build code with many branches to reach complexity 11-20
        StringBuilder code = new StringBuilder("public void complex() {\n");
        // 8 if statements + 1 for + 1 while + 1 && + 1 || = 12 + base 1 = 13
        for (int i = 0; i < 8; i++) {
            code.append("  if (x" + i + ") { do" + i + "(); }\n");
        }
        code.append("  for (int i = 0; i < n; i++) { work(); }\n");
        code.append("  while (a && b || c) { loop(); }\n");
        code.append("}");

        Map<String, Object> result = codeReviewService.checkComplexity(code.toString(), "java");

        int complexity = (int) result.get("totalComplexity");
        assertThat(complexity).isBetween(11, 20);
        assertThat(result.get("rating")).isEqualTo("high");
    }

    @Test
    @SuppressWarnings("unchecked")
    void checkComplexity_veryComplexCode_veryHighRating() {
        // Build code with many branches to push complexity > 20
        StringBuilder code = new StringBuilder("public void veryComplex() {\n");
        // 15 if + 3 for + 2 while + 2 && = 22 + base 1 = 23
        for (int i = 0; i < 15; i++) {
            code.append("  if (x" + i + ") { do" + i + "(); }\n");
        }
        for (int i = 0; i < 3; i++) {
            code.append("  for (int j = 0; j < n; j++) { work" + i + "(); }\n");
        }
        code.append("  while (a) { loop1(); }\n");
        code.append("  while (b && c) { loop2(); }\n");
        code.append("  if (d && e) { final1(); }\n");
        code.append("}");

        Map<String, Object> result = codeReviewService.checkComplexity(code.toString(), "java");

        int complexity = (int) result.get("totalComplexity");
        assertThat(complexity).isGreaterThan(20);
        assertThat(result.get("rating")).isEqualTo("very high");
    }

    @Test
    @SuppressWarnings("unchecked")
    void checkComplexity_countsCaseStatements() {
        String code = "switch (x) {\n  case 1: break;\n  case 2: break;\n  case 3: break;\n}";

        Map<String, Object> result = codeReviewService.checkComplexity(code, "java");

        List<Map<String, Object>> breakdown = (List<Map<String, Object>>) result.get("breakdown");
        assertThat(breakdown.stream()
                .filter(b -> "case".equals(b.get("pattern")))
                .mapToInt(b -> (int) b.get("count"))
                .sum()).isEqualTo(3);
    }

    @Test
    @SuppressWarnings("unchecked")
    void checkComplexity_countsCatchBlocks() {
        String code = "try {\n  riskyOp();\n} catch (IOException e) {\n  handle1();\n} catch (Exception e) {\n  handle2();\n}";

        Map<String, Object> result = codeReviewService.checkComplexity(code, "java");

        List<Map<String, Object>> breakdown = (List<Map<String, Object>>) result.get("breakdown");
        assertThat(breakdown.stream()
                .filter(b -> "catch/except".equals(b.get("pattern")))
                .mapToInt(b -> (int) b.get("count"))
                .sum()).isEqualTo(2);
    }

    @Test
    void checkComplexity_reportsCorrectLineCount() {
        String code = "line1\nline2\nline3\nline4\nline5";

        Map<String, Object> result = codeReviewService.checkComplexity(code, "python");

        assertThat(result.get("lineCount")).isEqualTo(5);
        assertThat(result.get("language")).isEqualTo("python");
    }

    @Test
    void checkComplexity_nullLanguage_usesUnknown() {
        Map<String, Object> result = codeReviewService.checkComplexity("code", null);

        assertThat(result.get("language")).isEqualTo("unknown");
    }

    @Test
    void checkComplexity_savesResultToRepository() {
        codeReviewService.checkComplexity("if (x) { y(); }", "java");

        ArgumentCaptor<CodeReviewResult> captor = ArgumentCaptor.forClass(CodeReviewResult.class);
        verify(codeReviewRepository).save(captor.capture());

        CodeReviewResult saved = captor.getValue();
        assertThat(saved.getReviewType()).isEqualTo("checkComplexity");
    }

    // ======================== suggestImprovements ========================

    @Test
    @SuppressWarnings("unchecked")
    void suggestImprovements_emptyCode_returnsEmptyResult() {
        Map<String, Object> result = codeReviewService.suggestImprovements("", "java");

        assertThat(result.get("totalSuggestions")).isEqualTo(0);
        assertThat((List<?>) result.get("suggestions")).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void suggestImprovements_detectsMagicNumbers() {
        String code = "int timeout = 3600;\nString name = \"test\";";

        Map<String, Object> result = codeReviewService.suggestImprovements(code, "java");

        List<Map<String, Object>> suggestions = (List<Map<String, Object>>) result.get("suggestions");
        assertThat(suggestions.stream()
                .anyMatch(s -> "magic-number".equals(s.get("type")))).isTrue();
        assertThat(suggestions.stream()
                .filter(s -> "magic-number".equals(s.get("type")))
                .allMatch(s -> "medium".equals(s.get("severity")))).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void suggestImprovements_excludesCommonNumbersFromMagic() {
        // 0, 1, 2, 10, 100, 1000, 24, 60, 1024 should NOT be flagged
        String code = "int a = 0;\nint b = 1;\nint c = 2;\nint d = 10;\nint e = 100;\nint f = 1000;\nint g = 24;\nint h = 60;\nint i = 1024;";

        Map<String, Object> result = codeReviewService.suggestImprovements(code, "java");

        List<Map<String, Object>> suggestions = (List<Map<String, Object>>) result.get("suggestions");
        assertThat(suggestions.stream()
                .noneMatch(s -> "magic-number".equals(s.get("type")))).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void suggestImprovements_skipsMagicNumbersOnConstLines() {
        String code = "final int TIMEOUT = 3600;\nstatic final long MAX_SIZE = 99999;";

        Map<String, Object> result = codeReviewService.suggestImprovements(code, "java");

        List<Map<String, Object>> suggestions = (List<Map<String, Object>>) result.get("suggestions");
        assertThat(suggestions.stream()
                .noneMatch(s -> "magic-number".equals(s.get("type")))).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void suggestImprovements_detectsLongFunctions() {
        StringBuilder code = new StringBuilder("public void longMethod() {\n");
        for (int i = 0; i < 35; i++) {
            code.append("  doSomething" + i + "();\n");
        }
        code.append("}");

        Map<String, Object> result = codeReviewService.suggestImprovements(code.toString(), "java");

        List<Map<String, Object>> suggestions = (List<Map<String, Object>>) result.get("suggestions");
        assertThat(suggestions.stream()
                .anyMatch(s -> "long-function".equals(s.get("type")))).isTrue();
        assertThat(suggestions.stream()
                .filter(s -> "long-function".equals(s.get("type")))
                .allMatch(s -> "high".equals(s.get("severity")))).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void suggestImprovements_noLongFunctionForShortCode() {
        String code = "public void shortMethod() {\n  doSomething();\n  return;\n}";

        Map<String, Object> result = codeReviewService.suggestImprovements(code, "java");

        List<Map<String, Object>> suggestions = (List<Map<String, Object>>) result.get("suggestions");
        assertThat(suggestions.stream()
                .noneMatch(s -> "long-function".equals(s.get("type")))).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void suggestImprovements_detectsDeepNesting() {
        String code = "public void deep() {\n" +
                "  if (a) {\n" +
                "    if (b) {\n" +
                "      if (c) {\n" +
                "        if (d) {\n" +
                "          if (e) {\n" +
                "            doSomething();\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        Map<String, Object> result = codeReviewService.suggestImprovements(code, "java");

        List<Map<String, Object>> suggestions = (List<Map<String, Object>>) result.get("suggestions");
        assertThat(suggestions.stream()
                .anyMatch(s -> "deep-nesting".equals(s.get("type")))).isTrue();
        assertThat(suggestions.stream()
                .filter(s -> "deep-nesting".equals(s.get("type")))
                .allMatch(s -> "high".equals(s.get("severity")))).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void suggestImprovements_detectsDuplicatePatterns() {
        String code = "doSomething(param);\n" +
                "doSomething(param);\n" +
                "doSomething(param);\n" +
                "otherMethod();";

        Map<String, Object> result = codeReviewService.suggestImprovements(code, "java");

        List<Map<String, Object>> suggestions = (List<Map<String, Object>>) result.get("suggestions");
        assertThat(suggestions.stream()
                .anyMatch(s -> "duplicate-code".equals(s.get("type")))).isTrue();
        assertThat(suggestions.stream()
                .filter(s -> "duplicate-code".equals(s.get("type")))
                .allMatch(s -> "medium".equals(s.get("severity")))).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void suggestImprovements_ignoresTrivialDuplicates() {
        // }, return;, break; should not be flagged as duplicates
        String code = "if (a) {\n  return;\n}\nif (b) {\n  return;\n}\nif (c) {\n  return;\n}";

        Map<String, Object> result = codeReviewService.suggestImprovements(code, "java");

        List<Map<String, Object>> suggestions = (List<Map<String, Object>>) result.get("suggestions");
        assertThat(suggestions.stream()
                .noneMatch(s -> "duplicate-code".equals(s.get("type")))).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void suggestImprovements_detectsUnusedVariables() {
        // unusedX is declared but never referenced again; usedY is declared and used in println
        String code = "String unusedX = \"hello\";\nString usedY = \"world\";\nSystem.out.println(usedY);";

        Map<String, Object> result = codeReviewService.suggestImprovements(code, "java");

        List<Map<String, Object>> suggestions = (List<Map<String, Object>>) result.get("suggestions");
        assertThat(suggestions.stream()
                .anyMatch(s -> "unused-variable".equals(s.get("type"))
                        && ((String) s.get("message")).contains("unusedX"))).isTrue();
        // usedY should NOT be flagged (appears twice: declaration + usage)
        assertThat(suggestions.stream()
                .noneMatch(s -> "unused-variable".equals(s.get("type"))
                        && ((String) s.get("message")).contains("usedY"))).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void suggestImprovements_sortsBySeverity() {
        // Build code that triggers high, medium, and low severity suggestions
        StringBuilder code = new StringBuilder();
        // Deep nesting -> high
        code.append("public void deep() {\n");
        code.append("  if (a) {\n    if (b) {\n      if (c) {\n        if (d) {\n          if (e) {\n");
        code.append("            int unused = 42;\n"); // magic-number -> medium, unused -> low
        code.append("          }\n        }\n      }\n    }\n  }\n}\n");

        Map<String, Object> result = codeReviewService.suggestImprovements(code.toString(), "java");

        List<Map<String, Object>> suggestions = (List<Map<String, Object>>) result.get("suggestions");
        if (suggestions.size() >= 2) {
            // Verify high severity comes before medium/low
            int firstHighIdx = -1;
            int lastLowIdx = -1;
            for (int i = 0; i < suggestions.size(); i++) {
                if ("high".equals(suggestions.get(i).get("severity")) && firstHighIdx == -1) {
                    firstHighIdx = i;
                }
                if ("low".equals(suggestions.get(i).get("severity"))) {
                    lastLowIdx = i;
                }
            }
            if (firstHighIdx >= 0 && lastLowIdx >= 0) {
                assertThat(firstHighIdx).isLessThan(lastLowIdx);
            }
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void suggestImprovements_buildsSuggestionsByTypeAndSeverity() {
        String code = "int timeout = 3600;\n" +
                "int delay = 5000;\n" +
                "int unusedX = 99;";

        Map<String, Object> result = codeReviewService.suggestImprovements(code, "java");

        int totalSuggestions = (int) result.get("totalSuggestions");
        assertThat(totalSuggestions).isGreaterThan(0);

        Map<String, Integer> suggestionsByType = (Map<String, Integer>) result.get("suggestionsByType");
        Map<String, Integer> suggestionsBySeverity = (Map<String, Integer>) result.get("suggestionsBySeverity");

        // Sum of suggestionsByType should equal totalSuggestions
        int typeSum = suggestionsByType.values().stream().mapToInt(Integer::intValue).sum();
        assertThat(typeSum).isEqualTo(totalSuggestions);

        // Sum of suggestionsBySeverity should equal totalSuggestions
        int severitySum = suggestionsBySeverity.values().stream().mapToInt(Integer::intValue).sum();
        assertThat(severitySum).isEqualTo(totalSuggestions);
    }

    @Test
    void suggestImprovements_savesResultToRepository() {
        codeReviewService.suggestImprovements("int x = 3600;", "java");

        ArgumentCaptor<CodeReviewResult> captor = ArgumentCaptor.forClass(CodeReviewResult.class);
        verify(codeReviewRepository).save(captor.capture());

        CodeReviewResult saved = captor.getValue();
        assertThat(saved.getReviewType()).isEqualTo("suggestImprovements");
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void suggestImprovements_nullCode_returnsEmptyResult() {
        Map<String, Object> result = codeReviewService.suggestImprovements(null, "java");

        assertThat(result.get("totalSuggestions")).isEqualTo(0);
        assertThat((List<?>) result.get("suggestions")).isEmpty();
    }
}
