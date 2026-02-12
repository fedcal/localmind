package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.LicenseAudit;
import com.localmind.domain.mcp.model.VulnerabilityScan;
import com.localmind.domain.mcp.port.in.DependencyAnalysisUseCase;
import com.localmind.domain.mcp.port.out.DependencyAnalysisRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Domain service implementing the dependency-manager tool group.
 * Provides vulnerability scanning, unused dependency detection, and license auditing.
 * Supports both Maven (pom.xml) and npm (package.json) projects.
 * No Spring annotations - registered as a bean via DomainConfig.
 */
public class DependencyAnalysisService implements DependencyAnalysisUseCase {

    private final DependencyAnalysisRepository repository;

    /**
     * Known vulnerable libraries with minimum safe versions.
     * Format: groupId:artifactId -> {maxVulnerableVersion, severity, description, fixVersion}
     */
    private static final Map<String, VulnerableLibrary> KNOWN_VULNERABILITIES = new LinkedHashMap<>();

    static {
        KNOWN_VULNERABILITIES.put("org.apache.logging.log4j:log4j-core", new VulnerableLibrary(
                "2.17.0", "critical", "Log4Shell - Remote Code Execution (CVE-2021-44228)", "2.17.1"));
        KNOWN_VULNERABILITIES.put("org.apache.logging.log4j:log4j-api", new VulnerableLibrary(
                "2.17.0", "critical", "Log4Shell - Remote Code Execution (CVE-2021-44228)", "2.17.1"));
        KNOWN_VULNERABILITIES.put("org.springframework:spring-core", new VulnerableLibrary(
                "5.3.18", "critical", "Spring4Shell - Remote Code Execution (CVE-2022-22965)", "5.3.18"));
        KNOWN_VULNERABILITIES.put("org.springframework:spring-beans", new VulnerableLibrary(
                "5.3.18", "critical", "Spring4Shell - Remote Code Execution (CVE-2022-22965)", "5.3.18"));
        KNOWN_VULNERABILITIES.put("com.fasterxml.jackson.core:jackson-databind", new VulnerableLibrary(
                "2.13.2.1", "high", "Deserialization vulnerability (CVE-2022-42003)", "2.13.2.2"));
        KNOWN_VULNERABILITIES.put("org.apache.commons:commons-text", new VulnerableLibrary(
                "1.10.0", "critical", "Text4Shell - Arbitrary code execution (CVE-2022-42889)", "1.10.0"));
        KNOWN_VULNERABILITIES.put("com.google.guava:guava", new VulnerableLibrary(
                "31.1", "medium", "Temporary directory information disclosure (CVE-2023-2976)", "32.0.0"));
        KNOWN_VULNERABILITIES.put("org.yaml:snakeyaml", new VulnerableLibrary(
                "1.33", "high", "Denial of Service via crafted YAML (CVE-2022-1471)", "2.0"));
        KNOWN_VULNERABILITIES.put("io.netty:netty-handler", new VulnerableLibrary(
                "4.1.86.Final", "medium", "HTTP Response Smuggling (CVE-2022-41881)", "4.1.86.Final"));
        KNOWN_VULNERABILITIES.put("org.apache.tomcat.embed:tomcat-embed-core", new VulnerableLibrary(
                "9.0.68", "high", "Request Smuggling (CVE-2022-42252)", "9.0.68"));
    }

    /**
     * Copyleft licenses that should be flagged during audit.
     */
    private static final Set<String> COPYLEFT_LICENSES = Set.of(
            "GPL-2.0", "GPL-3.0", "GPL-2.0-only", "GPL-3.0-only",
            "GPL-2.0-or-later", "GPL-3.0-or-later",
            "AGPL-1.0", "AGPL-3.0", "AGPL-3.0-only", "AGPL-3.0-or-later",
            "LGPL-2.0", "LGPL-2.1", "LGPL-3.0",
            "LGPL-2.0-only", "LGPL-2.1-only", "LGPL-3.0-only",
            "LGPL-2.0-or-later", "LGPL-2.1-or-later", "LGPL-3.0-or-later",
            "MPL-2.0", "EUPL-1.1", "EUPL-1.2",
            "CPAL-1.0", "OSL-3.0", "CC-BY-SA-4.0"
    );

