package com.localmind.infrastructure.config;

import com.localmind.domain.auth.port.out.LocalAuthRepository;
import com.localmind.domain.auth.port.out.PasswordHasherPort;
import com.localmind.domain.auth.service.LocalAuthService;
import com.localmind.domain.automation.port.out.WebhookRepository;
import com.localmind.domain.automation.port.out.WebhookClientPort;
import com.localmind.domain.automation.service.AutomationService;
import com.localmind.domain.calendar.port.out.CalendarPort;
import com.localmind.domain.calendar.service.CalendarService;
import com.localmind.domain.email.port.out.EmailPort;
import com.localmind.domain.email.service.EmailService;
import com.localmind.domain.llm.port.in.ChatUseCase;
import com.localmind.domain.messaging.port.in.MessagingChannelUseCase;
import com.localmind.domain.messaging.port.out.ChannelContextRepository;
import com.localmind.domain.messaging.port.out.MessagingChannelRepository;
import com.localmind.domain.messaging.port.out.MessagingClientPort;
import com.localmind.domain.messaging.service.MessagingChannelService;
import com.localmind.domain.messaging.service.MessagingDispatcherService;
import com.localmind.domain.marketplace.port.out.MarketplaceAgentRepository;
import com.localmind.domain.marketplace.port.out.MarketplaceReviewRepository;
import com.localmind.domain.marketplace.service.MarketplaceService;
import com.localmind.domain.knowledge.port.out.EntityExtractorPort;
import com.localmind.domain.knowledge.port.out.KnowledgeGraphPort;
import com.localmind.domain.knowledge.service.KnowledgeGraphService;
import com.localmind.domain.finetuning.port.out.FineTuningJobRepository;
import com.localmind.domain.finetuning.port.out.FineTuningPort;
import com.localmind.domain.finetuning.port.out.TrainingDatasetRepository;
import com.localmind.domain.finetuning.service.FineTuningService;
import com.localmind.domain.common.port.out.BackupHistoryPort;
import com.localmind.domain.common.port.out.BackupStoragePort;
import com.localmind.domain.common.port.out.ConfigExportPort;
import com.localmind.domain.common.port.out.DatabaseDumpPort;
import com.localmind.domain.common.port.out.DomainEventPublisherPort;
import com.localmind.domain.common.service.AnalyticsService;
import com.localmind.domain.common.service.BackupService;
import com.localmind.domain.plugin.port.out.PluginRegistryPort;
import com.localmind.domain.plugin.service.PluginManagementService;
import com.localmind.domain.document.port.in.DocumentIngestionPipelineUseCase;
import com.localmind.domain.document.port.out.*;
import com.localmind.domain.document.service.*;
import com.localmind.domain.llm.model.LlmProvider;
import com.localmind.domain.llm.port.out.ConversationExportPort;
import com.localmind.domain.llm.port.out.ConversationImportPort;
import com.localmind.domain.llm.port.out.ConversationRepository;
import com.localmind.domain.llm.port.out.LlmClient;
import com.localmind.domain.llm.port.out.LlmUsageRepository;
import com.localmind.domain.llm.port.out.OllamaModelPort;
import com.localmind.domain.llm.port.out.ProviderConfigRepository;
import com.localmind.domain.llm.port.out.StreamingLlmClient;
import com.localmind.domain.llm.service.ConversationExportService;
import com.localmind.domain.llm.service.ConversationService;
import com.localmind.domain.llm.service.LlmGatewayService;
import com.localmind.domain.llm.service.ModelManagementService;
import com.localmind.domain.llm.service.ProviderConfigService;
import com.localmind.domain.mcp.port.out.ApiDocumentationRepository;
import com.localmind.domain.mcp.port.out.AgileMetricRepository;
import com.localmind.domain.mcp.port.out.CodebaseKnowledgeRepository;
import com.localmind.domain.mcp.port.out.CicdMonitorRepository;
import com.localmind.domain.mcp.port.out.RetrospectiveRepository;
import com.localmind.domain.mcp.port.out.ScrumTaskRepository;
import com.localmind.domain.mcp.port.out.SprintRepository;
import com.localmind.domain.mcp.port.out.UserStoryRepository;
import com.localmind.domain.mcp.port.out.CodeReviewRepository;
import com.localmind.domain.mcp.port.out.DataMockRepository;
import com.localmind.domain.mcp.port.out.DependencyAnalysisRepository;
import com.localmind.domain.mcp.port.out.DockerAnalysisRepository;
import com.localmind.domain.mcp.port.out.HttpRequestRepository;
import com.localmind.domain.mcp.port.out.LogAnalysisRepository;
import com.localmind.domain.mcp.port.out.ProjectEconomicsRepository;
import com.localmind.domain.mcp.port.out.ScaffoldingRepository;
import com.localmind.domain.mcp.port.out.SnippetRepository;
import com.localmind.domain.mcp.port.out.PerformanceProfilerRepository;
import com.localmind.domain.mcp.port.out.SchemaExplorationRepository;
import com.localmind.domain.mcp.port.out.TestGeneratorRepository;
import com.localmind.domain.mcp.port.out.AccessPolicyRepository;
import com.localmind.domain.mcp.port.out.DecisionLogRepository;
import com.localmind.domain.mcp.port.out.StandupRepository;
import com.localmind.domain.mcp.port.out.IncidentRepository;
import com.localmind.domain.mcp.port.in.AgileMetricsUseCase;
import com.localmind.domain.mcp.port.in.IncidentManagerUseCase;
import com.localmind.domain.mcp.port.in.ProjectEconomicsUseCase;
import com.localmind.domain.mcp.port.in.QualityGateUseCase;
import com.localmind.domain.mcp.port.in.ScrumBoardUseCase;
import com.localmind.domain.mcp.port.in.TimeTrackingUseCase;
import com.localmind.domain.mcp.port.out.InsightRepository;
import com.localmind.domain.mcp.port.out.QualityGateRepository;
import com.localmind.domain.mcp.port.out.TimeTrackingRepository;
import com.localmind.domain.mcp.port.out.WorkflowRepository;
import com.localmind.domain.mcp.service.AccessPolicyService;
import com.localmind.domain.mcp.service.AgileMetricsService;
import com.localmind.domain.mcp.service.ApiDocumentationService;
import com.localmind.domain.mcp.service.CodebaseKnowledgeService;
import com.localmind.domain.mcp.service.CicdMonitorService;
import com.localmind.domain.mcp.service.DashboardService;
import com.localmind.domain.mcp.service.DecisionLogService;
import com.localmind.domain.mcp.service.RetrospectiveService;
import com.localmind.domain.mcp.service.ScrumBoardService;
import com.localmind.domain.mcp.service.CodeReviewService;
import com.localmind.domain.mcp.service.DataMockGeneratorService;
import com.localmind.domain.mcp.service.DbSchemaExplorerService;
import com.localmind.domain.mcp.service.DependencyAnalysisService;
import com.localmind.domain.mcp.service.DockerAnalysisService;
import com.localmind.domain.mcp.service.EnvironmentManagerService;
import com.localmind.domain.mcp.service.HttpClientService;
import com.localmind.domain.mcp.service.IncidentManagerService;
import com.localmind.domain.mcp.service.InsightEngineService;
import com.localmind.domain.mcp.service.LogAnalyzerService;
import com.localmind.domain.mcp.service.McpInternalRegistryService;
import com.localmind.domain.mcp.service.PerformanceProfilerService;
import com.localmind.domain.mcp.service.ProjectEconomicsService;
import com.localmind.domain.mcp.service.ProjectScaffoldingService;
import com.localmind.domain.mcp.service.QualityGateService;
import com.localmind.domain.mcp.service.RegexService;
import com.localmind.domain.mcp.service.SnippetService;
import com.localmind.domain.mcp.service.StandupService;
import com.localmind.domain.mcp.service.TestGeneratorService;
import com.localmind.domain.mcp.service.TimeTrackingService;
import com.localmind.domain.mcp.service.WorkflowOrchestratorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Configuration
public class DomainConfig {

