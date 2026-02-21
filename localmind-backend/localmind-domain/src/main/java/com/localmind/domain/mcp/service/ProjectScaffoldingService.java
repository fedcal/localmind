package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.ScaffoldedProject;
import com.localmind.domain.mcp.port.in.ProjectScaffoldingUseCase;
import com.localmind.domain.mcp.port.out.ScaffoldingRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProjectScaffoldingService implements ProjectScaffoldingUseCase {

    private final ScaffoldingRepository scaffoldingRepository;

    /**
     * Mappa dei template disponibili: templateName -> TemplateDefinition.
     */
    private static final Map<String, TemplateDefinition> TEMPLATES = new LinkedHashMap<>();

    static {
        // --- spring-boot-api ---
        TEMPLATES.put("spring-boot-api", new TemplateDefinition(
                "spring-boot-api",
                "Spring Boot REST API con Java 17, Maven, Spring Security",
                List.of(
                        new TemplateFile("pom.xml",
                                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                                "    <modelVersion>4.0.0</modelVersion>\n" +
                                "    <parent>\n" +
                                "        <groupId>org.springframework.boot</groupId>\n" +
                                "        <artifactId>spring-boot-starter-parent</artifactId>\n" +
                                "        <version>3.4.2</version>\n" +
                                "    </parent>\n" +
                                "    <groupId>com.{{author}}</groupId>\n" +
                                "    <artifactId>{{projectName}}</artifactId>\n" +
                                "    <version>0.0.1-SNAPSHOT</version>\n" +
                                "    <name>{{projectName}}</name>\n" +
                                "    <description>{{description}}</description>\n" +
                                "    <properties>\n" +
                                "        <java.version>17</java.version>\n" +
                                "    </properties>\n" +
                                "    <dependencies>\n" +
                                "        <dependency>\n" +
                                "            <groupId>org.springframework.boot</groupId>\n" +
                                "            <artifactId>spring-boot-starter-web</artifactId>\n" +
                                "        </dependency>\n" +
                                "        <dependency>\n" +
                                "            <groupId>org.springframework.boot</groupId>\n" +
                                "            <artifactId>spring-boot-starter-security</artifactId>\n" +
                                "        </dependency>\n" +
                                "        <dependency>\n" +
                                "            <groupId>org.springframework.boot</groupId>\n" +
                                "            <artifactId>spring-boot-starter-data-jpa</artifactId>\n" +
                                "        </dependency>\n" +
                                "        <dependency>\n" +
                                "            <groupId>org.springframework.boot</groupId>\n" +
                                "            <artifactId>spring-boot-starter-test</artifactId>\n" +
                                "            <scope>test</scope>\n" +
                                "        </dependency>\n" +
                                "    </dependencies>\n" +
                                "    <build>\n" +
                                "        <plugins>\n" +
                                "            <plugin>\n" +
                                "                <groupId>org.springframework.boot</groupId>\n" +
                                "                <artifactId>spring-boot-maven-plugin</artifactId>\n" +
                                "            </plugin>\n" +
                                "        </plugins>\n" +
                                "    </build>\n" +
                                "</project>\n"),
                        new TemplateFile("src/main/java/com/{{projectName}}/Application.java",
                                "package com.{{projectName}};\n\n" +
                                "import org.springframework.boot.SpringApplication;\n" +
                                "import org.springframework.boot.autoconfigure.SpringBootApplication;\n\n" +
                                "@SpringBootApplication\n" +
                                "public class Application {\n\n" +
                                "    public static void main(String[] args) {\n" +
                                "        SpringApplication.run(Application.class, args);\n" +
                                "    }\n" +
                                "}\n"),
                        new TemplateFile("src/main/resources/application.yml",
                                "spring:\n" +
                                "  application:\n" +
                                "    name: {{projectName}}\n" +
                                "server:\n" +
                                "  port: 8080\n"),
                        new TemplateFile("README.md",
                                "# {{projectName}}\n\n" +
                                "{{description}}\n\n" +
                                "## Author\n" +
                                "{{author}}\n\n" +
                                "## License\n" +
                                "{{license}}\n\n" +
                                "## Getting Started\n\n" +
                                "```bash\n" +
                                "mvn spring-boot:run\n" +
                                "```\n")
                )
        ));

        // --- angular-app ---
        TEMPLATES.put("angular-app", new TemplateDefinition(
                "angular-app",
                "Angular standalone app con routing, i18n, SCSS",
                List.of(
                        new TemplateFile("package.json",
                                "{\n" +
                                "  \"name\": \"{{projectName}}\",\n" +
                                "  \"version\": \"0.0.1\",\n" +
                                "  \"description\": \"{{description}}\",\n" +
                                "  \"author\": \"{{author}}\",\n" +
                                "  \"license\": \"{{license}}\",\n" +
                                "  \"scripts\": {\n" +
                                "    \"start\": \"ng serve\",\n" +
                                "    \"build\": \"ng build\",\n" +
                                "    \"test\": \"ng test\"\n" +
                                "  },\n" +
                                "  \"dependencies\": {\n" +
                                "    \"@angular/core\": \"^21.0.0\",\n" +
                                "    \"@angular/router\": \"^21.0.0\",\n" +
                                "    \"@angular/common\": \"^21.0.0\",\n" +
                                "    \"@ngx-translate/core\": \"^16.0.0\",\n" +
                                "    \"zone.js\": \"~0.15.0\"\n" +
                                "  }\n" +
                                "}\n"),
                        new TemplateFile("angular.json",
                                "{\n" +
                                "  \"$schema\": \"./node_modules/@angular/cli/lib/config/schema.json\",\n" +
                                "  \"version\": 1,\n" +
                                "  \"projects\": {\n" +
                                "    \"{{projectName}}\": {\n" +
                                "      \"root\": \"\",\n" +
                                "      \"sourceRoot\": \"src\",\n" +
                                "      \"architect\": {\n" +
                                "        \"build\": {\n" +
                                "          \"options\": {\n" +
                                "            \"outputPath\": \"dist/{{projectName}}\",\n" +
                                "            \"styles\": [\"src/styles.scss\"],\n" +
                                "            \"polyfills\": [\"zone.js\"]\n" +
                                "          }\n" +
                                "        }\n" +
                                "      }\n" +
                                "    }\n" +
                                "  }\n" +
                                "}\n"),
                        new TemplateFile("src/app/app.component.ts",
                                "import { Component } from '@angular/core';\n" +
                                "import { RouterOutlet } from '@angular/router';\n\n" +
                                "@Component({\n" +
                                "  selector: 'app-root',\n" +
                                "  standalone: true,\n" +
                                "  imports: [RouterOutlet],\n" +
                                "  template: `<router-outlet />`\n" +
                                "})\n" +
                                "export class AppComponent {\n" +
                                "  title = '{{projectName}}';\n" +
                                "}\n"),
                        new TemplateFile("src/app/app.routes.ts",
                                "import { Routes } from '@angular/router';\n\n" +
                                "export const routes: Routes = [\n" +
                                "  { path: '', redirectTo: 'home', pathMatch: 'full' },\n" +
                                "  { path: 'home', loadComponent: () => import('./features/home/home.component').then(m => m.HomeComponent) }\n" +
                                "];\n")
                )
        ));

        // --- maven-multi-module ---
        TEMPLATES.put("maven-multi-module", new TemplateDefinition(
                "maven-multi-module",
                "Maven multi-module con domain, infrastructure, api, app",
                List.of(
                        new TemplateFile("pom.xml",
                                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                                "    <modelVersion>4.0.0</modelVersion>\n" +
                                "    <groupId>com.{{author}}</groupId>\n" +
                                "    <artifactId>{{projectName}}</artifactId>\n" +
                                "    <version>0.0.1-SNAPSHOT</version>\n" +
                                "    <packaging>pom</packaging>\n" +
                                "    <name>{{projectName}}</name>\n" +
                                "    <description>{{description}}</description>\n" +
                                "    <modules>\n" +
                                "        <module>{{projectName}}-domain</module>\n" +
                                "        <module>{{projectName}}-infrastructure</module>\n" +
                                "        <module>{{projectName}}-api</module>\n" +
                                "        <module>{{projectName}}-app</module>\n" +
                                "    </modules>\n" +
                                "    <properties>\n" +
                                "        <java.version>17</java.version>\n" +
                                "    </properties>\n" +
                                "</project>\n"),
                        new TemplateFile("{{projectName}}-domain/pom.xml",
                                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                                "    <modelVersion>4.0.0</modelVersion>\n" +
                                "    <parent>\n" +
                                "        <groupId>com.{{author}}</groupId>\n" +
                                "        <artifactId>{{projectName}}</artifactId>\n" +
                                "        <version>0.0.1-SNAPSHOT</version>\n" +
                                "    </parent>\n" +
                                "    <artifactId>{{projectName}}-domain</artifactId>\n" +
                                "    <name>{{projectName}}-domain</name>\n" +
                                "    <description>Domain module - pure Java, zero framework dependencies</description>\n" +
                                "</project>\n"),
                        new TemplateFile("{{projectName}}-infrastructure/pom.xml",
                                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                                "    <modelVersion>4.0.0</modelVersion>\n" +
                                "    <parent>\n" +
                                "        <groupId>com.{{author}}</groupId>\n" +
                                "        <artifactId>{{projectName}}</artifactId>\n" +
                                "        <version>0.0.1-SNAPSHOT</version>\n" +
                                "    </parent>\n" +
                                "    <artifactId>{{projectName}}-infrastructure</artifactId>\n" +
                                "    <name>{{projectName}}-infrastructure</name>\n" +
                                "    <description>Infrastructure module - Spring-powered adapters</description>\n" +
                                "</project>\n"),
                        new TemplateFile("{{projectName}}-app/pom.xml",
                                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                                "    <modelVersion>4.0.0</modelVersion>\n" +
                                "    <parent>\n" +
                                "        <groupId>com.{{author}}</groupId>\n" +
                                "        <artifactId>{{projectName}}</artifactId>\n" +
                                "        <version>0.0.1-SNAPSHOT</version>\n" +
                                "    </parent>\n" +
                                "    <artifactId>{{projectName}}-app</artifactId>\n" +
                                "    <name>{{projectName}}-app</name>\n" +
                                "    <description>Application entry point</description>\n" +
                                "</project>\n")
                )
        ));

        // --- mcp-server ---
        TEMPLATES.put("mcp-server", new TemplateDefinition(
                "mcp-server",
                "MCP server con Spring AI, @Tool methods",
                List.of(
                        new TemplateFile("pom.xml",
                                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                                "    <modelVersion>4.0.0</modelVersion>\n" +
                                "    <parent>\n" +
                                "        <groupId>org.springframework.boot</groupId>\n" +
                                "        <artifactId>spring-boot-starter-parent</artifactId>\n" +
                                "        <version>3.4.2</version>\n" +
                                "    </parent>\n" +
                                "    <groupId>com.{{author}}</groupId>\n" +
                                "    <artifactId>{{projectName}}</artifactId>\n" +
                                "    <version>0.0.1-SNAPSHOT</version>\n" +
                                "    <name>{{projectName}}</name>\n" +
                                "    <description>{{description}}</description>\n" +
                                "    <properties>\n" +
                                "        <java.version>17</java.version>\n" +
                                "        <spring-ai.version>1.0.0</spring-ai.version>\n" +
                                "    </properties>\n" +
                                "    <dependencies>\n" +
                                "        <dependency>\n" +
                                "            <groupId>org.springframework.ai</groupId>\n" +
                                "            <artifactId>spring-ai-starter-model-ollama</artifactId>\n" +
                                "            <version>${spring-ai.version}</version>\n" +
                                "        </dependency>\n" +
                                "        <dependency>\n" +
                                "            <groupId>org.springframework.boot</groupId>\n" +
                                "            <artifactId>spring-boot-starter-web</artifactId>\n" +
                                "        </dependency>\n" +
                                "    </dependencies>\n" +
                                "</project>\n"),
                        new TemplateFile("src/main/java/com/{{projectName}}/McpServerApplication.java",
                                "package com.{{projectName}};\n\n" +
                                "import org.springframework.boot.SpringApplication;\n" +
                                "import org.springframework.boot.autoconfigure.SpringBootApplication;\n\n" +
                                "@SpringBootApplication\n" +
                                "public class McpServerApplication {\n\n" +
                                "    public static void main(String[] args) {\n" +
                                "        SpringApplication.run(McpServerApplication.class, args);\n" +
                                "    }\n" +
                                "}\n"),
                        new TemplateFile("src/main/java/com/{{projectName}}/tools/McpTools.java",
                                "package com.{{projectName}}.tools;\n\n" +
                                "import org.springframework.ai.tool.annotation.Tool;\n" +
                                "import org.springframework.ai.tool.annotation.ToolParam;\n" +
                                "import org.springframework.stereotype.Component;\n\n" +
                                "import java.util.HashMap;\n" +
                                "import java.util.Map;\n\n" +
                                "@Component\n" +
                                "public class McpTools {\n\n" +
                                "    @Tool(description = \"Example tool - returns a greeting\")\n" +
                                "    public Map<String, Object> greet(\n" +
                                "            @ToolParam(description = \"Name to greet\") String name) {\n" +
                                "        Map<String, Object> result = new HashMap<>();\n" +
                                "        result.put(\"message\", \"Hello, \" + name + \"!\");\n" +
                                "        return result;\n" +
                                "    }\n" +
                                "}\n"),
                        new TemplateFile("src/main/resources/application.yml",
                                "spring:\n" +
                                "  application:\n" +
                                "    name: {{projectName}}\n" +
                                "  ai:\n" +
                                "    ollama:\n" +
                                "      base-url: http://localhost:11434\n" +
                                "server:\n" +
                                "  port: 8080\n")
                )
        ));

        // --- react-app ---
        TEMPLATES.put("react-app", new TemplateDefinition(
                "react-app",
                "React con TypeScript e Vite",
                List.of(
                        new TemplateFile("package.json",
                                "{\n" +
                                "  \"name\": \"{{projectName}}\",\n" +
                                "  \"version\": \"0.0.1\",\n" +
                                "  \"description\": \"{{description}}\",\n" +
                                "  \"author\": \"{{author}}\",\n" +
                                "  \"license\": \"{{license}}\",\n" +
                                "  \"type\": \"module\",\n" +
                                "  \"scripts\": {\n" +
                                "    \"dev\": \"vite\",\n" +
                                "    \"build\": \"tsc && vite build\",\n" +
                                "    \"preview\": \"vite preview\"\n" +
                                "  },\n" +
                                "  \"dependencies\": {\n" +
                                "    \"react\": \"^19.0.0\",\n" +
                                "    \"react-dom\": \"^19.0.0\"\n" +
                                "  },\n" +
                                "  \"devDependencies\": {\n" +
                                "    \"@types/react\": \"^19.0.0\",\n" +
                                "    \"@types/react-dom\": \"^19.0.0\",\n" +
                                "    \"typescript\": \"^5.7.0\",\n" +
                                "    \"vite\": \"^6.0.0\",\n" +
                                "    \"@vitejs/plugin-react\": \"^4.3.0\"\n" +
                                "  }\n" +
                                "}\n"),
                        new TemplateFile("vite.config.ts",
                                "import { defineConfig } from 'vite';\n" +
                                "import react from '@vitejs/plugin-react';\n\n" +
                                "export default defineConfig({\n" +
                                "  plugins: [react()],\n" +
                                "});\n"),
                        new TemplateFile("src/App.tsx",
                                "import React from 'react';\n\n" +
                                "function App() {\n" +
                                "  return (\n" +
                                "    <div>\n" +
                                "      <h1>{{projectName}}</h1>\n" +
                                "      <p>{{description}}</p>\n" +
                                "    </div>\n" +
                                "  );\n" +
                                "}\n\n" +
                                "export default App;\n"),
                        new TemplateFile("src/main.tsx",
                                "import React from 'react';\n" +
                                "import ReactDOM from 'react-dom/client';\n" +
                                "import App from './App';\n\n" +
                                "ReactDOM.createRoot(document.getElementById('root')!).render(\n" +
                                "  <React.StrictMode>\n" +
                                "    <App />\n" +
                                "  </React.StrictMode>\n" +
                                ");\n")
                )
        ));
    }

    public ProjectScaffoldingService(ScaffoldingRepository scaffoldingRepository) {
        this.scaffoldingRepository = scaffoldingRepository;
    }

    @Override
    public List<Map<String, Object>> listProjectTemplates() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (TemplateDefinition template : TEMPLATES.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", template.name);
            map.put("description", template.description);
            List<String> fileNames = new ArrayList<>();
            for (TemplateFile file : template.files) {
                fileNames.add(file.path);
            }
            map.put("files", fileNames);
            result.add(map);
        }
        return result;
    }

    @Override
    public Map<String, Object> scaffoldProject(String template, String projectName, String outputDir,
                                                String author, String description, String license) {
        TemplateDefinition templateDef = TEMPLATES.get(template);
        if (templateDef == null) {
            throw new IllegalArgumentException("Template non trovato: " + template
                    + ". Template disponibili: " + String.join(", ", TEMPLATES.keySet()));
        }

        String effectiveLicense = (license != null && !license.isBlank()) ? license : "MIT";
        String effectiveDescription = (description != null && !description.isBlank())
                ? description
                : "A " + templateDef.description + " project";
        String effectiveAuthor = (author != null && !author.isBlank()) ? author : "unknown";

        List<Map<String, Object>> generatedFiles = new ArrayList<>();
        for (TemplateFile file : templateDef.files) {
            String resolvedPath = replacePlaceholders(file.path, projectName, effectiveAuthor,
                    effectiveDescription, effectiveLicense);
            String resolvedContent = replacePlaceholders(file.content, projectName, effectiveAuthor,
                    effectiveDescription, effectiveLicense);

            Map<String, Object> fileMap = new HashMap<>();
            fileMap.put("path", resolvedPath);
            fileMap.put("content", resolvedContent);
            generatedFiles.add(fileMap);
        }

        // Salva il record nel repository
        ScaffoldedProject project = ScaffoldedProject.builder()
                .id(UUID.randomUUID().toString())
                .templateName(template)
                .projectName(projectName)
                .outputPath(outputDir != null ? outputDir + "/" + projectName : projectName)
                .filesGenerated(generatedFiles.size())
                .createdAt(Instant.now())
                .build();
        scaffoldingRepository.save(project);

        Map<String, Object> result = new HashMap<>();
        result.put("template", template);
        result.put("projectName", projectName);
        result.put("files", generatedFiles);
        result.put("totalFiles", generatedFiles.size());
        return result;
    }

    @Override
    public Map<String, Object> scaffoldComponent(String name, String type, String language,
                                                   String outputDir) {
        validateComponentType(type);
        validateLanguage(language);

        String fileName;
        String code;

        if ("java".equalsIgnoreCase(language)) {
            fileName = generateJavaFileName(name, type);
            code = generateJavaCode(name, type);
        } else if ("typescript".equalsIgnoreCase(language)) {
            fileName = generateTsFileName(name, type);
            code = generateTsCode(name, type);
        } else {
            // javascript
            fileName = generateJsFileName(name, type);
            code = generateJsCode(name, type);
        }

        // Salva il record nel repository
        ScaffoldedProject project = ScaffoldedProject.builder()
                .id(UUID.randomUUID().toString())
                .templateName("component-" + type)
                .projectName(name)
                .outputPath(outputDir != null ? outputDir + "/" + fileName : fileName)
                .filesGenerated(1)
                .createdAt(Instant.now())
                .build();
        scaffoldingRepository.save(project);

        Map<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("type", type);
        result.put("language", language.toLowerCase());
        result.put("fileName", fileName);
        result.put("code", code);
        return result;
    }

    // --- Private helper methods ---

    private String replacePlaceholders(String text, String projectName, String author,
                                        String description, String license) {
        return text
                .replace("{{projectName}}", projectName)
                .replace("{{author}}", author)
                .replace("{{description}}", description)
                .replace("{{license}}", license);
    }

    private void validateComponentType(String type) {
        List<String> validTypes = List.of("component", "service", "controller", "model");
        if (type == null || !validTypes.contains(type.toLowerCase())) {
            throw new IllegalArgumentException("Tipo componente non valido: " + type
                    + ". Tipi supportati: " + String.join(", ", validTypes));
        }
    }

    private void validateLanguage(String language) {
        List<String> validLanguages = List.of("java", "typescript", "javascript");
        if (language == null || !validLanguages.contains(language.toLowerCase())) {
            throw new IllegalArgumentException("Linguaggio non valido: " + language
                    + ". Linguaggi supportati: " + String.join(", ", validLanguages));
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    // --- Java generators ---

    private String generateJavaFileName(String name, String type) {
        String capitalized = capitalize(name);
        switch (type.toLowerCase()) {
            case "service":
                return capitalized + "Service.java";
            case "controller":
                return capitalized + "Controller.java";
            case "model":
                return capitalized + ".java";
            case "component":
                return capitalized + "Component.java";
            default:
                return capitalized + ".java";
        }
    }

    private String generateJavaCode(String name, String type) {
        String capitalized = capitalize(name);
        switch (type.toLowerCase()) {
            case "service":
                return generateJavaService(capitalized);
            case "controller":
                return generateJavaController(capitalized);
            case "model":
                return generateJavaModel(capitalized);
            case "component":
                return generateJavaService(capitalized + "Component");
            default:
                return "";
        }
    }

    private String generateJavaService(String name) {
        return "import java.util.List;\n" +
                "import java.util.Optional;\n\n" +
                "public class " + name + "Service {\n\n" +
                "    public List<Object> findAll() {\n" +
                "        // TODO: implement\n" +
                "        return List.of();\n" +
                "    }\n\n" +
                "    public Optional<Object> findById(String id) {\n" +
                "        // TODO: implement\n" +
                "        return Optional.empty();\n" +
                "    }\n\n" +
                "    public Object create(Object entity) {\n" +
                "        // TODO: implement\n" +
                "        return entity;\n" +
                "    }\n\n" +
                "    public Object update(String id, Object entity) {\n" +
                "        // TODO: implement\n" +
                "        return entity;\n" +
                "    }\n\n" +
                "    public boolean delete(String id) {\n" +
                "        // TODO: implement\n" +
                "        return false;\n" +
                "    }\n" +
                "}\n";
    }

    private String generateJavaController(String name) {
        return "import org.springframework.web.bind.annotation.*;\n" +
                "import java.util.List;\n\n" +
                "@RestController\n" +
                "@RequestMapping(\"/api/v1/" + name.toLowerCase() + "\")\n" +
                "public class " + name + "Controller {\n\n" +
                "    @GetMapping\n" +
                "    public List<Object> findAll() {\n" +
                "        // TODO: implement\n" +
                "        return List.of();\n" +
                "    }\n\n" +
                "    @GetMapping(\"/{id}\")\n" +
                "    public Object findById(@PathVariable String id) {\n" +
                "        // TODO: implement\n" +
                "        return null;\n" +
                "    }\n\n" +
                "    @PostMapping\n" +
                "    public Object create(@RequestBody Object entity) {\n" +
                "        // TODO: implement\n" +
                "        return entity;\n" +
                "    }\n\n" +
                "    @PutMapping(\"/{id}\")\n" +
                "    public Object update(@PathVariable String id, @RequestBody Object entity) {\n" +
                "        // TODO: implement\n" +
                "        return entity;\n" +
                "    }\n\n" +
                "    @DeleteMapping(\"/{id}\")\n" +
                "    public void delete(@PathVariable String id) {\n" +
                "        // TODO: implement\n" +
                "    }\n" +
                "}\n";
    }

    private String generateJavaModel(String name) {
        return "import java.time.Instant;\n" +
                "import java.util.UUID;\n\n" +
                "public class " + name + " {\n\n" +
                "    private final String id;\n" +
                "    private final Instant createdAt;\n" +
                "    private final Instant updatedAt;\n\n" +
                "    private " + name + "(Builder builder) {\n" +
                "        this.id = builder.id;\n" +
                "        this.createdAt = builder.createdAt;\n" +
                "        this.updatedAt = builder.updatedAt;\n" +
                "    }\n\n" +
                "    public String getId() { return id; }\n" +
                "    public Instant getCreatedAt() { return createdAt; }\n" +
                "    public Instant getUpdatedAt() { return updatedAt; }\n\n" +
                "    public static Builder builder() { return new Builder(); }\n\n" +
                "    public static class Builder {\n" +
                "        private String id = UUID.randomUUID().toString();\n" +
                "        private Instant createdAt = Instant.now();\n" +
                "        private Instant updatedAt = Instant.now();\n\n" +
                "        public Builder id(String id) { this.id = id; return this; }\n" +
                "        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }\n" +
                "        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }\n\n" +
                "        public " + name + " build() { return new " + name + "(this); }\n" +
                "    }\n" +
                "}\n";
    }

    // --- TypeScript generators ---

    private String generateTsFileName(String name, String type) {
        String kebab = toKebabCase(name);
        switch (type.toLowerCase()) {
            case "component":
                return kebab + ".component.tsx";
            case "service":
                return kebab + ".service.ts";
            case "controller":
                return kebab + ".controller.ts";
            case "model":
                return kebab + ".model.ts";
            default:
                return kebab + ".ts";
        }
    }

    private String generateTsCode(String name, String type) {
        String capitalized = capitalize(name);
        switch (type.toLowerCase()) {
            case "component":
                return generateTsComponent(capitalized);
            case "service":
                return generateTsService(capitalized);
            case "controller":
                return generateTsController(capitalized);
            case "model":
                return generateTsModel(capitalized);
            default:
                return "";
        }
    }

    private String generateTsComponent(String name) {
        return "import React from 'react';\n\n" +
                "interface " + name + "Props {\n" +
                "  title?: string;\n" +
                "}\n\n" +
                "const " + name + ": React.FC<" + name + "Props> = ({ title = '" + name + "' }) => {\n" +
                "  return (\n" +
                "    <div className=\"" + toKebabCase(name) + "\">\n" +
                "      <h2>{title}</h2>\n" +
                "    </div>\n" +
                "  );\n" +
                "};\n\n" +
                "export default " + name + ";\n";
    }

    private String generateTsService(String name) {
        return "export class " + name + "Service {\n" +
                "  private baseUrl: string;\n\n" +
                "  constructor(baseUrl: string = '/api/v1/" + name.toLowerCase() + "') {\n" +
                "    this.baseUrl = baseUrl;\n" +
                "  }\n\n" +
                "  async findAll(): Promise<any[]> {\n" +
                "    const response = await fetch(this.baseUrl);\n" +
                "    return response.json();\n" +
                "  }\n\n" +
                "  async findById(id: string): Promise<any> {\n" +
                "    const response = await fetch(`${this.baseUrl}/${id}`);\n" +
                "    return response.json();\n" +
                "  }\n\n" +
                "  async create(data: any): Promise<any> {\n" +
                "    const response = await fetch(this.baseUrl, {\n" +
                "      method: 'POST',\n" +
                "      headers: { 'Content-Type': 'application/json' },\n" +
                "      body: JSON.stringify(data),\n" +
                "    });\n" +
                "    return response.json();\n" +
                "  }\n\n" +
                "  async update(id: string, data: any): Promise<any> {\n" +
                "    const response = await fetch(`${this.baseUrl}/${id}`, {\n" +
                "      method: 'PUT',\n" +
                "      headers: { 'Content-Type': 'application/json' },\n" +
                "      body: JSON.stringify(data),\n" +
                "    });\n" +
                "    return response.json();\n" +
                "  }\n\n" +
                "  async delete(id: string): Promise<void> {\n" +
                "    await fetch(`${this.baseUrl}/${id}`, { method: 'DELETE' });\n" +
                "  }\n" +
                "}\n";
    }

    private String generateTsController(String name) {
        return "import { Request, Response, Router } from 'express';\n\n" +
                "const router = Router();\n\n" +
                "router.get('/', async (req: Request, res: Response) => {\n" +
                "  // TODO: implement findAll\n" +
                "  res.json([]);\n" +
                "});\n\n" +
                "router.get('/:id', async (req: Request, res: Response) => {\n" +
                "  // TODO: implement findById\n" +
                "  res.json({ id: req.params.id });\n" +
                "});\n\n" +
                "router.post('/', async (req: Request, res: Response) => {\n" +
                "  // TODO: implement create\n" +
                "  res.status(201).json(req.body);\n" +
                "});\n\n" +
                "router.put('/:id', async (req: Request, res: Response) => {\n" +
                "  // TODO: implement update\n" +
                "  res.json({ id: req.params.id, ...req.body });\n" +
                "});\n\n" +
                "router.delete('/:id', async (req: Request, res: Response) => {\n" +
                "  // TODO: implement delete\n" +
                "  res.status(204).send();\n" +
                "});\n\n" +
                "export const " + name.toLowerCase() + "Controller = router;\n";
    }

    private String generateTsModel(String name) {
        return "export interface " + name + " {\n" +
                "  id: string;\n" +
                "  createdAt: string;\n" +
                "  updatedAt: string;\n" +
                "}\n\n" +
                "export function create" + name + "(partial: Partial<" + name + "> = {}): " + name + " {\n" +
                "  return {\n" +
                "    id: partial.id ?? crypto.randomUUID(),\n" +
                "    createdAt: partial.createdAt ?? new Date().toISOString(),\n" +
                "    updatedAt: partial.updatedAt ?? new Date().toISOString(),\n" +
                "  };\n" +
                "}\n\n" +
                "export function is" + name + "(obj: unknown): obj is " + name + " {\n" +
                "  return (\n" +
                "    typeof obj === 'object' &&\n" +
                "    obj !== null &&\n" +
                "    'id' in obj &&\n" +
                "    'createdAt' in obj &&\n" +
                "    'updatedAt' in obj\n" +
                "  );\n" +
                "}\n";
    }

    // --- JavaScript generators ---

    private String generateJsFileName(String name, String type) {
        String kebab = toKebabCase(name);
        switch (type.toLowerCase()) {
            case "component":
                return kebab + ".component.jsx";
            case "service":
                return kebab + ".service.js";
            case "controller":
                return kebab + ".controller.js";
            case "model":
                return kebab + ".model.js";
            default:
                return kebab + ".js";
        }
    }

    private String generateJsCode(String name, String type) {
        String capitalized = capitalize(name);
        switch (type.toLowerCase()) {
            case "component":
                return generateJsComponent(capitalized);
            case "service":
                return generateJsService(capitalized);
            case "controller":
                return generateJsController(capitalized);
            case "model":
                return generateJsModel(capitalized);
            default:
                return "";
        }
    }

    private String generateJsComponent(String name) {
        return "import React from 'react';\n\n" +
                "const " + name + " = ({ title = '" + name + "' }) => {\n" +
                "  return (\n" +
                "    <div className=\"" + toKebabCase(name) + "\">\n" +
                "      <h2>{title}</h2>\n" +
                "    </div>\n" +
                "  );\n" +
                "};\n\n" +
                "export default " + name + ";\n";
    }

    private String generateJsService(String name) {
        return "export class " + name + "Service {\n" +
                "  constructor(baseUrl = '/api/v1/" + name.toLowerCase() + "') {\n" +
                "    this.baseUrl = baseUrl;\n" +
                "  }\n\n" +
                "  async findAll() {\n" +
                "    const response = await fetch(this.baseUrl);\n" +
                "    return response.json();\n" +
                "  }\n\n" +
                "  async findById(id) {\n" +
                "    const response = await fetch(`${this.baseUrl}/${id}`);\n" +
                "    return response.json();\n" +
                "  }\n\n" +
                "  async create(data) {\n" +
                "    const response = await fetch(this.baseUrl, {\n" +
                "      method: 'POST',\n" +
                "      headers: { 'Content-Type': 'application/json' },\n" +
                "      body: JSON.stringify(data),\n" +
                "    });\n" +
                "    return response.json();\n" +
                "  }\n\n" +
                "  async update(id, data) {\n" +
                "    const response = await fetch(`${this.baseUrl}/${id}`, {\n" +
                "      method: 'PUT',\n" +
                "      headers: { 'Content-Type': 'application/json' },\n" +
                "      body: JSON.stringify(data),\n" +
                "    });\n" +
                "    return response.json();\n" +
                "  }\n\n" +
                "  async delete(id) {\n" +
                "    await fetch(`${this.baseUrl}/${id}`, { method: 'DELETE' });\n" +
                "  }\n" +
                "}\n";
    }

    private String generateJsController(String name) {
        return "const { Router } = require('express');\n\n" +
                "const router = Router();\n\n" +
                "router.get('/', async (req, res) => {\n" +
                "  // TODO: implement findAll\n" +
                "  res.json([]);\n" +
                "});\n\n" +
                "router.get('/:id', async (req, res) => {\n" +
                "  // TODO: implement findById\n" +
                "  res.json({ id: req.params.id });\n" +
                "});\n\n" +
                "router.post('/', async (req, res) => {\n" +
                "  // TODO: implement create\n" +
                "  res.status(201).json(req.body);\n" +
                "});\n\n" +
                "router.put('/:id', async (req, res) => {\n" +
                "  // TODO: implement update\n" +
                "  res.json({ id: req.params.id, ...req.body });\n" +
                "});\n\n" +
                "router.delete('/:id', async (req, res) => {\n" +
                "  // TODO: implement delete\n" +
                "  res.status(204).send();\n" +
                "});\n\n" +
                "module.exports = { " + name.toLowerCase() + "Controller: router };\n";
    }

    private String generateJsModel(String name) {
        return "/**\n" +
                " * Factory function to create a " + name + " object.\n" +
                " * @param {Partial<" + name + ">} partial\n" +
                " * @returns {" + name + "}\n" +
                " */\n" +
                "function create" + name + "(partial = {}) {\n" +
                "  return {\n" +
                "    id: partial.id ?? crypto.randomUUID(),\n" +
                "    createdAt: partial.createdAt ?? new Date().toISOString(),\n" +
                "    updatedAt: partial.updatedAt ?? new Date().toISOString(),\n" +
                "  };\n" +
                "}\n\n" +
                "/**\n" +
                " * Type guard for " + name + ".\n" +
                " * @param {*} obj\n" +
                " * @returns {boolean}\n" +
                " */\n" +
                "function is" + name + "(obj) {\n" +
                "  return (\n" +
                "    typeof obj === 'object' &&\n" +
                "    obj !== null &&\n" +
                "    'id' in obj &&\n" +
                "    'createdAt' in obj &&\n" +
                "    'updatedAt' in obj\n" +
                "  );\n" +
                "}\n\n" +
                "module.exports = { create" + name + ", is" + name + " };\n";
    }

    private String toKebabCase(String s) {
        if (s == null || s.isEmpty()) return s;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) sb.append('-');
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // --- Inner classes for template definitions ---

    static class TemplateDefinition {
        final String name;
        final String description;
        final List<TemplateFile> files;

        TemplateDefinition(String name, String description, List<TemplateFile> files) {
            this.name = name;
            this.description = description;
            this.files = files;
        }
    }

    static class TemplateFile {
        final String path;
        final String content;

        TemplateFile(String path, String content) {
            this.path = path;
            this.content = content;
        }
    }
}
