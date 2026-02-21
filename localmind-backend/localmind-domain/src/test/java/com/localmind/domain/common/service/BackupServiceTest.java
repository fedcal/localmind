package com.localmind.domain.common.service;

import com.localmind.domain.common.exception.LocalMindException;
import com.localmind.domain.common.model.BackupComponent;
import com.localmind.domain.common.model.BackupInfo;
import com.localmind.domain.common.port.out.BackupHistoryPort;
import com.localmind.domain.common.port.out.BackupStoragePort;
import com.localmind.domain.common.port.out.ConfigExportPort;
import com.localmind.domain.common.port.out.DatabaseDumpPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BackupServiceTest {

    @Mock
    private BackupStoragePort backupStoragePort;

    @Mock
    private DatabaseDumpPort databaseDumpPort;

    @Mock
    private BackupHistoryPort backupHistoryPort;

    @Mock
    private ConfigExportPort configExportPort;

    @InjectMocks
    private BackupService backupService;

    @Test
    void createBackup_full_shouldCallAllPortsAndSave() {
        // given
        Set<BackupComponent> components = EnumSet.allOf(BackupComponent.class);

        byte[] dbDump = "-- SQL dump".getBytes(StandardCharsets.UTF_8);
        byte[] configData = "[{\"name\":\"ollama\"}]".getBytes(StandardCharsets.UTF_8);
        byte[] metadataData = "[{\"filename\":\"doc.pdf\"}]".getBytes(StandardCharsets.UTF_8);

        when(databaseDumpPort.dumpDatabase()).thenReturn(dbDump);
        when(configExportPort.exportProviderConfigs()).thenReturn(configData);
        when(configExportPort.exportDocumentMetadata()).thenReturn(metadataData);
        when(backupHistoryPort.save(any(BackupInfo.class))).thenAnswer(invocation -> {
            BackupInfo info = invocation.getArgument(0);
            return BackupInfo.builder()
                    .id(info.getId())
                    .filename(info.getFilename())
                    .createdAt(info.getCreatedAt())
                    .sizeBytes(info.getSizeBytes())
                    .backupType(info.getBackupType())
                    .components(info.getComponents())
                    .build();
        });

        // when
        BackupInfo result = backupService.createBackup(components);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getFilename()).startsWith("backup-").endsWith(".zip");
        assertThat(result.getBackupType()).isEqualTo("manual");
        assertThat(result.getSizeBytes()).isGreaterThan(0);

        verify(databaseDumpPort).dumpDatabase();
        verify(configExportPort).exportProviderConfigs();
        verify(configExportPort).exportDocumentMetadata();

        ArgumentCaptor<byte[]> dataCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(backupStoragePort).save(anyString(), dataCaptor.capture());
        assertThat(dataCaptor.getValue()).isNotEmpty();

        verify(backupHistoryPort).save(any(BackupInfo.class));
    }

    @Test
    void createBackup_databaseOnly_shouldOnlyCallDatabaseDump() {
        // given
        Set<BackupComponent> components = EnumSet.of(BackupComponent.DATABASE);

        byte[] dbDump = "-- SQL dump".getBytes(StandardCharsets.UTF_8);
        when(databaseDumpPort.dumpDatabase()).thenReturn(dbDump);
        when(backupHistoryPort.save(any(BackupInfo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        BackupInfo result = backupService.createBackup(components);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getFilename()).startsWith("backup-");

        verify(databaseDumpPort).dumpDatabase();
        verify(configExportPort, never()).exportProviderConfigs();
        verify(configExportPort, never()).exportDocumentMetadata();
        verify(backupStoragePort).save(anyString(), any(byte[].class));
        verify(backupHistoryPort).save(any(BackupInfo.class));
    }

    @Test
    void createBackup_emptyComponents_shouldThrow() {
        assertThatThrownBy(() -> backupService.createBackup(Set.of()))
                .isInstanceOf(LocalMindException.class)
                .hasMessageContaining("At least one backup component");
    }

    @Test
    void createBackup_nullComponents_shouldThrow() {
        assertThatThrownBy(() -> backupService.createBackup(null))
                .isInstanceOf(LocalMindException.class)
                .hasMessageContaining("At least one backup component");
    }

    @Test
    void listBackups_shouldDelegateToHistoryPort() {
        // given
        List<BackupInfo> expected = List.of(
                BackupInfo.builder()
                        .id("id-1")
                        .filename("backup-2026-01-01.zip")
                        .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
                        .sizeBytes(1024)
                        .backupType("manual")
                        .components(EnumSet.of(BackupComponent.DATABASE))
                        .build(),
                BackupInfo.builder()
                        .id("id-2")
                        .filename("backup-2026-01-02.zip")
                        .createdAt(Instant.parse("2026-01-02T00:00:00Z"))
                        .sizeBytes(2048)
                        .backupType("manual")
                        .components(EnumSet.allOf(BackupComponent.class))
                        .build()
        );
        when(backupHistoryPort.findAll()).thenReturn(expected);

        // when
        List<BackupInfo> result = backupService.listBackups();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFilename()).isEqualTo("backup-2026-01-01.zip");
        assertThat(result.get(1).getFilename()).isEqualTo("backup-2026-01-02.zip");
        verify(backupHistoryPort).findAll();
    }

    @Test
    void downloadBackup_shouldDelegateToStoragePort() {
        // given
        byte[] expectedData = "zip content".getBytes(StandardCharsets.UTF_8);
        when(backupStoragePort.load("backup-test.zip")).thenReturn(expectedData);

        // when
        byte[] result = backupService.downloadBackup("backup-test.zip");

        // then
        assertThat(result).isEqualTo(expectedData);
        verify(backupStoragePort).load("backup-test.zip");
    }

    @Test
    void downloadBackup_nullFilename_shouldThrow() {
        assertThatThrownBy(() -> backupService.downloadBackup(null))
                .isInstanceOf(LocalMindException.class)
                .hasMessageContaining("filename is required");
    }

    @Test
    void downloadBackup_blankFilename_shouldThrow() {
        assertThatThrownBy(() -> backupService.downloadBackup("   "))
                .isInstanceOf(LocalMindException.class)
                .hasMessageContaining("filename is required");
    }

    @Test
    void deleteBackup_shouldCallBothStorageAndHistory() {
        // when
        backupService.deleteBackup("backup-to-delete.zip");

        // then
        verify(backupStoragePort).delete("backup-to-delete.zip");
        verify(backupHistoryPort).deleteByFilename("backup-to-delete.zip");
    }

    @Test
    void deleteBackup_nullFilename_shouldThrow() {
        assertThatThrownBy(() -> backupService.deleteBackup(null))
                .isInstanceOf(LocalMindException.class)
                .hasMessageContaining("filename is required");
    }

    @Test
    void restoreBackup_withValidZip_shouldRestoreDatabaseAndConfig() throws IOException {
        // given
        byte[] zipBytes = createTestZip(true, true);
        InputStream inputStream = new ByteArrayInputStream(zipBytes);

        // when
        backupService.restoreBackup(inputStream);

        // then
        verify(databaseDumpPort).restoreDatabase(any(byte[].class));
        verify(configExportPort).importProviderConfigs(any(byte[].class));
    }

    @Test
    void restoreBackup_withDatabaseOnlyZip_shouldRestoreOnlyDatabase() throws IOException {
        // given
        byte[] zipBytes = createTestZip(true, false);
        InputStream inputStream = new ByteArrayInputStream(zipBytes);

        // when
        backupService.restoreBackup(inputStream);

        // then
        verify(databaseDumpPort).restoreDatabase(any(byte[].class));
        verify(configExportPort, never()).importProviderConfigs(any(byte[].class));
    }

    @Test
    void restoreBackup_nullData_shouldThrow() {
        assertThatThrownBy(() -> backupService.restoreBackup(null))
                .isInstanceOf(LocalMindException.class)
                .hasMessageContaining("Backup data is required");
    }

    @Test
    void restoreBackup_emptyZip_shouldThrow() throws IOException {
        // given - a ZIP with only manifest, no restorable components
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry manifestEntry = new ZipEntry("manifest.json");
            zos.putNextEntry(manifestEntry);
            zos.write("{\"version\":\"1.0\"}".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
            zos.finish();
        }
        InputStream inputStream = new ByteArrayInputStream(baos.toByteArray());

        // when/then
        assertThatThrownBy(() -> backupService.restoreBackup(inputStream))
                .isInstanceOf(LocalMindException.class)
                .hasMessageContaining("No restorable components");
    }

    // ========== Helper methods ==========

    private byte[] createTestZip(boolean includeDatabase, boolean includeConfig) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            // Manifest
            ZipEntry manifestEntry = new ZipEntry("manifest.json");
            zos.putNextEntry(manifestEntry);
            String manifest = "{\"version\":\"1.0\",\"createdAt\":\"2026-01-01T00:00:00Z\",\"components\":[\"DATABASE\"]}";
            zos.write(manifest.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            if (includeDatabase) {
                ZipEntry dbEntry = new ZipEntry("database/localmind.sql");
                zos.putNextEntry(dbEntry);
                zos.write("-- SQL dump data".getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }

            if (includeConfig) {
                ZipEntry configEntry = new ZipEntry("config/providers.json");
                zos.putNextEntry(configEntry);
                zos.write("[{\"name\":\"ollama\",\"type\":\"OLLAMA\"}]".getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }

            zos.finish();
        }
        return baos.toByteArray();
    }
}
