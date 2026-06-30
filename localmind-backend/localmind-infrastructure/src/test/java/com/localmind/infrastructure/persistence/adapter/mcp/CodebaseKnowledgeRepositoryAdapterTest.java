package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.CodebaseKnowledge;
import com.localmind.infrastructure.persistence.entity.mcp.CodebaseKnowledgeEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaCodebaseKnowledgeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodebaseKnowledgeRepositoryAdapterTest {

    @Mock
    private JpaCodebaseKnowledgeRepository jpaRepository;

    private CodebaseKnowledgeRepositoryAdapter adapter;

    private static final UUID TEST_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final Instant TEST_INSTANT = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        adapter = new CodebaseKnowledgeRepositoryAdapter(jpaRepository);
    }

    @Test
    void save_mapsDomainToEntityCorrectly() {
        CodebaseKnowledge domain = CodebaseKnowledge.builder()
                .id(TEST_UUID.toString())
                .analysisType("search-code")
                .targetPath("/src/main")
                .resultJson("{\"matches\":[]}")
                .createdAt(TEST_INSTANT)
                .build();

        CodebaseKnowledgeEntity savedEntity = CodebaseKnowledgeEntity.builder()
                .id(TEST_UUID)
                .analysisType("search-code")
                .targetPath("/src/main")
                .result("{\"matches\":[]}")
                .createdAt(TEST_INSTANT)
                .build();

        when(jpaRepository.save(any(CodebaseKnowledgeEntity.class))).thenReturn(savedEntity);

        adapter.save(domain);

        ArgumentCaptor<CodebaseKnowledgeEntity> captor = ArgumentCaptor.forClass(CodebaseKnowledgeEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(TEST_UUID);
        assertThat(captor.getValue().getAnalysisType()).isEqualTo("search-code");
        assertThat(captor.getValue().getTargetPath()).isEqualTo("/src/main");
    }

    @Test
    void save_mapsEntityToDomainCorrectly() {
        CodebaseKnowledge domain = CodebaseKnowledge.builder()
                .id(TEST_UUID.toString())
                .analysisType("explain-module")
                .targetPath("/src/app.ts")
                .resultJson("{}")
                .createdAt(TEST_INSTANT)
                .build();

        CodebaseKnowledgeEntity savedEntity = CodebaseKnowledgeEntity.builder()
                .id(TEST_UUID)
                .analysisType("explain-module")
                .targetPath("/src/app.ts")
                .result("{}")
                .createdAt(TEST_INSTANT)
                .build();

        when(jpaRepository.save(any(CodebaseKnowledgeEntity.class))).thenReturn(savedEntity);

        CodebaseKnowledge result = adapter.save(domain);

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getAnalysisType()).isEqualTo("explain-module");
    }

    @Test
    void findByTargetPath_returnsConvertedList() {
        CodebaseKnowledgeEntity entity = CodebaseKnowledgeEntity.builder()
                .id(TEST_UUID)
                .analysisType("track-changes")
                .targetPath("src/auth")
                .result("{}")
                .createdAt(TEST_INSTANT)
                .build();

        when(jpaRepository.findByTargetPathOrderByCreatedAtDesc("src/auth"))
                .thenReturn(List.of(entity));

        List<CodebaseKnowledge> results = adapter.findByTargetPath("src/auth");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getAnalysisType()).isEqualTo("track-changes");
        assertThat(results.get(0).getTargetPath()).isEqualTo("src/auth");
    }
}
