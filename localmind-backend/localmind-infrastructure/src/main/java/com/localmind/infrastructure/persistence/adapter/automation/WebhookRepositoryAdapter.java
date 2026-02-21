package com.localmind.infrastructure.persistence.adapter.automation;

import com.localmind.domain.automation.model.AutomationEventType;
import com.localmind.domain.automation.model.Webhook;
import com.localmind.domain.automation.port.out.WebhookRepository;
import com.localmind.infrastructure.persistence.entity.automation.WebhookEntity;
import com.localmind.infrastructure.persistence.repository.automation.JpaWebhookRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class WebhookRepositoryAdapter implements WebhookRepository {

    private final JpaWebhookRepository jpaRepository;

    public WebhookRepositoryAdapter(JpaWebhookRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Webhook save(Webhook webhook) {
        WebhookEntity entity = toEntity(webhook);
        WebhookEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Webhook> findById(String id) {
        return jpaRepository.findById(UUID.fromString(id)).map(this::toDomain);
    }

    @Override
    public List<Webhook> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Webhook> findByEventType(AutomationEventType eventType) {
        return jpaRepository.findByEventType(eventType.name()).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(UUID.fromString(id));
    }

    private WebhookEntity toEntity(Webhook domain) {
        WebhookEntity.WebhookEntityBuilder builder = WebhookEntity.builder()
                .name(domain.getName())
                .url(domain.getUrl())
                .eventType(domain.getEventType().name())
                .active(domain.isActive());
        if (domain.getId() != null) {
            builder.id(UUID.fromString(domain.getId()));
        }
        return builder.build();
    }

    private Webhook toDomain(WebhookEntity entity) {
        return Webhook.builder()
                .id(entity.getId().toString())
                .name(entity.getName())
                .url(entity.getUrl())
                .eventType(AutomationEventType.valueOf(entity.getEventType()))
                .active(entity.isActive())
                .build();
    }
}
