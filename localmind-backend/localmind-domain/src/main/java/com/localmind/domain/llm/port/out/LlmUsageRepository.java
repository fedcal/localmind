package com.localmind.domain.llm.port.out;

import com.localmind.domain.llm.model.UsageRecord;

import java.time.Instant;
import java.util.List;

public interface LlmUsageRepository {
    void save(UsageRecord record);
    List<UsageRecord> findByDateRange(Instant from, Instant to);
    List<UsageRecord> findAll();
}
