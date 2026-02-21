package com.localmind.domain.common.service;

import com.localmind.domain.common.model.AnalyticsOverview;
import com.localmind.domain.common.model.AnalyticsOverview.ModelUsageEntry;
import com.localmind.domain.common.model.AnalyticsOverview.UsageDataPoint;
import com.localmind.domain.common.port.in.AnalyticsUseCase;
import com.localmind.domain.document.model.Document;
import com.localmind.domain.document.port.out.DocumentRepository;
import com.localmind.domain.llm.model.UsageRecord;
import com.localmind.domain.llm.port.out.ConversationRepository;
import com.localmind.domain.llm.port.out.LlmUsageRepository;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Pure domain service implementing analytics aggregation.
 * No Spring dependencies - wired via DomainConfig.
 */
public class AnalyticsService implements AnalyticsUseCase {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);
    private static final int TOP_MODELS_LIMIT = 5;

    private final LlmUsageRepository usageRepository;
    private final ConversationRepository conversationRepository;
    private final DocumentRepository documentRepository;

    public AnalyticsService(LlmUsageRepository usageRepository,
                            ConversationRepository conversationRepository,
                            DocumentRepository documentRepository) {
        this.usageRepository = usageRepository;
        this.conversationRepository = conversationRepository;
        this.documentRepository = documentRepository;
    }

    @Override
    public AnalyticsOverview getOverview(Instant from, Instant to) {
        List<UsageRecord> records = usageRepository.findByDateRange(from, to);

        long totalTokens = 0;
        double totalCost = 0.0;
        double totalLatencyMs = 0.0;

        Map<String, Long> tokensByProvider = new LinkedHashMap<>();
        Map<String, Double> costByProvider = new LinkedHashMap<>();
        Map<String, long[]> modelAggregation = new LinkedHashMap<>();
        Map<String, long[]> timelineTokens = new TreeMap<>();
        Map<String, double[]> timelineCost = new TreeMap<>();

        for (UsageRecord record : records) {
            int recordTokens = record.getTokenUsage() != null
                    ? record.getTokenUsage().getTotalTokens() : 0;
            totalTokens += recordTokens;
            totalCost += record.getCost();
            totalLatencyMs += record.getLatencyMs();

            String providerName = record.getProvider() != null
                    ? record.getProvider().name() : "UNKNOWN";
            tokensByProvider.merge(providerName, (long) recordTokens, Long::sum);
            costByProvider.merge(providerName, record.getCost(), Double::sum);

            String modelKey = record.getModel() + "|" + providerName;
            modelAggregation.computeIfAbsent(modelKey, k -> new long[2]);
            modelAggregation.get(modelKey)[0] += recordTokens;
            modelAggregation.get(modelKey)[1] += 1;

            String dateKey = record.getTimestamp() != null
                    ? DATE_FORMAT.format(record.getTimestamp()) : "unknown";
            timelineTokens.computeIfAbsent(dateKey, k -> new long[1]);
            timelineTokens.get(dateKey)[0] += recordTokens;
            timelineCost.computeIfAbsent(dateKey, k -> new double[1]);
            timelineCost.get(dateKey)[0] += record.getCost();
        }

        double avgResponseTimeMs = records.isEmpty() ? 0.0 : totalLatencyMs / records.size();

        List<ModelUsageEntry> topModels = modelAggregation.entrySet().stream()
                .map(entry -> {
                    String[] parts = entry.getKey().split("\\|", 2);
                    return ModelUsageEntry.builder()
                            .model(parts[0])
                            .provider(parts.length > 1 ? parts[1] : "UNKNOWN")
                            .totalTokens(entry.getValue()[0])
                            .requestCount(entry.getValue()[1])
                            .build();
                })
                .sorted(Comparator.comparingLong(ModelUsageEntry::getTotalTokens).reversed())
                .limit(TOP_MODELS_LIMIT)
                .collect(Collectors.toList());

        List<UsageDataPoint> usageTimeline = new ArrayList<>();
        Set<String> allDates = new TreeSet<>();
        allDates.addAll(timelineTokens.keySet());
        allDates.addAll(timelineCost.keySet());
        for (String date : allDates) {
            long tokens = timelineTokens.containsKey(date)
                    ? timelineTokens.get(date)[0] : 0;
            double cost = timelineCost.containsKey(date)
                    ? timelineCost.get(date)[0] : 0.0;
            usageTimeline.add(UsageDataPoint.builder()
                    .date(date)
                    .tokens(tokens)
                    .cost(cost)
                    .build());
        }

        long totalConversations = conversationRepository.findAllOrderByUpdatedAtDesc().size();

        List<Document> allDocuments = documentRepository.findAll();
        long totalDocuments = allDocuments.size();

        Map<String, Long> documentsByStatus = allDocuments.stream()
                .collect(Collectors.groupingBy(
                        doc -> doc.getStatus() != null ? doc.getStatus().name() : "UNKNOWN",
                        Collectors.counting()));

        return AnalyticsOverview.builder()
                .totalConversations(totalConversations)
                .totalDocuments(totalDocuments)
                .totalTokens(totalTokens)
                .totalCost(totalCost)
                .avgResponseTimeMs(avgResponseTimeMs)
                .tokensByProvider(tokensByProvider)
                .costByProvider(costByProvider)
                .documentsByStatus(documentsByStatus)
                .usageTimeline(usageTimeline)
                .topModels(topModels)
                .build();
    }
}
