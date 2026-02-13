package com.localmind.infrastructure.persistence.repository.mcp;

import com.localmind.infrastructure.persistence.entity.mcp.TimeEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for TimeEntryEntity.
 */
public interface JpaTimeEntryRepository extends JpaRepository<TimeEntryEntity, UUID> {

    /**
     * Finds the active timer (entry with non-null timerStartedAt).
     */
    Optional<TimeEntryEntity> findFirstByTimerStartedAtIsNotNull();

    /**
     * Finds all entries for a given user.
     */
    List<TimeEntryEntity> findByUserId(String userId);

    /**
     * Finds all entries for a given task.
     */
    List<TimeEntryEntity> findByTaskId(String taskId);

    /**
     * Finds entries within a date range.
     */
    List<TimeEntryEntity> findByEntryDateBetween(String startDate, String endDate);

    /**
     * Finds entries within a date range for a specific user.
     */
    List<TimeEntryEntity> findByEntryDateBetweenAndUserId(String startDate, String endDate, String userId);
}
