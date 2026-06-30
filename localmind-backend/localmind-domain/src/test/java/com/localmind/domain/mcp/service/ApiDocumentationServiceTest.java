package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.ApiDocumentation;
import com.localmind.domain.mcp.port.out.ApiDocumentationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.file.Files;
import java.nio.file.Path;
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
class ApiDocumentationServiceTest {

    @Mock
    private ApiDocumentationRepository repository;

    private ApiDocumentationService service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        service = new ApiDocumentationService(repository);
        when(repository.save(any(ApiDocumentation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    // ========== extractEndpoints Tests ==========

    @Test
    void extractEndpoints_withNullPath_returnsError() {
        Map<String, Object> result = service.extractEndpoints(null);
        assertThat(result.get("error")).isEqualTo("filePath is empty or null");
    }

    @Test
    void extractEndpoints_withEmptyPath_returnsError() {
        Map<String, Object> result = service.extractEndpoints("");
        assertThat(result.get("error")).isEqualTo("filePath is empty or null");
    }

    @Test
    @SuppressWarnings("unchecked")
    void extractEndpoints_withSpringController_extractsMappings() throws Exception {
        String content = """
                @RestController
                @RequestMapping("/api/v1/users")
                public class UserController {
                    @GetMapping("/list")
                    public List<User> list() { }

                    @PostMapping("/create")
                    public User create() { }

                    @DeleteMapping("/remove")
                    public void remove() { }
                }
                """;
        Path file = tempDir.resolve("UserController.java");
        Files.writeString(file, content);

        Map<String, Object> result = service.extractEndpoints(file.toString());

        assertThat((int) result.get("endpointCount")).isEqualTo(3);
        List<Map<String, Object>> endpoints = (List<Map<String, Object>>) result.get("endpoints");
        assertThat(endpoints).hasSize(3);
        assertThat(endpoints.get(0).get("framework")).isEqualTo("spring");
    }

    @Test
    @SuppressWarnings("unchecked")
    void extractEndpoints_withExpressRoutes_extractsThem() throws Exception {
        String content = """
                const router = express.Router();
                router.get('/users', getUsers);
                router.post('/users', createUser);
                app.delete('/users/:id', deleteUser);
                """;
        Path file = tempDir.resolve("routes.js");
        Files.writeString(file, content);

        Map<String, Object> result = service.extractEndpoints(file.toString());

        assertThat((int) result.get("endpointCount")).isEqualTo(3);
        List<Map<String, Object>> endpoints = (List<Map<String, Object>>) result.get("endpoints");
        assertThat(endpoints).anyMatch(e -> "GET".equals(e.get("method")) && "/users".equals(e.get("path")));
        assertThat(endpoints).anyMatch(e -> "DELETE".equals(e.get("method")));
    }

    @Test
    @SuppressWarnings("unchecked")
    void extractEndpoints_withNestJsController_extractsThem() throws Exception {
        String content = """
                @Controller('users')
                export class UsersController {
                    @Get('list')
                    findAll() { }

                    @Post('create')
                    create() { }
                }
                """;
        Path file = tempDir.resolve("users.controller.ts");
        Files.writeString(file, content);

        Map<String, Object> result = service.extractEndpoints(file.toString());

        assertThat((int) result.get("endpointCount")).isEqualTo(2);
        List<Map<String, Object>> endpoints = (List<Map<String, Object>>) result.get("endpoints");
        assertThat(endpoints.get(0).get("framework")).isEqualTo("nestjs");
    }

    @Test
    void extractEndpoints_withEmptyFile_returnsZeroEndpoints() throws Exception {
        Path file = tempDir.resolve("empty.java");
        Files.writeString(file, "");

        Map<String, Object> result = service.extractEndpoints(file.toString());

        assertThat((int) result.get("endpointCount")).isEqualTo(0);
    }

    @Test
    void extractEndpoints_withNonExistentFile_returnsEmptyEndpoints() {
        Map<String, Object> result = service.extractEndpoints("/nonexistent/path/file.java");

        assertThat((int) result.get("endpointCount")).isEqualTo(0);
    }

    @Test
    void extractEndpoints_savesToRepository() throws Exception {
        Path file = tempDir.resolve("test.java");
        Files.writeString(file, "@GetMapping(\"/test\") public void test() {}");

        service.extractEndpoints(file.toString());

        ArgumentCaptor<ApiDocumentation> captor = ArgumentCaptor.forClass(ApiDocumentation.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getDocumentationType()).isEqualTo("extract-endpoints");
        assertThat(captor.getValue().getId()).isNotNull();
    }

    // ========== generateOpenApi Tests ==========

    @Test
    void generateOpenApi_withNullEndpoints_returnsError() {
        Map<String, Object> result = service.generateOpenApi(null, "API", "1.0.0");
        assertThat(result.get("error")).isEqualTo("No endpoints provided");
    }

    @Test
    void generateOpenApi_withEmptyEndpoints_returnsError() {
        Map<String, Object> result = service.generateOpenApi(new ArrayList<>(), "API", "1.0.0");
        assertThat(result.get("error")).isEqualTo("No endpoints provided");
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateOpenApi_withValidEndpoints_generatesSpec() {
        List<Map<String, Object>> endpoints = new ArrayList<>();
        Map<String, Object> ep = new LinkedHashMap<>();
        ep.put("method", "GET");
        ep.put("path", "/api/users");
        endpoints.add(ep);

        Map<String, Object> result = service.generateOpenApi(endpoints, "User API", "1.0.0");

        assertThat(result.get("title")).isEqualTo("User API");
        assertThat(result.get("version")).isEqualTo("1.0.0");
        assertThat((int) result.get("endpointCount")).isEqualTo(1);
        Map<String, Object> spec = (Map<String, Object>) result.get("openApiSpec");
        assertThat(spec.get("openapi")).isEqualTo("3.0.3");
        assertThat(spec).containsKey("paths");
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateOpenApi_convertsExpressParamsToOpenApi() {
        List<Map<String, Object>> endpoints = new ArrayList<>();
        Map<String, Object> ep = new LinkedHashMap<>();
        ep.put("method", "GET");
        ep.put("path", "/users/:id/posts/:postId");
        endpoints.add(ep);

        Map<String, Object> result = service.generateOpenApi(endpoints, "API", "1.0.0");

        Map<String, Object> spec = (Map<String, Object>) result.get("openApiSpec");
        Map<String, Object> paths = (Map<String, Object>) spec.get("paths");
        assertThat(paths).containsKey("/users/{id}/posts/{postId}");
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateOpenApi_addsRequestBodyForPost() {
        List<Map<String, Object>> endpoints = new ArrayList<>();
        Map<String, Object> ep = new LinkedHashMap<>();
        ep.put("method", "POST");
        ep.put("path", "/users");
        endpoints.add(ep);

        Map<String, Object> result = service.generateOpenApi(endpoints, "API", "1.0.0");

        Map<String, Object> spec = (Map<String, Object>) result.get("openApiSpec");
        Map<String, Object> paths = (Map<String, Object>) spec.get("paths");
        Map<String, Object> pathItem = (Map<String, Object>) paths.get("/users");
        Map<String, Object> operation = (Map<String, Object>) pathItem.get("post");
        assertThat(operation).containsKey("requestBody");
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateOpenApi_addsStandardResponses() {
        List<Map<String, Object>> endpoints = new ArrayList<>();
        Map<String, Object> ep = new LinkedHashMap<>();
        ep.put("method", "GET");
        ep.put("path", "/users");
        endpoints.add(ep);

        Map<String, Object> result = service.generateOpenApi(endpoints, "API", "1.0.0");

        Map<String, Object> spec = (Map<String, Object>) result.get("openApiSpec");
        Map<String, Object> paths = (Map<String, Object>) spec.get("paths");
        Map<String, Object> pathItem = (Map<String, Object>) paths.get("/users");
        Map<String, Object> operation = (Map<String, Object>) pathItem.get("get");
        Map<String, Object> responses = (Map<String, Object>) operation.get("responses");
        assertThat(responses).containsKeys("200", "400", "404", "500");
    }

    @Test
    void generateOpenApi_withNullTitle_usesDefault() {
        List<Map<String, Object>> endpoints = new ArrayList<>();
        Map<String, Object> ep = new LinkedHashMap<>();
        ep.put("method", "GET");
        ep.put("path", "/test");
        endpoints.add(ep);

        Map<String, Object> result = service.generateOpenApi(endpoints, null, null);

        assertThat(result.get("title")).isEqualTo("API Documentation");
        assertThat(result.get("version")).isEqualTo("1.0.0");
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateOpenApi_addsTags() {
        List<Map<String, Object>> endpoints = new ArrayList<>();
        Map<String, Object> ep = new LinkedHashMap<>();
        ep.put("method", "GET");
        ep.put("path", "/users/list");
        endpoints.add(ep);

        Map<String, Object> result = service.generateOpenApi(endpoints, "API", "1.0.0");

        Map<String, Object> spec = (Map<String, Object>) result.get("openApiSpec");
        Map<String, Object> paths = (Map<String, Object>) spec.get("paths");
        Map<String, Object> pathItem = (Map<String, Object>) paths.get("/users/list");
        Map<String, Object> operation = (Map<String, Object>) pathItem.get("get");
        assertThat((List<String>) operation.get("tags")).contains("users");
    }

    @Test
    void generateOpenApi_savesToRepository() {
        List<Map<String, Object>> endpoints = new ArrayList<>();
        Map<String, Object> ep = new LinkedHashMap<>();
        ep.put("method", "GET");
        ep.put("path", "/test");
        endpoints.add(ep);

        service.generateOpenApi(endpoints, "API", "1.0.0");

        ArgumentCaptor<ApiDocumentation> captor = ArgumentCaptor.forClass(ApiDocumentation.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getDocumentationType()).isEqualTo("generate-openapi");
    }

    // ========== findUndocumented Tests ==========

    @Test
    void findUndocumented_withNullPath_returnsError() {
        Map<String, Object> result = service.findUndocumented(null);
        assertThat(result.get("error")).isEqualTo("filePath is empty or null");
    }

    @Test
    void findUndocumented_withEmptyPath_returnsError() {
        Map<String, Object> result = service.findUndocumented("");
        assertThat(result.get("error")).isEqualTo("filePath is empty or null");
        verify(repository, never()).save(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void findUndocumented_detectsUndocumentedFunctions() throws Exception {
        String content = """
                /** Documented function */
                export function documented() {}

                export function undocumented() {}

                /** Documented class */
                export class MyClass {}

                export class UndocClass {}
                """;
        Path file = tempDir.resolve("module.ts");
        Files.writeString(file, content);

        Map<String, Object> result = service.findUndocumented(file.toString());

        assertThat((int) result.get("totalExports")).isEqualTo(4);
        assertThat((int) result.get("documented")).isEqualTo(2);
        assertThat((int) result.get("undocumented")).isEqualTo(2);
        assertThat((double) result.get("coveragePercent")).isEqualTo(50.0);
    }

    @Test
    void findUndocumented_withFullyDocumentedFile_returns100Percent() throws Exception {
        String content = """
                /** Function doc */
                export function myFunc() {}

                /** Class doc */
                export class MyClass {}
                """;
        Path file = tempDir.resolve("documented.ts");
        Files.writeString(file, content);

        Map<String, Object> result = service.findUndocumented(file.toString());

        assertThat((double) result.get("coveragePercent")).isEqualTo(100.0);
        assertThat((int) result.get("undocumented")).isEqualTo(0);
    }

    @Test
    void findUndocumented_withEmptyFile_returns100Percent() throws Exception {
        Path file = tempDir.resolve("empty.ts");
        Files.writeString(file, "");

        Map<String, Object> result = service.findUndocumented(file.toString());

        assertThat((double) result.get("coveragePercent")).isEqualTo(100.0);
        assertThat((int) result.get("totalExports")).isEqualTo(0);
    }

    @Test
    void findUndocumented_savesToRepository() throws Exception {
        Path file = tempDir.resolve("test.ts");
        Files.writeString(file, "export function test() {}");

        service.findUndocumented(file.toString());

        ArgumentCaptor<ApiDocumentation> captor = ArgumentCaptor.forClass(ApiDocumentation.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getDocumentationType()).isEqualTo("find-undocumented");
    }
}
