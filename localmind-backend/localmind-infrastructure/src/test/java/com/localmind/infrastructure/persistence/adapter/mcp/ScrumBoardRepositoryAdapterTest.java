package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.ScrumTask;
import com.localmind.domain.mcp.model.Sprint;
import com.localmind.domain.mcp.model.UserStory;
import com.localmind.infrastructure.persistence.entity.mcp.ScrumTaskEntity;
import com.localmind.infrastructure.persistence.entity.mcp.SprintEntity;
import com.localmind.infrastructure.persistence.entity.mcp.UserStoryEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaScrumTaskRepository;
import com.localmind.infrastructure.persistence.repository.mcp.JpaSprintRepository;
import com.localmind.infrastructure.persistence.repository.mcp.JpaUserStoryRepository;
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
class ScrumBoardRepositoryAdapterTest {

    @Mock
    private JpaSprintRepository jpaSprintRepository;

    @Mock
    private JpaUserStoryRepository jpaUserStoryRepository;

    @Mock
    private JpaScrumTaskRepository jpaScrumTaskRepository;

    private ScrumBoardRepositoryAdapter sprintAdapter;
    private UserStoryRepositoryAdapter userStoryAdapter;
    private ScrumTaskRepositoryAdapter scrumTaskAdapter;

    private final UUID TEST_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final Instant NOW = Instant.parse("2026-02-12T10:00:00Z");

    @BeforeEach
    void setUp() {
        sprintAdapter = new ScrumBoardRepositoryAdapter(jpaSprintRepository);
        userStoryAdapter = new UserStoryRepositoryAdapter(jpaUserStoryRepository);
        scrumTaskAdapter = new ScrumTaskRepositoryAdapter(jpaScrumTaskRepository);
    }

    // ========== Sprint Tests ==========

