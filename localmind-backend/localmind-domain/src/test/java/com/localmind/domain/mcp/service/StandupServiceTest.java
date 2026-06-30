package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.StandupNote;
import com.localmind.domain.mcp.port.out.StandupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StandupServiceTest {

    @Mock
    private StandupRepository repository;

    private StandupService service;

    private final String TODAY = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

    @BeforeEach
    void setUp() {
        service = new StandupService(repository);
        when(repository.save(any(StandupNote.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.findAll()).thenReturn(Collections.emptyList());
    }

    // ========== logStandup Tests ==========

    @Test
    void logStandup_withValidInput_savesAndReturnsResult() {
        Map<String, Object> result = service.logStandup("Fixed bug #123", "Work on feature X", "Blocked by API");

        assertThat(result.get("id")).isNotNull();
        assertThat(result.get("yesterday")).isEqualTo("Fixed bug #123");
        assertThat(result.get("today")).isEqualTo("Work on feature X");
        assertThat(result.get("blockers")).isEqualTo("Blocked by API");
        assertThat(result.get("date")).isEqualTo(TODAY);
    }

    @Test
    void logStandup_withNullBlockers_savesWithNullBlockers() {
        Map<String, Object> result = service.logStandup("Did X", "Will do Y", null);

        assertThat(result.get("blockers")).isNull();
        assertThat(result.get("yesterday")).isEqualTo("Did X");
        assertThat(result.get("today")).isEqualTo("Will do Y");
    }

    @Test
    void logStandup_withBlankBlockers_savesWithNullBlockers() {
        Map<String, Object> result = service.logStandup("Did X", "Will do Y", "   ");

        assertThat(result.get("blockers")).isNull();
    }

    @Test
    void logStandup_trimsWhitespace() {
        Map<String, Object> result = service.logStandup("  Did X  ", "  Will do Y  ", "  Blocked  ");

        assertThat(result.get("yesterday")).isEqualTo("Did X");
        assertThat(result.get("today")).isEqualTo("Will do Y");
        assertThat(result.get("blockers")).isEqualTo("Blocked");
    }

    @Test
    void logStandup_countsItemsForToday() {
        StandupNote existing = StandupNote.builder()
                .id("existing-id")
                .yesterday("prev")
                .today("current")
                .standupDate(TODAY)
                .createdAt(Instant.now())
                .build();
        when(repository.findAll()).thenReturn(List.of(existing));

        // The saved note also has today's date, plus existing one = 2 items for today
        // But the save returns the argument (which also has today's date),
        // so findAll returns 1 existing + the service counts matching dates
        Map<String, Object> result = service.logStandup("Did X", "Will do Y", null);

        // The existing note + newly saved one (already in list as saved) = 1 existing
        // The newly saved note is not in findAll because the mock returns fixed list
        assertThat(result.get("itemCount")).isEqualTo(1);
    }

    @Test
    void logStandup_withNullYesterday_returnsError() {
        Map<String, Object> result = service.logStandup(null, "Will do Y", null);

        assertThat(result.get("error")).isEqualTo("Yesterday field is required");
        verify(repository, never()).save(any());
    }

    @Test
    void logStandup_withBlankYesterday_returnsError() {
        Map<String, Object> result = service.logStandup("   ", "Will do Y", null);

        assertThat(result.get("error")).isEqualTo("Yesterday field is required");
        verify(repository, never()).save(any());
    }

    @Test
    void logStandup_withNullToday_returnsError() {
        Map<String, Object> result = service.logStandup("Did X", null, null);

        assertThat(result.get("error")).isEqualTo("Today field is required");
        verify(repository, never()).save(any());
    }

    @Test
    void logStandup_withBlankToday_returnsError() {
        Map<String, Object> result = service.logStandup("Did X", "  ", null);

        assertThat(result.get("error")).isEqualTo("Today field is required");
        verify(repository, never()).save(any());
    }

    @Test
    void logStandup_savesToRepository() {
        service.logStandup("Did X", "Will do Y", null);

        ArgumentCaptor<StandupNote> captor = ArgumentCaptor.forClass(StandupNote.class);
        verify(repository).save(captor.capture());
        StandupNote saved = captor.getValue();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getYesterday()).isEqualTo("Did X");
        assertThat(saved.getToday()).isEqualTo("Will do Y");
        assertThat(saved.getStandupDate()).isEqualTo(TODAY);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    // ========== getHistory Tests ==========

    @Test
    @SuppressWarnings("unchecked")
    void getHistory_filtersLastNDays() {
        String todayStr = TODAY;
        String oldDateStr = LocalDate.now().minusDays(10).format(DateTimeFormatter.ISO_LOCAL_DATE);

        List<StandupNote> notes = List.of(
                buildNote("1", "Y1", "T1", null, todayStr),
                buildNote("2", "Y2", "T2", null, oldDateStr)
        );
        when(repository.findAll()).thenReturn(notes);

        Map<String, Object> result = service.getHistory(5);

        assertThat(result.get("days")).isEqualTo(5);
        List<Map<String, Object>> standups = (List<Map<String, Object>>) result.get("standups");
        assertThat(standups).hasSize(1);
        assertThat(standups.get(0).get("id")).isEqualTo("1");
    }

    @Test
    @SuppressWarnings("unchecked")
    void getHistory_returnsAllWithinRange() {
        String date1 = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String date2 = LocalDate.now().minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE);

        List<StandupNote> notes = List.of(
                buildNote("1", "Y1", "T1", null, date1),
                buildNote("2", "Y2", "T2", "Blocker", date2)
        );
        when(repository.findAll()).thenReturn(notes);

        Map<String, Object> result = service.getHistory(7);

        assertThat(result.get("count")).isEqualTo(2);
        List<Map<String, Object>> standups = (List<Map<String, Object>>) result.get("standups");
        assertThat(standups).hasSize(2);
        assertThat(standups.get(1).get("blockers")).isEqualTo("Blocker");
    }

    @Test
    void getHistory_withZeroDays_returnsError() {
        Map<String, Object> result = service.getHistory(0);

        assertThat(result.get("error")).isEqualTo("Days must be greater than zero");
    }

    @Test
    void getHistory_withNegativeDays_returnsError() {
        Map<String, Object> result = service.getHistory(-1);

        assertThat(result.get("error")).isEqualTo("Days must be greater than zero");
    }

    @Test
    @SuppressWarnings("unchecked")
    void getHistory_withNoStandups_returnsEmptyList() {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        Map<String, Object> result = service.getHistory(7);

        assertThat(result.get("count")).isEqualTo(0);
        List<Map<String, Object>> standups = (List<Map<String, Object>>) result.get("standups");
        assertThat(standups).isEmpty();
    }

    // ========== generateStatusReport Tests ==========

    @Test
    @SuppressWarnings("unchecked")
    void generateStatusReport_aggregatesCorrectly() {
        String date1 = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String date2 = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);

        List<StandupNote> notes = List.of(
                buildNote("1", "Fixed authentication bug", "Working on deployment", "API timeout", date1),
                buildNote("2", "Reviewed pull requests", "Testing integration", null, date2)
        );
        when(repository.findAll()).thenReturn(notes);

        Map<String, Object> result = service.generateStatusReport(7);

        assertThat(result.get("period")).isEqualTo("7 days");
        assertThat(result.get("totalStandups")).isEqualTo(2);
        assertThat(result.get("blockerCount")).isEqualTo(1);

        Map<String, Object> summary = (Map<String, Object>) result.get("summary");
        assertThat(summary).isNotNull();
        assertThat((List<String>) summary.get("themes")).isNotEmpty();
        assertThat((double) summary.get("blockerRatio")).isEqualTo(0.5);
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateStatusReport_withNoBlockers_countsZero() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        List<StandupNote> notes = List.of(
                buildNote("1", "Did work", "More work", null, dateStr)
        );
        when(repository.findAll()).thenReturn(notes);

        Map<String, Object> result = service.generateStatusReport(7);

        assertThat(result.get("blockerCount")).isEqualTo(0);
        Map<String, Object> summary = (Map<String, Object>) result.get("summary");
        assertThat((double) summary.get("blockerRatio")).isEqualTo(0.0);
    }

    @Test
    void generateStatusReport_withZeroDays_returnsError() {
        Map<String, Object> result = service.generateStatusReport(0);

        assertThat(result.get("error")).isEqualTo("Days must be greater than zero");
    }

    @Test
    void generateStatusReport_withNegativeDays_returnsError() {
        Map<String, Object> result = service.generateStatusReport(-3);

        assertThat(result.get("error")).isEqualTo("Days must be greater than zero");
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateStatusReport_withEmptyStandups_returnsZeroCounts() {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        Map<String, Object> result = service.generateStatusReport(7);

        assertThat(result.get("totalStandups")).isEqualTo(0);
        assertThat(result.get("blockerCount")).isEqualTo(0);
        Map<String, Object> summary = (Map<String, Object>) result.get("summary");
        assertThat((double) summary.get("blockerRatio")).isEqualTo(0.0);
    }

    // ========== Helper Method Tests ==========

    @Test
    void isDateAfterOrEqual_withDateInRange_returnsTrue() {
        LocalDate cutoff = LocalDate.now().minusDays(5);
        String dateStr = LocalDate.now().minusDays(3).format(DateTimeFormatter.ISO_LOCAL_DATE);

        assertThat(service.isDateAfterOrEqual(dateStr, cutoff)).isTrue();
    }

    @Test
    void isDateAfterOrEqual_withDateOnCutoff_returnsTrue() {
        LocalDate cutoff = LocalDate.now().minusDays(5);
        String dateStr = cutoff.format(DateTimeFormatter.ISO_LOCAL_DATE);

        assertThat(service.isDateAfterOrEqual(dateStr, cutoff)).isTrue();
    }

    @Test
    void isDateAfterOrEqual_withDateBeforeCutoff_returnsFalse() {
        LocalDate cutoff = LocalDate.now().minusDays(5);
        String dateStr = LocalDate.now().minusDays(10).format(DateTimeFormatter.ISO_LOCAL_DATE);

        assertThat(service.isDateAfterOrEqual(dateStr, cutoff)).isFalse();
    }

    @Test
    void isDateAfterOrEqual_withNullDate_returnsFalse() {
        assertThat(service.isDateAfterOrEqual(null, LocalDate.now())).isFalse();
    }

    @Test
    void isDateAfterOrEqual_withInvalidDate_returnsFalse() {
        assertThat(service.isDateAfterOrEqual("not-a-date", LocalDate.now())).isFalse();
    }

    @Test
    void extractThemes_extractsLongWords() {
        java.util.Set<String> themes = new java.util.LinkedHashSet<>();
        service.extractThemes("Working on deployment pipeline testing", themes);

        assertThat(themes).contains("working", "deployment", "pipeline", "testing");
    }

    @Test
    void extractThemes_ignoresShortWords() {
        java.util.Set<String> themes = new java.util.LinkedHashSet<>();
        service.extractThemes("I am on it", themes);

        assertThat(themes).isEmpty();
    }

    @Test
    void extractThemes_ignoresStopWords() {
        java.util.Set<String> themes = new java.util.LinkedHashSet<>();
        service.extractThemes("this have been with them", themes);

        assertThat(themes).isEmpty();
    }

    @Test
    void extractThemes_withNullText_doesNothing() {
        java.util.Set<String> themes = new java.util.LinkedHashSet<>();
        service.extractThemes(null, themes);

        assertThat(themes).isEmpty();
    }

    // ========== Test Helper Methods ==========

    private StandupNote buildNote(String id, String yesterday, String today,
                                   String blockers, String standupDate) {
        return StandupNote.builder()
                .id(id)
                .yesterday(yesterday)
                .today(today)
                .blockers(blockers)
                .standupDate(standupDate)
                .createdAt(Instant.now())
                .build();
    }
}
