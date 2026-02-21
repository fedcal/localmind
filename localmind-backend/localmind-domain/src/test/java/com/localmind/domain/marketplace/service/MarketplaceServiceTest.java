package com.localmind.domain.marketplace.service;

import com.localmind.domain.common.exception.LocalMindException;
import com.localmind.domain.common.exception.ResourceNotFoundException;
import com.localmind.domain.marketplace.model.AgentCategory;
import com.localmind.domain.marketplace.model.MarketplaceAgent;
import com.localmind.domain.marketplace.model.MarketplaceReview;
import com.localmind.domain.marketplace.port.out.MarketplaceAgentRepository;
import com.localmind.domain.marketplace.port.out.MarketplaceReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarketplaceServiceTest {

    @Mock
    private MarketplaceAgentRepository agentRepository;

    @Mock
    private MarketplaceReviewRepository reviewRepository;

    @InjectMocks
    private MarketplaceService marketplaceService;

    @Test
    void testListAgents_noFilters_returnsAll() {
        List<MarketplaceAgent> agents = List.of(
                MarketplaceAgent.builder().id("a1").name("Agent A").category(AgentCategory.CHAT).build(),
                MarketplaceAgent.builder().id("a2").name("Agent B").category(AgentCategory.AUTOMATION).build()
        );
        when(agentRepository.findAll()).thenReturn(agents);

        List<MarketplaceAgent> result = marketplaceService.listAgents(null, null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Agent A");
        assertThat(result.get(1).getName()).isEqualTo("Agent B");
        verify(agentRepository).findAll();
        verify(agentRepository, never()).findByCategory(any());
        verify(agentRepository, never()).searchByName(anyString());
    }

    @Test
    void testListAgents_withCategory_filtersCorrectly() {
        List<MarketplaceAgent> chatAgents = List.of(
                MarketplaceAgent.builder().id("a1").name("Chat Agent").category(AgentCategory.CHAT).build()
        );
        when(agentRepository.findByCategory(AgentCategory.CHAT)).thenReturn(chatAgents);

        List<MarketplaceAgent> result = marketplaceService.listAgents(AgentCategory.CHAT, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo(AgentCategory.CHAT);
        verify(agentRepository).findByCategory(AgentCategory.CHAT);
        verify(agentRepository, never()).findAll();
    }

    @Test
    void testListAgents_withSearch_searchesByName() {
        List<MarketplaceAgent> found = List.of(
                MarketplaceAgent.builder().id("a1").name("Code Helper").category(AgentCategory.CODE_ASSISTANT).build()
        );
        when(agentRepository.searchByName("Code")).thenReturn(found);

        List<MarketplaceAgent> result = marketplaceService.listAgents(null, "Code");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Code Helper");
        verify(agentRepository).searchByName("Code");
        verify(agentRepository, never()).findAll();
    }

    @Test
    void testGetAgent_found() {
        MarketplaceAgent agent = MarketplaceAgent.builder()
                .id("a1").name("Agent A").category(AgentCategory.CHAT).build();
        when(agentRepository.findById("a1")).thenReturn(Optional.of(agent));

        Optional<MarketplaceAgent> result = marketplaceService.getAgent("a1");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Agent A");
        verify(agentRepository).findById("a1");
    }

    @Test
    void testGetAgent_notFound() {
        when(agentRepository.findById("nonexistent")).thenReturn(Optional.empty());

        Optional<MarketplaceAgent> result = marketplaceService.getAgent("nonexistent");

        assertThat(result).isEmpty();
        verify(agentRepository).findById("nonexistent");
    }

    @Test
    void testPublishAgent_setsDefaults() {
        MarketplaceAgent agent = MarketplaceAgent.builder()
                .name("New Agent")
                .category(AgentCategory.CUSTOM)
                .author("Author")
                .version("1.0.0")
                .build();

        when(agentRepository.save(any(MarketplaceAgent.class))).thenAnswer(inv -> inv.getArgument(0));

        MarketplaceAgent result = marketplaceService.publishAgent(agent);

        assertThat(result.getId()).isNotNull().isNotBlank();
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getPublishedAt()).isNotNull();
        assertThat(result.getName()).isEqualTo("New Agent");

        ArgumentCaptor<MarketplaceAgent> captor = ArgumentCaptor.forClass(MarketplaceAgent.class);
        verify(agentRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isNotNull();
    }

    @Test
    void testPublishAgent_missingName_throwsException() {
        MarketplaceAgent agent = MarketplaceAgent.builder()
                .category(AgentCategory.CHAT)
                .build();

        assertThatThrownBy(() -> marketplaceService.publishAgent(agent))
                .isInstanceOf(LocalMindException.class)
                .hasMessageContaining("name is required");
    }

    @Test
    void testPublishAgent_missingCategory_throwsException() {
        MarketplaceAgent agent = MarketplaceAgent.builder()
                .name("Agent")
                .build();

        assertThatThrownBy(() -> marketplaceService.publishAgent(agent))
                .isInstanceOf(LocalMindException.class)
                .hasMessageContaining("category is required");
    }

    @Test
    void testInstallAgent_incrementsDownloadCount() {
        MarketplaceAgent agent = MarketplaceAgent.builder()
                .id("a1").name("Agent A").category(AgentCategory.CHAT).downloadCount(5).build();
        when(agentRepository.findById("a1")).thenReturn(Optional.of(agent));

        marketplaceService.installAgent("a1");

        verify(agentRepository).incrementDownloadCount("a1");
    }

    @Test
    void testInstallAgent_notFound_throwsException() {
        when(agentRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> marketplaceService.installAgent("missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("missing");

        verify(agentRepository, never()).incrementDownloadCount(anyString());
    }

    @Test
    void testAddReview_validRating() {
        String agentId = "a1";
        MarketplaceAgent agent = MarketplaceAgent.builder()
                .id(agentId).name("Agent A").category(AgentCategory.CHAT).avgRating(0.0).build();
        when(agentRepository.findById(agentId)).thenReturn(Optional.of(agent));
        when(reviewRepository.save(any(MarketplaceReview.class))).thenAnswer(inv -> inv.getArgument(0));
        when(reviewRepository.calculateAverageRating(agentId)).thenReturn(4.5);
        when(agentRepository.save(any(MarketplaceAgent.class))).thenAnswer(inv -> inv.getArgument(0));

        MarketplaceReview result = marketplaceService.addReview(agentId, 5, "Great agent!");

        assertThat(result).isNotNull();
        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getComment()).isEqualTo("Great agent!");
        assertThat(result.getAgentId()).isEqualTo(agentId);
        assertThat(result.getId()).isNotNull();
        assertThat(result.getCreatedAt()).isNotNull();

        verify(reviewRepository).save(any(MarketplaceReview.class));
        verify(reviewRepository).calculateAverageRating(agentId);
        // Agent avgRating updated
        ArgumentCaptor<MarketplaceAgent> agentCaptor = ArgumentCaptor.forClass(MarketplaceAgent.class);
        verify(agentRepository).save(agentCaptor.capture());
        assertThat(agentCaptor.getValue().getAvgRating()).isEqualTo(4.5);
    }

    @Test
    void testAddReview_invalidRating_throwsException() {
        assertThatThrownBy(() -> marketplaceService.addReview("a1", 0, "Bad"))
                .isInstanceOf(LocalMindException.class)
                .hasMessageContaining("Rating must be between 1 and 5");

        assertThatThrownBy(() -> marketplaceService.addReview("a1", 6, "Too high"))
                .isInstanceOf(LocalMindException.class)
                .hasMessageContaining("Rating must be between 1 and 5");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void testAddReview_agentNotFound_throwsException() {
        when(agentRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> marketplaceService.addReview("missing", 3, "Comment"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("missing");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void testGetReviews_delegatesToRepo() {
        String agentId = "a1";
        List<MarketplaceReview> reviews = List.of(
                MarketplaceReview.builder().id("r1").agentId(agentId).rating(4).comment("Good").build(),
                MarketplaceReview.builder().id("r2").agentId(agentId).rating(5).comment("Excellent").build()
        );
        when(reviewRepository.findByAgentId(agentId)).thenReturn(reviews);

        List<MarketplaceReview> result = marketplaceService.getReviews(agentId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRating()).isEqualTo(4);
        assertThat(result.get(1).getRating()).isEqualTo(5);
        verify(reviewRepository).findByAgentId(agentId);
    }

    @Test
    void testDeleteAgent_delegatesToRepo() {
        MarketplaceAgent agent = MarketplaceAgent.builder()
                .id("a1").name("Agent A").category(AgentCategory.CHAT).build();
        when(agentRepository.findById("a1")).thenReturn(Optional.of(agent));

        marketplaceService.deleteAgent("a1");

        verify(agentRepository).deleteById("a1");
    }

    @Test
    void testDeleteAgent_notFound_throwsException() {
        when(agentRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> marketplaceService.deleteAgent("missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("missing");

        verify(agentRepository, never()).deleteById(anyString());
    }
}
