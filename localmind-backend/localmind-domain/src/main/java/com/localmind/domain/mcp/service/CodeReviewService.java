package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.CodeReviewResult;
import com.localmind.domain.mcp.port.in.CodeReviewUseCase;
import com.localmind.domain.mcp.port.out.CodeReviewRepository;

import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Domain service implementing the code-review tool group.
 * Pure Java, zero Spring dependencies.
 */
public class CodeReviewService implements CodeReviewUseCase {

    private final CodeReviewRepository codeReviewRepository;

    public CodeReviewService(CodeReviewRepository codeReviewRepository) {
        this.codeReviewRepository = codeReviewRepository;
    }

    // ======================== analyzeDiff ========================

    @Override
    public Map<String, Object> analyzeDiff(String diff) {
        if (diff == null || diff.isBlank()) {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("stats", Map.of("filesChanged", 0, "linesAdded", 0, "linesRemoved", 0));
            empty.put("files", List.of());
            empty.put("totalIssues", 0);
            empty.put("issuesByType", Map.of());
            empty.put("issuesBySeverity", Map.of());
            empty.put("issues", List.of());
            saveResult("analyzeDiff", 0, empty);
            return empty;
        }

        List<String> lines = Arrays.asList(diff.split("\n"));
        List<String> files = new ArrayList<>();
        int linesAdded = 0;
        int linesRemoved = 0;
        List<Map<String, Object>> issues = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            // Extract modified files from diff headers
            if (line.startsWith("+++ ") && !line.equals("+++ /dev/null")) {
                String file = line.substring(4);
                if (file.startsWith("b/")) {
                    file = file.substring(2);
                }
                if (!files.contains(file)) {
                    files.add(file);
                }
            }
            if (line.startsWith("--- ") && !line.equals("--- /dev/null")) {
                String file = line.substring(4);
                if (file.startsWith("a/")) {
                    file = file.substring(2);
                }
                if (!files.contains(file)) {
                    files.add(file);
                }
            }

            // Count added/removed lines
            if (line.startsWith("+") && !line.startsWith("+++")) {
                linesAdded++;
                String addedLine = line.substring(1).trim();

                // Detect issues on ADDED lines only
                detectDiffIssues(addedLine, i + 1, issues);
            } else if (line.startsWith("-") && !line.startsWith("---")) {
                linesRemoved++;
            }
        }

        // Build issuesByType and issuesBySeverity
        Map<String, Integer> issuesByType = new LinkedHashMap<>();
        Map<String, Integer> issuesBySeverity = new LinkedHashMap<>();
        for (Map<String, Object> issue : issues) {
            String type = (String) issue.get("type");
            String severity = (String) issue.get("severity");
            issuesByType.merge(type, 1, Integer::sum);
            issuesBySeverity.merge(severity, 1, Integer::sum);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("filesChanged", files.size());
        stats.put("linesAdded", linesAdded);
        stats.put("linesRemoved", linesRemoved);
        result.put("stats", stats);
        result.put("files", files);
        result.put("totalIssues", issues.size());
        result.put("issuesByType", issuesByType);
        result.put("issuesBySeverity", issuesBySeverity);
        result.put("issues", issues);

        saveResult("analyzeDiff", issues.size(), result);
        return result;
    }

    private void detectDiffIssues(String line, int lineNumber, List<Map<String, Object>> issues) {
        // console.log/debug/info/warn/error/trace/dir -> severity: warning
        Pattern consolePattern = Pattern.compile("console\\.(log|debug|info|warn|error|trace|dir)\\s*\\(");
        if (consolePattern.matcher(line).find()) {
            issues.add(createIssue("console-statement", "warning", lineNumber,
                    "Console statement found in added code", line));
        }

        // TODO/FIXME/HACK/XXX/TEMP -> severity: info
        Pattern todoPattern = Pattern.compile("\\b(TODO|FIXME|HACK|XXX|TEMP)\\b");
        Matcher todoMatcher = todoPattern.matcher(line);
        if (todoMatcher.find()) {
            issues.add(createIssue("todo-comment", "info", lineNumber,
                    todoMatcher.group(1) + " comment found", line));
        }

        // debugger statement -> severity: error
        Pattern debuggerPattern = Pattern.compile("\\bdebugger\\b");
        if (debuggerPattern.matcher(line).find()) {
            issues.add(createIssue("debugger-statement", "error", lineNumber,
                    "Debugger statement found in added code", line));
        }

        // alert() -> severity: warning
        Pattern alertPattern = Pattern.compile("\\balert\\s*\\(");
        if (alertPattern.matcher(line).find()) {
            issues.add(createIssue("alert-call", "warning", lineNumber,
                    "Alert call found in added code", line));
        }

        // Hardcoded credentials -> severity: error
        Pattern credentialsPattern = Pattern.compile(
                "(password\\s*[:=]|secret\\s*[=]|api_key\\s*[:=]|apiKey\\s*[=]|token\\s*[=])",
                Pattern.CASE_INSENSITIVE);
        if (credentialsPattern.matcher(line).find()) {
            issues.add(createIssue("hardcoded-credentials", "error", lineNumber,
                    "Potential hardcoded credentials found", line));
        }

        // Empty catch blocks -> severity: warning
        Pattern emptyCatchPattern = Pattern.compile("catch\\s*\\([^)]*\\)\\s*\\{\\s*\\}");
        if (emptyCatchPattern.matcher(line).find()) {
            issues.add(createIssue("empty-catch", "warning", lineNumber,
                    "Empty catch block found", line));
        }
    }

