package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.DecisionLink;
import com.localmind.domain.mcp.model.DecisionRecord;
import com.localmind.infrastructure.persistence.entity.mcp.DecisionLinkEntity;
import com.localmind.infrastructure.persistence.entity.mcp.DecisionRecordEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaDecisionLinkRepository;
import com.localmind.infrastructure.persistence.repository.mcp.JpaDecisionRecordRepository;
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
class DecisionLogRepositoryAdapterTest {

    @Mock
    private JpaDecisionRecordRepository recordJpaRepository;

    @Mock
    private JpaDecisionLinkRepository linkJpaRepository;

    private DecisionLogRepositoryAdapter adapter;

    private final UUID TEST_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final UUID TEST_UUID_2 = UUID.fromString("b1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final Instant NOW = Instant.parse("2026-02-13T10:00:00Z");

    @BeforeEach
    void setUp() {
        adapter = new DecisionLogRepositoryAdapter(recordJpaRepository, linkJpaRepository);
    }

    // --- DecisionRecord Tests ---

    @Test
    void save_mapsDomainToEntityAndBack() {
        DecisionRecord domain = DecisionRecord.builder()
                .id(TEST_UUID.toString())
                .title("Use PostgreSQL")
                .context("Need relational DB")
                .decision("Chose PostgreSQL")
                .alternativesJson("[\"MySQL\"]")
                .consequences("Migration needed")
                .status("accepted")
                .relatedTicketsJson(null)
                .createdAt(NOW)
                .build();

        DecisionRecordEntity savedEntity = DecisionRecordEntity.builder()
                .id(TEST_UUID)
                .title("Use PostgreSQL")
                .context("Need relational DB")
                .decisionText("Chose PostgreSQL")
                .alternativesJson("[\"MySQL\"]")
                .consequences("Migration needed")
                .status("accepted")
                .relatedTicketsJson(null)
                .createdAt(NOW)
                .build();

        when(recordJpaRepository.save(any(DecisionRecordEntity.class))).thenReturn(savedEntity);

        DecisionRecord result = adapter.save(domain);

        ArgumentCaptor<DecisionRecordEntity> captor = ArgumentCaptor.forClass(DecisionRecordEntity.class);
        verify(recordJpaRepository).save(captor.capture());

        DecisionRecordEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getTitle()).isEqualTo("Use PostgreSQL");
        assertThat(captured.getContext()).isEqualTo("Need relational DB");
        assertThat(captured.getDecisionText()).isEqualTo("Chose PostgreSQL");
        assertThat(captured.getAlternativesJson()).isEqualTo("[\"MySQL\"]");
        assertThat(captured.getConsequences()).isEqualTo("Migration needed");
        assertThat(captured.getStatus()).isEqualTo("accepted");
        assertThat(captured.getCreatedAt()).isEqualTo(NOW);

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getTitle()).isEqualTo("Use PostgreSQL");
        assertThat(result.getDecision()).isEqualTo("Chose PostgreSQL");
        assertThat(result.getStatus()).isEqualTo("accepted");
    }

    @Test
    void findById_whenFound_returnsMappedDomain() {
        DecisionRecordEntity entity = DecisionRecordEntity.builder()
                .id(TEST_UUID)
                .title("Use Kafka")
                .context("Event streaming needed")
                .decisionText("Kafka chosen")
                .status("proposed")
                .createdAt(NOW)
                .build();

        when(recordJpaRepository.findById(TEST_UUID)).thenReturn(Optional.of(entity));

        Optional<DecisionRecord> result = adapter.findById(TEST_UUID.toString());

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Use Kafka");
        assertThat(result.get().getDecision()).isEqualTo("Kafka chosen");
        assertThat(result.get().getStatus()).isEqualTo("proposed");
    }

    @Test
    void findById_whenNotFound_returnsEmpty() {
        when(recordJpaRepository.findById(TEST_UUID)).thenReturn(Optional.empty());

        Optional<DecisionRecord> result = adapter.findById(TEST_UUID.toString());

        assertThat(result).isEmpty();
    }

    // --- DecisionLink Tests ---

    @Test
    void saveLink_mapsDomainToEntityAndBack() {
        DecisionLink domain = DecisionLink.builder()
                .id(TEST_UUID.toString())
                .decisionId("decision-1")
                .linkType("ticket")
                .targetId("JIRA-123")
                .description("Related ticket")
                .createdAt(NOW)
                .build();

        DecisionLinkEntity savedEntity = DecisionLinkEntity.builder()
                .id(TEST_UUID)
                .decisionId("decision-1")
                .linkType("ticket")
                .targetId("JIRA-123")
                .description("Related ticket")
                .createdAt(NOW)
                .build();

        when(linkJpaRepository.save(any(DecisionLinkEntity.class))).thenReturn(savedEntity);

        DecisionLink result = adapter.saveLink(domain);

        ArgumentCaptor<DecisionLinkEntity> captor = ArgumentCaptor.forClass(DecisionLinkEntity.class);
        verify(linkJpaRepository).save(captor.capture());

        DecisionLinkEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getDecisionId()).isEqualTo("decision-1");
        assertThat(captured.getLinkType()).isEqualTo("ticket");
        assertThat(captured.getTargetId()).isEqualTo("JIRA-123");
        assertThat(captured.getDescription()).isEqualTo("Related ticket");

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getLinkType()).isEqualTo("ticket");
        assertThat(result.getTargetId()).isEqualTo("JIRA-123");
    }

    @Test
    void findLinksByDecisionId_mapsEntitiesCorrectly() {
        DecisionLinkEntity entity1 = DecisionLinkEntity.builder()
                .id(TEST_UUID).decisionId("dec-1").linkType("ticket")
                .targetId("JIRA-100").description("Bug").createdAt(NOW).build();
        DecisionLinkEntity entity2 = DecisionLinkEntity.builder()
                .id(TEST_UUID_2).decisionId("dec-1").linkType("commit")
                .targetId("abc123").description(null).createdAt(NOW).build();

        when(linkJpaRepository.findByDecisionId("dec-1")).thenReturn(List.of(entity1, entity2));

        List<DecisionLink> result = adapter.findLinksByDecisionId("dec-1");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getLinkType()).isEqualTo("ticket");
        assertThat(result.get(0).getTargetId()).isEqualTo("JIRA-100");
        assertThat(result.get(1).getLinkType()).isEqualTo("commit");
        assertThat(result.get(1).getTargetId()).isEqualTo("abc123");
    }
}
