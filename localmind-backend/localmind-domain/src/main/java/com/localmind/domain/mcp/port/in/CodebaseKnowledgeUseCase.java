package com.localmind.domain.mcp.port.in;

import java.util.List;
import java.util.Map;

/**
 * Input port for the codebase-knowledge tool group.
 * Provides code search, module analysis, architecture mapping, dependency graphing and change tracking.
 */
public interface CodebaseKnowledgeUseCase {

    Map<String, Object> searchCode(String directory, String pattern, List<String> fileExtensions, int maxResults);

    Map<String, Object> explainModule(String filePath);

    Map<String, Object> architectureMap(String directory, int maxDepth);

    Map<String, Object> dependencyGraph(String directory);

    Map<String, Object> trackChanges(String modulePath, String changeType, String description,
                                      int filesChanged, String author, String commitRef, int historyLimit);
}
