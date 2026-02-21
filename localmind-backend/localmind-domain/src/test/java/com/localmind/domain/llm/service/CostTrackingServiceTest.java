package com.localmind.domain.llm.service;

import com.localmind.domain.llm.model.LlmProvider;
import com.localmind.domain.llm.model.UsageRecord;
import com.localmind.domain.llm.port.out.LlmUsageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CostTrackingServiceTest {

    @Mock
    private LlmUsageRepository usageRepository;

    @InjectMocks
    private CostTrackingService costTrackingService;

    private final Instant from = Instant.parse("2025-01-01T00:00:00Z");
    private final Instant to = Instant.parse("2025-01-31T23:59:59Z");

    @Test
    void getTotalCost_shouldSumCosts() {
        List<UsageRecord> records = List.of(
                UsageRecord.builder().provider(LlmProvider.OLLAMA).cost(1.50).build(),
                UsageRecord.builder().provider(LlmProvider.OPENAI).cost(2.75).build(),
                UsageRecord.builder().provider(LlmProvider.ANTHROPIC).cost(3.25).build()
        );
        when(usageRepository.findByDateRange(from, to)).thenReturn(records);

        double totalCost = costTrackingService.getTotalCost(from, to);

        assertThat(totalCost).isEqualTo(7.50);
        verify(usageRepository).findByDateRange(from, to);
    }

    @Test
    void getTotalCost_withNoRecords_shouldReturnZero() {
        when(usageRepository.findByDateRange(from, to)).thenReturn(List.of());

        double totalCost = costTrackingService.getTotalCost(from, to);

        assertThat(totalCost).isZero();
    }

    @Test
    void getCostByProvider_shouldGroupByProvider() {
        List<UsageRecord> records = List.of(
                UsageRecord.builder().provider(LlmProvider.OPENAI).cost(1.00).build(),
                UsageRecord.builder().provider(LlmProvider.OPENAI).cost(2.00).build(),
                UsageRecord.builder().provider(LlmProvider.ANTHROPIC).cost(5.00).build()
        );
        when(usageRepository.findByDateRange(from, to)).thenReturn(records);

        Map<LlmProvider, Double> costByProvider = costTrackingService.getCostByProvider(from, to);

        assertThat(costByProvider).hasSize(2);
        assertThat(costByProvider.get(LlmProvider.OPENAI)).isEqualTo(3.00);
        assertThat(costByProvider.get(LlmProvider.ANTHROPIC)).isEqualTo(5.00);
        assertThat(costByProvider).doesNotContainKey(LlmProvider.OLLAMA);
    }

    @Test
    void getUsageHistory_shouldReturnRecords() {
        List<UsageRecord> records = List.of(
                UsageRecord.builder()
                        .id("rec-1")
                        .provider(LlmProvider.OLLAMA)
                        .model("llama3")
                        .cost(0.0)
                        .latencyMs(150)
                        .build(),
                UsageRecord.builder()
                        .id("rec-2")
                        .provider(LlmProvider.OPENAI)
                        .model("gpt-4")
                        .cost(0.03)
                        .latencyMs(800)
                        .build()
        );
        when(usageRepository.findByDateRange(from, to)).thenReturn(records);

        List<UsageRecord> result = costTrackingService.getUsageHistory(from, to);

        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(records);
        verify(usageRepository).findByDateRange(from, to);
    }
}
