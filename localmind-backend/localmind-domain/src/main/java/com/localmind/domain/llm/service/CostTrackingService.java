package com.localmind.domain.llm.service;

import com.localmind.domain.llm.model.LlmProvider;
import com.localmind.domain.llm.model.UsageRecord;
import com.localmind.domain.llm.port.out.LlmUsageRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CostTrackingService {

    private final LlmUsageRepository usageRepository;

    public CostTrackingService(LlmUsageRepository usageRepository) {
        this.usageRepository = usageRepository;
    }

    public double getTotalCost(Instant from, Instant to) {
        return usageRepository.findByDateRange(from, to).stream()
                .mapToDouble(UsageRecord::getCost)
                .sum();
    }

    public Map<LlmProvider, Double> getCostByProvider(Instant from, Instant to) {
        return usageRepository.findByDateRange(from, to).stream()
                .collect(Collectors.groupingBy(
                        UsageRecord::getProvider,
                        Collectors.summingDouble(UsageRecord::getCost)
                ));
    }

    public List<UsageRecord> getUsageHistory(Instant from, Instant to) {
        return usageRepository.findByDateRange(from, to);
    }
}
