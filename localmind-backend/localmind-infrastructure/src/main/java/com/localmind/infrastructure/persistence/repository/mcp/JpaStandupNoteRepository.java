package com.localmind.infrastructure.persistence.repository.mcp;

import com.localmind.infrastructure.persistence.entity.mcp.StandupNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaStandupNoteRepository extends JpaRepository<StandupNoteEntity, UUID> {

    List<StandupNoteEntity> findAllByOrderByStandupDateDesc();
}
