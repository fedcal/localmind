package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.Incident;
import com.localmind.infrastructure.persistence.entity.mcp.IncidentEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaIncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncidentRepositoryAdapterTest {

    @Mock
    private JpaIncidentRepository jpaRepository;

    private IncidentRepositoryAdapter adapter;

    private final UUID TEST_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final Instant NOW = Instant.parse("2026-02-13T10:00:00Z");
    private final Instant RESOLVED_AT = Instant.parse("2026-02-13T14:00:00Z");

    @BeforeEach
    void setUp() {
        adapter = new IncidentRepositoryAdapter(jpaRepository);
    }

    @Test
    void save_mapsDomainToEntityAndBack() {
        Incident domain = Incident.builder()
                .id(TEST_UUID.toString())
                .title("DB Outage")
                .severity("critical")
                .status("resolved")
                .description("Database connection pool exhausted")
                .affectedSystemsJson("[\"api\",\"auth\"]")
                .timelineJson("[{\"timestamp\":\"2026-02-13T10:00:00Z\",\"description\":\"opened\",\"source\":\"system\"}]")
                .resolution("Increased pool size")
                .rootCause("Connection leak")
                .createdAt(NOW)
                .resolvedAt(RESOLVED_AT)
                .build();

        IncidentEntity savedEntity = IncidentEntity.builder()
                .id(TEST_UUID)
                .title("DB Outage")
                .severity("critical")
                .status("resolved")
                .description("Database connection pool exhausted")
                .affectedSystemsJson("[\"api\",\"auth\"]")
                .timelineJson("[{\"timestamp\":\"2026-02-13T10:00:00Z\",\"description\":\"opened\",\"source\":\"system\"}]")
                .resolution("Increased pool size")
                .rootCause("Connection leak")
                .createdAt(NOW)
                .resolvedAt(RESOLVED_AT)
                .build();

        when(jpaRepository.save(any(IncidentEntity.class))).thenReturn(savedEntity);

        Incident result = adapter.save(domain);

        ArgumentCaptor<IncidentEntity> captor = ArgumentCaptor.forClass(IncidentEntity.class);
        verify(jpaRepository).save(captor.capture());

        IncidentEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getTitle()).isEqualTo("DB Outage");
        assertThat(captured.getSeverity()).isEqualTo("critical");
        assertThat(captured.getStatus()).isEqualTo("resolved");
        assertThat(captured.getDescription()).isEqualTo("Database connection pool exhausted");
        assertThat(captured.getResolution()).isEqualTo("Increased pool size");
        assertThat(captured.getRootCause()).isEqualTo("Connection leak");
        assertThat(captured.getCreatedAt()).isEqualTo(NOW);
        assertThat(captured.getResolvedAt()).isEqualTo(RESOLVED_AT);

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getTitle()).isEqualTo("DB Outage");
        assertThat(result.getSeverity()).isEqualTo("critical");
        assertThat(result.getResolution()).isEqualTo("Increased pool size");
        assertThat(result.getResolvedAt()).isEqualTo(RESOLVED_AT);
    }

    @Test
    void findById_whenFound_returnsMappedDomain() {
        IncidentEntity entity = IncidentEntity.builder()
                .id(TEST_UUID)
                .title("Network Issue")
                .severity("high")
                .status("investigating")
                .description("Packet loss detected")
                .createdAt(NOW)
                .build();

        when(jpaRepository.findById(TEST_UUID)).thenReturn(Optional.of(entity));

        Optional<Incident> result = adapter.findById(TEST_UUID.toString());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.get().getTitle()).isEqualTo("Network Issue");
        assertThat(result.get().getSeverity()).isEqualTo("high");
        assertThat(result.get().getStatus()).isEqualTo("investigating");
    }

    @Test
    void findById_whenNotFound_returnsEmpty() {
        when(jpaRepository.findById(TEST_UUID)).thenReturn(Optional.empty());

        Optional<Incident> result = adapter.findById(TEST_UUID.toString());

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_mapsAllEntities() {
        UUID uuid2 = UUID.fromString("b1b2c3d4-e5f6-7890-abcd-ef1234567890");
        IncidentEntity entity1 = IncidentEntity.builder()
                .id(TEST_UUID).title("Inc 1").severity("high").status("open")
                .description("D1").createdAt(NOW).build();
        IncidentEntity entity2 = IncidentEntity.builder()
                .id(uuid2).title("Inc 2").severity("low").status("resolved")
                .description("D2").createdAt(NOW).resolvedAt(RESOLVED_AT).build();

        when(jpaRepository.findAll()).thenReturn(List.of(entity1, entity2));

        List<Incident> result = adapter.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.get(0).getTitle()).isEqualTo("Inc 1");
        assertThat(result.get(1).getId()).isEqualTo(uuid2.toString());
        assertThat(result.get(1).getTitle()).isEqualTo("Inc 2");
        assertThat(result.get(1).getResolvedAt()).isEqualTo(RESOLVED_AT);
    }

    @Test
    void save_withNullId_doesNotSetIdOnEntity() {
        Incident domain = Incident.builder()
                .title("New Incident")
                .severity("medium")
                .status("open")
                .description("Something")
                .createdAt(NOW)
                .build();

        IncidentEntity savedEntity = IncidentEntity.builder()
                .id(TEST_UUID)
                .title("New Incident")
                .severity("medium")
                .status("open")
                .description("Something")
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(IncidentEntity.class))).thenReturn(savedEntity);

        adapter.save(domain);

        ArgumentCaptor<IncidentEntity> captor = ArgumentCaptor.forClass(IncidentEntity.class);
        verify(jpaRepository).save(captor.capture());

        IncidentEntity captured = captor.getValue();
        assertThat(captured.getId()).isNull();
    }
}
