package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.ComposeAnalysis;
import com.localmind.domain.mcp.port.in.DockerAnalysisUseCase;
import com.localmind.domain.mcp.port.out.DockerAnalysisRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Domain service implementing the docker-compose tool group.
 * Provides docker-compose parsing, Dockerfile analysis, common service listing and compose generation.
 * No Spring annotations - registered as a bean via DomainConfig.
 */
public class DockerAnalysisService implements DockerAnalysisUseCase {

    private final DockerAnalysisRepository repository;

    /**
     * Large base images that should be flagged.
     */
    private static final Set<String> LARGE_BASE_IMAGES = Set.of(
            "ubuntu", "debian", "centos", "fedora", "amazonlinux"
    );

    /**
     * Static list of common Docker services.
     */
    private static final List<Map<String, Object>> COMMON_SERVICES;

    static {
        COMMON_SERVICES = new ArrayList<>();
        COMMON_SERVICES.add(buildServiceInfo("MySQL", "mysql:8.0", List.of("3306:3306"),
                "Relational database management system"));
        COMMON_SERVICES.add(buildServiceInfo("PostgreSQL", "postgres:16", List.of("5432:5432"),
                "Advanced open-source relational database"));
        COMMON_SERVICES.add(buildServiceInfo("Redis", "redis:7", List.of("6379:6379"),
                "In-memory data structure store, cache and message broker"));
        COMMON_SERVICES.add(buildServiceInfo("MongoDB", "mongo:7", List.of("27017:27017"),
                "Document-oriented NoSQL database"));
        COMMON_SERVICES.add(buildServiceInfo("Elasticsearch", "docker.elastic.co/elasticsearch/elasticsearch:8.12.0",
                List.of("9200:9200", "9300:9300"), "Distributed search and analytics engine"));
        COMMON_SERVICES.add(buildServiceInfo("RabbitMQ", "rabbitmq:3-management",
                List.of("5672:5672", "15672:15672"), "Message broker with management UI"));
        COMMON_SERVICES.add(buildServiceInfo("Nginx", "nginx:latest", List.of("80:80", "443:443"),
                "High-performance HTTP server and reverse proxy"));
        COMMON_SERVICES.add(buildServiceInfo("Apache", "httpd:2.4", List.of("80:80"),
                "Apache HTTP Server"));
        COMMON_SERVICES.add(buildServiceInfo("Grafana", "grafana/grafana:latest", List.of("3000:3000"),
                "Open-source analytics and monitoring platform"));
        COMMON_SERVICES.add(buildServiceInfo("Prometheus", "prom/prometheus:latest", List.of("9090:9090"),
                "Open-source monitoring and alerting toolkit"));
        COMMON_SERVICES.add(buildServiceInfo("Kafka", "confluentinc/cp-kafka:7.5.0",
                List.of("9092:9092"), "Distributed event streaming platform"));
        COMMON_SERVICES.add(buildServiceInfo("Zookeeper", "confluentinc/cp-zookeeper:7.5.0",
                List.of("2181:2181"), "Centralized service for maintaining configuration information"));
        COMMON_SERVICES.add(buildServiceInfo("MinIO", "minio/minio:latest",
                List.of("9000:9000", "9001:9001"), "High-performance S3-compatible object storage"));
        COMMON_SERVICES.add(buildServiceInfo("Qdrant", "qdrant/qdrant:latest",
                List.of("6333:6333", "6334:6334"), "Vector similarity search engine"));
        COMMON_SERVICES.add(buildServiceInfo("Adminer", "adminer:latest", List.of("8080:8080"),
                "Database management tool in a single PHP file"));
    }

    public DockerAnalysisService(DockerAnalysisRepository repository) {
        this.repository = repository;
    }

    // ========== 1. parseCompose ==========

    @Override
    public Map<String, Object> parseCompose(String content) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (content == null || content.isBlank()) {
            result.put("error", "Content is empty or null");
            return result;
        }

        List<Map<String, Object>> services = new ArrayList<>();
        List<String> networks = new ArrayList<>();
        List<String> volumes = new ArrayList<>();
        List<Map<String, Object>> validationIssues = new ArrayList<>();

        String[] lines = content.split("\n");

