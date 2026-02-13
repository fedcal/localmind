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
    public InsightEngineService insightEngineService(InsightRepository insightRepository) {
        return new InsightEngineService(insightRepository);
    }

    @Bean
    public DashboardService dashboardService() {
        return new DashboardService();
    }

    @Bean
    public McpInternalRegistryService mcpInternalRegistryService() {
        return new McpInternalRegistryService();
    }
}
