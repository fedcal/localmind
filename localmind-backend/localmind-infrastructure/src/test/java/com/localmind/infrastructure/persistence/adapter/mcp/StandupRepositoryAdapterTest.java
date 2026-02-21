package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.StandupNote;
import com.localmind.infrastructure.persistence.entity.mcp.StandupNoteEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaStandupNoteRepository;
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
class StandupRepositoryAdapterTest {

    @Mock
    private JpaStandupNoteRepository jpaRepository;

    private StandupRepositoryAdapter adapter;

    private final UUID TEST_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final Instant NOW = Instant.parse("2026-02-13T10:00:00Z");

    @BeforeEach
    void setUp() {
        adapter = new StandupRepositoryAdapter(jpaRepository);
    }

    @Test
    void save_mapsDomainToEntityCorrectly() {
        StandupNote domain = StandupNote.builder()
                .id(TEST_UUID.toString())
                .yesterday("Fixed bug #123")
                .today("Work on feature X")
                .blockers("Blocked by API")
                .standupDate("2026-02-13")
                .createdAt(NOW)
                .build();

        StandupNoteEntity savedEntity = StandupNoteEntity.builder()
                .id(TEST_UUID)
                .yesterday("Fixed bug #123")
                .today("Work on feature X")
                .blockers("Blocked by API")
                .standupDate("2026-02-13")
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(StandupNoteEntity.class))).thenReturn(savedEntity);

        StandupNote result = adapter.save(domain);

        ArgumentCaptor<StandupNoteEntity> captor = ArgumentCaptor.forClass(StandupNoteEntity.class);
        verify(jpaRepository).save(captor.capture());

        StandupNoteEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getYesterday()).isEqualTo("Fixed bug #123");
        assertThat(captured.getToday()).isEqualTo("Work on feature X");
        assertThat(captured.getBlockers()).isEqualTo("Blocked by API");
        assertThat(captured.getStandupDate()).isEqualTo("2026-02-13");
        assertThat(captured.getCreatedAt()).isEqualTo(NOW);

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getYesterday()).isEqualTo("Fixed bug #123");
        assertThat(result.getToday()).isEqualTo("Work on feature X");
        assertThat(result.getBlockers()).isEqualTo("Blocked by API");
        assertThat(result.getStandupDate()).isEqualTo("2026-02-13");
        assertThat(result.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void save_withNullId_doesNotSetIdOnEntity() {
        StandupNote domain = StandupNote.builder()
                .yesterday("Did work")
                .today("More work")
                .standupDate("2026-02-13")
                .createdAt(NOW)
                .build();

        StandupNoteEntity savedEntity = StandupNoteEntity.builder()
                .id(TEST_UUID)
                .yesterday("Did work")
                .today("More work")
                .standupDate("2026-02-13")
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(StandupNoteEntity.class))).thenReturn(savedEntity);

        adapter.save(domain);

        ArgumentCaptor<StandupNoteEntity> captor = ArgumentCaptor.forClass(StandupNoteEntity.class);
        verify(jpaRepository).save(captor.capture());

        StandupNoteEntity captured = captor.getValue();
        assertThat(captured.getId()).isNull();
        assertThat(captured.getYesterday()).isEqualTo("Did work");
    }

    @Test
    void findAll_mapsEntitiesToDomain() {
        StandupNoteEntity entity1 = StandupNoteEntity.builder()
                .id(TEST_UUID)
                .yesterday("Y1")
                .today("T1")
                .blockers("B1")
                .standupDate("2026-02-13")
                .createdAt(NOW)
                .build();

        UUID secondUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
        StandupNoteEntity entity2 = StandupNoteEntity.builder()
                .id(secondUuid)
                .yesterday("Y2")
                .today("T2")
                .blockers(null)
                .standupDate("2026-02-12")
                .createdAt(NOW.plusSeconds(60))
                .build();

        when(jpaRepository.findAllByOrderByStandupDateDesc())
                .thenReturn(List.of(entity1, entity2));

        List<StandupNote> results = adapter.findAll();

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getId()).isEqualTo(TEST_UUID.toString());
        assertThat(results.get(0).getYesterday()).isEqualTo("Y1");
        assertThat(results.get(0).getBlockers()).isEqualTo("B1");
        assertThat(results.get(1).getId()).isEqualTo(secondUuid.toString());
        assertThat(results.get(1).getBlockers()).isNull();
    }
}
