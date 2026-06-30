package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.BenchmarkResult;
import com.localmind.domain.mcp.port.in.PerformanceProfilerUseCase;
import com.localmind.domain.mcp.port.out.PerformanceProfilerRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Domain service implementing performance profiling tools.
 * Pure Java, zero Spring dependencies.
 */
public class PerformanceProfilerService implements PerformanceProfilerUseCase {

    private final PerformanceProfilerRepository repository;

    // Heavy dependency definitions: name -> estimated size description
    private static final Map<String, String> HEAVY_JS_DEPS = new LinkedHashMap<>();
    private static final Map<String, String> HEAVY_JAVA_DEPS = new LinkedHashMap<>();

    static {
        HEAVY_JS_DEPS.put("moment", "300KB");
        HEAVY_JS_DEPS.put("lodash", "70KB");
        HEAVY_JS_DEPS.put("rxjs", "50KB+");
        HEAVY_JS_DEPS.put("aws-sdk", "100MB+");
        HEAVY_JS_DEPS.put("@material-ui", "300KB+");
        HEAVY_JS_DEPS.put("@mui", "300KB+");
        HEAVY_JS_DEPS.put("chart.js", "200KB");
        HEAVY_JS_DEPS.put("three", "600KB+");
        HEAVY_JS_DEPS.put("jquery", "85KB");
        HEAVY_JS_DEPS.put("underscore", "20KB");

        HEAVY_JAVA_DEPS.put("spring-boot-starter-web", "20MB+");
        HEAVY_JAVA_DEPS.put("hibernate-core", "8MB+");
        HEAVY_JAVA_DEPS.put("jackson-databind", "3MB+");
        HEAVY_JAVA_DEPS.put("guava", "3MB+");
        HEAVY_JAVA_DEPS.put("commons-lang3", "500KB");
    }

    // Regex patterns for import parsing
    private static final Pattern ES_IMPORT_FROM = Pattern.compile(
            "import\\s+(?:\\{[^}]*\\}|\\*\\s+as\\s+\\w+|[\\w$]+(?:\\s*,\\s*\\{[^}]*\\})?)\\s+from\\s+['\"]([^'\"]+)['\"]");
    private static final Pattern ES_IMPORT_SIDE_EFFECT = Pattern.compile(
            "import\\s+['\"]([^'\"]+)['\"]");
    private static final Pattern CJS_REQUIRE = Pattern.compile(
            "(?:const|let|var)\\s+[\\w${}\\s,]+\\s*=\\s*require\\(['\"]([^'\"]+)['\"]\\)");
    private static final Pattern CJS_REQUIRE_BARE = Pattern.compile(
            "require\\(['\"]([^'\"]+)['\"]\\)");
    private static final Pattern JAVA_IMPORT = Pattern.compile(
            "import\\s+(static\\s+)?([\\w.]+(?:\\.[*])?);");

    // Bottleneck detection patterns
    private static final Pattern FOR_LOOP = Pattern.compile("\\b(for|while)\\s*\\(");
    private static final Pattern FOREACH_LOOP = Pattern.compile("\\.forEach\\s*\\(");

    public PerformanceProfilerService(PerformanceProfilerRepository repository) {
        this.repository = repository;
    }

    // ========================================================================
    // 1. analyzeBundle
    // ========================================================================

