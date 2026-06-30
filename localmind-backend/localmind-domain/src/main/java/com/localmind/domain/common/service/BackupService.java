package com.localmind.domain.common.service;

import com.localmind.domain.common.exception.LocalMindException;
import com.localmind.domain.common.model.BackupComponent;
import com.localmind.domain.common.model.BackupInfo;
import com.localmind.domain.common.port.in.BackupUseCase;
import com.localmind.domain.common.port.out.BackupHistoryPort;
import com.localmind.domain.common.port.out.BackupStoragePort;
import com.localmind.domain.common.port.out.ConfigExportPort;
import com.localmind.domain.common.port.out.DatabaseDumpPort;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Domain service implementing backup and restore operations.
 * Creates ZIP archives containing database dumps, provider configs, and document metadata.
 * No Spring annotations - registered as a bean via DomainConfig.
 */
public class BackupService implements BackupUseCase {

    private static final String MANIFEST_VERSION = "1.0";
    private static final String DATABASE_ENTRY = "database/localmind.sql";
    private static final String CONFIG_ENTRY = "config/providers.json";
    private static final String METADATA_ENTRY = "metadata/documents.json";
    private static final String MANIFEST_ENTRY = "manifest.json";

    private static final DateTimeFormatter FILENAME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss").withZone(ZoneId.systemDefault());

    private final BackupStoragePort backupStoragePort;
    private final DatabaseDumpPort databaseDumpPort;
    private final BackupHistoryPort backupHistoryPort;
    private final ConfigExportPort configExportPort;

    public BackupService(BackupStoragePort backupStoragePort,
                         DatabaseDumpPort databaseDumpPort,
                         BackupHistoryPort backupHistoryPort,
                         ConfigExportPort configExportPort) {
        this.backupStoragePort = backupStoragePort;
        this.databaseDumpPort = databaseDumpPort;
        this.backupHistoryPort = backupHistoryPort;
        this.configExportPort = configExportPort;
    }

    @Override
    public BackupInfo createBackup(Set<BackupComponent> components) {
        if (components == null || components.isEmpty()) {
            throw new LocalMindException("At least one backup component must be specified");
        }

        Instant now = Instant.now();
        String timestamp = FILENAME_FORMATTER.format(now);
        String filename = "backup-" + timestamp + ".zip";

        byte[] zipBytes = buildZipArchive(components, now);

        backupStoragePort.save(filename, zipBytes);

        BackupInfo backupInfo = BackupInfo.builder()
                .id(UUID.randomUUID().toString())
                .filename(filename)
                .createdAt(now)
                .sizeBytes(zipBytes.length)
                .backupType("manual")
                .components(components)
                .build();

        return backupHistoryPort.save(backupInfo);
    }

    @Override
    public List<BackupInfo> listBackups() {
        return backupHistoryPort.findAll();
    }

    @Override
    public byte[] downloadBackup(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new LocalMindException("Backup filename is required");
        }
        return backupStoragePort.load(filename);
    }

    @Override
    public void restoreBackup(InputStream data) {
        if (data == null) {
            throw new LocalMindException("Backup data is required");
        }

        try {
            byte[] allBytes = readAllBytes(data);
            restoreFromZip(allBytes);
        } catch (LocalMindException e) {
            throw e;
        } catch (Exception e) {
            throw new LocalMindException("Failed to restore backup: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteBackup(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new LocalMindException("Backup filename is required");
        }
        backupStoragePort.delete(filename);
        backupHistoryPort.deleteByFilename(filename);
    }

    // ========== Private helpers ==========

    private byte[] buildZipArchive(Set<BackupComponent> components, Instant createdAt) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            if (components.contains(BackupComponent.DATABASE)) {
                byte[] dumpData = databaseDumpPort.dumpDatabase();
                addZipEntry(zos, DATABASE_ENTRY, dumpData);
            }

            if (components.contains(BackupComponent.CONFIGURATION)) {
                byte[] configData = configExportPort.exportProviderConfigs();
                addZipEntry(zos, CONFIG_ENTRY, configData);
            }

            if (components.contains(BackupComponent.DOCUMENTS_METADATA)) {
                byte[] metadataData = configExportPort.exportDocumentMetadata();
                addZipEntry(zos, METADATA_ENTRY, metadataData);
            }

            String manifestJson = buildManifestJson(components, createdAt);
            addZipEntry(zos, MANIFEST_ENTRY, manifestJson.getBytes(StandardCharsets.UTF_8));

            zos.finish();
            return baos.toByteArray();

        } catch (IOException e) {
            throw new LocalMindException("Failed to create backup archive: " + e.getMessage(), e);
        }
    }

    private void addZipEntry(ZipOutputStream zos, String entryName, byte[] data) throws IOException {
        ZipEntry entry = new ZipEntry(entryName);
        entry.setSize(data.length);
        zos.putNextEntry(entry);
        zos.write(data);
        zos.closeEntry();
    }

    private String buildManifestJson(Set<BackupComponent> components, Instant createdAt) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"version\": \"").append(MANIFEST_VERSION).append("\",\n");
        sb.append("  \"createdAt\": \"").append(createdAt.toString()).append("\",\n");
        sb.append("  \"components\": [");

        boolean first = true;
        for (BackupComponent component : components) {
            if (!first) {
                sb.append(", ");
            }
            sb.append("\"").append(component.name()).append("\"");
            first = false;
        }

        sb.append("]\n");
        sb.append("}");
        return sb.toString();
    }

    private void restoreFromZip(byte[] zipBytes) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(zipBytes);
             ZipInputStream zis = new ZipInputStream(bais)) {

            ZipEntry entry;
            boolean restoredSomething = false;

            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();
                byte[] entryData = readZipEntryBytes(zis);

                if (DATABASE_ENTRY.equals(entryName)) {
                    databaseDumpPort.restoreDatabase(entryData);
                    restoredSomething = true;
                } else if (CONFIG_ENTRY.equals(entryName)) {
                    configExportPort.importProviderConfigs(entryData);
                    restoredSomething = true;
                }
                // manifest.json and metadata/documents.json are informational only during restore

                zis.closeEntry();
            }

            if (!restoredSomething) {
                throw new LocalMindException("No restorable components found in backup archive");
            }

        } catch (LocalMindException e) {
            throw e;
        } catch (IOException e) {
            throw new LocalMindException("Failed to read backup archive: " + e.getMessage(), e);
        }
    }

    private byte[] readZipEntryBytes(ZipInputStream zis) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = zis.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        return baos.toByteArray();
    }

    private byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        return baos.toByteArray();
    }
}
