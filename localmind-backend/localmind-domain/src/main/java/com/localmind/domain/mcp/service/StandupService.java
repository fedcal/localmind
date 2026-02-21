package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.StandupNote;
import com.localmind.domain.mcp.port.in.StandupUseCase;
import com.localmind.domain.mcp.port.out.StandupRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Domain service implementing the standup-notes tool group.
 * Provides standup logging, history retrieval and status report generation.
 * No Spring annotations — registered as a bean via DomainConfig.
 */
public class StandupService implements StandupUseCase {

    private final StandupRepository repository;

    public StandupService(StandupRepository repository) {
        this.repository = repository;
    }

    // ========== 1. logStandup ==========

    @Override
    public Map<String, Object> logStandup(String yesterday, String today, String blockers) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (yesterday == null || yesterday.isBlank()) {
            result.put("error", "Yesterday field is required");
            return result;
        }

        if (today == null || today.isBlank()) {
            result.put("error", "Today field is required");
            return result;
        }

        String standupDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        StandupNote note = StandupNote.builder()
                .id(UUID.randomUUID().toString())
                .yesterday(yesterday.trim())
                .today(today.trim())
                .blockers(blockers != null && !blockers.isBlank() ? blockers.trim() : null)
                .standupDate(standupDate)
                .createdAt(Instant.now())
                .build();

        StandupNote saved = repository.save(note);

        result.put("id", saved.getId());
        result.put("yesterday", saved.getYesterday());
        result.put("today", saved.getToday());
        result.put("blockers", saved.getBlockers());
        result.put("date", saved.getStandupDate());

        // Count total items for today
        List<StandupNote> allNotes = repository.findAll();
        long todayCount = allNotes.stream()
                .filter(n -> standupDate.equals(n.getStandupDate()))
                .count();
        result.put("itemCount", (int) todayCount);

        return result;
    }

    // ========== 2. getHistory ==========

    @Override
    public Map<String, Object> getHistory(int days) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (days <= 0) {
            result.put("error", "Days must be greater than zero");
            return result;
        }

        LocalDate cutoffDate = LocalDate.now().minusDays(days);
        List<StandupNote> allNotes = repository.findAll();
        List<Map<String, Object>> filteredStandups = new ArrayList<>();

        for (StandupNote note : allNotes) {
            if (isDateAfterOrEqual(note.getStandupDate(), cutoffDate)) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("id", note.getId());
                entry.put("yesterday", note.getYesterday());
                entry.put("today", note.getToday());
                entry.put("blockers", note.getBlockers());
                entry.put("date", note.getStandupDate());
                filteredStandups.add(entry);
            }
        }

        result.put("days", days);
        result.put("count", filteredStandups.size());
        result.put("standups", filteredStandups);

        return result;
    }

    // ========== 3. generateStatusReport ==========

    @Override
    public Map<String, Object> generateStatusReport(int days) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (days <= 0) {
            result.put("error", "Days must be greater than zero");
            return result;
        }

        LocalDate cutoffDate = LocalDate.now().minusDays(days);
        List<StandupNote> allNotes = repository.findAll();
        List<StandupNote> filtered = new ArrayList<>();

        for (StandupNote note : allNotes) {
            if (isDateAfterOrEqual(note.getStandupDate(), cutoffDate)) {
                filtered.add(note);
            }
        }

        int blockerCount = 0;
        Set<String> themes = new LinkedHashSet<>();

        for (StandupNote note : filtered) {
            if (note.getBlockers() != null && !note.getBlockers().isBlank()) {
                blockerCount++;
            }
            // Extract themes from today's work descriptions
            extractThemes(note.getToday(), themes);
            extractThemes(note.getYesterday(), themes);
        }

        result.put("period", days + " days");
        result.put("totalStandups", filtered.size());
        result.put("blockerCount", blockerCount);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("themes", new ArrayList<>(themes));
        summary.put("blockerRatio", filtered.isEmpty() ? 0.0
                : Math.round((double) blockerCount / filtered.size() * 100.0) / 100.0);
        summary.put("averagePerDay", days > 0
                ? Math.round((double) filtered.size() / days * 100.0) / 100.0 : 0.0);
        result.put("summary", summary);

        return result;
    }

    // ========== Helper Methods ==========

    /**
     * Checks if a date string (YYYY-MM-DD) is on or after the cutoff date.
     */
    boolean isDateAfterOrEqual(String dateStr, LocalDate cutoffDate) {
        if (dateStr == null || dateStr.isBlank()) {
            return false;
        }
        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            return !date.isBefore(cutoffDate);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Extracts simple themes from text by splitting into words and picking
     * longer keywords (4+ chars) as potential themes.
     * Limited to first 5 themes to keep output manageable.
     */
    void extractThemes(String text, Set<String> themes) {
        if (text == null || text.isBlank()) {
            return;
        }
        String[] words = text.toLowerCase().split("[\\s,;.!?]+");
        for (String word : words) {
            String cleaned = word.trim();
            if (cleaned.length() >= 4 && !isStopWord(cleaned) && themes.size() < 10) {
                themes.add(cleaned);
            }
        }
    }

    /**
     * Returns true if the word is a common stop word that should not be treated as a theme.
     */
    private boolean isStopWord(String word) {
        Set<String> stopWords = Set.of(
                "with", "from", "that", "this", "have", "been", "were", "will",
                "they", "them", "then", "than", "also", "some", "what", "when",
                "each", "made", "like", "more", "most", "only", "into", "over",
                "such", "very", "just", "about", "after", "before", "other"
        );
        return stopWords.contains(word);
    }
}
