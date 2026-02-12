package com.localmind.domain.mcp.port.out;

import com.localmind.domain.mcp.model.CodeSnippet;

import java.util.List;
import java.util.Optional;

public interface SnippetRepository {

    CodeSnippet save(CodeSnippet snippet);

    Optional<CodeSnippet> findById(String id);

    List<CodeSnippet> findAll();

    List<CodeSnippet> searchByKeyword(String keyword);

    List<CodeSnippet> findByTag(String tag);

    List<CodeSnippet> findByLanguage(String language);

    boolean deleteById(String id);
}
