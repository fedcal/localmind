package com.localmind.domain.document.service;

import com.localmind.domain.common.exception.DocumentProcessingException;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathValidationService {

    public String validateAndNormalize(String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            throw new DocumentProcessingException("Path cannot be null or empty");
        }

        String trimmed = rawPath.trim();

        try {
            Path path = Paths.get(trimmed).toAbsolutePath().normalize();

            if (!path.isAbsolute()) {
                throw new DocumentProcessingException("Path must be absolute: " + trimmed);
            }

            if (!Files.exists(path)) {
                throw new DocumentProcessingException("Path does not exist: " + path);
            }

            if (!Files.isDirectory(path)) {
                throw new DocumentProcessingException("Path is not a directory: " + path);
            }

            if (!Files.isReadable(path)) {
                throw new DocumentProcessingException("Path is not readable: " + path);
            }

            return path.toString();
        } catch (InvalidPathException e) {
            throw new DocumentProcessingException("Invalid path format: " + trimmed, e);
        }
    }

    public boolean isAbsolutePath(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        try {
            return Paths.get(path.trim()).isAbsolute();
        } catch (InvalidPathException e) {
            return false;
        }
    }
}
