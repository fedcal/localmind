package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.LicenseAudit;
import com.localmind.domain.mcp.model.VulnerabilityScan;
import com.localmind.domain.mcp.port.out.DependencyAnalysisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DependencyAnalysisServiceTest {

    @Mock
    private DependencyAnalysisRepository repository;

    private DependencyAnalysisService service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        service = new DependencyAnalysisService(repository);
        lenient().when(repository.saveVulnerabilityScan(any(VulnerabilityScan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(repository.saveLicenseAudit(any(LicenseAudit.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(repository.saveUnusedAnalysis(any(VulnerabilityScan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    // ========== checkVulnerabilities - Maven ==========

    @Test
    @SuppressWarnings("unchecked")
    void checkVulnerabilities_mavenWithVulnerableDeps_findsVulnerabilities() throws Exception {
        String pomContent = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                    <dependencies>
                        <dependency>
                            <groupId>org.apache.logging.log4j</groupId>
                            <artifactId>log4j-core</artifactId>
                            <version>2.14.1</version>
                        </dependency>
                        <dependency>
                            <groupId>com.fasterxml.jackson.core</groupId>
                            <artifactId>jackson-databind</artifactId>
                            <version>2.12.0</version>
                        </dependency>
                        <dependency>
                            <groupId>org.springframework</groupId>
                            <artifactId>spring-core</artifactId>
                            <version>5.3.15</version>
                        </dependency>
                    </dependencies>
                </project>
                """;
        Files.writeString(tempDir.resolve("pom.xml"), pomContent);

        Map<String, Object> result = service.checkVulnerabilities(tempDir.toString(), "maven");

        assertThat(result.get("projectType")).isEqualTo("maven");
        assertThat((int) result.get("totalVulnerabilities")).isEqualTo(3);

        Map<String, Object> bySeverity = (Map<String, Object>) result.get("bySeverity");
        assertThat((int) bySeverity.get("critical")).isEqualTo(2); // log4j + spring-core
        assertThat((int) bySeverity.get("high")).isEqualTo(1); // jackson-databind

        List<Map<String, Object>> vulns = (List<Map<String, Object>>) result.get("vulnerabilities");
        assertThat(vulns).hasSize(3);
        assertThat(vulns.get(0).get("name")).isEqualTo("org.apache.logging.log4j:log4j-core");
        assertThat(vulns.get(0).get("severity")).isEqualTo("critical");

        verify(repository).saveVulnerabilityScan(any(VulnerabilityScan.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void checkVulnerabilities_mavenWithSafeDeps_findsNoVulnerabilities() throws Exception {
        String pomContent = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                    <dependencies>
                        <dependency>
                            <groupId>org.apache.logging.log4j</groupId>
                            <artifactId>log4j-core</artifactId>
                            <version>2.20.0</version>
                        </dependency>
                        <dependency>
                            <groupId>com.fasterxml.jackson.core</groupId>
                            <artifactId>jackson-databind</artifactId>
                            <version>2.15.0</version>
                        </dependency>
                    </dependencies>
                </project>
                """;
        Files.writeString(tempDir.resolve("pom.xml"), pomContent);

        Map<String, Object> result = service.checkVulnerabilities(tempDir.toString(), "maven");

        assertThat((int) result.get("totalVulnerabilities")).isEqualTo(0);

        Map<String, Object> bySeverity = (Map<String, Object>) result.get("bySeverity");
        assertThat((int) bySeverity.get("critical")).isEqualTo(0);
        assertThat((int) bySeverity.get("high")).isEqualTo(0);
        assertThat((int) bySeverity.get("medium")).isEqualTo(0);
        assertThat((int) bySeverity.get("low")).isEqualTo(0);

        List<Map<String, Object>> vulns = (List<Map<String, Object>>) result.get("vulnerabilities");
        assertThat(vulns).isEmpty();
    }

    @Test
    void checkVulnerabilities_mavenFileNotFound_returnsError() {
        Map<String, Object> result = service.checkVulnerabilities(
                tempDir.resolve("nonexistent").toString(), "maven");

        assertThat(result.get("error")).isNotNull();
        assertThat((String) result.get("error")).contains("File not found");
    }

    @Test
    @SuppressWarnings("unchecked")
    void checkVulnerabilities_mavenWithPropertyVersions_skipsPropertyDeps() throws Exception {
        String pomContent = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                    <dependencies>
                        <dependency>
                            <groupId>org.apache.logging.log4j</groupId>
                            <artifactId>log4j-core</artifactId>
                            <version>${log4j.version}</version>
                        </dependency>
                        <dependency>
                            <groupId>com.fasterxml.jackson.core</groupId>
                            <artifactId>jackson-databind</artifactId>
                            <version>2.12.0</version>
                        </dependency>
                    </dependencies>
                </project>
                """;
        Files.writeString(tempDir.resolve("pom.xml"), pomContent);

        Map<String, Object> result = service.checkVulnerabilities(tempDir.toString(), "maven");

        // log4j with ${...} version is skipped, jackson is vulnerable
        assertThat((int) result.get("totalVulnerabilities")).isEqualTo(1);
        List<Map<String, Object>> vulns = (List<Map<String, Object>>) result.get("vulnerabilities");
        assertThat(vulns.get(0).get("name")).isEqualTo("com.fasterxml.jackson.core:jackson-databind");
    }

    @Test
    void checkVulnerabilities_unsupportedProjectType_returnsError() {
        Map<String, Object> result = service.checkVulnerabilities(tempDir.toString(), "gradle");

        assertThat(result.get("error")).isNotNull();
        assertThat((String) result.get("error")).contains("Unsupported project type");
    }

    // ========== checkVulnerabilities - persistence ==========

    @Test
    void checkVulnerabilities_persistsScanResult() throws Exception {
        String pomContent = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                    <dependencies>
                        <dependency>
                            <groupId>org.apache.logging.log4j</groupId>
                            <artifactId>log4j-core</artifactId>
                            <version>2.14.1</version>
                        </dependency>
                    </dependencies>
                </project>
                """;
        Files.writeString(tempDir.resolve("pom.xml"), pomContent);

        service.checkVulnerabilities(tempDir.toString(), "maven");

        ArgumentCaptor<VulnerabilityScan> captor = ArgumentCaptor.forClass(VulnerabilityScan.class);
        verify(repository).saveVulnerabilityScan(captor.capture());

        VulnerabilityScan saved = captor.getValue();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getProjectPath()).isEqualTo(tempDir.toString());
        assertThat(saved.getProjectType()).isEqualTo("maven");
        assertThat(saved.getTotalVulnerabilities()).isEqualTo(1);
        assertThat(saved.getResultJson()).contains("log4j-core");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    // ========== findUnusedDependencies - Maven ==========

    @Test
    @SuppressWarnings("unchecked")
    void findUnusedDependencies_mavenWithUsedAndUnusedDeps_identifiesCorrectly() throws Exception {
        String pomContent = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                    <dependencies>
                        <dependency>
                            <groupId>com.google.guava</groupId>
                            <artifactId>guava</artifactId>
                            <version>32.0.0</version>
                        </dependency>
                        <dependency>
                            <groupId>org.apache.commons</groupId>
                            <artifactId>commons-lang3</artifactId>
                            <version>3.12.0</version>
                        </dependency>
                    </dependencies>
                </project>
                """;
        Files.writeString(tempDir.resolve("pom.xml"), pomContent);

        // Create source dir with a Java file that imports guava but not commons
        Path srcDir = tempDir.resolve("src/main/java/com/example");
        Files.createDirectories(srcDir);
        String javaContent = """
                package com.example;

                import com.google.common.collect.ImmutableList;

                public class MyClass {
                    public void doSomething() {
                        ImmutableList<String> list = ImmutableList.of("hello");
                    }
                }
                """;
        Files.writeString(srcDir.resolve("MyClass.java"), javaContent);

        Map<String, Object> result = service.findUnusedDependencies(tempDir.toString(), "maven");

        assertThat(result.get("projectType")).isEqualTo("maven");
        assertThat((int) result.get("totalDependencies")).isEqualTo(2);
        assertThat((int) result.get("totalUnused")).isEqualTo(1);

        List<String> unused = (List<String>) result.get("unusedDependencies");
        assertThat(unused).containsExactly("org.apache.commons:commons-lang3");

        List<String> used = (List<String>) result.get("usedDependencies");
        assertThat(used).containsExactly("com.google.guava:guava");

        assertThat((int) result.get("sourceFilesScanned")).isEqualTo(1);

        verify(repository).saveUnusedAnalysis(any(VulnerabilityScan.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void findUnusedDependencies_mavenImplicitDeps_areTreatedAsUsed() throws Exception {
        String pomContent = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                    <dependencies>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-web</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>1.18.30</version>
                        </dependency>
                    </dependencies>
                </project>
                """;
        Files.writeString(tempDir.resolve("pom.xml"), pomContent);

        // No source files
        Path srcDir = tempDir.resolve("src/main/java");
        Files.createDirectories(srcDir);

        Map<String, Object> result = service.findUnusedDependencies(tempDir.toString(), "maven");

        assertThat((int) result.get("totalUnused")).isEqualTo(0);

        List<String> used = (List<String>) result.get("usedDependencies");
        assertThat(used).contains(
                "org.springframework.boot:spring-boot-starter-web",
                "org.projectlombok:lombok"
        );
    }

    @Test
    void findUnusedDependencies_mavenNoPomFile_returnsError() {
        Map<String, Object> result = service.findUnusedDependencies(
                tempDir.resolve("nonexistent").toString(), "maven");

        assertThat(result.get("error")).isNotNull();
        assertThat((String) result.get("error")).contains("File not found");
    }

    // ========== findUnusedDependencies - npm ==========

    @Test
    @SuppressWarnings("unchecked")
    void findUnusedDependencies_npmWithUsedAndUnusedDeps_identifiesCorrectly() throws Exception {
        String packageJson = """
                {
                    "name": "test-project",
                    "dependencies": {
                        "@angular/core": "^17.0.0",
                        "lodash": "^4.17.21",
                        "moment": "^2.29.4"
                    },
                    "devDependencies": {
                        "typescript": "^5.0.0"
                    }
                }
                """;
        Files.writeString(tempDir.resolve("package.json"), packageJson);

        Path srcDir = tempDir.resolve("src");
        Files.createDirectories(srcDir);
        String tsContent = """
                import { Component } from '@angular/core';
                import * as _ from 'lodash';

                @Component({})
                export class AppComponent {}
                """;
        Files.writeString(srcDir.resolve("app.component.ts"), tsContent);

        Map<String, Object> result = service.findUnusedDependencies(tempDir.toString(), "npm");

        assertThat(result.get("projectType")).isEqualTo("npm");
        assertThat((int) result.get("totalDependencies")).isEqualTo(3);
        assertThat((int) result.get("totalUnused")).isEqualTo(1);

        List<String> unused = (List<String>) result.get("unusedDependencies");
        assertThat(unused).containsExactly("moment");

        List<String> used = (List<String>) result.get("usedDependencies");
        assertThat(used).contains("@angular/core", "lodash");
    }

    // ========== licenseAudit - Maven ==========

    @Test
    @SuppressWarnings("unchecked")
    void licenseAudit_mavenWithCopyleftDep_flagsWarning() throws Exception {
        String pomContent = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                    <dependencies>
                        <dependency>
                            <groupId>mysql</groupId>
                            <artifactId>mysql-connector-java</artifactId>
                            <version>8.0.33</version>
                        </dependency>
                        <dependency>
                            <groupId>org.springframework</groupId>
                            <artifactId>spring-core</artifactId>
                            <version>6.0.0</version>
                        </dependency>
                    </dependencies>
                </project>
                """;
        Files.writeString(tempDir.resolve("pom.xml"), pomContent);

        Map<String, Object> result = service.licenseAudit(tempDir.toString(), "maven");

        assertThat(result.get("projectType")).isEqualTo("maven");
        assertThat((int) result.get("totalChecked")).isEqualTo(2);
        assertThat((int) result.get("copyleftCount")).isEqualTo(1);

        List<Map<String, String>> warnings = (List<Map<String, String>>) result.get("warnings");
        assertThat(warnings).hasSize(1);
        assertThat(warnings.get(0).get("name")).isEqualTo("mysql:mysql-connector-java");
        assertThat(warnings.get(0).get("license")).isEqualTo("GPL-2.0");

        Map<String, List<Map<String, String>>> licenseGroups =
                (Map<String, List<Map<String, String>>>) result.get("licenseGroups");
        assertThat(licenseGroups).containsKey("Apache-2.0");
        assertThat(licenseGroups).containsKey("GPL-2.0");

        verify(repository).saveLicenseAudit(any(LicenseAudit.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void licenseAudit_mavenWithNoCopyleft_noWarnings() throws Exception {
        String pomContent = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                    <dependencies>
                        <dependency>
                            <groupId>org.springframework</groupId>
                            <artifactId>spring-core</artifactId>
                            <version>6.0.0</version>
                        </dependency>
                        <dependency>
                            <groupId>org.slf4j</groupId>
                            <artifactId>slf4j-api</artifactId>
                            <version>2.0.7</version>
                        </dependency>
                    </dependencies>
                </project>
                """;
        Files.writeString(tempDir.resolve("pom.xml"), pomContent);

        Map<String, Object> result = service.licenseAudit(tempDir.toString(), "maven");

        assertThat((int) result.get("copyleftCount")).isEqualTo(0);

        List<Map<String, String>> warnings = (List<Map<String, String>>) result.get("warnings");
        assertThat(warnings).isEmpty();
    }

    @Test
    void licenseAudit_persistsAuditResult() throws Exception {
        String pomContent = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                    <dependencies>
                        <dependency>
                            <groupId>mysql</groupId>
                            <artifactId>mysql-connector-java</artifactId>
                            <version>8.0.33</version>
                        </dependency>
                    </dependencies>
                </project>
                """;
        Files.writeString(tempDir.resolve("pom.xml"), pomContent);

        service.licenseAudit(tempDir.toString(), "maven");

        ArgumentCaptor<LicenseAudit> captor = ArgumentCaptor.forClass(LicenseAudit.class);
        verify(repository).saveLicenseAudit(captor.capture());

        LicenseAudit saved = captor.getValue();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getProjectPath()).isEqualTo(tempDir.toString());
        assertThat(saved.getProjectType()).isEqualTo("maven");
        assertThat(saved.getTotalChecked()).isEqualTo(1);
        assertThat(saved.getCopyleftCount()).isEqualTo(1);
        assertThat(saved.getResultJson()).contains("GPL-2.0");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void licenseAudit_mavenUnknownGroupId_addedToNotFound() throws Exception {
        String pomContent = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                    <dependencies>
                        <dependency>
                            <groupId>com.unknown.vendor</groupId>
                            <artifactId>mystery-lib</artifactId>
                            <version>1.0.0</version>
                        </dependency>
                    </dependencies>
                </project>
                """;
        Files.writeString(tempDir.resolve("pom.xml"), pomContent);

        Map<String, Object> result = service.licenseAudit(tempDir.toString(), "maven");

        List<String> notFound = (List<String>) result.get("notFound");
        assertThat(notFound).contains("com.unknown.vendor:mystery-lib");
    }

    // ========== parseMavenDependencies ==========

    @Test
    void parseMavenDependencies_emptyPom_returnsEmptyList() {
        String pomContent = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                </project>
                """;

        List<Map<String, String>> deps = service.parseMavenDependencies(pomContent);

        assertThat(deps).isEmpty();
    }

    @Test
    void parseMavenDependencies_withComments_ignoresCommentedDeps() {
        String pomContent = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                    <dependencies>
                        <!-- <dependency>
                            <groupId>commented.out</groupId>
                            <artifactId>should-be-ignored</artifactId>
                            <version>1.0.0</version>
                        </dependency> -->
                        <dependency>
                            <groupId>org.real</groupId>
                            <artifactId>real-dep</artifactId>
                            <version>2.0.0</version>
                        </dependency>
                    </dependencies>
                </project>
                """;

        List<Map<String, String>> deps = service.parseMavenDependencies(pomContent);

        assertThat(deps).hasSize(1);
        assertThat(deps.get(0).get("groupId")).isEqualTo("org.real");
        assertThat(deps.get(0).get("artifactId")).isEqualTo("real-dep");
    }

    // ========== Version comparison ==========

    @Test
    void isVersionVulnerable_lowerVersion_returnsTrue() {
        assertThat(service.isVersionVulnerable("2.14.1", "2.17.0")).isTrue();
        assertThat(service.isVersionVulnerable("5.3.15", "5.3.18")).isTrue();
        assertThat(service.isVersionVulnerable("2.12.0", "2.13.2.1")).isTrue();
    }

    @Test
    void isVersionVulnerable_higherVersion_returnsFalse() {
        assertThat(service.isVersionVulnerable("2.20.0", "2.17.0")).isFalse();
        assertThat(service.isVersionVulnerable("6.0.0", "5.3.18")).isFalse();
        assertThat(service.isVersionVulnerable("2.15.0", "2.13.2.1")).isFalse();
    }

    @Test
    void isVersionVulnerable_equalVersion_returnsFalse() {
        assertThat(service.isVersionVulnerable("2.17.0", "2.17.0")).isFalse();
    }

    @Test
    void isVersionVulnerable_withQualifier_handlesCorrectly() {
        assertThat(service.isVersionVulnerable("4.1.80.Final", "4.1.86.Final")).isTrue();
        assertThat(service.isVersionVulnerable("4.1.90.Final", "4.1.86.Final")).isFalse();
    }

    // ========== isCopyleft ==========

    @Test
    void isCopyleft_gplLicense_returnsTrue() {
        assertThat(service.isCopyleft("GPL-2.0")).isTrue();
        assertThat(service.isCopyleft("GPL-3.0")).isTrue();
        assertThat(service.isCopyleft("AGPL-3.0")).isTrue();
        assertThat(service.isCopyleft("LGPL-2.1")).isTrue();
        assertThat(service.isCopyleft("MPL-2.0")).isTrue();
        assertThat(service.isCopyleft("EUPL-1.2")).isTrue();
        assertThat(service.isCopyleft("CC-BY-SA-4.0")).isTrue();
    }

    @Test
    void isCopyleft_permissiveLicense_returnsFalse() {
        assertThat(service.isCopyleft("MIT")).isFalse();
        assertThat(service.isCopyleft("Apache-2.0")).isFalse();
        assertThat(service.isCopyleft("BSD-2-Clause")).isFalse();
        assertThat(service.isCopyleft("ISC")).isFalse();
        assertThat(service.isCopyleft("EPL-2.0")).isFalse();
    }

    @Test
    void isCopyleft_nullLicense_returnsFalse() {
        assertThat(service.isCopyleft(null)).isFalse();
    }

    // ========== npm parsing ==========

    @Test
    void parseNpmDependencyNames_extractsDependencies() {
        String packageJson = """
                {
                    "name": "test",
                    "dependencies": {
                        "@angular/core": "^17.0.0",
                        "lodash": "^4.17.21"
                    },
                    "devDependencies": {
                        "typescript": "^5.0.0"
                    }
                }
                """;

        Set<String> deps = service.parseNpmDependencyNames(packageJson, "dependencies");
        assertThat(deps).containsExactlyInAnyOrder("@angular/core", "lodash");

        Set<String> devDeps = service.parseNpmDependencyNames(packageJson, "devDependencies");
        assertThat(devDeps).containsExactly("typescript");
    }

    @Test
    void parseNpmDependencyNames_noSection_returnsEmpty() {
        String packageJson = """
                {
                    "name": "test"
                }
                """;

        Set<String> deps = service.parseNpmDependencyNames(packageJson, "dependencies");
        assertThat(deps).isEmpty();
    }

    @Test
    void extractNpmImports_extractsEsModuleImports() {
        String content = """
                import { Component } from '@angular/core';
                import * as _ from 'lodash';
                import moment from 'moment';
                import { readFileSync } from 'fs';
                import './local-file';
                """;

        Set<String> packages = new java.util.HashSet<>();
        service.extractNpmImports(content, packages);

        assertThat(packages).contains("@angular/core", "lodash", "moment", "fs");
        // Local imports (starting with ./) should NOT be included
        assertThat(packages).doesNotContain("./local-file");
    }

    @Test
    void extractNpmImports_extractsRequireCalls() {
        String content = """
                const fs = require('fs');
                const path = require('path');
                const _ = require('lodash');
                const local = require('./local');
                """;

        Set<String> packages = new java.util.HashSet<>();
        service.extractNpmImports(content, packages);

        assertThat(packages).contains("fs", "path", "lodash");
        assertThat(packages).doesNotContain("./local");
    }

    @Test
    void extractNpmLicense_returnsLicenseField() {
        String packageJson = """
                {
                    "name": "test-package",
                    "version": "1.0.0",
                    "license": "MIT"
                }
                """;

        String license = service.extractNpmLicense(packageJson);
        assertThat(license).isEqualTo("MIT");
    }

    @Test
    void extractNpmLicense_noLicenseField_returnsNull() {
        String packageJson = """
                {
                    "name": "test-package",
                    "version": "1.0.0"
                }
                """;

        String license = service.extractNpmLicense(packageJson);
        assertThat(license).isNull();
    }

    // ========== guessMavenLicense ==========

    @Test
    void guessMavenLicense_knownGroupIds_returnsCorrectLicense() {
        assertThat(service.guessMavenLicense("org.apache.commons", "commons-lang3")).isEqualTo("Apache-2.0");
        assertThat(service.guessMavenLicense("org.springframework", "spring-core")).isEqualTo("Apache-2.0");
        assertThat(service.guessMavenLicense("org.slf4j", "slf4j-api")).isEqualTo("MIT");
        assertThat(service.guessMavenLicense("mysql", "mysql-connector-java")).isEqualTo("GPL-2.0");
        assertThat(service.guessMavenLicense("ch.qos.logback", "logback-classic")).isEqualTo("LGPL-2.1");
        assertThat(service.guessMavenLicense("org.hibernate", "hibernate-core")).isEqualTo("LGPL-2.1");
    }

    @Test
    void guessMavenLicense_unknownGroupId_returnsNull() {
        assertThat(service.guessMavenLicense("com.unknown.vendor", "mystery")).isNull();
    }

    // ========== npm license audit with node_modules ==========

    @Test
    @SuppressWarnings("unchecked")
    void licenseAudit_npmWithNodeModules_readsLicenseFromPackageJson() throws Exception {
        String packageJson = """
                {
                    "name": "test-project",
                    "dependencies": {
                        "safe-lib": "^1.0.0",
                        "gpl-lib": "^2.0.0"
                    }
                }
                """;
        Files.writeString(tempDir.resolve("package.json"), packageJson);

        // Create node_modules structure
        Path safeLibDir = tempDir.resolve("node_modules/safe-lib");
        Files.createDirectories(safeLibDir);
        Files.writeString(safeLibDir.resolve("package.json"), """
                {
                    "name": "safe-lib",
                    "version": "1.0.0",
                    "license": "MIT"
                }
                """);

        Path gplLibDir = tempDir.resolve("node_modules/gpl-lib");
        Files.createDirectories(gplLibDir);
        Files.writeString(gplLibDir.resolve("package.json"), """
                {
                    "name": "gpl-lib",
                    "version": "2.0.0",
                    "license": "GPL-3.0"
                }
                """);

        Map<String, Object> result = service.licenseAudit(tempDir.toString(), "npm");

        assertThat(result.get("projectType")).isEqualTo("npm");
        assertThat((int) result.get("totalChecked")).isEqualTo(2);
        assertThat((int) result.get("copyleftCount")).isEqualTo(1);

        List<Map<String, String>> warnings = (List<Map<String, String>>) result.get("warnings");
        assertThat(warnings).hasSize(1);
        assertThat(warnings.get(0).get("name")).isEqualTo("gpl-lib");
        assertThat(warnings.get(0).get("license")).isEqualTo("GPL-3.0");

        Map<String, List<Map<String, String>>> licenseGroups =
                (Map<String, List<Map<String, String>>>) result.get("licenseGroups");
        assertThat(licenseGroups).containsKey("MIT");
        assertThat(licenseGroups).containsKey("GPL-3.0");
    }

    @Test
    @SuppressWarnings("unchecked")
    void licenseAudit_npmMissingNodeModule_addedToNotFound() throws Exception {
        String packageJson = """
                {
                    "name": "test-project",
                    "dependencies": {
                        "missing-lib": "^1.0.0"
                    }
                }
                """;
        Files.writeString(tempDir.resolve("package.json"), packageJson);
        Files.createDirectories(tempDir.resolve("node_modules"));

        Map<String, Object> result = service.licenseAudit(tempDir.toString(), "npm");

        List<String> notFound = (List<String>) result.get("notFound");
        assertThat(notFound).contains("missing-lib");
    }

    // ========== parseNpmAuditOutput ==========

    @Test
    @SuppressWarnings("unchecked")
    void parseNpmAuditOutput_countsCorrectly() {
        String auditOutput = """
                {
                    "advisories": {
                        "1": {"severity":"critical","title":"RCE"},
                        "2": {"severity":"high","title":"XSS"},
                        "3": {"severity":"moderate","title":"DOS"},
                        "4": {"severity":"low","title":"Info leak"},
                        "5": {"severity":"critical","title":"RCE2"}
                    }
                }
                """;

        Map<String, Object> result = service.parseNpmAuditOutput(auditOutput, "/test/project");

        assertThat((int) result.get("totalVulnerabilities")).isEqualTo(5);

        Map<String, Object> bySeverity = (Map<String, Object>) result.get("bySeverity");
        assertThat((int) bySeverity.get("critical")).isEqualTo(2);
        assertThat((int) bySeverity.get("high")).isEqualTo(1);
        assertThat((int) bySeverity.get("medium")).isEqualTo(1);
        assertThat((int) bySeverity.get("low")).isEqualTo(1);
    }
}
