package com.localmind.domain.document.port.out;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public interface FileSystemScannerPort {

    record FileInfo(Path path, String mimeType, long size, String hash) {}

    List<FileInfo> scan(String directoryPath, boolean recursive);

    default InputStream openFile(Path path) throws IOException {
        return Files.newInputStream(path);
    }
}
