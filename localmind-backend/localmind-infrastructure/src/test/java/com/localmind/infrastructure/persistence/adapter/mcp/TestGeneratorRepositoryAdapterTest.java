package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.GeneratedTest;
import com.localmind.infrastructure.persistence.entity.mcp.GeneratedTestEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaGeneratedTestRepository;
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
class TestGeneratorRepositoryAdapterTest {

    @Mock
    private JpaGeneratedTestRepository jpaRepository;

    private TestGeneratorRepositoryAdapter adapter;

    private final UUID TEST_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final Instant NOW = Instant.parse("2026-02-12T10:00:00Z");

    @BeforeEach
    void setUp() {
        adapter = new TestGeneratorRepositoryAdapter(jpaRepository);
    }

    @Test
    void save_mapsAllFieldsCorrectlyToEntity() {
        GeneratedTest domain = GeneratedTest.builder()
                .id(TEST_UUID.toString())
                .sourceFilePath("/src/main/java/Example.java")
                .language("java")
                .framework("junit")
                .functionsFound(5)
                .testCode("@Test void test() {}")
                .createdAt(NOW)
                .build();

        GeneratedTestEntity savedEntity = GeneratedTestEntity.builder()
                .id(TEST_UUID)
                .sourceFilePath("/src/main/java/Example.java")
                .language("java")
                .framework("junit")
                .functionsFound(5)
                .testCode("@Test void test() {}")
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(GeneratedTestEntity.class))).thenReturn(savedEntity);

        GeneratedTest result = adapter.save(domain);

        ArgumentCaptor<GeneratedTestEntity> captor = ArgumentCaptor.forClass(GeneratedTestEntity.class);
        verify(jpaRepository).save(captor.capture());

        GeneratedTestEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getSourceFilePath()).isEqualTo("/src/main/java/Example.java");
        assertThat(captured.getLanguage()).isEqualTo("java");
        assertThat(captured.getFramework()).isEqualTo("junit");
        assertThat(captured.getFunctionsFound()).isEqualTo(5);
        assertThat(captured.getTestCode()).isEqualTo("@Test void test() {}");
        assertThat(captured.getCreatedAt()).isEqualTo(NOW);

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getSourceFilePath()).isEqualTo("/src/main/java/Example.java");
        assertThat(result.getLanguage()).isEqualTo("java");
        assertThat(result.getFramework()).isEqualTo("junit");
        assertThat(result.getFunctionsFound()).isEqualTo(5);
        assertThat(result.getTestCode()).isEqualTo("@Test void test() {}");
        assertThat(result.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void save_withNullSourceFilePath_mapsCorrectly() {
        GeneratedTest domain = GeneratedTest.builder()
                .id(TEST_UUID.toString())
                .sourceFilePath(null)
                .language("python")
                .framework("pytest")
                .functionsFound(0)
                .testCode(null)
                .createdAt(NOW)
                .build();

        GeneratedTestEntity savedEntity = GeneratedTestEntity.builder()
                .id(TEST_UUID)
                .sourceFilePath(null)
                .language("python")
                .framework("pytest")
                .functionsFound(0)
                .testCode(null)
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(GeneratedTestEntity.class))).thenReturn(savedEntity);

        GeneratedTest result = adapter.save(domain);

        ArgumentCaptor<GeneratedTestEntity> captor = ArgumentCaptor.forClass(GeneratedTestEntity.class);
        verify(jpaRepository).save(captor.capture());

        GeneratedTestEntity captured = captor.getValue();
        assertThat(captured.getSourceFilePath()).isNull();
        assertThat(captured.getTestCode()).isNull();
        assertThat(captured.getFunctionsFound()).isEqualTo(0);

        assertThat(result.getSourceFilePath()).isNull();
        assertThat(result.getTestCode()).isNull();
    }

    @Test
    void save_withNullId_doesNotSetEntityId() {
        GeneratedTest domain = GeneratedTest.builder()
                .id(null)
                .language("typescript")
                .framework("vitest")
                .functionsFound(3)
                .testCode("describe('test', () => {});")
                .createdAt(NOW)
                .build();

        UUID generatedUuid = UUID.fromString("11111111-2222-3333-4444-555555555555");
        GeneratedTestEntity savedEntity = GeneratedTestEntity.builder()
                .id(generatedUuid)
                .language("typescript")
                .framework("vitest")
                .functionsFound(3)
                .testCode("describe('test', () => {});")
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(GeneratedTestEntity.class))).thenReturn(savedEntity);

        GeneratedTest result = adapter.save(domain);

        ArgumentCaptor<GeneratedTestEntity> captor = ArgumentCaptor.forClass(GeneratedTestEntity.class);
        verify(jpaRepository).save(captor.capture());

        GeneratedTestEntity captured = captor.getValue();
        assertThat(captured.getId()).isNull(); // Non impostato perche' domain.id era null

        assertThat(result.getId()).isEqualTo(generatedUuid.toString());
        assertThat(result.getLanguage()).isEqualTo("typescript");
        assertThat(result.getFramework()).isEqualTo("vitest");
    }
}
