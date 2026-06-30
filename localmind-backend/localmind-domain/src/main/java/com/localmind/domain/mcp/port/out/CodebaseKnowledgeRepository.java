package com.localmind.domain.mcp.port.out;

import com.localmind.domain.mcp.model.CodebaseKnowledge;

import java.util.List;

/**
 * Output port for persisting codebase knowledge analysis results.
 */
public interface CodebaseKnowledgeRepository {

    CodebaseKnowledge save(CodebaseKnowledge knowledge);

    List<CodebaseKnowledge> findByTargetPath(String targetPath);
}
