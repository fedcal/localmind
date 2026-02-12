package com.localmind.infrastructure.config;

import com.localmind.domain.document.port.out.DocumentRepository;
import com.localmind.domain.document.port.out.FolderConfigRepository;
import com.localmind.domain.document.port.out.VectorStorePort;
import com.localmind.domain.document.service.DocumentSearchService;
import com.localmind.domain.document.service.DocumentService;
import com.localmind.domain.llm.model.LlmProvider;
import com.localmind.domain.llm.port.out.ConversationRepository;
import com.localmind.domain.llm.port.out.LlmClient;
import com.localmind.domain.llm.port.out.LlmUsageRepository;
import com.localmind.domain.llm.port.out.OllamaModelPort;
import com.localmind.domain.llm.port.out.ProviderConfigRepository;
import com.localmind.domain.llm.service.ConversationService;
import com.localmind.domain.llm.service.LlmGatewayService;
import com.localmind.domain.llm.service.ModelManagementService;
import com.localmind.domain.llm.service.ProviderConfigService;
import com.localmind.domain.mcp.port.out.CodeReviewRepository;
import com.localmind.domain.mcp.port.out.DependencyAnalysisRepository;
import com.localmind.domain.mcp.port.out.HttpRequestRepository;
import com.localmind.domain.mcp.port.out.ScaffoldingRepository;
import com.localmind.domain.mcp.port.out.SnippetRepository;
import com.localmind.domain.mcp.port.out.PerformanceProfilerRepository;
import com.localmind.domain.mcp.port.out.TestGeneratorRepository;
import com.localmind.domain.mcp.service.CodeReviewService;
import com.localmind.domain.mcp.service.DependencyAnalysisService;
import com.localmind.domain.mcp.service.HttpClientService;
import com.localmind.domain.mcp.service.PerformanceProfilerService;
import com.localmind.domain.mcp.service.ProjectScaffoldingService;
import com.localmind.domain.mcp.service.RegexService;
import com.localmind.domain.mcp.service.SnippetService;
import com.localmind.domain.mcp.service.TestGeneratorService;
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
            ProviderConfigRepository providerConfigRepository,
            @Value("${localmind.llm.fallback.order:OLLAMA,OPENAI,ANTHROPIC,GOOGLE}") String fallbackOrderStr,
            @Value("${localmind.llm.default-provider:OLLAMA}") String defaultProviderStr) {

        List<LlmProvider> fallbackOrder = Arrays.stream(fallbackOrderStr.split(","))
                .map(String::trim)
                .map(LlmProvider::valueOf)
                .toList();

        LlmProvider defaultProvider = LlmProvider.valueOf(defaultProviderStr.trim());

        return new LlmGatewayService(clients, usageRepository, providerConfigRepository, fallbackOrder, defaultProvider);
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

    @Bean
    public ConversationService conversationService(ConversationRepository conversationRepository) {
        return new ConversationService(conversationRepository);
    }

    @Bean
    public RegexService regexService() {
        return new RegexService();
    }

    @Bean
    public HttpClientService httpClientService(HttpRequestRepository httpRequestRepository) {
        return new HttpClientService(httpRequestRepository);
    }

    @Bean
    public SnippetService snippetService(SnippetRepository snippetRepository) {
        return new SnippetService(snippetRepository);
    }

    @Bean
    public CodeReviewService codeReviewService(CodeReviewRepository codeReviewRepository) {
        return new CodeReviewService(codeReviewRepository);
    }

    @Bean
    public TestGeneratorService testGeneratorService(TestGeneratorRepository testGeneratorRepository) {
        return new TestGeneratorService(testGeneratorRepository);
    }

    @Bean
    public ProjectScaffoldingService projectScaffoldingService(ScaffoldingRepository scaffoldingRepository) {
        return new ProjectScaffoldingService(scaffoldingRepository);
    }

    @Bean
    public DependencyAnalysisService dependencyAnalysisService(DependencyAnalysisRepository dependencyAnalysisRepository) {
        return new DependencyAnalysisService(dependencyAnalysisRepository);
    }

    @Bean
    public PerformanceProfilerService performanceProfilerService(PerformanceProfilerRepository performanceProfilerRepository) {
        return new PerformanceProfilerService(performanceProfilerRepository);
    }
}
