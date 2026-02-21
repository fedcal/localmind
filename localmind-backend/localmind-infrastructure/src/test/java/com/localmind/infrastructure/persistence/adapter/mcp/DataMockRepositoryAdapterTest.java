package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.GeneratedDataset;
import com.localmind.infrastructure.persistence.entity.mcp.GeneratedDatasetEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaGeneratedDatasetRepository;
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
class DataMockRepositoryAdapterTest {

    @Mock
    private JpaGeneratedDatasetRepository jpaRepository;

    private DataMockRepositoryAdapter adapter;

    private final UUID TEST_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final Instant NOW = Instant.parse("2026-02-12T10:00:00Z");

    @BeforeEach
    void setUp() {
        adapter = new DataMockRepositoryAdapter(jpaRepository);
    }

    @Test
    void save_mapsCorrectlyFromDomainToEntityAndBack() {
        GeneratedDataset domain = GeneratedDataset.builder()
                .id(TEST_UUID.toString())
                .name("test-dataset")
                .format("structured")
                .rowCount(10)
                .schemaJson("[{\"field\":\"name\",\"type\":\"firstName\"}]")
                .sampleDataJson("[{\"name\":\"Alice\"}]")
                .createdAt(NOW)
                .build();

        GeneratedDatasetEntity savedEntity = GeneratedDatasetEntity.builder()
                .id(TEST_UUID)
                .name("test-dataset")
                .format("structured")
                .rowCount(10)
                .schemaJson("[{\"field\":\"name\",\"type\":\"firstName\"}]")
                .sampleData("[{\"name\":\"Alice\"}]")
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(GeneratedDatasetEntity.class))).thenReturn(savedEntity);

        GeneratedDataset result = adapter.save(domain);

        ArgumentCaptor<GeneratedDatasetEntity> captor = ArgumentCaptor.forClass(GeneratedDatasetEntity.class);
        verify(jpaRepository).save(captor.capture());

        GeneratedDatasetEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getName()).isEqualTo("test-dataset");
        assertThat(captured.getFormat()).isEqualTo("structured");
        assertThat(captured.getRowCount()).isEqualTo(10);
        assertThat(captured.getSchemaJson()).isEqualTo("[{\"field\":\"name\",\"type\":\"firstName\"}]");
        assertThat(captured.getSampleData()).isEqualTo("[{\"name\":\"Alice\"}]");
        assertThat(captured.getCreatedAt()).isEqualTo(NOW);

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getName()).isEqualTo("test-dataset");
        assertThat(result.getFormat()).isEqualTo("structured");
        assertThat(result.getRowCount()).isEqualTo(10);
        assertThat(result.getSchemaJson()).isEqualTo("[{\"field\":\"name\",\"type\":\"firstName\"}]");
        assertThat(result.getSampleDataJson()).isEqualTo("[{\"name\":\"Alice\"}]");
        assertThat(result.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void save_withNullId_doesNotSetIdOnEntity() {
        GeneratedDataset domain = GeneratedDataset.builder()
                .id(null)
                .name("csv-export")
                .format("csv")
                .rowCount(5)
                .schemaJson(null)
                .sampleDataJson("Name,Email\nAlice,alice@test.com")
                .createdAt(NOW)
                .build();

        GeneratedDatasetEntity savedEntity = GeneratedDatasetEntity.builder()
                .id(TEST_UUID)
                .name("csv-export")
                .format("csv")
                .rowCount(5)
                .schemaJson(null)
                .sampleData("Name,Email\nAlice,alice@test.com")
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(GeneratedDatasetEntity.class))).thenReturn(savedEntity);

        GeneratedDataset result = adapter.save(domain);

        ArgumentCaptor<GeneratedDatasetEntity> captor = ArgumentCaptor.forClass(GeneratedDatasetEntity.class);
        verify(jpaRepository).save(captor.capture());

        GeneratedDatasetEntity captured = captor.getValue();
        assertThat(captured.getId()).isNull();

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getName()).isEqualTo("csv-export");
        assertThat(result.getFormat()).isEqualTo("csv");
    }

    @Test
    void save_withNullSchemaAndSampleData_handlesGracefully() {
        GeneratedDataset domain = GeneratedDataset.builder()
                .id(TEST_UUID.toString())
                .name("empty")
                .format("json-schema")
                .rowCount(0)
                .schemaJson(null)
                .sampleDataJson(null)
                .createdAt(NOW)
                .build();

        GeneratedDatasetEntity savedEntity = GeneratedDatasetEntity.builder()
                .id(TEST_UUID)
                .name("empty")
                .format("json-schema")
                .rowCount(0)
                .schemaJson(null)
                .sampleData(null)
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(GeneratedDatasetEntity.class))).thenReturn(savedEntity);

        GeneratedDataset result = adapter.save(domain);

        ArgumentCaptor<GeneratedDatasetEntity> captor = ArgumentCaptor.forClass(GeneratedDatasetEntity.class);
        verify(jpaRepository).save(captor.capture());

        assertThat(captor.getValue().getSchemaJson()).isNull();
        assertThat(captor.getValue().getSampleData()).isNull();
        assertThat(result.getSchemaJson()).isNull();
        assertThat(result.getSampleDataJson()).isNull();
    }
}