    @Override
    public Map<String, Object> analyzeBundle(String code, String filePath) {
        if (code == null || code.isBlank()) {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("filePath", filePath != null ? filePath : "unknown");
            empty.put("linesOfCode", 0);
            empty.put("totalImports", 0);
            empty.put("heavyDependencies", 0);
            empty.put("imports", List.of());
            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("totalEstimatedSize", "0KB");
            summary.put("heavyCount", 0);
            summary.put("suggestion", "No imports found.");
            empty.put("summary", summary);
            return empty;
        }

        String[] lines = code.split("\n");
        int linesOfCode = lines.length;

        List<Map<String, Object>> imports = new ArrayList<>();
        boolean isJavaFile = filePath != null && filePath.endsWith(".java");

        if (isJavaFile) {
            parseJavaImports(code, imports);
        } else {
            parseJsImports(code, imports);
        }

        int heavyCount = 0;
        long totalEstimatedBytes = 0;
        for (Map<String, Object> imp : imports) {
            if (Boolean.TRUE.equals(imp.get("isHeavy"))) {
                heavyCount++;
                totalEstimatedBytes += parseSizeToBytes((String) imp.get("estimatedSize"));
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("filePath", filePath != null ? filePath : "unknown");
        result.put("linesOfCode", linesOfCode);
        result.put("totalImports", imports.size());
        result.put("heavyDependencies", heavyCount);
        result.put("imports", imports);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalEstimatedSize", formatBytes(totalEstimatedBytes));
        summary.put("heavyCount", heavyCount);
        if (heavyCount == 0) {
            summary.put("suggestion", "No heavy dependencies detected. Bundle looks clean.");
        } else if (heavyCount <= 2) {
            summary.put("suggestion", "Consider tree-shaking or replacing heavy dependencies with lighter alternatives.");
        } else {
            summary.put("suggestion", "Multiple heavy dependencies detected. Consider lazy loading, code splitting, or lighter alternatives.");
        }
        result.put("summary", summary);

        // Persist
        BenchmarkResult benchmarkResult = BenchmarkResult.builder()
                .id(UUID.randomUUID().toString())
                .name("bundle-analysis:" + (filePath != null ? filePath : "unknown"))
                .language(isJavaFile ? "java" : "javascript")
                .resultJson(mapToJson(result))
                .createdAt(Instant.now())
                .build();
        repository.save(benchmarkResult);

        return result;
    }

    private void parseJsImports(String code, List<Map<String, Object>> imports) {
        // ES import from
        Matcher matcher = ES_IMPORT_FROM.matcher(code);
        while (matcher.find()) {
            String pkg = matcher.group(1);
            String rootPkg = extractRootPackage(pkg);
            addImport(imports, pkg, "es-import", rootPkg, false);
        }

        // ES side-effect import
        matcher = ES_IMPORT_SIDE_EFFECT.matcher(code);
        while (matcher.find()) {
            String pkg = matcher.group(1);
            // Avoid duplicates from the previous pattern
            if (!containsImportNamed(imports, pkg)) {
                String rootPkg = extractRootPackage(pkg);
                addImport(imports, pkg, "es-import", rootPkg, false);
            }
        }

        // CJS require
        matcher = CJS_REQUIRE.matcher(code);
        while (matcher.find()) {
            String pkg = matcher.group(1);
            if (!containsImportNamed(imports, pkg)) {
                String rootPkg = extractRootPackage(pkg);
                addImport(imports, pkg, "cjs-require", rootPkg, false);
            }
        }

        // CJS bare require
        matcher = CJS_REQUIRE_BARE.matcher(code);
        while (matcher.find()) {
            String pkg = matcher.group(1);
            if (!containsImportNamed(imports, pkg)) {
                String rootPkg = extractRootPackage(pkg);
                addImport(imports, pkg, "cjs-require", rootPkg, false);
            }
        }

        // Mark heavy deps
        for (Map<String, Object> imp : imports) {
            String name = (String) imp.get("name");
            String rootPkg = extractRootPackage(name);
            for (Map.Entry<String, String> entry : HEAVY_JS_DEPS.entrySet()) {
                if (rootPkg.equals(entry.getKey()) || name.startsWith(entry.getKey())) {
                    imp.put("isHeavy", true);
                    imp.put("estimatedSize", entry.getValue());
                    break;
                }
            }
        }
    }

    private void parseJavaImports(String code, List<Map<String, Object>> imports) {
        Matcher matcher = JAVA_IMPORT.matcher(code);
        while (matcher.find()) {
            String fullImport = matcher.group(2);
            boolean isStatic = matcher.group(1) != null;
            String type = isStatic ? "java-static-import" : "java-import";
            addImport(imports, fullImport, type, fullImport, true);
        }

        // Mark heavy Java deps based on package name patterns
        for (Map<String, Object> imp : imports) {
            String name = (String) imp.get("name");
            if (name.startsWith("org.springframework.boot")) {
                imp.put("isHeavy", true);
                imp.put("estimatedSize", HEAVY_JAVA_DEPS.get("spring-boot-starter-web"));
            } else if (name.startsWith("org.hibernate")) {
                imp.put("isHeavy", true);
                imp.put("estimatedSize", HEAVY_JAVA_DEPS.get("hibernate-core"));
            } else if (name.startsWith("com.fasterxml.jackson")) {
                imp.put("isHeavy", true);
                imp.put("estimatedSize", HEAVY_JAVA_DEPS.get("jackson-databind"));
            } else if (name.startsWith("com.google.common") || name.startsWith("com.google.guava")) {
                imp.put("isHeavy", true);
                imp.put("estimatedSize", HEAVY_JAVA_DEPS.get("guava"));
            } else if (name.startsWith("org.apache.commons.lang3")) {
                imp.put("isHeavy", true);
                imp.put("estimatedSize", HEAVY_JAVA_DEPS.get("commons-lang3"));
            }
        }
    }

    private void addImport(List<Map<String, Object>> imports, String name, String type,
                           String rootPkg, boolean isJava) {
        Map<String, Object> imp = new LinkedHashMap<>();
        imp.put("name", name);
        imp.put("type", type);
        imp.put("isHeavy", false);
        imp.put("estimatedSize", null);
        imports.add(imp);
    }

    private boolean containsImportNamed(List<Map<String, Object>> imports, String name) {
        return imports.stream().anyMatch(imp -> name.equals(imp.get("name")));
    }

    private String extractRootPackage(String pkg) {
        if (pkg.startsWith("@")) {
            // Scoped package: @scope/name -> @scope/name
            int slashIdx = pkg.indexOf('/');
            if (slashIdx > 0) {
                int secondSlash = pkg.indexOf('/', slashIdx + 1);
                return secondSlash > 0 ? pkg.substring(0, secondSlash) : pkg;
            }
            return pkg;
        }
        int slashIdx = pkg.indexOf('/');
        return slashIdx > 0 ? pkg.substring(0, slashIdx) : pkg;
    }

    private long parseSizeToBytes(String size) {
        if (size == null) return 0;
        String cleaned = size.replaceAll("[+>]", "").trim();
        try {
            if (cleaned.endsWith("MB")) {
                return (long) (Double.parseDouble(cleaned.replace("MB", "").trim()) * 1024 * 1024);
            } else if (cleaned.endsWith("KB")) {
                return (long) (Double.parseDouble(cleaned.replace("KB", "").trim()) * 1024);
            }
        } catch (NumberFormatException e) {
            return 0;
        }
        return 0;
    }

    private String formatBytes(long bytes) {
        if (bytes >= 1024 * 1024) {
            return String.format("%.1fMB", bytes / (1024.0 * 1024.0));
        } else if (bytes >= 1024) {
            return String.format("%.0fKB", bytes / 1024.0);
        }
        return bytes + "B";
    }

    // ========================================================================
    // 2. findBottlenecks
    // ========================================================================

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> findBottlenecks(String code, String language) {
        if (code == null || code.isBlank()) {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("totalBottlenecks", 0);
            empty.put("bySeverity", Map.of("critical", 0, "warning", 0, "info", 0));
            empty.put("byType", Map.of());
            empty.put("bottlenecks", List.of());
            return empty;
        }

        String lang = (language != null) ? language.toLowerCase() : "java";
        String[] lines = code.split("\n");
        List<Map<String, Object>> bottlenecks = new ArrayList<>();

        detectNestedLoops(lines, lang, bottlenecks);
        detectSyncIo(lines, lang, bottlenecks);
        detectLinearSearchInLoop(lines, lang, bottlenecks);
        detectMissingPagination(lines, lang, bottlenecks);
        detectDomQueryInLoop(lines, lang, bottlenecks);
        detectObjectCreationInRender(lines, lang, bottlenecks);
        detectStringConcatInLoop(lines, lang, bottlenecks);
        detectJsonInLoop(lines, lang, bottlenecks);
        detectRecursionWithoutMemo(lines, lang, bottlenecks);
        detectSequentialAwait(lines, lang, bottlenecks);

        // Build result
        int critical = 0, warning = 0, info = 0;
        Map<String, Integer> byType = new LinkedHashMap<>();
        for (Map<String, Object> b : bottlenecks) {
            String severity = (String) b.get("severity");
            switch (severity) {
                case "critical" -> critical++;
                case "warning" -> warning++;
                case "info" -> info++;
            }
            String type = (String) b.get("type");
            byType.merge(type, 1, Integer::sum);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalBottlenecks", bottlenecks.size());

        Map<String, Integer> bySeverity = new LinkedHashMap<>();
        bySeverity.put("critical", critical);
        bySeverity.put("warning", warning);
        bySeverity.put("info", info);
        result.put("bySeverity", bySeverity);
        result.put("byType", byType);
        result.put("bottlenecks", bottlenecks);

        // Persist
        BenchmarkResult benchmarkResult = BenchmarkResult.builder()
                .id(UUID.randomUUID().toString())
                .name("bottleneck-analysis")
                .language(lang)
                .resultJson(mapToJson(result))
                .createdAt(Instant.now())
                .build();
        repository.save(benchmarkResult);

        return result;
    }

    // --- Bottleneck detectors ---

    private void detectNestedLoops(String[] lines, String lang, List<Map<String, Object>> bottlenecks) {
        // Track loop nesting depth
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (isLoopStart(line, lang)) {
                // Look ahead for nested loop
                int braceDepth = 0;
                boolean foundOpen = false;
                for (int j = i; j < lines.length && j < i + 50; j++) {
                    String innerLine = lines[j].trim();
                    for (char c : innerLine.toCharArray()) {
                        if (c == '{') {
                            braceDepth++;
                            foundOpen = true;
                        }
                        if (c == '}') braceDepth--;
                    }
                    if (j > i && isLoopStart(innerLine, lang) && foundOpen && braceDepth > 0) {
                        addBottleneck(bottlenecks, "nested-loop", "critical", i + 1,
                                "Nested loop detected at line " + (j + 1) + " inside loop at line " + (i + 1) + ". Potential O(n^2) complexity.",
                                "Consider using a Map/Set for lookups, or refactor to avoid nested iteration.");
                        break;
                    }
                    if (foundOpen && braceDepth <= 0) break;
                }
            }
        }
    }

    private void detectSyncIo(String[] lines, String lang, List<Map<String, Object>> bottlenecks) {
        if (!"javascript".equals(lang) && !"typescript".equals(lang)) return;
        Pattern syncIoPattern = Pattern.compile("(readFileSync|writeFileSync|execSync|readSync|appendFileSync|mkdirSync)");
        for (int i = 0; i < lines.length; i++) {
            Matcher m = syncIoPattern.matcher(lines[i]);
            if (m.find()) {
                addBottleneck(bottlenecks, "sync-io", "warning", i + 1,
                        "Synchronous I/O operation '" + m.group(1) + "' detected. This blocks the event loop.",
                        "Use async alternatives: readFile, writeFile, exec with callback/promise.");
            }
        }
    }

    private void detectLinearSearchInLoop(String[] lines, String lang, List<Map<String, Object>> bottlenecks) {
        Pattern searchPattern;
        if ("java".equals(lang)) {
            searchPattern = Pattern.compile("\\.(indexOf|contains|find|search|lastIndexOf)\\s*\\(");
        } else {
            searchPattern = Pattern.compile("\\.(indexOf|includes|find|findIndex|search|lastIndexOf)\\s*\\(");
        }

        boolean inLoop = false;
        int loopLine = -1;
        int braceDepth = 0;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (isLoopStart(line, lang)) {
                inLoop = true;
                loopLine = i + 1;
                braceDepth = 0;
            }
            if (inLoop) {
                for (char c : line.toCharArray()) {
                    if (c == '{') braceDepth++;
                    if (c == '}') braceDepth--;
                }
                Matcher m = searchPattern.matcher(line);
                if (m.find() && braceDepth > 0) {
                    addBottleneck(bottlenecks, "linear-search-in-loop", "warning", i + 1,
                            "Linear search '" + m.group(1) + "' inside loop (started at line " + loopLine + "). This is O(n*m).",
                            "Pre-compute a Set or Map for O(1) lookups before the loop.");
                }
                if (braceDepth <= 0) {
                    inLoop = false;
                }
            }
        }
    }

    private void detectMissingPagination(String[] lines, String lang, List<Map<String, Object>> bottlenecks) {
        Pattern paginationPattern;
        if ("java".equals(lang)) {
            paginationPattern = Pattern.compile("(\\.findAll\\s*\\(\\s*\\)|\"SELECT\\s+\\*\\s+FROM)");
        } else if ("python".equals(lang)) {
            paginationPattern = Pattern.compile("(\\.find\\s*\\(\\s*\\{\\s*\\}\\s*\\)|\\.find\\s*\\(\\s*\\)|\"SELECT\\s+\\*\\s+FROM)");
        } else {
            paginationPattern = Pattern.compile("(\\.find\\s*\\(\\s*\\{\\s*\\}\\s*\\)|\\.find\\s*\\(\\s*\\)|\"SELECT\\s+\\*\\s+FROM)");
        }
        for (int i = 0; i < lines.length; i++) {
            Matcher m = paginationPattern.matcher(lines[i]);
            if (m.find()) {
                addBottleneck(bottlenecks, "missing-pagination", "warning", i + 1,
                        "Query without pagination/limit detected: '" + m.group(1).trim() + "'. May load unbounded data.",
                        "Add LIMIT/pagination parameters to avoid loading all records into memory.");
            }
        }
    }

    private void detectDomQueryInLoop(String[] lines, String lang, List<Map<String, Object>> bottlenecks) {
        if (!"javascript".equals(lang) && !"typescript".equals(lang)) return;
        Pattern domPattern = Pattern.compile("(querySelector|querySelectorAll|getElementById|getElementsByClassName|getElementsByTagName)\\s*\\(");

        boolean inLoop = false;
        int loopLine = -1;
        int braceDepth = 0;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (isLoopStart(line, lang)) {
                inLoop = true;
                loopLine = i + 1;
                braceDepth = 0;
            }
            if (inLoop) {
                for (char c : line.toCharArray()) {
                    if (c == '{') braceDepth++;
                    if (c == '}') braceDepth--;
                }
                Matcher m = domPattern.matcher(line);
                if (m.find() && braceDepth > 0) {
                    addBottleneck(bottlenecks, "dom-query-in-loop", "critical", i + 1,
                            "DOM query '" + m.group(1) + "' inside loop (started at line " + loopLine + "). Causes layout thrashing.",
                            "Cache the DOM query result before the loop.");
                }
                if (braceDepth <= 0) {
                    inLoop = false;
                }
            }
        }
    }

