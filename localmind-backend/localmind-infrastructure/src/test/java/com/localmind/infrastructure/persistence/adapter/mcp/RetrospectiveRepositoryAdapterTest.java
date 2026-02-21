package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.ActionItem;
import com.localmind.domain.mcp.model.RetroItem;
import com.localmind.domain.mcp.model.Retrospective;
import com.localmind.infrastructure.persistence.entity.mcp.ActionItemEntity;
import com.localmind.infrastructure.persistence.entity.mcp.RetroItemEntity;
import com.localmind.infrastructure.persistence.entity.mcp.RetrospectiveEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaActionItemRepository;
import com.localmind.infrastructure.persistence.repository.mcp.JpaRetroItemRepository;
import com.localmind.infrastructure.persistence.repository.mcp.JpaRetrospectiveRepository;
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
class RetrospectiveRepositoryAdapterTest {

    @Mock
    private JpaRetrospectiveRepository retroJpaRepository;

    @Mock
    private JpaRetroItemRepository itemJpaRepository;

    @Mock
    private JpaActionItemRepository actionItemJpaRepository;

    private RetrospectiveRepositoryAdapter adapter;

    private final UUID TEST_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final UUID TEST_UUID_2 = UUID.fromString("b1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final Instant NOW = Instant.parse("2026-02-12T10:00:00Z");

    @BeforeEach
    void setUp() {
        adapter = new RetrospectiveRepositoryAdapter(retroJpaRepository, itemJpaRepository, actionItemJpaRepository);
    }

    // --- Retrospective Tests ---

    @Test
    void saveRetro_mapsDomainToEntityAndBack() {
        Retrospective domain = Retrospective.builder()
                .id(TEST_UUID.toString())
                .sprintId("sprint-1")
                .format("mad-sad-glad")
                .createdAt(NOW)
                .build();

        RetrospectiveEntity savedEntity = RetrospectiveEntity.builder()
                .id(TEST_UUID)
                .sprintId("sprint-1")
                .format("mad-sad-glad")
                .createdAt(NOW)
                .build();

        when(retroJpaRepository.save(any(RetrospectiveEntity.class))).thenReturn(savedEntity);

        Retrospective result = adapter.saveRetro(domain);

        ArgumentCaptor<RetrospectiveEntity> captor = ArgumentCaptor.forClass(RetrospectiveEntity.class);
        verify(retroJpaRepository).save(captor.capture());

        RetrospectiveEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getSprintId()).isEqualTo("sprint-1");
        assertThat(captured.getFormat()).isEqualTo("mad-sad-glad");
        assertThat(captured.getCreatedAt()).isEqualTo(NOW);

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getSprintId()).isEqualTo("sprint-1");
        assertThat(result.getFormat()).isEqualTo("mad-sad-glad");
        assertThat(result.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void findRetroById_whenFound_returnsMappedDomain() {
        RetrospectiveEntity entity = RetrospectiveEntity.builder()
                .id(TEST_UUID)
                .sprintId("sprint-2")
                .format("4ls")
                .createdAt(NOW)
                .build();

        when(retroJpaRepository.findById(TEST_UUID)).thenReturn(Optional.of(entity));

        Optional<Retrospective> result = adapter.findRetroById(TEST_UUID.toString());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.get().getSprintId()).isEqualTo("sprint-2");
        assertThat(result.get().getFormat()).isEqualTo("4ls");
    }

    @Test
    void findRetroById_whenNotFound_returnsEmpty() {
        when(retroJpaRepository.findById(TEST_UUID)).thenReturn(Optional.empty());

        Optional<Retrospective> result = adapter.findRetroById(TEST_UUID.toString());

        assertThat(result).isEmpty();
    }

    // --- RetroItem Tests ---

