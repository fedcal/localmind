package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.ActionItem;
import com.localmind.domain.mcp.model.RetroItem;
import com.localmind.domain.mcp.model.Retrospective;
import com.localmind.domain.mcp.port.in.RetrospectiveUseCase;
import com.localmind.domain.mcp.port.out.RetrospectiveRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Domain service implementing the retrospective-manager tool group.
 * Provides retrospective creation, item management, voting, action item generation,
 * pattern detection, and smart suggestions.
 * No Spring annotations - registered as a bean via DomainConfig.
 */
public class RetrospectiveService implements RetrospectiveUseCase {

    private final RetrospectiveRepository repository;

    /**
     * Valid retrospective formats with their categories.
     */
    private static final Map<String, List<String>> FORMAT_CATEGORIES;

    /**
     * Common suggestion templates grouped by theme.
     */
    private static final List<Map<String, String>> SUGGESTION_TEMPLATES;

    /**
     * Stop words to exclude from pattern detection.
     */
    private static final Set<String> STOP_WORDS = Set.of(
            "the", "a", "an", "is", "are", "was", "were", "be", "been", "being",
            "have", "has", "had", "do", "does", "did", "will", "would", "could",
            "should", "may", "might", "shall", "can", "to", "of", "in", "for",
            "on", "with", "at", "by", "from", "as", "into", "about", "between",
            "through", "after", "before", "during", "above", "below", "and", "or",
            "but", "not", "no", "nor", "so", "yet", "both", "either", "neither",
            "each", "every", "all", "any", "few", "more", "most", "other", "some",
            "such", "than", "too", "very", "just", "also", "this", "that", "these",
            "those", "it", "its", "we", "our", "they", "their", "i", "my", "me",
            "il", "lo", "la", "le", "gli", "un", "una", "uno", "di", "da", "del",
            "della", "dei", "delle", "che", "non", "per", "con", "su", "tra",
            "fra", "sono", "e", "o", "ma", "se", "come", "anche", "si", "ci"
    );

    static {
        FORMAT_CATEGORIES = new LinkedHashMap<>();
        FORMAT_CATEGORIES.put("mad-sad-glad", List.of("mad", "sad", "glad"));
        FORMAT_CATEGORIES.put("4ls", List.of("liked", "learned", "lacked", "longed-for"));
        FORMAT_CATEGORIES.put("start-stop-continue", List.of("start", "stop", "continue"));

        SUGGESTION_TEMPLATES = new ArrayList<>();

        // Performance-related suggestions
        SUGGESTION_TEMPLATES.add(buildSuggestion("performance",
                "Build times are too long, consider parallelizing CI steps",
                "start"));
        SUGGESTION_TEMPLATES.add(buildSuggestion("performance",
                "Application response time degraded during peak hours",
                "mad"));
        SUGGESTION_TEMPLATES.add(buildSuggestion("performance",
                "Database queries need optimization for better throughput",
                "stop"));

        // Communication-related suggestions
        SUGGESTION_TEMPLATES.add(buildSuggestion("communication",
                "Daily standup meetings are running too long",
                "stop"));
        SUGGESTION_TEMPLATES.add(buildSuggestion("communication",
                "Great collaboration between frontend and backend teams",
                "glad"));
        SUGGESTION_TEMPLATES.add(buildSuggestion("communication",
                "Lack of documentation for new features makes onboarding difficult",
                "lacked"));

        // Process-related suggestions
        SUGGESTION_TEMPLATES.add(buildSuggestion("process",
                "Code reviews are thorough and catching bugs early",
                "continue"));
        SUGGESTION_TEMPLATES.add(buildSuggestion("process",
                "Sprint planning sessions need better estimation techniques",
                "start"));
        SUGGESTION_TEMPLATES.add(buildSuggestion("process",
                "Too many meetings interrupt deep work time",
                "mad"));

        // Quality-related suggestions
        SUGGESTION_TEMPLATES.add(buildSuggestion("quality",
                "Test coverage improved significantly this sprint",
                "liked"));
        SUGGESTION_TEMPLATES.add(buildSuggestion("quality",
                "Production bugs increased due to missing integration tests",
                "sad"));
        SUGGESTION_TEMPLATES.add(buildSuggestion("quality",
                "Learned about property-based testing for edge case detection",
                "learned"));

        // Team-related suggestions
        SUGGESTION_TEMPLATES.add(buildSuggestion("team",
                "Pair programming sessions helped share domain knowledge",
                "glad"));
        SUGGESTION_TEMPLATES.add(buildSuggestion("team",
                "Team morale is low due to constant deadline pressure",
                "sad"));
        SUGGESTION_TEMPLATES.add(buildSuggestion("team",
                "Need more cross-functional training opportunities",
                "longed-for"));

        // Technical debt suggestions
        SUGGESTION_TEMPLATES.add(buildSuggestion("technical-debt",
                "Legacy code refactoring is progressing well",
                "continue"));
        SUGGESTION_TEMPLATES.add(buildSuggestion("technical-debt",
                "Dependency updates are being postponed too often",
                "stop"));
        SUGGESTION_TEMPLATES.add(buildSuggestion("technical-debt",
                "Allocate dedicated time each sprint for tech debt reduction",
                "start"));
    }

