package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.TimeEntry;
import com.localmind.domain.mcp.model.TimeEstimate;
import com.localmind.infrastructure.persistence.entity.mcp.TimeEntryEntity;
import com.localmind.infrastructure.persistence.entity.mcp.TimeEstimateEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaTimeEntryRepository;
import com.localmind.infrastructure.persistence.repository.mcp.JpaTimeEstimateRepository;
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
class TimeTrackingRepositoryAdapterTest {

    @Mock
    private JpaTimeEntryRepository timeEntryJpaRepository;

    @Mock
    private JpaTimeEstimateRepository timeEstimateJpaRepository;

    private TimeTrackingRepositoryAdapter adapter;

    private final UUID TEST_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final Instant NOW = Instant.parse("2026-02-12T10:00:00Z");

    @BeforeEach
    void setUp() {
        adapter = new TimeTrackingRepositoryAdapter(timeEntryJpaRepository, timeEstimateJpaRepository);
    }

    @Test
    void saveEntry_mapsCorrectlyFromDomainToEntityAndBack() {
        TimeEntry domain = TimeEntry.builder()
                .id(TEST_UUID.toString())
                .taskId("TASK-001")
                .userId("user1")
                .durationMinutes(120)
                .description("Feature development")
                .entryDate("2026-02-12")
                .timerStartedAt(null)
                .createdAt(NOW)
                .build();

        TimeEntryEntity savedEntity = TimeEntryEntity.builder()
                .id(TEST_UUID)
                .taskId("TASK-001")
                .userId("user1")
                .durationMinutes(120)
                .description("Feature development")
                .entryDate("2026-02-12")
                .timerStartedAt(null)
                .createdAt(NOW)
                .build();

        when(timeEntryJpaRepository.save(any(TimeEntryEntity.class))).thenReturn(savedEntity);

        TimeEntry result = adapter.saveEntry(domain);

        ArgumentCaptor<TimeEntryEntity> captor = ArgumentCaptor.forClass(TimeEntryEntity.class);
        verify(timeEntryJpaRepository).save(captor.capture());

        TimeEntryEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getTaskId()).isEqualTo("TASK-001");
        assertThat(captured.getUserId()).isEqualTo("user1");
        assertThat(captured.getDurationMinutes()).isEqualTo(120);
        assertThat(captured.getDescription()).isEqualTo("Feature development");
        assertThat(captured.getEntryDate()).isEqualTo("2026-02-12");
        assertThat(captured.getTimerStartedAt()).isNull();
        assertThat(captured.getCreatedAt()).isEqualTo(NOW);

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getTaskId()).isEqualTo("TASK-001");
        assertThat(result.getUserId()).isEqualTo("user1");
        assertThat(result.getDurationMinutes()).isEqualTo(120);
    }

    @Test
    void saveEstimate_mapsCorrectlyFromDomainToEntityAndBack() {
        TimeEstimate domain = TimeEstimate.builder()
                .id(TEST_UUID.toString())
                .taskId("TASK-002")
                .estimateMinutes(240)
                .description("Sprint estimate")
                .createdAt(NOW)
                .build();

        TimeEstimateEntity savedEntity = TimeEstimateEntity.builder()
                .id(TEST_UUID)
                .taskId("TASK-002")
                .estimateMinutes(240)
                .description("Sprint estimate")
                .createdAt(NOW)
                .build();

        when(timeEstimateJpaRepository.save(any(TimeEstimateEntity.class))).thenReturn(savedEntity);

        TimeEstimate result = adapter.saveEstimate(domain);

        ArgumentCaptor<TimeEstimateEntity> captor = ArgumentCaptor.forClass(TimeEstimateEntity.class);
        verify(timeEstimateJpaRepository).save(captor.capture());

        TimeEstimateEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getTaskId()).isEqualTo("TASK-002");
        assertThat(captured.getEstimateMinutes()).isEqualTo(240);
        assertThat(captured.getDescription()).isEqualTo("Sprint estimate");
        assertThat(captured.getCreatedAt()).isEqualTo(NOW);

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getTaskId()).isEqualTo("TASK-002");
        assertThat(result.getEstimateMinutes()).isEqualTo(240);
    }

    @Test
    void findActiveTimer_whenFound_returnsMappedDomain() {
        TimeEntryEntity entity = TimeEntryEntity.builder()
                .id(TEST_UUID)
                .taskId("TASK-003")
                .userId("user1")
                .durationMinutes(0)
                .description("In progress")
                .entryDate("2026-02-12")
                .timerStartedAt(NOW)
                .createdAt(NOW)
                .build();

        when(timeEntryJpaRepository.findFirstByTimerStartedAtIsNotNull()).thenReturn(Optional.of(entity));

        Optional<TimeEntry> result = adapter.findActiveTimer();

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.get().getTaskId()).isEqualTo("TASK-003");
        assertThat(result.get().getTimerStartedAt()).isEqualTo(NOW);
    }

    @Test
    void findEntriesByDateRange_withUserId_usesFilteredQuery() {
        UUID uuid2 = UUID.fromString("b1b2c3d4-e5f6-7890-abcd-ef1234567890");
        TimeEntryEntity entity1 = TimeEntryEntity.builder()
                .id(TEST_UUID).taskId("T1").userId("user1").durationMinutes(60)
                .description("Task 1").entryDate("2026-02-10").createdAt(NOW)
                .build();
        TimeEntryEntity entity2 = TimeEntryEntity.builder()
                .id(uuid2).taskId("T2").userId("user1").durationMinutes(120)
                .description("Task 2").entryDate("2026-02-11").createdAt(NOW)
                .build();

        when(timeEntryJpaRepository.findByEntryDateBetweenAndUserId("2026-02-10", "2026-02-12", "user1"))
                .thenReturn(List.of(entity1, entity2));

        List<TimeEntry> result = adapter.findEntriesByDateRange("2026-02-10", "2026-02-12", "user1");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.get(0).getTaskId()).isEqualTo("T1");
        assertThat(result.get(0).getDurationMinutes()).isEqualTo(60);

        assertThat(result.get(1).getId()).isEqualTo(uuid2.toString());
        assertThat(result.get(1).getTaskId()).isEqualTo("T2");
        assertThat(result.get(1).getDurationMinutes()).isEqualTo(120);
    }

    @Test
    void findEstimateByTaskId_whenFound_returnsMappedDomain() {
        TimeEstimateEntity entity = TimeEstimateEntity.builder()
                .id(TEST_UUID)
                .taskId("TASK-004")
                .estimateMinutes(480)
                .description("Full day task")
                .createdAt(NOW)
                .build();

        when(timeEstimateJpaRepository.findByTaskId("TASK-004")).thenReturn(Optional.of(entity));

        Optional<TimeEstimate> result = adapter.findEstimateByTaskId("TASK-004");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.get().getTaskId()).isEqualTo("TASK-004");
        assertThat(result.get().getEstimateMinutes()).isEqualTo(480);
        assertThat(result.get().getDescription()).isEqualTo("Full day task");
    }

    @Test
    void findEntriesByTaskId_returnsMappedDomainList() {
        TimeEntryEntity entity = TimeEntryEntity.builder()
                .id(TEST_UUID)
                .taskId("TASK-005")
                .userId("user1")
                .durationMinutes(90)
                .description("Implementation")
                .entryDate("2026-02-12")
                .createdAt(NOW)
                .build();

        when(timeEntryJpaRepository.findByTaskId("TASK-005")).thenReturn(List.of(entity));

        List<TimeEntry> result = adapter.findEntriesByTaskId("TASK-005");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.get(0).getTaskId()).isEqualTo("TASK-005");
        assertThat(result.get(0).getDurationMinutes()).isEqualTo(90);
        assertThat(result.get(0).getDescription()).isEqualTo("Implementation");
    }
}