    @Bean
    public DocumentService documentService(DocumentRepository documentRepository,
                                           VectorStorePort vectorStorePort,
                                           DocumentChunkRepository documentChunkRepository,
                                           Optional<DomainEventPublisherPort> eventPublisher) {
        return new DocumentService(documentRepository, vectorStorePort, documentChunkRepository,
                eventPublisher.orElse(null));
    }

    @Bean
    public DocumentSearchService documentSearchService(VectorStorePort vectorStorePort) {
        return new DocumentSearchService(vectorStorePort);
    }

    @Bean
    public ChunkingService chunkingService(
            @Value("${localmind.batch.chunk-size:500}") int chunkSize,
            @Value("${localmind.batch.chunk-overlap:50}") int chunkOverlap) {
        return new ChunkingService(chunkSize, chunkOverlap);
    }

    @Bean
    public PathValidationService pathValidationService() {
        return new PathValidationService();
    }

    @Bean
    public DocumentIngestionPipelineService documentIngestionPipelineService(
            DocumentRepository documentRepository,
            DocumentChunkRepository documentChunkRepository,
            TextExtractorPort textExtractorPort,
            ChunkingService chunkingService,
            VectorStorePort vectorStorePort,
            FileSystemScannerPort fileSystemScannerPort,
            FolderConfigRepository folderConfigRepository,
            Optional<OcrExtractorPort> ocrExtractorPort,
            Optional<DomainEventPublisherPort> eventPublisher) {
        return new DocumentIngestionPipelineService(documentRepository, documentChunkRepository,
                textExtractorPort, chunkingService, vectorStorePort, fileSystemScannerPort,
                folderConfigRepository, ocrExtractorPort, eventPublisher.orElse(null));
    }