    private Map<String, Object> createIssue(String type, String severity, int line,
                                             String message, String content) {
        Map<String, Object> issue = new LinkedHashMap<>();
        issue.put("type", type);
        issue.put("severity", severity);
        issue.put("line", line);
        issue.put("message", message);
        issue.put("content", content);
        return issue;
    }

    // ======================== checkComplexity ========================

    @Override
    public Map<String, Object> checkComplexity(String code, String language) {
        if (code == null || code.isBlank()) {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("totalComplexity", 1);
            empty.put("rating", "low");
            empty.put("breakdown", List.of());
            empty.put("lineCount", 0);
            empty.put("language", language != null ? language : "unknown");
            saveResult("checkComplexity", 0, empty);
            return empty;
        }

        String cleaned = removeCommentsAndStrings(code, language);
        int complexity = 1;
        List<Map<String, Object>> breakdown = new ArrayList<>();

        // Define complexity patterns
        List<ComplexityPattern> patterns = new ArrayList<>();
        patterns.add(new ComplexityPattern("\\bif\\b", "if", "Conditional branch"));
        patterns.add(new ComplexityPattern("\\belse\\s+if\\b|\\belif\\b", "else if/elif", "Additional conditional branch"));
        patterns.add(new ComplexityPattern("\\bfor\\b", "for", "Loop construct"));
        patterns.add(new ComplexityPattern("\\bwhile\\b", "while", "Loop construct"));
        patterns.add(new ComplexityPattern("\\bcase\\b", "case", "Switch case branch"));
        patterns.add(new ComplexityPattern("\\bcatch\\b|\\bexcept\\b", "catch/except", "Exception handler"));
        patterns.add(new ComplexityPattern("&&|\\band\\b", "&&/and", "Logical AND operator"));
        patterns.add(new ComplexityPattern("\\|\\||\\bor\\b", "||/or", "Logical OR operator"));
        patterns.add(new ComplexityPattern("[^?]\\?[^?:.]", "?:", "Ternary operator"));

        for (ComplexityPattern cp : patterns) {
            int count = countPatternOccurrences(cleaned, cp.regex);
            if (count > 0) {
                complexity += count;
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("pattern", cp.name);
                entry.put("count", count);
                entry.put("description", cp.description);
                breakdown.add(entry);
            }
        }

        String rating;
        if (complexity <= 5) {
            rating = "low";
        } else if (complexity <= 10) {
            rating = "moderate";
        } else if (complexity <= 20) {
            rating = "high";
        } else {
            rating = "very high";
        }

        int lineCount = code.split("\n").length;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalComplexity", complexity);
        result.put("rating", rating);
        result.put("breakdown", breakdown);
        result.put("lineCount", lineCount);
        result.put("language", language != null ? language : "unknown");

        saveResult("checkComplexity", complexity > 5 ? complexity : 0, result);
        return result;
    }

    private String removeCommentsAndStrings(String code, String language) {
        // Remove single-line strings (double and single quotes)
        String cleaned = code.replaceAll("\"[^\"\\\\]*(\\\\.[^\"\\\\]*)*\"", "\"\"");
        cleaned = cleaned.replaceAll("'[^'\\\\]*(\\\\.[^'\\\\]*)*'", "''");
        // Remove single-line comments
        cleaned = cleaned.replaceAll("//.*", "");
        cleaned = cleaned.replaceAll("#.*", "");
        // Remove multi-line comments
        cleaned = cleaned.replaceAll("/\\*[\\s\\S]*?\\*/", "");
        return cleaned;
    }

