package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.SchemaExploration;
import com.localmind.infrastructure.persistence.entity.mcp.SchemaExplorationEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaSchemaExplorationRepository;
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
class SchemaExplorationRepositoryAdapterTest {

    @Mock
    private JpaSchemaExplorationRepository jpaRepository;

    private SchemaExplorationRepositoryAdapter adapter;

    private final UUID TEST_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final Instant NOW = Instant.parse("2026-02-12T10:00:00Z");

    @BeforeEach
    void setUp() {
        adapter = new SchemaExplorationRepositoryAdapter(jpaRepository);
    }

    @Test
    void save_mapsDomainToEntityCorrectly() {
        SchemaExploration domain = SchemaExploration.builder()
                .id(TEST_UUID.toString())
                .jdbcUrl("jdbc:mysql://localhost/testdb")
                .tableCount(5)
                .resultJson("{\"tableCount\": 5}")
                .exploredAt(NOW)
                .build();

        SchemaExplorationEntity savedEntity = SchemaExplorationEntity.builder()
                .id(TEST_UUID)
                .jdbcUrl("jdbc:mysql://localhost/testdb")
                .tableCount(5)
                .result("{\"tableCount\": 5}")
                .exploredAt(NOW)
                .build();

        when(jpaRepository.save(any(SchemaExplorationEntity.class))).thenReturn(savedEntity);

        SchemaExploration result = adapter.save(domain);

        ArgumentCaptor<SchemaExplorationEntity> captor = ArgumentCaptor.forClass(SchemaExplorationEntity.class);
        verify(jpaRepository).save(captor.capture());

        SchemaExplorationEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getJdbcUrl()).isEqualTo("jdbc:mysql://localhost/testdb");
        assertThat(captured.getTableCount()).isEqualTo(5);
        assertThat(captured.getResult()).isEqualTo("{\"tableCount\": 5}");
        assertThat(captured.getExploredAt()).isEqualTo(NOW);

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getJdbcUrl()).isEqualTo("jdbc:mysql://localhost/testdb");
        assertThat(result.getTableCount()).isEqualTo(5);
        assertThat(result.getResultJson()).isEqualTo("{\"tableCount\": 5}");
        assertThat(result.getExploredAt()).isEqualTo(NOW);
    }

    @Test
    void save_withNullId_doesNotSetIdOnEntity() {
        SchemaExploration domain = SchemaExploration.builder()
                .id(null)
                .jdbcUrl("jdbc:mysql://localhost/testdb")
                .tableCount(3)
                .resultJson("{\"tables\":[]}")
                .exploredAt(NOW)
                .build();

        UUID generatedUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
        SchemaExplorationEntity savedEntity = SchemaExplorationEntity.builder()
                .id(generatedUuid)
                .jdbcUrl("jdbc:mysql://localhost/testdb")
                .tableCount(3)
                .result("{\"tables\":[]}")
                .exploredAt(NOW)
                .build();

        when(jpaRepository.save(any(SchemaExplorationEntity.class))).thenReturn(savedEntity);

        SchemaExploration result = adapter.save(domain);

        ArgumentCaptor<SchemaExplorationEntity> captor = ArgumentCaptor.forClass(SchemaExplorationEntity.class);
        verify(jpaRepository).save(captor.capture());

        SchemaExplorationEntity captured = captor.getValue();
        assertThat(captured.getId()).isNull();

        assertThat(result.getId()).isEqualTo(generatedUuid.toString());
    }

    @Test
    void save_delegatesToJpaRepository() {
        SchemaExploration domain = SchemaExploration.builder()
                .id(TEST_UUID.toString())
                .jdbcUrl("jdbc:mysql://localhost/testdb")
                .tableCount(2)
                .resultJson("r")
                .exploredAt(NOW)
                .build();

        SchemaExplorationEntity savedEntity = SchemaExplorationEntity.builder()
                .id(TEST_UUID)
                .jdbcUrl("jdbc:mysql://localhost/testdb")
                .tableCount(2)
                .result("r")
                .exploredAt(NOW)
                .build();

        when(jpaRepository.save(any(SchemaExplorationEntity.class))).thenReturn(savedEntity);

        adapter.save(domain);

        verify(jpaRepository).save(any(SchemaExplorationEntity.class));
    }
}
