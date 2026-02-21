package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.CodeSnippet;
import com.localmind.domain.mcp.port.in.SnippetUseCase;
import com.localmind.domain.mcp.port.out.SnippetRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class SnippetService implements SnippetUseCase {

    private final SnippetRepository snippetRepository;

    public SnippetService(SnippetRepository snippetRepository) {
        this.snippetRepository = snippetRepository;
    }

    @Override
    public CodeSnippet save(String title, String code, String language, String description, Set<String> tags) {
        Instant now = Instant.now();
        CodeSnippet snippet = CodeSnippet.builder()
                .id(UUID.randomUUID().toString())
                .title(title)
                .code(code)
                .language(language)
                .description(description)
                .tags(tags != null ? tags : new HashSet<>())
                .createdAt(now)
                .updatedAt(now)
                .build();
        return snippetRepository.save(snippet);
    }

    @Override
    public List<CodeSnippet> search(String keyword, String tag, String language) {
        List<Set<String>> matchingSets = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            Set<String> ids = snippetRepository.searchByKeyword(keyword).stream()
                    .map(CodeSnippet::getId)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            matchingSets.add(ids);
        }

        if (tag != null && !tag.isBlank()) {
            Set<String> ids = snippetRepository.findByTag(tag).stream()
                    .map(CodeSnippet::getId)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            matchingSets.add(ids);
        }

        if (language != null && !language.isBlank()) {
            Set<String> ids = snippetRepository.findByLanguage(language).stream()
                    .map(CodeSnippet::getId)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            matchingSets.add(ids);
        }

        if (matchingSets.isEmpty()) {
            return snippetRepository.findAll();
        }

        Set<String> intersectedIds = matchingSets.get(0);
        for (int i = 1; i < matchingSets.size(); i++) {
            intersectedIds.retainAll(matchingSets.get(i));
        }

        Set<String> finalIds = intersectedIds;
        return snippetRepository.findAll().stream()
                .filter(s -> finalIds.contains(s.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CodeSnippet> getById(String id) {
        return snippetRepository.findById(id);
    }

    @Override
    public boolean delete(String id) {
        return snippetRepository.deleteById(id);
    }

    @Override
    public List<Map<String, Object>> listTags() {
        List<CodeSnippet> allSnippets = snippetRepository.findAll();
        Map<String, Long> tagCounts = new HashMap<>();

        for (CodeSnippet snippet : allSnippets) {
            if (snippet.getTags() != null) {
                for (String tag : snippet.getTags()) {
                    tagCounts.merge(tag, 1L, Long::sum);
                }
            }
        }

        return tagCounts.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("tag", entry.getKey());
                    map.put("count", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());
    }
}
