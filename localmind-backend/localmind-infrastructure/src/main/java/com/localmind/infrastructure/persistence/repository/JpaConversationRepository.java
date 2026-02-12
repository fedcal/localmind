package com.localmind.infrastructure.persistence.repository;

import com.localmind.infrastructure.persistence.entity.ConversationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaConversationRepository extends JpaRepository<ConversationEntity, UUID> {

    List<ConversationEntity> findAllByOrderByUpdatedAtDesc();

    Page<ConversationEntity> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    @Query("SELECT DISTINCT c FROM ConversationEntity c LEFT JOIN c.messages m WHERE c.title LIKE %:query% OR m.content LIKE %:query% ORDER BY c.updatedAt DESC")
    Page<ConversationEntity> searchByContent(@Param("query") String query, Pageable pageable);

    @Query("SELECT c FROM ConversationEntity c JOIN c.tags t WHERE t.tag = :tag ORDER BY c.updatedAt DESC")
    List<ConversationEntity> findByTag(@Param("tag") String tag);
}
