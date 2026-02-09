package com.localmind.domain.document.port.out;

import java.nio.file.Path;
import java.util.List;

public interface FileSystemScannerPort {

    record FileInfo(Path path, String mimeType, long size, String hash) {}

    List<FileInfo> scan(String directoryPath, boolean recursive);
}
