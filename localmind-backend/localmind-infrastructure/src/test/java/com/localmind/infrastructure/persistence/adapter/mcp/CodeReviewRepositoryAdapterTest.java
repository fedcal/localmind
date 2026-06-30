package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.CodeReviewResult;
import com.localmind.infrastructure.persistence.entity.mcp.CodeReviewEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaCodeReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodeReviewRepositoryAdapterTest {

    @Mock
    private JpaCodeReviewRepository jpaRepository;

    private CodeReviewRepositoryAdapter adapter;

    private final UUID TEST_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final Instant NOW = Instant.parse("2026-02-12T10:00:00Z");

    @BeforeEach
    void setUp() {
        adapter = new CodeReviewRepositoryAdapter(jpaRepository);
    }

    @Test
    void save_mapsdomainToEntityCorrectly() {
        CodeReviewResult domain = CodeReviewResult.builder()
                .id(TEST_UUID.toString())
                .reviewType("analyzeDiff")
                .issuesFound(5)
                .suggestionsJson("{\"totalIssues\": 5}")
                .resultJson("{\"stats\": {}}")
                .createdAt(NOW)
                .build();

        CodeReviewEntity savedEntity = CodeReviewEntity.builder()
                .id(TEST_UUID)
                .reviewType("analyzeDiff")
                .issuesFound(5)
                .suggestions("{\"totalIssues\": 5}")
                .result("{\"stats\": {}}")
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(CodeReviewEntity.class))).thenReturn(savedEntity);

        CodeReviewResult result = adapter.save(domain);

        ArgumentCaptor<CodeReviewEntity> captor = ArgumentCaptor.forClass(CodeReviewEntity.class);
        verify(jpaRepository).save(captor.capture());

        CodeReviewEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getReviewType()).isEqualTo("analyzeDiff");
        assertThat(captured.getIssuesFound()).isEqualTo(5);
        assertThat(captured.getSuggestions()).isEqualTo("{\"totalIssues\": 5}");
        assertThat(captured.getResult()).isEqualTo("{\"stats\": {}}");
        assertThat(captured.getCreatedAt()).isEqualTo(NOW);

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getReviewType()).isEqualTo("analyzeDiff");
        assertThat(result.getIssuesFound()).isEqualTo(5);
    }

    @Test
    void save_mapsEntityToDomainCorrectly() {
        CodeReviewResult domain = CodeReviewResult.builder()
                .id(TEST_UUID.toString())
                .reviewType("checkComplexity")
                .issuesFound(12)
                .suggestionsJson("complexity data")
                .resultJson("full result")
                .createdAt(NOW)
                .build();

        CodeReviewEntity savedEntity = CodeReviewEntity.builder()
                .id(TEST_UUID)
                .reviewType("checkComplexity")
                .issuesFound(12)
                .suggestions("complexity data")
                .result("full result")
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(CodeReviewEntity.class))).thenReturn(savedEntity);

        CodeReviewResult result = adapter.save(domain);

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getReviewType()).isEqualTo("checkComplexity");
        assertThat(result.getIssuesFound()).isEqualTo(12);
        assertThat(result.getSuggestionsJson()).isEqualTo("complexity data");
        assertThat(result.getResultJson()).isEqualTo("full result");
        assertThat(result.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void save_withNullId_doesNotSetIdOnEntity() {
        CodeReviewResult domain = CodeReviewResult.builder()
                .id(null)
                .reviewType("suggestImprovements")
                .issuesFound(3)
                .suggestionsJson("suggestions")
                .resultJson("result")
                .createdAt(NOW)
                .build();

        UUID generatedUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
        CodeReviewEntity savedEntity = CodeReviewEntity.builder()
                .id(generatedUuid)
                .reviewType("suggestImprovements")
                .issuesFound(3)
                .suggestions("suggestions")
                .result("result")
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(CodeReviewEntity.class))).thenReturn(savedEntity);

        CodeReviewResult result = adapter.save(domain);

        ArgumentCaptor<CodeReviewEntity> captor = ArgumentCaptor.forClass(CodeReviewEntity.class);
        verify(jpaRepository).save(captor.capture());

        CodeReviewEntity captured = captor.getValue();
        assertThat(captured.getId()).isNull();

        assertThat(result.getId()).isEqualTo(generatedUuid.toString());
    }

    @Test
    void save_withNullSuggestionsAndResult_mapsCorrectly() {
        CodeReviewResult domain = CodeReviewResult.builder()
                .id(TEST_UUID.toString())
                .reviewType("analyzeDiff")
                .issuesFound(0)
                .suggestionsJson(null)
                .resultJson(null)
                .createdAt(NOW)
                .build();

        CodeReviewEntity savedEntity = CodeReviewEntity.builder()
                .id(TEST_UUID)
                .reviewType("analyzeDiff")
                .issuesFound(0)
                .suggestions(null)
                .result(null)
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(CodeReviewEntity.class))).thenReturn(savedEntity);

        CodeReviewResult result = adapter.save(domain);

        ArgumentCaptor<CodeReviewEntity> captor = ArgumentCaptor.forClass(CodeReviewEntity.class);
        verify(jpaRepository).save(captor.capture());

        assertThat(captor.getValue().getSuggestions()).isNull();
        assertThat(captor.getValue().getResult()).isNull();
        assertThat(result.getSuggestionsJson()).isNull();
        assertThat(result.getResultJson()).isNull();
    }

    @Test
    void save_delegatesToJpaRepository() {
        CodeReviewResult domain = CodeReviewResult.builder()
                .id(TEST_UUID.toString())
                .reviewType("analyzeDiff")
                .issuesFound(1)
                .suggestionsJson("s")
                .resultJson("r")
                .createdAt(NOW)
                .build();

        CodeReviewEntity savedEntity = CodeReviewEntity.builder()
                .id(TEST_UUID)
                .reviewType("analyzeDiff")
                .issuesFound(1)
                .suggestions("s")
                .result("r")
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(CodeReviewEntity.class))).thenReturn(savedEntity);

        adapter.save(domain);

        verify(jpaRepository).save(any(CodeReviewEntity.class));
    }
}
