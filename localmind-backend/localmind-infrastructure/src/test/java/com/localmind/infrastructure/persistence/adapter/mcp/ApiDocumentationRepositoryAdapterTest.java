package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.ApiDocumentation;
import com.localmind.infrastructure.persistence.entity.mcp.ApiDocumentationEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaApiDocumentationRepository;
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
class ApiDocumentationRepositoryAdapterTest {

    @Mock
    private JpaApiDocumentationRepository jpaRepository;

    private ApiDocumentationRepositoryAdapter adapter;

    private static final UUID TEST_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final Instant TEST_INSTANT = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        adapter = new ApiDocumentationRepositoryAdapter(jpaRepository);
    }

    @Test
    void save_mapsDomainToEntityCorrectly() {
        ApiDocumentation domain = ApiDocumentation.builder()
                .id(TEST_UUID.toString())
                .documentationType("extract-endpoints")
                .filePath("/src/Controller.java")
                .resultJson("{\"endpointCount\":3}")
                .createdAt(TEST_INSTANT)
                .build();

        ApiDocumentationEntity savedEntity = ApiDocumentationEntity.builder()
                .id(TEST_UUID)
                .documentationType("extract-endpoints")
                .filePath("/src/Controller.java")
                .result("{\"endpointCount\":3}")
                .createdAt(TEST_INSTANT)
                .build();

        when(jpaRepository.save(any(ApiDocumentationEntity.class))).thenReturn(savedEntity);

        adapter.save(domain);

        ArgumentCaptor<ApiDocumentationEntity> captor = ArgumentCaptor.forClass(ApiDocumentationEntity.class);
        verify(jpaRepository).save(captor.capture());

        ApiDocumentationEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getDocumentationType()).isEqualTo("extract-endpoints");
        assertThat(captured.getFilePath()).isEqualTo("/src/Controller.java");
        assertThat(captured.getResult()).isEqualTo("{\"endpointCount\":3}");
        assertThat(captured.getCreatedAt()).isEqualTo(TEST_INSTANT);
    }

    @Test
    void save_mapsEntityToDomainCorrectly() {
        ApiDocumentation domain = ApiDocumentation.builder()
                .id(TEST_UUID.toString())
                .documentationType("generate-openapi")
                .filePath(null)
                .resultJson("{\"spec\":{}}")
                .createdAt(TEST_INSTANT)
                .build();

        ApiDocumentationEntity savedEntity = ApiDocumentationEntity.builder()
                .id(TEST_UUID)
                .documentationType("generate-openapi")
                .filePath(null)
                .result("{\"spec\":{}}")
                .createdAt(TEST_INSTANT)
                .build();

        when(jpaRepository.save(any(ApiDocumentationEntity.class))).thenReturn(savedEntity);

        ApiDocumentation result = adapter.save(domain);

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getDocumentationType()).isEqualTo("generate-openapi");
        assertThat(result.getResultJson()).isEqualTo("{\"spec\":{}}");
        assertThat(result.getCreatedAt()).isEqualTo(TEST_INSTANT);
    }

    @Test
    void save_withNullId_doesNotSetIdOnEntity() {
        ApiDocumentation domain = ApiDocumentation.builder()
                .documentationType("find-undocumented")
                .filePath("/src/module.ts")
                .resultJson("{}")
                .createdAt(TEST_INSTANT)
                .build();

        ApiDocumentationEntity savedEntity = ApiDocumentationEntity.builder()
                .id(TEST_UUID)
                .documentationType("find-undocumented")
                .filePath("/src/module.ts")
                .result("{}")
                .createdAt(TEST_INSTANT)
                .build();

        when(jpaRepository.save(any(ApiDocumentationEntity.class))).thenReturn(savedEntity);

        adapter.save(domain);

        ArgumentCaptor<ApiDocumentationEntity> captor = ArgumentCaptor.forClass(ApiDocumentationEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isNull();
    }
}
