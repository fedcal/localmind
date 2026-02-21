package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.ScaffoldedProject;
import com.localmind.domain.mcp.port.out.ScaffoldingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectScaffoldingServiceTest {

    @Mock
    private ScaffoldingRepository scaffoldingRepository;

    private ProjectScaffoldingService service;

    @BeforeEach
    void setUp() {
        service = new ProjectScaffoldingService(scaffoldingRepository);
    }

    // --- listProjectTemplates ---

    @Test
    void listProjectTemplates_returnsFiveTemplates() {
        List<Map<String, Object>> templates = service.listProjectTemplates();

        assertThat(templates).hasSize(5);
    }

    @Test
    void listProjectTemplates_eachTemplateHasNameDescriptionFiles() {
        List<Map<String, Object>> templates = service.listProjectTemplates();

        for (Map<String, Object> template : templates) {
            assertThat(template).containsKeys("name", "description", "files");
            assertThat((String) template.get("name")).isNotBlank();
            assertThat((String) template.get("description")).isNotBlank();
            assertThat((List<?>) template.get("files")).isNotEmpty();
        }
    }

    @Test
    void listProjectTemplates_containsExpectedTemplateNames() {
        List<Map<String, Object>> templates = service.listProjectTemplates();

        List<String> names = templates.stream()
                .map(t -> (String) t.get("name"))
                .toList();

        assertThat(names).containsExactly(
                "spring-boot-api",
                "angular-app",
                "maven-multi-module",
                "mcp-server",
                "react-app"
        );
    }

    // --- scaffoldProject: template valido ---

    @Test
    void scaffoldProject_validTemplate_returnsGeneratedFiles() {
        when(scaffoldingRepository.save(any(ScaffoldedProject.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> result = service.scaffoldProject(
                "spring-boot-api", "my-app", "/tmp", "testauthor", "My description", "Apache-2.0");

        assertThat(result.get("template")).isEqualTo("spring-boot-api");
        assertThat(result.get("projectName")).isEqualTo("my-app");
        assertThat((int) result.get("totalFiles")).isGreaterThanOrEqualTo(4);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> files = (List<Map<String, Object>>) result.get("files");
        assertThat(files).isNotEmpty();

        for (Map<String, Object> file : files) {
            assertThat(file).containsKeys("path", "content");
            assertThat((String) file.get("path")).isNotBlank();
            assertThat((String) file.get("content")).isNotBlank();
        }
    }

    // --- scaffoldProject: template invalido ---

    @Test
    void scaffoldProject_invalidTemplate_throwsException() {
        assertThatThrownBy(() ->
                service.scaffoldProject("non-existent", "my-app", "/tmp", "author", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Template non trovato: non-existent");
    }

    // --- scaffoldProject: placeholder sostituzione corretta ---

    @Test
    void scaffoldProject_replacesPlaceholdersCorrectly() {
        when(scaffoldingRepository.save(any(ScaffoldedProject.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> result = service.scaffoldProject(
                "spring-boot-api", "my-cool-app", "/output", "johndoe", "A cool app", "MIT");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> files = (List<Map<String, Object>>) result.get("files");

        // Verifica che i placeholder siano stati sostituiti nel contenuto
        for (Map<String, Object> file : files) {
            String content = (String) file.get("content");
            assertThat(content).doesNotContain("{{projectName}}");
            assertThat(content).doesNotContain("{{author}}");
            assertThat(content).doesNotContain("{{description}}");
            assertThat(content).doesNotContain("{{license}}");
        }

        // Verifica che i placeholder siano stati sostituiti nei path
        for (Map<String, Object> file : files) {
            String path = (String) file.get("path");
            assertThat(path).doesNotContain("{{projectName}}");
            assertThat(path).doesNotContain("{{author}}");
        }

        // Verifica valori specifici nel pom.xml
        Map<String, Object> pomFile = files.stream()
                .filter(f -> "pom.xml".equals(f.get("path")))
                .findFirst()
                .orElseThrow();
        String pomContent = (String) pomFile.get("content");
        assertThat(pomContent).contains("my-cool-app");
        assertThat(pomContent).contains("A cool app");
        assertThat(pomContent).contains("johndoe");
    }

    // --- scaffoldProject: default license ---

    @Test
    void scaffoldProject_nullLicense_defaultsToMit() {
        when(scaffoldingRepository.save(any(ScaffoldedProject.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> result = service.scaffoldProject(
                "react-app", "my-app", "/tmp", "author", "desc", null);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> files = (List<Map<String, Object>>) result.get("files");

        Map<String, Object> packageJson = files.stream()
                .filter(f -> "package.json".equals(f.get("path")))
                .findFirst()
                .orElseThrow();
        String content = (String) packageJson.get("content");
        assertThat(content).contains("\"license\": \"MIT\"");
    }

    // --- scaffoldProject: default description ---

    @Test
    void scaffoldProject_nullDescription_defaultsToTemplateDescription() {
        when(scaffoldingRepository.save(any(ScaffoldedProject.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> result = service.scaffoldProject(
                "spring-boot-api", "my-app", "/tmp", "author", null, "MIT");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> files = (List<Map<String, Object>>) result.get("files");

        Map<String, Object> pomFile = files.stream()
                .filter(f -> "pom.xml".equals(f.get("path")))
                .findFirst()
                .orElseThrow();
        String content = (String) pomFile.get("content");
        assertThat(content).contains("A Spring Boot REST API con Java 17, Maven, Spring Security project");
    }

    // --- scaffoldProject: salvataggio nel repository ---

    @Test
    void scaffoldProject_savesRecordToRepository() {
        when(scaffoldingRepository.save(any(ScaffoldedProject.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.scaffoldProject("spring-boot-api", "test-project", "/output", "author", "desc", "MIT");

        ArgumentCaptor<ScaffoldedProject> captor = ArgumentCaptor.forClass(ScaffoldedProject.class);
        verify(scaffoldingRepository).save(captor.capture());

        ScaffoldedProject saved = captor.getValue();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTemplateName()).isEqualTo("spring-boot-api");
        assertThat(saved.getProjectName()).isEqualTo("test-project");
        assertThat(saved.getOutputPath()).isEqualTo("/output/test-project");
        assertThat(saved.getFilesGenerated()).isGreaterThanOrEqualTo(4);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    // --- scaffoldComponent: java service ---

    @Test
    void scaffoldComponent_javaService_generatesCrudMethods() {
        when(scaffoldingRepository.save(any(ScaffoldedProject.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> result = service.scaffoldComponent("user", "service", "java", "/output");

        assertThat(result.get("name")).isEqualTo("user");
        assertThat(result.get("type")).isEqualTo("service");
        assertThat(result.get("language")).isEqualTo("java");
        assertThat(result.get("fileName")).isEqualTo("UserService.java");

        String code = (String) result.get("code");
        assertThat(code).contains("class UserService");
        assertThat(code).contains("findAll()");
        assertThat(code).contains("findById(");
        assertThat(code).contains("create(");
        assertThat(code).contains("update(");
        assertThat(code).contains("delete(");
    }

    // --- scaffoldComponent: java controller ---

    @Test
    void scaffoldComponent_javaController_generatesRestEndpoints() {
        when(scaffoldingRepository.save(any(ScaffoldedProject.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> result = service.scaffoldComponent("product", "controller", "java", "/output");

        assertThat(result.get("fileName")).isEqualTo("ProductController.java");

        String code = (String) result.get("code");
        assertThat(code).contains("@RestController");
        assertThat(code).contains("@RequestMapping");
        assertThat(code).contains("@GetMapping");
        assertThat(code).contains("@PostMapping");
        assertThat(code).contains("@PutMapping");
        assertThat(code).contains("@DeleteMapping");
    }

    // --- scaffoldComponent: java model ---

    @Test
    void scaffoldComponent_javaModel_generatesModelWithBuilder() {
        when(scaffoldingRepository.save(any(ScaffoldedProject.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> result = service.scaffoldComponent("order", "model", "java", "/output");

        assertThat(result.get("fileName")).isEqualTo("Order.java");

        String code = (String) result.get("code");
        assertThat(code).contains("class Order");
        assertThat(code).contains("String id");
        assertThat(code).contains("Instant createdAt");
        assertThat(code).contains("Instant updatedAt");
        assertThat(code).contains("class Builder");
        assertThat(code).contains("builder()");
    }

    // --- scaffoldComponent: typescript service ---

    @Test
    void scaffoldComponent_typescriptService_generatesAsyncCrudMethods() {
        when(scaffoldingRepository.save(any(ScaffoldedProject.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> result = service.scaffoldComponent("item", "service", "typescript", "/output");

        assertThat(result.get("fileName")).isEqualTo("item.service.ts");
        assertThat(result.get("language")).isEqualTo("typescript");

        String code = (String) result.get("code");
        assertThat(code).contains("class ItemService");
        assertThat(code).contains("async findAll()");
        assertThat(code).contains("async findById(");
        assertThat(code).contains("async create(");
        assertThat(code).contains("async update(");
        assertThat(code).contains("async delete(");
    }

    // --- scaffoldComponent: typescript component ---

    @Test
    void scaffoldComponent_typescriptComponent_generatesReactComponent() {
        when(scaffoldingRepository.save(any(ScaffoldedProject.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> result = service.scaffoldComponent("dashboard", "component", "typescript", "/output");

        assertThat(result.get("fileName")).isEqualTo("dashboard.component.tsx");

        String code = (String) result.get("code");
        assertThat(code).contains("import React from 'react'");
        assertThat(code).contains("Dashboard");
        assertThat(code).contains("React.FC");
    }

    // --- scaffoldComponent: tipo invalido ---

    @Test
    void scaffoldComponent_invalidType_throwsException() {
        assertThatThrownBy(() ->
                service.scaffoldComponent("test", "widget", "java", "/output"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tipo componente non valido: widget");
    }

    // --- scaffoldComponent: linguaggio invalido ---

    @Test
    void scaffoldComponent_invalidLanguage_throwsException() {
        assertThatThrownBy(() ->
                service.scaffoldComponent("test", "service", "python", "/output"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Linguaggio non valido: python");
    }

    // --- scaffoldComponent: salvataggio nel repository ---

    @Test
    void scaffoldComponent_savesRecordToRepository() {
        when(scaffoldingRepository.save(any(ScaffoldedProject.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.scaffoldComponent("user", "service", "java", "/output");

        ArgumentCaptor<ScaffoldedProject> captor = ArgumentCaptor.forClass(ScaffoldedProject.class);
        verify(scaffoldingRepository).save(captor.capture());

        ScaffoldedProject saved = captor.getValue();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTemplateName()).isEqualTo("component-service");
        assertThat(saved.getProjectName()).isEqualTo("user");
        assertThat(saved.getOutputPath()).isEqualTo("/output/UserService.java");
        assertThat(saved.getFilesGenerated()).isEqualTo(1);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    // --- scaffoldComponent: javascript service ---

    @Test
    void scaffoldComponent_javascriptService_generatesJsServiceClass() {
        when(scaffoldingRepository.save(any(ScaffoldedProject.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> result = service.scaffoldComponent("payment", "service", "javascript", "/output");

        assertThat(result.get("fileName")).isEqualTo("payment.service.js");
        assertThat(result.get("language")).isEqualTo("javascript");

        String code = (String) result.get("code");
        assertThat(code).contains("class PaymentService");
        assertThat(code).contains("async findAll()");
        assertThat(code).contains("async create(data)");
    }

    // --- scaffoldComponent: typescript model ---

    @Test
    void scaffoldComponent_typescriptModel_generatesInterfaceAndFactory() {
        when(scaffoldingRepository.save(any(ScaffoldedProject.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> result = service.scaffoldComponent("task", "model", "typescript", "/output");

        assertThat(result.get("fileName")).isEqualTo("task.model.ts");

        String code = (String) result.get("code");
        assertThat(code).contains("export interface Task");
        assertThat(code).contains("export function createTask(");
        assertThat(code).contains("export function isTask(");
    }
}