        // Check for deprecated 'version' key
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("version:")) {
                validationIssues.add(buildIssue("warning",
                        "Deprecated 'version' key found. Docker Compose V2 ignores this field."));
                break;
            }
        }

        // Parse top-level sections
        String currentTopSection = null;
        String currentService = null;
        String currentServiceKey = null;
        Map<String, Object> currentServiceData = null;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmed = line.trim();

            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }

            int indent = getIndentLevel(line);

            // Top-level section (indent 0)
            if (indent == 0 && trimmed.endsWith(":")) {
                String sectionName = trimmed.substring(0, trimmed.length() - 1).trim();
                if ("services".equals(sectionName)) {
                    currentTopSection = "services";
                } else if ("networks".equals(sectionName)) {
                    currentTopSection = "networks";
                } else if ("volumes".equals(sectionName)) {
                    currentTopSection = "volumes";
                } else {
                    currentTopSection = sectionName;
                }
                currentService = null;
                currentServiceData = null;
                currentServiceKey = null;
                continue;
            }

            // Inside services section
            if ("services".equals(currentTopSection)) {
                if (indent == 1 && trimmed.endsWith(":")) {
                    // New service name
                    if (currentServiceData != null) {
                        services.add(currentServiceData);
                    }
                    currentService = trimmed.substring(0, trimmed.length() - 1).trim();
                    currentServiceData = new LinkedHashMap<>();
                    currentServiceData.put("name", currentService);
                    currentServiceData.put("ports", new ArrayList<String>());
                    currentServiceData.put("environment", new LinkedHashMap<String, String>());
                    currentServiceData.put("volumes", new ArrayList<String>());
                    currentServiceKey = null;
                    continue;
                }

                if (currentServiceData != null && indent >= 2) {
                    parseServiceProperty(trimmed, currentServiceData, validationIssues);
                }
            }

            // Inside networks section
            if ("networks".equals(currentTopSection) && indent == 1) {
                String netName = trimmed.endsWith(":") ? trimmed.substring(0, trimmed.length() - 1).trim() : trimmed;
                if (!netName.isEmpty()) {
                    networks.add(netName);
                }
            }

            // Inside volumes section
            if ("volumes".equals(currentTopSection) && indent == 1) {
                String volName = trimmed.endsWith(":") ? trimmed.substring(0, trimmed.length() - 1).trim() : trimmed;
                if (!volName.isEmpty()) {
                    volumes.add(volName);
                }
            }
        }

        // Add last service
        if (currentServiceData != null) {
            services.add(currentServiceData);
        }

        // Validate each service
        for (Map<String, Object> svc : services) {
            String image = (String) svc.get("image");
            String build = (String) svc.get("build");
            Boolean privileged = (Boolean) svc.get("privileged");
            String networkMode = (String) svc.get("network_mode");

            if (image == null && build == null) {
                validationIssues.add(buildIssue("error",
                        "Service '" + svc.get("name") + "' has no 'image' or 'build' directive."));
            }

            if (image != null && image.endsWith(":latest")) {
                validationIssues.add(buildIssue("warning",
                        "Service '" + svc.get("name") + "' uses ':latest' tag. Pin to a specific version."));
            }

            if (Boolean.TRUE.equals(privileged)) {
                validationIssues.add(buildIssue("error",
                        "Service '" + svc.get("name") + "' runs in privileged mode. This is a security risk."));
            }

            if ("host".equals(networkMode)) {
                validationIssues.add(buildIssue("warning",
                        "Service '" + svc.get("name") + "' uses network_mode:host. Consider using bridge networking."));
            }
        }

        result.put("serviceCount", services.size());
        result.put("services", services);
        result.put("networks", networks);
        result.put("volumes", volumes);
        result.put("validationIssues", validationIssues);

        // Persist
        ComposeAnalysis analysis = ComposeAnalysis.builder()
                .id(UUID.randomUUID().toString())
                .contentType("docker-compose")
                .serviceCount(services.size())
                .resultJson(mapToJson(result))
                .createdAt(Instant.now())
                .build();
        repository.save(analysis);

        return result;
    }

    // ========== 2. analyzeDockerfile ==========

    @Override
    public Map<String, Object> analyzeDockerfile(String content) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (content == null || content.isBlank()) {
            result.put("error", "Content is empty or null");
            return result;
        }

        String[] lines = content.split("\n");
        List<Map<String, Object>> issues = new ArrayList<>();
        String baseImage = null;
        int stages = 0;
        int totalInstructions = 0;
        boolean hasHealthcheck = false;
        boolean hasUser = false;
        String previousInstruction = null;
        int consecutiveRunCount = 0;

        for (int lineNum = 0; lineNum < lines.length; lineNum++) {
            String line = lines[lineNum].trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            // Handle line continuations
            while (line.endsWith("\\") && lineNum + 1 < lines.length) {
                lineNum++;
                line = line.substring(0, line.length() - 1) + " " + lines[lineNum].trim();
            }

            String instruction = extractInstruction(line);
            if (instruction == null) {
                continue;
            }

            totalInstructions++;

            switch (instruction) {
                case "FROM":
                    stages++;
                    String fromImage = extractFromImage(line);
                    if (baseImage == null) {
                        baseImage = fromImage;
                    }

                    if (fromImage != null && fromImage.endsWith(":latest")) {
                        issues.add(buildDockerIssue(lineNum + 1, "warning", "latest-tag",
                                "FROM uses ':latest' tag: " + fromImage,
                                "Pin to a specific version for reproducible builds."));
                    }

                    if (fromImage != null && !fromImage.contains(":")) {
                        issues.add(buildDockerIssue(lineNum + 1, "warning", "no-tag",
                                "FROM uses no tag (implies :latest): " + fromImage,
                                "Specify an explicit version tag."));
                    }

                    // Check for large base images
                    if (fromImage != null) {
                        String imageName = fromImage.split(":")[0].toLowerCase();
                        if (LARGE_BASE_IMAGES.contains(imageName)) {
                            issues.add(buildDockerIssue(lineNum + 1, "info", "large-base-image",
                                    "Large base image detected: " + fromImage,
                                    "Consider using Alpine or distroless variants for smaller image size."));
                        }
                    }

                    consecutiveRunCount = 0;
                    previousInstruction = "FROM";
                    break;

                case "RUN":
                    consecutiveRunCount++;
                    if (consecutiveRunCount > 1) {
                        issues.add(buildDockerIssue(lineNum + 1, "warning", "consecutive-run",
                                "Consecutive RUN instructions detected (" + consecutiveRunCount + " in a row).",
                                "Combine RUN instructions with '&&' to reduce image layers."));
                    }

                    // Check apt-get without --no-install-recommends
                    String runContent = line.substring(line.indexOf(' ') + 1).trim();
                    if (runContent.contains("apt-get install") && !runContent.contains("--no-install-recommends")) {
                        issues.add(buildDockerIssue(lineNum + 1, "warning", "apt-no-recommends",
                                "apt-get install without --no-install-recommends.",
                                "Add --no-install-recommends to reduce image size."));
                    }

                    previousInstruction = "RUN";
                    break;

                case "ADD":
                    issues.add(buildDockerIssue(lineNum + 1, "info", "add-vs-copy",
                            "ADD instruction used.",
                            "Prefer COPY unless you need ADD's tar extraction or URL features."));
                    consecutiveRunCount = 0;
                    previousInstruction = "ADD";
                    break;

                case "COPY":
                    consecutiveRunCount = 0;
                    previousInstruction = "COPY";
                    break;

                case "HEALTHCHECK":
                    hasHealthcheck = true;
                    consecutiveRunCount = 0;
                    previousInstruction = "HEALTHCHECK";
                    break;

                case "USER":
                    hasUser = true;
                    consecutiveRunCount = 0;
                    previousInstruction = "USER";
                    break;

                default:
                    if (!"RUN".equals(instruction)) {
                        consecutiveRunCount = 0;
                    }
                    previousInstruction = instruction;
                    break;
            }
        }

        // Global checks
        if (!hasHealthcheck) {
            issues.add(buildDockerIssue(0, "warning", "no-healthcheck",
                    "No HEALTHCHECK instruction found.",
                    "Add a HEALTHCHECK instruction so Docker can detect container health."));
        }

        if (!hasUser) {
            issues.add(buildDockerIssue(0, "warning", "no-user",
                    "No USER instruction found. Container runs as root by default.",
                    "Add a USER instruction to run the container as a non-root user."));
        }

        // Build summary
        int errors = 0, warnings = 0, infos = 0;
        for (Map<String, Object> issue : issues) {
            String severity = (String) issue.get("severity");
            switch (severity) {
                case "error" -> errors++;
                case "warning" -> warnings++;
                case "info" -> infos++;
            }
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("errors", errors);
        summary.put("warnings", warnings);
        summary.put("info", infos);

        result.put("baseImage", baseImage);
        result.put("stages", stages);
        result.put("totalInstructions", totalInstructions);
        result.put("issues", issues);
        result.put("summary", summary);

        // Persist
        ComposeAnalysis analysis = ComposeAnalysis.builder()
                .id(UUID.randomUUID().toString())
                .contentType("dockerfile")
                .serviceCount(stages)
                .resultJson(mapToJson(result))
                .createdAt(Instant.now())
                .build();
        repository.save(analysis);

        return result;
    }

    // ========== 3. listDockerServices ==========

    @Override
    public Map<String, Object> listDockerServices() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("serviceCount", COMMON_SERVICES.size());
        result.put("services", COMMON_SERVICES);
        return result;
    }

    // ========== 4. generateCompose ==========

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> generateCompose(List<Map<String, Object>> services) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (services == null || services.isEmpty()) {
            result.put("error", "No services provided");
            result.put("yaml", "");
            result.put("serviceCount", 0);
            return result;
        }

        StringBuilder yaml = new StringBuilder();
        List<String> namedVolumes = new ArrayList<>();

        yaml.append("services:\n");

        for (Map<String, Object> svc : services) {
            String name = (String) svc.get("name");
            String image = (String) svc.get("image");
            List<String> ports = svc.get("ports") instanceof List ? (List<String>) svc.get("ports") : new ArrayList<>();
            Map<String, String> environment = svc.get("environment") instanceof Map
                    ? (Map<String, String>) svc.get("environment") : new LinkedHashMap<>();
            List<String> svcVolumes = svc.get("volumes") instanceof List
                    ? (List<String>) svc.get("volumes") : new ArrayList<>();

            if (name == null || name.isBlank()) {
                continue;
            }

            yaml.append("  ").append(name).append(":\n");

            if (image != null && !image.isBlank()) {
                yaml.append("    image: ").append(image).append("\n");
            }

            yaml.append("    restart: unless-stopped\n");

            if (!ports.isEmpty()) {
                yaml.append("    ports:\n");
                for (String port : ports) {
                    yaml.append("      - \"").append(port).append("\"\n");
                }
            }

            if (!environment.isEmpty()) {
                yaml.append("    environment:\n");
                for (Map.Entry<String, String> env : environment.entrySet()) {
                    yaml.append("      ").append(env.getKey()).append(": ").append(env.getValue()).append("\n");
                }
            }

            if (!svcVolumes.isEmpty()) {
                yaml.append("    volumes:\n");
                for (String vol : svcVolumes) {
                    yaml.append("      - ").append(vol).append("\n");
                    // Collect named volumes (those that don't start with . or /)
                    String volumeName = vol.split(":")[0].trim();
                    if (!volumeName.startsWith(".") && !volumeName.startsWith("/") && volumeName.contains("_")) {
                        if (!namedVolumes.contains(volumeName)) {
                            namedVolumes.add(volumeName);
                        }
                    }
                }
            }

            yaml.append("\n");
        }

        // Append named volumes section if any
        if (!namedVolumes.isEmpty()) {
            yaml.append("volumes:\n");
            for (String vol : namedVolumes) {
                yaml.append("  ").append(vol).append(":\n");
            }
        }

        String yamlStr = yaml.toString();
        result.put("yaml", yamlStr);
        result.put("serviceCount", services.size());

        // Persist
        ComposeAnalysis analysis = ComposeAnalysis.builder()
                .id(UUID.randomUUID().toString())
                .contentType("generated-compose")
                .serviceCount(services.size())
                .resultJson(mapToJson(result))
                .createdAt(Instant.now())
                .build();
        repository.save(analysis);

        return result;
    }

    // ========== YAML Parsing Helpers ==========

    /**
     * Gets the indentation level of a line (number of 2-space indents).
     */
    int getIndentLevel(String line) {
        int spaces = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == ' ') {
                spaces++;
            } else if (line.charAt(i) == '\t') {
                spaces += 2;
            } else {
                break;
            }
        }
        return spaces / 2;
    }

    /**
     * Parses a service-level property from a docker-compose YAML line.
     */
    @SuppressWarnings("unchecked")
    void parseServiceProperty(String trimmed, Map<String, Object> serviceData,
                              List<Map<String, Object>> validationIssues) {
        // List item (prefixed with -)
        if (trimmed.startsWith("- ")) {
            String value = trimmed.substring(2).trim().replace("\"", "").replace("'", "");
            // Determine which list this belongs to based on context
            // We look at last key set on the service
            Object lastPorts = serviceData.get("ports");
            Object lastVolumes = serviceData.get("volumes");

            // If value looks like a port mapping (contains :), add to ports
            if (value.matches("\\d+:\\d+.*")) {
                if (lastPorts instanceof List) {
                    ((List<String>) lastPorts).add(value);
                }
            } else if (value.contains(":") && (value.contains("/") || value.contains("_"))) {
                // Looks like a volume mapping
                if (lastVolumes instanceof List) {
                    ((List<String>) lastVolumes).add(value);
                }
            }
            return;
        }

        // Key-value pair
        int colonIdx = trimmed.indexOf(':');
        if (colonIdx > 0) {
            String key = trimmed.substring(0, colonIdx).trim();
            String value = trimmed.substring(colonIdx + 1).trim().replace("\"", "").replace("'", "");

            switch (key) {
                case "image":
                    serviceData.put("image", value);
                    break;
                case "build":
                    serviceData.put("build", value);
                    break;
                case "restart":
                    serviceData.put("restart", value);
                    break;
                case "privileged":
                    serviceData.put("privileged", "true".equalsIgnoreCase(value));
                    break;
                case "network_mode":
                    serviceData.put("network_mode", value);
                    break;
                case "container_name":
                    serviceData.put("container_name", value);
                    break;
                case "ports":
                case "volumes":
                case "environment":
                    // These are section headers; content comes in subsequent lines
                    break;
                default:
                    // Could be an environment variable
                    if (!value.isEmpty()) {
                        Object env = serviceData.get("environment");
                        if (env instanceof Map) {
                            ((Map<String, String>) env).put(key, value);
                        }
                    }
                    break;
            }
        }
    }

    // ========== Dockerfile Parsing Helpers ==========

    /**
     * Extracts the instruction keyword from a Dockerfile line.
     */
    String extractInstruction(String line) {
        if (line == null || line.isEmpty()) {
            return null;
        }
        String[] parts = line.split("\\s+", 2);
        String instruction = parts[0].toUpperCase();
        // Valid Dockerfile instructions
        Set<String> validInstructions = Set.of(
                "FROM", "RUN", "CMD", "LABEL", "EXPOSE", "ENV", "ADD", "COPY",
                "ENTRYPOINT", "VOLUME", "USER", "WORKDIR", "ARG", "ONBUILD",
                "STOPSIGNAL", "HEALTHCHECK", "SHELL"
        );
        return validInstructions.contains(instruction) ? instruction : null;
    }

    /**
     * Extracts the image name from a FROM instruction.
     */
    String extractFromImage(String line) {
        String[] parts = line.split("\\s+");
        if (parts.length >= 2) {
            String image = parts[1];
            // Handle "FROM image AS stage"
            return image;
        }
        return null;
    }

    // ========== Issue Builders ==========

    private Map<String, Object> buildIssue(String severity, String message) {
        Map<String, Object> issue = new LinkedHashMap<>();
        issue.put("severity", severity);
        issue.put("message", message);
        return issue;
    }

    private Map<String, Object> buildDockerIssue(int line, String severity, String rule,
                                                   String message, String suggestion) {
        Map<String, Object> issue = new LinkedHashMap<>();
        issue.put("line", line);
        issue.put("severity", severity);
        issue.put("rule", rule);
        issue.put("message", message);
        issue.put("suggestion", suggestion);
        return issue;
    }

    private static Map<String, Object> buildServiceInfo(String name, String image,
                                                          List<String> defaultPorts, String description) {
        Map<String, Object> service = new LinkedHashMap<>();
        service.put("name", name);
        service.put("image", image);
        service.put("defaultPorts", defaultPorts);
        service.put("description", description);
        return service;
    }

    // ========== JSON Serialization (minimal, no external deps) ==========

    private String mapToJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(escapeJson(entry.getKey())).append("\":");
            sb.append(valueToJson(entry.getValue()));
        }
        sb.append("}");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String valueToJson(Object value) {
        if (value == null) return "null";
        if (value instanceof String) return "\"" + escapeJson((String) value) + "\"";
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        if (value instanceof Map) return mapToJson((Map<String, Object>) value);
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(valueToJson(list.get(i)));
            }
            sb.append("]");
            return sb.toString();
        }
        if (value instanceof Set) {
            return valueToJson(new ArrayList<>((Set<?>) value));
        }
        return "\"" + escapeJson(value.toString()) + "\"";
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
