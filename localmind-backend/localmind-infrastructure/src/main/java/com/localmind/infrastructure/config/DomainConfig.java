package com.localmind.infrastructure.config;

import com.localmind.domain.document.port.out.DocumentRepository;
import com.localmind.domain.document.port.out.FolderConfigRepository;
import com.localmind.domain.document.port.out.VectorStorePort;
import com.localmind.domain.document.service.DocumentSearchService;
import com.localmind.domain.document.service.DocumentService;
import com.localmind.domain.llm.model.LlmProvider;
import com.localmind.domain.llm.port.out.LlmClient;
import com.localmind.domain.llm.port.out.LlmUsageRepository;
import com.localmind.domain.llm.port.out.OllamaModelPort;
import com.localmind.domain.llm.port.out.ProviderConfigRepository;
import com.localmind.domain.llm.service.LlmGatewayService;
import com.localmind.domain.llm.service.ModelManagementService;
import com.localmind.domain.llm.service.ProviderConfigService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class DomainConfig {

    @Bean
    public DocumentService documentService(DocumentRepository documentRepository,
                                           FolderConfigRepository folderConfigRepository) {
        return new DocumentService(documentRepository, folderConfigRepository);
    }

    @Bean
    public DocumentSearchService documentSearchService(VectorStorePort vectorStorePort) {
        return new DocumentSearchService(vectorStorePort);
    }

    @Bean
    public LlmGatewayService llmGatewayService(
            List<LlmClient> clients,
            LlmUsageRepository usageRepository,
            @Value("${localmind.llm.fallback.order:OLLAMA,OPENAI,ANTHROPIC,GOOGLE}") String fallbackOrderStr,
            @Value("${localmind.llm.default-provider:OLLAMA}") String defaultProviderStr) {

        List<LlmProvider> fallbackOrder = Arrays.stream(fallbackOrderStr.split(","))
                .map(String::trim)
                .map(LlmProvider::valueOf)
                .toList();

        LlmProvider defaultProvider = LlmProvider.valueOf(defaultProviderStr.trim());

        return new LlmGatewayService(clients, usageRepository, fallbackOrder, defaultProvider);
    }

    @Bean
    public ModelManagementService modelManagementService(List<LlmClient> clients) {
        return new ModelManagementService(clients);
    }

    @Bean
    public ProviderConfigService providerConfigService(ProviderConfigRepository providerConfigRepository,
                                                        OllamaModelPort ollamaModelPort) {
        return new ProviderConfigService(providerConfigRepository, ollamaModelPort);
    }
}
