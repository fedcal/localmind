package com.localmind.infrastructure.backup.adapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.domain.common.exception.LocalMindException;
import com.localmind.domain.common.port.out.ConfigExportPort;
import com.localmind.infrastructure.persistence.entity.DocumentEntity;
import com.localmind.infrastructure.persistence.entity.LlmProviderConfigEntity;
import com.localmind.infrastructure.persistence.repository.JpaDocumentRepository;
import com.localmind.infrastructure.persistence.repository.JpaProviderConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ConfigExportAdapter implements ConfigExportPort {

    private static final Logger log = LoggerFactory.getLogger(ConfigExportAdapter.class);

    private final ObjectMapper objectMapper;
    private final JpaProviderConfigRepository providerConfigRepository;
    private final JpaDocumentRepository documentRepository;

    public ConfigExportAdapter(ObjectMapper objectMapper,
                               JpaProviderConfigRepository providerConfigRepository,
                               JpaDocumentRepository documentRepository) {
        this.objectMapper = objectMapper;
        this.providerConfigRepository = providerConfigRepository;
        this.documentRepository = documentRepository;
    }

    @Override
    public byte[] exportProviderConfigs() {
        try {
            List<LlmProviderConfigEntity> configs = providerConfigRepository.findAll();
            List<Map<String, Object>> exportData = configs.stream()
                    .map(this::configToMap)
                    .toList();
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(exportData);
        } catch (Exception e) {
            throw new LocalMindException("Failed to export provider configs: " + e.getMessage(), e);
        }
    }

    @Override
    public void importProviderConfigs(byte[] data) {
        try {
            List<Map<String, Object>> configs = objectMapper.readValue(
                    data, new TypeReference<List<Map<String, Object>>>() {});

            for (Map<String, Object> configMap : configs) {
                String name = (String) configMap.get("name");
                String type = (String) configMap.get("type");

                List<LlmProviderConfigEntity> existing = providerConfigRepository.findByType(type);
                boolean alreadyExists = existing.stream()
                        .anyMatch(e -> e.getName().equals(name));

                if (!alreadyExists) {
                    LlmProviderConfigEntity entity = LlmProviderConfigEntity.builder()
                            .name(name)
                            .type(type)
                            .baseUrl((String) configMap.get("baseUrl"))
                            .apiKey((String) configMap.get("apiKey"))
                            .defaultModel((String) configMap.get("defaultModel"))
                            .enabled(configMap.get("enabled") != null && (Boolean) configMap.get("enabled"))
                            .build();
                    providerConfigRepository.save(entity);
                    log.info("Imported provider config: {} ({})", name, type);
                } else {
                    log.info("Skipped duplicate provider config: {} ({})", name, type);
                }
            }
        } catch (Exception e) {
            throw new LocalMindException("Failed to import provider configs: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] exportDocumentMetadata() {
        try {
            List<DocumentEntity> documents = documentRepository.findAll();
            List<Map<String, Object>> exportData = documents.stream()
                    .map(this::documentToMap)
                    .toList();
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(exportData);
        } catch (Exception e) {
            throw new LocalMindException("Failed to export document metadata: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> configToMap(LlmProviderConfigEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", entity.getName());
        map.put("type", entity.getType());
        map.put("baseUrl", entity.getBaseUrl());
        map.put("apiKey", entity.getApiKey());
        map.put("defaultModel", entity.getDefaultModel());
        map.put("enabled", entity.isEnabled());
        map.put("createdAt", entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null);
        return map;
    }

    private Map<String, Object> documentToMap(DocumentEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("filename", entity.getFilename());
        map.put("filePath", entity.getFilePath());
        map.put("fileHash", entity.getFileHash());
        map.put("status", entity.getStatus());
        map.put("createdAt", entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null);
        return map;
    }
}