    private int countPatternOccurrences(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    // ======================== suggestImprovements ========================

    @Override
    public Map<String, Object> suggestImprovements(String code, String language) {
        if (code == null || code.isBlank()) {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("totalSuggestions", 0);
            empty.put("suggestionsByType", Map.of());
            empty.put("suggestionsBySeverity", Map.of());
            empty.put("suggestions", List.of());
            saveResult("suggestImprovements", 0, empty);
            return empty;
        }

        String[] lines = code.split("\n");
        List<Map<String, Object>> suggestions = new ArrayList<>();

        detectMagicNumbers(lines, suggestions);
        detectLongFunctions(lines, suggestions);
        detectDeepNesting(lines, suggestions);
        detectDuplicatePatterns(lines, suggestions);
        detectUnusedVariables(lines, code, suggestions);

        // Sort by severity: high > medium > low
        suggestions.sort((a, b) -> {
            int sa = severityOrder((String) a.get("severity"));
            int sb = severityOrder((String) b.get("severity"));
            return Integer.compare(sa, sb);
        });

        // Build summaries
        Map<String, Integer> suggestionsByType = new LinkedHashMap<>();
        Map<String, Integer> suggestionsBySeverity = new LinkedHashMap<>();
        for (Map<String, Object> s : suggestions) {
            String type = (String) s.get("type");
            String severity = (String) s.get("severity");
            suggestionsByType.merge(type, 1, Integer::sum);
            suggestionsBySeverity.merge(severity, 1, Integer::sum);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalSuggestions", suggestions.size());
        result.put("suggestionsByType", suggestionsByType);
        result.put("suggestionsBySeverity", suggestionsBySeverity);
        result.put("suggestions", suggestions);

        saveResult("suggestImprovements", suggestions.size(), result);
        return result;
    }

    private void detectMagicNumbers(String[] lines, List<Map<String, Object>> suggestions) {
        Set<String> excludedNumbers = Set.of("0", "1", "2", "10", "100", "1000", "24", "60", "1024");
        Pattern magicNumberPattern = Pattern.compile("(?<![\\w.])(\\d{2,}|\\d+\\.\\d+)(?![\\w.])");
        Pattern constLinePattern = Pattern.compile("\\b(const|final|static|FINAL|STATIC|CONST)\\b", Pattern.CASE_INSENSITIVE);

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            // Skip const/final/static declarations
            if (constLinePattern.matcher(line).find()) {
                continue;
            }
            Matcher matcher = magicNumberPattern.matcher(line);
            while (matcher.find()) {
                String number = matcher.group(1);
                if (!excludedNumbers.contains(number)) {
                    suggestions.add(createSuggestion("magic-number", "medium",
                            "Magic number " + number + " should be extracted to a named constant",
                            i + 1, "Value: " + number));
                    break; // One suggestion per line
                }
            }
        }
    }

    private void detectLongFunctions(String[] lines, List<Map<String, Object>> suggestions) {
        Pattern functionStartPattern = Pattern.compile(
                "(\\b(public|private|protected|static|async|def|function)\\b.*\\{\\s*$)|" +
                "(\\b(public|private|protected|static|async|def|function)\\b.*\\(.*\\)\\s*\\{\\s*$)|" +
                "(\\bfunction\\s+\\w+\\s*\\()|" +
                "(\\b(def)\\s+\\w+\\s*\\()");

        int functionStartLine = -1;
        int braceDepth = 0;
        boolean inFunction = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (!inFunction && functionStartPattern.matcher(line).find()) {
                functionStartLine = i;
                inFunction = true;
                braceDepth = 0;
            }

            if (inFunction) {
                for (char c : line.toCharArray()) {
                    if (c == '{') braceDepth++;
                    if (c == '}') braceDepth--;
                }

                if (braceDepth <= 0 && functionStartLine != i) {
                    int functionLength = i - functionStartLine + 1;
                    if (functionLength > 30) {
                        suggestions.add(createSuggestion("long-function", "high",
                                "Function starting at line " + (functionStartLine + 1) + " is " + functionLength + " lines long (>30). Consider breaking it into smaller functions",
                                functionStartLine + 1, "Length: " + functionLength + " lines"));
                    }
                    inFunction = false;
                }
            }
        }
    }

