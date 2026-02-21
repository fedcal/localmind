package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.BenchmarkResult;
import com.localmind.infrastructure.persistence.entity.mcp.BenchmarkResultEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaBenchmarkResultRepository;
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
class PerformanceProfilerRepositoryAdapterTest {

    @Mock
    private JpaBenchmarkResultRepository jpaRepository;

    private PerformanceProfilerRepositoryAdapter adapter;

    private final UUID TEST_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final Instant NOW = Instant.parse("2026-02-12T10:00:00Z");

    @BeforeEach
    void setUp() {
        adapter = new PerformanceProfilerRepositoryAdapter(jpaRepository);
    }

    @Test
    void save_mapsCorrectlyFromDomainToEntityAndBack() {
        BenchmarkResult domain = BenchmarkResult.builder()
                .id(TEST_UUID.toString())
                .name("bundle-analysis:src/index.js")
                .language("javascript")
                .resultJson("{\"totalImports\":5}")
                .createdAt(NOW)
                .build();

        BenchmarkResultEntity savedEntity = BenchmarkResultEntity.builder()
                .id(TEST_UUID)
                .name("bundle-analysis:src/index.js")
                .language("javascript")
                .result("{\"totalImports\":5}")
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(BenchmarkResultEntity.class))).thenReturn(savedEntity);

        BenchmarkResult result = adapter.save(domain);

        ArgumentCaptor<BenchmarkResultEntity> captor = ArgumentCaptor.forClass(BenchmarkResultEntity.class);
        verify(jpaRepository).save(captor.capture());

        BenchmarkResultEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getName()).isEqualTo("bundle-analysis:src/index.js");
        assertThat(captured.getLanguage()).isEqualTo("javascript");
        assertThat(captured.getResult()).isEqualTo("{\"totalImports\":5}");
        assertThat(captured.getCreatedAt()).isEqualTo(NOW);

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getName()).isEqualTo("bundle-analysis:src/index.js");
        assertThat(result.getLanguage()).isEqualTo("javascript");
        assertThat(result.getResultJson()).isEqualTo("{\"totalImports\":5}");
        assertThat(result.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void save_withNullId_doesNotSetIdOnEntity() {
        BenchmarkResult domain = BenchmarkResult.builder()
                .id(null)
                .name("bottleneck-analysis")
                .language("java")
                .resultJson("{\"totalBottlenecks\":3}")
                .createdAt(NOW)
                .build();

        BenchmarkResultEntity savedEntity = BenchmarkResultEntity.builder()
                .id(TEST_UUID)
                .name("bottleneck-analysis")
                .language("java")
                .result("{\"totalBottlenecks\":3}")
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(BenchmarkResultEntity.class))).thenReturn(savedEntity);

        BenchmarkResult result = adapter.save(domain);

        ArgumentCaptor<BenchmarkResultEntity> captor = ArgumentCaptor.forClass(BenchmarkResultEntity.class);
        verify(jpaRepository).save(captor.capture());

        BenchmarkResultEntity captured = captor.getValue();
        assertThat(captured.getId()).isNull();

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getName()).isEqualTo("bottleneck-analysis");
        assertThat(result.getLanguage()).isEqualTo("java");
    }

    @Test
    void save_benchmarkCompareResult_mapsAllFields() {
        String benchmarkJson = "{\"language\":\"java\",\"iterations\":1000,\"benchmarkCode\":\"public class Benchmark {}\"}";

        BenchmarkResult domain = BenchmarkResult.builder()
                .id(TEST_UUID.toString())
                .name("benchmark-compare")
                .language("java")
                .resultJson(benchmarkJson)
                .createdAt(NOW)
                .build();

        BenchmarkResultEntity savedEntity = BenchmarkResultEntity.builder()
                .id(TEST_UUID)
                .name("benchmark-compare")
                .language("java")
                .result(benchmarkJson)
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(BenchmarkResultEntity.class))).thenReturn(savedEntity);

        BenchmarkResult result = adapter.save(domain);

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getName()).isEqualTo("benchmark-compare");
        assertThat(result.getLanguage()).isEqualTo("java");
        assertThat(result.getResultJson()).isEqualTo(benchmarkJson);
        assertThat(result.getCreatedAt()).isEqualTo(NOW);

        verify(jpaRepository).save(any(BenchmarkResultEntity.class));
    }

    @Test
    void save_withNullResultJson_handlesGracefully() {
        BenchmarkResult domain = BenchmarkResult.builder()
                .id(TEST_UUID.toString())
                .name("test")
                .language("java")
                .resultJson(null)
                .createdAt(NOW)
                .build();

        BenchmarkResultEntity savedEntity = BenchmarkResultEntity.builder()
                .id(TEST_UUID)
                .name("test")
                .language("java")
                .result(null)
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(BenchmarkResultEntity.class))).thenReturn(savedEntity);

        BenchmarkResult result = adapter.save(domain);

        ArgumentCaptor<BenchmarkResultEntity> captor = ArgumentCaptor.forClass(BenchmarkResultEntity.class);
        verify(jpaRepository).save(captor.capture());

        assertThat(captor.getValue().getResult()).isNull();
        assertThat(result.getResultJson()).isNull();
    }
}
