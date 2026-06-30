package com.localmind.domain.mcp.port.in;

import com.localmind.domain.mcp.model.CodeSnippet;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface SnippetUseCase {

    CodeSnippet save(String title, String code, String language, String description, Set<String> tags);

    List<CodeSnippet> search(String keyword, String tag, String language);

    Optional<CodeSnippet> getById(String id);

    boolean delete(String id);

    List<Map<String, Object>> listTags();
}
