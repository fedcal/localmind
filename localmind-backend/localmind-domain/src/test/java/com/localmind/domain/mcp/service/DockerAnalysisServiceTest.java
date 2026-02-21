package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.ComposeAnalysis;
import com.localmind.domain.mcp.port.out.DockerAnalysisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DockerAnalysisServiceTest {

    @Mock
    private DockerAnalysisRepository repository;

    private DockerAnalysisService service;

    @BeforeEach
    void setUp() {
        service = new DockerAnalysisService(repository);
        when(repository.save(any(ComposeAnalysis.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    // ========== parseCompose Tests ==========

    @Test
    void parseCompose_withValidYaml_extractsServices() {
        String yaml = """
                services:
                  mysql:
                    image: mysql:8.0
                    ports:
                      - "3306:3306"
                  redis:
                    image: redis:7
                    ports:
                      - "6379:6379"
                """;

        Map<String, Object> result = service.parseCompose(yaml);

        assertThat(result.get("serviceCount")).isEqualTo(2);
        List<Map<String, Object>> services = (List<Map<String, Object>>) result.get("services");
        assertThat(services).hasSize(2);
        assertThat(services.get(0).get("name")).isEqualTo("mysql");
        assertThat(services.get(0).get("image")).isEqualTo("mysql:8.0");
        assertThat(services.get(1).get("name")).isEqualTo("redis");
        assertThat(services.get(1).get("image")).isEqualTo("redis:7");
    }

    @Test
    void parseCompose_withNetworksAndVolumes_extractsThem() {
        String yaml = """
                services:
                  app:
                    image: myapp:1.0
                networks:
                  backend:
                  frontend:
                volumes:
                  db_data:
                  cache_data:
                """;

        Map<String, Object> result = service.parseCompose(yaml);

        List<String> networks = (List<String>) result.get("networks");
        assertThat(networks).containsExactly("backend", "frontend");

        List<String> volumes = (List<String>) result.get("volumes");
        assertThat(volumes).containsExactly("db_data", "cache_data");
    }

    @Test
    void parseCompose_withLatestTag_generatesWarning() {
        String yaml = """
                services:
                  nginx:
                    image: nginx:latest
                """;

        Map<String, Object> result = service.parseCompose(yaml);

        List<Map<String, Object>> issues = (List<Map<String, Object>>) result.get("validationIssues");
        assertThat(issues).anyMatch(issue ->
                "warning".equals(issue.get("severity"))
                        && ((String) issue.get("message")).contains(":latest"));
    }

    @Test
    void parseCompose_withNoImageOrBuild_generatesError() {
        String yaml = """
                services:
                  broken:
                    ports:
                      - "8080:8080"
                """;

        Map<String, Object> result = service.parseCompose(yaml);

        List<Map<String, Object>> issues = (List<Map<String, Object>>) result.get("validationIssues");
        assertThat(issues).anyMatch(issue ->
                "error".equals(issue.get("severity"))
                        && ((String) issue.get("message")).contains("no 'image' or 'build'"));
    }

    @Test
    void parseCompose_withPrivilegedTrue_generatesError() {
        String yaml = """
                services:
                  dangerous:
                    image: myapp:1.0
                    privileged: true
                """;

        Map<String, Object> result = service.parseCompose(yaml);

        List<Map<String, Object>> issues = (List<Map<String, Object>>) result.get("validationIssues");
        assertThat(issues).anyMatch(issue ->
                "error".equals(issue.get("severity"))
                        && ((String) issue.get("message")).contains("privileged mode"));
    }

    @Test
    void parseCompose_withNetworkModeHost_generatesWarning() {
        String yaml = """
                services:
                  hostnet:
                    image: myapp:1.0
                    network_mode: host
                """;

        Map<String, Object> result = service.parseCompose(yaml);

        List<Map<String, Object>> issues = (List<Map<String, Object>>) result.get("validationIssues");
        assertThat(issues).anyMatch(issue ->
                "warning".equals(issue.get("severity"))
                        && ((String) issue.get("message")).contains("network_mode:host"));
    }

    @Test
    void parseCompose_withDeprecatedVersionKey_generatesWarning() {
        String yaml = """
                version: "3.8"
                services:
                  app:
                    image: myapp:1.0
                """;

        Map<String, Object> result = service.parseCompose(yaml);

        List<Map<String, Object>> issues = (List<Map<String, Object>>) result.get("validationIssues");
        assertThat(issues).anyMatch(issue ->
                "warning".equals(issue.get("severity"))
                        && ((String) issue.get("message")).contains("Deprecated 'version' key"));
    }

    @Test
    void parseCompose_withEmptyContent_returnsError() {
        Map<String, Object> result = service.parseCompose("");

        assertThat(result.get("error")).isEqualTo("Content is empty or null");
        verify(repository, never()).save(any());
    }

    @Test
    void parseCompose_withNullContent_returnsError() {
        Map<String, Object> result = service.parseCompose(null);

        assertThat(result.get("error")).isEqualTo("Content is empty or null");
        verify(repository, never()).save(any());
    }

    @Test
    void parseCompose_savesToRepository() {
        String yaml = """
                services:
                  app:
                    image: myapp:1.0
                """;

        service.parseCompose(yaml);

        ArgumentCaptor<ComposeAnalysis> captor = ArgumentCaptor.forClass(ComposeAnalysis.class);
        verify(repository).save(captor.capture());
        ComposeAnalysis saved = captor.getValue();
        assertThat(saved.getContentType()).isEqualTo("docker-compose");
        assertThat(saved.getServiceCount()).isEqualTo(1);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    // ========== analyzeDockerfile Tests ==========

    @Test
    void analyzeDockerfile_withBasicDockerfile_extractsBaseImageAndStages() {
        String dockerfile = """
                FROM openjdk:17-slim
                WORKDIR /app
                COPY target/app.jar app.jar
                EXPOSE 8080
                CMD ["java", "-jar", "app.jar"]
                """;

        Map<String, Object> result = service.analyzeDockerfile(dockerfile);

        assertThat(result.get("baseImage")).isEqualTo("openjdk:17-slim");
        assertThat(result.get("stages")).isEqualTo(1);
        assertThat(result.get("totalInstructions")).isEqualTo(5);
    }

    @Test
    void analyzeDockerfile_withLatestTag_generatesWarning() {
        String dockerfile = """
                FROM node:latest
                COPY . .
                CMD ["node", "index.js"]
                """;

        Map<String, Object> result = service.analyzeDockerfile(dockerfile);

        List<Map<String, Object>> issues = (List<Map<String, Object>>) result.get("issues");
        assertThat(issues).anyMatch(issue ->
                "latest-tag".equals(issue.get("rule"))
                        && "warning".equals(issue.get("severity")));
    }

    @Test
    void analyzeDockerfile_withNoTag_generatesWarning() {
        String dockerfile = """
                FROM ubuntu
                RUN apt-get update
                """;

        Map<String, Object> result = service.analyzeDockerfile(dockerfile);

        List<Map<String, Object>> issues = (List<Map<String, Object>>) result.get("issues");
        assertThat(issues).anyMatch(issue ->
                "no-tag".equals(issue.get("rule")));
    }

    @Test
    void analyzeDockerfile_withLargeBaseImage_generatesInfo() {
        String dockerfile = """
                FROM ubuntu:22.04
                RUN apt-get update
                """;

        Map<String, Object> result = service.analyzeDockerfile(dockerfile);

        List<Map<String, Object>> issues = (List<Map<String, Object>>) result.get("issues");
        assertThat(issues).anyMatch(issue ->
                "large-base-image".equals(issue.get("rule"))
                        && "info".equals(issue.get("severity")));
    }

    @Test
    void analyzeDockerfile_withConsecutiveRuns_generatesWarning() {
        String dockerfile = """
                FROM alpine:3.18
                RUN apk add --no-cache curl
                RUN apk add --no-cache git
                RUN apk add --no-cache bash
                """;

        Map<String, Object> result = service.analyzeDockerfile(dockerfile);

        List<Map<String, Object>> issues = (List<Map<String, Object>>) result.get("issues");
        assertThat(issues).anyMatch(issue ->
                "consecutive-run".equals(issue.get("rule")));
    }

    @Test
    void analyzeDockerfile_withAptGetWithoutNoInstallRecommends_generatesWarning() {
        String dockerfile = """
                FROM debian:11
                RUN apt-get update && apt-get install -y curl
                USER appuser
                HEALTHCHECK CMD curl -f http://localhost/
                """;

        Map<String, Object> result = service.analyzeDockerfile(dockerfile);

        List<Map<String, Object>> issues = (List<Map<String, Object>>) result.get("issues");
        assertThat(issues).anyMatch(issue ->
                "apt-no-recommends".equals(issue.get("rule")));
    }

    @Test
    void analyzeDockerfile_withAddInstruction_generatesInfo() {
        String dockerfile = """
                FROM alpine:3.18
                ADD app.tar.gz /app
                USER appuser
                HEALTHCHECK CMD wget -q --spider http://localhost/
                """;

        Map<String, Object> result = service.analyzeDockerfile(dockerfile);

        List<Map<String, Object>> issues = (List<Map<String, Object>>) result.get("issues");
        assertThat(issues).anyMatch(issue ->
                "add-vs-copy".equals(issue.get("rule")));
    }

    @Test
    void analyzeDockerfile_withNoHealthcheck_generatesWarning() {
        String dockerfile = """
                FROM alpine:3.18
                COPY app /app
                USER appuser
                CMD ["/app"]
                """;

        Map<String, Object> result = service.analyzeDockerfile(dockerfile);

        List<Map<String, Object>> issues = (List<Map<String, Object>>) result.get("issues");
        assertThat(issues).anyMatch(issue ->
                "no-healthcheck".equals(issue.get("rule")));
    }

    @Test
    void analyzeDockerfile_withNoUser_generatesWarning() {
        String dockerfile = """
                FROM alpine:3.18
                COPY app /app
                HEALTHCHECK CMD wget -q --spider http://localhost/
                CMD ["/app"]
                """;

        Map<String, Object> result = service.analyzeDockerfile(dockerfile);

        List<Map<String, Object>> issues = (List<Map<String, Object>>) result.get("issues");
        assertThat(issues).anyMatch(issue ->
                "no-user".equals(issue.get("rule")));
    }

    @Test
    void analyzeDockerfile_withMultiStage_countStagesCorrectly() {
        String dockerfile = """
                FROM maven:3.9-eclipse-temurin-17 AS build
                WORKDIR /build
                COPY pom.xml .
                RUN mvn dependency:go-offline
                COPY src ./src
                RUN mvn package -DskipTests

                FROM eclipse-temurin:17-jre-alpine
                WORKDIR /app
                COPY --from=build /build/target/app.jar app.jar
                USER 1000
                HEALTHCHECK CMD wget -q --spider http://localhost:8080/actuator/health
                CMD ["java", "-jar", "app.jar"]
                """;

        Map<String, Object> result = service.analyzeDockerfile(dockerfile);

        assertThat(result.get("stages")).isEqualTo(2);
        assertThat(result.get("baseImage")).isEqualTo("maven:3.9-eclipse-temurin-17");
    }

    @Test
    void analyzeDockerfile_withEmptyContent_returnsError() {
        Map<String, Object> result = service.analyzeDockerfile("");

        assertThat(result.get("error")).isEqualTo("Content is empty or null");
        verify(repository, never()).save(any());
    }

    @Test
    void analyzeDockerfile_buildsSummaryCorrectly() {
        String dockerfile = """
                FROM ubuntu:22.04
                RUN apt-get update
                RUN apt-get install -y curl
                ADD file.tar.gz /app
                CMD ["/app"]
                """;

        Map<String, Object> result = service.analyzeDockerfile(dockerfile);

        Map<String, Object> summary = (Map<String, Object>) result.get("summary");
        assertThat(summary).containsKey("errors");
        assertThat(summary).containsKey("warnings");
        assertThat(summary).containsKey("info");
        // Should have: large-base-image(info), no-tag is not triggered since :22.04 is present
        // consecutive-run(warning), apt-no-recommends(warning), add-vs-copy(info),
        // no-healthcheck(warning), no-user(warning)
        assertThat((int) summary.get("warnings")).isGreaterThanOrEqualTo(2);
        assertThat((int) summary.get("info")).isGreaterThanOrEqualTo(1);
    }

    @Test
    void analyzeDockerfile_savesToRepository() {
        String dockerfile = """
                FROM alpine:3.18
                USER appuser
                HEALTHCHECK CMD true
                CMD ["/bin/sh"]
                """;

        service.analyzeDockerfile(dockerfile);

        ArgumentCaptor<ComposeAnalysis> captor = ArgumentCaptor.forClass(ComposeAnalysis.class);
        verify(repository).save(captor.capture());
        ComposeAnalysis saved = captor.getValue();
        assertThat(saved.getContentType()).isEqualTo("dockerfile");
        assertThat(saved.getServiceCount()).isEqualTo(1);
    }

    // ========== listDockerServices Tests ==========

    @Test
    void listDockerServices_returnsNonEmptyList() {
        Map<String, Object> result = service.listDockerServices();

        int serviceCount = (int) result.get("serviceCount");
        List<Map<String, Object>> services = (List<Map<String, Object>>) result.get("services");

        assertThat(serviceCount).isGreaterThanOrEqualTo(10);
        assertThat(services).hasSameSizeAs(services);

        // Check that each service has required fields
        for (Map<String, Object> svc : services) {
            assertThat(svc).containsKeys("name", "image", "defaultPorts", "description");
            assertThat((String) svc.get("name")).isNotBlank();
            assertThat((String) svc.get("image")).isNotBlank();
            assertThat((List<?>) svc.get("defaultPorts")).isNotNull();
        }
    }

    @Test
    void listDockerServices_containsMySQLAndRedis() {
        Map<String, Object> result = service.listDockerServices();

        List<Map<String, Object>> services = (List<Map<String, Object>>) result.get("services");
        List<String> names = services.stream()
                .map(s -> (String) s.get("name"))
                .toList();

        assertThat(names).contains("MySQL", "Redis", "PostgreSQL", "Nginx");
    }

    // ========== generateCompose Tests ==========

    @Test
    void generateCompose_withSingleService_generatesValidYaml() {
        List<Map<String, Object>> services = new ArrayList<>();
        Map<String, Object> svc = new LinkedHashMap<>();
        svc.put("name", "mysql");
        svc.put("image", "mysql:8.0");
        svc.put("ports", List.of("3306:3306"));
        Map<String, String> env = new LinkedHashMap<>();
        env.put("MYSQL_ROOT_PASSWORD", "secret");
        svc.put("environment", env);
        svc.put("volumes", List.of("mysql_data:/var/lib/mysql"));
        services.add(svc);

        Map<String, Object> result = service.generateCompose(services);

        String yaml = (String) result.get("yaml");
        assertThat(yaml).contains("services:");
        assertThat(yaml).contains("mysql:");
        assertThat(yaml).contains("image: mysql:8.0");
        assertThat(yaml).contains("restart: unless-stopped");
        assertThat(yaml).contains("3306:3306");
        assertThat(yaml).contains("MYSQL_ROOT_PASSWORD: secret");
        assertThat(yaml).contains("mysql_data:/var/lib/mysql");
        assertThat((int) result.get("serviceCount")).isEqualTo(1);
    }

    @Test
    void generateCompose_withMultipleServices_generatesAll() {
        List<Map<String, Object>> services = new ArrayList<>();

        Map<String, Object> mysql = new LinkedHashMap<>();
        mysql.put("name", "mysql");
        mysql.put("image", "mysql:8.0");
        mysql.put("ports", List.of("3306:3306"));
        services.add(mysql);

        Map<String, Object> redis = new LinkedHashMap<>();
        redis.put("name", "redis");
        redis.put("image", "redis:7");
        redis.put("ports", List.of("6379:6379"));
        services.add(redis);

        Map<String, Object> result = service.generateCompose(services);

        String yaml = (String) result.get("yaml");
        assertThat(yaml).contains("mysql:");
        assertThat(yaml).contains("redis:");
        assertThat((int) result.get("serviceCount")).isEqualTo(2);
    }

    @Test
    void generateCompose_withEmptyList_returnsError() {
        Map<String, Object> result = service.generateCompose(new ArrayList<>());

        assertThat(result.get("error")).isEqualTo("No services provided");
        assertThat((int) result.get("serviceCount")).isEqualTo(0);
    }

    @Test
    void generateCompose_withNullList_returnsError() {
        Map<String, Object> result = service.generateCompose(null);

        assertThat(result.get("error")).isEqualTo("No services provided");
    }

    @Test
    void generateCompose_addsRestartPolicy() {
        List<Map<String, Object>> services = new ArrayList<>();
        Map<String, Object> svc = new LinkedHashMap<>();
        svc.put("name", "app");
        svc.put("image", "myapp:1.0");
        services.add(svc);

        Map<String, Object> result = service.generateCompose(services);

        String yaml = (String) result.get("yaml");
        assertThat(yaml).contains("restart: unless-stopped");
    }

    @Test
    void generateCompose_collectsNamedVolumes() {
        List<Map<String, Object>> services = new ArrayList<>();
        Map<String, Object> svc = new LinkedHashMap<>();
        svc.put("name", "db");
        svc.put("image", "postgres:16");
        svc.put("volumes", List.of("pg_data:/var/lib/postgresql/data"));
        services.add(svc);

        Map<String, Object> result = service.generateCompose(services);

        String yaml = (String) result.get("yaml");
        assertThat(yaml).contains("volumes:");
        assertThat(yaml).contains("pg_data:");
    }

    @Test
    void generateCompose_savesToRepository() {
        List<Map<String, Object>> services = new ArrayList<>();
        Map<String, Object> svc = new LinkedHashMap<>();
        svc.put("name", "app");
        svc.put("image", "myapp:1.0");
        services.add(svc);

        service.generateCompose(services);

        ArgumentCaptor<ComposeAnalysis> captor = ArgumentCaptor.forClass(ComposeAnalysis.class);
        verify(repository).save(captor.capture());
        ComposeAnalysis saved = captor.getValue();
        assertThat(saved.getContentType()).isEqualTo("generated-compose");
        assertThat(saved.getServiceCount()).isEqualTo(1);
    }

    // ========== Helper Methods Tests ==========

    @Test
    void getIndentLevel_calculatesCorrectly() {
        assertThat(service.getIndentLevel("services:")).isEqualTo(0);
        assertThat(service.getIndentLevel("  mysql:")).isEqualTo(1);
        assertThat(service.getIndentLevel("    image: mysql:8.0")).isEqualTo(2);
        assertThat(service.getIndentLevel("      - \"3306:3306\"")).isEqualTo(3);
    }

    @Test
    void extractInstruction_parsesValidInstructions() {
        assertThat(service.extractInstruction("FROM alpine:3.18")).isEqualTo("FROM");
        assertThat(service.extractInstruction("RUN apt-get update")).isEqualTo("RUN");
        assertThat(service.extractInstruction("COPY . /app")).isEqualTo("COPY");
        assertThat(service.extractInstruction("HEALTHCHECK CMD true")).isEqualTo("HEALTHCHECK");
        assertThat(service.extractInstruction("USER appuser")).isEqualTo("USER");
    }

    @Test
    void extractInstruction_returnsNullForInvalid() {
        assertThat(service.extractInstruction("")).isNull();
        assertThat(service.extractInstruction(null)).isNull();
        assertThat(service.extractInstruction("INVALIDCMD test")).isNull();
    }

    @Test
    void extractFromImage_parsesImageName() {
        assertThat(service.extractFromImage("FROM openjdk:17-slim")).isEqualTo("openjdk:17-slim");
        assertThat(service.extractFromImage("FROM alpine:3.18 AS build")).isEqualTo("alpine:3.18");
        assertThat(service.extractFromImage("FROM ubuntu")).isEqualTo("ubuntu");
    }
}
