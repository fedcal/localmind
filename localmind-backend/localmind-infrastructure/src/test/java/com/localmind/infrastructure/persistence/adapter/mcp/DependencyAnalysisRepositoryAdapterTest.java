package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.LicenseAudit;
import com.localmind.domain.mcp.model.VulnerabilityScan;
import com.localmind.infrastructure.persistence.entity.mcp.LicenseAuditEntity;
import com.localmind.infrastructure.persistence.entity.mcp.VulnerabilityScanEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaLicenseAuditRepository;
import com.localmind.infrastructure.persistence.repository.mcp.JpaVulnerabilityScanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DependencyAnalysisRepositoryAdapterTest {

    @Mock
    private JpaVulnerabilityScanRepository vulnRepository;

    @Mock
    private JpaLicenseAuditRepository licenseRepository;

    private DependencyAnalysisRepositoryAdapter adapter;

    private final UUID TEST_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final Instant NOW = Instant.parse("2026-02-12T10:00:00Z");

    @BeforeEach
    void setUp() {
        adapter = new DependencyAnalysisRepositoryAdapter(vulnRepository, licenseRepository);
    }

    // ========== saveVulnerabilityScan ==========

    @Test
    void saveVulnerabilityScan_mapsCorrectly() {
        VulnerabilityScan domain = VulnerabilityScan.builder()
                .id(TEST_UUID.toString())
                .projectPath("/path/to/project")
                .projectType("maven")
                .totalVulnerabilities(3)
                .resultJson("{\"totalVulnerabilities\":3}")
                .createdAt(NOW)
                .build();

        VulnerabilityScanEntity savedEntity = VulnerabilityScanEntity.builder()
                .id(TEST_UUID)
                .projectPath("/path/to/project")
                .projectType("maven")
                .totalVulnerabilities(3)
                .result("{\"totalVulnerabilities\":3}")
                .createdAt(NOW)
                .build();

        when(vulnRepository.save(any(VulnerabilityScanEntity.class))).thenReturn(savedEntity);

        VulnerabilityScan result = adapter.saveVulnerabilityScan(domain);

        ArgumentCaptor<VulnerabilityScanEntity> captor = ArgumentCaptor.forClass(VulnerabilityScanEntity.class);
        verify(vulnRepository).save(captor.capture());

        VulnerabilityScanEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getProjectPath()).isEqualTo("/path/to/project");
        assertThat(captured.getProjectType()).isEqualTo("maven");
        assertThat(captured.getTotalVulnerabilities()).isEqualTo(3);
        assertThat(captured.getResult()).isEqualTo("{\"totalVulnerabilities\":3}");
        assertThat(captured.getCreatedAt()).isEqualTo(NOW);

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getProjectPath()).isEqualTo("/path/to/project");
        assertThat(result.getProjectType()).isEqualTo("maven");
        assertThat(result.getTotalVulnerabilities()).isEqualTo(3);
        assertThat(result.getResultJson()).isEqualTo("{\"totalVulnerabilities\":3}");
        assertThat(result.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void saveVulnerabilityScan_withNullId_doesNotSetIdOnEntity() {
        VulnerabilityScan domain = VulnerabilityScan.builder()
                .id(null)
                .projectPath("/path/to/project")
                .projectType("npm")
                .totalVulnerabilities(0)
                .resultJson("{}")
                .createdAt(NOW)
                .build();

        VulnerabilityScanEntity savedEntity = VulnerabilityScanEntity.builder()
                .id(TEST_UUID)
                .projectPath("/path/to/project")
                .projectType("npm")
                .totalVulnerabilities(0)
                .result("{}")
                .createdAt(NOW)
                .build();

        when(vulnRepository.save(any(VulnerabilityScanEntity.class))).thenReturn(savedEntity);

        VulnerabilityScan result = adapter.saveVulnerabilityScan(domain);

        ArgumentCaptor<VulnerabilityScanEntity> captor = ArgumentCaptor.forClass(VulnerabilityScanEntity.class);
        verify(vulnRepository).save(captor.capture());

        assertThat(captor.getValue().getId()).isNull();
        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
    }

    // ========== saveLicenseAudit ==========

    @Test
    void saveLicenseAudit_mapsCorrectly() {
        LicenseAudit domain = LicenseAudit.builder()
                .id(TEST_UUID.toString())
                .projectPath("/path/to/project")
                .projectType("maven")
                .totalChecked(10)
                .copyleftCount(2)
                .resultJson("{\"copyleftCount\":2}")
                .createdAt(NOW)
                .build();

        LicenseAuditEntity savedEntity = LicenseAuditEntity.builder()
                .id(TEST_UUID)
                .projectPath("/path/to/project")
                .projectType("maven")
                .totalChecked(10)
                .copyleftCount(2)
                .result("{\"copyleftCount\":2}")
                .createdAt(NOW)
                .build();

        when(licenseRepository.save(any(LicenseAuditEntity.class))).thenReturn(savedEntity);

        LicenseAudit result = adapter.saveLicenseAudit(domain);

        ArgumentCaptor<LicenseAuditEntity> captor = ArgumentCaptor.forClass(LicenseAuditEntity.class);
        verify(licenseRepository).save(captor.capture());

        LicenseAuditEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getProjectPath()).isEqualTo("/path/to/project");
        assertThat(captured.getProjectType()).isEqualTo("maven");
        assertThat(captured.getTotalChecked()).isEqualTo(10);
        assertThat(captured.getCopyleftCount()).isEqualTo(2);
        assertThat(captured.getResult()).isEqualTo("{\"copyleftCount\":2}");
        assertThat(captured.getCreatedAt()).isEqualTo(NOW);

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getProjectPath()).isEqualTo("/path/to/project");
        assertThat(result.getProjectType()).isEqualTo("maven");
        assertThat(result.getTotalChecked()).isEqualTo(10);
        assertThat(result.getCopyleftCount()).isEqualTo(2);
        assertThat(result.getResultJson()).isEqualTo("{\"copyleftCount\":2}");
        assertThat(result.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void saveLicenseAudit_withNullId_doesNotSetIdOnEntity() {
        LicenseAudit domain = LicenseAudit.builder()
                .id(null)
                .projectPath("/path/to/npm-project")
                .projectType("npm")
                .totalChecked(5)
                .copyleftCount(0)
                .resultJson("{}")
                .createdAt(NOW)
                .build();

        LicenseAuditEntity savedEntity = LicenseAuditEntity.builder()
                .id(TEST_UUID)
                .projectPath("/path/to/npm-project")
                .projectType("npm")
                .totalChecked(5)
                .copyleftCount(0)
                .result("{}")
                .createdAt(NOW)
                .build();

        when(licenseRepository.save(any(LicenseAuditEntity.class))).thenReturn(savedEntity);

        LicenseAudit result = adapter.saveLicenseAudit(domain);

        ArgumentCaptor<LicenseAuditEntity> captor = ArgumentCaptor.forClass(LicenseAuditEntity.class);
        verify(licenseRepository).save(captor.capture());

        assertThat(captor.getValue().getId()).isNull();
        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getProjectType()).isEqualTo("npm");
    }

    // ========== saveUnusedAnalysis ==========

    @Test
    void saveUnusedAnalysis_delegatesToVulnRepository() {
        VulnerabilityScan domain = VulnerabilityScan.builder()
                .id(TEST_UUID.toString())
                .projectPath("/path/to/project")
                .projectType("maven-unused")
                .totalVulnerabilities(5)
                .resultJson("{\"totalUnused\":5}")
                .createdAt(NOW)
                .build();

        VulnerabilityScanEntity savedEntity = VulnerabilityScanEntity.builder()
                .id(TEST_UUID)
                .projectPath("/path/to/project")
                .projectType("maven-unused")
                .totalVulnerabilities(5)
                .result("{\"totalUnused\":5}")
                .createdAt(NOW)
                .build();

        when(vulnRepository.save(any(VulnerabilityScanEntity.class))).thenReturn(savedEntity);

        VulnerabilityScan result = adapter.saveUnusedAnalysis(domain);

        verify(vulnRepository).save(any(VulnerabilityScanEntity.class));
        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getProjectType()).isEqualTo("maven-unused");
        assertThat(result.getTotalVulnerabilities()).isEqualTo(5);
    }

    @Test
    void saveUnusedAnalysis_npmType_mapsCorrectly() {
        UUID uuid2 = UUID.fromString("22222222-2222-2222-2222-222222222222");

        VulnerabilityScan domain = VulnerabilityScan.builder()
                .id(uuid2.toString())
                .projectPath("/path/to/npm-project")
                .projectType("npm-unused")
                .totalVulnerabilities(2)
                .resultJson("{\"totalUnused\":2}")
                .createdAt(NOW)
                .build();

        VulnerabilityScanEntity savedEntity = VulnerabilityScanEntity.builder()
                .id(uuid2)
                .projectPath("/path/to/npm-project")
                .projectType("npm-unused")
                .totalVulnerabilities(2)
                .result("{\"totalUnused\":2}")
                .createdAt(NOW)
                .build();

        when(vulnRepository.save(any(VulnerabilityScanEntity.class))).thenReturn(savedEntity);

        VulnerabilityScan result = adapter.saveUnusedAnalysis(domain);

        ArgumentCaptor<VulnerabilityScanEntity> captor = ArgumentCaptor.forClass(VulnerabilityScanEntity.class);
        verify(vulnRepository).save(captor.capture());

        VulnerabilityScanEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(uuid2);
        assertThat(captured.getProjectPath()).isEqualTo("/path/to/npm-project");
        assertThat(captured.getProjectType()).isEqualTo("npm-unused");

        assertThat(result.getId()).isEqualTo(uuid2.toString());
        assertThat(result.getProjectType()).isEqualTo("npm-unused");
    }
}