    private void detectObjectCreationInRender(String[] lines, String lang, List<Map<String, Object>> bottlenecks) {
        if (!"javascript".equals(lang) && !"typescript".equals(lang)) return;
        boolean inRender = false;
        int parenDepth = 0;
        int renderStartLine = -1;
        Pattern renderPattern = Pattern.compile("(render\\s*\\(|return\\s*\\()");
        Pattern objectCreation = Pattern.compile("(new\\s+(Array|Object|Map|Set|Date)\\s*\\(|\\{\\s*\\w+\\s*:|\\[\\s*[^\\]]+\\s*\\])");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!inRender && renderPattern.matcher(line).find()) {
                inRender = true;
                parenDepth = 0;
                renderStartLine = i;
                // Count parens on this line
                for (char c : line.toCharArray()) {
                    if (c == '(') parenDepth++;
                    if (c == ')') parenDepth--;
                }
                continue;
            }
            if (inRender) {
                for (char c : line.toCharArray()) {
                    if (c == '(') parenDepth++;
                    if (c == ')') parenDepth--;
                }
                Matcher m = objectCreation.matcher(line);
                if (m.find()) {
                    addBottleneck(bottlenecks, "object-creation-in-render", "warning", i + 1,
                            "New object/array creation detected in render context. Causes unnecessary re-renders.",
                            "Extract object/array creation outside render using useMemo, useCallback, or class fields.");
                }
                if (parenDepth <= 0 && i > renderStartLine) {
                    inRender = false;
                }
            }
        }
    }

    private void detectStringConcatInLoop(String[] lines, String lang, List<Map<String, Object>> bottlenecks) {
        Pattern concatPattern = Pattern.compile("\\+=\\s*[\"'`]|\\+=\\s*\\w");

        boolean inLoop = false;
        int loopLine = -1;
        int braceDepth = 0;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (isLoopStart(line, lang)) {
                inLoop = true;
                loopLine = i + 1;
                braceDepth = 0;
            }
            if (inLoop) {
                for (char c : line.toCharArray()) {
                    if (c == '{') braceDepth++;
                    if (c == '}') braceDepth--;
                }
                if (concatPattern.matcher(line).find() && braceDepth > 0) {
                    String suggestion;
                    if ("java".equals(lang)) {
                        suggestion = "Use StringBuilder instead of string concatenation in loops.";
                    } else if ("python".equals(lang)) {
                        suggestion = "Use list append + ''.join() instead of string concatenation in loops.";
                    } else {
                        suggestion = "Collect parts in an array and use array.join('') instead of string concatenation.";
                    }
                    addBottleneck(bottlenecks, "string-concat-in-loop", "info", i + 1,
                            "String concatenation with += inside loop (started at line " + loopLine + "). Creates new string objects each iteration.",
                            suggestion);
                }
                if (braceDepth <= 0) {
                    inLoop = false;
                }
            }
        }
    }

    private void detectJsonInLoop(String[] lines, String lang, List<Map<String, Object>> bottlenecks) {
        Pattern jsonPattern;
        if ("java".equals(lang)) {
            jsonPattern = Pattern.compile("(ObjectMapper|JsonParser|JsonGenerator|Gson|writeValueAsString|readValue|fromJson|toJson)");
        } else {
            jsonPattern = Pattern.compile("(JSON\\.parse|JSON\\.stringify)");
        }

        boolean inLoop = false;
        int loopLine = -1;
        int braceDepth = 0;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (isLoopStart(line, lang)) {
                inLoop = true;
                loopLine = i + 1;
                braceDepth = 0;
            }
            if (inLoop) {
                for (char c : line.toCharArray()) {
                    if (c == '{') braceDepth++;
                    if (c == '}') braceDepth--;
                }
                Matcher m = jsonPattern.matcher(line);
                if (m.find() && braceDepth > 0) {
                    addBottleneck(bottlenecks, "json-in-loop", "warning", i + 1,
                            "JSON serialization/deserialization '" + m.group(1) + "' inside loop (started at line " + loopLine + "). Expensive per iteration.",
                            "Move JSON operations outside the loop, or batch process data.");
                }
                if (braceDepth <= 0) {
                    inLoop = false;
                }
            }
        }
    }

    private void detectRecursionWithoutMemo(String[] lines, String lang, List<Map<String, Object>> bottlenecks) {
        // Find function definitions, then check if the function calls itself without memoization
        Pattern funcPattern;
        if ("java".equals(lang)) {
            funcPattern = Pattern.compile("(?:public|private|protected|static|\\s)+\\s+\\w+\\s+(\\w+)\\s*\\(");
        } else if ("python".equals(lang)) {
            funcPattern = Pattern.compile("def\\s+(\\w+)\\s*\\(");
        } else {
            funcPattern = Pattern.compile("(?:function\\s+(\\w+)|(?:const|let|var)\\s+(\\w+)\\s*=\\s*(?:function|\\([^)]*\\)\\s*=>))");
        }

        for (int i = 0; i < lines.length; i++) {
            Matcher m = funcPattern.matcher(lines[i]);
            if (m.find()) {
                String funcName = m.group(1);
                if (funcName == null && m.groupCount() >= 2) {
                    funcName = m.group(2);
                }
                if (funcName == null) continue;

                // Look for self-call within the function body
                int braceDepth = 0;
                boolean foundOpen = false;
                boolean selfCall = false;
                boolean hasMemo = false;
                for (int j = i; j < lines.length && j < i + 100; j++) {
                    String bodyLine = lines[j];
                    for (char c : bodyLine.toCharArray()) {
                        if (c == '{') { braceDepth++; foundOpen = true; }
                        if (c == '}') braceDepth--;
                    }
                    if (j > i && bodyLine.contains(funcName + "(")) {
                        selfCall = true;
                    }
                    if (bodyLine.contains("memo") || bodyLine.contains("cache") || bodyLine.contains("Map")
                            || bodyLine.contains("@Cacheable") || bodyLine.contains("lru_cache")
                            || bodyLine.contains("HashMap") || bodyLine.contains("ConcurrentHashMap")) {
                        hasMemo = true;
                    }
                    if (foundOpen && braceDepth <= 0) break;
                }
                if (selfCall && !hasMemo) {
                    addBottleneck(bottlenecks, "recursion-without-memo", "info", i + 1,
                            "Recursive function '" + funcName + "' without apparent memoization. May cause exponential time complexity.",
                            "Add memoization (cache/Map) or convert to iterative approach.");
                }
            }
        }
    }

    private void detectSequentialAwait(String[] lines, String lang, List<Map<String, Object>> bottlenecks) {
        Pattern awaitPattern;
        if ("java".equals(lang)) {
            awaitPattern = Pattern.compile("\\.(get|join)\\s*\\(");
        } else if ("python".equals(lang)) {
            awaitPattern = Pattern.compile("await\\s+");
        } else {
            awaitPattern = Pattern.compile("await\\s+");
        }

        boolean inLoop = false;
        int loopLine = -1;
        int braceDepth = 0;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (isLoopStart(line, lang)) {
                inLoop = true;
                loopLine = i + 1;
                braceDepth = 0;
            }
            if (inLoop) {
                for (char c : line.toCharArray()) {
                    if (c == '{') braceDepth++;
                    if (c == '}') braceDepth--;
                }
                Matcher m = awaitPattern.matcher(line);
                if (m.find() && braceDepth > 0) {
                    String suggestion;
                    if ("java".equals(lang)) {
                        suggestion = "Collect CompletableFutures and use CompletableFuture.allOf() for parallel execution.";
                    } else if ("python".equals(lang)) {
                        suggestion = "Use asyncio.gather() for parallel execution instead of sequential await.";
                    } else {
                        suggestion = "Collect promises and use Promise.all() for parallel execution.";
                    }
                    addBottleneck(bottlenecks, "sequential-await", "warning", i + 1,
                            "Sequential async operation inside loop (started at line " + loopLine + "). Each iteration waits for completion.",
                            suggestion);
                }
                if (braceDepth <= 0) {
                    inLoop = false;
                }
            }
        }
    }

    private boolean isLoopStart(String line, String lang) {
        if ("python".equals(lang)) {
            return line.matches("\\s*(for|while)\\s+.*:.*") || line.matches("(for|while)\\s+.*:.*");
        }
        return FOR_LOOP.matcher(line).find() || FOREACH_LOOP.matcher(line).find()
                || line.contains(".stream()") || line.contains(".map(") || line.contains(".filter(");
    }

    private void addBottleneck(List<Map<String, Object>> bottlenecks, String type, String severity,
                               int line, String message, String suggestion) {
        Map<String, Object> b = new LinkedHashMap<>();
        b.put("type", type);
        b.put("severity", severity);
        b.put("line", line);
        b.put("message", message);
        b.put("suggestion", suggestion);
        bottlenecks.add(b);
    }

    // ========================================================================
    // 3. benchmarkCompare
    // ========================================================================

    @Override
    public Map<String, Object> benchmarkCompare(String codeA, String codeB, int iterations, String language) {
        String lang = (language != null && !language.isBlank()) ? language.toLowerCase() : "java";
        int iters = iterations > 0 ? iterations : 1000;

        String benchmarkCode;
        String instructions;

        if ("javascript".equals(lang)) {
            benchmarkCode = generateJsBenchmark(codeA, codeB, iters);
            instructions = "Save the code to a file (e.g., benchmark.js) and run: node benchmark.js";
        } else {
            benchmarkCode = generateJavaBenchmark(codeA, codeB, iters);
            instructions = "Save the code to Benchmark.java, compile: javac Benchmark.java, run: java Benchmark";
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("language", lang);
        result.put("iterations", iters);
        result.put("benchmarkCode", benchmarkCode);
        result.put("instructions", instructions);

        // Persist
        BenchmarkResult benchmarkResult = BenchmarkResult.builder()
                .id(UUID.randomUUID().toString())
                .name("benchmark-compare")
                .language(lang)
                .resultJson(mapToJson(result))
                .createdAt(Instant.now())
                .build();
        repository.save(benchmarkResult);

        return result;
    }

    private String generateJavaBenchmark(String codeA, String codeB, int iterations) {
        int warmupIterations = Math.min(iterations / 10, 100);
        return "import java.util.Arrays;\n" +
                "\n" +
                "public class Benchmark {\n" +
                "\n" +
                "    public static void main(String[] args) {\n" +
                "        int iterations = " + iterations + ";\n" +
                "        int warmup = " + warmupIterations + ";\n" +
                "\n" +
                "        // --- Warmup Phase ---\n" +
                "        System.out.println(\"Warming up (\" + warmup + \" iterations)...\");\n" +
                "        for (int i = 0; i < warmup; i++) {\n" +
                "            codeA();\n" +
                "            codeB();\n" +
                "        }\n" +
                "\n" +
                "        // --- Benchmark Code A ---\n" +
                "        long[] timesA = new long[iterations];\n" +
                "        for (int i = 0; i < iterations; i++) {\n" +
                "            long start = System.nanoTime();\n" +
                "            codeA();\n" +
                "            timesA[i] = System.nanoTime() - start;\n" +
                "        }\n" +
                "\n" +
                "        // --- Benchmark Code B ---\n" +
                "        long[] timesB = new long[iterations];\n" +
                "        for (int i = 0; i < iterations; i++) {\n" +
                "            long start = System.nanoTime();\n" +
                "            codeB();\n" +
                "            timesB[i] = System.nanoTime() - start;\n" +
                "        }\n" +
                "\n" +
                "        // --- Results ---\n" +
                "        printStats(\"Code A\", timesA, iterations);\n" +
                "        printStats(\"Code B\", timesB, iterations);\n" +
                "\n" +
                "        double meanA = Arrays.stream(timesA).average().orElse(0);\n" +
                "        double meanB = Arrays.stream(timesB).average().orElse(0);\n" +
                "        String winner = meanA < meanB ? \"Code A\" : \"Code B\";\n" +
                "        double ratio = meanA < meanB ? meanB / meanA : meanA / meanB;\n" +
                "        System.out.printf(\"%nWinner: %s (%.2fx faster)%n\", winner, ratio);\n" +
                "    }\n" +
                "\n" +
                "    static void codeA() {\n" +
                "        " + codeA.replace("\n", "\n        ") + "\n" +
                "    }\n" +
                "\n" +
                "    static void codeB() {\n" +
                "        " + codeB.replace("\n", "\n        ") + "\n" +
                "    }\n" +
                "\n" +
                "    static void printStats(String label, long[] times, int iterations) {\n" +
                "        Arrays.sort(times);\n" +
                "        long total = Arrays.stream(times).sum();\n" +
                "        double mean = (double) total / iterations;\n" +
                "        long median = times[iterations / 2];\n" +
                "        long min = times[0];\n" +
                "        long max = times[iterations - 1];\n" +
                "        long p95 = times[(int)(iterations * 0.95)];\n" +
                "        long p99 = times[(int)(iterations * 0.99)];\n" +
                "        double variance = Arrays.stream(times).mapToDouble(t -> Math.pow(t - mean, 2)).average().orElse(0);\n" +
                "        double stdDev = Math.sqrt(variance);\n" +
                "        double opsPerSecond = 1_000_000_000.0 / mean;\n" +
                "\n" +
                "        System.out.printf(\"%n=== %s ===%n\", label);\n" +
                "        System.out.printf(\"Total:        %,d ns (%.2f ms)%n\", total, total / 1_000_000.0);\n" +
                "        System.out.printf(\"Mean:         %.2f ns%n\", mean);\n" +
                "        System.out.printf(\"Median:       %,d ns%n\", median);\n" +
                "        System.out.printf(\"Min:          %,d ns%n\", min);\n" +
                "        System.out.printf(\"Max:          %,d ns%n\", max);\n" +
                "        System.out.printf(\"P95:          %,d ns%n\", p95);\n" +
                "        System.out.printf(\"P99:          %,d ns%n\", p99);\n" +
                "        System.out.printf(\"StdDev:       %.2f ns%n\", stdDev);\n" +
                "        System.out.printf(\"Ops/sec:      %,.0f%n\", opsPerSecond);\n" +
                "    }\n" +
                "}\n";
    }

    private String generateJsBenchmark(String codeA, String codeB, int iterations) {
        int warmupIterations = Math.min(iterations / 10, 100);
        return "const { performance } = require('perf_hooks');\n" +
                "\n" +
                "function codeA() {\n" +
                "    " + codeA.replace("\n", "\n    ") + "\n" +
                "}\n" +
                "\n" +
                "function codeB() {\n" +
                "    " + codeB.replace("\n", "\n    ") + "\n" +
                "}\n" +
                "\n" +
                "function benchmark(fn, label, iterations, warmup) {\n" +
                "    // Warmup\n" +
                "    for (let i = 0; i < warmup; i++) fn();\n" +
                "\n" +
                "    const times = [];\n" +
                "    for (let i = 0; i < iterations; i++) {\n" +
                "        const start = performance.now();\n" +
                "        fn();\n" +
                "        times.push((performance.now() - start) * 1_000_000); // to ns\n" +
                "    }\n" +
                "\n" +
                "    times.sort((a, b) => a - b);\n" +
                "    const total = times.reduce((a, b) => a + b, 0);\n" +
                "    const mean = total / iterations;\n" +
                "    const median = times[Math.floor(iterations / 2)];\n" +
                "    const min = times[0];\n" +
                "    const max = times[iterations - 1];\n" +
                "    const p95 = times[Math.floor(iterations * 0.95)];\n" +
                "    const p99 = times[Math.floor(iterations * 0.99)];\n" +
                "    const variance = times.reduce((acc, t) => acc + Math.pow(t - mean, 2), 0) / iterations;\n" +
                "    const stdDev = Math.sqrt(variance);\n" +
                "    const opsPerSecond = 1_000_000_000 / mean;\n" +
                "\n" +
                "    console.log(`\\n=== ${label} ===`);\n" +
                "    console.log(`Total:     ${total.toFixed(2)} ns (${(total / 1_000_000).toFixed(2)} ms)`);\n" +
                "    console.log(`Mean:      ${mean.toFixed(2)} ns`);\n" +
                "    console.log(`Median:    ${median.toFixed(2)} ns`);\n" +
                "    console.log(`Min:       ${min.toFixed(2)} ns`);\n" +
                "    console.log(`Max:       ${max.toFixed(2)} ns`);\n" +
                "    console.log(`P95:       ${p95.toFixed(2)} ns`);\n" +
                "    console.log(`P99:       ${p99.toFixed(2)} ns`);\n" +
                "    console.log(`StdDev:    ${stdDev.toFixed(2)} ns`);\n" +
                "    console.log(`Ops/sec:   ${opsPerSecond.toFixed(0)}`);\n" +
                "\n" +
                "    return { mean, median, min, max, p95, p99, stdDev, opsPerSecond };\n" +
                "}\n" +
                "\n" +
                "const iterations = " + iterations + ";\n" +
                "const warmup = " + warmupIterations + ";\n" +
                "\n" +
                "console.log(`Running benchmark (${iterations} iterations, ${warmup} warmup)...`);\n" +
                "const statsA = benchmark(codeA, 'Code A', iterations, warmup);\n" +
                "const statsB = benchmark(codeB, 'Code B', iterations, warmup);\n" +
                "\n" +
                "const winner = statsA.mean < statsB.mean ? 'Code A' : 'Code B';\n" +
                "const ratio = statsA.mean < statsB.mean ? statsB.mean / statsA.mean : statsA.mean / statsB.mean;\n" +
                "console.log(`\\nWinner: ${winner} (${ratio.toFixed(2)}x faster)`);\n";
    }

    // ========================================================================
    // Utility
    // ========================================================================

    private String mapToJson(Map<String, Object> map) {
        // Simple JSON serialization without external libraries
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

    @SuppressWarnings("unchecked")
    private String valueToJson(Object value) {
        if (value == null) return "null";
        if (value instanceof String) return "\"" + escapeJson((String) value) + "\"";
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        if (value instanceof Map) return mapToJson((Map<String, Object>) value);
        if (value instanceof List) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : (List<?>) value) {
                if (!first) sb.append(",");
                sb.append(valueToJson(item));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }
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