    /**
     * Maven dependencies that should be skipped when checking for usage
     * (they are runtime/provided, not explicitly imported).
     */
    private static final Set<String> MAVEN_IMPLICIT_DEPENDENCIES = Set.of(
            "spring-boot-starter", "spring-boot-starter-web", "spring-boot-starter-test",
            "spring-boot-starter-data-jpa", "spring-boot-starter-security",
            "spring-boot-starter-validation", "spring-boot-starter-actuator",
            "spring-boot-starter-webflux", "spring-boot-devtools",
            "lombok", "junit-jupiter", "mockito-core", "assertj-core",
            "mysql-connector-j", "mysql-connector-java", "h2",
            "flyway-core", "flyway-mysql", "jackson-databind",
            "jakarta.annotation-api", "jakarta.persistence-api",
            "spring-boot-starter-parent"
    );

    /**
     * npm packages that are typically devDependencies or implicitly used.
     */
    private static final Set<String> NPM_IMPLICIT_PACKAGES = Set.of(
            "typescript", "tslib", "@types/node", "@types/jest",
            "zone.js", "rxjs"
    );

    public DependencyAnalysisService(DependencyAnalysisRepository repository) {
        this.repository = repository;
    }

    @Override
    public Map<String, Object> checkVulnerabilities(String projectPath, String projectType) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("projectPath", projectPath);
        result.put("projectType", projectType);