    @Test
    void saveSprint_mapsDomainToEntityAndBack() {
        Sprint domain = Sprint.builder()
                .id(TEST_UUID.toString())
                .name("Sprint 1")
                .startDate("2026-02-12")
                .endDate("2026-02-26")
                .goalsJson("[\"Goal 1\"]")
                .createdAt(NOW)
                .build();

        SprintEntity savedEntity = SprintEntity.builder()
                .id(TEST_UUID)
                .name("Sprint 1")
                .startDate("2026-02-12")
                .endDate("2026-02-26")
                .goals("[\"Goal 1\"]")
                .createdAt(NOW)
                .build();

        when(jpaSprintRepository.save(any(SprintEntity.class))).thenReturn(savedEntity);

        Sprint result = sprintAdapter.save(domain);

        ArgumentCaptor<SprintEntity> captor = ArgumentCaptor.forClass(SprintEntity.class);
        verify(jpaSprintRepository).save(captor.capture());

        SprintEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getName()).isEqualTo("Sprint 1");
        assertThat(captured.getStartDate()).isEqualTo("2026-02-12");
        assertThat(captured.getEndDate()).isEqualTo("2026-02-26");
        assertThat(captured.getGoals()).isEqualTo("[\"Goal 1\"]");
        assertThat(captured.getCreatedAt()).isEqualTo(NOW);

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getName()).isEqualTo("Sprint 1");
        assertThat(result.getGoalsJson()).isEqualTo("[\"Goal 1\"]");
    }

    @Test
    void findSprintById_mapsToDomain() {
        SprintEntity entity = SprintEntity.builder()
                .id(TEST_UUID)
                .name("Sprint 1")
                .startDate("2026-02-12")
                .endDate("2026-02-26")
                .goals("[\"Goal 1\"]")
                .createdAt(NOW)
                .build();

        when(jpaSprintRepository.findById(TEST_UUID)).thenReturn(Optional.of(entity));

        Optional<Sprint> result = sprintAdapter.findById(TEST_UUID.toString());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.get().getName()).isEqualTo("Sprint 1");
    }

    @Test
    void findAllSprints_mapsToDomain() {
        SprintEntity entity = SprintEntity.builder()
                .id(TEST_UUID).name("Sprint 1").createdAt(NOW).build();

        when(jpaSprintRepository.findAll()).thenReturn(List.of(entity));

        List<Sprint> result = sprintAdapter.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Sprint 1");
    }

    @Test
    void saveSprint_withNullId_doesNotSetIdOnEntity() {
        Sprint domain = Sprint.builder()
                .name("Sprint 1")
                .createdAt(NOW)
                .build();

        SprintEntity savedEntity = SprintEntity.builder()
                .id(TEST_UUID).name("Sprint 1").createdAt(NOW).build();

        when(jpaSprintRepository.save(any(SprintEntity.class))).thenReturn(savedEntity);

        sprintAdapter.save(domain);

        ArgumentCaptor<SprintEntity> captor = ArgumentCaptor.forClass(SprintEntity.class);
        verify(jpaSprintRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isNull();
    }

    // ========== UserStory Tests ==========

    @Test
    void saveUserStory_mapsDomainToEntityAndBack() {
        UserStory domain = UserStory.builder()
                .id(TEST_UUID.toString())
                .title("Login feature")
                .description("Implement login")
                .acceptanceCriteriaJson("[\"AC 1\"]")
                .storyPoints(5)
                .priority("high")
                .sprintId("sprint-1")
                .createdAt(NOW)
                .build();

        UserStoryEntity savedEntity = UserStoryEntity.builder()
                .id(TEST_UUID)
                .title("Login feature")
                .description("Implement login")
                .acceptanceCriteria("[\"AC 1\"]")
                .storyPoints(5)
                .priority("high")
                .sprintId("sprint-1")
                .createdAt(NOW)
                .build();

        when(jpaUserStoryRepository.save(any(UserStoryEntity.class))).thenReturn(savedEntity);

        UserStory result = userStoryAdapter.save(domain);

        ArgumentCaptor<UserStoryEntity> captor = ArgumentCaptor.forClass(UserStoryEntity.class);
        verify(jpaUserStoryRepository).save(captor.capture());

        UserStoryEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getTitle()).isEqualTo("Login feature");
        assertThat(captured.getAcceptanceCriteria()).isEqualTo("[\"AC 1\"]");
        assertThat(captured.getStoryPoints()).isEqualTo(5);
        assertThat(captured.getPriority()).isEqualTo("high");
        assertThat(captured.getSprintId()).isEqualTo("sprint-1");

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getTitle()).isEqualTo("Login feature");
        assertThat(result.getAcceptanceCriteriaJson()).isEqualTo("[\"AC 1\"]");
    }

    @Test
    void findUserStoriesBySprintId_mapsToDomain() {
        UserStoryEntity entity = UserStoryEntity.builder()
                .id(TEST_UUID).title("Story 1").storyPoints(3).priority("medium")
                .sprintId("sprint-1").createdAt(NOW).build();

        when(jpaUserStoryRepository.findBySprintId("sprint-1")).thenReturn(List.of(entity));

        List<UserStory> result = userStoryAdapter.findBySprintId("sprint-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Story 1");
        assertThat(result.get(0).getSprintId()).isEqualTo("sprint-1");
    }

    @Test
    void findUnassignedUserStories_mapsToDomain() {
        UserStoryEntity entity = UserStoryEntity.builder()
                .id(TEST_UUID).title("Backlog Story").storyPoints(5).priority("low")
                .createdAt(NOW).build();

        when(jpaUserStoryRepository.findBySprintIdIsNull()).thenReturn(List.of(entity));

        List<UserStory> result = userStoryAdapter.findUnassigned();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Backlog Story");
        assertThat(result.get(0).getSprintId()).isNull();
    }

    // ========== ScrumTask Tests ==========

    @Test
    void saveScrumTask_mapsDomainToEntityAndBack() {
        ScrumTask domain = ScrumTask.builder()
                .id(TEST_UUID.toString())
                .title("Implement form")
                .description("Build the HTML form")
                .storyId("story-1")
                .assignee("John")
                .status("todo")
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();

        ScrumTaskEntity savedEntity = ScrumTaskEntity.builder()
                .id(TEST_UUID)
                .title("Implement form")
                .description("Build the HTML form")
                .storyId("story-1")
                .assignee("John")
                .status("todo")
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();

        when(jpaScrumTaskRepository.save(any(ScrumTaskEntity.class))).thenReturn(savedEntity);

        ScrumTask result = scrumTaskAdapter.save(domain);

        ArgumentCaptor<ScrumTaskEntity> captor = ArgumentCaptor.forClass(ScrumTaskEntity.class);
        verify(jpaScrumTaskRepository).save(captor.capture());

        ScrumTaskEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getTitle()).isEqualTo("Implement form");
        assertThat(captured.getStoryId()).isEqualTo("story-1");
        assertThat(captured.getAssignee()).isEqualTo("John");
        assertThat(captured.getStatus()).isEqualTo("todo");

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getTitle()).isEqualTo("Implement form");
        assertThat(result.getStatus()).isEqualTo("todo");
    }

    @Test
    void findTasksByStoryId_mapsToDomain() {
        ScrumTaskEntity entity = ScrumTaskEntity.builder()
                .id(TEST_UUID).title("Task 1").storyId("story-1")
                .status("in_progress").createdAt(NOW).updatedAt(NOW).build();

        when(jpaScrumTaskRepository.findByStoryId("story-1")).thenReturn(List.of(entity));

        List<ScrumTask> result = scrumTaskAdapter.findByStoryId("story-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Task 1");
        assertThat(result.get(0).getStatus()).isEqualTo("in_progress");
    }

    @Test
    void findTaskById_mapsToDomain() {
        ScrumTaskEntity entity = ScrumTaskEntity.builder()
                .id(TEST_UUID).title("Task 1").storyId("story-1")
                .assignee("John").status("done").createdAt(NOW).updatedAt(NOW).build();

        when(jpaScrumTaskRepository.findById(TEST_UUID)).thenReturn(Optional.of(entity));

        Optional<ScrumTask> result = scrumTaskAdapter.findById(TEST_UUID.toString());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.get().getTitle()).isEqualTo("Task 1");
        assertThat(result.get().getAssignee()).isEqualTo("John");
        assertThat(result.get().getStatus()).isEqualTo("done");
    }
}