    public RetrospectiveService(RetrospectiveRepository repository) {
        this.repository = repository;
    }

    // ========== 1. createRetro ==========

    @Override
    public Map<String, Object> createRetro(String sprintId, String format) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (sprintId == null || sprintId.isBlank()) {
            result.put("error", "Sprint ID is required");
            return result;
        }

        String normalizedFormat = (format == null || format.isBlank()) ? "mad-sad-glad" : format.trim().toLowerCase();

        if (!FORMAT_CATEGORIES.containsKey(normalizedFormat)) {
            result.put("error", "Invalid format: " + format + ". Valid formats: " +
                    String.join(", ", FORMAT_CATEGORIES.keySet()));
            return result;
        }

        Retrospective retro = Retrospective.builder()
                .id(UUID.randomUUID().toString())
                .sprintId(sprintId.trim())
                .format(normalizedFormat)
                .createdAt(Instant.now())
                .build();

        Retrospective saved = repository.saveRetro(retro);

        result.put("id", saved.getId());
        result.put("sprintId", saved.getSprintId());
        result.put("format", saved.getFormat());
        result.put("categories", FORMAT_CATEGORIES.get(normalizedFormat));
        result.put("createdAt", saved.getCreatedAt().toString());

        return result;
    }

    // ========== 2. addRetroItem ==========

    @Override
    public Map<String, Object> addRetroItem(String retroId, String category, String content) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (retroId == null || retroId.isBlank()) {
            result.put("error", "Retro ID is required");
            return result;
        }

        if (category == null || category.isBlank()) {
            result.put("error", "Category is required");
            return result;
        }

        if (content == null || content.isBlank()) {
            result.put("error", "Content is required");
            return result;
        }

        Optional<Retrospective> retroOpt = repository.findRetroById(retroId.trim());
        if (retroOpt.isEmpty()) {
            result.put("error", "Retrospective not found: " + retroId);
            return result;
        }

        Retrospective retro = retroOpt.get();
        List<String> validCategories = FORMAT_CATEGORIES.get(retro.getFormat());
        String normalizedCategory = category.trim().toLowerCase();

        if (validCategories != null && !validCategories.contains(normalizedCategory)) {
            result.put("error", "Invalid category '" + category + "' for format '" + retro.getFormat()
                    + "'. Valid categories: " + String.join(", ", validCategories));
            return result;
        }

        RetroItem item = RetroItem.builder()
                .id(UUID.randomUUID().toString())
                .retroId(retroId.trim())
                .category(normalizedCategory)
                .content(content.trim())
                .votes(0)
                .createdAt(Instant.now())
                .build();

        RetroItem saved = repository.saveItem(item);

        result.put("id", saved.getId());
        result.put("retroId", saved.getRetroId());
        result.put("category", saved.getCategory());
        result.put("content", saved.getContent());
        result.put("votes", saved.getVotes());
        result.put("createdAt", saved.getCreatedAt().toString());

        return result;
    }

    // ========== 3. voteRetroItem ==========

    @Override
    public Map<String, Object> voteRetroItem(String itemId) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (itemId == null || itemId.isBlank()) {
            result.put("error", "Item ID is required");
            return result;
        }

        Optional<RetroItem> itemOpt = repository.findItemById(itemId.trim());
        if (itemOpt.isEmpty()) {
            result.put("error", "Retro item not found: " + itemId);
            return result;
        }

        RetroItem existing = itemOpt.get();
        RetroItem updated = RetroItem.builder()
                .id(existing.getId())
                .retroId(existing.getRetroId())
                .category(existing.getCategory())
                .content(existing.getContent())
                .votes(existing.getVotes() + 1)
                .createdAt(existing.getCreatedAt())
                .build();

        RetroItem saved = repository.saveItem(updated);

        result.put("id", saved.getId());
        result.put("retroId", saved.getRetroId());
        result.put("category", saved.getCategory());
        result.put("content", saved.getContent());
        result.put("votes", saved.getVotes());
        result.put("previousVotes", existing.getVotes());

        return result;
    }

    // ========== 4. generateActionItems ==========

    @Override
    public Map<String, Object> generateActionItems(String retroId, int topN) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (retroId == null || retroId.isBlank()) {
            result.put("error", "Retro ID is required");
            return result;
        }

        if (topN <= 0) {
            result.put("error", "topN must be greater than 0");
            return result;
        }

        Optional<Retrospective> retroOpt = repository.findRetroById(retroId.trim());
        if (retroOpt.isEmpty()) {
            result.put("error", "Retrospective not found: " + retroId);
            return result;
        }

        List<RetroItem> items = repository.findItemsByRetroId(retroId.trim());
        if (items.isEmpty()) {
            result.put("error", "No items found for retrospective: " + retroId);
            return result;
        }

        // Sort by votes descending and take topN
        List<RetroItem> topItems = items.stream()
                .sorted(Comparator.comparingInt(RetroItem::getVotes).reversed())
                .limit(topN)
                .toList();

        List<Map<String, Object>> actionItemResults = new ArrayList<>();

        for (RetroItem item : topItems) {
            String description = generateActionDescription(item);
            String assignee = "team";

            ActionItem actionItem = ActionItem.builder()
                    .id(UUID.randomUUID().toString())
                    .retroId(retroId.trim())
                    .description(description)
                    .assignee(assignee)
                    .dueDate("next-sprint")
                    .status("pending")
                    .createdAt(Instant.now())
                    .build();

            ActionItem saved = repository.saveActionItem(actionItem);

            Map<String, Object> actionMap = new LinkedHashMap<>();
            actionMap.put("id", saved.getId());
            actionMap.put("description", saved.getDescription());
            actionMap.put("assignee", saved.getAssignee());
            actionMap.put("dueDate", saved.getDueDate());
            actionMap.put("status", saved.getStatus());
            actionMap.put("sourceItem", item.getContent());
            actionMap.put("sourceVotes", item.getVotes());
            actionItemResults.add(actionMap);
        }

        result.put("retroId", retroId);
        result.put("actionItemCount", actionItemResults.size());
        result.put("actionItems", actionItemResults);

        return result;
    }

    // ========== 5. getRetro ==========

    @Override
    public Map<String, Object> getRetro(String retroId) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (retroId == null || retroId.isBlank()) {
            result.put("error", "Retro ID is required");
            return result;
        }

        Optional<Retrospective> retroOpt = repository.findRetroById(retroId.trim());
        if (retroOpt.isEmpty()) {
            result.put("error", "Retrospective not found: " + retroId);
            return result;
        }

        Retrospective retro = retroOpt.get();
        List<RetroItem> items = repository.findItemsByRetroId(retroId.trim());
        List<ActionItem> actionItems = repository.findActionItemsByRetroId(retroId.trim());

        result.put("id", retro.getId());
        result.put("sprintId", retro.getSprintId());
        result.put("format", retro.getFormat());
        result.put("categories", FORMAT_CATEGORIES.get(retro.getFormat()));
        result.put("createdAt", retro.getCreatedAt().toString());

        // Group items by category
        Map<String, List<Map<String, Object>>> groupedItems = new LinkedHashMap<>();
        List<String> categories = FORMAT_CATEGORIES.getOrDefault(retro.getFormat(), List.of());
        for (String cat : categories) {
            groupedItems.put(cat, new ArrayList<>());
        }

        for (RetroItem item : items) {
            Map<String, Object> itemMap = new LinkedHashMap<>();
            itemMap.put("id", item.getId());
            itemMap.put("category", item.getCategory());
            itemMap.put("content", item.getContent());
            itemMap.put("votes", item.getVotes());
            itemMap.put("createdAt", item.getCreatedAt().toString());

            groupedItems.computeIfAbsent(item.getCategory(), k -> new ArrayList<>()).add(itemMap);
        }

        result.put("items", groupedItems);
        result.put("itemCount", items.size());

        // Action items
        List<Map<String, Object>> actionItemList = new ArrayList<>();
        for (ActionItem ai : actionItems) {
            Map<String, Object> aiMap = new LinkedHashMap<>();
            aiMap.put("id", ai.getId());
            aiMap.put("description", ai.getDescription());
            aiMap.put("assignee", ai.getAssignee());
            aiMap.put("dueDate", ai.getDueDate());
            aiMap.put("status", ai.getStatus());
            aiMap.put("createdAt", ai.getCreatedAt().toString());
            actionItemList.add(aiMap);
        }

        result.put("actionItems", actionItemList);
        result.put("actionItemCount", actionItemList.size());

        return result;
    }

    // ========== 6. detectPatterns ==========

    @Override
    public Map<String, Object> detectPatterns() {
        Map<String, Object> result = new LinkedHashMap<>();

        List<RetroItem> allItems = repository.findAllItems();

        if (allItems.isEmpty()) {
            result.put("patternCount", 0);
            result.put("patterns", new ArrayList<>());
            result.put("message", "No retrospective items found for analysis");
            return result;
        }

        // Count word occurrences (excluding stop words) across all items
        Map<String, Integer> wordFrequency = new LinkedHashMap<>();
        Map<String, List<String>> wordCategories = new LinkedHashMap<>();

        for (RetroItem item : allItems) {
            String[] words = item.getContent().toLowerCase()
                    .replaceAll("[^a-zA-Z0-9àèéìòùÀÈÉÌÒÙ\\s-]", "")
                    .split("\\s+");

            for (String word : words) {
                String trimmedWord = word.trim();
                if (trimmedWord.length() < 3 || STOP_WORDS.contains(trimmedWord)) {
                    continue;
                }
                wordFrequency.merge(trimmedWord, 1, Integer::sum);
                wordCategories.computeIfAbsent(trimmedWord, k -> new ArrayList<>())
                        .add(item.getCategory());
            }
        }

        // Find the top patterns (words appearing in multiple items)
        List<Map<String, Object>> patterns = wordFrequency.entrySet().stream()
                .filter(entry -> entry.getValue() >= 2)
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(20)
                .map(entry -> {
                    Map<String, Object> pattern = new LinkedHashMap<>();
                    pattern.put("word", entry.getKey());
                    pattern.put("occurrences", entry.getValue());

                    // Count categories for this word
                    List<String> cats = wordCategories.getOrDefault(entry.getKey(), List.of());
                    Map<String, Long> catCounts = cats.stream()
                            .collect(Collectors.groupingBy(c -> c, Collectors.counting()));
                    pattern.put("categories", catCounts);

                    // Determine sentiment based on prevalent category
                    pattern.put("sentiment", determineSentiment(catCounts));

                    return pattern;
                })
                .collect(Collectors.toList());

        // Identify recurring themes
        List<Map<String, Object>> themes = identifyThemes(allItems);

        result.put("totalItemsAnalyzed", allItems.size());
        result.put("patternCount", patterns.size());
        result.put("patterns", patterns);
        result.put("themes", themes);

        return result;
    }

    // ========== 7. suggestItems ==========

    @Override
    public Map<String, Object> suggestItems(int limit) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (limit <= 0) {
            result.put("error", "Limit must be greater than 0");
            return result;
        }

        int effectiveLimit = Math.min(limit, SUGGESTION_TEMPLATES.size());

        // Collect existing item contents to avoid duplicate suggestions
        List<RetroItem> existingItems = repository.findAllItems();
        Set<String> existingContents = existingItems.stream()
                .map(item -> item.getContent().toLowerCase())
                .collect(Collectors.toSet());

        List<Map<String, Object>> suggestions = new ArrayList<>();
        int index = 0;

        for (Map<String, String> template : SUGGESTION_TEMPLATES) {
            if (suggestions.size() >= effectiveLimit) {
                break;
            }

            String content = template.get("content");
            // Skip if a very similar item already exists
            if (existingContents.contains(content.toLowerCase())) {
                continue;
            }

            Map<String, Object> suggestion = new LinkedHashMap<>();
            suggestion.put("index", index++);
            suggestion.put("theme", template.get("theme"));
            suggestion.put("suggestedContent", content);
            suggestion.put("suggestedCategory", template.get("category"));
            suggestions.add(suggestion);
        }

        result.put("suggestionCount", suggestions.size());
        result.put("suggestions", suggestions);

        return result;
    }

    // ========== Helper Methods ==========

    /**
     * Generates an action item description from a retro item.
     */
    String generateActionDescription(RetroItem item) {
        String category = item.getCategory();
        String content = item.getContent();

        return switch (category) {
            case "mad", "sad", "stop" ->
                    "Address issue: " + content + " - Create improvement plan";
            case "glad", "liked", "continue" ->
                    "Reinforce practice: " + content + " - Document and share";
            case "start", "longed-for", "lacked" ->
                    "Implement: " + content + " - Define steps and timeline";
            case "learned" ->
                    "Apply learning: " + content + " - Share with team";
            default ->
                    "Action: " + content;
        };
    }

    /**
     * Determines a sentiment label based on category distribution.
     */
    private String determineSentiment(Map<String, Long> catCounts) {
        Set<String> negativeCats = Set.of("mad", "sad", "stop", "lacked");
        Set<String> positiveCats = Set.of("glad", "liked", "continue", "learned");

        long negative = catCounts.entrySet().stream()
                .filter(e -> negativeCats.contains(e.getKey()))
                .mapToLong(Map.Entry::getValue)
                .sum();

        long positive = catCounts.entrySet().stream()
                .filter(e -> positiveCats.contains(e.getKey()))
                .mapToLong(Map.Entry::getValue)
                .sum();

        if (negative > positive) {
            return "negative";
        } else if (positive > negative) {
            return "positive";
        }
        return "neutral";
    }

    /**
     * Identifies recurring themes from retrospective items.
     */
    private List<Map<String, Object>> identifyThemes(List<RetroItem> items) {
        List<Map<String, Object>> themes = new ArrayList<>();

        Map<String, List<RetroItem>> byCategory = items.stream()
                .collect(Collectors.groupingBy(RetroItem::getCategory));

        for (Map.Entry<String, List<RetroItem>> entry : byCategory.entrySet()) {
            if (entry.getValue().size() >= 2) {
                Map<String, Object> theme = new LinkedHashMap<>();
                theme.put("category", entry.getKey());
                theme.put("itemCount", entry.getValue().size());
                theme.put("totalVotes", entry.getValue().stream()
                        .mapToInt(RetroItem::getVotes).sum());
                theme.put("avgVotes", entry.getValue().stream()
                        .mapToInt(RetroItem::getVotes).average().orElse(0.0));
                themes.add(theme);
            }
        }

        // Sort by total votes descending
        themes.sort((a, b) -> Integer.compare(
                (int) b.get("totalVotes"), (int) a.get("totalVotes")));

        return themes;
    }

    private static Map<String, String> buildSuggestion(String theme, String content, String category) {
        Map<String, String> suggestion = new LinkedHashMap<>();
        suggestion.put("theme", theme);
        suggestion.put("content", content);
        suggestion.put("category", category);
        return suggestion;
    }

    // ========== JSON Serialization (minimal, no external deps) ==========

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
        if (value instanceof Set) {
            return valueToJson(new ArrayList<>((Set<?>) value));
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