        try {
            if ("maven".equalsIgnoreCase(projectType)) {
                return checkMavenVulnerabilities(projectPath);
            } else if ("npm".equalsIgnoreCase(projectType)) {
                return checkNpmVulnerabilities(projectPath);
            } else {
                result.put("error", "Unsupported project type: " + projectType + ". Use 'maven' or 'npm'.");
                return result;
            }
        } catch (Exception e) {
            result.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
            return result;
        }
    }

    @Override
    public Map<String, Object> findUnusedDependencies(String projectPath, String projectType) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("projectPath", projectPath);
        result.put("projectType", projectType);

        try {
            if ("maven".equalsIgnoreCase(projectType)) {
                return findUnusedMavenDependencies(projectPath);
            } else if ("npm".equalsIgnoreCase(projectType)) {
                return findUnusedNpmDependencies(projectPath);
            } else {
                result.put("error", "Unsupported project type: " + projectType + ". Use 'maven' or 'npm'.");
                return result;
            }
        } catch (Exception e) {
            result.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
            return result;
        }
    }

    @Override
    public Map<String, Object> licenseAudit(String projectPath, String projectType) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("projectPath", projectPath);
        result.put("projectType", projectType);

        try {
            if ("maven".equalsIgnoreCase(projectType)) {
                return auditMavenLicenses(projectPath);
            } else if ("npm".equalsIgnoreCase(projectType)) {
                return auditNpmLicenses(projectPath);
            } else {
                result.put("error", "Unsupported project type: " + projectType + ". Use 'maven' or 'npm'.");
                return result;
            }
        } catch (Exception e) {
            result.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
            return result;
        }
    }

    // ========== Maven Vulnerability Check ==========

    private Map<String, Object> checkMavenVulnerabilities(String projectPath) throws Exception {
        Path pomPath = Paths.get(projectPath, "pom.xml");
        String pomContent = readFileContent(pomPath);

        List<Map<String, String>> dependencies = parseMavenDependencies(pomContent);
        List<Map<String, Object>> vulnerabilities = new ArrayList<>();

        int critical = 0, high = 0, medium = 0, low = 0;

        for (Map<String, String> dep : dependencies) {
            String groupId = dep.get("groupId");
            String artifactId = dep.get("artifactId");
            String version = dep.get("version");

            if (version == null || version.startsWith("${")) {
                continue;
            }

            String key = groupId + ":" + artifactId;
            VulnerableLibrary vulnLib = KNOWN_VULNERABILITIES.get(key);

            if (vulnLib != null && isVersionVulnerable(version, vulnLib.maxVulnerableVersion)) {
                Map<String, Object> vuln = new LinkedHashMap<>();
                vuln.put("name", key);
                vuln.put("version", version);
                vuln.put("severity", vulnLib.severity);
                vuln.put("description", vulnLib.description);
                vuln.put("fixVersion", vulnLib.fixVersion);
                vulnerabilities.add(vuln);

                switch (vulnLib.severity) {
                    case "critical" -> critical++;
                    case "high" -> high++;
                    case "medium" -> medium++;
                    case "low" -> low++;
                }
            }
        }

        Map<String, Object> bySeverity = new LinkedHashMap<>();
        bySeverity.put("critical", critical);
        bySeverity.put("high", high);
        bySeverity.put("medium", medium);
        bySeverity.put("low", low);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("projectPath", projectPath);
        result.put("projectType", "maven");
        result.put("totalVulnerabilities", vulnerabilities.size());
        result.put("bySeverity", bySeverity);
        result.put("vulnerabilities", vulnerabilities);

        // Persist
        VulnerabilityScan scan = VulnerabilityScan.builder()
                .id(UUID.randomUUID().toString())
                .projectPath(projectPath)
                .projectType("maven")
                .totalVulnerabilities(vulnerabilities.size())
                .resultJson(mapToJson(result))
                .createdAt(Instant.now())
                .build();
        repository.saveVulnerabilityScan(scan);

        return result;
    }

    private Map<String, Object> checkNpmVulnerabilities(String projectPath) throws Exception {
        Path packageJsonPath = Paths.get(projectPath, "package.json");
        if (!Files.exists(packageJsonPath)) {
            throw new IllegalArgumentException("package.json not found at: " + packageJsonPath);
        }

        try {
            ProcessBuilder pb = new ProcessBuilder("npm", "audit", "--json");
            pb.directory(Paths.get(projectPath).toFile());
            pb.redirectErrorStream(true);
            Process process = pb.start();

            String output;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                output = reader.lines().collect(Collectors.joining("\n"));
            }

            process.waitFor();

            Map<String, Object> result = parseNpmAuditOutput(output, projectPath);

            // Persist
            int totalVulnerabilities = result.containsKey("totalVulnerabilities")
                    ? (int) result.get("totalVulnerabilities") : 0;
            VulnerabilityScan scan = VulnerabilityScan.builder()
                    .id(UUID.randomUUID().toString())
                    .projectPath(projectPath)
                    .projectType("npm")
                    .totalVulnerabilities(totalVulnerabilities)
                    .resultJson(mapToJson(result))
                    .createdAt(Instant.now())
                    .build();
            repository.saveVulnerabilityScan(scan);

            return result;

        } catch (Exception e) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("projectPath", projectPath);
            result.put("projectType", "npm");
            result.put("error", "Failed to run npm audit: " + e.getMessage());
            result.put("note", "Ensure npm is installed and package-lock.json exists");
            return result;
        }
    }

    // ========== Find Unused Dependencies ==========

    private Map<String, Object> findUnusedMavenDependencies(String projectPath) throws Exception {
        Path pomPath = Paths.get(projectPath, "pom.xml");
        String pomContent = readFileContent(pomPath);

        List<Map<String, String>> dependencies = parseMavenDependencies(pomContent);

        // Collect all import statements from .java files
        Path srcPath = Paths.get(projectPath, "src");
        Set<String> allImports = new HashSet<>();
        int sourceFilesScanned = 0;

        if (Files.exists(srcPath)) {
            try (Stream<Path> paths = Files.walk(srcPath)) {
                List<Path> javaFiles = paths
                        .filter(p -> p.toString().endsWith(".java"))
                        .filter(p -> !p.toString().contains("/target/"))
                        .filter(p -> !p.toString().contains("/.git/"))
                        .collect(Collectors.toList());

                sourceFilesScanned = javaFiles.size();

                for (Path javaFile : javaFiles) {
                    String content = readFileContent(javaFile);
                    Pattern importPattern = Pattern.compile("import\\s+(static\\s+)?([\\w.]+);");
                    Matcher matcher = importPattern.matcher(content);
                    while (matcher.find()) {
                        allImports.add(matcher.group(2));
                    }
                }
            }
        }

        List<String> unusedDeps = new ArrayList<>();
        List<String> usedDeps = new ArrayList<>();

        for (Map<String, String> dep : dependencies) {
            String artifactId = dep.get("artifactId");
            String groupId = dep.get("groupId");
            String fullName = groupId + ":" + artifactId;

            // Skip implicit dependencies
            if (MAVEN_IMPLICIT_DEPENDENCIES.contains(artifactId)) {
                usedDeps.add(fullName);
                continue;
            }

            // Check if any import matches the groupId package pattern.
            // We check multiple prefixes because some libs have packages
            // that differ from the groupId (e.g., com.google.guava -> com.google.common)
            String packagePrefix = groupId.replace("-", ".");
            // Also check the base groupId (first 2 segments, e.g., "com.google")
            String[] segments = packagePrefix.split("\\.");
            String basePrefix = segments.length >= 2
                    ? segments[0] + "." + segments[1]
                    : packagePrefix;
            boolean isUsed = allImports.stream()
                    .anyMatch(imp -> imp.startsWith(packagePrefix) || imp.startsWith(basePrefix));

            if (isUsed) {
                usedDeps.add(fullName);
            } else {
                unusedDeps.add(fullName);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("projectPath", projectPath);
        result.put("projectType", "maven");
        result.put("totalDependencies", dependencies.size());
        result.put("totalUnused", unusedDeps.size());
        result.put("unusedDependencies", unusedDeps);
        result.put("usedDependencies", usedDeps);
        result.put("sourceFilesScanned", sourceFilesScanned);
        result.put("note", "Implicit/runtime dependencies (starters, test, JDBC drivers) are assumed used.");

        // Persist
        VulnerabilityScan scan = VulnerabilityScan.builder()
                .id(UUID.randomUUID().toString())
                .projectPath(projectPath)
                .projectType("maven-unused")
                .totalVulnerabilities(unusedDeps.size())
                .resultJson(mapToJson(result))
                .createdAt(Instant.now())
                .build();
        repository.saveUnusedAnalysis(scan);

        return result;
    }

    private Map<String, Object> findUnusedNpmDependencies(String projectPath) throws Exception {
        Path packageJsonPath = Paths.get(projectPath, "package.json");
        String packageJsonContent = readFileContent(packageJsonPath);

        // Parse dependencies from package.json
        Set<String> declaredDeps = parseNpmDependencyNames(packageJsonContent, "dependencies");
        Set<String> declaredDevDeps = parseNpmDependencyNames(packageJsonContent, "devDependencies");
        Set<String> allDeclaredDeps = new HashSet<>(declaredDeps);

        // Scan source files for imports/requires
        Path srcPath = Paths.get(projectPath, "src");
        Set<String> usedPackages = new HashSet<>();
        int sourceFilesScanned = 0;

        if (Files.exists(srcPath)) {
            try (Stream<Path> paths = Files.walk(srcPath)) {
                List<Path> sourceFiles = paths
                        .filter(p -> {
                            String name = p.toString();
                            return name.endsWith(".ts") || name.endsWith(".js")
                                    || name.endsWith(".tsx") || name.endsWith(".jsx");
                        })
                        .filter(p -> !p.toString().contains("/node_modules/"))
                        .filter(p -> !p.toString().contains("/dist/"))
                        .filter(p -> !p.toString().contains("/.git/"))
                        .collect(Collectors.toList());

                sourceFilesScanned = sourceFiles.size();

                for (Path sourceFile : sourceFiles) {
                    String content = readFileContent(sourceFile);
                    extractNpmImports(content, usedPackages);
                }
            }
        }

        List<String> unusedDeps = new ArrayList<>();
        List<String> usedDeps = new ArrayList<>();

        for (String dep : allDeclaredDeps) {
            if (NPM_IMPLICIT_PACKAGES.contains(dep)) {
                usedDeps.add(dep);
                continue;
            }

            // Check if the package or any subpath is imported
            boolean isUsed = usedPackages.stream()
                    .anyMatch(used -> used.equals(dep) || used.startsWith(dep + "/"));

            if (isUsed) {
                usedDeps.add(dep);
            } else {
                unusedDeps.add(dep);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("projectPath", projectPath);
        result.put("projectType", "npm");
        result.put("totalDependencies", allDeclaredDeps.size());
        result.put("totalUnused", unusedDeps.size());
        result.put("unusedDependencies", unusedDeps);
        result.put("usedDependencies", usedDeps);
        result.put("sourceFilesScanned", sourceFilesScanned);
        result.put("note", "Only 'dependencies' are checked (devDependencies are excluded). Implicit packages (zone.js, rxjs, etc.) are assumed used.");

        // Persist
        VulnerabilityScan scan = VulnerabilityScan.builder()
                .id(UUID.randomUUID().toString())
                .projectPath(projectPath)
                .projectType("npm-unused")
                .totalVulnerabilities(unusedDeps.size())
                .resultJson(mapToJson(result))
                .createdAt(Instant.now())
                .build();
        repository.saveUnusedAnalysis(scan);

        return result;
    }

    // ========== License Audit ==========

    private Map<String, Object> auditMavenLicenses(String projectPath) throws Exception {
        Path pomPath = Paths.get(projectPath, "pom.xml");
        String pomContent = readFileContent(pomPath);

        List<Map<String, String>> dependencies = parseMavenDependencies(pomContent);
        Map<String, List<Map<String, String>>> licenseGroups = new LinkedHashMap<>();
        List<Map<String, String>> warnings = new ArrayList<>();
        List<String> notFound = new ArrayList<>();
        Set<String> uniqueLicenses = new HashSet<>();

        // Parse licenses declared in pom.xml
        Map<String, String> declaredLicenses = parseMavenLicenses(pomContent);

        for (Map<String, String> dep : dependencies) {
            String artifactId = dep.get("artifactId");
            String groupId = dep.get("groupId");
            String version = dep.getOrDefault("version", "unknown");
            String fullName = groupId + ":" + artifactId;

            String license = declaredLicenses.getOrDefault(fullName,
                    guessMavenLicense(groupId, artifactId));

            if (license == null) {
                notFound.add(fullName);
                continue;
            }

            uniqueLicenses.add(license);

            Map<String, String> depInfo = new LinkedHashMap<>();
            depInfo.put("name", fullName);
            depInfo.put("version", version);

            licenseGroups.computeIfAbsent(license, k -> new ArrayList<>()).add(depInfo);

            // Check if copyleft
            if (isCopyleft(license)) {
                Map<String, String> warning = new LinkedHashMap<>();
                warning.put("name", fullName);
                warning.put("version", version);
                warning.put("license", license);
                warnings.add(warning);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("projectPath", projectPath);
        result.put("projectType", "maven");
        result.put("totalChecked", dependencies.size());
        result.put("uniqueLicenses", uniqueLicenses.size());
        result.put("copyleftCount", warnings.size());
        result.put("warnings", warnings);
        result.put("licenseGroups", licenseGroups);
        result.put("notFound", notFound);

        // Persist
        LicenseAudit audit = LicenseAudit.builder()
                .id(UUID.randomUUID().toString())
                .projectPath(projectPath)
                .projectType("maven")
                .totalChecked(dependencies.size())
                .copyleftCount(warnings.size())
                .resultJson(mapToJson(result))
                .createdAt(Instant.now())
                .build();
        repository.saveLicenseAudit(audit);

        return result;
    }

    private Map<String, Object> auditNpmLicenses(String projectPath) throws Exception {
        Path packageJsonPath = Paths.get(projectPath, "package.json");
        String packageJsonContent = readFileContent(packageJsonPath);

        Set<String> declaredDeps = parseNpmDependencyNames(packageJsonContent, "dependencies");
        Map<String, List<Map<String, String>>> licenseGroups = new LinkedHashMap<>();
        List<Map<String, String>> warnings = new ArrayList<>();
        List<String> notFound = new ArrayList<>();
        Set<String> uniqueLicenses = new HashSet<>();

        Path nodeModulesPath = Paths.get(projectPath, "node_modules");

        for (String depName : declaredDeps) {
            Path depPackageJson = nodeModulesPath.resolve(depName).resolve("package.json");

            if (!Files.exists(depPackageJson)) {
                notFound.add(depName);
                continue;
            }

            String depContent = readFileContent(depPackageJson);
            String license = extractNpmLicense(depContent);
            String version = extractNpmField(depContent, "version");

            if (license == null || license.isBlank()) {
                notFound.add(depName);
                continue;
            }

            uniqueLicenses.add(license);

            Map<String, String> depInfo = new LinkedHashMap<>();
            depInfo.put("name", depName);
            depInfo.put("version", version != null ? version : "unknown");

            licenseGroups.computeIfAbsent(license, k -> new ArrayList<>()).add(depInfo);

            if (isCopyleft(license)) {
                Map<String, String> warning = new LinkedHashMap<>();
                warning.put("name", depName);
                warning.put("version", version != null ? version : "unknown");
                warning.put("license", license);
                warnings.add(warning);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("projectPath", projectPath);
        result.put("projectType", "npm");
        result.put("totalChecked", declaredDeps.size());
        result.put("uniqueLicenses", uniqueLicenses.size());
        result.put("copyleftCount", warnings.size());
        result.put("warnings", warnings);
        result.put("licenseGroups", licenseGroups);
        result.put("notFound", notFound);

        // Persist
        LicenseAudit audit = LicenseAudit.builder()
                .id(UUID.randomUUID().toString())
                .projectPath(projectPath)
                .projectType("npm")
                .totalChecked(declaredDeps.size())
                .copyleftCount(warnings.size())
                .resultJson(mapToJson(result))
                .createdAt(Instant.now())
                .build();
        repository.saveLicenseAudit(audit);

        return result;
    }

    // ========== XML Parsing Helpers ==========

    /**
     * Parses Maven dependency declarations from pom.xml content.
     * Extracts groupId, artifactId, and version from each dependency block.
     */
    List<Map<String, String>> parseMavenDependencies(String pomContent) {
        List<Map<String, String>> dependencies = new ArrayList<>();

        // Remove comments
        String cleaned = pomContent.replaceAll("<!--[\\s\\S]*?-->", "");

        // Find <dependencies> section (not <dependencyManagement>)
        Pattern depsBlockPattern = Pattern.compile(
                "<dependencies>(.*?)</dependencies>",
                Pattern.DOTALL);
        Matcher blockMatcher = depsBlockPattern.matcher(cleaned);

        while (blockMatcher.find()) {
            String depsBlock = blockMatcher.group(1);

            // Check this is not inside <dependencyManagement>
            int blockStart = blockMatcher.start();
            String before = cleaned.substring(Math.max(0, blockStart - 100), blockStart);
            if (before.contains("<dependencyManagement>")) {
                continue;
            }

            Pattern depPattern = Pattern.compile(
                    "<dependency>(.*?)</dependency>",
                    Pattern.DOTALL);
            Matcher depMatcher = depPattern.matcher(depsBlock);

            while (depMatcher.find()) {
                String depBlock = depMatcher.group(1);
                Map<String, String> dep = new LinkedHashMap<>();

                String groupId = extractXmlTag(depBlock, "groupId");
                String artifactId = extractXmlTag(depBlock, "artifactId");
                String version = extractXmlTag(depBlock, "version");

                if (groupId != null && artifactId != null) {
                    dep.put("groupId", groupId);
                    dep.put("artifactId", artifactId);
                    if (version != null) {
                        dep.put("version", version);
                    }
                    dependencies.add(dep);
                }
            }
        }

        return dependencies;
    }

    /**
     * Parses license declarations from pom.xml content.
     */
    Map<String, String> parseMavenLicenses(String pomContent) {
        Map<String, String> licenses = new LinkedHashMap<>();

        // Look for <licenses> block in project-level
        Pattern licenseBlockPattern = Pattern.compile(
                "<licenses>(.*?)</licenses>",
                Pattern.DOTALL);
        Matcher blockMatcher = licenseBlockPattern.matcher(pomContent);

        // This captures the project's own license
        if (blockMatcher.find()) {
            String licensesBlock = blockMatcher.group(1);
            Pattern licensePattern = Pattern.compile(
                    "<license>(.*?)</license>",
                    Pattern.DOTALL);
            Matcher licenseMatcher = licensePattern.matcher(licensesBlock);

            while (licenseMatcher.find()) {
                String licenseName = extractXmlTag(licenseMatcher.group(1), "name");
                if (licenseName != null) {
                    // This is the project's own license, store it generically
                    licenses.put("_project_license_", licenseName);
                }
            }
        }

        return licenses;
    }

    private String extractXmlTag(String content, String tagName) {
        Pattern pattern = Pattern.compile("<" + tagName + ">\\s*(.*?)\\s*</" + tagName + ">", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    // ========== npm Parsing Helpers ==========

    /**
     * Parses dependency names from a package.json section.
     */
    Set<String> parseNpmDependencyNames(String packageJsonContent, String section) {
        Set<String> deps = new HashSet<>();

        // Simple regex to extract dependency names from a section
        Pattern sectionPattern = Pattern.compile(
                "\"" + section + "\"\\s*:\\s*\\{([^}]*)\\}",
                Pattern.DOTALL);
        Matcher sectionMatcher = sectionPattern.matcher(packageJsonContent);

        if (sectionMatcher.find()) {
            String sectionContent = sectionMatcher.group(1);
            Pattern depPattern = Pattern.compile("\"([^\"]+)\"\\s*:");
            Matcher depMatcher = depPattern.matcher(sectionContent);

            while (depMatcher.find()) {
                deps.add(depMatcher.group(1));
            }
        }

        return deps;
    }

    /**
     * Extracts import/require package names from JS/TS source content.
     */
    void extractNpmImports(String content, Set<String> packages) {
        // ES6 imports: import X from 'package'
        Pattern importPattern = Pattern.compile(
                "(?:import|export)\\s+(?:[^;]*?\\s+from\\s+)?['\"]([^'\"./][^'\"]*)['\"]");
        Matcher importMatcher = importPattern.matcher(content);
        while (importMatcher.find()) {
            String pkg = importMatcher.group(1);
            packages.add(getPackageName(pkg));
        }

        // require: require('package')
        Pattern requirePattern = Pattern.compile(
                "require\\s*\\(\\s*['\"]([^'\"./][^'\"]*)['\"]\\s*\\)");
        Matcher requireMatcher = requirePattern.matcher(content);
        while (requireMatcher.find()) {
            String pkg = requireMatcher.group(1);
            packages.add(getPackageName(pkg));
        }
    }

    /**
     * Extracts the root package name from an import path.
     * Handles scoped packages (@scope/name).
     */
    private String getPackageName(String importPath) {
        if (importPath.startsWith("@")) {
            // Scoped package: @scope/name/sub -> @scope/name
            String[] parts = importPath.split("/");
            if (parts.length >= 2) {
                return parts[0] + "/" + parts[1];
            }
            return importPath;
        }
        // Regular package: name/sub -> name
        int slashIndex = importPath.indexOf('/');
        return slashIndex > 0 ? importPath.substring(0, slashIndex) : importPath;
    }

    /**
     * Extracts the "license" field from a package.json file.
     */
    String extractNpmLicense(String packageJsonContent) {
        return extractNpmField(packageJsonContent, "license");
    }

    /**
     * Extracts a simple string field from package.json content.
     */
    String extractNpmField(String content, String fieldName) {
        Pattern pattern = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    // ========== Version Comparison ==========

    /**
     * Checks if a version string is vulnerable (i.e., less than the max vulnerable version).
     */
    boolean isVersionVulnerable(String currentVersion, String maxVulnerableVersion) {
        try {
            String[] current = normalizeVersion(currentVersion);
            String[] maxVuln = normalizeVersion(maxVulnerableVersion);

            int length = Math.max(current.length, maxVuln.length);
            for (int i = 0; i < length; i++) {
                int c = i < current.length ? parseVersionPart(current[i]) : 0;
                int m = i < maxVuln.length ? parseVersionPart(maxVuln[i]) : 0;

                if (c < m) return true;
                if (c > m) return false;
            }

            // Equal versions are considered vulnerable
            return false;
        } catch (Exception e) {
            // If version parsing fails, assume not vulnerable
            return false;
        }
    }

    private String[] normalizeVersion(String version) {
        // Remove qualifiers like -SNAPSHOT, .RELEASE, .Final
        String cleaned = version.replaceAll("[-.]?(SNAPSHOT|RELEASE|Final|GA|SR\\d+)$", "");
        return cleaned.split("[.]");
    }

    private int parseVersionPart(String part) {
        try {
            return Integer.parseInt(part.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ========== License Check ==========

    /**
     * Checks if a license identifier matches a known copyleft license.
     */
    boolean isCopyleft(String license) {
        if (license == null) return false;
        String normalized = license.trim();
        // Direct match
        if (COPYLEFT_LICENSES.contains(normalized)) {
            return true;
        }
        // Case-insensitive partial match for common names
        String upper = normalized.toUpperCase();
        return upper.contains("GPL") || upper.contains("AGPL")
                || upper.contains("LGPL") || upper.equals("MPL-2.0")
                || upper.contains("EUPL") || upper.contains("CPAL")
                || upper.contains("OSL") || upper.contains("CC-BY-SA");
    }

    // ========== npm Audit Output Parsing ==========

    /**
     * Parses npm audit JSON output into a structured result map.
     */
    Map<String, Object> parseNpmAuditOutput(String output, String projectPath) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("projectPath", projectPath);
        result.put("projectType", "npm");

        // Simple parsing of npm audit --json output
        int critical = countOccurrences(output, "\"severity\":\"critical\"");
        int high = countOccurrences(output, "\"severity\":\"high\"");
        int medium = countOccurrences(output, "\"severity\":\"moderate\"");
        int low = countOccurrences(output, "\"severity\":\"low\"");

        int total = critical + high + medium + low;

        Map<String, Object> bySeverity = new LinkedHashMap<>();
        bySeverity.put("critical", critical);
        bySeverity.put("high", high);
        bySeverity.put("medium", medium);
        bySeverity.put("low", low);

        result.put("totalVulnerabilities", total);
        result.put("bySeverity", bySeverity);
        result.put("rawOutput", output.length() > 5000
                ? output.substring(0, 5000) + "... [truncated]"
                : output);

        return result;
    }

    private int countOccurrences(String text, String search) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(search, idx)) != -1) {
            count++;
            idx += search.length();
        }
        return count;
    }

    // ========== License Guessing ==========

    /**
     * Guesses the license for well-known Maven artifacts based on groupId.
     */
    String guessMavenLicense(String groupId, String artifactId) {
        if (groupId.startsWith("org.apache")) return "Apache-2.0";
        if (groupId.startsWith("org.springframework")) return "Apache-2.0";
        if (groupId.startsWith("com.fasterxml.jackson")) return "Apache-2.0";
        if (groupId.startsWith("com.google")) return "Apache-2.0";
        if (groupId.startsWith("io.netty")) return "Apache-2.0";
        if (groupId.startsWith("org.slf4j")) return "MIT";
        if (groupId.startsWith("ch.qos.logback")) return "LGPL-2.1";
        if (groupId.startsWith("org.projectlombok")) return "MIT";
        if (groupId.startsWith("org.junit")) return "EPL-2.0";
        if (groupId.startsWith("org.mockito")) return "MIT";
        if (groupId.startsWith("org.assertj")) return "Apache-2.0";
        if (groupId.startsWith("mysql") || groupId.startsWith("com.mysql")) return "GPL-2.0";
        if (groupId.startsWith("org.postgresql")) return "BSD-2-Clause";
        if (groupId.startsWith("org.hibernate")) return "LGPL-2.1";
        if (groupId.startsWith("jakarta")) return "EPL-2.0";
        return null;
    }

    // ========== File I/O ==========

    /**
     * Reads the content of a file as a string.
     * Package-private for testability.
     */
    String readFileContent(Path path) throws Exception {
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("File not found: " + path);
        }
        return Files.readString(path);
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

    // ========== Inner Classes ==========

    /**
     * Represents a known vulnerable library entry.
     */
    static class VulnerableLibrary {
        final String maxVulnerableVersion;
        final String severity;
        final String description;
        final String fixVersion;

        VulnerableLibrary(String maxVulnerableVersion, String severity, String description, String fixVersion) {
            this.maxVulnerableVersion = maxVulnerableVersion;
            this.severity = severity;
            this.description = description;
            this.fixVersion = fixVersion;
        }
    }
}