    @Test
    void saveItem_mapsDomainToEntityAndBack() {
        RetroItem domain = RetroItem.builder()
                .id(TEST_UUID.toString())
                .retroId("retro-1")
                .category("mad")
                .content("Slow builds")
                .votes(5)
                .createdAt(NOW)
                .build();

        RetroItemEntity savedEntity = RetroItemEntity.builder()
                .id(TEST_UUID)
                .retroId("retro-1")
                .category("mad")
                .content("Slow builds")
                .votes(5)
                .createdAt(NOW)
                .build();

        when(itemJpaRepository.save(any(RetroItemEntity.class))).thenReturn(savedEntity);

        RetroItem result = adapter.saveItem(domain);

        ArgumentCaptor<RetroItemEntity> captor = ArgumentCaptor.forClass(RetroItemEntity.class);
        verify(itemJpaRepository).save(captor.capture());

        RetroItemEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getRetroId()).isEqualTo("retro-1");
        assertThat(captured.getCategory()).isEqualTo("mad");
        assertThat(captured.getContent()).isEqualTo("Slow builds");
        assertThat(captured.getVotes()).isEqualTo(5);
        assertThat(captured.getCreatedAt()).isEqualTo(NOW);

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getRetroId()).isEqualTo("retro-1");
        assertThat(result.getCategory()).isEqualTo("mad");
        assertThat(result.getContent()).isEqualTo("Slow builds");
        assertThat(result.getVotes()).isEqualTo(5);
    }

    @Test
    void findItemsByRetroId_mapsEntitiesCorrectly() {
        RetroItemEntity entity1 = RetroItemEntity.builder()
                .id(TEST_UUID).retroId("retro-1").category("mad")
                .content("Issue A").votes(3).createdAt(NOW).build();

        RetroItemEntity entity2 = RetroItemEntity.builder()
                .id(TEST_UUID_2).retroId("retro-1").category("glad")
                .content("Good B").votes(7).createdAt(NOW).build();

        when(itemJpaRepository.findByRetroId("retro-1")).thenReturn(List.of(entity1, entity2));

        List<RetroItem> result = adapter.findItemsByRetroId("retro-1");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.get(0).getCategory()).isEqualTo("mad");
        assertThat(result.get(0).getContent()).isEqualTo("Issue A");
        assertThat(result.get(1).getId()).isEqualTo(TEST_UUID_2.toString());
        assertThat(result.get(1).getCategory()).isEqualTo("glad");
        assertThat(result.get(1).getVotes()).isEqualTo(7);
    }

    @Test
    void findItemById_whenFound_returnsMappedDomain() {
        RetroItemEntity entity = RetroItemEntity.builder()
                .id(TEST_UUID).retroId("retro-1").category("sad")
                .content("Missing docs").votes(2).createdAt(NOW).build();

        when(itemJpaRepository.findById(TEST_UUID)).thenReturn(Optional.of(entity));

        Optional<RetroItem> result = adapter.findItemById(TEST_UUID.toString());

        assertThat(result).isPresent();
        assertThat(result.get().getCategory()).isEqualTo("sad");
        assertThat(result.get().getContent()).isEqualTo("Missing docs");
    }

    // --- ActionItem Tests ---

    @Test
    void saveActionItem_mapsDomainToEntityAndBack() {
        ActionItem domain = ActionItem.builder()
                .id(TEST_UUID.toString())
                .retroId("retro-1")
                .description("Fix slow builds")
                .assignee("team")
                .dueDate("next-sprint")
                .status("pending")
                .createdAt(NOW)
                .build();

        ActionItemEntity savedEntity = ActionItemEntity.builder()
                .id(TEST_UUID)
                .retroId("retro-1")
                .description("Fix slow builds")
                .assignee("team")
                .dueDate("next-sprint")
                .status("pending")
                .createdAt(NOW)
                .build();

        when(actionItemJpaRepository.save(any(ActionItemEntity.class))).thenReturn(savedEntity);

        ActionItem result = adapter.saveActionItem(domain);

        ArgumentCaptor<ActionItemEntity> captor = ArgumentCaptor.forClass(ActionItemEntity.class);
        verify(actionItemJpaRepository).save(captor.capture());

        ActionItemEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getRetroId()).isEqualTo("retro-1");
        assertThat(captured.getDescription()).isEqualTo("Fix slow builds");
        assertThat(captured.getAssignee()).isEqualTo("team");
        assertThat(captured.getDueDate()).isEqualTo("next-sprint");
        assertThat(captured.getStatus()).isEqualTo("pending");
        assertThat(captured.getCreatedAt()).isEqualTo(NOW);

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getDescription()).isEqualTo("Fix slow builds");
        assertThat(result.getAssignee()).isEqualTo("team");
        assertThat(result.getStatus()).isEqualTo("pending");
    }

    @Test
    void findActionItemsByRetroId_mapsEntitiesCorrectly() {
        ActionItemEntity entity1 = ActionItemEntity.builder()
                .id(TEST_UUID).retroId("retro-1")
                .description("Action A").assignee("dev1")
                .dueDate("2026-03-01").status("pending")
                .createdAt(NOW).build();

        ActionItemEntity entity2 = ActionItemEntity.builder()
                .id(TEST_UUID_2).retroId("retro-1")
                .description("Action B").assignee("dev2")
                .dueDate("2026-03-15").status("in-progress")
                .createdAt(NOW).build();

        when(actionItemJpaRepository.findByRetroId("retro-1")).thenReturn(List.of(entity1, entity2));

        List<ActionItem> result = adapter.findActionItemsByRetroId("retro-1");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.get(0).getDescription()).isEqualTo("Action A");
        assertThat(result.get(0).getAssignee()).isEqualTo("dev1");
        assertThat(result.get(0).getStatus()).isEqualTo("pending");

        assertThat(result.get(1).getId()).isEqualTo(TEST_UUID_2.toString());
        assertThat(result.get(1).getDescription()).isEqualTo("Action B");
        assertThat(result.get(1).getAssignee()).isEqualTo("dev2");
        assertThat(result.get(1).getStatus()).isEqualTo("in-progress");
    }

    @Test
    void saveRetro_withNullId_doesNotSetIdOnEntity() {
        Retrospective domain = Retrospective.builder()
                .sprintId("sprint-1")
                .format("mad-sad-glad")
                .createdAt(NOW)
                .build();

        RetrospectiveEntity savedEntity = RetrospectiveEntity.builder()
                .id(TEST_UUID)
                .sprintId("sprint-1")
                .format("mad-sad-glad")
                .createdAt(NOW)
                .build();

        when(retroJpaRepository.save(any(RetrospectiveEntity.class))).thenReturn(savedEntity);

        adapter.saveRetro(domain);

        ArgumentCaptor<RetrospectiveEntity> captor = ArgumentCaptor.forClass(RetrospectiveEntity.class);
        verify(retroJpaRepository).save(captor.capture());

        RetrospectiveEntity captured = captor.getValue();
        assertThat(captured.getId()).isNull();
    }
}
