package com.localmind.infrastructure.persistence.repository;

import com.localmind.infrastructure.persistence.entity.ConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaConversationRepository extends JpaRepository<ConversationEntity, UUID> {
}