    private void detectDeepNesting(String[] lines, List<Map<String, Object>> suggestions) {
        int maxNesting = 0;
        int currentNesting = 0;
        int deepestLine = 0;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            for (char c : line.toCharArray()) {
                if (c == '{') {
                    currentNesting++;
                    if (currentNesting > maxNesting) {
                        maxNesting = currentNesting;
                        deepestLine = i;
                    }
                }
                if (c == '}') {
                    currentNesting--;
                    if (currentNesting < 0) currentNesting = 0;
                }
            }

            if (currentNesting > 4) {
                suggestions.add(createSuggestion("deep-nesting", "high",
                        "Code has nesting depth of " + currentNesting + " levels (>4) at line " + (i + 1) + ". Consider extracting nested logic into separate methods",
                        i + 1, "Nesting depth: " + currentNesting));
            }
        }
    }

    private void detectDuplicatePatterns(String[] lines, List<Map<String, Object>> suggestions) {
        Set<String> trivialLines = new HashSet<>(Arrays.asList(
                "}", "return;", "break;", "continue;", "{", "});",
                "return", "break", "continue", "else {", "} else {"));
        Map<String, List<Integer>> lineOccurrences = new LinkedHashMap<>();

        for (int i = 0; i < lines.length; i++) {
            String trimmed = lines[i].trim();
            if (trimmed.isEmpty() || trivialLines.contains(trimmed) || trimmed.length() < 5) {
                continue;
            }
            lineOccurrences.computeIfAbsent(trimmed, k -> new ArrayList<>()).add(i + 1);
        }

        Set<String> alreadyReported = new HashSet<>();
        for (Map.Entry<String, List<Integer>> entry : lineOccurrences.entrySet()) {
            if (entry.getValue().size() >= 3 && !alreadyReported.contains(entry.getKey())) {
                alreadyReported.add(entry.getKey());
                suggestions.add(createSuggestion("duplicate-code", "medium",
                        "Line appears " + entry.getValue().size() + " times. Consider extracting to a reusable function or variable",
                        entry.getValue().get(0),
                        "Occurrences at lines: " + entry.getValue()));
            }
        }
    }

    private void detectUnusedVariables(String[] lines, String fullCode, List<Map<String, Object>> suggestions) {
        Pattern varDeclPattern = Pattern.compile(
                "\\b(const|let|var|int|String|long|double|float|boolean|char)\\s+(\\w+)\\s*[=;]");

        for (int i = 0; i < lines.length; i++) {
            Matcher matcher = varDeclPattern.matcher(lines[i]);
            while (matcher.find()) {
                String varName = matcher.group(2);
                // Count occurrences of the variable name in the full code (as a whole word)
                Pattern varUsage = Pattern.compile("\\b" + Pattern.quote(varName) + "\\b");
                Matcher usageMatcher = varUsage.matcher(fullCode);
                int count = 0;
                while (usageMatcher.find()) {
                    count++;
                }
                // If it only appears once (the declaration), it's unused
                if (count <= 1) {
                    suggestions.add(createSuggestion("unused-variable", "low",
                            "Variable '" + varName + "' is declared but never used",
                            i + 1, "Variable: " + varName));
                }
            }
        }
    }

    private Map<String, Object> createSuggestion(String type, String severity, String message,
                                                   int line, String details) {
        Map<String, Object> suggestion = new LinkedHashMap<>();
        suggestion.put("type", type);
        suggestion.put("severity", severity);
        suggestion.put("message", message);
        suggestion.put("line", line);
        suggestion.put("details", details);
        return suggestion;
    }

    private int severityOrder(String severity) {
        switch (severity) {
            case "high": return 0;
            case "medium": return 1;
            case "low": return 2;
            default: return 3;
        }
    }

    // ======================== Persistence ========================

    private void saveResult(String reviewType, int issuesFound, Map<String, Object> result) {
        CodeReviewResult review = CodeReviewResult.builder()
                .id(UUID.randomUUID().toString())
                .reviewType(reviewType)
                .issuesFound(issuesFound)
                .suggestionsJson(result.toString())
                .resultJson(result.toString())
                .createdAt(Instant.now())
                .build();
        codeReviewRepository.save(review);
    }

    // Helper class for complexity patterns
    private static class ComplexityPattern {
        final String regex;
        final String name;
        final String description;

        ComplexityPattern(String regex, String name, String description) {
            this.regex = regex;
            this.name = name;
            this.description = description;
        }
    }
}
