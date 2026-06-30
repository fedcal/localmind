package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.ActionItem;
import com.localmind.domain.mcp.model.RetroItem;
import com.localmind.domain.mcp.model.Retrospective;
import com.localmind.domain.mcp.port.out.RetrospectiveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RetrospectiveServiceTest {

    @Mock
    private RetrospectiveRepository repository;

    private RetrospectiveService service;

    private static final Instant NOW = Instant.parse("2026-02-12T10:00:00Z");

    @BeforeEach
    void setUp() {
        service = new RetrospectiveService(repository);

        when(repository.saveRetro(any(Retrospective.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.saveItem(any(RetroItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.saveActionItem(any(ActionItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    // ========== createRetro Tests ==========

    @Test
    void createRetro_withMadSadGlad_createsSuccessfully() {
        Map<String, Object> result = service.createRetro("sprint-1", "mad-sad-glad");

        assertThat(result.get("sprintId")).isEqualTo("sprint-1");
        assertThat(result.get("format")).isEqualTo("mad-sad-glad");
        assertThat(result.get("id")).isNotNull();
        assertThat(result.get("createdAt")).isNotNull();
        @SuppressWarnings("unchecked")
        List<String> categories = (List<String>) result.get("categories");
        assertThat(categories).containsExactly("mad", "sad", "glad");
    }

    @Test
    void createRetro_with4ls_createsSuccessfully() {
        Map<String, Object> result = service.createRetro("sprint-2", "4ls");

        assertThat(result.get("format")).isEqualTo("4ls");
        @SuppressWarnings("unchecked")
        List<String> categories = (List<String>) result.get("categories");
        assertThat(categories).containsExactly("liked", "learned", "lacked", "longed-for");
    }

    @Test
    void createRetro_withStartStopContinue_createsSuccessfully() {
        Map<String, Object> result = service.createRetro("sprint-3", "start-stop-continue");

        assertThat(result.get("format")).isEqualTo("start-stop-continue");
        @SuppressWarnings("unchecked")
        List<String> categories = (List<String>) result.get("categories");
        assertThat(categories).containsExactly("start", "stop", "continue");
    }

    @Test
    void createRetro_withNullFormat_defaultsToMadSadGlad() {
        Map<String, Object> result = service.createRetro("sprint-4", null);

        assertThat(result.get("format")).isEqualTo("mad-sad-glad");
    }

    @Test
    void createRetro_withBlankFormat_defaultsToMadSadGlad() {
        Map<String, Object> result = service.createRetro("sprint-5", "  ");

        assertThat(result.get("format")).isEqualTo("mad-sad-glad");
    }

    @Test
    void createRetro_withInvalidFormat_returnsError() {
        Map<String, Object> result = service.createRetro("sprint-1", "invalid-format");

        assertThat(result.get("error")).isNotNull();
        assertThat((String) result.get("error")).contains("Invalid format");
        verify(repository, never()).saveRetro(any());
    }

    @Test
    void createRetro_withNullSprintId_returnsError() {
        Map<String, Object> result = service.createRetro(null, "mad-sad-glad");

        assertThat(result.get("error")).isEqualTo("Sprint ID is required");
        verify(repository, never()).saveRetro(any());
    }

    @Test
    void createRetro_withBlankSprintId_returnsError() {
        Map<String, Object> result = service.createRetro("  ", "mad-sad-glad");

        assertThat(result.get("error")).isEqualTo("Sprint ID is required");
        verify(repository, never()).saveRetro(any());
    }

    @Test
    void createRetro_savesToRepository() {
        service.createRetro("sprint-1", "mad-sad-glad");

        ArgumentCaptor<Retrospective> captor = ArgumentCaptor.forClass(Retrospective.class);
        verify(repository).saveRetro(captor.capture());
        Retrospective saved = captor.getValue();
        assertThat(saved.getSprintId()).isEqualTo("sprint-1");
        assertThat(saved.getFormat()).isEqualTo("mad-sad-glad");
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void createRetro_normalizesFormatToLowerCase() {
        Map<String, Object> result = service.createRetro("sprint-1", "MAD-SAD-GLAD");

        assertThat(result.get("format")).isEqualTo("mad-sad-glad");
    }

    // ========== addRetroItem Tests ==========

    @Test
    void addRetroItem_withValidData_createsItem() {
        Retrospective retro = Retrospective.builder()
                .id("retro-1").sprintId("sprint-1").format("mad-sad-glad").createdAt(NOW).build();
        when(repository.findRetroById("retro-1")).thenReturn(Optional.of(retro));

        Map<String, Object> result = service.addRetroItem("retro-1", "mad", "Builds are slow");

        assertThat(result.get("retroId")).isEqualTo("retro-1");
        assertThat(result.get("category")).isEqualTo("mad");
        assertThat(result.get("content")).isEqualTo("Builds are slow");
        assertThat(result.get("votes")).isEqualTo(0);
        assertThat(result.get("id")).isNotNull();
    }

    @Test
    void addRetroItem_withNullRetroId_returnsError() {
        Map<String, Object> result = service.addRetroItem(null, "mad", "content");

        assertThat(result.get("error")).isEqualTo("Retro ID is required");
        verify(repository, never()).saveItem(any());
    }

    @Test
    void addRetroItem_withNullCategory_returnsError() {
        Map<String, Object> result = service.addRetroItem("retro-1", null, "content");

        assertThat(result.get("error")).isEqualTo("Category is required");
        verify(repository, never()).saveItem(any());
    }

    @Test
    void addRetroItem_withNullContent_returnsError() {
        Map<String, Object> result = service.addRetroItem("retro-1", "mad", null);

        assertThat(result.get("error")).isEqualTo("Content is required");
        verify(repository, never()).saveItem(any());
    }

    @Test
    void addRetroItem_withBlankContent_returnsError() {
        Map<String, Object> result = service.addRetroItem("retro-1", "mad", "  ");

        assertThat(result.get("error")).isEqualTo("Content is required");
        verify(repository, never()).saveItem(any());
    }

    @Test
    void addRetroItem_withNonExistentRetro_returnsError() {
        when(repository.findRetroById("nonexistent")).thenReturn(Optional.empty());

        Map<String, Object> result = service.addRetroItem("nonexistent", "mad", "content");

        assertThat(result.get("error")).isNotNull();
        assertThat((String) result.get("error")).contains("not found");
        verify(repository, never()).saveItem(any());
    }

    @Test
    void addRetroItem_withInvalidCategory_returnsError() {
        Retrospective retro = Retrospective.builder()
                .id("retro-1").sprintId("sprint-1").format("mad-sad-glad").createdAt(NOW).build();
        when(repository.findRetroById("retro-1")).thenReturn(Optional.of(retro));

        Map<String, Object> result = service.addRetroItem("retro-1", "invalid-cat", "content");

        assertThat(result.get("error")).isNotNull();
        assertThat((String) result.get("error")).contains("Invalid category");
        verify(repository, never()).saveItem(any());
    }

    @Test
    void addRetroItem_normalizesCategory() {
        Retrospective retro = Retrospective.builder()
                .id("retro-1").sprintId("sprint-1").format("mad-sad-glad").createdAt(NOW).build();
        when(repository.findRetroById("retro-1")).thenReturn(Optional.of(retro));

        Map<String, Object> result = service.addRetroItem("retro-1", "MAD", "content");

        assertThat(result.get("category")).isEqualTo("mad");
    }

    @Test
    void addRetroItem_savesWithZeroVotes() {
        Retrospective retro = Retrospective.builder()
                .id("retro-1").sprintId("sprint-1").format("mad-sad-glad").createdAt(NOW).build();
        when(repository.findRetroById("retro-1")).thenReturn(Optional.of(retro));

        service.addRetroItem("retro-1", "glad", "Great teamwork");

        ArgumentCaptor<RetroItem> captor = ArgumentCaptor.forClass(RetroItem.class);
        verify(repository).saveItem(captor.capture());
        assertThat(captor.getValue().getVotes()).isEqualTo(0);
    }

    // ========== voteRetroItem Tests ==========

    @Test
    void voteRetroItem_incrementsVotes() {
        RetroItem existing = RetroItem.builder()
                .id("item-1").retroId("retro-1").category("mad")
                .content("Slow builds").votes(3).createdAt(NOW).build();
        when(repository.findItemById("item-1")).thenReturn(Optional.of(existing));

        Map<String, Object> result = service.voteRetroItem("item-1");

        assertThat(result.get("votes")).isEqualTo(4);
        assertThat(result.get("previousVotes")).isEqualTo(3);
    }

    @Test
    void voteRetroItem_withNullItemId_returnsError() {
        Map<String, Object> result = service.voteRetroItem(null);

        assertThat(result.get("error")).isEqualTo("Item ID is required");
        verify(repository, never()).saveItem(any());
    }

    @Test
    void voteRetroItem_withBlankItemId_returnsError() {
        Map<String, Object> result = service.voteRetroItem("  ");

        assertThat(result.get("error")).isEqualTo("Item ID is required");
        verify(repository, never()).saveItem(any());
    }

    @Test
    void voteRetroItem_withNonExistentItem_returnsError() {
        when(repository.findItemById("nonexistent")).thenReturn(Optional.empty());

        Map<String, Object> result = service.voteRetroItem("nonexistent");

        assertThat(result.get("error")).isNotNull();
        assertThat((String) result.get("error")).contains("not found");
    }

    @Test
    void voteRetroItem_savesUpdatedItem() {
        RetroItem existing = RetroItem.builder()
                .id("item-1").retroId("retro-1").category("glad")
                .content("Good reviews").votes(0).createdAt(NOW).build();
        when(repository.findItemById("item-1")).thenReturn(Optional.of(existing));

        service.voteRetroItem("item-1");

        ArgumentCaptor<RetroItem> captor = ArgumentCaptor.forClass(RetroItem.class);
        verify(repository).saveItem(captor.capture());
        RetroItem saved = captor.getValue();
        assertThat(saved.getVotes()).isEqualTo(1);
        assertThat(saved.getId()).isEqualTo("item-1");
        assertThat(saved.getContent()).isEqualTo("Good reviews");
    }

    @Test
    void voteRetroItem_preservesAllFieldsExceptVotes() {
        RetroItem existing = RetroItem.builder()
                .id("item-1").retroId("retro-1").category("sad")
                .content("Missing docs").votes(5).createdAt(NOW).build();
        when(repository.findItemById("item-1")).thenReturn(Optional.of(existing));

        service.voteRetroItem("item-1");

        ArgumentCaptor<RetroItem> captor = ArgumentCaptor.forClass(RetroItem.class);
        verify(repository).saveItem(captor.capture());
        RetroItem saved = captor.getValue();
        assertThat(saved.getRetroId()).isEqualTo("retro-1");
        assertThat(saved.getCategory()).isEqualTo("sad");
        assertThat(saved.getContent()).isEqualTo("Missing docs");
        assertThat(saved.getCreatedAt()).isEqualTo(NOW);
    }

    // ========== generateActionItems Tests ==========

    @Test
    void generateActionItems_generatesFromTopVotedItems() {
        Retrospective retro = Retrospective.builder()
                .id("retro-1").sprintId("sprint-1").format("mad-sad-glad").createdAt(NOW).build();
        when(repository.findRetroById("retro-1")).thenReturn(Optional.of(retro));

        List<RetroItem> items = List.of(
                RetroItem.builder().id("i1").retroId("retro-1").category("mad")
                        .content("Slow builds").votes(10).createdAt(NOW).build(),
                RetroItem.builder().id("i2").retroId("retro-1").category("glad")
                        .content("Good reviews").votes(5).createdAt(NOW).build(),
                RetroItem.builder().id("i3").retroId("retro-1").category("sad")
                        .content("Missing docs").votes(8).createdAt(NOW).build()
        );
        when(repository.findItemsByRetroId("retro-1")).thenReturn(items);

        Map<String, Object> result = service.generateActionItems("retro-1", 2);

        assertThat(result.get("actionItemCount")).isEqualTo(2);
        List<Map<String, Object>> actionItems = (List<Map<String, Object>>) result.get("actionItems");
        assertThat(actionItems).hasSize(2);
        // First should be the highest voted (10 votes - "Slow builds")
        assertThat((int) actionItems.get(0).get("sourceVotes")).isEqualTo(10);
        // Second should be 8 votes - "Missing docs"
        assertThat((int) actionItems.get(1).get("sourceVotes")).isEqualTo(8);
    }

    @Test
    void generateActionItems_withNullRetroId_returnsError() {
        Map<String, Object> result = service.generateActionItems(null, 3);

        assertThat(result.get("error")).isEqualTo("Retro ID is required");
    }

    @Test
    void generateActionItems_withZeroTopN_returnsError() {
        Map<String, Object> result = service.generateActionItems("retro-1", 0);

        assertThat(result.get("error")).isEqualTo("topN must be greater than 0");
    }

    @Test
    void generateActionItems_withNegativeTopN_returnsError() {
        Map<String, Object> result = service.generateActionItems("retro-1", -1);

        assertThat(result.get("error")).isEqualTo("topN must be greater than 0");
    }

    @Test
    void generateActionItems_withNonExistentRetro_returnsError() {
        when(repository.findRetroById("nonexistent")).thenReturn(Optional.empty());

        Map<String, Object> result = service.generateActionItems("nonexistent", 3);

        assertThat(result.get("error")).isNotNull();
        assertThat((String) result.get("error")).contains("not found");
    }

    @Test
    void generateActionItems_withNoItems_returnsError() {
        Retrospective retro = Retrospective.builder()
                .id("retro-1").sprintId("sprint-1").format("mad-sad-glad").createdAt(NOW).build();
        when(repository.findRetroById("retro-1")).thenReturn(Optional.of(retro));
        when(repository.findItemsByRetroId("retro-1")).thenReturn(new ArrayList<>());

        Map<String, Object> result = service.generateActionItems("retro-1", 3);

        assertThat(result.get("error")).isNotNull();
        assertThat((String) result.get("error")).contains("No items found");
    }

    @Test
    void generateActionItems_savesEachActionItem() {
        Retrospective retro = Retrospective.builder()
                .id("retro-1").sprintId("sprint-1").format("mad-sad-glad").createdAt(NOW).build();
        when(repository.findRetroById("retro-1")).thenReturn(Optional.of(retro));

        List<RetroItem> items = List.of(
                RetroItem.builder().id("i1").retroId("retro-1").category("mad")
                        .content("Issue A").votes(5).createdAt(NOW).build(),
                RetroItem.builder().id("i2").retroId("retro-1").category("glad")
                        .content("Good B").votes(3).createdAt(NOW).build()
        );
        when(repository.findItemsByRetroId("retro-1")).thenReturn(items);

        service.generateActionItems("retro-1", 2);

        verify(repository, times(2)).saveActionItem(any(ActionItem.class));
    }

    @Test
    void generateActionItems_setsStatusPendingAndAssigneeTeam() {
        Retrospective retro = Retrospective.builder()
                .id("retro-1").sprintId("sprint-1").format("mad-sad-glad").createdAt(NOW).build();
        when(repository.findRetroById("retro-1")).thenReturn(Optional.of(retro));

        List<RetroItem> items = List.of(
                RetroItem.builder().id("i1").retroId("retro-1").category("mad")
                        .content("Issue").votes(5).createdAt(NOW).build()
        );
        when(repository.findItemsByRetroId("retro-1")).thenReturn(items);

        Map<String, Object> result = service.generateActionItems("retro-1", 1);

        List<Map<String, Object>> actionItems = (List<Map<String, Object>>) result.get("actionItems");
        assertThat(actionItems.get(0).get("status")).isEqualTo("pending");
        assertThat(actionItems.get(0).get("assignee")).isEqualTo("team");
        assertThat(actionItems.get(0).get("dueDate")).isEqualTo("next-sprint");
    }

    // ========== getRetro Tests ==========

    @Test
    @SuppressWarnings("unchecked")
    void getRetro_returnsFullDetails() {
        Retrospective retro = Retrospective.builder()
                .id("retro-1").sprintId("sprint-1").format("mad-sad-glad").createdAt(NOW).build();
        when(repository.findRetroById("retro-1")).thenReturn(Optional.of(retro));

        List<RetroItem> items = List.of(
                RetroItem.builder().id("i1").retroId("retro-1").category("mad")
                        .content("Slow builds").votes(5).createdAt(NOW).build(),
                RetroItem.builder().id("i2").retroId("retro-1").category("glad")
                        .content("Good team").votes(3).createdAt(NOW).build()
        );
        when(repository.findItemsByRetroId("retro-1")).thenReturn(items);

        List<ActionItem> actionItems = List.of(
                ActionItem.builder().id("a1").retroId("retro-1")
                        .description("Fix builds").assignee("team")
                        .dueDate("next-sprint").status("pending").createdAt(NOW).build()
        );
        when(repository.findActionItemsByRetroId("retro-1")).thenReturn(actionItems);

        Map<String, Object> result = service.getRetro("retro-1");

        assertThat(result.get("id")).isEqualTo("retro-1");
        assertThat(result.get("sprintId")).isEqualTo("sprint-1");
        assertThat(result.get("format")).isEqualTo("mad-sad-glad");
        assertThat(result.get("itemCount")).isEqualTo(2);
        assertThat(result.get("actionItemCount")).isEqualTo(1);

        Map<String, List<Map<String, Object>>> groupedItems =
                (Map<String, List<Map<String, Object>>>) result.get("items");
        assertThat(groupedItems).containsKeys("mad", "sad", "glad");
        assertThat(groupedItems.get("mad")).hasSize(1);
        assertThat(groupedItems.get("glad")).hasSize(1);
        assertThat(groupedItems.get("sad")).isEmpty();
    }

    @Test
    void getRetro_withNullRetroId_returnsError() {
        Map<String, Object> result = service.getRetro(null);

        assertThat(result.get("error")).isEqualTo("Retro ID is required");
    }

    @Test
    void getRetro_withBlankRetroId_returnsError() {
        Map<String, Object> result = service.getRetro("  ");

        assertThat(result.get("error")).isEqualTo("Retro ID is required");
    }

    @Test
    void getRetro_withNonExistentRetro_returnsError() {
        when(repository.findRetroById("nonexistent")).thenReturn(Optional.empty());

        Map<String, Object> result = service.getRetro("nonexistent");

        assertThat(result.get("error")).isNotNull();
        assertThat((String) result.get("error")).contains("not found");
    }

    @Test
    @SuppressWarnings("unchecked")
    void getRetro_withNoItemsOrActions_returnsEmptyLists() {
        Retrospective retro = Retrospective.builder()
                .id("retro-1").sprintId("sprint-1").format("mad-sad-glad").createdAt(NOW).build();
        when(repository.findRetroById("retro-1")).thenReturn(Optional.of(retro));
        when(repository.findItemsByRetroId("retro-1")).thenReturn(new ArrayList<>());
        when(repository.findActionItemsByRetroId("retro-1")).thenReturn(new ArrayList<>());

        Map<String, Object> result = service.getRetro("retro-1");

        assertThat(result.get("itemCount")).isEqualTo(0);
        assertThat(result.get("actionItemCount")).isEqualTo(0);
        assertThat((List<?>) result.get("actionItems")).isEmpty();
    }

    // ========== detectPatterns Tests ==========

    @Test
    void detectPatterns_withNoItems_returnsEmpty() {
        when(repository.findAllItems()).thenReturn(new ArrayList<>());

        Map<String, Object> result = service.detectPatterns();

        assertThat(result.get("patternCount")).isEqualTo(0);
        assertThat((List<?>) result.get("patterns")).isEmpty();
        assertThat(result.get("message")).isNotNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void detectPatterns_identifiesRecurringWords() {
        List<RetroItem> items = List.of(
                RetroItem.builder().id("i1").retroId("r1").category("mad")
                        .content("Build process is slow and unreliable").votes(3).createdAt(NOW).build(),
                RetroItem.builder().id("i2").retroId("r1").category("sad")
                        .content("The build pipeline keeps failing").votes(2).createdAt(NOW).build(),
                RetroItem.builder().id("i3").retroId("r2").category("mad")
                        .content("Build times have increased significantly").votes(5).createdAt(NOW).build()
        );
        when(repository.findAllItems()).thenReturn(items);

        Map<String, Object> result = service.detectPatterns();

        assertThat((int) result.get("totalItemsAnalyzed")).isEqualTo(3);
        List<Map<String, Object>> patterns = (List<Map<String, Object>>) result.get("patterns");
        assertThat(patterns).isNotEmpty();

        // "build" should appear as a top pattern (appears in all 3 items)
        boolean hasBuildPattern = patterns.stream()
                .anyMatch(p -> "build".equals(p.get("word")));
        assertThat(hasBuildPattern).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void detectPatterns_excludesStopWords() {
        List<RetroItem> items = List.of(
                RetroItem.builder().id("i1").retroId("r1").category("mad")
                        .content("the deployment the deployment").votes(1).createdAt(NOW).build(),
                RetroItem.builder().id("i2").retroId("r1").category("sad")
                        .content("the deployment process").votes(1).createdAt(NOW).build()
        );
        when(repository.findAllItems()).thenReturn(items);

        Map<String, Object> result = service.detectPatterns();

        List<Map<String, Object>> patterns = (List<Map<String, Object>>) result.get("patterns");
        boolean hasThePattern = patterns.stream()
                .anyMatch(p -> "the".equals(p.get("word")));
        assertThat(hasThePattern).isFalse();
    }

    @Test
    @SuppressWarnings("unchecked")
    void detectPatterns_detectsSentiment() {
        List<RetroItem> items = List.of(
                RetroItem.builder().id("i1").retroId("r1").category("mad")
                        .content("Testing coverage is insufficient").votes(3).createdAt(NOW).build(),
                RetroItem.builder().id("i2").retroId("r1").category("sad")
                        .content("Testing framework has issues").votes(2).createdAt(NOW).build()
        );
        when(repository.findAllItems()).thenReturn(items);

        Map<String, Object> result = service.detectPatterns();

        List<Map<String, Object>> patterns = (List<Map<String, Object>>) result.get("patterns");
        Optional<Map<String, Object>> testingPattern = patterns.stream()
                .filter(p -> "testing".equals(p.get("word")))
                .findFirst();

        assertThat(testingPattern).isPresent();
        assertThat(testingPattern.get().get("sentiment")).isEqualTo("negative");
    }

    @Test
    @SuppressWarnings("unchecked")
    void detectPatterns_identifiesThemes() {
        List<RetroItem> items = List.of(
                RetroItem.builder().id("i1").retroId("r1").category("mad")
                        .content("Issue one").votes(3).createdAt(NOW).build(),
                RetroItem.builder().id("i2").retroId("r1").category("mad")
                        .content("Issue two").votes(5).createdAt(NOW).build(),
                RetroItem.builder().id("i3").retroId("r1").category("glad")
                        .content("Good thing").votes(2).createdAt(NOW).build()
        );
        when(repository.findAllItems()).thenReturn(items);

        Map<String, Object> result = service.detectPatterns();

        List<Map<String, Object>> themes = (List<Map<String, Object>>) result.get("themes");
        assertThat(themes).isNotEmpty();

        // "mad" category should be a theme with 2 items and 8 total votes
        Optional<Map<String, Object>> madTheme = themes.stream()
                .filter(t -> "mad".equals(t.get("category")))
                .findFirst();
        assertThat(madTheme).isPresent();
        assertThat(madTheme.get().get("itemCount")).isEqualTo(2);
        assertThat(madTheme.get().get("totalVotes")).isEqualTo(8);
    }

    @Test
    @SuppressWarnings("unchecked")
    void detectPatterns_filtersWordsWithMinimumTwoOccurrences() {
        List<RetroItem> items = List.of(
                RetroItem.builder().id("i1").retroId("r1").category("mad")
                        .content("unique-word-xyz performance").votes(1).createdAt(NOW).build(),
                RetroItem.builder().id("i2").retroId("r1").category("sad")
                        .content("another-unique performance").votes(1).createdAt(NOW).build()
        );
        when(repository.findAllItems()).thenReturn(items);

        Map<String, Object> result = service.detectPatterns();

        List<Map<String, Object>> patterns = (List<Map<String, Object>>) result.get("patterns");
        // "performance" appears 2 times, should be a pattern
        boolean hasPerformance = patterns.stream()
                .anyMatch(p -> "performance".equals(p.get("word")));
        assertThat(hasPerformance).isTrue();

        // unique words appearing once should NOT be patterns
        boolean hasUnique = patterns.stream()
                .anyMatch(p -> "unique-word-xyz".equals(p.get("word")));
        assertThat(hasUnique).isFalse();
    }

    // ========== suggestItems Tests ==========

    @Test
    void suggestItems_returnsRequestedLimit() {
        when(repository.findAllItems()).thenReturn(new ArrayList<>());

        Map<String, Object> result = service.suggestItems(5);

        assertThat((int) result.get("suggestionCount")).isEqualTo(5);
        assertThat((List<?>) result.get("suggestions")).hasSize(5);
    }

    @Test
    void suggestItems_withZeroLimit_returnsError() {
        Map<String, Object> result = service.suggestItems(0);

        assertThat(result.get("error")).isEqualTo("Limit must be greater than 0");
    }

    @Test
    void suggestItems_withNegativeLimit_returnsError() {
        Map<String, Object> result = service.suggestItems(-1);

        assertThat(result.get("error")).isEqualTo("Limit must be greater than 0");
    }

    @Test
    @SuppressWarnings("unchecked")
    void suggestItems_containsThemeAndCategory() {
        when(repository.findAllItems()).thenReturn(new ArrayList<>());

        Map<String, Object> result = service.suggestItems(3);

        List<Map<String, Object>> suggestions = (List<Map<String, Object>>) result.get("suggestions");
        for (Map<String, Object> suggestion : suggestions) {
            assertThat(suggestion).containsKeys("theme", "suggestedContent", "suggestedCategory", "index");
            assertThat((String) suggestion.get("theme")).isNotBlank();
            assertThat((String) suggestion.get("suggestedContent")).isNotBlank();
            assertThat((String) suggestion.get("suggestedCategory")).isNotBlank();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void suggestItems_excludesExistingItems() {
        // Create items that match some suggestion templates exactly
        List<RetroItem> existingItems = List.of(
                RetroItem.builder().id("i1").retroId("r1").category("start")
                        .content("Build times are too long, consider parallelizing CI steps")
                        .votes(0).createdAt(NOW).build()
        );
        when(repository.findAllItems()).thenReturn(existingItems);

        Map<String, Object> result = service.suggestItems(18);

        List<Map<String, Object>> suggestions = (List<Map<String, Object>>) result.get("suggestions");
        boolean hasDuplicate = suggestions.stream()
                .anyMatch(s -> "Build times are too long, consider parallelizing CI steps"
                        .equals(s.get("suggestedContent")));
        assertThat(hasDuplicate).isFalse();
    }

    @Test
    void suggestItems_capsToTemplateSize() {
        when(repository.findAllItems()).thenReturn(new ArrayList<>());

        Map<String, Object> result = service.suggestItems(1000);

        // Should not exceed template count
        assertThat((int) result.get("suggestionCount")).isLessThanOrEqualTo(18);
    }

    // ========== generateActionDescription Tests ==========

    @Test
    void generateActionDescription_forMadCategory_containsAddressIssue() {
        RetroItem item = RetroItem.builder()
                .id("i1").retroId("r1").category("mad")
                .content("Slow builds").votes(5).createdAt(NOW).build();

        String desc = service.generateActionDescription(item);

        assertThat(desc).contains("Address issue");
        assertThat(desc).contains("Slow builds");
    }

    @Test
    void generateActionDescription_forGladCategory_containsReinforce() {
        RetroItem item = RetroItem.builder()
                .id("i1").retroId("r1").category("glad")
                .content("Good teamwork").votes(3).createdAt(NOW).build();

        String desc = service.generateActionDescription(item);

        assertThat(desc).contains("Reinforce practice");
        assertThat(desc).contains("Good teamwork");
    }

    @Test
    void generateActionDescription_forStartCategory_containsImplement() {
        RetroItem item = RetroItem.builder()
                .id("i1").retroId("r1").category("start")
                .content("Daily standups").votes(2).createdAt(NOW).build();

        String desc = service.generateActionDescription(item);

        assertThat(desc).contains("Implement");
        assertThat(desc).contains("Daily standups");
    }

    @Test
    void generateActionDescription_forLearnedCategory_containsApplyLearning() {
        RetroItem item = RetroItem.builder()
                .id("i1").retroId("r1").category("learned")
                .content("TDD approach").votes(1).createdAt(NOW).build();

        String desc = service.generateActionDescription(item);

        assertThat(desc).contains("Apply learning");
        assertThat(desc).contains("TDD approach");
    }

    @Test
    void generateActionDescription_forUnknownCategory_containsAction() {
        RetroItem item = RetroItem.builder()
                .id("i1").retroId("r1").category("custom")
                .content("Something").votes(0).createdAt(NOW).build();

        String desc = service.generateActionDescription(item);

        assertThat(desc).contains("Action");
        assertThat(desc).contains("Something");
    }
}
