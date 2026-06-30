package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.CodebaseKnowledge;
import com.localmind.domain.mcp.port.in.CodebaseKnowledgeUseCase;
import com.localmind.domain.mcp.port.out.CodebaseKnowledgeRepository;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CodebaseKnowledgeService implements CodebaseKnowledgeUseCase {

    private final CodebaseKnowledgeRepository repository;

    private static final Set<String> SKIP_DIRS = Set.of(
            "node_modules", ".git", "dist", "build", ".next", ".cache",
            "coverage", "__pycache__", "target", ".idea", ".vscode"
    );

    private static final Set<String> SOURCE_EXTENSIONS = Set.of(
            ".java", ".ts", ".tsx", ".js", ".jsx", ".mjs", ".mts", ".py", ".go", ".rs", ".kt"
    );

    public CodebaseKnowledgeService(CodebaseKnowledgeRepository repository) {
        this.repository = repository;
    }

    // ========== 1. searchCode ==========

    @Override
    public Map<String, Object> searchCode(String directory, String pattern, List<String> fileExtensions, int maxResults) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (directory == null || directory.isBlank()) {
            result.put("error", "directory is empty or null");
            return result;
        }
        if (pattern == null || pattern.isBlank()) {
            result.put("error", "pattern is empty or null");
            return result;
        }
        if (maxResults <= 0) maxResults = 20;

        Path dirPath = Path.of(directory);
        if (!Files.isDirectory(dirPath)) {
            result.put("error", "Directory does not exist: " + directory);
            return result;
        }

        Pattern searchPattern;
        try {
            searchPattern = Pattern.compile(pattern);
        } catch (Exception e) {
            searchPattern = Pattern.compile(Pattern.quote(pattern));
        }

        List<Map<String, Object>> matches = new ArrayList<>();
        int[] totalFilesScanned = {0};

        searchDirectory(dirPath, dirPath, searchPattern, fileExtensions, matches, totalFilesScanned, maxResults);

        result.put("totalFilesScanned", totalFilesScanned[0]);
        result.put("totalMatches", matches.size());
        result.put("matchingFiles", matches.stream().map(m -> m.get("relativePath")).distinct().collect(Collectors.toList()));
        result.put("matches", matches);

        saveResult("search-code", directory, result);
        return result;
    }

    private void searchDirectory(Path root, Path dir, Pattern pattern, List<String> extensions,
                                  List<Map<String, Object>> matches, int[] totalScanned, int maxResults) {
        if (matches.size() >= maxResults) return;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                if (matches.size() >= maxResults) break;

                if (Files.isDirectory(entry)) {
                    String dirName = entry.getFileName().toString();
                    if (!SKIP_DIRS.contains(dirName)) {
                        searchDirectory(root, entry, pattern, extensions, matches, totalScanned, maxResults);
                    }
                } else if (Files.isRegularFile(entry)) {
                    String fileName = entry.getFileName().toString();
                    if (extensions != null && !extensions.isEmpty()) {
                        boolean match = extensions.stream().anyMatch(fileName::endsWith);
                        if (!match) continue;
                    }

                    totalScanned[0]++;
                    try {
                        List<String> lines = Files.readAllLines(entry);
                        for (int i = 0; i < lines.size() && matches.size() < maxResults; i++) {
                            Matcher m = pattern.matcher(lines.get(i));
                            if (m.find()) {
                                Map<String, Object> matchInfo = new LinkedHashMap<>();
                                matchInfo.put("file", entry.toString());
                                matchInfo.put("relativePath", root.relativize(entry).toString());
                                matchInfo.put("line", i + 1);
                                matchInfo.put("content", lines.get(i).trim());
                                matches.add(matchInfo);
                            }
                        }
                    } catch (IOException ignored) {
                        // Skip files that can't be read
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }

    // ========== 2. explainModule ==========

    @Override
    public Map<String, Object> explainModule(String filePath) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (filePath == null || filePath.isBlank()) {
            result.put("error", "filePath is empty or null");
            return result;
        }

        Path path = Path.of(filePath);
        if (!Files.isRegularFile(path)) {
            result.put("error", "File does not exist: " + filePath);
            return result;
        }

        try {
            String content = Files.readString(path);
            String[] lines = content.split("\n");

            List<String> imports = new ArrayList<>();
            List<String> exports = new ArrayList<>();
            List<String> functions = new ArrayList<>();
            List<String> classes = new ArrayList<>();
            List<String> interfaces = new ArrayList<>();
            List<String> typeAliases = new ArrayList<>();

            for (String line : lines) {
                String trimmed = line.trim();

                // Imports
                if (trimmed.startsWith("import ")) {
                    imports.add(trimmed);
                }

                // Exports
                if (trimmed.startsWith("export ")) {
                    exports.add(trimmed.length() > 80 ? trimmed.substring(0, 80) + "..." : trimmed);
                }

                // Functions
                Matcher funcMatcher = Pattern.compile(
                        "(?:export\\s+)?(?:public\\s+)?(?:static\\s+)?(?:async\\s+)?(?:function\\s+)(\\w+)")
                        .matcher(trimmed);
                if (funcMatcher.find()) {
                    functions.add(funcMatcher.group(1));
                }

                // Arrow functions as const
                Matcher arrowMatcher = Pattern.compile(
                        "(?:export\\s+)?(?:const|let|var)\\s+(\\w+)\\s*=\\s*(?:async\\s+)?\\(").matcher(trimmed);
                if (arrowMatcher.find()) {
                    functions.add(arrowMatcher.group(1));
                }

                // Classes
                Matcher classMatcher = Pattern.compile(
                        "(?:export\\s+)?(?:public\\s+)?(?:abstract\\s+)?class\\s+(\\w+)").matcher(trimmed);
                if (classMatcher.find()) {
                    classes.add(classMatcher.group(1));
                }

                // Interfaces
                Matcher ifaceMatcher = Pattern.compile(
                        "(?:export\\s+)?(?:public\\s+)?interface\\s+(\\w+)").matcher(trimmed);
                if (ifaceMatcher.find()) {
                    interfaces.add(ifaceMatcher.group(1));
                }

                // Type aliases
                Matcher typeMatcher = Pattern.compile(
                        "(?:export\\s+)?type\\s+(\\w+)\\s*=").matcher(trimmed);
                if (typeMatcher.find()) {
                    typeAliases.add(typeMatcher.group(1));
                }
            }

            result.put("filePath", filePath);
            result.put("fileName", path.getFileName().toString());
            result.put("extension", getExtension(path.getFileName().toString()));
            result.put("lineCount", lines.length);
            result.put("imports", imports);
            result.put("exports", exports);
            result.put("functions", functions);
            result.put("classes", classes);
            result.put("interfaces", interfaces);
            result.put("typeAliases", typeAliases);

        } catch (IOException e) {
            result.put("error", "Failed to read file: " + e.getMessage());
            return result;
        }

        saveResult("explain-module", filePath, result);
        return result;
    }

    // ========== 3. architectureMap ==========

    @Override
    public Map<String, Object> architectureMap(String directory, int maxDepth) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (directory == null || directory.isBlank()) {
            result.put("error", "directory is empty or null");
            return result;
        }
        if (maxDepth <= 0) maxDepth = 3;

        Path dirPath = Path.of(directory);
        if (!Files.isDirectory(dirPath)) {
            result.put("error", "Directory does not exist: " + directory);
            return result;
        }

        StringBuilder tree = new StringBuilder();
        Map<String, Integer> extensionCounts = new LinkedHashMap<>();
        int[] totalFiles = {0};

        tree.append(dirPath.getFileName().toString()).append("/\n");
        buildTree(dirPath, "", maxDepth, 0, tree, extensionCounts, totalFiles);

        result.put("directory", directory);
        result.put("tree", tree.toString());
        result.put("totalFiles", totalFiles[0]);
        result.put("extensionCounts", extensionCounts);

        saveResult("architecture-map", directory, result);
        return result;
    }

    private void buildTree(Path dir, String prefix, int maxDepth, int currentDepth,
                            StringBuilder tree, Map<String, Integer> extCounts, int[] totalFiles) {
        if (currentDepth >= maxDepth) return;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            List<Path> entries = new ArrayList<>();
            stream.forEach(entries::add);

            // Sort: directories first, then files
            entries.sort((a, b) -> {
                boolean aDir = Files.isDirectory(a);
                boolean bDir = Files.isDirectory(b);
                if (aDir != bDir) return aDir ? -1 : 1;
                return a.getFileName().toString().compareTo(b.getFileName().toString());
            });

            for (int i = 0; i < entries.size(); i++) {
                Path entry = entries.get(i);
                String name = entry.getFileName().toString();
                boolean isLast = (i == entries.size() - 1);
                String connector = isLast ? "└── " : "├── ";
                String childPrefix = isLast ? "    " : "│   ";

                if (Files.isDirectory(entry)) {
                    if (SKIP_DIRS.contains(name)) continue;
                    tree.append(prefix).append(connector).append(name).append("/\n");
                    buildTree(entry, prefix + childPrefix, maxDepth, currentDepth + 1, tree, extCounts, totalFiles);
                } else {
                    tree.append(prefix).append(connector).append(name).append("\n");
                    totalFiles[0]++;
                    String ext = getExtension(name);
                    if (!ext.isEmpty()) {
                        extCounts.merge(ext, 1, Integer::sum);
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }

    // ========== 4. dependencyGraph ==========

    @Override
    public Map<String, Object> dependencyGraph(String directory) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (directory == null || directory.isBlank()) {
            result.put("error", "directory is empty or null");
            return result;
        }

        Path dirPath = Path.of(directory);
        if (!Files.isDirectory(dirPath)) {
            result.put("error", "Directory does not exist: " + directory);
            return result;
        }

        Map<String, List<String>> adjacencyList = new LinkedHashMap<>();
        List<Path> sourceFiles = new ArrayList<>();
        collectSourceFiles(dirPath, sourceFiles);

        Pattern importPattern = Pattern.compile(
                "(?:import\\s+.*?from\\s+[\"'](\\..*?)[\"']|require\\s*\\(\\s*[\"'](\\..*?)[\"']\\s*\\))");

        for (Path file : sourceFiles) {
            String relPath = dirPath.relativize(file).toString();
            List<String> deps = new ArrayList<>();

            try {
                String content = Files.readString(file);
                Matcher matcher = importPattern.matcher(content);
                while (matcher.find()) {
                    String importPath = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
                    // Resolve relative import
                    Path resolved = file.getParent().resolve(importPath).normalize();
                    String resolvedRel = dirPath.relativize(resolved).toString();
                    deps.add(resolvedRel);
                }
            } catch (IOException ignored) {
            }

            if (!deps.isEmpty()) {
                adjacencyList.put(relPath, deps);
            }
        }

        int totalEdges = adjacencyList.values().stream().mapToInt(List::size).sum();

        // Generate Mermaid diagram
        StringBuilder mermaid = new StringBuilder("graph TD\n");
        for (Map.Entry<String, List<String>> entry : adjacencyList.entrySet()) {
            String from = sanitizeMermaidId(entry.getKey());
            for (String to : entry.getValue()) {
                String toId = sanitizeMermaidId(to);
                mermaid.append("    ").append(from).append(" --> ").append(toId).append("\n");
            }
        }

        result.put("totalFiles", sourceFiles.size());
        result.put("totalDependencyEdges", totalEdges);
        result.put("adjacencyList", adjacencyList);
        result.put("mermaidDiagram", mermaid.toString());

        saveResult("dependency-graph", directory, result);
        return result;
    }

    private void collectSourceFiles(Path dir, List<Path> files) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    if (!SKIP_DIRS.contains(entry.getFileName().toString())) {
                        collectSourceFiles(entry, files);
                    }
                } else {
                    String name = entry.getFileName().toString();
                    String ext = getExtension(name);
                    if (SOURCE_EXTENSIONS.contains(ext)) {
                        files.add(entry);
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }

    // ========== 5. trackChanges ==========

    @Override
    public Map<String, Object> trackChanges(String modulePath, String changeType, String description,
                                              int filesChanged, String author, String commitRef, int historyLimit) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (modulePath == null || modulePath.isBlank()) {
            result.put("error", "modulePath is empty or null");
            return result;
        }

        if (changeType != null && !changeType.isBlank()) {
            // Record a change
            Map<String, Object> change = new LinkedHashMap<>();
            change.put("modulePath", modulePath);
            change.put("changeType", changeType);
            change.put("description", description != null ? description : "");
            change.put("filesChanged", filesChanged);
            change.put("author", author != null ? author : "unknown");
            change.put("commitRef", commitRef != null ? commitRef : "");
            change.put("recordedAt", Instant.now().toString());

            result.put("action", "recorded");
            result.put("change", change);

            saveResult("track-changes", modulePath, result);
        } else {
            // Return history
            if (historyLimit <= 0) historyLimit = 20;
            List<CodebaseKnowledge> history = repository.findByTargetPath(modulePath);
            List<Map<String, Object>> entries = new ArrayList<>();
            int limit = Math.min(historyLimit, history.size());
            for (int i = 0; i < limit; i++) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("analysisType", history.get(i).getAnalysisType());
                entry.put("targetPath", history.get(i).getTargetPath());
                entry.put("createdAt", history.get(i).getCreatedAt().toString());
                entries.add(entry);
            }

            result.put("action", "history");
            result.put("modulePath", modulePath);
            result.put("totalEntries", history.size());
            result.put("entries", entries);
        }

        return result;
    }

    // ========== Helpers ==========

    private String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot) : "";
    }

    private String sanitizeMermaidId(String path) {
        return path.replaceAll("[^a-zA-Z0-9]", "_");
    }

    private void saveResult(String type, String targetPath, Map<String, Object> result) {
        CodebaseKnowledge knowledge = CodebaseKnowledge.builder()
                .id(UUID.randomUUID().toString())
                .analysisType(type)
                .targetPath(targetPath)
                .resultJson(mapToJson(result))
                .createdAt(Instant.now())
                .build();
        repository.save(knowledge);
    }

    private String mapToJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(escapeJson(entry.getKey())).append("\":");
            sb.append(valueToJson(entry.getValue()));
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
            List<?> list = (List<?>) value;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(valueToJson(list.get(i)));
            }
            sb.append("]");
            return sb.toString();
        }
        return "\"" + escapeJson(value.toString()) + "\"";
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