    @Bean
    public FolderManagementService folderManagementService(
            FolderConfigRepository folderConfigRepository,
            DocumentIngestionPipelineUseCase ingestionPipeline,
            PathValidationService pathValidationService) {
        return new FolderManagementService(folderConfigRepository, ingestionPipeline, pathValidationService);
    }

    @Bean
    public LlmGatewayService llmGatewayService(
            List<LlmClient> clients,
            List<StreamingLlmClient> streamingClients,
            LlmUsageRepository usageRepository,
            ProviderConfigRepository providerConfigRepository,
            Optional<DomainEventPublisherPort> eventPublisher,
            @Value("${localmind.llm.fallback.order:OLLAMA,OPENAI,ANTHROPIC,GOOGLE}") String fallbackOrderStr,
            @Value("${localmind.llm.default-provider:OLLAMA}") String defaultProviderStr) {

        List<LlmProvider> fallbackOrder = Arrays.stream(fallbackOrderStr.split(","))
                .map(String::trim)
                .map(LlmProvider::valueOf)
                .toList();

        LlmProvider defaultProvider = LlmProvider.valueOf(defaultProviderStr.trim());

        return new LlmGatewayService(clients, streamingClients, usageRepository, providerConfigRepository,
                fallbackOrder, defaultProvider, eventPublisher.orElse(null));
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
    public ConversationExportService conversationExportService(
            ConversationRepository conversationRepository,
            ConversationExportPort conversationExportPort,
            ConversationImportPort conversationImportPort) {
        return new ConversationExportService(conversationRepository, conversationExportPort, conversationImportPort);
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

    @Bean
    public DockerAnalysisService dockerAnalysisService(DockerAnalysisRepository dockerAnalysisRepository) {
        return new DockerAnalysisService(dockerAnalysisRepository);
    }

    @Bean
    public LogAnalyzerService logAnalyzerService(LogAnalysisRepository logAnalysisRepository) {
        return new LogAnalyzerService(logAnalysisRepository);
    }

    @Bean
    public CicdMonitorService cicdMonitorService(CicdMonitorRepository cicdMonitorRepository) {
        return new CicdMonitorService(cicdMonitorRepository);
    }

    @Bean
    public DbSchemaExplorerService dbSchemaExplorerService(SchemaExplorationRepository schemaExplorationRepository) {
        return new DbSchemaExplorerService(schemaExplorationRepository);
    }

    @Bean
    public DataMockGeneratorService dataMockGeneratorService(DataMockRepository dataMockRepository) {
        return new DataMockGeneratorService(dataMockRepository);
    }

    @Bean
    public ApiDocumentationService apiDocumentationService(ApiDocumentationRepository apiDocumentationRepository) {
        return new ApiDocumentationService(apiDocumentationRepository);
    }

    @Bean
    public CodebaseKnowledgeService codebaseKnowledgeService(CodebaseKnowledgeRepository codebaseKnowledgeRepository) {
        return new CodebaseKnowledgeService(codebaseKnowledgeRepository);
    }

    @Bean
    public TimeTrackingService timeTrackingService(TimeTrackingRepository timeTrackingRepository) {
        return new TimeTrackingService(timeTrackingRepository);
    }

    @Bean
    public AgileMetricsService agileMetricsService(AgileMetricRepository agileMetricRepository) {
        return new AgileMetricsService(agileMetricRepository);
    }

    @Bean
    public RetrospectiveService retrospectiveService(RetrospectiveRepository retrospectiveRepository) {
        return new RetrospectiveService(retrospectiveRepository);
    }

    @Bean
    public ScrumBoardService scrumBoardService(SprintRepository sprintRepository,
                                               UserStoryRepository userStoryRepository,
                                               ScrumTaskRepository scrumTaskRepository) {
        return new ScrumBoardService(sprintRepository, userStoryRepository, scrumTaskRepository);
    }

    @Bean
    public ProjectEconomicsService projectEconomicsService(ProjectEconomicsRepository projectEconomicsRepository) {
        return new ProjectEconomicsService(projectEconomicsRepository);
    }

    @Bean
    public StandupService standupService(StandupRepository standupRepository) {
        return new StandupService(standupRepository);
    }

    @Bean
    public EnvironmentManagerService environmentManagerService() {
        return new EnvironmentManagerService();
    }

    @Bean
    public AccessPolicyService accessPolicyService(AccessPolicyRepository accessPolicyRepository) {
        return new AccessPolicyService(accessPolicyRepository);
    }

    @Bean
    public DecisionLogService decisionLogService(DecisionLogRepository decisionLogRepository) {
        return new DecisionLogService(decisionLogRepository);
    }

    @Bean
    public IncidentManagerService incidentManagerService(IncidentRepository incidentRepository) {
        return new IncidentManagerService(incidentRepository);
    }

    @Bean
    public WorkflowOrchestratorService workflowOrchestratorService(WorkflowRepository workflowRepository) {
        return new WorkflowOrchestratorService(workflowRepository);
    }

    @Bean
    public QualityGateService qualityGateService(QualityGateRepository qualityGateRepository) {
        return new QualityGateService(qualityGateRepository);
    }

    @Bean
    public InsightEngineService insightEngineService(InsightRepository insightRepository,
                                                       ScrumBoardUseCase scrumBoardUseCase,
                                                       ProjectEconomicsUseCase projectEconomicsUseCase,
                                                       IncidentManagerUseCase incidentManagerUseCase,
                                                       QualityGateUseCase qualityGateUseCase) {
        return new InsightEngineService(insightRepository, scrumBoardUseCase,
                                         projectEconomicsUseCase, incidentManagerUseCase,
                                         qualityGateUseCase);
    }

    @Bean
    public DashboardService dashboardService(ScrumBoardUseCase scrumBoardUseCase,
                                              AgileMetricsUseCase agileMetricsUseCase,
                                              TimeTrackingUseCase timeTrackingUseCase,
                                              ProjectEconomicsUseCase projectEconomicsUseCase,
                                              IncidentManagerUseCase incidentManagerUseCase,
                                              QualityGateUseCase qualityGateUseCase) {
        return new DashboardService(scrumBoardUseCase, agileMetricsUseCase,
                                     timeTrackingUseCase, projectEconomicsUseCase,
                                     incidentManagerUseCase, qualityGateUseCase);
    }

    @Bean
    public McpInternalRegistryService mcpInternalRegistryService() {
        return new McpInternalRegistryService();
    }

    @Bean
    public LocalAuthService localAuthService(LocalAuthRepository localAuthRepository,
                                              PasswordHasherPort passwordHasherPort) {
        return new LocalAuthService(localAuthRepository, passwordHasherPort);
    }

    @Bean
    public AutomationService automationService(WebhookRepository webhookRepository,
                                                WebhookClientPort webhookClientPort) {
        return new AutomationService(webhookRepository, webhookClientPort);
    }

    @Bean
    public PluginManagementService pluginManagementService(PluginRegistryPort pluginRegistryPort) {
        return new PluginManagementService(pluginRegistryPort);
    }

    @Bean
    public BackupService backupService(BackupStoragePort backupStoragePort,
                                        DatabaseDumpPort databaseDumpPort,
                                        BackupHistoryPort backupHistoryPort,
                                        ConfigExportPort configExportPort) {
        return new BackupService(backupStoragePort, databaseDumpPort, backupHistoryPort, configExportPort);
    }

    @Bean
    public AnalyticsService analyticsService(LlmUsageRepository usageRepository,
                                              ConversationRepository conversationRepository,
                                              com.localmind.domain.document.port.out.DocumentRepository documentRepository) {
        return new AnalyticsService(usageRepository, conversationRepository, documentRepository);
    }

    @Bean
    public CalendarService calendarService(Optional<CalendarPort> calendarPort) {
        return calendarPort.map(CalendarService::new).orElse(null);
    }

    @Bean
    public EmailService emailService(Optional<EmailPort> emailPort, List<LlmClient> clients) {
        if (emailPort.isEmpty()) {
            return null;
        }
        LlmClient defaultClient = clients.isEmpty() ? null : clients.get(0);
        return new EmailService(emailPort.get(), defaultClient);
    }

    @Bean
    public KnowledgeGraphService knowledgeGraphService(Optional<KnowledgeGraphPort> knowledgeGraphPort,
                                                       Optional<EntityExtractorPort> entityExtractorPort) {
        if (knowledgeGraphPort.isEmpty() || entityExtractorPort.isEmpty()) {
            return null;
        }
        return new KnowledgeGraphService(knowledgeGraphPort.get(), entityExtractorPort.get());
    }

    @Bean
    public MarketplaceService marketplaceService(MarketplaceAgentRepository agentRepository,
                                                  MarketplaceReviewRepository reviewRepository) {
        return new MarketplaceService(agentRepository, reviewRepository);
    }

    @Bean
    public FineTuningService fineTuningService(Optional<FineTuningPort> fineTuningPort,
                                                FineTuningJobRepository jobRepository,
                                                TrainingDatasetRepository datasetRepository) {
        if (fineTuningPort.isEmpty()) {
            return null;
        }
        return new FineTuningService(fineTuningPort.get(), jobRepository, datasetRepository);
    }

    @Bean
    public MessagingChannelService messagingChannelService(MessagingChannelRepository messagingChannelRepository,
                                                            List<MessagingClientPort> messagingClients) {
        return new MessagingChannelService(messagingChannelRepository, messagingClients);
    }

    @Bean
    public MessagingDispatcherService messagingDispatcherService(MessagingChannelRepository messagingChannelRepository,
                                                                  ChannelContextRepository channelContextRepository,
                                                                  ChatUseCase chatUseCase,
                                                                  List<MessagingClientPort> messagingClients) {
        return new MessagingDispatcherService(messagingChannelRepository, channelContextRepository,
                chatUseCase, messagingClients);
    }
}
