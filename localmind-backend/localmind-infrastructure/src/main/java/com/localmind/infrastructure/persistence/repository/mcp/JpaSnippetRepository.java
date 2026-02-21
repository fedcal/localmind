package com.localmind.infrastructure.persistence.repository.mcp;

import com.localmind.infrastructure.persistence.entity.mcp.SnippetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JpaSnippetRepository extends JpaRepository<SnippetEntity, UUID> {

    @Query("SELECT s FROM SnippetEntity s WHERE LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.code) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<SnippetEntity> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT s FROM SnippetEntity s JOIN s.tags t WHERE t.tag = :tag")
    List<SnippetEntity> findByTag(@Param("tag") String tag);

    List<SnippetEntity> findByLanguage(String language);
}
