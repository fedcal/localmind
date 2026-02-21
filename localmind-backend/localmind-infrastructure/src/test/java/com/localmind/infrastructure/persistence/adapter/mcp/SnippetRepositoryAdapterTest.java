package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.CodeSnippet;
import com.localmind.infrastructure.persistence.entity.mcp.SnippetEntity;
import com.localmind.infrastructure.persistence.entity.mcp.SnippetTagEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaSnippetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SnippetRepositoryAdapterTest {

    @Mock
    private JpaSnippetRepository jpaRepository;

    private SnippetRepositoryAdapter adapter;

    private final UUID TEST_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final Instant NOW = Instant.parse("2026-02-12T10:00:00Z");
    private final Instant LATER = Instant.parse("2026-02-12T11:00:00Z");

    @BeforeEach
    void setUp() {
        adapter = new SnippetRepositoryAdapter(jpaRepository);
    }

    @Test
    void save_mapsCorrectly() {
        CodeSnippet domain = CodeSnippet.builder()
                .id(TEST_UUID.toString())
                .title("Quick sort")
                .code("public void sort() {}")
                .language("java")
                .description("Algoritmo quick sort")
                .tags(new HashSet<>(Set.of("sorting", "algorithm")))
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();

        SnippetEntity savedEntity = buildSnippetEntity(TEST_UUID, "Quick sort", "public void sort() {}",
                "java", "Algoritmo quick sort", NOW, NOW,
                List.of(buildTagEntity("sorting"), buildTagEntity("algorithm")));

        when(jpaRepository.save(any(SnippetEntity.class))).thenReturn(savedEntity);

        CodeSnippet result = adapter.save(domain);

        ArgumentCaptor<SnippetEntity> captor = ArgumentCaptor.forClass(SnippetEntity.class);
        verify(jpaRepository).save(captor.capture());

        SnippetEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getTitle()).isEqualTo("Quick sort");
        assertThat(captured.getCode()).isEqualTo("public void sort() {}");
        assertThat(captured.getLanguage()).isEqualTo("java");
        assertThat(captured.getDescription()).isEqualTo("Algoritmo quick sort");
        assertThat(captured.getCreatedAt()).isEqualTo(NOW);
        assertThat(captured.getUpdatedAt()).isEqualTo(NOW);
        assertThat(captured.getTags()).hasSize(2);

        Set<String> tagValues = new HashSet<>();
        captured.getTags().forEach(t -> tagValues.add(t.getTag()));
        assertThat(tagValues).containsExactlyInAnyOrder("sorting", "algorithm");

        captured.getTags().forEach(tagEntity ->
                assertThat(tagEntity.getSnippet()).isSameAs(captured));

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getTitle()).isEqualTo("Quick sort");
        assertThat(result.getCode()).isEqualTo("public void sort() {}");
        assertThat(result.getLanguage()).isEqualTo("java");
        assertThat(result.getDescription()).isEqualTo("Algoritmo quick sort");
        assertThat(result.getTags()).containsExactlyInAnyOrder("sorting", "algorithm");
        assertThat(result.getCreatedAt()).isEqualTo(NOW);
        assertThat(result.getUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void findById_found_mapsToDomain() {
        SnippetEntity entity = buildSnippetEntity(TEST_UUID, "Hello World", "print('hello')",
                "python", "Stampa hello world", NOW, LATER,
                List.of(buildTagEntity("basic")));

        when(jpaRepository.findById(TEST_UUID)).thenReturn(Optional.of(entity));

        Optional<CodeSnippet> result = adapter.findById(TEST_UUID.toString());

        assertThat(result).isPresent();
        CodeSnippet snippet = result.get();
        assertThat(snippet.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(snippet.getTitle()).isEqualTo("Hello World");
        assertThat(snippet.getCode()).isEqualTo("print('hello')");
        assertThat(snippet.getLanguage()).isEqualTo("python");
        assertThat(snippet.getDescription()).isEqualTo("Stampa hello world");
        assertThat(snippet.getTags()).containsExactly("basic");
        assertThat(snippet.getCreatedAt()).isEqualTo(NOW);
        assertThat(snippet.getUpdatedAt()).isEqualTo(LATER);

        verify(jpaRepository).findById(TEST_UUID);
    }

    @Test
    void findById_notFound_returnsEmpty() {
        when(jpaRepository.findById(TEST_UUID)).thenReturn(Optional.empty());

        Optional<CodeSnippet> result = adapter.findById(TEST_UUID.toString());

        assertThat(result).isEmpty();
        verify(jpaRepository).findById(TEST_UUID);
    }

    @Test
    void searchByKeyword_delegatesToJpa() {
        SnippetEntity entity = buildSnippetEntity(TEST_UUID, "Sort algorithm", "void sort() {}",
                "java", "Sorting", NOW, NOW, List.of());

        when(jpaRepository.searchByKeyword("sort")).thenReturn(List.of(entity));

        List<CodeSnippet> result = adapter.searchByKeyword("sort");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.get(0).getTitle()).isEqualTo("Sort algorithm");
        verify(jpaRepository).searchByKeyword("sort");
    }

    @Test
    void findByTag_delegatesToJpa() {
        SnippetEntity entity = buildSnippetEntity(TEST_UUID, "Tagged snippet", "code",
                "java", "Desc", NOW, NOW, List.of(buildTagEntity("util")));

        when(jpaRepository.findByTag("util")).thenReturn(List.of(entity));

        List<CodeSnippet> result = adapter.findByTag("util");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.get(0).getTags()).contains("util");
        verify(jpaRepository).findByTag("util");
    }

    @Test
    void findByLanguage_delegatesToJpa() {
        SnippetEntity entity = buildSnippetEntity(TEST_UUID, "Python snippet", "print()",
                "python", "Desc", NOW, NOW, List.of());

        when(jpaRepository.findByLanguage("python")).thenReturn(List.of(entity));

        List<CodeSnippet> result = adapter.findByLanguage("python");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLanguage()).isEqualTo("python");
        verify(jpaRepository).findByLanguage("python");
    }

    @Test
    void deleteById_existing_returnsTrue() {
        when(jpaRepository.existsById(TEST_UUID)).thenReturn(true);

        boolean result = adapter.deleteById(TEST_UUID.toString());

        assertThat(result).isTrue();
        verify(jpaRepository).existsById(TEST_UUID);
        verify(jpaRepository).deleteById(TEST_UUID);
    }

    @Test
    void deleteById_nonExisting_returnsFalse() {
        when(jpaRepository.existsById(TEST_UUID)).thenReturn(false);

        boolean result = adapter.deleteById(TEST_UUID.toString());

        assertThat(result).isFalse();
        verify(jpaRepository).existsById(TEST_UUID);
    }

    @Test
    void findAll_returnsAllMappedSnippets() {
        UUID uuid1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID uuid2 = UUID.fromString("22222222-2222-2222-2222-222222222222");

        SnippetEntity entity1 = buildSnippetEntity(uuid1, "Snippet 1", "code1",
                "java", "Desc 1", NOW, NOW, List.of(buildTagEntity("tag1")));
        SnippetEntity entity2 = buildSnippetEntity(uuid2, "Snippet 2", "code2",
                "python", "Desc 2", NOW, LATER, List.of());

        when(jpaRepository.findAll()).thenReturn(List.of(entity1, entity2));

        List<CodeSnippet> result = adapter.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(uuid1.toString());
        assertThat(result.get(0).getTitle()).isEqualTo("Snippet 1");
        assertThat(result.get(0).getTags()).containsExactly("tag1");
        assertThat(result.get(1).getId()).isEqualTo(uuid2.toString());
        assertThat(result.get(1).getTitle()).isEqualTo("Snippet 2");
        assertThat(result.get(1).getTags()).isEmpty();
        verify(jpaRepository).findAll();
    }

    @Test
    void save_withNullTags_setsEmptyTagsList() {
        CodeSnippet domain = CodeSnippet.builder()
                .id(TEST_UUID.toString())
                .title("No tags")
                .code("code")
                .language("java")
                .description(null)
                .tags(null)
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();

        SnippetEntity savedEntity = buildSnippetEntity(TEST_UUID, "No tags", "code",
                "java", null, NOW, NOW, List.of());

        when(jpaRepository.save(any(SnippetEntity.class))).thenReturn(savedEntity);

        adapter.save(domain);

        ArgumentCaptor<SnippetEntity> captor = ArgumentCaptor.forClass(SnippetEntity.class);
        verify(jpaRepository).save(captor.capture());

        SnippetEntity captured = captor.getValue();
        assertThat(captured.getTags()).isEmpty();
        assertThat(captured.getDescription()).isNull();
    }

    // --- Helper methods ---

    private SnippetEntity buildSnippetEntity(UUID id, String title, String code, String language,
                                              String description, Instant createdAt, Instant updatedAt,
                                              List<SnippetTagEntity> tags) {
        return SnippetEntity.builder()
                .id(id)
                .title(title)
                .code(code)
                .language(language)
                .description(description)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .tags(new ArrayList<>(tags))
                .build();
    }

    private SnippetTagEntity buildTagEntity(String tag) {
        return SnippetTagEntity.builder()
                .id(UUID.randomUUID())
                .tag(tag)
                .build();
    }
}
