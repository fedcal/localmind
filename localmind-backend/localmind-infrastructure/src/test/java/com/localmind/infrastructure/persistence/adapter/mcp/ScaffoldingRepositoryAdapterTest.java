package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.ScaffoldedProject;
import com.localmind.infrastructure.persistence.entity.mcp.ScaffoldedProjectEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaScaffoldedProjectRepository;
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
class ScaffoldingRepositoryAdapterTest {

    @Mock
    private JpaScaffoldedProjectRepository jpaRepository;

    private ScaffoldingRepositoryAdapter adapter;

    private final UUID TEST_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final Instant NOW = Instant.parse("2026-02-12T10:00:00Z");

    @BeforeEach
    void setUp() {
        adapter = new ScaffoldingRepositoryAdapter(jpaRepository);
    }

    @Test
    void save_mapsFromDomainToEntityCorrectly() {
        ScaffoldedProject domain = ScaffoldedProject.builder()
                .id(TEST_UUID.toString())
                .templateName("spring-boot-api")
                .projectName("my-app")
                .outputPath("/tmp/my-app")
                .filesGenerated(4)
                .createdAt(NOW)
                .build();

        ScaffoldedProjectEntity savedEntity = ScaffoldedProjectEntity.builder()
                .id(TEST_UUID)
                .templateName("spring-boot-api")
                .projectName("my-app")
                .outputPath("/tmp/my-app")
                .filesGenerated(4)
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(ScaffoldedProjectEntity.class))).thenReturn(savedEntity);

        adapter.save(domain);

        ArgumentCaptor<ScaffoldedProjectEntity> captor = ArgumentCaptor.forClass(ScaffoldedProjectEntity.class);
        verify(jpaRepository).save(captor.capture());

        ScaffoldedProjectEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getTemplateName()).isEqualTo("spring-boot-api");
        assertThat(captured.getProjectName()).isEqualTo("my-app");
        assertThat(captured.getOutputPath()).isEqualTo("/tmp/my-app");
        assertThat(captured.getFilesGenerated()).isEqualTo(4);
        assertThat(captured.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void save_mapsFromEntityToDomainCorrectly() {
        ScaffoldedProject domain = ScaffoldedProject.builder()
                .id(TEST_UUID.toString())
                .templateName("angular-app")
                .projectName("frontend")
                .outputPath("/projects/frontend")
                .filesGenerated(4)
                .createdAt(NOW)
                .build();

        ScaffoldedProjectEntity savedEntity = ScaffoldedProjectEntity.builder()
                .id(TEST_UUID)
                .templateName("angular-app")
                .projectName("frontend")
                .outputPath("/projects/frontend")
                .filesGenerated(4)
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(ScaffoldedProjectEntity.class))).thenReturn(savedEntity);

        ScaffoldedProject result = adapter.save(domain);

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getTemplateName()).isEqualTo("angular-app");
        assertThat(result.getProjectName()).isEqualTo("frontend");
        assertThat(result.getOutputPath()).isEqualTo("/projects/frontend");
        assertThat(result.getFilesGenerated()).isEqualTo(4);
        assertThat(result.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void save_withNullId_setsNullIdOnEntity() {
        ScaffoldedProject domain = ScaffoldedProject.builder()
                .templateName("react-app")
                .projectName("ui")
                .outputPath("/tmp/ui")
                .filesGenerated(4)
                .createdAt(NOW)
                .build();

        ScaffoldedProjectEntity savedEntity = ScaffoldedProjectEntity.builder()
                .id(TEST_UUID)
                .templateName("react-app")
                .projectName("ui")
                .outputPath("/tmp/ui")
                .filesGenerated(4)
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(ScaffoldedProjectEntity.class))).thenReturn(savedEntity);

        ScaffoldedProject result = adapter.save(domain);

        ArgumentCaptor<ScaffoldedProjectEntity> captor = ArgumentCaptor.forClass(ScaffoldedProjectEntity.class);
        verify(jpaRepository).save(captor.capture());

        ScaffoldedProjectEntity captured = captor.getValue();
        assertThat(captured.getId()).isNull();

        // Il risultato dal DB ha comunque un ID generato
        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
    }

    @Test
    void save_withNullOutputPath_mapsNullCorrectly() {
        ScaffoldedProject domain = ScaffoldedProject.builder()
                .id(TEST_UUID.toString())
                .templateName("mcp-server")
                .projectName("tool-server")
                .outputPath(null)
                .filesGenerated(3)
                .createdAt(NOW)
                .build();

        ScaffoldedProjectEntity savedEntity = ScaffoldedProjectEntity.builder()
                .id(TEST_UUID)
                .templateName("mcp-server")
                .projectName("tool-server")
                .outputPath(null)
                .filesGenerated(3)
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(ScaffoldedProjectEntity.class))).thenReturn(savedEntity);

        ScaffoldedProject result = adapter.save(domain);

        assertThat(result.getOutputPath()).isNull();

        ArgumentCaptor<ScaffoldedProjectEntity> captor = ArgumentCaptor.forClass(ScaffoldedProjectEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue().getOutputPath()).isNull();
    }
}
