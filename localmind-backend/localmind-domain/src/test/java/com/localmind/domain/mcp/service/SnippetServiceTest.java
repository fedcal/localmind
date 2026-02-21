package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.CodeSnippet;
import com.localmind.domain.mcp.port.out.SnippetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SnippetServiceTest {

    @Mock
    private SnippetRepository snippetRepository;

    private SnippetService snippetService;

    @BeforeEach
    void setUp() {
        snippetService = new SnippetService(snippetRepository);
    }

    @Test
    void save_createsSnippetWithUuid() {
        when(snippetRepository.save(any(CodeSnippet.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CodeSnippet result = snippetService.save("Titolo", "System.out.println()", "java", "Descrizione", Set.of("util"));

        assertThat(result.getId()).isNotNull().isNotBlank();
        assertThat(result.getTitle()).isEqualTo("Titolo");
        assertThat(result.getCode()).isEqualTo("System.out.println()");
        assertThat(result.getLanguage()).isEqualTo("java");
        assertThat(result.getDescription()).isEqualTo("Descrizione");
        assertThat(result.getTags()).contains("util");

        ArgumentCaptor<CodeSnippet> captor = ArgumentCaptor.forClass(CodeSnippet.class);
        verify(snippetRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isNotNull();
    }

    @Test
    void save_setsCreatedAtAndUpdatedAt() {
        Instant before = Instant.now();

        when(snippetRepository.save(any(CodeSnippet.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CodeSnippet result = snippetService.save("Titolo", "code", "java", null, null);

        Instant after = Instant.now();

        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
        assertThat(result.getCreatedAt()).isBetween(before, after);
        assertThat(result.getUpdatedAt()).isBetween(before, after);
    }

    @Test
    void search_withKeyword_delegatesToRepository() {
        CodeSnippet snippet = buildSnippet("1", "Test snippet", "java");
        when(snippetRepository.searchByKeyword("test")).thenReturn(List.of(snippet));
        when(snippetRepository.findAll()).thenReturn(List.of(snippet));

        List<CodeSnippet> result = snippetService.search("test", null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("1");
        verify(snippetRepository).searchByKeyword("test");
    }

    @Test
    void search_withTag_delegatesToRepository() {
        CodeSnippet snippet = buildSnippet("1", "Test snippet", "java");
        when(snippetRepository.findByTag("util")).thenReturn(List.of(snippet));
        when(snippetRepository.findAll()).thenReturn(List.of(snippet));

        List<CodeSnippet> result = snippetService.search(null, "util", null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("1");
        verify(snippetRepository).findByTag("util");
    }

    @Test
    void search_withLanguage_delegatesToRepository() {
        CodeSnippet snippet = buildSnippet("1", "Test snippet", "python");
        when(snippetRepository.findByLanguage("python")).thenReturn(List.of(snippet));
        when(snippetRepository.findAll()).thenReturn(List.of(snippet));

        List<CodeSnippet> result = snippetService.search(null, null, "python");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("1");
        verify(snippetRepository).findByLanguage("python");
    }

    @Test
    void search_withMultipleFilters_combinesResults() {
        CodeSnippet snippet1 = buildSnippet("1", "Java snippet", "java");
        CodeSnippet snippet2 = buildSnippet("2", "Python snippet", "python");
        CodeSnippet snippet3 = buildSnippet("3", "Java altro", "java");

        when(snippetRepository.searchByKeyword("snippet")).thenReturn(List.of(snippet1, snippet2));
        when(snippetRepository.findByLanguage("java")).thenReturn(List.of(snippet1, snippet3));
        when(snippetRepository.findAll()).thenReturn(List.of(snippet1, snippet2, snippet3));

        List<CodeSnippet> result = snippetService.search("snippet", null, "java");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("1");
    }

    @Test
    void search_withNoFilters_returnsAll() {
        CodeSnippet snippet1 = buildSnippet("1", "Snippet 1", "java");
        CodeSnippet snippet2 = buildSnippet("2", "Snippet 2", "python");
        when(snippetRepository.findAll()).thenReturn(List.of(snippet1, snippet2));

        List<CodeSnippet> result = snippetService.search(null, null, null);

        assertThat(result).hasSize(2);
        verify(snippetRepository).findAll();
    }

    @Test
    void getById_found_returnsSnippet() {
        CodeSnippet snippet = buildSnippet("1", "Test", "java");
        when(snippetRepository.findById("1")).thenReturn(Optional.of(snippet));

        Optional<CodeSnippet> result = snippetService.getById("1");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("1");
        assertThat(result.get().getTitle()).isEqualTo("Test");
        verify(snippetRepository).findById("1");
    }

    @Test
    void getById_notFound_returnsEmpty() {
        when(snippetRepository.findById("non-existent")).thenReturn(Optional.empty());

        Optional<CodeSnippet> result = snippetService.getById("non-existent");

        assertThat(result).isEmpty();
        verify(snippetRepository).findById("non-existent");
    }

    @Test
    void delete_existing_returnsTrue() {
        when(snippetRepository.deleteById("1")).thenReturn(true);

        boolean result = snippetService.delete("1");

        assertThat(result).isTrue();
        verify(snippetRepository).deleteById("1");
    }

    @Test
    void delete_nonExisting_returnsFalse() {
        when(snippetRepository.deleteById("non-existent")).thenReturn(false);

        boolean result = snippetService.delete("non-existent");

        assertThat(result).isFalse();
        verify(snippetRepository).deleteById("non-existent");
    }

    @Test
    void listTags_aggregatesTagCounts() {
        CodeSnippet snippet1 = CodeSnippet.builder()
                .id("1").title("S1").code("c1").language("java")
                .tags(new HashSet<>(Set.of("util", "java")))
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .build();
        CodeSnippet snippet2 = CodeSnippet.builder()
                .id("2").title("S2").code("c2").language("python")
                .tags(new HashSet<>(Set.of("util", "python")))
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .build();
        CodeSnippet snippet3 = CodeSnippet.builder()
                .id("3").title("S3").code("c3").language("java")
                .tags(new HashSet<>(Set.of("java")))
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .build();

        when(snippetRepository.findAll()).thenReturn(List.of(snippet1, snippet2, snippet3));

        List<Map<String, Object>> result = snippetService.listTags();

        assertThat(result).hasSize(3);

        Map<String, Long> tagMap = result.stream()
                .collect(java.util.stream.Collectors.toMap(
                        m -> (String) m.get("tag"),
                        m -> (Long) m.get("count")
                ));

        assertThat(tagMap).containsEntry("util", 2L);
        assertThat(tagMap).containsEntry("java", 2L);
        assertThat(tagMap).containsEntry("python", 1L);
        verify(snippetRepository).findAll();
    }

    private CodeSnippet buildSnippet(String id, String title, String language) {
        return CodeSnippet.builder()
                .id(id)
                .title(title)
                .code("code-" + id)
                .language(language)
                .description("Descrizione " + id)
                .tags(new HashSet<>())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
