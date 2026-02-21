package com.localmind.domain.common.service;

import com.localmind.domain.common.model.AnalyticsOverview;
import com.localmind.domain.common.model.AnalyticsOverview.ModelUsageEntry;
import com.localmind.domain.common.model.AnalyticsOverview.UsageDataPoint;
import com.localmind.domain.document.model.Document;
import com.localmind.domain.document.model.DocumentStatus;
import com.localmind.domain.document.port.out.DocumentRepository;
import com.localmind.domain.llm.model.ConversationContext;
import com.localmind.domain.llm.model.LlmProvider;
import com.localmind.domain.llm.model.TokenUsage;
import com.localmind.domain.llm.model.UsageRecord;
import com.localmind.domain.llm.port.out.ConversationRepository;
import com.localmind.domain.llm.port.out.LlmUsageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private LlmUsageRepository usageRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private DocumentRepository documentRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private final Instant from = Instant.now().minus(30, ChronoUnit.DAYS);
    private final Instant to = Instant.now();

    @Test
    void getOverview_withData_computesCorrectTotals() {
        Instant day1 = Instant.parse("2026-02-10T10:00:00Z");
        Instant day2 = Instant.parse("2026-02-11T14:00:00Z");

        List<UsageRecord> records = List.of(
                UsageRecord.builder()
                        .id("r1")
                        .provider(LlmProvider.OLLAMA)
                        .model("llama3.2")
                        .tokenUsage(TokenUsage.builder().promptTokens(100).completionTokens(200).totalTokens(300).build())
                        .cost(0.0)
                        .latencyMs(150)
                        .timestamp(day1)
                        .build(),
                UsageRecord.builder()
                        .id("r2")
                        .provider(LlmProvider.OPENAI)
                        .model("gpt-4o")
                        .tokenUsage(TokenUsage.builder().promptTokens(500).completionTokens(700).totalTokens(1200).build())
                        .cost(0.05)
                        .latencyMs(350)
                        .timestamp(day2)
                        .build(),
                UsageRecord.builder()
                        .id("r3")
                        .provider(LlmProvider.OLLAMA)
                        .model("llama3.2")
                        .tokenUsage(TokenUsage.builder().promptTokens(50).completionTokens(150).totalTokens(200).build())
                        .cost(0.0)
                        .latencyMs(100)
                        .timestamp(day1)
                        .build()
        );

        List<ConversationContext> conversations = List.of(
                ConversationContext.builder().id("c1").build(),
                ConversationContext.builder().id("c2").build(),
                ConversationContext.builder().id("c3").build()
        );

        List<Document> documents = List.of(
                Document.builder().id("d1").status(DocumentStatus.INDEXED).build(),
                Document.builder().id("d2").status(DocumentStatus.INDEXED).build(),
                Document.builder().id("d3").status(DocumentStatus.PENDING).build(),
                Document.builder().id("d4").status(DocumentStatus.FAILED).build()
        );

        when(usageRepository.findByDateRange(any(), any())).thenReturn(records);
        when(conversationRepository.findAllOrderByUpdatedAtDesc()).thenReturn(conversations);
        when(documentRepository.findAll()).thenReturn(documents);

        AnalyticsOverview result = analyticsService.getOverview(from, to);

        assertThat(result.getTotalTokens()).isEqualTo(1700);
        assertThat(result.getTotalCost()).isEqualTo(0.05);
        assertThat(result.getAvgResponseTimeMs()).isEqualTo(200.0);
        assertThat(result.getTotalConversations()).isEqualTo(3);
        assertThat(result.getTotalDocuments()).isEqualTo(4);

        assertThat(result.getTokensByProvider()).containsEntry("OLLAMA", 500L);
        assertThat(result.getTokensByProvider()).containsEntry("OPENAI", 1200L);

        assertThat(result.getCostByProvider()).containsEntry("OLLAMA", 0.0);
        assertThat(result.getCostByProvider()).containsEntry("OPENAI", 0.05);

        assertThat(result.getDocumentsByStatus()).containsEntry("INDEXED", 2L);
        assertThat(result.getDocumentsByStatus()).containsEntry("PENDING", 1L);
        assertThat(result.getDocumentsByStatus()).containsEntry("FAILED", 1L);

        assertThat(result.getUsageTimeline()).hasSize(2);
        UsageDataPoint day1Point = result.getUsageTimeline().stream()
                .filter(p -> "2026-02-10".equals(p.getDate())).findFirst().orElse(null);
        assertThat(day1Point).isNotNull();
        assertThat(day1Point.getTokens()).isEqualTo(500);
        assertThat(day1Point.getCost()).isEqualTo(0.0);

        UsageDataPoint day2Point = result.getUsageTimeline().stream()
                .filter(p -> "2026-02-11".equals(p.getDate())).findFirst().orElse(null);
        assertThat(day2Point).isNotNull();
        assertThat(day2Point.getTokens()).isEqualTo(1200);
        assertThat(day2Point.getCost()).isEqualTo(0.05);
    }

    @Test
    void getOverview_emptyData_returnsZeros() {
        when(usageRepository.findByDateRange(any(), any())).thenReturn(Collections.emptyList());
        when(conversationRepository.findAllOrderByUpdatedAtDesc()).thenReturn(Collections.emptyList());
        when(documentRepository.findAll()).thenReturn(Collections.emptyList());

        AnalyticsOverview result = analyticsService.getOverview(from, to);

        assertThat(result.getTotalTokens()).isZero();
        assertThat(result.getTotalCost()).isZero();
        assertThat(result.getAvgResponseTimeMs()).isZero();
        assertThat(result.getTotalConversations()).isZero();
        assertThat(result.getTotalDocuments()).isZero();
        assertThat(result.getTokensByProvider()).isEmpty();
        assertThat(result.getCostByProvider()).isEmpty();
        assertThat(result.getDocumentsByStatus()).isEmpty();
        assertThat(result.getUsageTimeline()).isEmpty();
        assertThat(result.getTopModels()).isEmpty();
    }

    @Test
    void getOverview_topModels_sortedByTokensAndLimitedToFive() {
        List<UsageRecord> records = new ArrayList<>();
        String[] modelNames = {"model-a", "model-b", "model-c", "model-d", "model-e", "model-f", "model-g"};
        for (int i = 0; i < modelNames.length; i++) {
            records.add(UsageRecord.builder()
                    .id("r" + i)
                    .provider(LlmProvider.OLLAMA)
                    .model(modelNames[i])
                    .tokenUsage(TokenUsage.builder()
                            .promptTokens((i + 1) * 100)
                            .completionTokens((i + 1) * 100)
                            .totalTokens((i + 1) * 200)
                            .build())
                    .cost(0.0)
                    .latencyMs(100)
                    .timestamp(Instant.parse("2026-02-15T10:00:00Z"))
                    .build());
        }

        when(usageRepository.findByDateRange(any(), any())).thenReturn(records);
        when(conversationRepository.findAllOrderByUpdatedAtDesc()).thenReturn(Collections.emptyList());
        when(documentRepository.findAll()).thenReturn(Collections.emptyList());

        AnalyticsOverview result = analyticsService.getOverview(from, to);

        assertThat(result.getTopModels()).hasSize(5);

        ModelUsageEntry first = result.getTopModels().get(0);
        assertThat(first.getModel()).isEqualTo("model-g");
        assertThat(first.getTotalTokens()).isEqualTo(1400);
        assertThat(first.getRequestCount()).isEqualTo(1);

        ModelUsageEntry last = result.getTopModels().get(4);
        assertThat(last.getModel()).isEqualTo("model-c");
        assertThat(last.getTotalTokens()).isEqualTo(600);

        List<String> topModelNames = result.getTopModels().stream()
                .map(ModelUsageEntry::getModel).toList();
        assertThat(topModelNames).doesNotContain("model-a", "model-b");
    }

    @Test
    void getOverview_withNullTokenUsage_treatsAsZero() {
        List<UsageRecord> records = List.of(
                UsageRecord.builder()
                        .id("r1")
                        .provider(LlmProvider.OLLAMA)
                        .model("llama3.2")
                        .tokenUsage(null)
                        .cost(0.01)
                        .latencyMs(200)
                        .timestamp(Instant.parse("2026-02-12T10:00:00Z"))
                        .build()
        );

        when(usageRepository.findByDateRange(any(), any())).thenReturn(records);
        when(conversationRepository.findAllOrderByUpdatedAtDesc()).thenReturn(Collections.emptyList());
        when(documentRepository.findAll()).thenReturn(Collections.emptyList());

        AnalyticsOverview result = analyticsService.getOverview(from, to);

        assertThat(result.getTotalTokens()).isZero();
        assertThat(result.getTotalCost()).isEqualTo(0.01);
        assertThat(result.getTokensByProvider()).containsEntry("OLLAMA", 0L);
    }

    @Test
    void getOverview_multipleRecordsSameModel_aggregatesCorrectly() {
        List<UsageRecord> records = List.of(
                UsageRecord.builder()
                        .id("r1").provider(LlmProvider.OPENAI).model("gpt-4o")
                        .tokenUsage(TokenUsage.builder().totalTokens(500).build())
                        .cost(0.02).latencyMs(100)
                        .timestamp(Instant.parse("2026-02-13T10:00:00Z"))
                        .build(),
                UsageRecord.builder()
                        .id("r2").provider(LlmProvider.OPENAI).model("gpt-4o")
                        .tokenUsage(TokenUsage.builder().totalTokens(300).build())
                        .cost(0.01).latencyMs(200)
                        .timestamp(Instant.parse("2026-02-13T11:00:00Z"))
                        .build()
        );

        when(usageRepository.findByDateRange(any(), any())).thenReturn(records);
        when(conversationRepository.findAllOrderByUpdatedAtDesc()).thenReturn(Collections.emptyList());
        when(documentRepository.findAll()).thenReturn(Collections.emptyList());

        AnalyticsOverview result = analyticsService.getOverview(from, to);

        assertThat(result.getTopModels()).hasSize(1);
        ModelUsageEntry entry = result.getTopModels().get(0);
        assertThat(entry.getModel()).isEqualTo("gpt-4o");
        assertThat(entry.getProvider()).isEqualTo("OPENAI");
        assertThat(entry.getTotalTokens()).isEqualTo(800);
        assertThat(entry.getRequestCount()).isEqualTo(2);

        assertThat(result.getUsageTimeline()).hasSize(1);
        assertThat(result.getUsageTimeline().get(0).getDate()).isEqualTo("2026-02-13");
        assertThat(result.getUsageTimeline().get(0).getTokens()).isEqualTo(800);
    }
}
