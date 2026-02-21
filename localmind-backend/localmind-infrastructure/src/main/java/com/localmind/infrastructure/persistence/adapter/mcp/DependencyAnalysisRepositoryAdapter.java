package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.LicenseAudit;
import com.localmind.domain.mcp.model.VulnerabilityScan;
import com.localmind.domain.mcp.port.out.DependencyAnalysisRepository;
import com.localmind.infrastructure.persistence.entity.mcp.LicenseAuditEntity;
import com.localmind.infrastructure.persistence.entity.mcp.VulnerabilityScanEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaLicenseAuditRepository;
import com.localmind.infrastructure.persistence.repository.mcp.JpaVulnerabilityScanRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Adapter bridging the domain DependencyAnalysisRepository port to JPA persistence.
 */
@Component
public class DependencyAnalysisRepositoryAdapter implements DependencyAnalysisRepository {

    private final JpaVulnerabilityScanRepository vulnRepository;
    private final JpaLicenseAuditRepository licenseRepository;

    public DependencyAnalysisRepositoryAdapter(JpaVulnerabilityScanRepository vulnRepository,
                                                JpaLicenseAuditRepository licenseRepository) {
        this.vulnRepository = vulnRepository;
        this.licenseRepository = licenseRepository;
    }

    @Override
    public VulnerabilityScan saveVulnerabilityScan(VulnerabilityScan scan) {
        VulnerabilityScanEntity entity = toVulnEntity(scan);
        VulnerabilityScanEntity saved = vulnRepository.save(entity);
        return toVulnDomain(saved);
    }

    @Override
    public LicenseAudit saveLicenseAudit(LicenseAudit audit) {
        LicenseAuditEntity entity = toLicenseEntity(audit);
        LicenseAuditEntity saved = licenseRepository.save(entity);
        return toLicenseDomain(saved);
    }

    @Override
    public VulnerabilityScan saveUnusedAnalysis(VulnerabilityScan scan) {
        VulnerabilityScanEntity entity = toVulnEntity(scan);
        VulnerabilityScanEntity saved = vulnRepository.save(entity);
        return toVulnDomain(saved);
    }

    // ========== VulnerabilityScan mapping ==========

    private VulnerabilityScanEntity toVulnEntity(VulnerabilityScan domain) {
        VulnerabilityScanEntity entity = VulnerabilityScanEntity.builder()
                .projectPath(domain.getProjectPath())
                .projectType(domain.getProjectType())
                .totalVulnerabilities(domain.getTotalVulnerabilities())
                .result(domain.getResultJson())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private VulnerabilityScan toVulnDomain(VulnerabilityScanEntity entity) {
        return VulnerabilityScan.builder()
                .id(entity.getId().toString())
                .projectPath(entity.getProjectPath())
                .projectType(entity.getProjectType())
                .totalVulnerabilities(entity.getTotalVulnerabilities())
                .resultJson(entity.getResult())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    // ========== LicenseAudit mapping ==========

    private LicenseAuditEntity toLicenseEntity(LicenseAudit domain) {
        LicenseAuditEntity entity = LicenseAuditEntity.builder()
                .projectPath(domain.getProjectPath())
                .projectType(domain.getProjectType())
                .totalChecked(domain.getTotalChecked())
                .copyleftCount(domain.getCopyleftCount())
                .result(domain.getResultJson())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }

        return entity;
    }

    private LicenseAudit toLicenseDomain(LicenseAuditEntity entity) {
        return LicenseAudit.builder()
                .id(entity.getId().toString())
                .projectPath(entity.getProjectPath())
                .projectType(entity.getProjectType())
                .totalChecked(entity.getTotalChecked())
                .copyleftCount(entity.getCopyleftCount())
                .resultJson(entity.getResult())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
