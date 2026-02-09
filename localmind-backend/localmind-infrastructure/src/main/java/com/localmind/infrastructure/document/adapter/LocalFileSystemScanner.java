package com.localmind.infrastructure.document.adapter;

import com.localmind.domain.document.port.out.FileSystemScannerPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

@Slf4j
@Component
public class LocalFileSystemScanner implements FileSystemScannerPort {

    @Override
    public List<FileInfo> scan(String directoryPath, boolean recursive) {
        List<FileInfo> files = new ArrayList<>();
        Path root = Paths.get(directoryPath);

        if (!Files.isDirectory(root)) {
            log.warn("Path is not a directory: {}", directoryPath);
            return files;
        }

        try {
            int maxDepth = recursive ? Integer.MAX_VALUE : 1;
            Files.walkFileTree(root, java.util.EnumSet.noneOf(FileVisitOption.class), maxDepth,
                    new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            try {
                                String mimeType = Files.probeContentType(file);
                                long size = attrs.size();
                                String hash = computeHash(file);
                                files.add(new FileInfo(file, mimeType, size, hash));
                            } catch (Exception e) {
                                log.warn("Error processing file {}: {}", file, e.getMessage());
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
        } catch (IOException e) {
            log.error("Error scanning directory {}: {}", directoryPath, e.getMessage());
        }

        return files;
    }

    private String computeHash(Path file) {
        try (InputStream is = Files.newInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (Exception e) {
            log.warn("Could not compute hash for {}: {}", file, e.getMessage());
            return null;
        }
    }
}
