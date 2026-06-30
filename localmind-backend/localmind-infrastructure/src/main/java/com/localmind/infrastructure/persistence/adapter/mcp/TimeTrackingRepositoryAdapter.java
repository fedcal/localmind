package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.TimeEntry;
import com.localmind.domain.mcp.model.TimeEstimate;
import com.localmind.domain.mcp.port.out.TimeTrackingRepository;
import com.localmind.infrastructure.persistence.entity.mcp.TimeEntryEntity;
import com.localmind.infrastructure.persistence.entity.mcp.TimeEstimateEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaTimeEntryRepository;
import com.localmind.infrastructure.persistence.repository.mcp.JpaTimeEstimateRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter bridging the domain TimeTrackingRepository port to JPA persistence.
 */
@Component
public class TimeTrackingRepositoryAdapter implements TimeTrackingRepository {

    private final JpaTimeEntryRepository timeEntryJpaRepository;
    private final JpaTimeEstimateRepository timeEstimateJpaRepository;

    public TimeTrackingRepositoryAdapter(JpaTimeEntryRepository timeEntryJpaRepository,
                                          JpaTimeEstimateRepository timeEstimateJpaRepository) {
        this.timeEntryJpaRepository = timeEntryJpaRepository;
        this.timeEstimateJpaRepository = timeEstimateJpaRepository;
    }

    @Override
    public TimeEntry saveEntry(TimeEntry entry) {
        TimeEntryEntity entity = toEntryEntity(entry);
        TimeEntryEntity saved = timeEntryJpaRepository.save(entity);
        return toEntryDomain(saved);
    }

    @Override
    public TimeEstimate saveEstimate(TimeEstimate estimate) {
        TimeEstimateEntity entity = toEstimateEntity(estimate);
        TimeEstimateEntity saved = timeEstimateJpaRepository.save(entity);
        return toEstimateDomain(saved);
    }

    @Override
    public Optional<TimeEntry> findActiveTimer() {
        return timeEntryJpaRepository.findFirstByTimerStartedAtIsNotNull()
                .map(this::toEntryDomain);
    }

    @Override
    public List<TimeEntry> findEntriesByUserId(String userId) {
        return timeEntryJpaRepository.findByUserId(userId)
                .stream()
                .map(this::toEntryDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeEntry> findEntriesByDateRange(String startDate, String endDate, String userId) {
        if (userId != null && !userId.isBlank()) {
            return timeEntryJpaRepository.findByEntryDateBetweenAndUserId(startDate, endDate, userId)
                    .stream()
                    .map(this::toEntryDomain)
                    .collect(Collectors.toList());
        }
        return timeEntryJpaRepository.findByEntryDateBetween(startDate, endDate)
                .stream()
                .map(this::toEntryDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TimeEstimate> findEstimateByTaskId(String taskId) {
        return timeEstimateJpaRepository.findByTaskId(taskId)
                .map(this::toEstimateDomain);
    }

    @Override
    public List<TimeEntry> findEntriesByTaskId(String taskId) {
        return timeEntryJpaRepository.findByTaskId(taskId)
                .stream()
                .map(this::toEntryDomain)
                .collect(Collectors.toList());
    }

    // --- TimeEntry mapping ---

    private TimeEntryEntity toEntryEntity(TimeEntry domain) {
        TimeEntryEntity entity = TimeEntryEntity.builder()
                .taskId(domain.getTaskId())
                .userId(domain.getUserId())
                .durationMinutes(domain.getDurationMinutes())
                .description(domain.getDescription())
                .entryDate(domain.getEntryDate())
                .timerStartedAt(domain.getTimerStartedAt())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private TimeEntry toEntryDomain(TimeEntryEntity entity) {
        return TimeEntry.builder()
                .id(entity.getId().toString())
                .taskId(entity.getTaskId())
                .userId(entity.getUserId())
                .durationMinutes(entity.getDurationMinutes())
                .description(entity.getDescription())
                .entryDate(entity.getEntryDate())
                .timerStartedAt(entity.getTimerStartedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    // --- TimeEstimate mapping ---

    private TimeEstimateEntity toEstimateEntity(TimeEstimate domain) {
        TimeEstimateEntity entity = TimeEstimateEntity.builder()
                .taskId(domain.getTaskId())
                .estimateMinutes(domain.getEstimateMinutes())
                .description(domain.getDescription())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private TimeEstimate toEstimateDomain(TimeEstimateEntity entity) {
        return TimeEstimate.builder()
                .id(entity.getId().toString())
                .taskId(entity.getTaskId())
                .estimateMinutes(entity.getEstimateMinutes())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
