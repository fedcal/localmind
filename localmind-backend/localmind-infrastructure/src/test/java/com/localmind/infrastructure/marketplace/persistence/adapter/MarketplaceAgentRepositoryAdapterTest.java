package com.localmind.infrastructure.marketplace.persistence.adapter;

import com.localmind.domain.marketplace.model.AgentCategory;
import com.localmind.domain.marketplace.model.MarketplaceAgent;
import com.localmind.infrastructure.marketplace.persistence.entity.MarketplaceAgentEntity;
import com.localmind.infrastructure.marketplace.persistence.repository.MarketplaceAgentJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
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
class MarketplaceAgentRepositoryAdapterTest {

    @Mock
    private MarketplaceAgentJpaRepository jpaRepository;

    @InjectMocks
    private MarketplaceAgentRepositoryAdapter adapter;

    private final UUID TEST_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final Instant NOW = Instant.parse("2026-01-15T10:30:00Z");

    @Test
    void testSave_mapsFieldsCorrectly() {
        MarketplaceAgent agent = MarketplaceAgent.builder()
                .id(TEST_UUID.toString())
                .name("Test Agent")
                .description("Test Description")
                .author("Author")
                .version("1.0.0")
                .category(AgentCategory.CHAT)
                .downloadUrl("https://example.com/agent.jar")
                .downloadCount(10)
                .avgRating(4.5)
                .publishedAt(NOW)
                .createdAt(NOW)
                .build();

        MarketplaceAgentEntity savedEntity = MarketplaceAgentEntity.builder()
                .id(TEST_UUID)
                .name("Test Agent")
                .description("Test Description")
                .author("Author")
                .version("1.0.0")
                .category("CHAT")
                .downloadUrl("https://example.com/agent.jar")
                .downloadCount(10)
                .avgRating(4.5)
                .publishedAt(NOW)
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(MarketplaceAgentEntity.class))).thenReturn(savedEntity);

        MarketplaceAgent result = adapter.save(agent);

        ArgumentCaptor<MarketplaceAgentEntity> captor = ArgumentCaptor.forClass(MarketplaceAgentEntity.class);
        verify(jpaRepository).save(captor.capture());

        MarketplaceAgentEntity capturedEntity = captor.getValue();
        assertThat(capturedEntity.getId()).isEqualTo(TEST_UUID);
        assertThat(capturedEntity.getName()).isEqualTo("Test Agent");
        assertThat(capturedEntity.getDescription()).isEqualTo("Test Description");
        assertThat(capturedEntity.getAuthor()).isEqualTo("Author");
        assertThat(capturedEntity.getVersion()).isEqualTo("1.0.0");
        assertThat(capturedEntity.getCategory()).isEqualTo("CHAT");
        assertThat(capturedEntity.getDownloadUrl()).isEqualTo("https://example.com/agent.jar");
        assertThat(capturedEntity.getDownloadCount()).isEqualTo(10);
        assertThat(capturedEntity.getAvgRating()).isEqualTo(4.5);
        assertThat(capturedEntity.getPublishedAt()).isEqualTo(NOW);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getName()).isEqualTo("Test Agent");
        assertThat(result.getCategory()).isEqualTo(AgentCategory.CHAT);
        assertThat(result.getAvgRating()).isEqualTo(4.5);
    }

    @Test
    void testFindById_found() {
        MarketplaceAgentEntity entity = MarketplaceAgentEntity.builder()
                .id(TEST_UUID)
                .name("Agent A")
                .description("Desc")
                .author("Author")
                .version("2.0.0")
                .category("AUTOMATION")
                .downloadCount(50)
                .avgRating(3.8)
                .createdAt(NOW)
                .build();

        when(jpaRepository.findById(TEST_UUID)).thenReturn(Optional.of(entity));

        Optional<MarketplaceAgent> result = adapter.findById(TEST_UUID.toString());

        assertThat(result).isPresent();
        MarketplaceAgent agent = result.get();
        assertThat(agent.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(agent.getName()).isEqualTo("Agent A");
        assertThat(agent.getCategory()).isEqualTo(AgentCategory.AUTOMATION);
        assertThat(agent.getDownloadCount()).isEqualTo(50);
    }

    @Test
    void testFindById_notFound() {
        when(jpaRepository.findById(TEST_UUID)).thenReturn(Optional.empty());

        Optional<MarketplaceAgent> result = adapter.findById(TEST_UUID.toString());

        assertThat(result).isEmpty();
    }

    @Test
    void testFindByCategory_delegatesToJpa() {
        MarketplaceAgentEntity entity = MarketplaceAgentEntity.builder()
                .id(TEST_UUID)
                .name("Code Agent")
                .category("CODE_ASSISTANT")
                .createdAt(NOW)
                .build();

        when(jpaRepository.findByCategory("CODE_ASSISTANT")).thenReturn(List.of(entity));

        List<MarketplaceAgent> result = adapter.findByCategory(AgentCategory.CODE_ASSISTANT);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo(AgentCategory.CODE_ASSISTANT);
        verify(jpaRepository).findByCategory("CODE_ASSISTANT");
    }

    @Test
    void testSearchByName_delegatesToJpa() {
        MarketplaceAgentEntity entity = MarketplaceAgentEntity.builder()
                .id(TEST_UUID)
                .name("Code Helper")
                .category("CODE_ASSISTANT")
                .createdAt(NOW)
                .build();

        when(jpaRepository.findByNameContainingIgnoreCase("Code")).thenReturn(List.of(entity));

        List<MarketplaceAgent> result = adapter.searchByName("Code");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Code Helper");
        verify(jpaRepository).findByNameContainingIgnoreCase("Code");
    }

    @Test
    void testIncrementDownloadCount() {
        adapter.incrementDownloadCount(TEST_UUID.toString());

        verify(jpaRepository).incrementDownloadCount(TEST_UUID);
    }

    @Test
    void testDeleteById_delegatesToJpa() {
        adapter.deleteById(TEST_UUID.toString());

        verify(jpaRepository).deleteById(TEST_UUID);
    }

    @Test
    void testFindAll_returnsMappedAgents() {
        MarketplaceAgentEntity entity1 = MarketplaceAgentEntity.builder()
                .id(TEST_UUID)
                .name("Agent 1")
                .category("CHAT")
                .createdAt(NOW)
                .build();

        UUID secondUuid = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
        MarketplaceAgentEntity entity2 = MarketplaceAgentEntity.builder()
                .id(secondUuid)
                .name("Agent 2")
                .category("INTEGRATION")
                .createdAt(NOW)
                .build();

        when(jpaRepository.findAll()).thenReturn(List.of(entity1, entity2));

        List<MarketplaceAgent> result = adapter.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Agent 1");
        assertThat(result.get(0).getCategory()).isEqualTo(AgentCategory.CHAT);
        assertThat(result.get(1).getName()).isEqualTo("Agent 2");
        assertThat(result.get(1).getCategory()).isEqualTo(AgentCategory.INTEGRATION);
    }
}
