package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.HttpRequestResult;
import com.localmind.domain.mcp.port.out.HttpRequestRepository;
import com.localmind.infrastructure.persistence.entity.mcp.HttpRequestEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaHttpRequestRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter bridging the domain HttpRequestRepository port to JPA persistence.
 */
@Component
public class HttpRequestRepositoryAdapter implements HttpRequestRepository {

    private final JpaHttpRequestRepository jpaRepository;

    public HttpRequestRepositoryAdapter(JpaHttpRequestRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public HttpRequestResult save(HttpRequestResult result) {
        HttpRequestEntity entity = toEntity(result);
        HttpRequestEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<HttpRequestResult> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private HttpRequestEntity toEntity(HttpRequestResult domain) {
        HttpRequestEntity entity = HttpRequestEntity.builder()
                .url(domain.getUrl())
                .method(domain.getMethod())
                .statusCode(domain.getStatusCode())
                .responseTimeMs(domain.getResponseTimeMs())
                .responseBody(domain.getResponseBody())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private HttpRequestResult toDomain(HttpRequestEntity entity) {
        return HttpRequestResult.builder()
                .id(entity.getId().toString())
                .url(entity.getUrl())
                .method(entity.getMethod())
                .statusCode(entity.getStatusCode())
                .responseTimeMs(entity.getResponseTimeMs())
                .responseBody(entity.getResponseBody())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
